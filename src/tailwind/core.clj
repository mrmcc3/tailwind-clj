(ns tailwind.core
  (:require
    [clojure.core.match :refer [match]]
    [clojure.pprint :refer [pprint]]
    [tailwind.config :as cfg :refer [cfg->]]
    [tailwind.base :as base]
    [tailwind.util :as u]
    [tailwind.transform :as t]
    [clojure.java.io :as io]
    [clojure.string :as str])
  (:import (java.io Writer StringWriter)))

;; emotion style

(defn tw->emotion [strings]
  (->> (mapcat u/split-classes strings)
       (map u/split-fragments)
       (map t/fragments->emotion)
       (apply str)))

(defmacro tw
  "Given one or more strings containing whitespace separated tailwind classes
  return a string of css.

  The intention is that the result can be processed by a css-in-js library
  such as emotion. Example (tw \"w-full max-w-sm my-3\")"
  [& strings]
  (tw->emotion strings))

;; extract css

(def empty-rules
  (reduce-kv
    #(assoc %1 %3 (sorted-map))
    (sorted-map nil (sorted-map))
    (cfg-> :screens)))

(def rules (atom empty-rules))

(def mwm (partial merge-with merge))

(defmacro tw! [& strings]
  (let [classes (mapcat u/split-classes strings)]
    (apply swap! rules mwm (map t/class->css classes))
    (str/join " " classes)))

(defn write-rules! [^Writer writer rules base?]
  (with-open [out writer]
    (when base?
      (.write out (base/styles))
      (.write out "\n\n"))
    (doseq [[bp bp-rules] rules]
      (when (seq bp-rules)
        (when bp (.write out (format "\n@media (min-width: %spx) {\n" bp)))
        (doseq [[_ rule] bp-rules] (.write out (str rule "\n")))
        (when bp (.write out "}\n"))))))

(defmacro spit-css! [path]
  (io/make-parents path)
  (write-rules! (io/writer path) @rules true))

(defn tw->css
  ([strings] (tw->css strings false))
  ([strings base?]
   (let [sw (StringWriter.)
         rs (->> (mapcat u/split-classes strings)
                 (map t/class->css)
                 (apply mwm empty-rules))]
     (write-rules! sw rs base?)
     (str sw))))

(defmacro base [] (base/styles))

;; cli entry point

(defn -main [& args]
  (match (vec args)
    ["base"] (println (base/styles))
    ["tw" & rest] (println (tw->emotion rest))
    ["tw!" & rest] (print (tw->css rest))
    ["hash" & rest] (-> rest tw->emotion u/emotion-hash println)
    ["default" & rest] (pprint (get-in cfg/default-config rest))
    ["config" "keys"] (-> cfg/config keys sort pprint)
    ["config" & rest] (pprint (get-in cfg/config rest))))
