#include "relay_host_functions.h"

m3ApiRawFunction(responseGetBodyAsDoc) {
    m3ApiReturnType(u32)
    m3ApiGetArg(u32, docId);

    u32 id = host_response_get_body_as_doc(docId);

    m3ApiReturn(id)
}


m3ApiRawFunction(htmlParseFunc) {
    m3ApiReturnType(i32)
    m3ApiGetArgMem(const char*, html);
    m3ApiGetArg(i32, len);

    i32 id = host_html_parse(html, len);

    m3ApiReturn(id)
}

m3ApiRawFunction(querySelectorFunc) {

    host_log("Sel", 3);
    m3ApiReturnType(u32)

    m3ApiGetArg(i32, docId);
    m3ApiGetArgMem(const char*, query);
    m3ApiGetArg(i32, len);

    host_log("Sel", 3);

    u32 id = host_query_selector(docId, query, len);

    m3ApiReturn(id)
}

m3ApiRawFunction(querySelectorAllFunc)
{
    m3ApiReturnType(u32)
    m3ApiGetArg(i32, docId)
    m3ApiGetArgMem(const char*, query)
    m3ApiGetArg(i32, len)

    m3ApiReturn(0)

    /*

    uint32_t array_len = 0;
    int32_t* host_array = host_query_selector_all(docId, query, len, &array_len);
    if (!host_array || array_len == 0)
    m3ApiReturn(0);

    // Byte count for the array, and padded to next 64KB boundary
    uint32_t wasm_bytes = array_len * sizeof(uint32_t);
    uint32_t padded_bytes = (wasm_bytes + 65535u) & ~65535u;

    // Allocate array — pass PADDED BYTE COUNT, not element count
    uint32_t body_ptr = 0;
    {
        const void* args[1] = { &padded_bytes };
        if (m3_Call(alloc_fn, 1, args) != m3Err_none) {
            host_log("Array alloc failed", 18);
            m3ApiReturn(0);
        }
        const void* rets[1] = { &body_ptr };
        m3_GetResults(alloc_fn, 1, rets);
    }
    if (body_ptr == 0) {
        host_log("Null array pointer", 18);
        m3ApiReturn(0);
    }

    // Allocate struct
    uint32_t struct_ptr = 0;
    {
        uint32_t struct_size = 8;
        const void* args[1] = { &struct_size };
        if (m3_Call(alloc_fn, 1, args) != m3Err_none) {
            host_log("Struct alloc failed", 19);
            m3ApiReturn(0);
        }
        const void* rets[1] = { &struct_ptr };
        m3_GetResults(alloc_fn, 1, rets);
    }
    if (struct_ptr == 0) {
        host_log("Null struct pointer", 19);
        m3ApiReturn(0);
    }

    // Single m3_GetMemory fetch after ALL allocations
    uint32_t mem_size = 0;
    uint8_t* memory = m3_GetMemory(runtime, &mem_size, 0);

    if (!memory || (uint64_t)body_ptr + wasm_bytes > mem_size) {
        host_log("FATAL: array exceeds memory bounds", 34);
        m3ApiReturn(0);
    }

    // memcpy uses ACTUAL byte count (wasm_bytes), not element count, not padded
    memcpy(memory + body_ptr, host_array, wasm_bytes);

    uint32_t* s = reinterpret_cast<uint32_t*>(memory + struct_ptr);
    s[0] = body_ptr;
    s[1] = array_len; // element count for WASM to iterate with

    m3ApiReturn(struct_ptr);
     */
}


m3ApiRawFunction(nodeTextFunc)
{
    m3ApiReturnType(u32)

    m3ApiGetArg(i32, nodeId);

    uint32_t resp_len = 0;
    const char* resp_data = host_node_text(nodeId, &resp_len);

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

m3ApiRawFunction(nodeAttrFunc) {
    m3ApiReturnType(u32)

    m3ApiGetArg(i32, nodeId);
    m3ApiGetArgMem(const char*, attr);
    m3ApiGetArg(i32, len);

    uint32_t resp_len = 0;
    const char* resp_data = host_node_attr(nodeId, attr, len, &resp_len);

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

m3ApiRawFunction(nodeQuerySelectorFunc) {
    m3ApiReturnType(u32)

    m3ApiGetArg(i32, docId);
    m3ApiGetArgMem(const char*, query);
    m3ApiGetArg(i32, len);

    u32 id = host_node_query_selector(docId, query, len);

    m3ApiReturn(id)
}

m3ApiRawFunction(nodeQuerySelectorAllFunc) {
    m3ApiReturnType(u32)
    m3ApiReturn(0)
}
