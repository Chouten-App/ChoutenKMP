#pragma once
#include "wasm3.h"
#include "m3_env.h"

struct RelayResponse {
    uint32_t ptr;
    uint32_t len;
};

IM3Function alloc_fn;
IM3Function grow_memory_fn;
IM3Function store_response_fn;

void host_log(const char* msg, size_t len);
const char* host_request(const char *url, size_t len, int32_t method, uint32_t *pInt);
u32 host_html_parse(const char* html, size_t len);
u32 host_query_selector(size_t docId, const char* query, size_t len);
const char* host_node_text(size_t nodeId, uint32_t *pInt);

m3ApiRawFunction(logFunc);
m3ApiRawFunction(requestFunc);
m3ApiRawFunction(htmlParseFunc);
m3ApiRawFunction(querySelectorFunc);
m3ApiRawFunction(nodeTextFunc);