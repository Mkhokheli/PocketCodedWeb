#include <jni.h>
#include <android/log.h>
#include <cstdlib>
#include <cstring>
#include <memory>
#include <vector>
#include "node.h"

namespace {
constexpr const char* TAG = "PocketCodedNode";
}

extern "C" JNIEXPORT jint JNICALL
Java_com_mkhokheli_pocketcodedweb_NativeNodeBridge_start(
        JNIEnv* env,
        jobject,
        jobjectArray arguments) {
    const jsize count = env->GetArrayLength(arguments);
    if (count <= 0 || count > 64) {
        __android_log_write(ANDROID_LOG_ERROR, TAG, "Rejected invalid Node argument count");
        return -1;
    }

    std::vector<std::string> values;
    values.reserve(static_cast<size_t>(count));
    size_t byte_count = 0;
    for (jsize index = 0; index < count; ++index) {
        auto value = static_cast<jstring>(env->GetObjectArrayElement(arguments, index));
        if (value == nullptr) return -2;
        const char* utf = env->GetStringUTFChars(value, nullptr);
        if (utf == nullptr) {
            env->DeleteLocalRef(value);
            return -3;
        }
        values.emplace_back(utf);
        byte_count += values.back().size() + 1;
        env->ReleaseStringUTFChars(value, utf);
        env->DeleteLocalRef(value);
    }

    auto storage = std::make_unique<char[]>(byte_count);
    std::vector<char*> argv(static_cast<size_t>(count));
    char* cursor = storage.get();
    for (jsize index = 0; index < count; ++index) {
        const auto& value = values[static_cast<size_t>(index)];
        std::memcpy(cursor, value.c_str(), value.size() + 1);
        argv[static_cast<size_t>(index)] = cursor;
        cursor += value.size() + 1;
    }

    __android_log_write(ANDROID_LOG_INFO, TAG, "Starting persistent Node.js Mobile worker");
    return static_cast<jint>(node::Start(count, argv.data()));
}
