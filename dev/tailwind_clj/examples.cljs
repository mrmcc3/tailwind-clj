(ns ^:figwheel-hooks tailwind-clj.examples
  (:require-macros
    [tailwind-clj.macros :refer [tw base]])
  (:require
    [cljsjs.emotion]
    [uix.core.alpha :as uix]
    [uix.dom.alpha :as uix.dom]))

;; Alerts

; https://tailwindcss.com/components/alerts/#modern-with-badge
(defn modern-alert []
  [:div {:css (tw "bg-indigo-900 text-center py-4 lg:px-4")}
   [:div {:css  (tw "p-2 bg-indigo-800 items-center text-indigo-100 leading-none"
                    "lg:rounded-full flex lg:inline-flex")
          :role "alert"}
    [:span {:css (tw "flex rounded-full bg-indigo-500 uppercase px-2 py-1"
                     "text-xs font-bold mr-3")} "New"]
    [:span {:css (tw "font-semibold mr-2 text-left flex-auto")}
     "Get the coolest t-shirts from our brand new store"]
    [:svg {:css     (tw "fill-current opacity-75 h-4 w-4")
           :xmlns   "http://www.w3.org/2000/svg"
           :viewBox "0 0 20 20"}
     [:path {:d "M12.95 10.707l.707-.707L8 4.343 6.586 5.757 10.828 10l-4.242 4.243L8 15.657l4.95-4.95z"}]]]])

;; Cards

; https://tailwindcss.com/components/cards#stacked
(defn card-1 []
  (let [s1 (tw "inline-block bg-gray-200 rounded-full px-3 py-1 "
               "text-sm font-semibold text-gray-700")]
    [:div {:css (tw "max-w-sm rounded overflow-hidden shadow-lg")}
     [:img {:css (tw "w-full") :src "https://tailwindcss.com/img/card-top.jpg"}]
     [:div {:css (tw "px-6 py-4")}
      [:div {:css (tw "font-bold text-xl mb-2")} "The Coldest Sunset"]
      [:p {:css (tw "text-gray-700 text-base")}
       "Lorem ipsum dolor sit amet, consectetur adipisicing elit. Voluptatibus quia, nulla! Maiores et perferendis eaque, exercitationem praesentium nihil"]]
     [:div {:css (tw "px-6 py-4")}
      [:span {:css [s1 (tw "mr-2")]} "#photography"]
      [:span {:css [s1 (tw "mr-2")]} "#travel"]
      [:span {:css s1} "#winter"]]]))

; https://tailwindcss.com/components/cards#horizontal
(defn card-2 []
  [:div {:css (tw "max-w-sm w-full lg:max-w-full lg:flex")}
   [:div
    {:css [{:background-image "url('https://tailwindcss.com/img/card-left.jpg')"
            :title            "Woman holding a mug"}
           (tw "h-48 lg:h-auto lg:w-48 flex-none bg-cover rounded-t"
               "lg:rounded-t-none lg:rounded-l text-center overflow-hidden")]}]
   [:div {:css (tw "border-r border-b border-l border-gray-400 lg:border-l-0"
                   "lg:border-t lg:border-gray-400 bg-white rounded-b"
                   "lg:rounded-b-none lg:rounded-r p-4 flex flex-col"
                   "justify-between leading-normal")}
    [:div {:css (tw "mb-8")}
     [:p {:css (tw "text-sm text-gray-600 flex items-center")}
      [:svg
       {:css   (tw "fill-current text-gray-500 w-3 h-3 mr-2")
        :xmlns "http://www.w3.org/2000/svg" :viewBox "0 0 20 20"}
       [:path {:d "M4 8V6a6 6 0 1 1 12 0v2h1a2 2 0 0 1 2 2v8a2 2 0 0 1-2 2H3a2 2 0 0 1-2-2v-8c0-1.1.9-2 2-2h1zm5 6.73V17h2v-2.27a2 2 0 1 0-2 0zM7 6v2h6V6a3 3 0 0 0-6 0z"}]] "Members only"]
     [:div {:css (tw "text-gray-900 font-bold text-xl mb-2")}
      "Can coffee make you a better developer?"]
     [:p {:css (tw "text-gray-700 text-base")}
      "Lorem ipsum dolor sit amet, consectetur adipisicing elit. Voluptatibus quia, nulla! Maiores et perferendis eaque, exercitationem praesentium nihil."]]
    [:div {:css (tw "flex items-center")}
     [:img {:css (tw "w-10 h-10 rounded-full mr-4")
            :src "https://tailwindcss.com/img/jonathan.jpg"
            :alt "Avatar of Jonathan Reinink"}]
     [:div {:css (tw "text-sm")}
      [:p {:css (tw "text-gray-900 leading-none")} "Jonathan Reinink"]
      [:p {:css (tw "text-gray-600")} "Aug 18"]]]]])

;; forms

