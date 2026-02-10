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
    if (!gJvm || !gNativeBridgeObj) return -1;

    JNIEnv* env = nullptr;
    if (gJvm->GetEnv(reinterpret_cast<void**>(&env), JNI_VERSION_1_6) != JNI_OK) {
        return -1;
    }

    // Copy Wasm memory into a temporary buffer + null terminator
    std::vector<char> buf(len + 1);
    memcpy(buf.data(), url, len);
    buf[len] = '\0';  // ensure null-terminated

    // Optional: validate UTF-8 here if needed
    jstring jurl = env->NewStringUTF(buf.data());
    if (!jurl) return -1; // allocation failed

    jint jmethod = (jint)method;

    jclass cls = env->GetObjectClass(gNativeBridgeObj);
    if (!cls) return -1;

    jmethodID requestId = env->GetMethodID(cls, "request", "(Ljava/lang/String;I)I");
    if (!requestId) {
        env->DeleteLocalRef(cls);
        env->DeleteLocalRef(jurl);
        return -1;
    }

    jint result = env->CallIntMethod(gNativeBridgeObj, requestId, jurl, jmethod);

    env->DeleteLocalRef(jurl);
    env->DeleteLocalRef(cls);

    return (int32_t)result;
}


// Keep a static module instance for now
static Wasm3Module* wasmModule = nullptr;

extern "C" {
    JNIEXPORT void JNICALL
    Java_dev_chouten_runners_relay_NativeBridge_initLogger(JNIEnv* env, jobject thiz, jobject logger) {
        env->GetJavaVM(&gJvm);
        gLoggerObj = env->NewGlobalRef(logger);
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
