;;;;
;;;;   Copyright (c) Ludger Solbach. All rights reserved.
;;;;
;;;;   The use and distribution terms for this software are covered by the
;;;;   Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
;;;;   which can be found in the file license.txt at the root of this distribution.
;;;;   By using this software in any fashion, you are agreeing to be bound by
;;;;   the terms of this license.
;;;;
;;;;   You must not remove this notice, or any other, from this software.
;;;;

(ns org.soulspace.tools.repo
  (:require [clojure.string :as str]
            [clojure.spec.alpha :as s]))

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
;;; Package URL handling
;;;
;;; see https://github.com/package-url/purl-spec
;;;

(def package-regex #"^([a-zA-Z]+[a-zA-Z0-9._-]*)(?:/([a-zA-Z]+[a-zA-Z0-9._-]*))?/([a-zA-Z]+[a-zA-Z0-9._-]*)(?:@([a-zA-Z0-9]+[a-zA-Z0-9._-]*))?$")

;(def type-regex #"")

(s/def ::scheme (s/and string? #(= % "pkg")))
(s/def ::type string?)
(s/def ::namespace string?)
(s/def ::name string?)
(s/def ::version string?)
(s/def ::qualifiers map?)
(s/def ::subpath string?)

(s/def ::package-url
  (s/keys :req-un [::type ::name]
          :opt-un [::namespace ::version ::qualifiers ::subpath]))

(comment
  (s/explain ::package-url {:type "maven"})
  (s/conform ::package-url {:type "maven"})
  (s/explain ::package-url {:type "maven" :name "clj.base"})
  (s/conform ::package-url {:type "maven" :name "clj.base"})
  (s/explain ::package-url {:type "maven" :version "0.8.3"})
  (s/conform ::package-url {:type "maven" :version "0.8.3"}))

(defn clean
  "Removes keys from map for which the value is nil."
  [m]
  (apply dissoc
         m
         (for [[k v] m :when (nil? v)] k)))

(comment
(defn url-decode
  "Returns the URL decoded string."
  [s]
  (URLDecoder/decode s))

(defn url-encode
  "Returns the URL encoded string."
  [s]
  (URLEncoder/encode s))
)

(defn parse-qualifiers
  "Returns a map of qualifiers for the qualifier part of the package url."
  [s]
  (->> s
       (#(str/split % #"&"))
       (map #(str/split % #"="))
       (into {})))

(defn parse-optional
  "Returns a map of the optional qualifiers and "
  [coll]
  (cond
    (= 0 (count coll)) {}
    (= 1 (count coll)) (if (str/index-of (first coll) "=")
                         {:qualifiers (parse-qualifiers (first coll))}
                         {:subpath (first coll)})
    (= 2 (count coll)) {:qualifiers (parse-qualifiers (first coll))
                        :subpath (second coll)}
    :else {}))

(s/fdef parse
  :args (s/cat :s string?)
  :ret ::package-url)

(defn parse
  "Parses the package URL string into a package URL map."
  [purl]
  (let [;s (url-decode purl)
        s purl]
    (when (str/starts-with? s "pkg:")
      (let [parts (str/split (subs s 4) #"(\?|#)")
            _ (println parts)
            [_ type namespace name version] (re-matches package-regex (first parts))]
        (merge (clean {:type type :namespace namespace :name name :version version})
               (parse-optional (rest parts)))))))

(s/fdef generate
  :args (s/cat :purl ::package-url)
  :ret string?)

(defn generate
  "Generates the package URL string for the package URL map."
  ([purl]
   (let [checked (s/conform ::package-url purl)]
     (if (s/invalid? checked)
       (throw (ex-info "Invalid input" (s/explain-data ::package-url purl)))
       (str "pkg:"
            (:type purl)
            (when (:namespace purl)
              (str "/" (:namespace purl)))
            "/" (:name purl)
            (when (:version purl)
              (str "@" (:version purl)))
            (when (:qualifiers purl)
              (str "?" (map (fn [[k v]] (str k "=" v)) (:qualifiers purl))))
            (when (:subpath purl)
              (str "#" (:subpath purl))))))))

(defn package-key
  "Returns a string key for the package `p`, disregarding a version."
  [p]
  (str (:namespace p) "/" (:name p)))

(defn package-version-key
  "Returns a string key for the package `p` including the version."
  [p]
  (str (package-key p) "[" (:version p) "]"))

(defn same-package?
  "Returns true, when the packages `p1` and `p2` have the same package key."
  [p1 p2]
  (= (package-key p1) (package-key p2)))

(defn same-package-version?
  "Returns true, when the packages p1 and p2 have the same package version key."
  [p1 p2]
  (= (package-version-key p1) (package-version-key p2)))



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
;;; repository protocols
;;;

(defprotocol Repository
  (get-package [this package] "Returns the package from the repository.")
 )

(defprotocol CachingRepository
  (cached? [this package] "Returns true, if the package is cached.")
  (cache-package [this package] "Caches the package."))

(defprotocol VersionedRepository
  (versions [this package] "Returns the versions for the package.")
  (latest-version [this package] "Returns the latest version for the package."))


(comment
  (sstr/substring 4 "pkg:maven/clj.base@0.8.3")
  (str/split "maven/clj.base@0.8.3?k=v&l=w#/foo" #"(\?|#)")
  (str/split "maven/clj.base@0.8.3?k=v&l=w" #"(\?|#)")
  (str/split "maven/clj.base@0.8.3#/foo" #"(\?|#)")
  (str/split (sstr/substring 4 "pkg:maven/clj.base@0.8.3?k=v&l=w#/foo") #"(\?|#)")

  (generate {:type "maven" :version "0.8.3"})
  (generate {:type "maven" :name "clj.base" :version "0.8.3"})
  (generate {:type "maven" :namespace "org.soulspace.clj" :name "clj.base" :version "0.8.3"})
  (re-matches package-regex "maven/clj.base@0.8.3")
  (re-matches package-regex "maven/org.soulspace.clj/clj.base@0.8.3")
  (parse-qualifiers "k=v&l=w")
  (parse-optional ["k=v&l=w"])
  (parse-optional ["path"])
  (parse-optional ["k=v&l=w" "path"])

  (parse "pkg:maven/clj.base@0.8.3?k=v&l=w#/foo")
  (parse "pkg:maven/org.apache.xmlgraphics/batik-anim@1.9.1?repository_url=repo.spring.io%2Frelease")

  "pkg:bitbucket/birkenfeld/pygments-main@244fd47e07d1014f0aed9c"

  "pkg:deb/debian/curl@7.50.3-1?arch=i386&distro=jessie"

  "pkg:docker/cassandra@sha256:244fd47e07d1004f0aed9c"
  "pkg:docker/customer/dockerimage@sha256:244fd47e07d1004f0aed9c?repository_url=gcr.io"

  "pkg:gem/jruby-launcher@1.1.2?platform=java"
  "pkg:gem/ruby-advisory-db-check@0.12.4"

  "pkg:github/package-url/purl-spec@244fd47e07d1004f0aed9c"

  "pkg:golang/google.golang.org/genproto#googleapis/api/annotations"

  "pkg:maven/org.apache.xmlgraphics/batik-anim@1.9.1?packaging=sources"
  "pkg:maven/org.apache.xmlgraphics/batik-anim@1.9.1?repository_url=repo.spring.io%2Frelease"

  "pkg:npm/%40angular/animation@12.3.1"
  "pkg:npm/foobar@12.3.1"

  "pkg:nuget/EnterpriseLibrary.Common@6.0.1304"

  "pkg:pypi/django@1.11.1"

  "pkg:rpm/fedora/curl@7.50.3-1.fc25?arch=i386&distro=fedora-25"
  "pkg:rpm/opensuse/curl@7.56.1-1.1.?arch=i386&distro=opensuse-tumbleweed")

