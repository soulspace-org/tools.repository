(ns org.soulspace.tools.spdx
  (:require [clojure.string :as str]
            [clojure.data.json :as json]))

;;;;
;;;; SPDX license handling
;;;;

;;
;; SPDX predicates
;;

(defn osi-approved?
  "Returns true, if the license is approved by the Open Software Initiative."
  [l]
  (= (:isOsiApproved l) true))

(defn fsf-libre?
  "Returns true, if the license is approved by the Free Software Foundation."
  [l]
  (= (:isFsfLibre l) true))

(defn deprecated-id?
  "Returns true, if the license is approved by the Free Software Foundation."
  [l]
  (= (:isDeprecatedLicenseId l) true))

(defn with-exception?
  "Returns true, if the license has an exception."
  [id]
  (str/includes? id " WITH "))

(def spdx-root "https://spdx.org")

(defn licenses
  "Fetch the licenses from SPDX."
  []
  (-> (slurp (str spdx-root "/licenses/licenses.json"))
      (json/read-str :key-fn keyword)
      (:licenses)))

(defn license
  "Fetch the license from SPDX"
  [spdx-id]
  (-> (slurp (str spdx-root "/licenses/" spdx-id ".json"))
      (json/read-str :key-fn keyword)))

(defn exceptions
  "Fetch the exceptions from SPDX."
  []
  (-> (slurp (str spdx-root "/licenses/exceptions.json"))
      (json/read-str :key-fn keyword)
      (:licenses)))

(defn exception
  "Fetch the exception from SPDX"
  [spdx-id]
  (-> (slurp (str spdx-root "/licenses/" spdx-id ".json"))
      (json/read-str :key-fn keyword)))

(comment
  (println (licenses))
  (println (exceptions))
  (println (license "Apache-2.0"))
  (println (exception "Classpath-exception-2.0")))

