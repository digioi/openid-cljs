(ns site.core
  [:require 
   [reagent.core :as r]
   [oidc.core :as oidc]
   [site.components :as themed]])

(def user (r/atom {}))

(defn home-page []
  (js/console.log "Current User" (clj->js @user))
  [themed/body
   [themed/div "CurrentUser: " 
    (if (empty? @user)
      [themed/text "Not Set"]
      [themed/ul
       [themed/li "Email: " (:email @user)]])]
   
   [themed/button {:on-click oidc/login} "Login"]
   [themed/button {:on-click oidc/logout} "Logout"]])
  

(defn init []
  (oidc/set-user! user)
  (r/render [home-page] (js/document.getElementById "app")))