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

(ns org.soulspace.tools.package-url
  (:require [clojure.spec.alpha :as s]
            [clojure.string :as str]
            [org.soulspace.clj.string :as sstr])
  (:import [java.net URLDecoder URLEncoder]))

;;;;
;;;; Package URL handling
;;;;

;;;
;;; see https://github.com/package-url/purl-spec
;;;

(def artifact-regex #"^([a-zA-Z]+[a-zA-Z0-9._-]*)(?:/([a-zA-Z]+[a-zA-Z0-9._-]*))?/([a-zA-Z]+[a-zA-Z0-9._-]*)(?:@([a-zA-Z0-9]+[a-zA-Z0-9._-]*))?$")

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
  (s/conform ::package-url {:type "maven" :version "0.8.3"})
  )

(defn clean
  "Removes keys from map for which the value is nil."
  [m]
  (apply dissoc
         m
         (for [[k v] m :when (nil? v)] k)))

(defn url-decode
  "Returns the URL decoded string."
  [s]
  (URLDecoder/decode s))

(defn url-encode
  "Returns the URL encoded string."
  [s]
  (URLEncoder/encode s))

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
      (let [parts (str/split (sstr/substring 4 s) #"(\?|#)")
            _ (println parts)
            [_ type namespace name version] (re-matches artifact-regex (first parts))]
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

(comment
  (sstr/substring 4 "pkg:maven/clj.base@0.8.3")
  (str/split "maven/clj.base@0.8.3?k=v&l=w#/foo" #"(\?|#)")
  (str/split "maven/clj.base@0.8.3?k=v&l=w" #"(\?|#)")
  (str/split "maven/clj.base@0.8.3#/foo" #"(\?|#)")
  (str/split (sstr/substring 4 "pkg:maven/clj.base@0.8.3?k=v&l=w#/foo") #"(\?|#)")

  (generate {:type "maven" :version "0.8.3"})
  (generate {:type "maven" :name "clj.base" :version "0.8.3"})
  (generate {:type "maven" :namespace "org.soulspace.clj" :name "clj.base" :version "0.8.3"})
  (re-matches artifact-regex "maven/clj.base@0.8.3")
  (re-matches artifact-regex "maven/org.soulspace.clj/clj.base@0.8.3")
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
  "pkg:rpm/opensuse/curl@7.56.1-1.1.?arch=i386&distro=opensuse-tumbleweed"

  )

