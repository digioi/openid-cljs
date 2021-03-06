(ns site.core
  [:require 
   [reagent.core :as r]
   [oidc.context :as user-context]
   [oidc.core :as oidc]
   [site.components :as themed]])

(defn userLabel [user] 
  [themed/div "CurrentUser: "
   (if (empty? user)
     [themed/text "Not Set"] 
     [themed/ul
      [themed/li "Email: " (:email user)]
      [themed/li "AccessToken: " (:access-token user)]
      [themed/li "JWT: " (:jwt user)]
      [themed/li "Groups: " [:ul (map (fn [g] [:li g]) (:groups user))]]])])

(defn authentication-button [user]
  (if (empty? user)
    [themed/button {:on-click oidc/login} "Login"]
    [themed/button {:on-click oidc/logout} "Logout"])
  )

(defn home-page []
  [themed/body
    [user-context/with-current-user userLabel]
    [user-context/with-current-user authentication-button]
    [themed/button {:on-click oidc/reissue-token} "Refresh Token"]])
  
(defn init []
  (r/render [user-context/provider [home-page]] (js/document.getElementById "app")))

