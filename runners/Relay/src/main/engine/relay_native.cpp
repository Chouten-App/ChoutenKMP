#include "relay_native.hpp"
#include "relay_host_functions.h"

inline void logWasm3Result(M3Result result, const char* action) {
    char logBuf[256];
    if (result) {
        snprintf(logBuf, sizeof(logBuf), "[Wasm3Module] %s FAILED: %s", action, result);
    } else {
        snprintf(logBuf, sizeof(logBuf), "[Wasm3Module] %s succeeded", action);
    }
    host_log(logBuf, strlen(logBuf));
}

inline void logFormatted(const char* format, ...) {
    char logBuf[256];
    va_list args;
    va_start(args, format);
    vsnprintf(logBuf, sizeof(logBuf), format, args);
    va_end(args);
    host_log(logBuf, strlen(logBuf));
}

Wasm3Module::Wasm3Module(const uint8_t* data, size_t size) {
    logFormatted("[Wasm3Module] Constructor called, data size: %zu", size);

    env = m3_NewEnvironment();
    logWasm3Result(nullptr, "Environment created");

    runtime = m3_NewRuntime(env, 1024*1024*8, nullptr);
    logWasm3Result(nullptr, "Runtime created (64MB stack)");

    logWasm3Result(m3_ParseModule(env, &module, data, size), "ParseModule");
    logWasm3Result(m3_LoadModule(runtime, module), "LoadModule");

    if (m3_FindFunction(&alloc_fn, runtime, "alloc")) {
        logFormatted("Warning: alloc function not found");
    }

    if (m3_FindFunction(&grow_memory_fn, runtime, "grow_memory")) {
        logFormatted("Warning: grow_memory function not found");
    }

    initHostFunctions();
}

Wasm3Module::~Wasm3Module() {
    host_log("[~Wasm3Module] Destructor called", strlen("[~Wasm3Module] Destructor called"));

    alloc_fn = nullptr;
    grow_memory_fn = nullptr;

    if (runtime) {
        m3_FreeRuntime(runtime);
        runtime = nullptr;
        host_log("[~Wasm3Module] Runtime freed", strlen("[~Wasm3Module] Runtime freed"));
    }

    module = nullptr;

    if (env) {
        m3_FreeEnvironment(env);
        env = nullptr;
        host_log("[~Wasm3Module] Environment freed", strlen("[~Wasm3Module] Environment freed"));
    }
}

void Wasm3Module::initHostFunctions() const {
    struct HostFunction {
        const char* name;
        const char* signature;
        M3RawCall function;
    };

    const HostFunction hostFunctions[] = {
            {"log_host", "v(ii)", &logFunc},
            {"request_host", "i(iii)", &requestFunc},
            {"html_parse_host", "i(ii)", &htmlParseFunc},
            {"html_query_selector_host", "i(iii)", &querySelectorFunc},
            {"html_node_text_host", "i(ii)", &nodeTextFunc}
    };

    char logBuf[256];

    for (const auto& fn : hostFunctions) {
        M3Result linkResult = m3_LinkRawFunction(module, "env", fn.name, fn.signature, fn.function);
        if (linkResult) {
            snprintf(logBuf, sizeof(logBuf), "[Wasm3Module] LinkRawFunction %s FAILED: %s", fn.name, linkResult);
        } else {
            snprintf(logBuf, sizeof(logBuf), "[Wasm3Module] Linked %s function", fn.name);
        }
        host_log(logBuf, strlen(logBuf));
    }
}

const char* Wasm3Module::callMethod(const char* name) {
    char logBuf[256];
    snprintf(logBuf, sizeof(logBuf), "[callMethod] Called with name='%s'", name);
    host_log(logBuf, strlen(logBuf));

    if (!env || !runtime || !module) {
        host_log("[callMethod] ERROR: Wasm3Module not properly initialized", strlen("[callMethod] ERROR: Wasm3Module not properly initialized"));
        return nullptr;
    }

    M3Result store_response_result = m3_FindFunction(&store_response_fn, runtime, "store_response");
    if (store_response_result) {
        host_log("Store fn not found", strlen("Store fn not found"));
    }
    M3Result result = m3_FindFunction(&func, runtime, name);
    if (result) {
        snprintf(logBuf, sizeof(logBuf), "[callMethod] m3_FindFunction FAILED: %s", result);
        host_log(logBuf, strlen(logBuf));
        return nullptr;
    }
    host_log("[callMethod] Found function", strlen("[callMethod] Found function"));

    M3Result res = m3_CallV(func);
    if (res) {
        snprintf(logBuf, sizeof(logBuf), "[callMethod] m3_CallV FAILED: %s", res);
        host_log(logBuf, strlen(logBuf));
        return nullptr;
    }
    host_log("[callMethod] m3_CallV succeeded", strlen("[callMethod] m3_CallV succeeded"));

    int32_t string_offset = 0;
    const void* retPtrs[1] = { &string_offset };
    m3_GetResults(func, 1, retPtrs);

    snprintf(logBuf, sizeof(logBuf), "[callMethod] string_offset=%d", string_offset);
    host_log(logBuf, strlen(logBuf));

    uint8_t* memory = m3_GetMemory(runtime, nullptr, 0);
    if (!memory) {
        host_log("[callMethod] ERROR: m3_GetMemory returned null", strlen("[callMethod] ERROR: m3_GetMemory returned null"));
        return nullptr;
    }

    const char* resultStr = reinterpret_cast<const char*>(memory + string_offset);
    snprintf(logBuf, sizeof(logBuf), "[callMethod] Returning string at offset %d", string_offset);
    host_log(logBuf, strlen(logBuf));

    return resultStr;
}
