#!/bin/bash
cd "$(dirname "$0")"
rm -rf build/darwin-amd64
mkdir -p build/darwin-amd64 && cd build/darwin-amd64
cmake ../.. -DCMAKE_BUILD_TYPE=Release
make -j$(sysctl -n hw.ncpu)
strip zpatch
mkdir -p ../../dist
cp zpatch ../../dist/zpatch-darwin-amd64
