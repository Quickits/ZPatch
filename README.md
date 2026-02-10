# ZPatch

**High-performance differential patch library powered by Zstd**

[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)
[![Release](https://img.shields.io/github/v/release/Quickits/ZPatch)](https://github.com/Quickits/ZPatch/releases)
[![GitHub Actions](https://github.com/Quickits/ZPatch/actions/workflows/release.yml/badge.svg)](https://github.com/Quickits/ZPatch/actions)

---

**Language:** [English](README.md) | [中文](README_zh.md)

## Overview

**ZPatch** is a high-performance differential patch library powered by [Zstd](https://github.com/facebook/zstd) compression. It uses the old file as a dictionary to compress the new file, achieving significantly higher compression ratios than traditional diff algorithms.

## Features

- 🚀 **High Compression Ratio** - Leverages Zstd dictionary compression
- 📦 **Zero External Dependencies** - Pure C++ implementation with bundled Zstd
- 🌍 **Cross-Platform** - Supports Android, iOS, Linux, macOS, Windows
- 📱 **Android Ready** - 16KB page size support for Android 15+
- 🔧 **Simple API** - Easy to integrate into any project

## Downloads

Pre-built binaries are available on the [Releases](https://github.com/Quickits/ZPatch/releases) page:

| File | Platform | Architecture |
|------|----------|--------------|
| `zpatch-darwin-amd64` | macOS | x86_64 |
| `zpatch-darwin-arm64` | macOS | ARM64 (Apple Silicon) |
| `zpatch-linux-amd64` | Linux | x86_64 |
| `zpatch-android.aar` | Android | AAR Library |

## Quick Start

### CLI Tool

```bash
# Show version
./zpatch version

# Create a patch
./zpatch create <old-file> <new-file> <patch-file>

# Apply a patch
./zpatch apply <old-file> <patch-file> <new-file>
```

### Android Integration

Add to your `build.gradle.kts`:

```kotlin
dependencies {
    implementation("com.github.quickits:zpatch:VERSION")
}
```

Kotlin API:

```kotlin
import com.quickits.zpatch.ZPatch

// Create patch
val result = ZPatch.createPatch(oldPath, newPath, patchPath)
if (result.isSuccess) {
    println("Patch created: ${result.originalSize} bytes")
}

// Apply patch
val result = ZPatch.applyPatch(oldPath, patchPath, newPath)
if (result.isSuccess) {
    println("File restored: ${result.originalSize} bytes")
}
```

### C++ API

```cpp
#include "zpatch/core.h"

// Create patch
zpatch::Result result = zpatch::createPatch("old.bin", "new.bin", "patch.zst");

// Apply patch
zpatch::Result result = zpatch::applyPatch("old.bin", "patch.zst", "new.bin");
```

## Building

### CLI Tool

```bash
cd cli

# Build for current platform
./build-mac-amd64.sh    # macOS x86_64
./build-mac-arm64.sh    # macOS ARM64
./build-linux-amd64.sh  # Linux x86_64

# Build all platforms
./build-all.sh
```

### Android Library

```bash
cd android
./gradlew :library:assembleRelease
```

### Core Library

```bash
cd core
mkdir build && cd build
cmake ..
make
```

## API Reference

```cpp
namespace zpatch {
    // Get version information
    const char* getVersion();

    // Create a patch file
    Result createPatch(const char* oldFile, const char* newFile, const char* patchFile);

    // Apply a patch file
    Result applyPatch(const char* oldFile, const char* patchFile, const char* newFile);
}

struct Result {
    int code;           // 0 = success, non-zero = failure
    const char* message; // Error message (static string)
    uint64_t originalSize; // Output file size
};
```

## Project Structure

```
zpatch/
├── core/       # C++ core library
├── cli/        # Command-line tool
├── android/    # Android library + sample app
└── .github/    # GitHub Actions workflows
```

## License

MIT License - see [LICENSE](LICENSE) for details.
