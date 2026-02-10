#!/bin/bash
echo "Building ZPatch CLI for all platforms..."
rm -rf dist
mkdir -p dist

# macOS amd64 (x86_64)
echo "Building macOS amd64..."
./build-mac-amd64.sh
if [ -f dist/zpatch-darwin-amd64 ]; then
    echo "  -> dist/zpatch-darwin-amd64 created"
else
    echo "  -> Failed to build macOS amd64"
fi

# macOS arm64
echo "Building macOS arm64..."
./build-mac-arm64.sh
if [ -f dist/zpatch-darwin-arm64 ]; then
    echo "  -> dist/zpatch-darwin-arm64 created"
else
    echo "  -> Failed to build macOS arm64"
fi

# Linux amd64 (x86_64, 需要在 Linux 环境或使用 Docker)
echo "Building Linux amd64..."
./build-linux-amd64.sh
if [ -f dist/zpatch-linux-amd64 ]; then
    echo "  -> dist/zpatch-linux-amd64 created"
else
    echo "  -> Failed to build Linux amd64 (may need Linux environment)"
fi

echo ""
echo "Done! Output in dist/:"
ls -lh dist/ 2>/dev/null || echo "No binaries found"
