cmake_minimum_required(VERSION 3.18.1)
project("zpatch_jni")

# 设置 C++ 标准
set(CMAKE_CXX_STANDARD 17)
set(CMAKE_CXX_STANDARD_REQUIRED ON)

# Core 目录路径 (通过 Gradle 传入的 ANDROID_PROJECT_ROOT 寻址)
# ANDROID_PROJECT_ROOT = android/ 目录
# core/ 目录与 android/ 同级
set(CORE_DIR ${ANDROID_PROJECT_ROOT}/../core)

# 导入 zpatch_core 静态库
add_subdirectory(${CORE_DIR} ${CMAKE_CURRENT_BINARY_DIR}/zpatch_core)

# JNI 源文件
set(JNI_SOURCES
    zpatch_jni.cpp
)

# 构建 JNI 共享库
add_library(zpatch_jni SHARED ${JNI_SOURCES})

# 链接 zpatch_core 静态库
target_link_libraries(zpatch_jni
    zpatch_core
    android
    log
)

# 头文件路径
target_include_directories(zpatch_jni PRIVATE
    ${CORE_DIR}/include
    ${CORE_DIR}/src
)
