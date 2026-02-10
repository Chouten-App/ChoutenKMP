#pragma once
#include <stddef.h>
#include <stdint.h>

#ifdef __cplusplus
extern "C" {
    #endif

    // Setup stuff
    void relay_set_logger(void (*logger)(const char*, size_t));
    void* relay_create_module(const uint8_t* bytes, size_t size);
    void relay_destroy_module(void* modulePtr);

    // Host functions

    // Module functions
    int relay_add(void* modulePtr, int a, int b);
    const char* relay_callMethod(void* modulePtr, const char* name);

    #ifdef __cplusplus
}
#endif