; https://tailwindcss.com/components/forms#inline-form
(defn form-1 []
  (let [d1 (tw "md:flex md:items-center mb-6")
        l1 (tw "block text-gray-500 font-bold md:text-right mb-1 md:mb-0 pr-4")
        i1 (tw "bg-gray-200 appearance-none border-2 border-gray-200"
               "rounded w-full py-2 px-4 text-gray-700 leading-tight"
               "focus:outline-none focus:bg-white focus:border-purple-500")
        b1 (tw "shadow bg-purple-500 hover:bg-purple-400 focus:shadow-outline"
               "focus:outline-none text-white font-bold py-2 px-4 rounded")]
    [:form {:css (tw "w-full max-w-sm")}
     [:div {:css d1}
      [:div {:css (tw "md:w-1/3")}
       [:label {:for "inline-full-name" :css l1} "Full Name"]]
      [:div {:css (tw "md:w-2/3")}
       [:input#inline-full-name
        {:type "text" :placeholder "Jane Doe" :css i1}]]]
     [:div {:css d1}
      [:div {:css (tw "md:w-1/3")}
       [:label {:for "inline-username" :css l1} "Password"]]
      [:div {:css (tw "md:w-2/3")}
       [:input#inline-username
        {:css i1 :type "password" :placeholder "******************"}]]]
     [:div {:css d1}
      [:div {:css (tw "md:w-1/3")}]
      [:label {:css (tw "md:w-2/3 block text-gray-500 font-bold")}
       [:input {:type "checkbox" :css (tw "mr-2 leading-tight")}]
       [:span {:css (tw "text-sm")} "Send me your newsletter!"]]]
     [:div {:css (tw "md:flex md:items-center")}
      [:div {:css (tw "md:w-1/3")}]
      [:div {:css (tw "md:w-2/3")}
       [:button {:type "button" :css b1} "Sign Up"]]]]))

; https://tailwindcss.com/components/forms#underline-form
(defn form-2 []
  (let [i1 (tw "appearance-none bg-transparent border-none w-full"
               "text-gray-700 mr-3 py-1 px-2 leading-tight focus:outline-none")
        b1 (tw "flex-shrink-0 bg-teal-500 hover:bg-teal-700 border-teal-500"
               "hover:border-teal-700 text-sm border-4 text-white py-1 px-2 rounded")
        b2 (tw "flex-shrink-0 border-transparent border-4 text-teal-500"
               "hover:text-teal-800 text-sm py-1 px-2 rounded")]
    [:form {:css (tw "w-full max-w-sm")}
     [:div {:css (tw "flex items-center border-b border-teal-500 py-2")}
      [:input {:css i1 :type "text" :placeholder "Jane Doe" :aria-label "Full name"}]
      [:button {:css b1 :type "button"} "Sign Up"]
      [:button {:css b2 :type "button"} "Cancel"]]]))

; https://tailwindcss.com/components/navigation/#responsive-header
(defn header []
  [:nav {:css (tw "flex items-center justify-between flex-wrap bg-teal-500 p-6")}
   [:div {:css (tw "flex items-center flex-shrink-0 text-white mr-6")}
    [:svg {:css     (tw "fill-current h-8 w-8 mr-2")
           :width   "54"
           :height  "54"
           :viewBox "0 0 54 54"
           :xmlns   "http://www.w3.org/2000/svg"}
     [:path {:d "M13.5 22.1c1.8-7.2 6.3-10.8 13.5-10.8 10.8 0 12.15 8.1 17.55 9.45 3.6.9 6.75-.45 9.45-4.05-1.8 7.2-6.3 10.8-13.5 10.8-10.8 0-12.15-8.1-17.55-9.45-3.6-.9-6.75.45-9.45 4.05zM0 38.3c1.8-7.2 6.3-10.8 13.5-10.8 10.8 0 12.15 8.1 17.55 9.45 3.6.9 6.75-.45 9.45-4.05-1.8 7.2-6.3 10.8-13.5 10.8-10.8 0-12.15-8.1-17.55-9.45-3.6-.9-6.75.45-9.45 4.05z"}]]
    [:span {:css (tw "font-semibold text-xl tracking-tight")} "Tailwind CSS"]]
   [:div {:css (tw "block lg:hidden")}
    [:button {:css (tw "flex items-center px-3 py-2 border rounded text-teal-200 border-teal-400 hover:text-white hover:border-white")}
     [:svg {:css     (tw "fill-current h-3 w-3")
            :viewBox "0 0 20 20"
            :xmlns   "http://www.w3.org/2000/svg"}
      [:title "Menu"]
      [:path {:d "M0 3h20v2H0V3zm0 6h20v2H0V9zm0 6h20v2H0v-2z"}]]]]
   [:div {:css (tw "w-full block flex-grow lg:flex lg:items-center lg:w-auto")}
    (let [a (tw "block mt-4 lg:inline-block lg:mt-0 text-teal-200 hover:text-white mr-4")]
      [:div {:css (tw "text-sm lg:flex-grow")}
       [:a {:href "#responsive-header" :css a} "Docs"]
       [:a {:href "#responsive-header" :css a} "Examples"]
       [:a {:href "#responsive-header" :css a} "Blog"]])
    [:div
     [:a {:href "#"
          :css  (tw "inline-block text-sm px-4 py-2 leading-none border rounded"
                    "text-white border-white hover:border-transparent hover:text-teal-500"
                    "hover:bg-white mt-4 lg:mt-0")}
      "Download"]]]])


(defn spacer []
  [:div {:css (tw "my-4 w-full border-b border-gray-400")}])

(defn examples []
  [:div {:css (tw "flex flex-col items-center p-4")}
   [header]
   [spacer]
   [card-1]
   [spacer]
   [card-2]
   [spacer]
   [modern-alert]
   [spacer]
   [form-1]
   [spacer]
   [form-2]
   [spacer]
   [:pre [:code "(println \"test mono font config\")"]]])

;; setup

(defn ^:after-load render []
  (uix.dom/render [examples] js/root))

;; from uix.recipes.dynamic-styles
(defn css-uix-transform [attrs]
  (if-not (contains? attrs :css)
    attrs
    (let [classes (:class attrs)
          css     (:css attrs)
          class   (->> (clj->js css)
                       js/emotion.css
                       (str classes " "))]
      (-> (dissoc attrs :css)
          (assoc :class class)))))

(defonce startup
  (do (js/emotion.injectGlobal (base))
      (uix/add-transform-fn css-uix-transform)
      (render)
      true))
