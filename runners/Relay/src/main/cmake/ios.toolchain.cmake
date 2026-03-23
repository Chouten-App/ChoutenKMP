# iOS Toolchain for CMake
set(CMAKE_SYSTEM_NAME iOS)
set(CMAKE_OSX_DEPLOYMENT_TARGET "15.0" CACHE STRING "Minimum iOS deployment version")

# Set architecture based on input
if(NOT DEFINED IOS_ARCH)
    set(IOS_ARCH "arm64")
endif()

if(IOS_ARCH STREQUAL "arm64")
    set(CMAKE_OSX_ARCHITECTURES "arm64")
    set(CMAKE_OSX_SYSROOT "iphoneos")
elseif(IOS_ARCH STREQUAL "x86_64" OR IOS_ARCH STREQUAL "arm64_simulator")
    set(CMAKE_OSX_ARCHITECTURES "arm64")
    set(CMAKE_OSX_SYSROOT "iphonesimulator")
endif()

set(CMAKE_C_FLAGS "${CMAKE_C_FLAGS} -fembed-bitcode")
set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -fembed-bitcode")