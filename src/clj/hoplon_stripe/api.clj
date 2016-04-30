(ns hoplon-stripe.api
  (:require [castra.core :refer [defrpc]]
            [hoplon-stripe.stripe :as stripe]))

(defrpc handle-charge [data]
  (-> data stripe/handle-charge)) ;;; CHANGER STR
