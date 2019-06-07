(ns tailwind-clj.macros
  (:require
    [tailwind-clj.config :as config]
    [clojure.core.match :refer [match]]
    [clojure.string :as str])
  (:import
    (com.sangupta.murmur Murmur2)))

;; tailwind helpers

(defn rule [& kvs]
  (->> (partition-all 2 kvs)
       (map (fn [[k v]] (str (name k) ":" v ";")))
       (apply str)))

(defn cfg-> [& paths]
  (get-in config/default (map name paths)))

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

;; tailwind -> css data.
;; the transform is represented using core.match patterns

(def tw->emo
  "Accepts tailwind classname fragments and returns the corresponding css data."
  (memoize
    (fn [fragments]
      (match [fragments]

        ;; responsive breakpoints - media queries

        [[(s :guard (cfg-> :screens)) & rest]]
        (format "@media(min-width: %s){%s}" (cfg-> :screens s) (tw->emo rest))

        ;; pseudo classes

        [[(p :guard pseudo-classes) & rest]]
        (format ":%s{%s}" p (tw->emo rest))

        ;; -------------------- LAYOUT -----------------------

        ;; container

        [["container"]]
        (reduce-kv
          #(str %1 (format "@media(min-width:%s){max-width:%s}" %3 %3))
          "width:100;"
          (cfg-> :screens))

        ;; display

        [["block"]] (rule "display" "block")
        [["inline" "block"]] (rule "display" "inline-block")
        [["inline"]] (rule "display" "inline")
        [["flex"]] (rule "display" "flex")
        [["inline" "flex"]] (rule "display" "inline-flex")
        [["table"]] (rule "display" "table")
        [["table" "row"]] (rule "display" "table-row")
        [["table" "cell"]] (rule "display" "table-cell")
        [["hidden"]] (rule "display" "none")

        ;; float

        [["float" (f :guard #{"right" "left" "none"})]] {"float" f}
        [["clearfix"]] "&::after{content:\"\";display:table;clear:both;}"

        ;; object fit

        [["object" "scale" "down"]] (rule "object-fit" "scale-down")
        [["object" (f :guard #{"contain" "cover" "fill" "none"})]]
        (rule "object-fit" f)

        ;; object position

        [["object" (p :guard #{"top" "left" "right" "bottom" "center"})]]
        (rule "object-position" p)
        [["object" (lr :guard #{"left" "right"}) (tb :guard #{"top" "bottom"})]]
        (rule "object-position" (str lr " " tb))

        ;; overflow

        [["overflow" (o :guard overflow)]] (rule "overflow" o)
        [["overflow" (d :guard #{"x" "y"}) (o :guard overflow)]]
        (rule (str "overflow-" d) o)
        [["scrolling" (o :guard #{"touch" "auto"})]]
        (rule "-webkit-overflow-scrolling" o)

        ;; position

        [[(p :guard #{"static" "fixed" "absolute" "relative" "sticky"})]]
        (rule "position" p)

        ;; top left bottom right

        [[(p :guard #{"top" "left" "bottom" "right"}) (i :guard (cfg-> :inset))]]
        (rule p (cfg-> :inset i))

        [["inset" (i :guard (cfg-> :inset))]]
        (rule "top" i "left" i "bottom" i "right" i)

        [["inset" "x" (i :guard (cfg-> :inset))]] (rule "left" i "right" i)
        [["inset" "y" (i :guard (cfg-> :inset))]] (rule "top" i "bottom" i)

        ;; visibility

        [["visible"]] (rule "visibility" "visible")
        [["invisible"]] (rule "visibility" "hidden")

        ;; z-index

        [["z" (z :guard (cfg-> :z-index))]]
        (rule "z-index" (cfg-> :z-index z))

        ;; ------------------ TYPOGRAPHY ---------------------

        ;; font family

        [["font" (f :guard (cfg-> :font-family))]]
        (rule "font-family" (str/join "," (cfg-> :font-family f)))

        ;; font size

        [["text" (s :guard (cfg-> :font-size))]]
        (rule "font-size" (cfg-> :font-size s))

        ;; font smoothing

        [["antialiased"]]
        (rule "-webkit-font-smoothing" "antialiased"
              "-moz-osx-font-smoothing" "grayscale")
        [["subpixel-antialiased"]]
        (rule "-webkit-font-smoothing" "auto"
              "-moz-osx-font-smoothing" "auto")

        ;; font style

        [["italic"]] (rule "font-style" "italic")
        [["not-italic"]] (rule "font-style" "normal")

        ;; font weight

        [["font" (w :guard (cfg-> :font-weight))]]
        (rule "font-weight" (cfg-> :font-weight w))

        ;; letter spacing

        [["tracking" (s :guard (cfg-> :letter-spacing))]]
        (rule "letter-spacing" (cfg-> :letter-spacing s))

        ;; line height

        [["leading" (h :guard (cfg-> :line-height))]]
        (rule "line-height" (cfg-> :line-height h))

        ;; list style type

        [["list" (s :guard #{"none" "disc" "decimal"})]]
        (rule "list-style-type" s)

        ;; list style position

        [["list" (s :guard #{"inside" "outside"})]]
        (rule "list-style-position" s)

        ;; text align

        [["text" (c :guard text-align)]]
        (rule "text-align" c)

        ;; text color

        [["text" (c :guard (cfg-> :colors)) & rest]]
        (rule "color" (apply cfg-> :colors c rest))

        ;; text decoration

        [["underline"]] (rule "text-decoration" "underline")
        [["line" "through"]] (rule "text-decoration" "line-through")
        [["no" "underline"]] (rule "text-decoration" "none")

        ;; text transform

        [["uppercase"]] (rule "text-transform" "uppercase")
        [["lowercase"]] (rule "text-transform" "lowercase")
        [["capitalize"]] (rule "text-transform" "capitalize")
        [["normal-case"]] (rule "text-transform" "none")

        ;; vertical align

        [["align" (s :guard #{"baseline" "top" "middle" "bottom"})]]
        (rule "vertical-align" s)
        [["align" "text" (s :guard #{"top" "bottom"})]]
        (rule "vertical-align" (str "text-" s))

        ;; whitespace

        [["whitespace" "normal"]] (rule "white-space" "normal")
        [["whitespace" "no" "wrap"]] (rule "white-space" "nowrap")
        [["whitespace" "pre"]] (rule "white-space" "pre")
        [["whitespace" "pre" "line"]] (rule "white-space" "pre-line")
        [["whitespace" "pre" "wrap"]] (rule "white-space" "pre-wrap")

        ;; word break

        [["break" "normal"]] (rule "word-break" "normal" "overflow-wrap" "normal")
        [["break" "words"]] (rule "overflow-wrap" "break-word")
        [["break" "all"]] (rule "word-break" "break-all")
        [["truncate"]] (rule "overflow" "hidden"
                             "text-overflow" "ellipsis"
                             "white-space" "nowrap")

        ;; ------------------ BACKGROUNDS --------------------

        ;; attachment

        [["bg" (a :guard background-attachments)]]
        (rule "background-attachment" a)

        ;; color

        [["bg" (c :guard (cfg-> :background-color)) & rest]]
        (rule "background-color" (apply cfg-> :background-color c rest))

        ;; position

        [["bg" "left" "bottom"]] (rule "background-position" "left bottom")
        [["bg" "left" "top"]] (rule "background-position" "left top")
        [["bg" "right" "bottom"]] (rule "background-position" "right bottom")
        [["bg" "right" "top"]] (rule "background-position" "right top")
        [["bg" (p :guard #{"bottom" "center" "left" "right" "top"})]]
        (rule "background-position" p)

        ;; repeat

        [["bg" "repeat"]] (rule "background-repeat" "repeat")
        [["bg" "no" "repeat"]] (rule "background-repeat" "no-repeat")
        [["bg" "repeat" "x"]] (rule "background-repeat" "repeat-x")
        [["bg" "repeat" "y"]] (rule "background-repeat" "repeat-y")
        [["bg" "repeat" "round"]] (rule "background-repeat" "repeat-round")
        [["bg" "repeat" "space"]] (rule "background-repeat" "repeat-space")

        ;; size

        [["bg" (s :guard (cfg-> :background-size))]]
        (rule "background-size" (cfg-> :background-size s))

        ;; -------------------- BORDERS ----------------------

        ;; color

        [["border" (c :guard (cfg-> :colors)) & rest]]
        (rule "border-color" (apply cfg-> :colors c rest))

        ;; style

        [["border" (s :guard border-style)]]
        (rule "border-style" s)

        ;; width

        [["border"]] (tw->emo ["border" "default"])

        [["border" (s :guard border-sides)]]
        (rule (format "border-%s-width" (border-sides s)) (cfg-> :border-width "default"))

        [["border" (s :guard border-sides) (w :guard (cfg-> :border-width))]]
        (rule (format "border-%s-width" (border-sides s)) (cfg-> :border-width w))

        [["border" (w :guard (cfg-> :border-width))]]
        (rule "border-width" (cfg-> :border-width w))

        ;; radius

        [["rounded"]] (tw->emo ["rounded" "default"])
        [["rounded" (r :guard (cfg-> :border-radius))]]
        (rule "border-radius" (cfg-> :border-radius r))

        [["rounded" (c :guard corners)]] (tw->emo ["rounded" c "default"])
        [["rounded" (c :guard corners) (r :guard (cfg-> :border-radius))]]
        (rule (format "border-%s-radius" (corners c)) (cfg-> :border-radius r))

        [["rounded" (s :guard sides)]] (tw->emo ["rounded" s "default"])
        [["rounded" (s :guard sides) (r :guard (cfg-> :border-radius))]]
        (apply str (map #(tw->emo ["rounded" % r]) (sides s)))

        ;; -------------------- FLEXBOX ----------------------

        ;; direction

        [["flex" "row"]] (rule "flex-direction" "row")
        [["flex" "row" "reverse"]] (rule "flex-direction" "row-reverse")
        [["flex" "col"]] (rule "flex-direction" "column")
        [["flex" "col" "reverse"]] (rule "flex-direction" "column-reverse")

        ;; wrap

        [["flex" "no" "wrap"]] (rule "flex-wrap" "no-wrap")
        [["flex" "wrap"]] (rule "flex-wrap" "wrap")
        [["flex" "wrap" "reverse"]] (rule "flex-wrap" "wrap-reverse")

        ;; align items

        [["items" "stretch"]] (rule "align-items" "stretch")
        [["items" "start"]] (rule "align-items" "flex-start")
        [["items" "center"]] (rule "align-items" "center")
        [["items" "end"]] (rule "align-items" "flex-end")
        [["items" "baseline"]] (rule "align-items" "baseline")

        ;; align content

        [["content" "start"]] (rule "align-content" "flex-start")
        [["content" "center"]] (rule "align-content" "center")
        [["content" "end"]] (rule "align-content" "flex-end")
        [["content" "between"]] (rule "align-content" "space-between")
        [["content" "around"]] (rule "align-content" "space-around")

        ;; align self

        [["self" "auto"]] (rule "align-self" "auto")
        [["self" "start"]] (rule "align-self" "flex-start")
        [["self" "center"]] (rule "align-self" "center")
        [["self" "end"]] (rule "align-self" "flex-end")
        [["self" "stretch"]] (rule "align-self" "stretch")

        ;; justify content

        [["justify" "start"]] (rule "justify-content" "flex-start")
        [["justify" "center"]] (rule "justify-content" "center")
        [["justify" "end"]] (rule "justify-content" "flex-end")
        [["justify" "between"]] (rule "justify-content" "space-between")
        [["justify" "around"]] (rule "justify-content" "space-around")

        ;; flex

        [["flex" (f :guard (cfg-> :flex))]]
        (rule "flex" (cfg-> :flex f))

        ;; grow

        [["flex" "grow"]] (tw->emo ["flex" "grow" "default"])
        [["flex" "grow" (g :guard (cfg-> :flex-grow))]]
        (rule "flex-grow" (cfg-> :flex-grow g))

        ;; shrink

        [["flex" "shrink"]] (tw->emo ["flex" "shrink" "default"])
        [["flex" "shrink" (s :guard (cfg-> :flex-shrink))]]
        (rule "flex-shrink" (cfg-> :flex-shrink s))

        ;; order

        [["order" (o :guard (cfg-> :order))]]
        (rule "order" (cfg-> :order o))

        ;; -------------------- SPACING ----------------------

        ;; padding

        [[(v :guard padding-fns) (p :guard (cfg-> :padding))]]
        ((padding-fns v) (cfg-> :padding p))

        ;; margin

        [[(v :guard margin-fns) (m :guard (cfg-> :margin))]]
        ((margin-fns v) (cfg-> :margin m))
        [["" (v :guard margin-fns) (m :guard (cfg-> :margin))]]
        ((margin-fns v) (cfg-> :margin (str "-" m)))

        ;; -------------------- SIZING -----------------------

        ;; width

        [["w" (w :guard (cfg-> :width))]]
        (rule "width" (cfg-> :width w))

        ;; min width

        [["min" "w" (w :guard (cfg-> :min-width))]]
        (rule "min-width" (cfg-> :min-width w))

        ;; max width

        [["max" "w" (s :guard (cfg-> :max-width))]]
        (rule "max-width" (cfg-> :max-width s))

        ;; height

        [["h" (h :guard (cfg-> :height))]]
        (rule "height" (cfg-> :height h))

        ;; min height

        [["min" "h" (h :guard (cfg-> :min-height))]]
        (rule "min-height" (cfg-> :min-height h))

        ;; max height

        [["max" "h" (h :guard (cfg-> :max-height))]]
        (rule "max-height" (cfg-> :max-height h))

        ;; -------------------- TABLES -----------------------

        ;; border collapse

        [["border" "collapse"]] (rule "border-collapse" "collapse")
        [["border" "separate"]] (rule "border-collapse" "separate")

        ;; table layout

        [["table" "auto"]] (rule "table-layout" "auto")
        [["table" "fixed"]] (rule "table-layout" "fixed")

        ;; ------------------- EFFECTS -----------------------

        ;; box shadows

        [["shadow"]] (tw->emo ["shadow" "default"])
        [["shadow" (s :guard (cfg-> :box-shadow))]]
        (rule "box-shadow" (cfg-> :box-shadow s))

        ;; opacity

        [["opacity" (o :guard (cfg-> :opacity))]]
        (rule "opacity" (cfg-> :opacity o))

        ;; ---------------- INTERACTIVITY --------------------

        ;; appearance

        [["appearance" "none"]] (rule "appearance" "none")
        [["outline" "none"]] (rule "outline" "none")

        ;; cursor

        [["cursor" & rest]]
        (rule "cursor" (cfg-> :cursor (str/join "-" rest)))

        ;; outline

        [["outline" "none"]] (rule "outline" "0")

        ;; pointer events

        [["pointer" "events" "none"]] (rule "pointer-events" "none")
        [["pointer" "events" "auto"]] (rule "pointer-events" "auto")

        ;; resize

        [["resize" "none"]] (rule "resize" "none")
        [["resize"]] (rule "resize" "both")
        [["resize" "y"]] (rule "resize" "vertical")
        [["resize" "x"]] (rule "resize" "horizontal")

        ;; user select

        [["select" (s :guard #{"none" "text" "all" "auto"})]]
        (rule "user-select" s)

        ;; --------------------- SVG -------------------------

        ;; fill

        [["fill" (f :guard (cfg-> :fill))]]
        (rule "fill" (cfg-> :fill f))

        ;; stroke

        [["stroke" (s :guard (cfg-> :stroke))]]
        (rule "stroke" (cfg-> :stroke s))))))

(defn tailwind->emotion
  "given a single tailwind classname split it into fragments
  before invoking core.match to generate the css data"
  [classname]
  (tw->emo (str/split classname #"[:-]")))

(defn split-classes [s]
  (-> (name s) str/trim (str/split #"\s+")))

(defmacro tw
  "Given one or more strings containing whitespace separated tailwind classes
  return a string of css.

  The intention is that the result can be processed by a css-in-js library
  such as emotion. Example (tw \"w-full max-w-sm my-3\")"
  [& strings]
  (->> (mapcat split-classes strings)
       (map tailwind->emotion)
       (apply str)))

(defmacro preflight []
  "/*! normalize.css v8.0.1 | MIT License | github.com/necolas/normalize.css */html{line-height:1.15;-webkit-text-size-adjust:100%}body{margin:0}main{display:block}h1{font-size:2em;margin:.67em 0}hr{box-sizing:content-box;height:0;overflow:visible}pre{font-family:monospace,monospace;font-size:1em}a{background-color:transparent}abbr[title]{border-bottom:none;text-decoration:underline;text-decoration:underline dotted}b,strong{font-weight:bolder}code,kbd,samp{font-family:monospace,monospace;font-size:1em}small{font-size:80%}sub,sup{font-size:75%;line-height:0;position:relative;vertical-align:baseline}sub{bottom:-.25em}sup{top:-.5em}img{border-style:none}button,input,optgroup,select,textarea{font-family:inherit;font-size:100%;line-height:1.15;margin:0}button,input{overflow:visible}button,select{text-transform:none}[type=button],[type=reset],[type=submit],button{-webkit-appearance:button}[type=button]::-moz-focus-inner,[type=reset]::-moz-focus-inner,[type=submit]::-moz-focus-inner,button::-moz-focus-inner{border-style:none;padding:0}[type=button]:-moz-focusring,[type=reset]:-moz-focusring,[type=submit]:-moz-focusring,button:-moz-focusring{outline:1px dotted ButtonText}fieldset{padding:.35em .75em .625em}legend{box-sizing:border-box;color:inherit;display:table;max-width:100%;padding:0;white-space:normal}progress{vertical-align:baseline}textarea{overflow:auto}[type=checkbox],[type=radio]{box-sizing:border-box;padding:0}[type=number]::-webkit-inner-spin-button,[type=number]::-webkit-outer-spin-button{height:auto}[type=search]{-webkit-appearance:textfield;outline-offset:-2px}[type=search]::-webkit-search-decoration{-webkit-appearance:none}::-webkit-file-upload-button{-webkit-appearance:button;font:inherit}details{display:block}summary{display:list-item}template{display:none}[hidden]{display:none}html{box-sizing:border-box;font-family:sans-serif}*,::after,::before{box-sizing:inherit}blockquote,dd,dl,figure,h1,h2,h3,h4,h5,h6,p,pre{margin:0}button{background:0 0;padding:0}button:focus{outline:1px dotted;outline:5px auto -webkit-focus-ring-color}fieldset{margin:0;padding:0}ol,ul{list-style:none;margin:0;padding:0}html{font-family:-apple-system,BlinkMacSystemFont,\"Segoe UI\",Roboto,\"Helvetica Neue\",Arial,\"Noto Sans\",sans-serif,\"Apple Color Emoji\",\"Segoe UI Emoji\",\"Segoe UI Symbol\",\"Noto Color Emoji\";line-height:1.5}*,::after,::before{border-width:0;border-style:solid;border-color:theme('borderColor.default', currentColor)}img{border-style:solid}textarea{resize:vertical}input::placeholder,textarea::placeholder{color:inherit;opacity:.5}[role=button],button{cursor:pointer}table{border-collapse:collapse}h1,h2,h3,h4,h5,h6{font-size:inherit;font-weight:inherit}a{color:inherit;text-decoration:inherit}button,input,optgroup,select,textarea{padding:0;line-height:inherit;color:inherit}code,kbd,pre,samp{font-family:theme('fontFamily.mono', SFMono-Regular, Menlo, Monaco, Consolas, \"Liberation Mono\", \"Courier New\", monospace)}audio,canvas,embed,iframe,img,object,svg,video{display:block;vertical-align:middle}img,video{max-width:100%;height:auto}")

;; emotion helpers

(defn data->css [d]
  (cond
    (string? d) d
    (map? d) (->> (sort d) (sequence cat) (apply rule))
    (coll? d) (apply str (map data->css d))))

(defmacro css
  "Converts basic css data (key value rules) to a string."
  [& d]
  (data->css d))

(defn emotion-hash [s]
  (let [bytes (.getBytes s)
        hash  (Murmur2/hash bytes (count bytes) (count s))]
    (str "css-" (Long/toString hash 36))))
