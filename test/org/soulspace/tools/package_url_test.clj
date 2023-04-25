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

(ns org.soulspace.tools.package-url-test
  (:require [clojure.test :refer :all]
            [org.soulspace.tools.package-url :refer :all]))

(deftest parse-test
  (are [x y] (= x y)
    {:type "maven" :name "clj.base" :version "0.8.3"} (parse "pkg:maven/clj.base@0.8.3")
    {:type "maven" :namespace "org.soulspace.clj" :name "clj.base" :version "0.8.3"} (parse "pkg:maven/org.soulspace.clj/clj.base@0.8.3")))

(deftest generate-test
  (are [x y] (= x y)
    "pkg:maven/clj.base@0.8.3" (generate {:type "maven" :name "clj.base" :version "0.8.3"})
    "pkg:maven/org.soulspace.clj/clj.base@0.8.3" (generate {:type "maven" :namespace "org.soulspace.clj" :name "clj.base" :version "0.8.3"})))

