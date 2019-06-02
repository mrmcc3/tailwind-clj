### tailwind-clj

A clojure library that transforms [tailwindcss](https://tailwindcss.com/) 
utility classnames into css data (edn) that can be processed by 
css-in-js libraries such as [emotion](https://emotion.sh/docs/introduction)

```clojure
;; tw macro accepts tailwind utility classes
(tw "flex flex-col items-center py-3 text-gray-800")

;; => css data
[{"display" "flex"}
 {"flex-direction" "column"}
 {"align-items" "center"}
 {"padding-bottom" "0.75rem", "padding-top" "0.75rem"}
 {"color" "#2d3748"}]

;; can be processed by emotion in clojurescript
(js/emotion.css (clj->js css-data))
```

The library is very much a work in progress. I created it mainly as a learning
exercise and to experiment with tailwind, emotion and uix.

`dev/tailwind-clj/examples.cljs` has some example components from the tailwind
site. result https://mrmcc3.github.io/tailwind-clj/index.html

