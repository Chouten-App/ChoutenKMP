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
