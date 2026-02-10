#include "relay_native.hpp"

m3ApiRawFunction(logFunc) {
    m3ApiGetArgMem(const char*, msg);
    m3ApiGetArg(i32, len);
    host_log(msg, len);
    m3ApiSuccess();
}

m3ApiRawFunction(requestFunc) {
    m3ApiGetArgMem(const char*, url);
    m3ApiGetArg(i32, len);
    m3ApiGetArg(i32, method);
    i32 result = host_request(url, len, method);
    _sp[0] = (uint64_t)result;

    return m3Err_none;
}

Wasm3Module::Wasm3Module(const uint8_t* data, size_t size) {
    env = m3_NewEnvironment();
    runtime = m3_NewRuntime(env, 1024*1024*5, nullptr);

    m3_ParseModule(env, &module, data, size);
    m3_LoadModule(runtime, module);
    m3_LinkRawFunction(module, "env", "log_host", "v(ii)", &logFunc);
    m3_LinkRawFunction(module, "env", "request_host", "i(iii)", &requestFunc);
}

Wasm3Module::~Wasm3Module() {
    if (runtime) m3_FreeRuntime(runtime);
    if (env) m3_FreeEnvironment(env);
}

int32_t Wasm3Module::add(int32_t a, int32_t b) {
    if (!env || !runtime || !module) {
        host_log("Wasm3Module not properly initialized", strlen("Wasm3Module not properly initialized"));
        return 0;
    }

    M3Result result = m3_FindFunction(&func, runtime, "add");
    M3Result res = m3_CallV(func, a, b);
    if (res) return 0;

    int32_t value = 0;
    const void* retPtrs[1] = { &value };
    m3_GetResults(func, 1, retPtrs);

    return value;
}

const char* Wasm3Module::callMethod(const char* name) {
    if (!env || !runtime || !module) {
        host_log("Wasm3Module not properly initialized", strlen("Wasm3Module not properly initialized"));
        return 0;
    }

    M3Result result = m3_FindFunction(&func, runtime, name);
    if (result) {
        return 0;
    }
    M3Result res = m3_CallV(func);
    if (res) return 0;

    int32_t string_offset = 0;
    const void* retPtrs[1] = { &string_offset };
    m3_GetResults(func, 1, retPtrs);

    uint8_t* memory = m3_GetMemory(runtime, nullptr, 0);
    return reinterpret_cast<const char*>(memory + string_offset);
}
