(ns org.soulspace.tools.package-url
  (:require [clojure.spec.alpha :as s])
  (:import [java.net URLDecoder URLEncoder])
  )

;;;;
;;;; Package URL handling
;;;;

;;;
;;; see https://github.com/package-url/purl-spec
;;;

; TODO add correct qualifiers and subpath expressions
; TODO validate/test regex
(def purl-regex #"^pkg:([a-z]+[a-z0-9._-]*){1,1}
                  (/[a-z]+[a-z0-9._-]*){0,1}
                  /([a-z]+[a-z0-9._-]*){1,1}
                  (@([a-z]+[a-z0-9._-]*){1,1}){0,1}
                  (\?(([a-z]+[a-z0-9._-]*){1,1}=([a-z]+[a-z0-9._-]*){1,1})){0,1}
                  (#){0,1}")
;(def type-regex #"")

;(s/def ::scheme (s/and string? #(= % "pkg")))
(s/def ::type string?)
(s/def ::name string?)
(s/def ::namespace string?)
(s/def ::version string?)
(s/def ::qualifiers map?)
(s/def ::subpath string?)

(s/def ::package-url
       (s/keys :req-un [::type ::name]
               :opt-un [::namespace ::version ::qualifiers ::subpath]))

(defn url-decode
  "Returns the URL decoded string."
  [s]
  (URLDecoder/decode s))

(defn url-encode
  "Returns the URL encoded string."
  [s]
  (URLEncoder/encode s))

(defn parse
  "Parses the string into package url."
  [s]
  ; TODO use regex or grammar
  )

(defn generate
  "Generates the URL string for the package URL."
  [purl]
  (str "pkg:"
       (:type purl)
       (when (:namespace purl)
         (str "/" (:namespace purl)))
       "/" (:name purl)
       (when (:version purl)
         (str "@" (:version purl)))
       (when (:qualifiers purl)
         (str "?" ; TODO k=v joined by "&"
              ))
       ))

(comment
  (generate {:type "maven" :name "clj.base" :version "0.8.3"})
  (generate {:type "maven" :namespace "org.soulspace.clj" :name "clj.base" :version "0.8.3"})
  )