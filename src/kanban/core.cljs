(ns kanban.core
    (:require
      [reagent.core :as r]
      [reagent.dom :as d]))

(def app-state 
  (r/atom {:columns [{:id (random-uuid)
                      :title "Todos"
                      :cards [{:id (random-uuid)
                               :title "Refresher on Reagent"}
                              {:id (random-uuid)
                               :title "Portfolio"}]}
                     {:id (random-uuid)
                      :title "Improve"
                      :cards [{:id (random-uuid)
                               :title "Work out"}
                              {:id (random-uuid)
                               :title "Read"}]}]}))

(defn auto-focus-input [props]
  (r/create-class {:displayName "AutoFocusInput"
                   :component-did-mount (fn [component]
                                          (.focus (d/dom-node component)))
                   :reagent-render (fn [props]
                                     [:input props])}))

(defn update-title [cursor title]
  (swap! cursor assoc :title title))

(defn stop-editing [cursor]
  (swap! cursor dissoc :editing))

(defn start-editing [cursor]
  (swap! cursor assoc :editing true))

(defn editable [el cursor]
  (let [{:keys [editing title]} @cursor]
    (if editing
      [el {:className "editing"}
       [auto-focus-input 
        {:type  "text"
         :value title
         :on-change    #(update-title cursor (.. % -target -value))
         :on-blur      #(stop-editing cursor)
         :on-key-press #(when (= (.-charCode %) 13)
                          (stop-editing cursor))}]]
      [el {:on-click #(start-editing cursor)}
       title])))

(defn card [cursor]
  [editable :div.card cursor])

(defn add-new-card [col-cur]
  (swap! col-cur update :cards conj {:id (random-uuid)
                                     :title ""
                                     :editing true}))

(defn new-card [col-cur]
  [:div.new-card {:on-click #(add-new-card col-cur)}
   "+ add new card"])

(defn column [col-cur]
  (let [{:keys [cards]} @col-cur]
    [:div.column 
     ^{:key "title"} [editable :h2 col-cur]
     (map-indexed (fn [idx {id :id}]
                    (let [card-cur (r/cursor col-cur [:cards idx])]
                      ^{:key id} [card card-cur]))
                  cards)
     ^{:key "new"} [new-card col-cur]]))

(defn add-new-column [board]
  (swap! board update :columns conj {:id (random-uuid)
                                     :title ""
                                     :cards []
                                     :editing true}))
      
(defn new-column [board]
  [:div.new-column {:on-click #(add-new-column board)}
   "+ add new column"])

(defn board [board]
  [:div.board
   (map-indexed (fn [idx {id :id}]
                 (let [col-cur (r/cursor board  [:columns idx])]
                   ^{:key id} [column col-cur]))
                (:columns @board))
   ^{:key "new"} [new-column board]])
            

(defn mount-root []
  (d/render [board app-state] (.getElementById js/document "app")))

(defn ^:export init! []
  (mount-root))
