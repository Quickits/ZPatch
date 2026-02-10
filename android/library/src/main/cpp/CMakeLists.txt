cmake_minimum_required(VERSION 3.22.1)
project("zpatch_jni")

# Set C++ standard
set(CMAKE_CXX_STANDARD 17)
set(CMAKE_CXX_STANDARD_REQUIRED ON)

# Core directory path (accessed via ANDROID_PROJECT_ROOT passed from Gradle)
# ANDROID_PROJECT_ROOT = android/ directory
# core/ directory is at the same level as android/
set(CORE_DIR ${ANDROID_PROJECT_ROOT}/../core)

# Import zpatch_core static library
add_subdirectory(${CORE_DIR} ${CMAKE_CURRENT_BINARY_DIR}/zpatch_core)

# JNI source files
set(JNI_SOURCES
    zpatch_jni.cpp
)

# Build JNI shared library
add_library(zpatch_jni SHARED ${JNI_SOURCES})

# Support 16KB page size (Android 15+)
# NDK r27+ handles this automatically via ANDROID_SUPPORT_FLEXIBLE_PAGE_SIZES
# Additional linker flag to ensure correct alignment for all architectures
if(ANDROID_SUPPORT_FLEXIBLE_PAGE_SIZES)
    target_link_options(zpatch_jni PRIVATE
        "LINKER:-z,max-page-size=16384"
    )
endif()

# Link zpatch_core static library
target_link_libraries(zpatch_jni
    zpatch_core
    android
    log
)

# Header file paths
target_include_directories(zpatch_jni PRIVATE
    ${CORE_DIR}/include
    ${CORE_DIR}/src
)
