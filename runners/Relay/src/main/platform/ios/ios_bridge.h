#ifndef iOS_BRIDGE_H
#define iOS_BRIDGE_H

#include <stdint.h>
#include <stddef.h>

#ifdef __cplusplus
extern "C" {
#endif

// Function pointer typedef - must be visible to cinterop

typedef int32_t (*HostRequestFn)(const char* url, size_t len, int32_t method);

// Public C API
void relay_set_request_handler(HostRequestFn handler);
void relay_set_logger(void (*logger)(const char*, size_t));
void* relay_create_module(const uint8_t* bytes, size_t size);
int relay_add(void* modulePtr, int a, int b);
const char* relay_callMethod(void* modulePtr, const char* name);
void relay_destroy_module(void* modulePtr);

#ifdef __cplusplus
}
#endif

#endif // iOS_BRIDGE_H