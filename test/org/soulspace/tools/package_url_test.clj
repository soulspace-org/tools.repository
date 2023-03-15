(ns org.soulspace.tools.package-url-test
  (:require [clojure.test :refer :all]
            [org.soulspace.tools.package-url :refer :all]))

(deftest parse-test
  (are [x y] (= x y)
    
    ))

(deftest generate-test
  (are [x y] (= x y)
    (generate {:type "maven" :name "clj.base" :version "0.8.3"}) "pkg:maven/clj.base@0.8.3"))


