(ns tailwind.config
  (:require
    [clojure.edn :as edn]
    [clojure.java.io :as io]
    [meta-merge.core :as mm]))

(defn negate [m]
  (reduce-kv #(assoc %1 (str "-" %2) (str "-" %3)) {} m))

(defn final [v f & args]
  (if (:final (meta v)) v (apply f v args)))

(defn border-color-default [cfg]
  (let [default (get-in cfg ["border-color" "default"])]
    (if (vector? default)
      (assoc-in cfg ["border-color" "default"]
                (get-in cfg default "currentColor"))
      cfg)))

(defn expand [{:strs [colors spacing] :as cfg}]
  (-> (border-color-default cfg)
      (update "background-color" final merge colors)
      (update "border-color" final merge colors)
      (update "text-color" final merge colors)
      (update "height" final merge spacing)
      (update "padding" final merge spacing)
      (update "width" final merge spacing)
      (update "margin" final merge spacing (negate spacing))))

(def init-defaults
  (-> "tailwind/defaults.edn" io/resource slurp edn/read-string))

(def init-user
  (some-> "tailwind.edn" io/resource slurp edn/read-string))

(def default-config
  (expand init-defaults))

(def config
  (expand (mm/meta-merge init-defaults init-user)))

(defn cfg-> [& paths]
  (get-in config (map name paths)))
