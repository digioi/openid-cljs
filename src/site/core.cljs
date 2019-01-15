(ns site.core
  [:require [reagent.core :as r]
            [oidc.core :as oidc]])

(def user (r/atom {}))

(def themed-body :div.p-2)
(def themed-button :button.bg-green.p-2.m-2.rounded-lg.text-white.hover:bg-green-dark)

(defn home-page []
  (js/console.log "Current User" (clj->js @user))
  [:div
   [themed-body "CurrentUser: "
    [:ul
     [:li "Email: " (:email @user)]]]
   
   [themed-button {:on-click oidc/login} "Login"]
   [themed-button {:on-click oidc/logout} "Logout"]])
  

(defn init []
  (oidc/set-user! user)
  (r/render [home-page] (js/document.getElementById "app")))