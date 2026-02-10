package com.quickits.zpatch

/**
 * ZPatch - Zstd based differential patch library for Android
 */
object ZPatch {

    init {
        System.loadLibrary("zpatch_jni")
    }

    /**
     * Get the version of ZPatch library
     */
    external fun getVersion(): String

    /**
     * Create a patch file using old file as dictionary
     *
     * @param oldPath Path to the old file (used as dictionary)
     * @param newPath Path to the new file (to be compressed)
     * @param patchPath Path to the output patch file
     * @return PatchResult containing the operation result
     */
    external fun createPatch(
        oldPath: String,
        newPath: String,
        patchPath: String
    ): PatchResult

    /**
     * Apply a patch file to restore the new file
     *
     * @param oldPath Path to the old file
     * @param patchPath Path to the patch file
     * @param newPath Path to the output new file
     * @return PatchResult containing the operation result
     */
    external fun applyPatch(
        oldPath: String,
        patchPath: String,
        newPath: String
    ): PatchResult
}

/**
 * Result of a patch operation
 *
 * @property code 0 for success, non-zero for failure
 * @property originalSize Original file size in bytes
 * @property message Error message (if any)
 */
data class PatchResult(
    val code: Int,
    val originalSize: Long,
    val message: String
) {
    val isSuccess: Boolean get() = code == 0
    val errorMessage: String? get() = if (code != 0) message else null
}
