(ns oidc.context
  (:require ["react" :as react]
            [oidc.core :as oidc]
            [reagent.core :as r]))


(defonce context (react/createContext #js {:email "Fake"}))

(defn user-render-fn [render] 
  (fn [user-obj] 
    (let [user (js->clj user-obj :keywordize-keys true)]
      (js/console.log "REndered Call:" user)
      (r/as-element (render user)))))

(defn with-current-user [render]
  (let [consumer (r/adapt-react-class (.-Consumer context))
        render-fn (user-render-fn render)]
    (js/console.log "In Consumer")
    [consumer {} render-fn]))
  

(defn provider [body]
  (let [user (r/atom nil)]
    (js/console.log (clj->js @user))
    (fn []
      (let [user-obj (clj->js @user)]
        (oidc/on-update #(reset! user %))
        [:> (.-Provider context) {:value user-obj} body]))))