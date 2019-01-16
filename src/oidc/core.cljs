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
  (-> (.signinSilent UserManager)
      (.then #(.getUser UserManager))))

(defn get-token [user redirect]
  #(cond
       js/OIDC_CONFIG.automaticSilentRenew (reissue-token)
       redirect (login)
       :else user))

(defn set-user! [user & {:keys [redirect] :or {redirect false}}]
  (let [process-user-promise #(if (nil? %) (process-user user) %)
        login-user-promise   #(if (and (nil? %) redirect) (get-token user redirect) %)
        set-user             #(if-not (or (nil? %) (nil? (.-id_token %)))
                                (let [new-user (merge {:jwt (.-id_token %)} (js->clj (.-profile %) :keywordize-keys true))]
                                  (reset! user new-user)
                                  new-user)
                                %)
        update-user #(update-user process-user-promise set-user login-user-promise)]
    (update-user)
    (.addUserSignedOut UserManager.events #(reset! user {}))
    (.addAccessTokenExpiring UserManager.events (get-token user redirect)) ; removes User data on Token Expiry
    (.addSilentRenewError UserManager.events js/console.log) ; handle on Token Error
    (.addAccessTokenExpired UserManager.events (get-token user redirect)) ; removes User data on Token Expiry
    (.addUserLoaded UserManager.events update-user)))

