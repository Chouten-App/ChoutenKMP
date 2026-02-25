#import <Foundation/Foundation.h>
#import "relay_native.hpp"

static void (*gHostLogger)(const char*, size_t) = nullptr;

typedef const char* (*HostRequestFn)(const char* url, size_t len, int32_t method);

static HostRequestFn gHostRequest = nullptr;

extern "C" void relay_set_request_handler(HostRequestFn handler) {
    gHostRequest = handler;
}

// Called from Swift to register logger
extern "C" void relay_set_logger(void (*logger)(const char*, size_t)) {
    gHostLogger = logger;
}

// This is the function Wasm3 calls
void host_log(const char* msg, size_t len) {
    if (gHostLogger) {
        gHostLogger(msg, len);
    }
}

// Host request function - stub for now
int32_t host_request(const char* url, size_t len, int32_t method) {
    if (!gHostRequest) {
        host_log("[host_request] No handler set", 30);
        return -1;
    }
    const char* result = gHostRequest(url, len, method);
    return result ? (int32_t)(intptr_t)result : -1;
}

extern "C" void* relay_create_module(const uint8_t* bytes, size_t size) {
    return new Wasm3Module(bytes, size);
}

extern "C" int relay_add(void* modulePtr, int a, int b) {
    Wasm3Module* module = (Wasm3Module*)modulePtr;
    return module->add(a, b);
}

extern "C" const char* relay_callMethod(void* modulePtr, const char* name) {
    Wasm3Module* module = (Wasm3Module*)modulePtr;
    return module->callMethod(name);
}

extern "C" void relay_destroy_module(void* modulePtr) {
    delete (Wasm3Module*)modulePtr;
}