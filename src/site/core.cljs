(ns site.core
  [:require [reagent.core :as r]
            [oidc.core :as oidc]])

(def user (r/atom {}))

(defn home-page []
  (js/console.log "Current User" (clj->js @user))
  [:div
   [:div "CurrentUser: " (:email @user)]
   [:button {:on-click oidc/login} "Login"]
   [:button {:on-click oidc/logout} "Logout"]])
  

(defn init []
  (oidc/set-user! user)
  (r/render [home-page] (js/document.getElementById "app")))