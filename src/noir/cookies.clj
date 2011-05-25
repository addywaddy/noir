(ns noir.cookies
  (:refer-clojure :exclude [get remove])
  (:use ring.middleware.cookies))

(def *cur-cookies* nil)
(def *new-cookies* nil)

(defn- keyword->str [kw]
  (subs (str kw) 1))

(defn put! 
  "Add a new cookie whose name is k and has the value v. If v is a string
  a cookie map is created with :path '/'. To set custom attributes, such as,
  expires provide a map as v."
  [k v]
  (let [props (if (map? v)
                v
                {:value v :path "/"})]
    (swap! *new-cookies* assoc k props)))

(defn get
  "Get the value of a cookie from the request. k can either be a string or keyword"
  [k]
  (let [str-k (if (keyword? k)
                (keyword->str k)
                k)]
    (get-in *cur-cookies* [str-k :value])))

(defn noir-cookies [handler]
  (fn [request]
    (binding [*cur-cookies* (:cookies request)
              *new-cookies* (atom {})]
      (let [final (handler request)]
        (assoc final :cookies (merge (:cookies final) @*new-cookies*))))))

(defn wrap-noir-cookies [handler]
  (-> handler
    (noir-cookies)
    (wrap-cookies)))
