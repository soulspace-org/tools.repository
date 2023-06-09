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

(ns org.soulspace.tools.repo-test
  (:require [clojure.test :refer :all]
            [org.soulspace.tools.repo :refer :all]))

;;;
;;; tests for versions
;;;

(deftest digits?-test
  (are [x y] (= x y)
    (digits? "0") true
    (digits? "1") true
    (digits? "2") true
    (digits? "3") true
    (digits? "4") true
    (digits? "5") true
    (digits? "6") true
    (digits? "7") true
    (digits? "8") true
    (digits? "9") true
    (digits? "10") true
    (digits? "0123456789") true
    (digits? nil) false
    (digits? "") false
    (digits? "a") false
    (digits? "1.0") false
    (digits? "a1") false
    (digits? "1a") false))

(deftest split-version-test
  (are [x y] (= x y)
    (split-version nil)  nil
    (split-version "") nil
    (split-version "1") ["1"]
    (split-version "1.2.3") ["1" "2" "3"]
    (split-version "1.2.3-GA") ["1" "2" "3-GA"]
    (split-version "1.2.3-alpha2") ["1" "2" "3-alpha2"]))

(deftest compare-revision-test
  (are [x y] (= (Math/signum (double x)) y)
    (compare-revision nil nil) 0.0
    (compare-revision "" nil) 1.0
    (compare-revision "" "") 0.0
    (compare-revision "1" nil) 1.0
    (compare-revision nil "1") -1.0
    (compare-revision "1" "1") 0.0
    (compare-revision "3-alpha2" "3-alpha2") 0.0
    (compare-revision "3-alpha1" "3-alpha2") -1.0))

(deftest compare-version-test
  (are [x y] (= (Math/signum (double x)) y)
    (compare-version nil nil) 0.0
    (compare-version "" nil) 1.0
    (compare-version "" "") 0.0
    (compare-version "1" "1") 0.0
    (compare-version "1.2" "1.2") 0.0
    (compare-version "1.2.3" "1.2.3") 0.0
    (compare-version "1.2.3-alpha2" "1.2.3-alpha2") 0.0
    (compare-version "1.2.3-alpha2" "1.2.3-alpha1") 1.0
    (compare-version "1.2.3-alpha1" "1.2.3-alpha2") -1.0
    (compare-version "1.2.3" "1.2.13") -1.0
    (compare-version "1.2.c" "1.2.ac") 1.0
    ))

(deftest lesser-version?-test
  (are [x y] (= x y)
    (lesser-version? "0.0.1" "0.0.2") true
    (lesser-version? "0.0.1" "0.0.1") false
    (lesser-version? "0.0.2" "0.0.1") false
    (lesser-version? "0.1.0" "0.2.0") true
    (lesser-version? "0.1.0" "0.1.0") false
    (lesser-version? "0.2.0" "0.1.0") false
    (lesser-version? "1.0.0" "2.0.0") true
    (lesser-version? "1.0.0" "1.0.0") false
    (lesser-version? "2.0.0" "1.0.0") false
    ))

(deftest same-version?-test
  (are [x y] (= x y)
    (same-version? "0.0.1" "0.0.2") false
    (same-version? "0.0.1" "0.0.1") true
    (same-version? "0.0.2" "0.0.1") false
    (same-version? "0.1.0" "0.2.0") false
    (same-version? "0.1.0" "0.1.0") true
    (same-version? "0.2.0" "0.1.0") false
    (same-version? "1.0.0" "2.0.0") false
    (same-version? "1.0.0" "1.0.0") true
    (same-version? "2.0.0" "1.0.0") false))

(deftest greater-version?-test
  (are [x y] (= x y)
    (greater-version? "0.0.1" "0.0.2") false
    (greater-version? "0.0.1" "0.0.1") false
    (greater-version? "0.0.2" "0.0.1") true
    (greater-version? "0.1.0" "0.2.0") false
    (greater-version? "0.1.0" "0.1.0") false
    (greater-version? "0.2.0" "0.1.0") true
    (greater-version? "1.0.0" "2.0.0") false
    (greater-version? "1.0.0" "1.0.0") false
    (greater-version? "2.0.0" "1.0.0") true))

;;;
;;; tests for artifacts
;;;
(deftest artifact-key-test
  (are [x y] (= x y)
    (artifact-key {:group-id "a"
                   :artifact-id "b"
                   :version "1.0"}) "a/b"
    (artifact-key {:group-id "a"
                   :artifact-id "b.c"
                   :version "1.0"}) "a/b.c"
    (artifact-key {:group-id "a.d"
                   :artifact-id "b.c"
                   :version "1.0"}) "a.d/b.c"
    (artifact-version-key {:group-id "a"
                           :artifact-id "b"
                           :version "1.0"}) "a/b[1.0]"
    (artifact-version-key {:group-id "a"
                           :artifact-id "b.c"
                           :version "1.0"}) "a/b.c[1.0]"
    (artifact-version-key {:group-id "a.d"
                           :artifact-id "b.c"
                           :version "1.0"}) "a.d/b.c[1.0]"))

(deftest same-artifact?-test
  (are [x y] (= x y)
    (same-artifact? {:group-id "a" :artifact-id "b" :version "1.0"}
                    {:group-id "a" :artifact-id "c" :version "1.0"}) false
    (same-artifact? {:group-id "a" :artifact-id "b" :version "1.0"}
                    {:group-id "c" :artifact-id "b" :version "1.0"}) false
    (same-artifact? {:group-id "a" :artifact-id "b" :version "1.0"}
                    {:group-id "a" :artifact-id "b" :version "1.1"}) true
    (same-artifact? {:group-id "a" :artifact-id "b" :version "1.0"}
                    {:group-id "a" :artifact-id "b" :version "1.0-SNAPSHOT"}) true
    (same-artifact? {:group-id "a" :artifact-id "b" :version "1.0"}
                    {:group-id "a" :artifact-id "b" :version "1.0"}) true
    (same-artifact-version? {:group-id "a" :artifact-id "b" :version "1.0"}
                            {:group-id "a" :artifact-id "c" :version "1.0"}) false
    (same-artifact-version? {:group-id "a" :artifact-id "b" :version "1.0"}
                            {:group-id "c" :artifact-id "b" :version "1.0"}) false
    (same-artifact-version? {:group-id "a" :artifact-id "b" :version "1.0"}
                            {:group-id "a" :artifact-id "b" :version "1.1"}) false
    (same-artifact-version? {:group-id "a" :artifact-id "b" :version "1.0"}
                            {:group-id "a" :artifact-id "b" :version "1.0-SNAPSHOT"}) false
    (same-artifact-version? {:group-id "a" :artifact-id "b" :version "1.0"}
                            {:group-id "a" :artifact-id "b" :version "1.0"}) true))

(deftest parse-test
  (are [x y] (= x y)
    {:type "maven" :name "clj.base" :version "0.8.3"} (parse "pkg:maven/clj.base@0.8.3")
    {:type "maven" :namespace "org.soulspace.clj" :name "clj.base" :version "0.8.3"} (parse "pkg:maven/org.soulspace.clj/clj.base@0.8.3")))

(deftest generate-test
  (are [x y] (= x y)
    "pkg:maven/clj.base@0.8.3" (generate {:type "maven" :name "clj.base" :version "0.8.3"})
    "pkg:maven/org.soulspace.clj/clj.base@0.8.3" (generate {:type "maven" :namespace "org.soulspace.clj" :name "clj.base" :version "0.8.3"})))

