cmake_minimum_required(VERSION 3.22.1)
project(pocketcoded_node_bridge)

set(NODE_ROOT "${CMAKE_CURRENT_SOURCE_DIR}/src/main/nodejs-mobile")
if(NOT EXISTS "${NODE_ROOT}/include/node/node.h")
    message(FATAL_ERROR "Node.js Mobile runtime is missing. Run tools/prepare-nodejs-mobile.ps1 first.")
endif()

add_library(node SHARED IMPORTED)
set_target_properties(node PROPERTIES
    IMPORTED_LOCATION "${CMAKE_CURRENT_SOURCE_DIR}/src/main/jniLibs/${ANDROID_ABI}/libnode.so")

add_library(pocketcoded-node SHARED src/main/cpp/pocketcoded_node.cpp)
target_link_options(pocketcoded-node PRIVATE "-Wl,-z,max-page-size=16384")
target_include_directories(pocketcoded-node PRIVATE "${NODE_ROOT}/include/node")
find_library(log-lib log)
target_link_libraries(pocketcoded-node node ${log-lib})
