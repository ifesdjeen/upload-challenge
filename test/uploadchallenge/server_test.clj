(ns uploadchallenge.server-test
  (:use clojure.test
        uploadchallenge.server
        uploadchallenge.routing))

(deftest routes-test
  (testing "Adding a route"
    (reset-routes!)
    (is (= 0 (count @routes)))
    (add-route :get "/" #())
    (is (= 1 (count @routes)))))

(deftest match-route-test
  (testing "Matching a route"
    (reset-routes!)
    (def x (atom 0))
    (add-route :get "/" (fn []
                          (reset! x 20)))
    (match-route :get "/")
    (is (= 20 @x)))

  (testing "Matches a route with regexp"
    (reset-routes!)
    (def x (atom 0))
    (add-route :get "/javascripts/(.*).js" (fn []
                                          (reset! x 20)))
    (match-route :get "/javascripts/jquery.js")
    (is (= 20 @x))
    ))

(deftest extract-filename-test
  (testing "Extracts filename"
    (reset-routes!)
    (add-route :get "/javascripts/(.*).js" (fn []
                                             (reset! x 20)))

    (is (= "jquery" (extract-filename :get "/javascripts/jquery.js")))))

