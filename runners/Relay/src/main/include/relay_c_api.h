#pragma once
#include <stddef.h>
#include <stdint.h>

#ifdef __cplusplus
extern "C" {
#endif

    typedef uint32_t (*HostRequestFn)(const char* url, uint32_t len, uint32_t method);
    typedef const char* (*HostNodeTextFn)(size_t nodeId, uint32_t* out_len);
    typedef const char* (*HostNodeAttrFn)(size_t nodeId, const char* attr, size_t len, uint32_t* out_len);


    // Setup stuff
    void relay_set_logger(void (*logger)(const char*, size_t));
    void relay_set_request_handler(HostRequestFn handler);
    void relay_set_response_body_as_doc_handler(uint32_t (*response_body_as_doc)(uint32_t));
    void relay_set_html_parse_handler(int32_t (*html_parse)(const char*, size_t));
    void relay_set_query_selector_handler(int32_t (*query_selector)(size_t, const char*, size_t));
    void relay_set_query_selector_all_handler(int32_t* (*query_selector_all)(size_t, const char*, size_t, uint32_t* out_len));
    void relay_set_node_query_selector_handler(int32_t (*node_query_selector)(size_t, const char*, size_t));
    void relay_set_node_text_handler(HostNodeTextFn handler);
    void relay_set_node_attr_handler(HostNodeAttrFn handler);

    void* relay_create_module(const uint8_t* bytes, size_t size);
    void relay_destroy_module(void* modulePtr);

    // Host functions

    // Module functions
    int relay_add(void* modulePtr, int a, int b);
    const char* relay_callMethod(void* modulePtr, const char* name);

    #ifdef __cplusplus
}
#endif