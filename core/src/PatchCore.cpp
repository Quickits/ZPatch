#include "zpatch/core.h"
#include "FileBuffer.h"
#include "zstd.h"
#include <cstring>
#include <cstdlib>

#ifdef ANDROID
    #include <android/log.h>
    #define LOG_TAG "ZPatch"
    #define LOGI(...) __android_log_print(ANDROID_LOG_INFO, LOG_TAG, __VA_ARGS__)
    #define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, __VA_ARGS__)
#else
    #define LOGI(...)
    #define LOGE(...)
#endif

#define COMPRESSION_LEVEL 3

namespace zpatch {

// 静态错误消息
static const char* MSG_SUCCESS = "Success";
static const char* MSG_READ_FAILED = "Failed to read input files";
static const char* MSG_WRITE_FAILED = "Failed to write output file";
static const char* MSG_COMPRESS_ERROR = "Compression error";
static const char* MSG_DECOMPRESS_ERROR = "Decompression error";
static const char* MSG_MEMORY_ERROR = "Memory allocation failed";
static const char* MSG_CONTEXT_ERROR = "Failed to create context";
static const char* MSG_INVALID_PATCH = "Invalid patch file";

static unsigned calculate_window_log(unsigned long long file_size) {
    unsigned wlog = 0;
    while (file_size > 0) { wlog++; file_size >>= 1; }
    if (wlog < 10) wlog = 10;
    if (wlog > 31) wlog = 31;
    return wlog;
}

const char* getVersion() {
    return ZSTD_versionString();
}

Result createPatch(
    const char* oldFilePath,
    const char* newFilePath,
    const char* patchFilePath
) {
    Result result = {1, MSG_SUCCESS, 0};

    FileBuffer old_fb, new_fb;
    ZSTD_CCtx* cctx = nullptr;
    void* patch_data = nullptr;

    do {
        if (!old_fb.readFromFile(oldFilePath) || !new_fb.readFromFile(newFilePath)) {
            result.message = MSG_READ_FAILED;
            break;
        }

        LOGI("Old file size: %zu, New file size: %zu", old_fb.size, new_fb.size);

        cctx = ZSTD_createCCtx();
        if (!cctx) {
            result.message = MSG_CONTEXT_ERROR;
            break;
        }

        // 配置压缩参数
        unsigned wlog = calculate_window_log(static_cast<unsigned long long>(new_fb.size));
        ZSTD_CCtx_setParameter(cctx, ZSTD_c_windowLog, wlog);
        ZSTD_CCtx_setParameter(cctx, ZSTD_c_enableLongDistanceMatching, 1);
        ZSTD_CCtx_setParameter(cctx, ZSTD_c_compressionLevel, COMPRESSION_LEVEL);

        // 分配合配缓冲区
        size_t patch_size = ZSTD_compressBound(new_fb.size);
        patch_data = malloc(patch_size);
        if (!patch_data) {
            result.message = MSG_MEMORY_ERROR;
            break;
        }

        // 使用旧文件作为字典进行压缩
        ZSTD_CCtx_refPrefix(cctx, old_fb.data, old_fb.size);
        size_t compressed_size = ZSTD_compress2(cctx, patch_data, patch_size,
                                                 new_fb.data, new_fb.size);

        if (ZSTD_isError(compressed_size)) {
            result.message = MSG_COMPRESS_ERROR;
            LOGE("Compression error: %s", ZSTD_getErrorName(compressed_size));
            break;
        }

        LOGI("Compressed size: %zu -> %zu", new_fb.size, compressed_size);
        result.originalSize = compressed_size;

        // 写入补丁文件
        if (FileBuffer::writeToFile(patchFilePath, patch_data, compressed_size)) {
            LOGI("Patch written to: %s", patchFilePath);
            result.code = 0;
            result.message = MSG_SUCCESS;
        } else {
            result.message = MSG_WRITE_FAILED;
        }
    } while (0);

    // 清理资源
    if (patch_data) free(patch_data);
    if (cctx) ZSTD_freeCCtx(cctx);
    old_fb.release();
    new_fb.release();

    return result;
}

Result applyPatch(
    const char* oldFilePath,
    const char* patchFilePath,
    const char* newFilePath
) {
    Result result = {1, MSG_SUCCESS, 0};

    FileBuffer old_fb, patch_fb;
    ZSTD_DCtx* dctx = nullptr;
    void* new_data = nullptr;
    unsigned long long new_size = 0;

    do {
        if (!old_fb.readFromFile(oldFilePath) || !patch_fb.readFromFile(patchFilePath)) {
            result.message = MSG_READ_FAILED;
            break;
        }

        dctx = ZSTD_createDCtx();
        if (!dctx) {
            result.message = MSG_CONTEXT_ERROR;
            break;
        }

        // 获取解压后大小
        new_size = ZSTD_getFrameContentSize(patch_fb.data, patch_fb.size);
        if (new_size == ZSTD_CONTENTSIZE_ERROR || new_size == ZSTD_CONTENTSIZE_UNKNOWN) {
            result.message = MSG_INVALID_PATCH;
            break;
        }

        new_data = malloc(new_size);
        if (!new_data) {
            result.message = MSG_MEMORY_ERROR;
            break;
        }

        // 使用旧文件作为字典进行解压
        ZSTD_DCtx_refPrefix(dctx, old_fb.data, old_fb.size);
        size_t decompress_result = ZSTD_decompressDCtx(dctx, new_data, new_size,
                                                       patch_fb.data, patch_fb.size);

        if (ZSTD_isError(decompress_result)) {
            result.message = MSG_DECOMPRESS_ERROR;
            LOGE("Decompression error: %s", ZSTD_getErrorName(decompress_result));
            break;
        }

        result.originalSize = decompress_result;

        // 写入新文件
        if (FileBuffer::writeToFile(newFilePath, new_data, decompress_result)) {
            result.code = 0;
            result.message = MSG_SUCCESS;
        } else {
            result.message = MSG_WRITE_FAILED;
        }
    } while (0);

    // 清理资源
    if (new_data) free(new_data);
    if (dctx) ZSTD_freeDCtx(dctx);
    old_fb.release();
    patch_fb.release();

    return result;
}

} // namespace zpatch
