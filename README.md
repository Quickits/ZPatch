# ZPatch

基于 Zstd 的差分补丁库，使用旧文件作为字典压缩新文件，生成高压缩比的补丁文件。

## 特性

- **高压缩比**：利用 Zstd 的字典压缩技术，大幅减小补丁文件大小
- **跨平台**：支持 Android、iOS、Linux、macOS、Windows
- **16KB 页面支持**：适配 Android API 35+ 的新设备
- **纯 C++ 实现**：无外部依赖，易于集成

## 编译

```bash
cd core
mkdir build && cd build
cmake ..
make
```

## API

```cpp
namespace zpatch {

// 获取版本信息
const char* getVersion();

// 创建补丁
Result createPatch(
    const char* oldFilePath,   // 旧文件（作为字典）
    const char* newFilePath,   // 新文件
    const char* patchFilePath  // 输出补丁文件
);

// 应用补丁
Result applyPatch(
    const char* oldFilePath,   // 旧文件
    const char* patchFilePath, // 补丁文件
    const char* newFilePath    // 输出新文件
);

}
```

## 项目结构

```
ZPatch/
└── core/              # C++ 核心库
    ├── include/       # 公共头文件
    │   └── zpatch/core.h
    ├── src/           # 源代码
    │   ├── FileBuffer.{h,cpp}
    │   ├── PatchCore.cpp
    │   └── zstd_lib/  # Zstd 库
    └── CMakeLists.txt
```

## 许可证

MIT License

## 致谢

- [Zstd](https://github.com/facebook/zstd) - Facebook 的高性能压缩算法
