(ns user)

(comment

  ;; start figwheel repl
  (do (require '[figwheel.main.api])
      (figwheel.main.api/start "dev"))

  (require '[tailwind.core :refer [tw]]
           '[tailwind.util :as u])

  @(def css (tw "flex flex-col items-center" "py-3" :text-gray-800))

  (u/emotion-hash css)


  )





