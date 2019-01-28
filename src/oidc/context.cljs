(ns oidc.context
  (:require ["react" :as react]
            [oidc.core :as oidc]
            [reagent.core :as r]))


(defonce context (react/createContext))
(def obj->clj #(js->clj % :keywordize-keys true))

(defn user-render-fn [render] 
  (fn [user-obj]
    (r/as-element (render (obj->clj user-obj))))) ; TODO: Figure out why i have to convert as object

(defn with-current-user [render]
  (let [consumer (r/adapt-react-class (.-Consumer context))
        render-fn (user-render-fn render)]
   [consumer {} render-fn]))
  

(defn provider [body]
  (let [user (r/atom nil)
        update-user #(reset! user %)]
    (.then (oidc/getUser) update-user)
    (fn []
      (oidc/on-update update-user)
      [:> (.-Provider context) {:value @user} body])))