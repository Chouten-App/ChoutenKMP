#include "relay_host_functions.h"

m3ApiRawFunction(htmlParseFunc) {
    m3ApiReturnType(u32)
    m3ApiGetArgMem(const char*, html);
    m3ApiGetArg(i32, len);

    u32 id = host_html_parse(html, len);

    m3ApiReturn(id)
}

m3ApiRawFunction(querySelectorFunc) {
    m3ApiReturnType(u32)

    m3ApiGetArg(i32, docId);
    m3ApiGetArgMem(const char*, query);
    m3ApiGetArg(i32, len);

    u32 id = host_query_selector(docId, query, len);

    m3ApiReturn(id)
}

m3ApiRawFunction(querySelectorAllFunc)
{
    m3ApiReturnType(u32)

    m3ApiGetArg(i32, docId)
    m3ApiGetArgMem(const char*, query)
    m3ApiGetArg(i32, len)

    uint32_t array_len = 0;
    uint32_t* host_array = host_query_selector_all(docId, query, len, &array_len);

    if (!host_array || array_len == 0)
        m3ApiReturn(0);

    uint32_t wasm_bytes = array_len * sizeof(uint32_t);

    const void* alloc_args[1] = { &array_len };
    M3Result res = m3_Call(alloc_fn, 1, alloc_args);

    if (res != m3Err_none) {
        host_log("Body alloc failed", 17);
        m3ApiReturn(0);
    }

    uint32_t body_ptr = 0;
    const void* alloc_ret[1] = { &body_ptr };
    m3_GetResults(alloc_fn, 1, alloc_ret);

    uint8_t* memory = m3_GetMemory(runtime, nullptr, 0);
    memcpy(memory + body_ptr, host_array, array_len);

    // allocate response struct
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
    struct_data[1] = array_len;

    m3ApiReturn(struct_ptr);
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
