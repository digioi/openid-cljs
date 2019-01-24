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

(defn update-user [process-user-promise set-user login-user-promise]
  (-> (.getUser UserManager)
        (.then process-user-promise)
        (.then login-user-promise)
        (.then #(do (js/console.log (clj->js %) ) %))
        (.then set-user)
        (.catch #(js/console.log "signinRedirect Failed:" %))))

(defn reissue-token []
  (when js/OIDC_CONFIG.automaticSilentRenew
    (-> (.signinSilent UserManager)
        (.then #(.getUser UserManager)))))

(defn get-token [user redirect]
  #(cond
       js/OIDC_CONFIG.automaticSilentRenew (reissue-token)
       redirect (login)
       :else user))

(defn getUser [& [update-fn]]
  (let [update-fn (or update-fn identity)]
    (-> (.getUser UserManager)
        (.then #(if (nil? %) (process-user) %))
        (.then #(merge {:jwt (.-id_token %) "access-token" (.-access_token %)} (js->clj (.-profile %))))
        (.then update-fn))))

(defn on-update [listener-fn]
  (.addUserLoaded UserManager.events #(getUser listener-fn))
  (.addUserUnloaded UserManager.events #(listener-fn nil))
  (.addUserSignedOut UserManager.events #(listener-fn nil))
  (.addSilentRenewError UserManager.events js/console.error))

(.addAccessTokenExpiring UserManager.events reissue-token)
(.addAccessTokenExpired UserManager.events #(if js/OIDC_CONFIG.automaticSilentRenew
                                              (reissue-token)
                                              (.removeUser UserManager)))

