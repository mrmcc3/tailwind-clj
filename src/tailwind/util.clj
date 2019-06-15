(ns tailwind.util
  (:require [clojure.string :as str])
  (:import (com.sangupta.murmur Murmur2)))

(defn split-classes [s]
  (-> (name s) str/trim (str/split #"\s+")))

(defn split-fragments [s]
  (str/split s #"[:-]"))

(defn escape [class]
  (-> (str/replace class #":" "\\\\:")
      (str/replace #"/" "\\\\/")))

(defn rule [& kvs]
  (->> (partition-all 2 kvs)
       (map (fn [[k v]] (str (name k) ":" v ";")))
       (apply str)))

(def pseudo-classes
  #{"group-hover" "focus-within" "hover" "focus" "active"})

(def background-attachments
  #{"fixed" "local" "scroll"})

(def corners
  {"tl" "top-left"
   "tr" "top-right"
   "bl" "bottom-left"
   "br" "bottom-right"})

(def sides
  {"t" #{"tl" "tr"}
   "r" #{"tr" "br"}
   "b" #{"br" "bl"}
   "l" #{"tl" "bl"}})

(def text-align
  #{"left" "center" "right" "justify"})

(defn side-fns [pre v]
  (case (second v)
    nil #(rule pre %)
    \t #(rule (str pre "-top") %)
    \b #(rule (str pre "-bottom") %)
    \l #(rule (str pre "-left") %)
    \r #(rule (str pre "-right") %)
    \x #(rule (str pre "-left") % (str pre "-right") %)
    \y #(rule (str pre "-top") % (str pre "-bottom") %)))

(def padding-fns
  (let [ps ["p" "px" "py" "pt" "pl" "pr" "pb"]]
    (zipmap ps (map (partial side-fns "padding") ps))))

(def margin-fns
  (let [ps ["m" "mx" "my" "mt" "ml" "mr" "mb"]]
    (zipmap ps (map (partial side-fns "margin") ps))))

(def overflow
  #{"auto" "hidden" "visible" "scroll"})

(def border-sides
  {"t" "top" "l" "left" "r" "right" "b" "bottom"})

(def border-style
  #{"solid" "dashed" "dotted" "none"})

(defn emotion-hash
  "Replicate emotion hash. useful for server side rendering"
  [s]
  (let [bytes (.getBytes s)
        hash  (Murmur2/hash bytes (count bytes) (count s))]
    (str "css-" (Long/toString hash 36))))

