#include "../../engine/relay_native.hpp"
#include <string>
#include <vector>
#include <jni.h>

#define JNI_VERSION JNI_VERSION_1_6

JavaVM* gJvm;
jobject gLoggerObj;
jobject gNativeBridgeObj;

#include <cstdio>
#include <cstring>

JNIEXPORT jint JNICALL
JNI_OnLoad(JavaVM* vm, void*) {
    gJvm = vm;
    return JNI_VERSION;
}

class JNIHelper {
public:
    static JNIEnv* getEnv() {
        if (!gJvm) return nullptr;

        JNIEnv* env = nullptr;
        if (gJvm->GetEnv(reinterpret_cast<void**>(&env), JNI_VERSION_1_6) != JNI_OK) {
            return nullptr;
        }
        return env;
    }

    // RAII wrapper for automatic cleanup
    class LocalRefGuard {
        JNIEnv* env_;
        jobject ref_;
    public:
        LocalRefGuard(JNIEnv* env, jobject ref) : env_(env), ref_(ref) {}
        ~LocalRefGuard() {
            if (env_ && ref_) env_->DeleteLocalRef(ref_);
        }
        LocalRefGuard(const LocalRefGuard&) = delete;
        LocalRefGuard& operator=(const LocalRefGuard&) = delete;
    };

    struct MethodCache {
        jmethodID requestMethod = nullptr;
        jmethodID htmlParseMethod = nullptr;
        jmethodID htmlQuerySelectorMethod = nullptr;
        jmethodID htmlQuerySelectorAllMethod = nullptr;

        bool init(JNIEnv* env, jobject bridgeObj) {
            if (!env || !bridgeObj) return false;

            jclass cls = env->GetObjectClass(bridgeObj);
            if (!cls) return false;

            requestMethod = env->GetMethodID(
                cls,
                "request",
                "(Ljava/lang/String;I)Ljava/lang/String;"
            );

            htmlParseMethod = env->GetMethodID(
                cls,
                "html_parse",
                "(Ljava/lang/String;)I"
            );
            htmlQuerySelectorMethod = env->GetMethodID(
                cls,
                "html_query_selector",
                "(ILjava/lang/String;)I"
            );
            htmlQuerySelectorAllMethod = env->GetMethodID(
                cls,
                "html_query_selector_all",
                "(ILjava/lang/String;)Ljava/util/List;"
            );

            env->DeleteLocalRef(cls);
            return requestMethod != nullptr && htmlParseMethod != nullptr;
        }
    };

    static MethodCache methodCache;
};

JNIHelper::MethodCache JNIHelper::methodCache;

// Call this during initialization
bool initializeJNICache(JNIEnv* env, jobject bridgeObj) {
    return JNIHelper::methodCache.init(env, bridgeObj);
}


void host_log(const char* msg, size_t len) {
    if (!gJvm || !gLoggerObj) return;

    JNIEnv* env = nullptr;
    if (gJvm->GetEnv(reinterpret_cast<void**>(&env), JNI_VERSION) != JNI_OK) {
        return;
    }

    jstring jmsg = env->NewStringUTF(std::string(msg, len).c_str());
    jclass cls = env->GetObjectClass(gLoggerObj);
    jmethodID logId = env->GetMethodID(cls, "log", "(Ljava/lang/String;)V");

    env->CallVoidMethod(gLoggerObj, logId, jmsg);
    env->DeleteLocalRef(jmsg);
}

const char* host_request(const char *url, size_t len, int32_t method, uint32_t *pInt) {
    JNIEnv* env = JNIHelper::getEnv();
    if (!env || !gNativeBridgeObj) {
        return "";
    }

    jstring jurl = env->NewStringUTF(std::string(url).c_str());
    if (!jurl) {
        return "";
    }
    JNIHelper::LocalRefGuard urlGuard(env, jurl);

    jstring jresult = (jstring) env->CallObjectMethod(
            gNativeBridgeObj,
            JNIHelper::methodCache.requestMethod,
            jurl,
            (jint)method
        );
    JNIHelper::LocalRefGuard resultGuard(env, jresult);

    jsize jlen = env->GetStringUTFLength(jresult);
    if (pInt) {
        *pInt = (uint32_t)jlen;
    }

    const char* utf = env->GetStringUTFChars(jresult, nullptr);

    return utf;
}

