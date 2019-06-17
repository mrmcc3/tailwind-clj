#!/usr/bin/env bash
set -e
clojure -Adev -m figwheel.main -O advanced -bo dev
CSS="resources/public/css/examples.css"
postcss ${CSS} -u autoprefixer -r --no-map
cleancss -O2 ${CSS} -o ${CSS}
rm -rf resources/public/js/build docs
cp -rf resources/public docs