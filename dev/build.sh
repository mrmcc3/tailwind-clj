#!/usr/bin/env bash
set -e
clojure -Adev -m figwheel.main -O advanced -bo dev
CSS="resources/public/css/examples.css"
cleancss -O2 ${CSS} -o ${CSS}
postcss ${CSS} -u autoprefixer -r --no-map
rm -rf resources/public/js/build docs
cp -rf resources/public docs