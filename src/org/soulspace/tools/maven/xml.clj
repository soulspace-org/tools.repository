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

(ns org.soulspace.tools.maven.xml
  (:require [clojure.java.io :as io]
            [clojure.data.xml :as xml]
            [org.soulspace.tools.maven.xml-parser :as mvn]))

;;;;
;;;; Read and parse Apache Maven xml files
;;;;

;;
;; Parse the given source with the parse function.
;;
(defn parse-xml
  "Parses the source XML input with the given parse function."
  [parse-fn source]
  (with-open [input (io/input-stream source)]
    (->> input
         (xml/parse)
         (parse-fn))))

;;;
;;; read the maven xml 
;;;
(defn read-pom-xml
  "Returns a map with the contents of the maven POM referenced by source."
  [source]
  (parse-xml mvn/parse-pom-xml source))

(defn read-settings-xml
  "Returns a map with the contents of the maven settings referenced by source."
  [source]
  (parse-xml mvn/parse-settings-xml source))

(defn read-metadata-xml
  "Returns a map with the contents of the maven metadata referenced by source."
  [source]
  (parse-xml mvn/parse-metadata-xml source))

(comment
  (mvn/parse-pom-xml
   (xml/parse-str
    "<project>
     <modelVersion>4.0.0</modelVersion>
     <groupId>org.soulspace.clj</groupId>
     <artifactId>clj.java</artifactId>
     <version>0.8.3</version>
     <dependencyManagement>
       <dependencies>
         <dependency>
           <groupId>org.soulspace.clj</groupId>
           <artifactId>clj.java</artifactId>
           <version>0.8.3</version>
         </dependency>
       </dependencies>
     </dependencyManagement>
     </project>"))
  (read-pom-xml "/home/soulman/tmp/repository/org/soulspace/clj/clj.base/0.8.3/clj.base-0.8.3.pom")
  (read-settings-xml "/home/soulman/.m2/settings.xml")
  (read-metadata-xml "/home/soulman/.m2/repository/org/soulspace/clj/clj.base/maven-metadata-clojars.xml")
  )

