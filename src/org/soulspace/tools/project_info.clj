(ns org.soulspace.tools.project-info
  (:require [clojure.data.json :as json]
            [clojure.data.xml :as xml]))

;;;;
;;;; external project information
;;;;

;;;
;;; Github handling
;;;

(def github-pattern #"https://github.com/(.*)")
(def github-root "https://github.com")

(defn github-url?
  "Returns truthy, if the url is a github project url."
  [url]
  (re-matches github-pattern url))

(defn github-releases-url
  "Returns the URL for the releases of the project."
  [p]
  (str github-root "/" p "/releases"))

(defn github-contributors-url
  "Returns the URL for contributors graph of the project."
  [p]
  (str github-root "/" p "/graphs/contributors"))

(comment
  (github-releases-url "lsolbach/CljBase")
  (github-contributors-url "lsolbach/CljBase"))

;;;
;;; Apache handling
;;;

(def apache-pattern #"https://(.*)\.apache\.org.*")

(defn apache-url?
  "Returns truthy, if the url is a apache project url."
  [url]
  (re-matches apache-pattern url))

;;;
;;; OpenHub handling
;;;

;; Docs
; https://github.com/blackducksoftware/ohloh_api
; https://github.com/blackducksoftware/ohloh_api/blob/master/reference/project.md

;; URLs
; https://www.openhub.net/projects.xml
; https://www.openhub.net/projects/{project_id}.xml

(def openhub-root "https://www.openhub.net")
(def openhub-api-key "") ; TODO set from ENV

(defn openhub-projects-xml
  "Fetches the projects from openhub.net."
  []
  (-> (slurp (str openhub-root "/projects.xml"))
      (xml/parse)))

(defn openhub-project-xml
  "Fetches the project from openhub.net."
  [id]
  (-> (slurp (str openhub-root "/projects/" id ".xml"))
      (xml/parse)))

(comment
  (openhub-projects-xml))