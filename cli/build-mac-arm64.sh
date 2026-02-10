#!/bin/bash
cd "$(dirname "$0")"
rm -rf build/darwin-arm64
mkdir -p build/darwin-arm64 && cd build/darwin-arm64
cmake ../.. -DCMAKE_BUILD_TYPE=Release -DCMAKE_OSX_ARCHITECTURES=arm64
make -j$(sysctl -n hw.ncpu)
strip zpatch
mkdir -p ../../dist
cp zpatch ../../dist/zpatch-darwin-arm64
