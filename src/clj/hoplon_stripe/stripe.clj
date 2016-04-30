(ns hoplon-stripe.stripe
  (:require [clj-http.client :as client]
            [ring.util.codec :refer [form-encode]]))


(def sk-token "sk_test_kkkkkkkkkkkkkkkkkkkkkkkkk")

(defn create-charge [body]
  (client/post "https://api.stripe.com/v1/charges"
               {:query-params body
                :headers {:Authorization (str "Bearer " sk-token)}
                :as :x-www-form-urlencoded
                :accept :json
                :throw-exceptions false
                }))

(defn handle-charge [params]
  (let [p params]
    (if (-> p :form :amount)
      (create-charge
       {:source      (-> p :token :id)
        :amount      (-> p :form :amount)
        :currency    "cad"
        :receipt_email (-> p :form :email)
        :description (str "Order from " (-> p :form :email) ": ")
        :metadata    (-> p :form)})
      (-> p :form))))
