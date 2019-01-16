(ns oidc.silent
  [:require 
   [oidc.core :as oidc ]])

(defn ^:export init []
  (js/console.log "Silent Login")
  (-> (.signinSilentCallback oidc/UserManager)
      (.then js/console.log)
      (.catch js/console.log)))