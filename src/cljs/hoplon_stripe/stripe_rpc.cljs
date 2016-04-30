(ns hoplon-stripe.stripe-rpc
  (:require-macros
    [javelin.core :refer [defc defc= cell=]])
  (:require
   [javelin.core]
   [castra.core :refer [mkremote]]
   [clojure.set :as set :refer [rename-keys]]
   [cognitect.transit :as transit]))

(def r (transit/reader :json))

;;; Static data

(def stripe-pk "pk_test_kkkkkkkkkkkkkkkkkkkkkkkk")

(def stripe-url "https://js.stripe.com/v2/")


;;; Cells

(defc order-data nil)
(defc order-error nil)
(defc order-loading [])

(defc success-message nil)
(defc process-message nil)
(defc error-message nil)


;;; Handlers

(defn reset-all! [messages new-val]
  (doseq [m messages] (reset! m new-val)))


;;; Stripe RPC handler

(def handle-charge (mkremote 'hoplon-stripe.api/handle-charge
                             order-data
                             order-error
                             order-loading))

(defn handle-stripe-message [data]
  (when-let [s (:status data)]
    (do
      (reset-all! [success-message process-message error-message] nil)
      (case s
        200 (do
              (reset! success-message "Transaction accepted!")
              (:body data))
        (let [e-data (->> data :body (transit/read r))
              e-message (-> e-data (get "error") (get "message"))]
          (reset! error-message (str "Transaction failed. "
                                     e-message
                                     ". Please try again.")))))))

(cell= (handle-stripe-message order-data))

;;; Stripe JS handler

(defn handle-stripe-js-response
  [form-data token-data status response]
  (let [r (js->clj response :keywordize-keys true)]
    (do
      (reset-all! [error-message process-message success-message] nil)
      (case status
        200 (do (reset! process-message "Card's informations seem ok...")
                (swap! form-data dissoc :card-number :cvv :exp-month :exp-year)
                (reset! token-data {:token r
                                    :form @form-data})
                (->> (handle-charge @token-data))
                (swap! process-message str " Connecting to payment processor..."))
        (reset! error-message (-> r :error :message) {})))))

(defn get-stripe-token [form-data token-data key-mappings]
  (let [norm-data (rename-keys @form-data key-mappings)]
    (do
      (reset-all! [success-message process-message error-message] nil)
      (.createToken (.-card js/Stripe) (clj->js norm-data)
                    (partial handle-stripe-js-response form-data token-data)))))
