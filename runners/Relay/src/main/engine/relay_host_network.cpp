#include "relay_host_functions.h"

m3ApiRawFunction(requestFunc)
{
    m3ApiReturnType(u32)
    m3ApiGetArgMem(const char*, url);
    m3ApiGetArg(u32, len);
    m3ApiGetArg(u32, method);

    host_log("before request", 14);
    u32 resp_data = host_request(url, len, method);
    host_log("after request", 13);
    m3ApiReturn(resp_data)
    /*
    struct ResponseGuard {
        const char* data;
        ~ResponseGuard() { if (data) free((void*)data); }
    } guard{resp_data};

    char log_buf[128];
    snprintf(log_buf, sizeof(log_buf), "Response: %u bytes", resp_len);
    host_log(log_buf, strlen(log_buf));

    if (!resp_data || resp_len == 0) {
        host_log("ERROR: No response", 18);
        m3ApiReturn(0);
    }

    auto wasmAlloc = [&](uint32_t size, uint32_t& out_ptr) -> bool {
        const void* args[1] = { &size };
        if (m3_Call(alloc_fn, 1, args) != m3Err_none) return false;
        const void* rets[1] = { &out_ptr };
        m3_GetResults(alloc_fn, 1, rets);
        return out_ptr != 0;
    };

    // Pad to next 64KB page boundary to ensure the allocator grows with a
    // full page of headroom — without this, the allocator grows to the exact
    // minimum and the memcpy lands within 3 bytes of the end of linear memory,
    // stomping wasm3's internal metadata in the host heap.
    uint32_t padded_len = (resp_len + 65535u) & ~65535u;

    // Allocate both regions before writing anything
    uint32_t body_ptr = 0;
    if (!wasmAlloc(padded_len, body_ptr)) {
        host_log("Body alloc failed", 17);
        m3ApiReturn(0);
    }

    uint32_t struct_ptr = 0;
    if (!wasmAlloc(8u, struct_ptr)) {
        host_log("Struct alloc failed", 19);
        m3ApiReturn(0);
    }

    // Sanity checks
    uint32_t mem_size = 0;
    uint8_t* memory = m3_GetMemory(runtime, &mem_size, 0);

    snprintf(log_buf, sizeof(log_buf),
            "Alloc: body=%u struct=%u mem=%u need=%llu",
            body_ptr, struct_ptr, mem_size,
            (unsigned long long)body_ptr + resp_len);
    host_log(log_buf, strlen(log_buf));

    if (!memory) {
        host_log("FATAL: null memory base", 23);
        m3ApiReturn(0);
    }
    if ((uint64_t)body_ptr + resp_len > mem_size) {
        host_log("FATAL: body exceeds memory bounds", 33);
        m3ApiReturn(0);
    }
    if ((uint64_t)struct_ptr + 8u > mem_size) {
        host_log("FATAL: struct exceeds memory bounds", 35);
        m3ApiReturn(0);
    }

    // Check body/struct don't overlap
    bool body_before_struct = body_ptr + resp_len <= struct_ptr;
    bool struct_before_body = struct_ptr + 8u <= body_ptr;
    if (!body_before_struct && !struct_before_body) {
        host_log("FATAL: body/struct overlap", 26);
        m3ApiReturn(0);
    }

    // Write — single m3_GetMemory fetch, no more m3_Calls after this point
    memcpy(memory + body_ptr, resp_data, resp_len);

    uint32_t* s = reinterpret_cast<uint32_t*>(memory + struct_ptr);
    s[0] = body_ptr;
    s[1] = resp_len;

    snprintf(log_buf, sizeof(log_buf),
            "Struct at %u: ptr=%u len=%u", struct_ptr, s[0], s[1]);
    host_log(log_buf, strlen(log_buf));

    m3ApiReturn(struct_ptr);
     */
}