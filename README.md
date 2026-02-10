<div align="center">

# ZPatch

**基于 Zstd 的高性能差分补丁库**

[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)
[![Release](https://img.shields.io/github/v/release/Quickits/ZPatch)](https://github.com/Quickits/ZPatch/releases)
[![GitHub Actions](https://github.com/Quickits/ZPatch/actions/workflows/release.yml/badge.svg)](https://github.com/Quickits/ZPatch/actions)

---

<div id="lang-switch">
  <a href="#" onclick="showLang('en'); return false;">English</a> |
  <a href="#" onclick="showLang('zh'); return false;">中文</a>
</div>

---

</div>

## English

<div id="en-content">

### Overview

**ZPatch** is a high-performance differential patch library powered by [Zstd](https://github.com/facebook/zstd) compression. It uses the old file as a dictionary to compress the new file, achieving significantly higher compression ratios than traditional diff algorithms.

### Features

- 🚀 **High Compression Ratio** - Leverages Zstd dictionary compression
- 📦 **Zero External Dependencies** - Pure C++ implementation with bundled Zstd
- 🌍 **Cross-Platform** - Supports Android, iOS, Linux, macOS, Windows
- 📱 **Android Ready** - 16KB page size support for Android 15+
- 🔧 **Simple API** - Easy to integrate into any project

### Downloads

Pre-built binaries are available on the [Releases](https://github.com/Quickits/ZPatch/releases) page:

| File | Platform | Architecture |
|------|----------|--------------|
| `zpatch-darwin-amd64` | macOS | x86_64 |
| `zpatch-darwin-arm64` | macOS | ARM64 (Apple Silicon) |
| `zpatch-linux-amd64` | Linux | x86_64 |
| `zpatch-android.aar` | Android | AAR Library |

### Quick Start

#### CLI Tool

```bash
# Show version
./zpatch version

# Create a patch
./zpatch create <old-file> <new-file> <patch-file>

# Apply a patch
./zpatch apply <old-file> <patch-file> <new-file>
```

#### Android Integration

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

#### C++ API

```cpp
#include "zpatch/core.h"

// Create patch
zpatch::Result result = zpatch::createPatch("old.bin", "new.bin", "patch.zst");

// Apply patch
zpatch::Result result = zpatch::applyPatch("old.bin", "patch.zst", "new.bin");
```

### Building

#### CLI Tool

```bash
cd cli

# Build for current platform
./build-mac-amd64.sh    # macOS x86_64
./build-mac-arm64.sh    # macOS ARM64
./build-linux-amd64.sh  # Linux x86_64

# Build all platforms
./build-all.sh
```

#### Android Library

```bash
cd android
./gradlew :library:assembleRelease
```

#### Core Library

```bash
cd core
mkdir build && cd build
cmake ..
make
```

### API Reference

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

### Project Structure

```
zpatch/
├── core/       # C++ core library
├── cli/        # Command-line tool
├── android/    # Android library + sample app
└── .github/    # GitHub Actions workflows
```

### License

MIT License - see [LICENSE](LICENSE) for details.

</div>

---

## 中文

<div id="zh-content">

### 简介

**ZPatch** 是一个基于 [Zstd](https://github.com/facebook/zstd) 压缩的高性能差分补丁库。它使用旧文件作为字典来压缩新文件，相比传统差分算法能大幅减小补丁文件大小。

### 特性

- 🚀 **高压缩比** - 利用 Zstd 字典压缩技术
- 📦 **零外部依赖** - 纯 C++ 实现，内嵌 Zstd 库
- 🌍 **跨平台** - 支持 Android、iOS、Linux、macOS、Windows
- 📱 **Android 适配** - 支持 Android 15+ 的 16KB 页面大小
- 🔧 **简单易用** - API 简洁，易于集成

### 下载

预编译二进制文件可在 [Releases](https://github.com/Quickits/ZPatch/releases) 页面获取：

| 文件 | 平台 | 架构 |
|------|------|------|
| `zpatch-darwin-amd64` | macOS | x86_64 |
| `zpatch-darwin-arm64` | macOS | ARM64 (Apple Silicon) |
| `zpatch-linux-amd64` | Linux | x86_64 |
| `zpatch-android.aar` | Android | AAR 库 |

### 快速开始

#### 命令行工具

```bash
# 查看版本
./zpatch version

# 创建补丁
./zpatch create <旧文件> <新文件> <补丁文件>

# 应用补丁
./zpatch apply <旧文件> <补丁文件> <新文件>
```

#### Android 集成

在 `build.gradle.kts` 中添加：

```kotlin
dependencies {
    implementation("com.github.quickits:zpatch:VERSION")
}
```

Kotlin API：

```kotlin
import com.quickits.zpatch.ZPatch

// 创建补丁
val result = ZPatch.createPatch(oldPath, newPath, patchPath)
if (result.isSuccess) {
    println("补丁创建成功: ${result.originalSize} 字节")
}

// 应用补丁
val result = ZPatch.applyPatch(oldPath, patchPath, newPath)
if (result.isSuccess) {
    println("文件还原成功: ${result.originalSize} 字节")
}
```

#### C++ API

```cpp
#include "zpatch/core.h"

// 创建补丁
zpatch::Result result = zpatch::createPatch("old.bin", "new.bin", "patch.zst");

// 应用补丁
zpatch::Result result = zpatch::applyPatch("old.bin", "patch.zst", "new.bin");
```

### 编译

#### 命令行工具

```bash
cd cli

# 构建当前平台
./build-mac-amd64.sh    # macOS x86_64
./build-mac-arm64.sh    # macOS ARM64
./build-linux-amd64.sh  # Linux x86_64

# 构建所有平台
./build-all.sh
```

#### Android 库

```bash
cd android
./gradlew :library:assembleRelease
```

#### 核心库

```bash
cd core
mkdir build && cd build
cmake ..
make
```

### API 参考

```cpp
namespace zpatch {
    // 获取版本信息
    const char* getVersion();

    // 创建补丁文件
    Result createPatch(const char* 旧文件, const char* 新文件, const char* 补丁文件);

    // 应用补丁文件
    Result applyPatch(const char* 旧文件, const char* 补丁文件, const char* 新文件);
}

struct Result {
    int code;           // 0 = 成功，非 0 = 失败
    const char* message; // 错误信息（静态字符串）
    uint64_t originalSize; // 输出文件大小
};
```

### 项目结构

```
zpatch/
├── core/       # C++ 核心库
├── cli/        # 命令行工具
├── android/    # Android 库 + 示例应用
└── .github/    # GitHub Actions 工作流
```

### 许可证

MIT License - 详见 [LICENSE](LICENSE) 文件。

</div>

---

<script>
function showLang(lang) {
  const en = document.getElementById('en-content');
  const zh = document.getElementById('zh-content');

  if (lang === 'en') {
    en.style.display = 'block';
    zh.style.display = 'none';
  } else {
    en.style.display = 'none';
    zh.style.display = 'block';
  }
}

// Auto-detect browser language
if (navigator.language.startsWith('zh')) {
  showLang('zh');
} else {
  showLang('en');
}
</script>
