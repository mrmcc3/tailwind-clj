(ns tailwind-clj.macros
  (:require
    [tailwind-clj.config :as config]
    [clojure.core.match :refer [match]]
    [clojure.string :as str]))

;; tailwind helpers

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
    nil #(hash-map pre %)
    \t #(hash-map (str pre "-top") %)
    \b #(hash-map (str pre "-bottom") %)
    \l #(hash-map (str pre "-left") %)
    \r #(hash-map (str pre "-right") %)
    \x #(hash-map (str pre "-left") % (str pre "-right") %)
    \y #(hash-map (str pre "-top") % (str pre "-bottom") %)))

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
        {(format "@media (min-width: %s)" (cfg-> :screens s))
         (tw->emo rest)}

        ;; pseudo classes

        [[(p :guard pseudo-classes) & rest]]
        {(str ":" p) (tw->emo rest)}

        ;; -------------------- LAYOUT -----------------------

        ;; container

        ;; TODO

        ;; display

        [["block"]] {"display" "block"}
        [["inline" "block"]] {"display" "inline-block"}
        [["inline"]] {"display" "inline"}
        [["flex"]] {"display" "flex"}
        [["inline" "flex"]] {"display" "inline-flex"}
        [["table"]] {"display" "table"}
        [["table" "row"]] {"display" "table-row"}
        [["table" "cell"]] {"display" "table-cell"}
        [["hidden"]] {"display" "none"}

        ;; float

        [["float" (f :guard #{"right" "left" "none"})]] {"float" f}
        [["clearfix"]] "&::after{content:\"\";display:table;clear:both;}"

        ;; object fit

        [["object" "scale" "down"]] {"object-fit" "scale-down"}
        [["object" (f :guard #{"contain" "cover" "fill" "none"})]]
        {"object-fit" f}

        ;; object position

        [["object" (p :guard #{"top" "left" "right" "bottom" "center"})]]
        {"object-position" p}
        [["object" (lr :guard #{"left" "right"}) (tb :guard #{"top" "bottom"})]]
        {"object-position" (str lr " " tb)}

        ;; overflow

        [["overflow" (o :guard overflow)]] {"overflow" o}
        [["overflow" (d :guard #{"x" "y"}) (o :guard overflow)]]
        {(str "overflow-" d) o}
        [["scrolling" (o :guard #{"touch" "auto"})]]
        {"-webkit-overflow-scrolling" o}

        ;; position

        [[(p :guard #{"static" "fixed" "absolute" "relative" "sticky"})]]
        {"position" p}

        ;; top left bottom right

        [[(p :guard #{"top" "left" "bottom" "right"}) (i :guard (cfg-> :inset))]]
        {p (cfg-> :inset i)}

        [["inset" (i :guard (cfg-> :inset))]]
        {"top" i "left" i "bottom" i "right" i}

        [["inset" "x" (i :guard (cfg-> :inset))]] {"left" i "right" i}
        [["inset" "y" (i :guard (cfg-> :inset))]] {"top" i "bottom" i}

        ;; visibility

        [["visible"]] {"visibility" "visible"}
        [["invisible"]] {"visibility" "hidden"}

        ;; z-index

        [["z" (z :guard (cfg-> :z-index))]]
        {"z-index" (cfg-> :z-index z)}

        ;; ------------------ TYPOGRAPHY ---------------------

        ;; font family

        ;; TODO

        ;; font size

        [["text" (s :guard (cfg-> :font-size))]]
        {"font-size" (cfg-> :font-size s)}

        ;; font smoothing

        ;; TODO

        ;; font style

        ;; TODO

        ;; font weight

        [["font" (w :guard (cfg-> :font-weight))]]
        {"font-weight" (cfg-> :font-weight w)}

        ;; letter spacing

        [["tracking" (s :guard (cfg-> :letter-spacing))]]
        {"letter-spacing" (cfg-> :letter-spacing s)}

        ;; line height

        [["leading" (h :guard (cfg-> :line-height))]]
        {"line-height" (cfg-> :line-height h)}

        ;; list style type

        ;; TODO

        ;; list style position

        ;; TODO

        ;; text align

        [["text" (c :guard text-align)]]
        {"text-align" c}

        ;; text color

        [["text" (c :guard (cfg-> :colors)) & rest]]
        {"color" (apply cfg-> :colors c rest)}

        ;; text decoration

        ;; TODO

        ;; text transform

        [["uppercase"]] {"text-transform" "uppercase"}
        [["lowercase"]] {"text-transform" "lowercase"}
        [["capitalize"]] {"text-transform" "capitalize"}
        [["normal-case"]] {"text-transform" "none"}

        ;; vertical align

        ;; TODO

        ;; whitespace

        ;; TODO

        ;; word break

        ;; TODO

        ;; ------------------ BACKGROUNDS --------------------

        ;; attachment

        [["bg" (a :guard background-attachments)]]
        {"background-attachment" a}

        ;; color

        [["bg" (c :guard (cfg-> :background-color)) & rest]]
        {"background-color" (apply cfg-> :background-color c rest)}

        ;; position

        [["bg" "left" "bottom"]] "background-position: left bottom"
        [["bg" "left" "top"]] "background-position: left bottom"
        [["bg" "right" "bottom"]] "background-position: left bottom"
        [["bg" "right" "top"]] "background-position: left bottom"
        [["bg" (p :guard #{"bottom" "center" "left" "right" "top"})]]
        {"background-position" p}

        ;; repeat

        [["bg" "repeat"]] "background-repeat: repeat"
        [["bg" "no" "repeat"]] "background-repeat: no-repeat"
        [["bg" "repeat" "x"]] "background-repeat: repeat-x"
        [["bg" "repeat" "y"]] "background-repeat: repeat-y"
        [["bg" "repeat" "round"]] "background-repeat: repeat-round"
        [["bg" "repeat" "space"]] "background-repeat: repeat-space"

        ;; size

        [["bg" (s :guard (cfg-> :background-size))]]
        {"background-size" (cfg-> :background-size s)}

        ;; -------------------- BORDERS ----------------------

        ;; color

        [["border" (c :guard (cfg-> :colors)) & rest]]
        {"border-color" (apply cfg-> :colors c rest)}

        ;; style

        [["border" (s :guard border-style)]]
        {"border-style" s}

        ;; width

        [["border"]] (tw->emo ["border" "default"])

        [["border" (s :guard border-sides)]]
        {(format "border-%s-width" (border-sides s)) (cfg-> :border-width "default")}

        [["border" (s :guard border-sides) (w :guard (cfg-> :border-width))]]
        {(format "border-%s-width" (border-sides s)) (cfg-> :border-width w)}

        [["border" (w :guard (cfg-> :border-width))]]
        {"border-width" (cfg-> :border-width w)}

        ;; radius

        [["rounded"]] (tw->emo ["rounded" "default"])
        [["rounded" (r :guard (cfg-> :border-radius))]]
        {"border-radius" (cfg-> :border-radius r)}

        [["rounded" (c :guard corners)]] (tw->emo ["rounded" c "default"])
        [["rounded" (c :guard corners) (r :guard (cfg-> :border-radius))]]
        {(format "border-%s-radius" (corners c)) (cfg-> :border-radius r)}

        [["rounded" (s :guard sides)]] (tw->emo ["rounded" s "default"])
        [["rounded" (s :guard sides) (r :guard (cfg-> :border-radius))]]
        (apply merge (map #(tw->emo ["rounded" % r]) (sides s)))

        ;; -------------------- FLEXBOX ----------------------

        ;; direction

        [["flex" "row"]] {"flex-direction" "row"}
        [["flex" "row" "reverse"]] {"flex-direction" "row-reverse"}
        [["flex" "col"]] {"flex-direction" "column"}
        [["flex" "col" "reverse"]] {"flex-direction" "column-reverse"}

        ;; wrap

        [["flex" "no" "wrap"]] {"flex-wrap" "no-wrap"}
        [["flex" "wrap"]] {"flex-wrap" "wrap"}
        [["flex" "wrap" "reverse"]] {"flex-wrap" "wrap-reverse"}

        ;; align items

        [["items" "stretch"]] {"align-items" "stretch"}
        [["items" "start"]] {"align-items" "flex-start"}
        [["items" "center"]] {"align-items" "center"}
        [["items" "end"]] {"align-items" "flex-end"}
        [["items" "baseline"]] {"align-items" "baseline"}

        ;; align content

        ;; TODO

        ;; align self

        ;; TODO

        ;; justify content

        [["justify" "start"]] {"justify-content" "flex-start"}
        [["justify" "center"]] {"justify-content" "center"}
        [["justify" "end"]] {"justify-content" "flex-end"}
        [["justify" "between"]] {"justify-content" "space-between"}
        [["justify" "around"]] {"justify-content" "space-around"}

        ;; flex

        [["flex" (f :guard (cfg-> :flex))]]
        {"flex" (cfg-> :flex f)}

        ;; grow

        [["flex" "grow"]] (tw->emo ["flex" "grow" "default"])
        [["flex" "grow" (g :guard (cfg-> :flex-grow))]]
        {"flex-grow" (cfg-> :flex-grow g)}

        ;; shrink

        [["flex" "shrink"]] (tw->emo ["flex" "shrink" "default"])
        [["flex" "shrink" (s :guard (cfg-> :flex-shrink))]]
        {"flex-shrink" (cfg-> :flex-shrink s)}

        ;; order

        ;; TODO

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
        {"width" (cfg-> :width w)}

        ;; min width

        ;; TODO

        ;; max width

        [["max" "w" (s :guard (cfg-> :max-width))]]
        {"max-width" (cfg-> :max-width s)}

        ;; height

        [["h" (h :guard (cfg-> :height))]]
        {"height" (cfg-> :height h)}

        ;; min height

        ;; TODO

        ;; max height

        ;; TODO

        ;; -------------------- TABLES -----------------------

        ;; border collapse

        ;; TODO

        ;; table layout

        ;; TODO

        ;; ------------------- EFFECTS -----------------------

        ;; box shadows

        [["shadow"]] (tw->emo ["shadow" "default"])
        [["shadow" (s :guard (cfg-> :box-shadow))]]
        {"box-shadow" (cfg-> :box-shadow s)}

        ;; opacity

        [["opacity" (o :guard (cfg-> :opacity))]]
        {"opacity" (cfg-> :opacity o)}


        ;; ---------------- INTERACTIVITY --------------------

        ;; appearance

        [["appearance" "none"]] {"appearance" "none"}
        [["outline" "none"]] {"outline" "none"}

        ;; cursor

        ;; TODO

        ;; outline

        ;; TODO

        ;; pointer events

        ;; TODO

        ;; resize

        ;; TODO

        ;; user select

        ;; TODO

        ;; --------------------- SVG -------------------------

        ;; fill

        [["fill" (f :guard (cfg-> :fill))]]
        {"fill" (cfg-> :fill f)}

        ;; stroke

        [["stroke" (s :guard (cfg-> :stroke))]]
        {"stroke" (cfg-> :stroke s)}

        ))))

(defn tailwind->emotion
  "given a single tailwind classname split it into fragments
  before invoking core.match to generate the css data"
  [classname]
  (tw->emo (str/split classname #"[:-]")))

(defn split-classes [s]
  (-> (name s) str/trim (str/split #"\s+")))

(defmacro tw
  "Given one or more strings containing whitespace separated tailwind classes
  return a vector of the css data that represents the classes.

  The intention is that the result can be processed by a css-in-js library
  such as emotion. Example (tw \"w-full max-w-sm my-3\")"
  [& strings]
  (mapv tailwind->emotion (mapcat split-classes strings)))

(defmacro preflight []
  "/*! normalize.css v8.0.1 | MIT License | github.com/necolas/normalize.css */html{line-height:1.15;-webkit-text-size-adjust:100%}body{margin:0}main{display:block}h1{font-size:2em;margin:.67em 0}hr{box-sizing:content-box;height:0;overflow:visible}pre{font-family:monospace,monospace;font-size:1em}a{background-color:transparent}abbr[title]{border-bottom:none;text-decoration:underline;text-decoration:underline dotted}b,strong{font-weight:bolder}code,kbd,samp{font-family:monospace,monospace;font-size:1em}small{font-size:80%}sub,sup{font-size:75%;line-height:0;position:relative;vertical-align:baseline}sub{bottom:-.25em}sup{top:-.5em}img{border-style:none}button,input,optgroup,select,textarea{font-family:inherit;font-size:100%;line-height:1.15;margin:0}button,input{overflow:visible}button,select{text-transform:none}[type=button],[type=reset],[type=submit],button{-webkit-appearance:button}[type=button]::-moz-focus-inner,[type=reset]::-moz-focus-inner,[type=submit]::-moz-focus-inner,button::-moz-focus-inner{border-style:none;padding:0}[type=button]:-moz-focusring,[type=reset]:-moz-focusring,[type=submit]:-moz-focusring,button:-moz-focusring{outline:1px dotted ButtonText}fieldset{padding:.35em .75em .625em}legend{box-sizing:border-box;color:inherit;display:table;max-width:100%;padding:0;white-space:normal}progress{vertical-align:baseline}textarea{overflow:auto}[type=checkbox],[type=radio]{box-sizing:border-box;padding:0}[type=number]::-webkit-inner-spin-button,[type=number]::-webkit-outer-spin-button{height:auto}[type=search]{-webkit-appearance:textfield;outline-offset:-2px}[type=search]::-webkit-search-decoration{-webkit-appearance:none}::-webkit-file-upload-button{-webkit-appearance:button;font:inherit}details{display:block}summary{display:list-item}template{display:none}[hidden]{display:none}html{box-sizing:border-box;font-family:sans-serif}*,::after,::before{box-sizing:inherit}blockquote,dd,dl,figure,h1,h2,h3,h4,h5,h6,p,pre{margin:0}button{background:0 0;padding:0}button:focus{outline:1px dotted;outline:5px auto -webkit-focus-ring-color}fieldset{margin:0;padding:0}ol,ul{list-style:none;margin:0;padding:0}html{font-family:-apple-system,BlinkMacSystemFont,\"Segoe UI\",Roboto,\"Helvetica Neue\",Arial,\"Noto Sans\",sans-serif,\"Apple Color Emoji\",\"Segoe UI Emoji\",\"Segoe UI Symbol\",\"Noto Color Emoji\";line-height:1.5}*,::after,::before{border-width:0;border-style:solid;border-color:theme('borderColor.default', currentColor)}img{border-style:solid}textarea{resize:vertical}input::placeholder,textarea::placeholder{color:inherit;opacity:.5}[role=button],button{cursor:pointer}table{border-collapse:collapse}h1,h2,h3,h4,h5,h6{font-size:inherit;font-weight:inherit}a{color:inherit;text-decoration:inherit}button,input,optgroup,select,textarea{padding:0;line-height:inherit;color:inherit}code,kbd,pre,samp{font-family:theme('fontFamily.mono', SFMono-Regular, Menlo, Monaco, Consolas, \"Liberation Mono\", \"Courier New\", monospace)}audio,canvas,embed,iframe,img,object,svg,video{display:block;vertical-align:middle}img,video{max-width:100%;height:auto}")

