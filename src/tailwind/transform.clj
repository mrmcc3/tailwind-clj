(ns tailwind.transform
  (:require
    [clojure.core.match :refer [match]]
    [tailwind.config :refer [cfg->]]
    [tailwind.util :as u :refer [rule]]
    [clojure.string :as str]))

;; tailwind classes -> css
;; the transform is represented using core.match patterns

(def fragments->emotion
  "Accepts tailwind class fragments and returns the corresponding css (emotion style)."
  (memoize
    (fn [fragments]
      (match fragments

        ;; responsive breakpoints - media queries

        [(s :guard (cfg-> :screens)) & rest]
        (format "@media(min-width: %spx){%s}" (cfg-> :screens s) (fragments->emotion rest))

        ;; pseudo classes

        [(p :guard u/pseudo-classes) & rest]
        (format ":%s{%s}" p (fragments->emotion rest))

        ;; -------------------- LAYOUT -----------------------

        ;; container

        ["container"]
        (reduce-kv
          #(str %1 (format "@media(min-width:%spx){max-width:%spx;}" %3 %3))
          "width:100%;"
          (cfg-> :screens))

        ;; display

        ["block"] (rule "display" "block")
        ["inline" "block"] (rule "display" "inline-block")
        ["inline"] (rule "display" "inline")
        ["flex"] (rule "display" "flex")
        ["inline" "flex"] (rule "display" "inline-flex")
        ["table"] (rule "display" "table")
        ["table" "row"] (rule "display" "table-row")
        ["table"] (rule "display" "table-cell")
        ["hidden"] (rule "display" "none")

        ;; float

        ["float" (f :guard #{"right" "left" "none"})] {"float" f}
        ["clearfix"] "&::after{content:\"\";display:table;clear:both;}"

        ;; object fit

        ["object" "scale" "down"] (rule "object-fit" "scale-down")
        ["object" (f :guard #{"contain" "cover" "fill" "none"})]
        (rule "object-fit" f)

        ;; object position

        ["object" (p :guard #{"top" "left" "right" "bottom" "center"})]
        (rule "object-position" p)
        ["object" (lr :guard #{"left" "right"}) (tb :guard #{"top" "bottom"})]
        (rule "object-position" (str lr " " tb))

        ;; overflow

        ["overflow" (o :guard u/overflow)] (rule "overflow" o)
        ["overflow" (d :guard #{"x" "y"}) (o :guard u/overflow)]
        (rule (str "overflow-" d) o)
        ["scrolling" (o :guard #{"touch" "auto"})]
        (rule "-webkit-overflow-scrolling" o)

        ;; position

        [(p :guard #{"static" "fixed" "absolute" "relative" "sticky"})]
        (rule "position" p)

        ;; top left bottom right

        [(p :guard #{"top" "left" "bottom" "right"}) (i :guard (cfg-> :inset))]
        (rule p (cfg-> :inset i))

        ["inset" (i :guard (cfg-> :inset))]
        (rule "top" i "left" i "bottom" i "right" i)

        ["inset" "x" (i :guard (cfg-> :inset))] (rule "left" i "right" i)
        ["inset" "y" (i :guard (cfg-> :inset))] (rule "top" i "bottom" i)

        ;; visibility

        ["visible"] (rule "visibility" "visible")
        ["invisible"] (rule "visibility" "hidden")

        ;; z-index

        ["z" (z :guard (cfg-> :z-index))]
        (rule "z-index" (cfg-> :z-index z))

        ;; ------------------ TYPOGRAPHY ---------------------

        ;; font family

        ["font" (f :guard (cfg-> :font-family))]
        (rule "font-family" (str/join "," (cfg-> :font-family f)))

        ;; font size

        ["text" (s :guard (cfg-> :font-size))]
        (rule "font-size" (cfg-> :font-size s))

        ;; font smoothing

        ["antialiased"]
        (rule "-webkit-font-smoothing" "antialiased"
              "-moz-osx-font-smoothing" "grayscale")
        ["subpixel-antialiased"]
        (rule "-webkit-font-smoothing" "auto"
              "-moz-osx-font-smoothing" "auto")

        ;; font style

        ["italic"] (rule "font-style" "italic")
        ["not-italic"] (rule "font-style" "normal")

        ;; font weight

        ["font" (w :guard (cfg-> :font-weight))]
        (rule "font-weight" (cfg-> :font-weight w))

        ;; letter spacing

        ["tracking" (s :guard (cfg-> :letter-spacing))]
        (rule "letter-spacing" (cfg-> :letter-spacing s))

        ;; line height

        ["leading" (h :guard (cfg-> :line-height))]
        (rule "line-height" (cfg-> :line-height h))

        ;; list style type

        ["list" (s :guard #{"none" "disc" "decimal"})]
        (rule "list-style-type" s)

        ;; list style position

        ["list" (s :guard #{"inside" "outside"})]
        (rule "list-style-position" s)

        ;; text align

        ["text" (c :guard u/text-align)]
        (rule "text-align" c)

        ;; text color

        ["text" (c :guard (cfg-> :colors)) & rest]
        (rule "color" (apply cfg-> :colors c rest))

        ;; text decoration

        ["underline"] (rule "text-decoration" "underline")
        ["line" "through"] (rule "text-decoration" "line-through")
        ["no" "underline"] (rule "text-decoration" "none")

        ;; text transform

        ["uppercase"] (rule "text-transform" "uppercase")
        ["lowercase"] (rule "text-transform" "lowercase")
        ["capitalize"] (rule "text-transform" "capitalize")
        ["normal-case"] (rule "text-transform" "none")

        ;; vertical align

        ["align" (s :guard #{"baseline" "top" "middle" "bottom"})]
        (rule "vertical-align" s)
        ["align" "text" (s :guard #{"top" "bottom"})]
        (rule "vertical-align" (str "text-" s))

        ;; whitespace

        ["whitespace" "normal"] (rule "white-space" "normal")
        ["whitespace" "no" "wrap"] (rule "white-space" "nowrap")
        ["whitespace" "pre"] (rule "white-space" "pre")
        ["whitespace" "pre" "line"] (rule "white-space" "pre-line")
        ["whitespace" "pre" "wrap"] (rule "white-space" "pre-wrap")

        ;; word break

        ["break" "normal"] (rule "word-break" "normal" "overflow-wrap" "normal")
        ["break" "words"] (rule "overflow-wrap" "break-word")
        ["break" "all"] (rule "word-break" "break-all")
        ["truncate"] (rule "overflow" "hidden"
                           "text-overflow" "ellipsis"
                           "white-space" "nowrap")

        ;; ------------------ BACKGROUNDS --------------------

        ;; attachment

        ["bg" (a :guard u/background-attachments)]
        (rule "background-attachment" a)

        ;; color

        ["bg" (c :guard (cfg-> :background-color)) & rest]
        (rule "background-color" (apply cfg-> :background-color c rest))

        ;; position

        ["bg" "left" "bottom"] (rule "background-position" "left bottom")
        ["bg" "left" "top"] (rule "background-position" "left top")
        ["bg" "right" "bottom"] (rule "background-position" "right bottom")
        ["bg" "right" "top"] (rule "background-position" "right top")
        ["bg" (p :guard #{"bottom" "center" "left" "right" "top"})]
        (rule "background-position" p)

        ;; repeat

        ["bg" "repeat"] (rule "background-repeat" "repeat")
        ["bg" "no" "repeat"] (rule "background-repeat" "no-repeat")
        ["bg" "repeat" "x"] (rule "background-repeat" "repeat-x")
        ["bg" "repeat" "y"] (rule "background-repeat" "repeat-y")
        ["bg" "repeat" "round"] (rule "background-repeat" "repeat-round")
        ["bg" "repeat" "space"] (rule "background-repeat" "repeat-space")

        ;; size

        ["bg" (s :guard (cfg-> :background-size))]
        (rule "background-size" (cfg-> :background-size s))

        ;; -------------------- BORDERS ----------------------

        ;; color

        ["border" (c :guard (cfg-> :colors)) & rest]
        (rule "border-color" (apply cfg-> :colors c rest))

        ;; style

        ["border" (s :guard u/border-style)]
        (rule "border-style" s)

        ;; width

        ["border"] (fragments->emotion ["border" "default"])

        ["border" (s :guard u/border-sides)]
        (rule (format "border-%s-width" (u/border-sides s)) (cfg-> :border-width "default"))

        ["border" (s :guard u/border-sides) (w :guard (cfg-> :border-width))]
        (rule (format "border-%s-width" (u/border-sides s)) (cfg-> :border-width w))

        ["border" (w :guard (cfg-> :border-width))]
        (rule "border-width" (cfg-> :border-width w))

        ;; radius

        ["rounded"] (fragments->emotion ["rounded" "default"])
        ["rounded" (r :guard (cfg-> :border-radius))]
        (rule "border-radius" (cfg-> :border-radius r))

        ["rounded" (c :guard u/corners)] (fragments->emotion ["rounded" c "default"])
        ["rounded" (c :guard u/corners) (r :guard (cfg-> :border-radius))]
        (rule (format "border-%s-radius" (u/corners c)) (cfg-> :border-radius r))

        ["rounded" (s :guard u/sides)] (fragments->emotion ["rounded" s "default"])
        ["rounded" (s :guard u/sides) (r :guard (cfg-> :border-radius))]
        (apply str (map #(fragments->emotion ["rounded" % r]) (u/sides s)))

        ;; -------------------- FLEXBOX ----------------------

        ;; direction

        ["flex" "row"] (rule "flex-direction" "row")
        ["flex" "row" "reverse"] (rule "flex-direction" "row-reverse")
        ["flex" "col"] (rule "flex-direction" "column")
        ["flex" "col" "reverse"] (rule "flex-direction" "column-reverse")

        ;; wrap

        ["flex" "no" "wrap"] (rule "flex-wrap" "no-wrap")
        ["flex" "wrap"] (rule "flex-wrap" "wrap")
        ["flex" "wrap" "reverse"] (rule "flex-wrap" "wrap-reverse")

        ;; align items

        ["items" "stretch"] (rule "align-items" "stretch")
        ["items" "start"] (rule "align-items" "flex-start")
        ["items" "center"] (rule "align-items" "center")
        ["items" "end"] (rule "align-items" "flex-end")
        ["items" "baseline"] (rule "align-items" "baseline")

        ;; align content

        ["content" "start"] (rule "align-content" "flex-start")
        ["content" "center"] (rule "align-content" "center")
        ["content" "end"] (rule "align-content" "flex-end")
        ["content" "between"] (rule "align-content" "space-between")
        ["content" "around"] (rule "align-content" "space-around")

        ;; align self

        ["self" "auto"] (rule "align-self" "auto")
        ["self" "start"] (rule "align-self" "flex-start")
        ["self" "center"] (rule "align-self" "center")
        ["self" "end"] (rule "align-self" "flex-end")
        ["self" "stretch"] (rule "align-self" "stretch")

        ;; justify content

        ["justify" "start"] (rule "justify-content" "flex-start")
        ["justify" "center"] (rule "justify-content" "center")
        ["justify" "end"] (rule "justify-content" "flex-end")
        ["justify" "between"] (rule "justify-content" "space-between")
        ["justify" "around"] (rule "justify-content" "space-around")

        ;; flex

        ["flex" (f :guard (cfg-> :flex))]
        (rule "flex" (cfg-> :flex f))

        ;; grow

        ["flex" "grow"] (fragments->emotion ["flex" "grow" "default"])
        ["flex" "grow" (g :guard (cfg-> :flex-grow))]
        (rule "flex-grow" (cfg-> :flex-grow g))

        ;; shrink

        ["flex" "shrink"] (fragments->emotion ["flex" "shrink" "default"])
        ["flex" "shrink" (s :guard (cfg-> :flex-shrink))]
        (rule "flex-shrink" (cfg-> :flex-shrink s))

        ;; order

        ["order" (o :guard (cfg-> :order))]
        (rule "order" (cfg-> :order o))

        ;; -------------------- SPACING ----------------------

        ;; padding

        [(v :guard u/padding-fns) (p :guard (cfg-> :padding))]
        ((u/padding-fns v) (cfg-> :padding p))

        ;; margin

        [(v :guard u/margin-fns) (m :guard (cfg-> :margin))]
        ((u/margin-fns v) (cfg-> :margin m))
        ["" (v :guard u/margin-fns) (m :guard (cfg-> :margin))]
        ((u/margin-fns v) (cfg-> :margin (str "-" m)))

        ;; -------------------- SIZING -----------------------

        ;; width

        ["w" (w :guard (cfg-> :width))]
        (rule "width" (cfg-> :width w))

        ;; min width

        ["min" "w" (w :guard (cfg-> :min-width))]
        (rule "min-width" (cfg-> :min-width w))

        ;; max width

        ["max" "w" (s :guard (cfg-> :max-width))]
        (rule "max-width" (cfg-> :max-width s))

        ;; height

        ["h" (h :guard (cfg-> :height))]
        (rule "height" (cfg-> :height h))

        ;; min height

        ["min" "h" (h :guard (cfg-> :min-height))]
        (rule "min-height" (cfg-> :min-height h))

        ;; max height

        ["max" "h" (h :guard (cfg-> :max-height))]
        (rule "max-height" (cfg-> :max-height h))

        ;; -------------------- TABLES -----------------------

        ;; border collapse

        ["border" "collapse"] (rule "border-collapse" "collapse")
        ["border" "separate"] (rule "border-collapse" "separate")

        ;; table layout

        ["table" "auto"] (rule "table-layout" "auto")
        ["table" "fixed"] (rule "table-layout" "fixed")

        ;; ------------------- EFFECTS -----------------------

        ;; box shadows

        ["shadow"] (fragments->emotion ["shadow" "default"])
        ["shadow" (s :guard (cfg-> :box-shadow))]
        (rule "box-shadow" (cfg-> :box-shadow s))

        ;; opacity

        ["opacity" (o :guard (cfg-> :opacity))]
        (rule "opacity" (cfg-> :opacity o))

        ;; ---------------- INTERACTIVITY --------------------

        ;; appearance

        ["appearance" "none"] (rule "appearance" "none")
        ["outline" "none"] (rule "outline" "none")

        ;; cursor

        ["cursor" & rest]
        (rule "cursor" (cfg-> :cursor (str/join "-" rest)))

        ;; outline

        ["outline" "none"] (rule "outline" "0")

        ;; pointer events

        ["pointer" "events" "none"] (rule "pointer-events" "none")
        ["pointer" "events" "auto"] (rule "pointer-events" "auto")

        ;; resize

        ["resize" "none"] (rule "resize" "none")
        ["resize"] (rule "resize" "both")
        ["resize" "y"] (rule "resize" "vertical")
        ["resize" "x"] (rule "resize" "horizontal")

        ;; user select

        ["select" (s :guard #{"none" "text" "all" "auto"})]
        (rule "user-select" s)

        ;; --------------------- SVG -------------------------

        ;; fill

        ["fill" (f :guard (cfg-> :fill))]
        (rule "fill" (cfg-> :fill f))

        ;; stroke

        ["stroke" (s :guard (cfg-> :stroke))]
        (rule "stroke" (cfg-> :stroke s))

        ;; ---------------- screenreader ---------------------

        ["sr" "only"]
        (rule "position" "absolute"
              "width" "1px"
              "height" "1px"
              "padding" "0"
              "margin" "-1px"
              "overflow" "hidden"
              "clip" "rect(0, 0, 0, 0)"
              "whiteSpace" "nowrap"
              "borderWidth" "0")

        ["not" "sr" "only"]
        (rule "position" "static"
              "width" "auto"
              "height" "auto"
              "padding" "0"
              "margin" "0"
              "overflow" "visible"
              "clip" "auto"
              "whiteSpace" "normal")

        ))))


(def fragments->css
  "TODO"
  (memoize
    (fn [class fragments]
      (match fragments

        ;; responsive breakpoints - media queries
        [(screen :guard (cfg-> :screens)) & rest]
        {(cfg-> :screens screen)
         (get (fragments->css class rest) nil)}

        ;; pseudo classes
        [(p :guard u/pseudo-classes) & rest]
        {nil {class (format ".%s:%s{%s}" (u/escape class) p (fragments->emotion rest))}}

        ;; container
        ["container"]
        (reduce-kv
          #(assoc %1 %3 {class (format ".container{max-width:%spx;}" %3)})
          {nil {class ".container{width:100%;}"}}
          (cfg-> :screens))

        ;; everything else
        :else
        {nil {class (format ".%s{%s}" (u/escape class) (fragments->emotion fragments))}}))))

(defn class->css [class]
  (fragments->css class (u/split-fragments class)))
