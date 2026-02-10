cmake_minimum_required(VERSION 3.18)
project(zpatch_core C CXX ASM)

# Set C++ standard
set(CMAKE_CXX_STANDARD 17)
set(CMAKE_CXX_STANDARD_REQUIRED ON)

# Define Zstd library source files
file(GLOB_RECURSE ZSTD_C_SOURCES
    "${CMAKE_CURRENT_SOURCE_DIR}/src/zstd_lib/common/*.c"
    "${CMAKE_CURRENT_SOURCE_DIR}/src/zstd_lib/compress/*.c"
    "${CMAKE_CURRENT_SOURCE_DIR}/src/zstd_lib/decompress/*.c"
)

# AMD64 assembly files (for x86_64 only)
set(ZSTD_ASM_SOURCES
    ${CMAKE_CURRENT_SOURCE_DIR}/src/zstd_lib/decompress/huf_decompress_amd64.S
)

set(ZSTD_SOURCES ${ZSTD_C_SOURCES})

# Core source files
set(CORE_SOURCES
    src/FileBuffer.cpp
    src/PatchCore.cpp
)

# Build static library
add_library(zpatch_core STATIC ${CORE_SOURCES} ${ZSTD_C_SOURCES} ${ZSTD_ASM_SOURCES})

# Public header directories
target_include_directories(zpatch_core PUBLIC
    ${CMAKE_CURRENT_SOURCE_DIR}/include
    ${CMAKE_CURRENT_SOURCE_DIR}/src
)

# Zstd header paths
target_include_directories(zpatch_core PRIVATE
    ${CMAKE_CURRENT_SOURCE_DIR}/src/zstd_lib
)

# Platform-specific configuration
if(ANDROID)
    find_library(log-lib log)
    target_link_libraries(zpatch_core ${log-lib})
elseif(UNIX AND NOT APPLE)
    target_link_libraries(zpatch_core pthread)
elseif(APPLE)
    target_link_libraries(zpatch_core "-framework Foundation")
elseif(WIN32)
    # Windows link configuration
    target_link_libraries(zpatch_core)
endif()
