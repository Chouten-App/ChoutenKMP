#import <Foundation/Foundation.h>
#import "relay_native.hpp"

static void (*gHostLogger)(const char*, size_t) = nullptr;
static uint32_t (*gHostHtmlParse)(const char*, size_t) = nullptr;
static uint32_t (*gHostQuerySelector)(size_t, const char*, size_t) = nullptr;

typedef const char* (*HostNodeTextFn)(size_t nodeId, uint32_t* out_len);
static HostNodeTextFn gHostNodeText = nullptr;

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
extern "C" void relay_set_node_text_handler(HostNodeTextFn handler) {
    gHostNodeText = handler;
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


const char* host_node_text(
        size_t nodeId,
        uint32_t *out_len)
{
    if (!gHostNodeText) {
        host_log("[host_request] No handler set", 30);
        *out_len = 0;
        return "";
    }

    return gHostNodeText(nodeId, out_len);
}

extern "C" void* relay_create_module(const uint8_t* bytes, size_t size) {
    return new Wasm3Module(bytes, size);
}

extern "C" int relay_add(void* modulePtr, int a, int b) {
    Wasm3Module* module = (Wasm3Module*)modulePtr;
    return module->add(a, b);
}

extern "C" const char* relay_callMethod(void* modulePtr, const char* name) {
    Wasm3Module* module = (Wasm3Module*)modulePtr;
    return module->callMethod(name);
}

extern "C" void relay_destroy_module(void* modulePtr) {
    delete (Wasm3Module*)modulePtr;
}