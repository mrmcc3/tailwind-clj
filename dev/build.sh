#!/usr/bin/env bash
set -e

# clean build
rm -rf docs

# build js
clojure -Adev -m figwheel.main -O advanced -bo dev
mkdir -p docs/js
cp resources/public/js/*.js docs/js
rm -rf resources/public/js/*

# build css
mkdir -p docs/css
cleancss -O2 resources/public/css/examples.css -o docs/css/examples.css
postcss docs/css/examples.css -u autoprefixer -r --no-map
rm -rf resources/public/css/*

# install the rest
cp -rf resources/public/* docs
