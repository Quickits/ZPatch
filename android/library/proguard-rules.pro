# ZPatch Library ProGuard Rules

# Keep ZPatch class and its native methods
-keep class com.quickits.zpatch.ZPatch {
    *;
}

# Keep PatchResult class
-keep class com.quickits.zpatch.PatchResult {
    *;
}

# Keep native methods
-keepclasseswithmembernames class * {
    native <methods>;
}

# Keep JNI related
-keep class com.quickits.zpatch.** { *; }
-keep interface com.quickits.zpatch.** { *; }
