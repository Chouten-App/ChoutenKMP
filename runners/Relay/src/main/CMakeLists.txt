cmake_minimum_required(VERSION 3.18)
project(relay_native)

# 1️⃣ Collect Wasm3 source files
file(GLOB WASM3_SRC "wasm3/*.c")

# 2️⃣ Build Wasm3 as a static library
add_library(wasm3 STATIC ${WASM3_SRC})
target_include_directories(wasm3 PUBLIC wasm3)

# 3️⃣ Build your relay JNI library
add_library(relay SHARED
    engine/relay_native.cpp
    platform/android/jni_bridge.cpp
)

# 4️⃣ Link Wasm3 + log library
find_library(log-lib log)
target_link_libraries(relay PRIVATE wasm3 ${log-lib})

# 5️⃣ Compiler flags
target_compile_features(relay PRIVATE cxx_std_17)
target_compile_options(relay PRIVATE -frtti -fexceptions -fno-limit-debug-info)
