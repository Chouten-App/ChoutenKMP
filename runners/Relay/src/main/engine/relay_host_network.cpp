#include "relay_host_functions.h"

m3ApiRawFunction(requestFunc)
{
    m3ApiReturnType(u32)  // Return pointer to struct
    m3ApiGetArgMem(const char*, url);
    m3ApiGetArg(i32, len);
    m3ApiGetArg(i32, method);

    // Get response from host
    uint32_t resp_len = 0;
    const char* resp_data = host_request(url, len, method, &resp_len);

    struct ResponseGuard {
        const char* data;
        ~ResponseGuard() { if (data) free((void*)data); }
    } guard{resp_data};

    if (!resp_data || resp_len == 0) {
        host_log("ERROR: No response", 18);
        m3ApiReturn(0);
    }

    char log_buf[128];
    snprintf(log_buf, sizeof(log_buf), "Response: %u bytes", resp_len);
    host_log(log_buf, strlen(log_buf));

    // Allocate body in WASM
    const void* alloc_args[1] = { &resp_len };
    M3Result res = m3_Call(alloc_fn, 1, alloc_args);

    if (res != m3Err_none) {
        host_log("Body alloc failed", 17);
        m3ApiReturn(0);
    }

    uint32_t body_ptr = 0;
    const void* alloc_ret[1] = { &body_ptr };
    m3_GetResults(alloc_fn, 1, alloc_ret);

    if (body_ptr == 0) {
        host_log("Null body pointer", 17);
        m3ApiReturn(0);
    }

    // Copy data to WASM memory
    uint8_t* memory = m3_GetMemory(runtime, nullptr, 0);
    memcpy(memory + body_ptr, resp_data, resp_len);

    snprintf(log_buf, sizeof(log_buf), "Copied to ptr=%u", body_ptr);
    host_log(log_buf, strlen(log_buf));

    // Allocate struct (8 bytes for two u32s)
    uint32_t struct_size = 8;
    const void* struct_args[1] = { &struct_size };
    res = m3_Call(alloc_fn, 1, struct_args);

    if (res != m3Err_none) {
        host_log("Struct alloc failed", 19);
        m3ApiReturn(0);
    }

    uint32_t struct_ptr = 0;
    const void* struct_ret[1] = { &struct_ptr };
    m3_GetResults(alloc_fn, 1, struct_ret);

    if (struct_ptr == 0) {
        host_log("Null struct pointer", 19);
        m3ApiReturn(0);
    }

    // Write struct: [body_ptr, body_len]
    memory = m3_GetMemory(runtime, nullptr, 0);
    uint32_t* struct_data = (uint32_t*)(memory + struct_ptr);
    struct_data[0] = body_ptr;
    struct_data[1] = resp_len;

    snprintf(log_buf, sizeof(log_buf), "Struct at %u: ptr=%u len=%u",
    struct_ptr, struct_data[0], struct_data[1]);
    host_log(log_buf, strlen(log_buf));

    // Return pointer to struct
    m3ApiReturn(struct_ptr);
}