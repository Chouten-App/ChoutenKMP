# =============================================================================
# CMakeLists.txt - Relay Native Library
# =============================================================================
# This builds the native WASM runtime for both Android and iOS.
#
# Android: Built automatically by Gradle via externalNativeBuild
#          Produces: librelay.so (shared library for JNI)
#
# iOS:     Built manually before Gradle build
#          Run: cmake -S . -B ../../build/ios-arm64 -DCMAKE_SYSTEM_NAME=iOS
#               cmake --build ../../build/ios-arm64
#          Produces: librelay.a + libwasm3.a (static libraries for cinterop)
# =============================================================================

cmake_minimum_required(VERSION 3.18)
project(relay_native C CXX)

set(CMAKE_C_STANDARD 11)
set(CMAKE_CXX_STANDARD 17)

# -----------------------------------------------------------------------------
# 1. Wasm3 - WebAssembly interpreter (https://github.com/wasm3/wasm3)
# -----------------------------------------------------------------------------
# Collect all wasm3 source files from the wasm3/ directory
file(GLOB WASM3_SRC "wasm3/*.c")

add_library(wasm3 STATIC ${WASM3_SRC})
target_include_directories(wasm3 PUBLIC wasm3)
target_compile_options(wasm3 PRIVATE -Wno-extern-c-compat)

# -----------------------------------------------------------------------------
# 2. Platform-specific relay library
# -----------------------------------------------------------------------------
if(CMAKE_SYSTEM_NAME STREQUAL "iOS")
    # iOS: Static library (.a) - linked into Kotlin/Native framework via cinterop
    # Uses ios_bridge.mm which implements the C API (relay_c_api.h)
    add_library(relay STATIC
        engine/relay_native.cpp      # Wasm3Module C++ wrapper
        platform/ios/ios_bridge.mm   # C API implementation for iOS
    )
    target_include_directories(relay PUBLIC
        ${CMAKE_CURRENT_SOURCE_DIR}/include  # relay_c_api.h
        ${CMAKE_CURRENT_SOURCE_DIR}/engine   # relay_native.hpp
    )
    target_link_libraries(relay PRIVATE wasm3)
    target_compile_options(relay PRIVATE -fPIC -Wno-extern-c-compat)
    # Enable ARC (Automatic Reference Counting) for Objective-C++
    set_source_files_properties(platform/ios/ios_bridge.mm PROPERTIES COMPILE_FLAGS "-fobjc-arc")
else()
    # Android: Shared library (.so) - loaded at runtime via System.loadLibrary()
    # Uses jni_bridge.cpp which provides JNI functions for Kotlin
    add_library(relay SHARED
        engine/relay_native.cpp        # Wasm3Module C++ wrapper
        platform/android/jni_bridge.cpp # JNI bridge for Android
    )
    find_library(log-lib log)  # Android logging library
    target_link_libraries(relay PRIVATE wasm3 ${log-lib})
    target_compile_options(relay PRIVATE -frtti -fexceptions -fno-limit-debug-info)
endif()

target_compile_features(relay PRIVATE cxx_std_17)
