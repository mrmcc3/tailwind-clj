## tailwind-clj

A clojure library that processes [tailwindcss][tailwind]
utility classes, generates css rules and either writes the output to a
css file or returns css data (suitable for css-in-js libraries such as 
[emotion]).

When developing client applications with ClojureScript and `tailwind-clj`

* you can use macros to only generate css for the tailwind utilities that you actually use
* you don't have to integrate any nodejs tooling into your dev flow
* customize tailwind with a `tailwind.edn` file on the classpath
* fits nicely with figwheel based development
* get all the benefits of tailwind (see rationale below)

### Example

```clojure
(ns tailwind.example
  (:require [tailwind.core :refer [tw! spit-css!]]))

(tw! "flex flex-col items-center" "py-3 m-4" :text-gray-800) ;; strings or keywords
;; => "flex flex-col items-center py-3 text-gray-800"

(spit-css! "styles.css")
```

```base
$ clojure -m cljs.main -c tailwind.example
```

```css
/*! normalize.css v8.0.1 | MIT License | github.com/necolas/normalize.css */html{line-height:1.15;-webkit-text-size-adjust:100%}body{margin:0}main{display:block}h1{font-size:2em;margin:.67em 0}hr{box-sizing:content-box;height:0;overflow:visible}pre{font-family:monospace,monospace;font-size:1em}a{background-color:transparent}abbr[title]{border-bottom:none;text-decoration:underline;text-decoration:underline dotted}b,strong{font-weight:bolder}code,kbd,samp{font-family:monospace,monospace;font-size:1em}small{font-size:80%}sub,sup{font-size:75%;line-height:0;position:relative;vertical-align:baseline}sub{bottom:-.25em}sup{top:-.5em}img{border-style:none}button,input,optgroup,select,textarea{font-family:inherit;font-size:100%;line-height:1.15;margin:0}button,input{overflow:visible}button,select{text-transform:none}[type=button],[type=reset],[type=submit],button{-webkit-appearance:button}[type=button]::-moz-focus-inner,[type=reset]::-moz-focus-inner,[type=submit]::-moz-focus-inner,button::-moz-focus-inner{border-style:none;padding:0}[type=button]:-moz-focusring,[type=reset]:-moz-focusring,[type=submit]:-moz-focusring,button:-moz-focusring{outline:1px dotted ButtonText}fieldset{padding:.35em .75em .625em}legend{box-sizing:border-box;color:inherit;display:table;max-width:100%;padding:0;white-space:normal}progress{vertical-align:baseline}textarea{overflow:auto}[type=checkbox],[type=radio]{box-sizing:border-box;padding:0}[type=number]::-webkit-inner-spin-button,[type=number]::-webkit-outer-spin-button{height:auto}[type=search]{-webkit-appearance:textfield;outline-offset:-2px}[type=search]::-webkit-search-decoration{-webkit-appearance:none}::-webkit-file-upload-button{-webkit-appearance:button;font:inherit}details{display:block}summary{display:list-item}template{display:none}[hidden]{display:none}html{box-sizing:border-box;font-family:sans-serif}*,::after,::before{box-sizing:inherit}blockquote,dd,dl,figure,h1,h2,h3,h4,h5,h6,p,pre{margin:0}button{background:0 0;padding:0}button:focus{outline:1px dotted;outline:5px auto -webkit-focus-ring-color}fieldset{margin:0;padding:0}ol,ul{list-style:none;margin:0;padding:0}html{font-family:-apple-system,BlinkMacSystemFont,"Segoe UI",Roboto,"Helvetica Neue",Arial,"Noto Sans",sans-serif,"Apple Color Emoji","Segoe UI Emoji","Segoe UI Symbol","Noto Color Emoji";line-height:1.5}*,::after,::before{border-width:0;border-style:solid;border-color:#e2e8f0;}img{border-style:solid}textarea{resize:vertical}input::placeholder,textarea::placeholder{color:inherit;opacity:.5}[role=button],button{cursor:pointer}table{border-collapse:collapse}h1,h2,h3,h4,h5,h6{font-size:inherit;font-weight:inherit}a{color:inherit;text-decoration:inherit}button,input,optgroup,select,textarea{padding:0;line-height:inherit;color:inherit}code,kbd,pre,samp{font-family:"Ubuntu Mono",monospace;}audio,canvas,embed,iframe,img,object,svg,video{display:block;vertical-align:middle}img,video{max-width:100%;height:auto}

.flex{display:flex;}
.flex-col{flex-direction:column;}
.items-center{align-items:center;}
.m-4{margin:1rem;}
.py-3{padding-top:0.75rem;padding-bottom:0.75rem;}
.text-gray-800{color:#2d3748;}
```

