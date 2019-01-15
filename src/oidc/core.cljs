(ns oidc.core
  [:require ["oidc-client" :as oidc]
            [clojure.string]])

(set! (-> oidc .-Log .-logger) js/console)
(set! (-> oidc .-Log .-level) (-> oidc .-Log .-level))

(def UserManager (oidc/UserManager. js/OIDC_CONFIG))

(defn login []
  (.signinRedirect UserManager #js {:state "testState"}))

(defn logout []
  (.signoutRedirect UserManager #js {:state "testState"})
  (set! js/window.location "/"))

(defn process-user []
  (let [[_ token] (.split js/window.location.href "#")]
    (if-not (clojure.string/blank? token)
      (-> (.signinRedirectCallback UserManager)
          (.then #(do (js/console.log "Fetched Data:" %) %))
          (.catch #(js/console.info "Loading from Session instead")))
      (js/Promise.resolve nil))))

(defn handle-token-expiring [user] 
  #(reset! user {}))

(defn set-user! [user & {:keys [redirect] :or {redirect false}}]
  (let [process-user-promise #(if (nil? %) (process-user) %)
        login-user-promise   #(if (and (nil? %) redirect) (login) %)
        set-user             #(let [new-user (merge {:access-token (.-id_token %)} (js->clj (.-profile %) :keywordize-keys true))]
                                (reset! user new-user)
                                new-user)]
    (.addAccessTokenExpiring UserManager.events (handle-token-expiring user)) ; removes User data on Token Expiry
    
    (-> (.getUser UserManager)
        (.then process-user-promise)
        (.then login-user-promise)
        (.then set-user)
        (.then #(js/console.log "Updated User" %))
        (.catch #(js/console.log "signinRedirect Failed:" %)))))

