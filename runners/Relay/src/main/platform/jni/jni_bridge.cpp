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

int32_t host_request(const char* url, size_t len, int32_t method) {
    host_log("[requestFunc] Calling host request func", strlen("[requestFunc] Calling host request func"));
    if (!gJvm || !gNativeBridgeObj) return -1;

    JNIEnv* env = nullptr;
    if (gJvm->GetEnv(reinterpret_cast<void**>(&env), JNI_VERSION_1_6) != JNI_OK) {
        return -1;
    }

    host_log("[requestFunc] Create jstring url", strlen("[requestFunc] Create jstring url"));
    jstring jurl = env->NewStringUTF(std::string(url, len).c_str());
    if (!jurl) return -1; // allocation failed

    host_log("[requestFunc] Convert method into jint", strlen("[requestFunc] Convert method into jint"));
    jint jmethod = (jint)method;

    host_log("[requestFunc] Finding NativeBridge", strlen("[requestFunc] Finding NativeBridge"));
    jclass cls = env->GetObjectClass(gNativeBridgeObj);
    if (!cls) return -1;

    host_log("[requestFunc] Finding Kotlin request func", strlen("[requestFunc] Finding kotlin request func"));
    jmethodID requestId = env->GetMethodID(
        cls,
        "request",
        "(Ljava/lang/String;I)Ljava/lang/String;"
    );
    if (!requestId) {
        env->DeleteLocalRef(cls);
        env->DeleteLocalRef(jurl);
        return -1;
    }

    host_log("[requestFunc] Calling Kotlin request func", strlen("[requestFunc] Calling kotlin request func"));
    jstring result = (jstring) env->CallObjectMethod(gNativeBridgeObj, requestId, jurl, jmethod);
    const char* utf = env->GetStringUTFChars(result, nullptr);
    size_t utf_len = env->GetStringUTFLength(result);

    env->DeleteLocalRef(jurl);
    env->DeleteLocalRef(cls);
    env->ReleaseStringUTFChars(result, utf);
    env->DeleteLocalRef(result);

    return (int32_t)utf;
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
        if (!wasmModule) return 0;
        return wasmModule->add(a, b);
    }

    JNIEXPORT jstring JNICALL
    Java_dev_chouten_runners_relay_NativeBridge_callMethod(JNIEnv* env, jobject, jstring jname) {
        if (!wasmModule) return 0;
        const char* name = env->GetStringUTFChars(jname, nullptr);
        const char* ret = wasmModule->callMethod(name);

        return env->NewStringUTF(ret);
    }
}
