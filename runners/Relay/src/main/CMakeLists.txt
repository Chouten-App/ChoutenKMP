cmake_minimum_required(VERSION 3.18)
if(IOS)
    project(relay_native C CXX OBJCXX)
else()
    project(relay_native C CXX)
endif()

set(CMAKE_C_STANDARD 11)
set(CMAKE_CXX_STANDARD 17)

file(GLOB WASM3_SRC "wasm3/*.c")

file(GLOB ENGINE_SOURCES "engine/*.cpp")

if(IOS)
    set(PLATFORM_SOURCES platform/ios/ios_bridge.mm)
    set(LIBRARY_TYPE STATIC)
else()
    file(GLOB PLATFORM_SOURCES "platform/jni/*.cpp")
    set(LIBRARY_TYPE SHARED)
endif()

add_library(relay ${LIBRARY_TYPE}
    ${WASM3_SRC}
    ${ENGINE_SOURCES}
    ${PLATFORM_SOURCES}
)

target_link_libraries(relay PRIVATE wasm3)

target_include_directories(relay PUBLIC
    include
    engine
    wasm3
)

target_compile_options(relay PRIVATE -fexceptions)

if(IOS)
    target_compile_options(relay PRIVATE -fPIC -Wno-extern-c-compat)
    set_source_files_properties(${PLATFORM_SOURCES}
        PROPERTIES COMPILE_FLAGS "-fobjc-arc"
    )
else()
    target_compile_options(relay PRIVATE -frtti -fno-limit-debug-info)
    if(ANDROID)
        find_library(log-lib log)
        target_link_libraries(relay PRIVATE ${log-lib})
    endif()
endif()
