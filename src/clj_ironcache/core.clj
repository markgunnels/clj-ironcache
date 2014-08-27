(ns clj-ironcache.core
  (:require [clj-http.client :as http]
            [slingshot.slingshot :refer [throw+ try+]]
            [cheshire.core :refer :all]))

(defn base-url
  [project-id]
  (format "https://cache-aws-us-east-1.iron.io/1/projects/%s/caches" 
          project-id))

(defn- build-url
  [project-id extended-path]
  (if extended-path
    (format "%s/%s" 
            (base-url project-id) 
            extended-path)
    (base-url project-id)))

(defn call
  [method extended-path options token project-id]  
  (-> (http/request (merge {:method method 
                            :url (build-url project-id extended-path)
                            :headers {"Authorization" 
                                      (str "OAuth " token)}
                            :content-type :json
                            :accept :json
                            :as :json} 
                           options))
      :body))


(defn list-caches
  "Get a list of all caches in a project. 100 caches are listed at a time. To see more, use the page parameter.
  /projects/ {Project ID}/caches GET List Caches"
  [token project-id]
  (call :get nil {} token project-id))

;; /projects/ {Project ID}/caches/ {Cache Name} GET Get Info About a Cache
(defn cache-info
  [token project-id cache-name]
  (call :get cache-name {} token project-id))

;; /projects/ {Project ID}/caches/ {Cache Name} DELETE Delete a Cache
(defn delete-cache
  [token project-id cache-name]
  (call :delete cache-name {} token project-id))

;; /projects/ {Project ID}/caches/ {Cache Name}/clear POST Clear a Cache
(defn clear-cache
  [token project-id cache-name]
  (call :delete cache-name {} token project-id))

;; /projects/ {Project ID}/caches/ {Cache Name}/items/{Key} PUT Put an Item into a Cache
(defn put-item-in-cache
  [token project-id cache-name key value-map]
  {:pre [(contains? value-map :value)]}
  (call :put 
        (format "%s/items/%s" cache-name key) 
        {:body (generate-string value-map)}  
        token 
        project-id))

;; /projects/ {Project ID}/caches/ {Cache Name}/items/{Key}/increment POST Increment an Item's value
(defn increment-item-in-cache
  [token project-id cache-name key amount]
  (call :post (format "%s/items/%s/increment" cache-name key) 
        {:body (generate-string {:amount amount})}  
        token 
        project-id))

;; /projects/ {Project ID}/caches/ {Cache Name}/items/{Key} GET Get an Item from a Cache
(defn get-item-from-cache
  [token project-id cache-name key]
  (call :get (format "%s/items/%s" cache-name key) {} token project-id))

;; /projects/ {Project ID}/caches/ {Cache Name}/items/{Key} DELETE Delete an Item from a Cache
(defn delete-item-from-cache
  [token project-id cache-name key]
  (call :delete 
        (format "%s/items/%s" cache-name key) 
        {} 
        token 
        project-id))




