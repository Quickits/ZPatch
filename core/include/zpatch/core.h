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

// Version information
ZPATCH_API const char* getVersion();

// Operation result
struct Result {
    int code;           // 0 = success, non-zero = failure
    const char* message; // Error message (static string, do not free)
    uint64_t originalSize; // Original file size
};

// Create patch
// Compress new file into patch using old file as dictionary
ZPATCH_API Result createPatch(
    const char* oldFilePath,
    const char* newFilePath,
    const char* patchFilePath
);

// Apply patch
// Restore new file using old file and patch file
ZPATCH_API Result applyPatch(
    const char* oldFilePath,
    const char* patchFilePath,
    const char* newFilePath
);

} // namespace zpatch

#endif // ZPATCH_CORE_H