You can also test the `tw!` macro at the command line

```
$ clj -m tailwind.core tw! font-mono
.font-mono{font-family:Menlo,Monaco,Consolas,"Liberation Mono","Courier New",monospace;}
```

`dev/tailwind/examples.cljs` has some example components from the tailwind site rendered
using [uix] and [emotion]. [result][examples]

### Rationale

If you're unfamiliar with the rationale behind tailwind css 
read [utility first][utility-first] page from the tailwind docs. 
In short the idea is that you can generate a whole bunch of utility 
classes that in most cases correspond to a single css rule. By combining 
these classes in various ways you can create complex user interfaces. 
At first it seems cumbersome to add numerous classes to your markup 
but in practice some really nice benefits fall out of it.

* for the most you don't have to write or maintain any css
* you don't have to keep inventing class names
* you are constrained to the provided set of utility classes establishing
a predefined design system. The result is visually consistent UIs
* After a while you get familiar with the standard utility classes speeding
up the design process
* re-using standard utilities means your CSS stops growing over time
* making changes feels safer

One downside is that Tailwind has to generate css for every utility **and** its variants.
Variants include a combination of pseudo classes and media queries which means the 
size of the resulting css has a combinatorial explosion. Tailwind minimizes the 
issue by carefully choosing the default set of utilities and disabling all but 
the most used variants. Even then you're looking at ~400KB of uncompressed css. 
A good portion of that is most likely unused. Tools like 
[purgecss] can help remove the unused classes.

In ClojureScript we can just generate the utility classes as we need them using 
macros at compile time. While we're at it we can make customization simpler by
just dropping a `tailwind.edn` file somewhere on the classpath.

### Configuration

The tailwind config/design system is built by first defining some base attributes 
like colors and spacing. Then the config is expanded by using the base definitions 
to define attributes like border-color, padding and margin.

* The default config before expansion is at `src/tailwind/defaults.edn`.
* To view the expanded default config `clj -m tailwind.core default`
* To drill down into the config pass extra args `clj -m tailwind.core default colors blue`

### User customization

If you would like to customize the configuration then place a `tailwind.edn`
file on the classpath with your customizations. This file will be read and
merged with the default config **before** expansion using 
[meta-merge]. 

For example `{"spacing" {"perfect" "23px"}}` would add `perfect` to `spacing`
and all attributes that depend on it like `margin` and `padding`

```
$ clj -m tailwind.core tw! mb-perfect px-perfect
.mb-perfect{margin-bottom:23px;}
.px-perfect{padding-left:23px;padding-right:23px;}
```

If you prefer to completely replace the default spacing scale then the
meta-merge hint ^:replace is what you want 
`{"spacing" ^:replace {"perfect" "23px"}}`

If you want to skip the expansion mechanism you can add an extra `^:final` 
hint to a calculated attribute. For example
`{"padding" ^:replace ^:final {"perfect" "23px"}}`

[tailwind]: https://tailwindcss.com/ 
[examples]: https://mrmcc3.github.io/tailwind-clj/
[utility-first]: https://tailwindcss.com/docs/utility-first
[purgecss]: https://github.com/FullHuman/purgecss
[meta-merge]: https://github.com/weavejester/meta-merge 
[emotion]: https://emotion.sh/docs/introduction
[uix]: https://github.com/roman01la/uix