(ns combo-online.online
  (:require [combo.api :as combo]
            [cljs.core.async :as async]
            [cljs.core.match :refer-macros [match]]
            [om-tools.dom :as dom :include-macros true]
            [om.core :as om :include-macros true]))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Utils

(defn- remove-from-vector [v pos]
  (vec (concat (subvec v 0 pos) (subvec v (inc pos) (count v)))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Behavior

(def empty-unit :foo)

(defn- messages [state]
  [[:constructor ::units (:units state)]])

(defn- init []
  (let [state {:units [empty-unit]}]
    [state (messages state)]))

(defn- add [state]
  (let [state (update-in state [:units] #(conj % empty-unit))]
    [state (messages state)]))

(defn- delete [state id]
  (let [state (update-in state [:units] #(remove-from-vector % id))]
    [state (messages state)]))

(defn behavior [state message]
  (match message
    [:combo/init :data _] (init)
    [:add :click _]       (add state)
    [[::unit id]]         (delete state id)
    :else [state []]))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Render

(def render-functions
  {:input    {:render combo/input    :schema {}}
   :select   {:render combo/select   :schema {}}
   :textarea {:render combo/textarea :schema {}}
   :checkbox {:render combo/checkbox :schema {}}
   :button   {:render combo/button   :schema {}}
   :form     {:render combo/form     :schema {}}
   :span     {:render combo/span     :schema {}}
   :div      {:render combo/div      :schema {}}
   :a        {:render combo/a        :schema {}}})

(defn- return [owner message]
  (fn [e]
    (async/put! (om/get-state owner :return-chan) message)
    (.preventDefault e)))

(defn- field
  ([label]
   (field label
     (dom/input {:type "text" :class "form-control"})))
  ([label dom]
   (dom/div {:class "form-group"}
     (dom/div {:class "col-xs-4"}
       (dom/label {:class "control-label"} label))
     (dom/div {:class "col-xs-8"} dom))))

(defn- unit-form [owner]
  (fn [unit id]
    (dom/div {:class "unit form-horizontal"}
      (dom/div {:class "form-group"}
        (dom/button {:class "close"
                     :on-click (return owner [[::unit id]])}
          (dom/i {:class "fa fa-times"})))
      (field ":render"
        (dom/select {:class "form-control"}
          (for [[k v] render-functions
                :let [id (name k)]]
            (dom/option {:value id} id))))
      (field ":entity")
      (field ":class")
      (field ":value"))))

(defn render-constructor [owner _]
  (dom/div {:class "box resizable constructor"}
    (map (unit-form owner) (om/get-state owner ::units) (range))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Spec

(def behavior-init-source "
(fn [state event]
  (match event
    [:click-me :click _] [state [[:alert :value (rand-int 1000)]]]
    :else [state []]))
")

(def units
  [{:render combo/div
    :class "col-xs-3"
    :units [{:render combo/button
             :entity :add
             :class "btn btn-primary btn-block"
             :value "Add unit"}
            {:render render-constructor
             :entity :constructor}]}
   {:render combo/div
    :class "col-xs-4"
    :units [{:render combo/div
             :entity :input
             :class "box messages no-margin-top"}
            {:render combo/textarea
             :entity :behavior
             :value behavior-init-source
             :class "box"}
            {:render combo/div
             :entity :output
             :class "box messages"}
            {:render combo/div
             :entity :spec
             :value "foo bar"
             :class "box resizable source"}]}
   {:render combo/div
    :class "col-xs-5"
    :value "Result..."}])

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Public

(defn view []
  (om/build combo/view nil
    {:opts {:layout combo/bootstrap-layout
            :behavior behavior
            :debug? true
            :units units}}))
