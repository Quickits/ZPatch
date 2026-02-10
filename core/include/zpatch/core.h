#ifndef ZPATCH_CORE_H
#define ZPATCH_CORE_H

#include <cstdint>

#ifdef _WIN32
    #define ZPATCH_API __declspec(dllexport)
#else
    #if defined(__GNUC__) && __GNUC__ >= 4
        #define ZPATCH_API __attribute__ ((visibility ("default")))
    #else
        #define ZPATCH_API
    #endif
#endif

namespace zpatch {

// 版本信息
ZPATCH_API const char* getVersion();

// 操作结果
struct Result {
    int code;           // 0 = 成功, 非 0 = 失败
    const char* message; // 错误信息 (静态字符串，无需释放)
    uint64_t originalSize; // 原始文件大小
};

// 创建补丁
// 使用旧文件作为字典，将新文件压缩成补丁文件
ZPATCH_API Result createPatch(
    const char* oldFilePath,
    const char* newFilePath,
    const char* patchFilePath
);

// 应用补丁
// 使用旧文件和补丁文件，还原出新文件
ZPATCH_API Result applyPatch(
    const char* oldFilePath,
    const char* patchFilePath,
    const char* newFilePath
);

} // namespace zpatch

#endif // ZPATCH_CORE_H
