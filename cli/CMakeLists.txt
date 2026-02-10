cmake_minimum_required(VERSION 3.18)
project(zpatch)

set(CMAKE_CXX_STANDARD 17)
set(CMAKE_CXX_STANDARD_REQUIRED ON)

# 导入核心库
add_subdirectory(${CMAKE_CURRENT_SOURCE_DIR}/../core ${CMAKE_CURRENT_BINARY_DIR}/core)

# 命令行工具
add_executable(zpatch main.cpp)

target_link_libraries(zpatch
    zpatch_core
)

target_include_directories(zpatch PRIVATE
    ${CMAKE_CURRENT_SOURCE_DIR}/../core/include
)

# 安装规则
install(TARGETS zpatch DESTINATION bin)
