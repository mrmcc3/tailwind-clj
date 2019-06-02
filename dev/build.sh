#!/usr/bin/env bash
set -e
clojure -Adev -m figwheel.main -O advanced -bo dev
rm -rf resources/public/js/build docs
cp -rf resources/public docs