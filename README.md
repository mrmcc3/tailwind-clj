### tailwind-clj

A clojure library that transforms [tailwindcss](https://tailwindcss.com/) 
utility classnames into css data (string) that can be processed by 
css-in-js libraries such as [emotion](https://emotion.sh/docs/introduction)

```clojure
(require '[tailwind.core :refer [tw]]
         '[tailwind.util :as u])

;; tw macro accepts tailwind utility classes as strings or keywords
user=> @(def css (tw "flex flex-col items-center" "py-3" :text-gray-800))
"display:flex;flex-direction:column;align-items:center;padding-top:0.75rem;padding-bottom:0.75rem;color:#2d3748;"

;; can be processed by emotion in clojurescript
cljs.user=> (js/emotion.css css) ;; installs css and returns class name
"css-b85zbn"

;; can compute hash directly. useful for server side rendering
user=> (u/emotion-hash css)
"css-b85zbn"
```

The library is very much a work in progress. I created it mainly as a learning
exercise and to experiment with tailwind, emotion and uix.

`dev/tailwind/examples.cljs` has some example components from the tailwind
site. result https://mrmcc3.github.io/tailwind-clj/

### Configuration

The tailwind config/design system is built by first defining some base attributes 
like colors and spacing. Then the config is expanded by using the base definitions 
to define attributes like border-color, padding and margin.

* The default config before expansion is at `src/tailwind/defaults.edn`.
* To view the expanded default config run `clj -m tailwind.core default`
* To drill down into the config pass extra args `clj -m tailwind.core default colors blue`

You can also test the `tw` macro out at the command line

```
$ clj -m tailwind.core tw font-mono
font-family:Menlo,Monaco,Consolas,"Liberation Mono","Courier New",monospace;
```

### User customization

If you would like to customize the configuration then place a `tailwind.edn`
file in the classpath with your customizations. This file will be read and
merged with the default config **before** expansion using 
[meta-merge](https://github.com/weavejester/meta-merge). 

For example `{"spacing" {"perfect" "23px"}}` would add `perfect` to `spacing`
and all attributes that depend on it like `margin` and `padding`

```
$ clj -m tailwind.core tw mb-perfect px-perfect
margin-bottom:23px;padding-left:23px;padding-right:23px;
```

If you prefer to completely replace the default spacing scale then the
meta-merge hint ^:replace is what you want 
`{"spacing" ^:replace {"perfect" "23px"}}`

If you want to skip the expansion mechanism you can add an extra `^:final` 
hint to a calculated attribute. For example
`{"padding" ^:replace ^:final {"perfect" "23px"}}`

### Base styles

Tailwind uses a combination of normalize.css and some extra 
`preflight` resets. You can call the `base` macro to generate
the global styles. Or run from the command line
```
$ clj -m tailwind-clj.macros base
```

### Extract css files

TODO write docs. see `tw!` macro