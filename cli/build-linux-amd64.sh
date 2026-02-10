#!/bin/bash
cd "$(dirname "$0")"
rm -rf build/linux-amd64
mkdir -p build/linux-amd64 && cd build/linux-amd64
cmake ../.. -DCMAKE_BUILD_TYPE=Release
make -j$(nproc)
strip zpatch
mkdir -p ../../dist
cp zpatch ../../dist/zpatch-linux-amd64
