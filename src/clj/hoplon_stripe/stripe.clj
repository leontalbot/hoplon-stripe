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

#_(sh/use-token! (:stripe-secret env/config))

#_(sh/use-token! "sk_test_Evoth804PfKzuCF2B1BMNfK5")

#_(defn create-charge [options]
  (sh/post-req "charges" {:stripe-params options}))

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

;; TESTS
#_(create-charge {:source "tok_15p25FCKaFkL2HYPSiWprPzc"
                  :amount "2000"
                  :currency "cad"
                  :description "repl test"
                  :metadata {:name "HI!"}})

#_(create-charge "amount=2000&currency=cad&source=tok_185hdLCKaFkL2HYPoR2I1tHi&description=postman_test")


;; RPC
#_(->> params
     (stripe/handle-charge (-> data/db :bazar :prices))
     stripe/handle-response-after-charge
     (fb/conj-and-return! fb/bazar-inscriptions)
     (views/bazar-step-3 (-> data/db :bazar :prices)))
