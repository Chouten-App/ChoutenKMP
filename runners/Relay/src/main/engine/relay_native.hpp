#pragma once
#include "m3_env.h"
#include "wasm3.h"
#include <cstdint>

int32_t relay_add(int32_t a, int32_t b);

void host_log(const char* msg, size_t len);
int32_t host_request(const char* url, size_t len, int32_t method);

m3ApiRawFunction(logFunc);

struct Wasm3Module {
    IM3Environment env;
    IM3Runtime runtime;
    IM3Module module;
    IM3Function func;

    Wasm3Module(const uint8_t* data, size_t size);
    ~Wasm3Module();

    int32_t add(int32_t a, int32_t b);
    const char* callMethod(const char* name);
};