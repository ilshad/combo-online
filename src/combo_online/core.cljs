(ns combo-online.core
  (:require [om-tools.dom :as dom :include-macros true]
            [om.core :as om :include-macros true]
            [combo-online.online :as online]))

(enable-console-print!)

(defn header []
  (dom/div {:class "navbar"}
    (dom/div {:class "navbar-header"}
      (dom/span {:class "navbar-brand"} "Combo Online"))
    (dom/ul {:class "nav navbar-nav navbar-right nav-pills"}
      (dom/li (dom/a {:href "#"} "About"))
      (dom/li (dom/a {:href "#"} "Help")))))

(defn root [app owner]
  (om/component
    (dom/div {:class "container-fluid"} (header)
      (dom/div {:class "row"} (online/view)))))

(defn main []
  (om/root root nil {:target js/document.body}))

(set! (.-onload js/window) main)
