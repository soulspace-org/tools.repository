(ns org.soulspace.tools.repo
  (:require [clojure.string :as str]
            [clojure.set :as set]
            [clojure.walk :as walk]
            [clojure.spec.alpha :as s]
            [clojure.java.io :as io]
            [org.httpkit.client :as http]
            [org.soulspace.clj.string :as sstr]
            [org.soulspace.clj.namespace :as nsp]
            [org.soulspace.clj.file :as file]
            [org.soulspace.clj.property-replacement :as prop]
            [org.soulspace.tools.maven-xml :as mvnx]))

;;;
;;; version handling
;;;

(defn digits?
  "Tests if s contains only digits."
  [s]
  (if (empty? s)
    false
    (every? #(Character/isDigit %) s)))

(defn split-version
  "Splits a version string into revision components."
  ([version]
   (split-version version #"[.]"))
  ([version re]
   (if (empty? version)
     nil
     (str/split version re))))

(defn compare-revision
  "Compares two revision components c1 and c2."
  [c1 c2]
  ; compare numerically or lexically based on type
  (if (and (digits? c1) (digits? c2))
    (compare (Long/valueOf c1) (Long/valueOf c2))
    (compare c1 c2)))

(defn compare-version
  "Compares to versions v1 and v2."
  [v1 v2]
  (if (or (nil? v1) (nil? v2))
    (compare v1 v2)
    (loop [c1 (split-version v1)
           c2 (split-version v2)]
          ; split the versions and compare them part for part
      (if (and (seq c1) (seq c1))
        (if (not= (first c1) (first c2))
          (compare-revision (first c1) (first c2))
          (recur (rest c1) (rest c2)))
        (compare-revision (first c1) (first c2))))))

(defn lesser-version?
  "Returns true, if v1 is less than v2."
  [v1 v2]
  (< (compare-version v1 v2) 0.0))

(defn greater-version?
  "Returns true, if v1 is greater than v2."
  [v1 v2]
  (> (compare-version v1 v2) 0.0))

(defn same-version?
  "Returns true, if both versions are the same."
  [v1 v2]
  (= (compare-version v1 v2) 0))

(defn contains-version?
  "Returns true if the version v is contained in the version range given by from and to or as a map."
  ([r v]
   (contains-version? (:from r) (:to r) v))
  ([from to v]
   (cond
     (empty? v) false
     (and (empty? from) (empty? to)) true
     (and (empty? from) (lesser-version? v to)) true
     (and (empty? to) (not (lesser-version? v from))) true
     (and (not (lesser-version? v from)) (lesser-version? v to)) true
     :default false)))

; TODO version patterns and version pattern to version range conversion

;;;
;;; artifact adressing and comparing
;;;
;;
;; artifacts are described by maps containing at least a key :artifact-id
;; and optional keys :group-id and :version
;;
(s/def ::artifact (s/keys :req-un [::artifact-id]
                          :opt-un [::group-id ::version]))

(defn artifact-key
  "Returns a string key for the artifact a, disregarding a version."
  [a]
  (str (:group-id a) "/" (:artifact-id a)))

(defn artifact-version-key
  "Returns a string key for the artifact a including a version."
  [a]
  (str (artifact-key a) "[" (:version a) "]"))

(defn same-artifact?
  "Returns true, when the artifacts a1 and a2 have the same artifact key."
  [a1 a2]
  (= (artifact-key a1) (artifact-key a2)))

(defn same-artifact-version?
  "Returns true, when the artifacts a1 and a2 have the same artifact version key."
  [a1 a2]
  (= (artifact-version-key a1) (artifact-version-key a2)))

;;;
;;; repository handling
;;;
;;
;; single repository functions
;;
(s/def ::repository (s/keys :req-un [::id ::url ::name]
                            :opt-un [::username ::password ::releases ::snapshots ::layout]))

(def user-home (System/getProperty "user.home"))
(def local-repository (str user-home "/.m2/repository"))

(def hash-schemes #{"asc" "md5" "sha1"})

(defn artifact-relative-path
  "Returns the path to the artifact."
  [a]
  (str (nsp/ns-to-path (str (:group-id a))) "/" (:artifact-id a)))

(defn artifact-version-relative-path
  "Returns the path to the artifact."
  [a]
  (str (artifact-relative-path a) "/" (:version a)))

(defn artifact-local-path
  "Returns the path of the artifact in the local filesystem."
  ([a]
   (str local-repository "/" (artifact-relative-path a))))

(defn artifact-version-local-path
  "Returns the path of the artifact in the local filesystem."
  ([a]
   (str local-repository "/" (artifact-version-relative-path a))))

(defn artifact-filename
  "Returns the filename of the artifact in the repository.
   Takes classifier and extension as an optional parameter."
  ([a]
   (artifact-filename nil "jar" a))
  ([extension a]
   (artifact-filename nil extension a))
  ([classifier extension a]
   (str (:artifact-id a)
        "-" (:version a)
        (when classifier (str "-" classifier))
        "." extension)))

(defn artifact-url
  "Returns the URL of the artifact on the remote server.
   Takes classifier and extension as an optional parameter."
  ([repo a]
   (artifact-url repo "jar" a))
  ([repo extension a]
   (str (:url repo) "/" (artifact-version-relative-path a)
        "/" (artifact-filename extension a)))
  ([repo classifier extension a]
   (str (:url repo) "/" (artifact-version-relative-path a)
        "/" (artifact-filename classifier extension a))))

(defn artifact-metadata-url
  "Returns the URL to the maven metadata of the artifact."
  [repo a]
  (str (:url repo) "/" (nsp/ns-to-path (str (:group-id a)))
       "/" (:artifact-id a) "/maven-metadata.xml"))

(defn local-artifact?
  "Checks if the repository has the local artifact a.
   Takes classifier and extension as an optional parameter."
  ([a]
   (local-artifact? nil "jar" a))
  ([extension a]
   (local-artifact? nil extension a))
  ([classifier extension a]
   ;(file/is-dir? (io/as-file (artifact-version-local-path a)))
   (file/exists? (io/as-file (str (artifact-version-local-path a)
                                  "/" (artifact-filename classifier extension a))))))

(defn remote-artifact?
  "Checks if the repository has the remote artifact a.
   Takes classifier and extension as an optional parameter."
  ([repo a]
   (remote-artifact? repo nil "jar" a))
  ([repo extension a]
   (remote-artifact? repo nil extension a))
  ([repo classifier extension a]
   (= 200 (:status @(http/request {:url (artifact-url repo classifier extension a)
                                   :method :head
                                   :timeout 1000})))))

(defn download-artifact
  "Downloads the artifact a from the repository.
   Takes classifier and extension as an optional parameter."
  ([repo a]
   (download-artifact repo nil "jar" a))
  ([repo extension a]
   (download-artifact repo nil extension a))
  ([repo classifier extension a]
   (let [url (io/as-url (artifact-url repo classifier extension a))
         local-path (artifact-version-local-path a)]
     (when-not (file/exists? local-path) ; missing local directory
       (file/create-dir (io/as-file local-path))) ; create it
     (io/copy (io/input-stream url)
              (io/as-file (str local-path "/" (artifact-filename classifier extension a)))))))

(defn artifact-versions
  ""
  [repo a]
; TODO extract versions
  )

;;
;; repositories
;;

(def central {:id "central" :url "https://repo.maven.apache.org/maven2"
              :name "Central Repository" :snapshots {:enabled "false"}
              :layout "default"})
(def clojars {:id "clojars" :name "Clojars" :url "https://repo.clojars.org"
              :layout "default"})
(def repositories (atom [clojars central]))

;;
;; functions using the reopsitories list
;;

(defn add-repository
  "Adds a repository to the list of configured repositories."
  [repo]
  {:pre [(s/valid? ::repository repo)]}
  (swap! repositories conj repo))

(defn cache-artifact
  "Downloads and caches an artifact including it's associated pom."
  ([a]
   (cache-artifact nil "jar" a))
  ([extension a]
   (cache-artifact nil extension a))
  ([classifier extension a]
   ; (println "caching artifact" (artifact-filename classifier extension a))
   (loop [repos @repositories]
     (when-let [repo  (first repos)]
       (if (remote-artifact? repo classifier extension a)
         (do ; TODO download checksums and common classified artifacts too
           ; (println "downloading from repo" (:name repo))
           (download-artifact repo "pom" a)
           (download-artifact repo classifier extension a))
         (recur (rest repos)))))))

(defn read-artifact-pom
  "Returns the POM of the artifact."
  [a]
  ; (println "reading " a)
  (when-not (local-artifact? "pom" a)
    (cache-artifact "pom" a))
  (mvnx/read-pom-xml (str (artifact-version-local-path a) "/" (artifact-filename "pom" a))))

(defn managed-dependencies
  "Returns the list of managed dependencies of th POM."
  [pom]
  (get-in pom [:dependency-management :dependencies]))

(defn managed-versions
  "Transforms the dependency manangement list into a map of artifact keys to versions."
  [pom]
  (into {} (map #(vector (artifact-key %) (:version %))
                (managed-dependencies pom))))

(defn merge-build-section
  "Merges the build of parent POM p1 and child POM p2"
  [p1 p2]
  {:default-goal (if (contains? p2 :default-goal)
                   (:default-goal p2)
                   (:default-goal p1)) ; TODO check
   :directory (if (contains? p2 :directory)
                (:directory p2)
                (:directory p1))
   :source-directory (if (contains? p2 :source-directory)
                       (:source-directory p2)
                       (:source-directory p1))
   :script-source-directory (if (contains? p2 :script-source-directory)
                              (:script-source-directory p2)
                              (:script-source-directory p1))
   :test-source-directory (if (contains? p2 :test-source-directory)
                            (:test-source-directory p2)
                            (:test-source-directory p1))
   :output-directory (if (contains? p2 :output-directory)
                       (:output-directory p2)
                       (:output-directory p1))
   :test-output-directory (if (contains? p2 :test-output-directory)
                            (:test-output-directory p2)
                            (:test-output-directory p1))
   :extensions (into [] (concat (:extensions p1) (:extensions p2)))
   :final-name (if (contains? p2 :final-name)
                 (:final-name p2)
                 (:final-name p1))
   :filters (into [] (concat (:filters p1) (:filters p2)))
   :resources (into [] (concat (:resources p1) (:resources p2)))
   :test-resources (into [] (concat (:test-resources p1) (:test-resources p2)))
   :plugins (into [] (concat (:plugins p1) (:plugins p2)))
   :plugin-management (merge-with concat
                                  (:plugin-management p1)
                                  (:plugin-management p2))})

(defn merge-reporting-section
  "Merges the reporting section of parent POM p1 and child POM p2."
  [p1 p2]
  {:exclude-defaults (if (contains? p2 :exclude-defaults)
                       (:exclude-defaults p2)
                       (:exclude-defaults p1))
   :output-directory (if (contains? p2 :output-directory)
                       (:output-directory p2)
                       (:output-directory p1))
   :plugins (into [] (concat (:plugins p2) (:plugins p1)))
   :inherited  (if (contains? p2 :inherited)
                 (:inherited p2)
                 (:inherited p1))
  ; TODO add configuration
  })

(defn merge-poms
  "Merges a parent POM p1 and a child POM p2."
  ([])
  ([p1 p2]
   ; p1 is the parent of p2, so p2 is more specific
   {:model-version (:model-version p2)
    :group-id (:group-id p2)
    :artifact-id (:artifact-id p2)
    :version (:version p2)
    :packaging (:packaging p2)
    :dependency-management {:dependencies (into [] (concat (managed-dependencies p2)
                                                           (managed-dependencies p1)))}
    :dependencies (into [] (concat (:dependencies p1) (:dependencies p2)))
    :parent (:parent p2)
    :modules (:modules p2)
    :properties (merge (:properties p1) (:properties p2))
    :build (merge-build-section p1 p2)
    :reporting (merge-reporting-section p1 p2)
    :description (:description p2)
    :url (:url p2)
    :inception-year (:inception-year p2)
    :licenses (if (contains? p2 :licenses)
                (:licenses p2)
                (:licenses p1)) ; TODO check
    :organization (:organization p2)
    :developers (into [] (concat (:developers p2) (:developers p1)))
    :contributors (into [] (concat (:contributors p2) (:contributors p1)))
    :issue-management (if (contains? p2 :issue-management)
                        (:issue-management p2)
                        (:issue-management p1)) ; TODO check
    :ci-management (if (contains? p2 :ci-management)
                     (:ci-management p2)
                     (:ci-management p1)) ; TODO check
    :mailing-lists (if (contains? p2 :mailing-lists)
                     (:mailing-lists p2)
                     (:mailing-lists p1)) ; TODO check
    :scm (if (contains? p2 :scm)
           (:scm p2)
           (:scm p1)) ; TODO check
    :prerequisites (:prerequisites p2)
    :repositories (into [] (concat (:repositories p2) (:repositories p1)))
    :plugin-repositories (into [] (concat (:plugin-repositories p2)
                                          (:plugin-repositories p1)))
    :distribution-management (if (contains? p2 :distribution-management)
                               (:distribution-management p2)
                               (:distribution-management p1)) ; TODO check
    :profiles (into [] (concat (:profiles p2) (:profiles p1)))}))

(defn replace-properties-in-pom
  "Returns the map for of the POM with the properties replaced with their values."
  [pom]
  (let [properties (:properties pom)]
    (->> (dissoc pom :properties) ; remove properties from POM
        ; use walk/prewalk to replace the string values in the pom
        ;   with the property replaced values
         (walk/prewalk #(if (string? %)
                          (prop/replace-properties-recursive properties %)
                          %))))) ; recursive replacement to handle nested properties
;    (assoc pom :properties properties))) ; reinsert properties

(comment
  (prop/replace-properties {:version "1.5"} "${version}")
  (prop/replace-properties-recursive {:coords "a/b [${version}]"
                                      :version "1.5"} "${coords}")
)

(defn pom-for-artifact
  "Builds the project object model map for the artifact by loading and merging the POM and it's parent POM's, if any."
  [a]
  (loop [artifact a poms []]
    ; (println "reading pom for" artifact)
    (let [pom (read-artifact-pom artifact)]
      (if-let [parent (:parent pom)]
        (recur parent (conj poms pom)) ; add parent pom to the vector 
        ;(apply merge (conj poms pom))
        (->> (conj poms pom)
             (reverse)
             (reduce merge-poms)
             (replace-properties-in-pom))))))

; Cache calls, TODO: use real cache
; (def pom-for-artifact (memoize pom-for-artifact))

;;;
;;; dependencies
;;;
;;
;; a dependency is an extension of artifacts with optional :scope and :exclusions keys
;;
; TODO complete
(s/def ::exclusion (s/keys :req [::artifact-id]
                           :opt [::group-id ::version]))
(s/def ::dependency (s/keys :req [::artifact-id]
                            :opt [::group-id ::version ::classifier ::type ::scope ::system-path ::optional ::exclusions]))

;;;
;;; dependency handling
;;;

(defn matches-component?
  "Returns true, if the dependency component string matches the component pattern."
  [p s]
  (if (and p s)
    (if (str/ends-with? p "*") ; star pattern
      (str/starts-with? s (sstr/substring 0 (- (count p) 1) p))
      (= p s))
    false))

(defn matches-exclude?
  "Returns true, if the dependency matches the exclude."
  [exclude dependency]
  (and (matches-component? (:group-id exclude) (:group-id dependency))
       (matches-component? (:artifact-id exclude) (:artifact-id dependency))))

(comment
  (matches-component? "org.soulspace" "org.soulspace")
  (matches-component? "org.soulspace" "org.dingdong")
  (matches-component? "org.soul*" "org.soulspace")
  (matches-component? "org.soul*" "org.dingdong")
  )

(defn resolved?
  "Checks, if the artifact is already part of the resolved set."
  [resolved dep]
  (contains? resolved (artifact-key dep))) ; TODO artifack-version-key?

(defn cycle?
  "Checks, if the artifact produces a cycle."
  [path dep]
  ; path is a vector so use some instead of contains?
  (some #(= (artifact-version-key dep) %) path))

(defn excluded?
  "Checks if the dependency is part of the excluded set."
  [exclusions dep]
  (some #(matches-exclude? % dep) exclusions))

(defn follow?
  "Checks if the dependency should be followed."
  ([dep]
   (follow? #{"compile" "provided" "runtime" "system"} dep))
  ([scopes-to-follow dep]
   (and (not (get dep :optional false))
       (contains? scopes-to-follow (get dep :scope "compile")))))

(defn exclude-artifact
  "Returns the exclusions collection with the artifact key added."
  [exclusions a]
  (let [k (artifact-key a)]
    (if (contains? exclusions k)
      exclusions ; artifact key already contained
      (conj exclusions k) ; not contained, add artifact key 
      )))

(defn versioned-dependency
  "Returns the dependency with version filled in from "
  [dm dependency]
  (if (nil? (:version dependency))
    (assoc dependency :version (dm (artifact-key dependency)))
    dependency))

(defn exclusion-set
  "Returns a set of exclusions from the exclusion lists of POM dependencies"
  [e]
  ;(println "Exclusion list: " e)
  (if (seq e)
    (into #{} (map artifact-key) e)
    #{}))

(defn dependency-key
  "Returns a key for the dependency."
  [dep]
  (str (:group-id dep)
       "/" (:artifact-id dep)
       "-" (:version dep)
       (when (:classifier dep) (str "-" (:scope dep)))
       (when (:optional dep) (str " {optional: " (:optional dep) "}"))
       (when (:scope dep) (str " {scope: " (:scope dep) "}"))
       (when (:type dep) (str " {type: " (:type dep) "}"))))

(defn build-dependency-node
  "Creates a node for the dependency tree."
  ([a]
   (build-dependency-node a [] #{}))
  ([a dependencies]
   (build-dependency-node a dependencies #{}))
  ([a dependencies exclusions]
   {:group-id (:group-id a)
    :artifact-id (:artifact-id a)
    :version (:version a)
    :optional (:optional a)
    :dependencies dependencies
    :exclusions exclusions
    :scope (:scope a)
    :type (:type a)
    :system-path (:system-path a)}))


; transitive dependency resolution with depth first search
; build up exclusions on the way down and inclusions on the way up
(defn resolve-dependencies
  "Resolves the (transitive) dependencies of the artifact."
  ([a]
   (println "resolve dependencies for artifact" (artifact-version-key a))
   (build-dependency-node a))
  ([d path exclusions]
   (let [pom (pom-for-artifact d)
         dm (managed-versions pom)
         dependencies (:dependencies pom)
         exclusions (get-in pom [:dependencies :exclusions])]
     (loop [deps dependencies path []]
       (if (seq deps)
         (let [dep (first deps)]
           (println dep)
           (resolve-dependencies (first deps)))
         (recur (rest deps) path))) ; FIXME build dep node and include
     )))

; TODO handle exclusions (WIP) to break cycles

(defn print-deps
  "Print dependencies."
  ([dep]
   (let [pom (pom-for-artifact dep)]
     (print-deps pom (:dependencies pom)
                 [(artifact-version-key dep)]
                 (exclusion-set (get-in pom [:dependencies :exclusions])) 
                 0)))
  ([pom dependencies path exclusions indent]
    (println "Called for" (artifact-version-key pom))
   ; (println "Path" (str/join "->" path))
   ; (println "DM: " (managed-dependencies pom))
   (let [dm (managed-versions pom)] ; normalize versions
     ; (println dm)
     (if (seq dependencies)
       (loop [deps dependencies p path e exclusions]
         (if (seq deps)
           (let [dep (versioned-dependency dm (first deps))
                 dpom (pom-for-artifact dep)]
             (if (cycle? path dep)
               (println "Cycle:"
                        (str/join " -> " path)
                        "->" (artifact-version-key dep))
               (if (excluded? e dep)
                 (println "Excluded:" dep)
                 (if-not (follow? dep)
                   (println "Not followed:" dep)
                   (print-deps dep (:dependencies dpom)
                               (conj path (artifact-version-key dep))
                               (set/union e (exclusion-set (:exclusions dep)))
                               (inc indent)))))
             (recur (rest deps) p e))
           (println "end of list")))
       (println (str/join (repeat indent "-")) (dependency-key pom))))))

(comment
  (str "a-" nil "-b")
  (artifact-relative-path {:group-id "a" :artifact-id "b" :version "1.0"})
  (artifact-filename {:group-id "a" :artifact-id "b" :version "1.0"})
  (artifact-local-path {:group-id "a" :artifact-id "b" :version "1.0"})
  (artifact-url {:id "clojars" :remote "http://repo.clojars.org"}
                {:group-id "a" :artifact-id "b" :version "1.0"})
  (artifact-metadata-url {:id "clojars" :remote "http://repo.clojars.org"}
                         {:group-id "a" :artifact-id "b" :version "1.0"})
  (local-artifact? {:group-id "org.soulspace.clj" :artifact-id "clj.base"
                    :version "0.8.3"})
  (download-artifact {:id "clojars" :remote "http://repo.clojars.org"
                      :local "/home/soulman/tmp/repository"}
                     {:group-id "org.soulspace.clj" :artifact-id "clj.base"
                      :version "0.8.3"})
  (cache-artifact {:id "clojars" :remote "http://repo.clojars.org"
                   :local "/home/soulman/tmp/repository"}
                  {:group-id "org.soulspace.clj" :artifact-id "clj.base"
                   :version "0.8.3"})
  (artifact-relative-path {:group-id "org.soulspace.clj"
                           :artifact-id "clj.base"
                           :version "0.8.3"})
  (read-artifact-pom {:group-id "org.soulspace.clj" :artifact-id "clj.base"
                      :version "0.8.3"})
  (pom-for-artifact {:group-id "org.soulspace.clj" :artifact-id "clj.base"
                     :version "0.8.3"})
  (pom-for-artifact {:group-id "commons-codec" :artifact-id "commons-codec"
                     :version "1.13"})
  (slurp (str "http://repo.clojars.org/"
              (artifact-relative-path {:group-id "org.soulspace.clj"
                                       :artifact-id "clj.base"
                                       :version "0.8.3"})))
  @(http/head "http://repo.clojars.org/org/soulspace/clj/clj.base/")
  @(http/request {:url "http://repo.clojars.org/org/soulspace/clj/clj.base/"
                  :method :head
                  :timeout 500})
  (resolve-dependencies {:group-id "org.soulspace.clj" :artifact-id "clj.base"
                         :version "0.8.3"})
  (resolve-dependencies {:group-id "org.soulspace.clj" :artifact-id "clj.base"
                         :version "0.8.3"})
  (cycle? ["org.scalatest/scalatest_2.12[3.2.9]" "org.scala-lang/scala-compiler[2.12.13]"
           "org.scala-lang.modules/scala-xml_2.12[1.0.6]" "org.scala-lang/scala-compiler[2.12.0]"
           "org.scala-lang.modules/scala-xml_2.12[1.0.5]"]
          {:group-id "org.scala-lang", :artifact-id "scala-compiler", :version "2.12.0"})
  (print-deps {:group-id "org.soulspace.clj" :artifact-id "clj.base"
               :version "0.8.3"})
  (print-deps {:group-id "org.apache.spark" :artifact-id "spark-core_2.12"
               :version "3.2.1"})
  (merge-poms {:dependency-management {:dependencies [{:group-id "a" :artifact-id "b" :version "1"}]}}
              {:dependency-management {:dependencies [{:group-id "c" :artifact-id "d" :version "2"}]}})
  )

