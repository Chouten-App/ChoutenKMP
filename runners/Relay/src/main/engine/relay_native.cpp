#include "relay_native.hpp"
#include "relay_host_functions.h"

IM3Function alloc_fn;
IM3Function grow_memory_fn;
IM3Function store_response_fn;
IM3Function discover_fn;

void logWasm3Result(M3Result result, const char* action) {
    char logBuf[256];
    if (result) {
        snprintf(logBuf, sizeof(logBuf), "[Wasm3Module] %s FAILED: %s", action, result);
    } else {
        snprintf(logBuf, sizeof(logBuf), "[Wasm3Module] %s succeeded", action);
    }
    host_log(logBuf, strlen(logBuf));
}

void logFormatted(const char* format, ...) {
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

    runtime = m3_NewRuntime(env, 1024*1024*64, nullptr);
    logWasm3Result(nullptr, "Runtime created (64MB stack)");

    logWasm3Result(m3_ParseModule(env, &module, data, size), "ParseModule");
    logWasm3Result(m3_LoadModule(runtime, module), "LoadModule");


    /*
    if (m3_FindFunction(&alloc_fn, runtime, "alloc")) {
        logFormatted("Warning: alloc function not found");
    }

    if (m3_FindFunction(&grow_memory_fn, runtime, "grow_memory")) {
        logFormatted("Warning: grow_memory function not found");
    }
     */

    initHostFunctions();
    initialize();
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
            {"response_get_body_as_doc_host", "i(i)", &responseGetBodyAsDoc},
            {"html_parse_host", "i(ii)", &htmlParseFunc},
            {"html_query_selector_host", "i(iii)", &querySelectorFunc},
            {"html_query_selector_all_host", "i(iiii)", &querySelectorAllFunc},
            {"html_node_text_host", "i(ii)", &nodeTextFunc},
            {"html_node_attr_host", "i(iiii)", &nodeAttrFunc},
            {"html_node_query_selector_host", "i(iii)", &nodeQuerySelectorFunc},
            {"html_node_query_selector_all_host", "i(iiii)", &nodeQuerySelectorAllFunc}
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

M3Result Wasm3Module::initialize() {
    M3Result result;

    result = m3_FindFunction(&store_response_fn, runtime, "store_response");
    if (result) host_log("store_response not found", 24);

    result = m3_FindFunction(&alloc_fn, runtime, "alloc");
    if (result) host_log("alloc not found", 15);

    // pre-find discover_impl and any other entry points you call
    result = m3_FindFunction(&discover_fn, runtime, "discover_impl");
    if (result) host_log("discover_impl not found", 23);

    return m3Err_none;
}

const char* Wasm3Module::callMethod(const char* name) {
    if (!env || !runtime || !module) {
        host_log("[callMethod] ERROR: Wasm3Module not properly initialized", strlen("[callMethod] ERROR: Wasm3Module not properly initialized"));
        return nullptr;
    }

    uint32_t mem_size = 0;
    m3_GetMemory(runtime, &mem_size, 0);
    char buf[64];
    snprintf(buf, sizeof(buf), "Linear memory size: %u bytes", mem_size);
    host_log(buf, strlen(buf));


    char logBuf[256];
    snprintf(logBuf, sizeof(logBuf), "[callMethod] Called with name='%s'", name);
    host_log(logBuf, strlen(logBuf));


    func = nullptr;
    if (strcmp(name, "discover_impl") == 0) func = discover_fn;


    if (!func) {
        // fall back to lookup only if not pre-cached
        M3Result result = m3_FindFunction(&func, runtime, name);
        if (result) {
            snprintf(logBuf, sizeof(logBuf), "[callMethod] m3_FindFunction FAILED: %s", result);
            host_log(logBuf, strlen(logBuf));
            return nullptr;
        }
    }

    if (!discover_fn) {
        host_log("[callMethod] ERROR: discover_fn is null", 39);
        return nullptr;
    }
    snprintf(logBuf, sizeof(logBuf), "[callMethod] calling func=%p", (void*)discover_fn);
    host_log(logBuf, strlen(logBuf));

    M3Result res = m3_CallV(func);

    snprintf(logBuf, sizeof(logBuf), "[callMethod] m3_CallV result=%s", res ? res : "null/success");
    host_log(logBuf, strlen(logBuf));

    if (res) {
        M3ErrorInfo info;
        m3_GetErrorInfo(runtime, &info);
        snprintf(logBuf, sizeof(logBuf), "ErrorInfo: msg='%s' file='%s' line=%d",
                info.message ? info.message : "null",
                info.file ? info.file : "null",
                info.line);
        host_log(logBuf, strlen(logBuf));

        // Also check if it's a known error string
        snprintf(logBuf, sizeof(logBuf), "res ptr=%p val='%.50s'", res, res);
        host_log(logBuf, strlen(logBuf));
        return nullptr;
    }
    host_log("[callMethod] m3_CallV succeeded", 32);

    uint32_t struct_offset = 0;
    const void* retPtrs[1] = { &struct_offset };
    m3_GetResults(func, 1, retPtrs);

    snprintf(logBuf, sizeof(logBuf), "[callMethod] struct_offset=%d", struct_offset);
    host_log(logBuf, strlen(logBuf));

    if (struct_offset == 0) {
        host_log("[callMethod] ERROR: null struct pointer", 38);
        return nullptr;
    }

    uint8_t* memory = m3_GetMemory(runtime, nullptr, 0);
    if (!memory) {
        host_log("[callMethod] ERROR: null memory", 30);
        return nullptr;
    }

    uint32_t* result_struct = reinterpret_cast<uint32_t*>(memory + struct_offset);
    uint32_t json_ptr = result_struct[0];
    uint32_t json_len = result_struct[1];

    snprintf(logBuf, sizeof(logBuf), "[callMethod] json_ptr=%u json_len=%u", json_ptr, json_len);
    host_log(logBuf, strlen(logBuf));

    if (json_ptr == 0 || json_len == 0) {
        host_log("[callMethod] ERROR: invalid json ptr/len", 39);
        return nullptr;
    }

    char* result = static_cast<char*>(malloc(json_len + 1));
    memcpy(result, memory + json_ptr, json_len);
    result[json_len] = '\0';

    IM3Function free_fn = nullptr;
    if (m3_FindFunction(&free_fn, runtime, "chouten_free_result") == m3Err_none) {
        const void* free_args[2] = { &struct_offset, &json_len };
        m3_Call(free_fn, 2, free_args);
    }

    return result;
}
