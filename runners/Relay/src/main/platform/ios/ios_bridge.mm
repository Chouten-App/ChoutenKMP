#import <Foundation/Foundation.h>
#import "relay_native.hpp"

static void (*gHostLogger)(const char*, size_t) = nullptr;
static uint32_t (*gHostHtmlParse)(const char*, size_t) = nullptr;
static uint32_t (*gHostQuerySelector)(size_t, const char*, size_t) = nullptr;
static uint32_t (*gHostNodeQuerySelector)(size_t, const char*, size_t) = nullptr;
static uint32_t* (*gHostQuerySelectorAll)(size_t, const char*, size_t, uint32_t*) = nullptr;

typedef const char* (*HostNodeTextFn)(size_t nodeId, uint32_t* out_len);
static HostNodeTextFn gHostNodeText = nullptr;

typedef const char* (*HostNodeAttrFn)(size_t nodeId, const char* attr, size_t len, uint32_t* out_len);
static HostNodeAttrFn gHostNodeAttr = nullptr;

typedef const char* (*HostRequestFn)(const char* url, size_t len, int32_t method, uint32_t* out_len);
static HostRequestFn gHostRequest = nullptr;

extern "C" void relay_set_request_handler(HostRequestFn handler) {
    gHostRequest = handler;
}

// Called from Swift to register logger
extern "C" void relay_set_logger(void (*logger)(const char*, size_t)) {
    gHostLogger = logger;
}

extern "C" void relay_set_html_parse_handler(uint32_t (*html_parse)(const char*, size_t)) {
    gHostHtmlParse = html_parse;
}

extern "C" void relay_set_query_selector_handler(uint32_t (*query_selector)(size_t, const char*, size_t)) {
    gHostQuerySelector = query_selector;
}

extern "C" void relay_set_query_selector_all_handler(uint32_t* (*query_selector_all)(size_t, const char*, size_t, uint32_t* out_len)) {
    gHostQuerySelectorAll = query_selector_all;
}


extern "C" void relay_set_node_query_selector_handler(uint32_t (*node_query_selector)(size_t, const char*, size_t)) {
    gHostNodeQuerySelector = node_query_selector;
}

extern "C" void relay_set_node_text_handler(HostNodeTextFn handler) {
    gHostNodeText = handler;
}

extern "C" void relay_set_node_attr_handler(HostNodeAttrFn handler) {
    gHostNodeAttr = handler;
}

// This is the function Wasm3 calls
void host_log(const char* msg, size_t len) {
    if (gHostLogger) {
        gHostLogger(msg, len);
    }
}



// Host request function - stub for now
const char* host_request(const char *url,
        size_t len,
        int32_t method,
        uint32_t *out_len)
{
    if (!gHostRequest) {
        host_log("[host_request] No handler set", 30);
        *out_len = 0;
        return "";
    }

    return gHostRequest(url, len, method, out_len);
}


uint32_t host_html_parse(const char* html, size_t len) {
    if (gHostHtmlParse) {
        return gHostHtmlParse(html, len);
    }

    return 0;
}


uint32_t host_query_selector(size_t docId, const char* html, size_t len) {
    if (gHostQuerySelector) {
        return gHostQuerySelector(docId, html, len);
    }

    return 0;
}


uint32_t* host_query_selector_all(size_t docId, const char* query, size_t len, uint32_t *pInt) {
    if (gHostQuerySelectorAll) {
        return gHostQuerySelectorAll(docId, query, len, pInt);
    }

    return nullptr;
}


uint32_t host_node_query_selector(size_t docId, const char* html, size_t len) {
    if (gHostNodeQuerySelector) {
        return gHostNodeQuerySelector(docId, html, len);
    }

    return 0;
}

const char* host_node_text(
        size_t nodeId,
        uint32_t *out_len)
{
    if (!gHostNodeText) {
        host_log("[host_node_text] No handler set", 30);
        *out_len = 0;
        return "";
    }

    return gHostNodeText(nodeId, out_len);
}


const char* host_node_attr(
        size_t nodeId,
        const char* attr,
        size_t len,
        uint32_t *out_len)
{
    if (!gHostNodeAttr) {
        host_log("[host_node_attr] No handler set", 30);
        *out_len = 0;
        return "";
    }

    return gHostNodeAttr(nodeId, attr, len, out_len);
}

extern "C" void* relay_create_module(const uint8_t* bytes, size_t size) {
    return new Wasm3Module(bytes, size);
}

extern "C" const char* relay_callMethod(void* modulePtr, const char* name) {
    Wasm3Module* module = (Wasm3Module*)modulePtr;
    return module->callMethod(name);
}

extern "C" void relay_destroy_module(void* modulePtr) {
    delete (Wasm3Module*)modulePtr;
}