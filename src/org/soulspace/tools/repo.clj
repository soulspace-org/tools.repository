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

(defprotocol PackageManager
  (get-artifact [this artifact])
 )

(defprotocol Caching
  (cache-artifact [this artifact]))

;;
;; single repository functions
;;
