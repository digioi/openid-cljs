(ns site.core
  [:require 
   [reagent.core :as r]
   [oidc.context :as user-context]
   [oidc.core :as oidc]
   [site.components :as themed]])

(defn userLabel [user] 
  [themed/div "CurrentUser: "
   (js/console.log "UserLabel" user)
   (if (empty? user)
     [themed/text "Not Set"]
     [themed/ul
      [themed/li "Email: " (:email user)]])])

(defn home-page []
  [themed/body
    [user-context/with-current-user userLabel]
    [themed/button {:on-click oidc/login} "Login"]
    [themed/button {:on-click oidc/logout} "Logout"]
    [themed/button {:on-click oidc/reissue-token} "reset"]])
  
(defn init []
  (r/render [user-context/provider [home-page]] (js/document.getElementById "app")))

