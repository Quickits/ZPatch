cmake_minimum_required(VERSION 3.18)
project(zpatch)

set(CMAKE_CXX_STANDARD 17)
set(CMAKE_CXX_STANDARD_REQUIRED ON)

# Import core library
add_subdirectory(${CMAKE_CURRENT_SOURCE_DIR}/../core ${CMAKE_CURRENT_BINARY_DIR}/core)

# Command-line tool
add_executable(zpatch main.cpp)

target_link_libraries(zpatch
    zpatch_core
)

target_include_directories(zpatch PRIVATE
    ${CMAKE_CURRENT_SOURCE_DIR}/../core/include
)

# Install rules
install(TARGETS zpatch DESTINATION bin)