u32 host_html_parse(const char* html, size_t len) {
    JNIEnv* env = JNIHelper::getEnv();
    if (!env || !gNativeBridgeObj) {
        return 0;
    }

    jstring jhtml = env->NewStringUTF(std::string(html).c_str());
    if (!jhtml) {
        return 0;
    }
    JNIHelper::LocalRefGuard urlGuard(env, jhtml);

    jint jresult = (
        env->CallIntMethod(
            gNativeBridgeObj,
            JNIHelper::methodCache.htmlParseMethod,
            jhtml
        )
    );

    if (!jresult) {
        logFormatted("[htmlParseFunc] Parse failed.");
        return 0;
    }

    return 0;
}

u32 host_query_selector(size_t docId, const char* query, size_t len) {
    JNIEnv* env = JNIHelper::getEnv();
    if (!env || !gNativeBridgeObj) {
        return 0;
    }

    jstring jquery = env->NewStringUTF(std::string(html).c_str());
    if (!jquery) {
        return 0;
    }
    JNIHelper::LocalRefGuard urlGuard(env, jquery);

    jint jresult = (
        env->CallIntMethod(
            gNativeBridgeObj,
            JNIHelper::methodCache.htmlQuerySelectorMethod,
            (jint)docId,
            jquery
        )
    );

    if (!jresult) {
        logFormatted("[htmlQuerySelectorFunc] Parse failed.");
        return 0;
    }

    return 0;
}

uint32_t* host_query_selector_all(size_t docId, const char* query, size_t len, uint32_t *pInt) {

    riteturn nullptr;
}

uint32_t host_node_query_selector(size_t docId, const char* html, size_t len) {
    return 0;
}

const char* host_node_text(size_t nodeId, uint32_t *pInt) {
    return "";
}

const char* host_node_attr(size_t nodeId, const char* attr, size_t lens, uint32_t *pInt) {
    return "";
}

// Keep a static module instance for now
static Wasm3Module* wasmModule = nullptr;

extern "C" {
    JNIEXPORT void JNICALL
    Java_dev_chouten_runners_relay_NativeBridge_initLogger(JNIEnv* env, jobject thiz, jobject logger) {
        env->GetJavaVM(&gJvm);
        gLoggerObj = env->NewGlobalRef(logger);
    }

    JNIEXPORT void JNICALL
    Java_dev_chouten_runners_relay_NativeBridge_initNativeBridge(JNIEnv* env, jobject thiz, jobject nativeBridge) {
        env->GetJavaVM(&gJvm);
        gNativeBridgeObj = env->NewGlobalRef(nativeBridge);
        initializeJNICache(env, nativeBridge);
    }

    // Load WASM module from byte array
    JNIEXPORT void JNICALL
    Java_dev_chouten_runners_relay_NativeBridge_nativeLoadWasm(JNIEnv* env, jobject thiz, jbyteArray wasmBytes) {
        gNativeBridgeObj = env->NewGlobalRef(thiz);

        jsize len = env->GetArrayLength(wasmBytes);
        jbyte* data = env->GetByteArrayElements(wasmBytes, nullptr);

        if (wasmModule) delete wasmModule;
        wasmModule = new Wasm3Module((uint8_t*)data, len);

        env->ReleaseByteArrayElements(wasmBytes, data, JNI_ABORT);
    }

    // Call add function
    JNIEXPORT jint JNICALL
    Java_dev_chouten_runners_relay_NativeBridge_add(JNIEnv*, jobject, jint a, jint b) {
        return 0;
    }

    JNIEXPORT jstring JNICALL
    Java_dev_chouten_runners_relay_NativeBridge_callMethod(JNIEnv* env, jobject, jstring jname) {
        if (!wasmModule) return 0;
        const char* name = env->GetStringUTFChars(jname, nullptr);
        const char* ret = wasmModule->callMethod(name);

        return env->NewStringUTF(ret);
    }
}
