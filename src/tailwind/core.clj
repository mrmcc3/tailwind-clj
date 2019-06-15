(ns tailwind.core
  (:require
    [clojure.core.match :refer [match]]
    [clojure.pprint :refer [pprint]]
    [tailwind.config :as cfg :refer [cfg->]]
    [tailwind.base :as base]
    [tailwind.util :as u]
    [tailwind.transform :as t]
    [clojure.java.io :as io]
    [clojure.string :as str]))

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

;; generate css files

(def build-files
  (delay
    (let [out  (io/file (cfg-> :output-file))
          dir  (.getParentFile out)
          base (io/file dir "tw-build" "base.css")]
      (io/make-parents out)
      (spit out "@import 'tw-build/base.css';\n")
      (io/make-parents base)
      (spit base (base/styles))
      (reduce
        (fn [acc [screen _]]
          (let [path (format "tw-build/%s.css" screen)
                file (io/file dir path)
                rule (format "@import '%s';\n" path)]
            (spit out rule :append true)
            (spit file "")
            (assoc acc screen file)))
        {"base" base "out" out}
        (sort-by second (cfg-> :screens))))))

(def persisted (atom #{}))

(defmacro tw!
  "Given one or more strings containing whitespace separated tailwind classes
  generate tailwind css and append it to the build files.

  Return a single string with all the tailwind classes."
  [& strings]
  (let [classes (mapcat u/split-classes strings)]
    (doseq [class classes]
      (when-not (contains? @persisted class)
        (doseq [[screen rule] (t/fragments->css class (u/split-fragments class))]
          (spit (get @build-files screen) rule :append true))
        (swap! persisted conj class)))
    (str/join " " classes)))

(defmacro base [] (base/styles))

;; cli entry point

(defn -main [& args]
  (match (vec args)
    ["base"] (println (base/styles))
    ["tw" & rest] (println (tw->emotion rest))
    ["hash" & rest] (-> rest tw->emotion u/emotion-hash println)
    ["default" & rest] (pprint (get-in cfg/default-config rest))
    ["config" "keys"] (-> cfg/config keys sort pprint)
    ["config" & rest] (pprint (get-in cfg/config rest))))
