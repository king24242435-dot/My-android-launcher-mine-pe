#include <jni.h>
#include <dlfcn.h>
#include <android/log.h>
#include <string>

#define LOG_TAG "SOlauncherNative"
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO, LOG_TAG, __VA_ARGS__)
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, __VA_ARGS__)

extern "C" {

JNIEXPORT jboolean JNICALL
Java_com_solauncher_bedrock_NativeBridge_nativeLoadLibrary(
        JNIEnv* env,
        jobject /* thiz */,
        jstring libPath) {
    const char* path = env->GetStringUTFChars(libPath, nullptr);
    if (!path) return JNI_FALSE;

    dlerror(); // clear previous error
    void* handle = dlopen(path, RTLD_NOW | RTLD_GLOBAL);
    if (!handle) {
        const char* err = dlerror();
        LOGE("Failed to dlopen %s: %s", path, err ? err : "unknown error");
        env->ReleaseStringUTFChars(libPath, path);
        return JNI_FALSE;
    }

    LOGI("Successfully loaded native library: %s", path);
    env->ReleaseStringUTFChars(libPath, path);
    return JNI_TRUE;
}

JNIEXPORT jstring JNICALL
Java_com_solauncher_bedrock_NativeBridge_nativeGetLastError(
        JNIEnv* env,
        jobject /* thiz */) {
    const char* err = dlerror();
    return env->NewStringUTF(err ? err : "");
}

}
