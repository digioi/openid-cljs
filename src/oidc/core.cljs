(ns oidc.core
  [:require 
   ["oidc-client" :as oidc]
   [clojure.spec.alpha :as s]
   [clojure.string]])

(set! (-> oidc .-Log .-logger) js/console)
(set! (-> oidc .-Log .-level) (-> oidc .-Log .-level))

(def UserManager (oidc/UserManager. js/OIDC_CONFIG))

(defn login []
  (.signinRedirect UserManager #js {:state "testState"}))

(defn logout []
  (.signoutRedirect UserManager #js {:state "testState"})
  (set! js/window.location "/"))

(defn process-user [user]
  (let [[_ token] (.split js/window.location.href "#")]
    (if-not (clojure.string/blank? token)
      (-> (.signinRedirectCallback UserManager)
          (.then #(do (js/console.log "Fetched Data:" %) %))
          (.catch #(js/console.info "Loading from Session instead")))
      (js/Promise.resolve user))))

(defn handle-token-expiring [user redirect] 
  #(do
     (js/console.log user "is expiring" %) 
     (cond
       js/OIDC_CONFIG.automaticSilentRenew (.signinSilent UserManager)
       redirect (login)
       :else user)))

(defn handle-token-error [user]
  #(js/console.log %))

(defn renew-user-promise [user-obj]
  (js/console.log user-obj)
  user-obj)

; (defn atom? [o] (instance? clojure.lang.IAtom o))
; ; (s/def ::promise #(= (class %) (class (promise))))

; (s/fdef set-user!
;   :args (s/cat :user atom?
;                :kwargs (s/keys* :refresh? boolean?)))

(defn set-user! [user & {:keys [redirect] :or {redirect false}}]
  (let [process-user-promise #(if (nil? %) (process-user user) %)
        login-user-promise   #(if (and (nil? %) redirect) (login) %)
        set-user             #(if-not (nil? %)
                                (let [new-user (merge {:access-token (.-id_token %)} (js->clj (.-profile %) :keywordize-keys true))]
                                  (reset! user new-user)
                                  new-user)
                                %)]
    (.addAccessTokenExpiring UserManager.events (handle-token-expiring user redirect)) ; removes User data on Token Expiry
    (.addSilentRenewError UserManager.events (handle-token-error user)) ; handle on Token Error
    (.addAccessTokenExpired UserManager.events (handle-token-expiring user redirect)) ; removes User data on Token Expiry

    (-> (.getUser UserManager)
        (.then process-user-promise)
        (.then renew-user-promise)
        ; (.then login-user-promise)
        (.then #(do (js/console.log (clj->js %) ) %))
        (.then set-user)
        (.catch #(js/console.log "signinRedirect Failed:" %)))))

