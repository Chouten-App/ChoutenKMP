#pragma once
#include <stddef.h>
#include <stdint.h>

#ifdef __cplusplus
extern "C" {
#endif

    typedef const char* (*HostRequestFn)(const char* url, size_t len, int32_t method, uint32_t* out_len);

    // Setup stuff
    void relay_set_logger(void (*logger)(const char*, size_t));
    void relay_set_request_handler(HostRequestFn handler);
    void relay_set_html_parse_handler(uint32_t (*html_parse)(const char*, size_t));
    void* relay_create_module(const uint8_t* bytes, size_t size);
    void relay_destroy_module(void* modulePtr);

    // Host functions

    // Module functions
    int relay_add(void* modulePtr, int a, int b);
    const char* relay_callMethod(void* modulePtr, const char* name);

    #ifdef __cplusplus
}
#endif