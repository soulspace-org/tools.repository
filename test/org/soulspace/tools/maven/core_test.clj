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

(ns org.soulspace.tools.maven.core-test
  (:require [clojure.test :refer :all]
            [org.soulspace.tools.maven.core :refer :all]))

;;;
;;; tests for dependencies
;;;

;;;
;;; tests for repositories
;;;

(deftest artifact-path-test
  (are [x y] (= x y)
    (artifact-relative-path {:group-id "a"
                             :artifact-id "b"
                             :version "1.0"}) "a/b"
    (artifact-relative-path {:group-id "a"
                             :artifact-id "b.c"
                             :version "1.0"}) "a/b.c"
    (artifact-relative-path {:group-id "a.d"
                             :artifact-id "b.c"
                             :version "1.0"}) "a/d/b.c"
    (artifact-version-relative-path {:group-id "a"
                                     :artifact-id "b"
                                     :version "1.0"}) "a/b/1.0"
    (artifact-version-relative-path {:group-id "a"
                                     :artifact-id "b.c"
                                     :version "1.0"}) "a/b.c/1.0"
    (artifact-version-relative-path {:group-id "a.d"
                                     :artifact-id "b.c"
                                     :version "1.0"}) "a/d/b.c/1.0"))

(deftest artifact-filename-test
  (are [x y] (= x y)
    (artifact-filename {:group-id "a"
                        :artifact-id "b"
                        :version "1.0"}) "b-1.0.jar"
    (artifact-filename {:group-id "a"
                        :artifact-id "b"
                        :version "1.0"} "pom") "b-1.0.pom"))

(deftest artifact-url-test
  (are [x y] (= x y)
    (artifact-url {:id "clojars" :url "http://repo.clojars.org"}
                  {:group-id "a" :artifact-id "b" :version "1.0"})
    "http://repo.clojars.org/a/b/1.0/b-1.0.jar"))

(deftest artifact-metadata-url-test
  (are [x y] (= x y)
    (artifact-metadata-url {:id "clojars" :url "http://repo.clojars.org"}
                           {:group-id "a" :artifact-id "b" :version "1.0"})
    "http://repo.clojars.org/a/b/maven-metadata.xml"))

(comment
  (run-tests))
