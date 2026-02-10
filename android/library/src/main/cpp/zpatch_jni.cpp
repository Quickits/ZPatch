#include <jni.h>
#include <cstring>
#include "zpatch/core.h"

extern "C" {

// Helper class to manage JNI local references
class JStringGuard {
public:
    JStringGuard(JNIEnv* env, jstring jstr) : env_(env), jstr_(jstr) {
        cstr_ = env->GetStringUTFChars(jstr, nullptr);
    }
    ~JStringGuard() {
        if (cstr_) {
            env_->ReleaseStringUTFChars(jstr_, cstr_);
        }
    }
    const char* c_str() const { return cstr_; }

private:
    JNIEnv* env_;
    jstring jstr_;
    const char* cstr_;
};

// Get version
JNIEXPORT jstring JNICALL
Java_com_quickits_zpatch_ZPatch_getVersion(JNIEnv* env, jclass clazz) {
    const char* version = zpatch::getVersion();
    return env->NewStringUTF(version);
}

// Create patch
JNIEXPORT jobject JNICALL
Java_com_quickits_zpatch_ZPatch_createPatch(
    JNIEnv* env,
    jclass clazz,
    jstring oldFilePath,
    jstring newFilePath,
    jstring patchFilePath
) {
    JStringGuard oldPath(env, oldFilePath);
    JStringGuard newPath(env, newFilePath);
    JStringGuard patchPath(env, patchFilePath);

    zpatch::Result result = zpatch::createPatch(
        oldPath.c_str(),
        newPath.c_str(),
        patchPath.c_str()
    );

    // Create PatchResult object
    jclass resultClass = env->FindClass("com/quickits/zpatch/PatchResult");
    if (!resultClass) return nullptr;

    jmethodID constructor = env->GetMethodID(resultClass, "<init>", "(IJLjava/lang/String;)V");
    if (!constructor) return nullptr;

    jstring message = env->NewStringUTF(result.message ? result.message : "");

    jobject resultObj = env->NewObject(
        resultClass,
        constructor,
        static_cast<jint>(result.code),
        static_cast<jlong>(result.originalSize),
        message
    );

    env->DeleteLocalRef(resultClass);
    env->DeleteLocalRef(message);

    return resultObj;
}

// Apply patch
JNIEXPORT jobject JNICALL
Java_com_quickits_zpatch_ZPatch_applyPatch(
    JNIEnv* env,
    jclass clazz,
    jstring oldFilePath,
    jstring patchFilePath,
    jstring newFilePath
) {
    JStringGuard oldPath(env, oldFilePath);
    JStringGuard patchPath(env, patchFilePath);
    JStringGuard newPath(env, newFilePath);

    zpatch::Result result = zpatch::applyPatch(
        oldPath.c_str(),
        patchPath.c_str(),
        newPath.c_str()
    );

    // Create PatchResult object
    jclass resultClass = env->FindClass("com/quickits/zpatch/PatchResult");
    if (!resultClass) return nullptr;

    jmethodID constructor = env->GetMethodID(resultClass, "<init>", "(IJLjava/lang/String;)V");
    if (!constructor) return nullptr;

    jstring message = env->NewStringUTF(result.message ? result.message : "");

    jobject resultObj = env->NewObject(
        resultClass,
        constructor,
        static_cast<jint>(result.code),
        static_cast<jlong>(result.originalSize),
        message
    );

    env->DeleteLocalRef(resultClass);
    env->DeleteLocalRef(message);

    return resultObj;
}

} // extern "C"
