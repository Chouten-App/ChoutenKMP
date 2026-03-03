#include "relay_native.hpp"

IM3Function alloc_fn;

m3ApiRawFunction(logFunc) {
    m3ApiGetArgMem(const char*, msg);
    m3ApiGetArg(i32, len);
    host_log(msg, len);
    m3ApiSuccess();
}

struct RelayResponse {
    uint32_t ptr;
    uint32_t len;
};

m3ApiRawFunction(requestFunc)
{
    m3ApiReturnType(u32)

    m3ApiGetArgMem(const char*, url);
    m3ApiGetArg(i32, len);
    m3ApiGetArg(i32, method);

    uint32_t resp_len = 0;
    const char* resp_data = host_request(url, len, method, &resp_len);

    if (!resp_data || resp_len == 0)
    m3ApiReturn(0);

    // --- allocate string in WASM ---
    m3_CallV(alloc_fn, resp_len);

    uint64_t alloc_result = 0;
    m3_GetResultsV(alloc_fn, &alloc_result);

    uint32_t string_offset = (uint32_t)alloc_result;
    uint8_t* wasm_string = (uint8_t*)m3ApiOffsetToPtr(string_offset);

    memcpy(wasm_string, resp_data, resp_len);

    // --- allocate RelayResponse in WASM ---
    m3_CallV(alloc_fn, sizeof(RelayResponse));

    m3_GetResultsV(alloc_fn, &alloc_result);

    uint32_t struct_offset = (uint32_t)alloc_result;
    RelayResponse* resp_struct =
            (RelayResponse*)m3ApiOffsetToPtr(struct_offset);

    resp_struct->ptr = string_offset;
    resp_struct->len = resp_len;

    m3ApiReturn(struct_offset);
}

m3ApiRawFunction(htmlParseFunc) {
    m3ApiReturnType(u32)
    m3ApiGetArgMem(const char*, html);
    m3ApiGetArg(i32, len);

    u32 id = host_html_parse(html, len);

    m3ApiReturn(id)
}

Wasm3Module::Wasm3Module(const uint8_t* data, size_t size) {
    char logBuf[256];
    snprintf(logBuf, sizeof(logBuf), "[Wasm3Module] Constructor called, data size: %zu", size);
    host_log(logBuf, strlen(logBuf));

    env = m3_NewEnvironment();
    host_log("[Wasm3Module] Environment created", strlen("[Wasm3Module] Environment created"));

    runtime = m3_NewRuntime(env, 1024*1024*5, nullptr);
    host_log("[Wasm3Module] Runtime created (5MB stack)", strlen("[Wasm3Module] Runtime created (5MB stack)"));

    M3Result parseResult = m3_ParseModule(env, &module, data, size);
    if (parseResult) {
        snprintf(logBuf, sizeof(logBuf), "[Wasm3Module] ParseModule FAILED: %s", parseResult);
        host_log(logBuf, strlen(logBuf));
    } else {
        host_log("[Wasm3Module] ParseModule succeeded", strlen("[Wasm3Module] ParseModule succeeded"));
    }

    M3Result loadResult = m3_LoadModule(runtime, module);
    if (loadResult) {
        snprintf(logBuf, sizeof(logBuf), "[Wasm3Module] LoadModule FAILED: %s", loadResult);
        host_log(logBuf, strlen(logBuf));
    } else {
        host_log("[Wasm3Module] LoadModule succeeded", strlen("[Wasm3Module] LoadModule succeeded"));
    }

    M3Result alloc_result = m3_FindFunction(&alloc_fn, runtime, "alloc");

    M3Result linkResult = m3_LinkRawFunction(module, "env", "log_host", "v(ii)", &logFunc);
    if (linkResult) {
        snprintf(logBuf, sizeof(logBuf), "[Wasm3Module] LinkRawFunction log_host FAILED: %s", linkResult);
        host_log(logBuf, strlen(logBuf));
    } else {
        host_log("[Wasm3Module] Linked log_host function", strlen("[Wasm3Module] Linked log_host function"));
    }
    M3Result requestLinkResult = m3_LinkRawFunction(module, "env", "request_host", "i(iii)", &requestFunc);
    if (requestLinkResult) {
        snprintf(logBuf, sizeof(logBuf), "[Wasm3Module] LinkRawFunction request_host FAILED: %s", requestLinkResult);
        host_log(logBuf, strlen(logBuf));
    } else {
        host_log("[Wasm3Module] Linked request_host function", strlen("[Wasm3Module] Linked request_host function"));
    }
}

Wasm3Module::~Wasm3Module() {
    host_log("[~Wasm3Module] Destructor called", strlen("[~Wasm3Module] Destructor called"));
    if (runtime) {
        m3_FreeRuntime(runtime);
        host_log("[~Wasm3Module] Runtime freed", strlen("[~Wasm3Module] Runtime freed"));
    }
    if (env) {
        m3_FreeEnvironment(env);
        host_log("[~Wasm3Module] Environment freed", strlen("[~Wasm3Module] Environment freed"));
    }
}

int32_t Wasm3Module::add(int32_t a, int32_t b) {
    char logBuf[256];
    snprintf(logBuf, sizeof(logBuf), "[add] Called with a=%d, b=%d", a, b);
    host_log(logBuf, strlen(logBuf));

    if (!env || !runtime || !module) {
        host_log("[add] ERROR: Wasm3Module not properly initialized", strlen("[add] ERROR: Wasm3Module not properly initialized"));
        return 0;
    }

    M3Result result = m3_FindFunction(&func, runtime, "add");
    if (result) {
        snprintf(logBuf, sizeof(logBuf), "[add] m3_FindFunction FAILED: %s", result);
        host_log(logBuf, strlen(logBuf));
        return 0;
    }
    host_log("[add] Found 'add' function", strlen("[add] Found 'add' function"));

    M3Result res = m3_CallV(func, a, b);
    if (res) {
        snprintf(logBuf, sizeof(logBuf), "[add] m3_CallV FAILED: %s", res);
        host_log(logBuf, strlen(logBuf));
        return 0;
    }
    host_log("[add] m3_CallV succeeded", strlen("[add] m3_CallV succeeded"));

    int32_t value = 0;
    const void* retPtrs[1] = { &value };
    m3_GetResults(func, 1, retPtrs);

    snprintf(logBuf, sizeof(logBuf), "[add] Result: %d", value);
    host_log(logBuf, strlen(logBuf));

    return value;
}

const char* Wasm3Module::callMethod(const char* name) {
    char logBuf[256];
    snprintf(logBuf, sizeof(logBuf), "[callMethod] Called with name='%s'", name);
    host_log(logBuf, strlen(logBuf));

    if (!env || !runtime || !module) {
        host_log("[callMethod] ERROR: Wasm3Module not properly initialized", strlen("[callMethod] ERROR: Wasm3Module not properly initialized"));
        return nullptr;
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
