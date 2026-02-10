cmake_minimum_required(VERSION 3.18)
project(zpatch_core C CXX ASM)

# 设置 C++ 标准
set(CMAKE_CXX_STANDARD 17)
set(CMAKE_CXX_STANDARD_REQUIRED ON)

# 定义 Zstd 库源文件
file(GLOB_RECURSE ZSTD_C_SOURCES
    "${CMAKE_CURRENT_SOURCE_DIR}/src/zstd_lib/common/*.c"
    "${CMAKE_CURRENT_SOURCE_DIR}/src/zstd_lib/compress/*.c"
    "${CMAKE_CURRENT_SOURCE_DIR}/src/zstd_lib/decompress/*.c"
)

# AMD64 汇编文件（仅用于 x86_64）
set(ZSTD_ASM_SOURCES
    ${CMAKE_CURRENT_SOURCE_DIR}/src/zstd_lib/decompress/huf_decompress_amd64.S
)

set(ZSTD_SOURCES ${ZSTD_C_SOURCES})

# 核心源文件
set(CORE_SOURCES
    src/FileBuffer.cpp
    src/PatchCore.cpp
)

# 构建静态库
add_library(zpatch_core STATIC ${CORE_SOURCES} ${ZSTD_C_SOURCES} ${ZSTD_ASM_SOURCES})

# 公共头文件目录
target_include_directories(zpatch_core PUBLIC
    ${CMAKE_CURRENT_SOURCE_DIR}/include
    ${CMAKE_CURRENT_SOURCE_DIR}/src
)

# Zstd 头文件路径
target_include_directories(zpatch_core PRIVATE
    ${CMAKE_CURRENT_SOURCE_DIR}/src/zstd_lib
)

# 平台特定配置
if(ANDROID)
    find_library(log-lib log)
    target_link_libraries(zpatch_core ${log-lib})
elseif(UNIX AND NOT APPLE)
    target_link_libraries(zpatch_core pthread)
elseif(APPLE)
    target_link_libraries(zpatch_core "-framework Foundation")
elseif(WIN32)
    # Windows 链接配置
    target_link_libraries(zpatch_core)
endif()
