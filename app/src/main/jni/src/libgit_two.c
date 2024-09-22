#include "git2.h"
#include "common.h"
#include <errno.h>
#include <android/log.h>
#include <jni.h>
#include <assert.h>
#include <string.h>
#include <stdbool.h>

#define LOG_TAG "JNI_libgit_two"

JNIEnv * globalEnv=NULL;

JNIEXPORT jstring JNICALL J_MAKE_METHOD(LibgitTwo_hello)(JNIEnv *env, jclass type, jint a, jint b) {
    //如果要算a+b需要创建一个char数组，然后把下面的字符串和计算结果装到里面，再返回char数组，太麻烦了，算了。
    return (*env)->NewStringUTF(env, "Hello from NDK,sum is:");
}


/*  辅助方法 start */
static jclass findClass(JNIEnv *env, const char *name) {
    jclass localClass = (*env)->FindClass(env, name);
    if (!localClass) {
        ALOGE("Failed to find class '%s'", name);
        abort();
    }
    jclass globalClass = (*env)->NewGlobalRef(env, localClass);
    (*env)->DeleteLocalRef(env, localClass);
    if (!globalClass) {
        ALOGE("Failed to create a global reference for '%s'", name);
        abort();
    }
    return globalClass;
}

static jfieldID findField(JNIEnv *env, jclass clazz, const char *name, const char *signature) {
    jfieldID field = (*env)->GetFieldID(env, clazz, name, signature);
    if (!field) {
        ALOGE("Failed to find field '%s' '%s'", name, signature);
        abort();
    }
    return field;
}

static jmethodID findMethod(JNIEnv *env, jclass clazz, const char *name, const char *signature) {
    jmethodID method = (*env)->GetMethodID(env, clazz, name, signature);
    if (!method) {
        ALOGE("Failed to find method '%s' '%s'", name, signature);
        abort();
    }
    return method;
}


static void throwException(JNIEnv *env, jclass exceptionClass, jmethodID constructor3,
                           jmethodID constructor2, const char *functionName, int error) {
    jthrowable cause = NULL;
    if ((*env)->ExceptionCheck(env)) {
        cause = (*env)->ExceptionOccurred(env);
        (*env)->ExceptionClear(env);
    }
    jstring detailMessage = (*env)->NewStringUTF(env, functionName);
    if (!detailMessage) {
        // Not really much we can do here. We're probably dead in the water,
        // but let's try to stumble on...
        (*env)->ExceptionClear(env);
    }
    jobject exception;
    if (cause) {
        exception = (*env)->NewObject(env, exceptionClass, constructor3, detailMessage, error,
                                      cause);
    } else {
        exception = (*env)->NewObject(env, exceptionClass, constructor2, detailMessage, error);
    }
    (*env)->Throw(env, exception);
    if (detailMessage) {
        (*env)->DeleteLocalRef(env, detailMessage);
    }
}
static jclass getLibgitTwoExceptionClass(JNIEnv *env) {
    static jclass exceptionClass = NULL;
    if (!exceptionClass) {
        exceptionClass = findClass(env,
                                   "com/catpuppyapp/puppygit/jni/LibGitTwoException");
    }
    return exceptionClass;
}

__attribute__((unused))  static void throwLibgitTwoException(JNIEnv* env, const char* functionName) {
    int error = errno;  //获取当前的errno值，好像是每个进程有各自的errno？类似 java 的ThreadLocal
    static jmethodID constructor3 = NULL;
    if (!constructor3) {
        constructor3 = findMethod(env, getLibgitTwoExceptionClass(env), "<init>",
                                  "(Ljava/lang/String;ILjava/lang/Throwable;)V");
    }
    static jmethodID constructor2 = NULL;
    if (!constructor2) {
        constructor2 = findMethod(env, getLibgitTwoExceptionClass(env), "<init>",
                                  "(Ljava/lang/String;I)V");
    }
    throwException(env, getLibgitTwoExceptionClass(env), constructor3, constructor2, functionName,
                   error);
}

//拷贝字符串并在其末尾加个\0
static char *j_strcopy(const char* src) {
    char *copy=NULL;
    size_t len = strlen(src)+1;
    if(src) {
        copy = malloc(len);
        if(copy) {
            strncpy(copy, src, len);
        }
    }
    return copy;
}

static char *j_copy_of_jstring(JNIEnv *env, jstring jstr, bool nullable) {
    if(!nullable) {
        //如果jstr为假，打印&&后面的字符串
        assert(jstr && "Cannot cast null to c string");
    }
    //如果jstr为无效内存地址0，返回0
    if(!jstr) {
        return NULL;
    }
    //最后一个参数是个 jboolean指针，如果其指向的地址不是NULL，则会对其赋值：创建了拷贝，设为真，否则设为假
    const char *c_str = (*env)->GetStringUTFChars(env,jstr,NULL);
    char *copy = j_strcopy(c_str);
    (*env)->ReleaseStringUTFChars(env, jstr, c_str);
    return copy;
}

/*  辅助方法 end */
JNIEXPORT void JNICALL J_MAKE_METHOD(LibgitTwo_jniLibgitTwoInit)(JNIEnv *env, jclass callerJavaClass) {
    git_libgit2_init();

    //TODO 不知道为什么，下面返回值就会报错，稍后再弄
//    int ret = git_libgit2_init();
//    if(ret!=0) {
//        //        throwLibgitTwoException(env,"jniClone");
//        const git_error *err = git_error_last();
//        ALOGE("jniLibgitTwoInit::ERROR '%s'", err->message);
//
//        return JNI_ERR;
//    }
//    return (jlong)ret;
}
//用https时，openssl验证证书失败也直接放行，解决证书无效的方法，若有其他解决方案则可删
int passCertCheck(git_cert *cert, int valid, const char *host, void *payload) {
/* 返回值说明：0允许连接；小于0拒绝连接；大于0表示这个callback不做决策，遵循其他验证器的结果。
    参见：https://libgit2.org/libgit2/#HEAD/group/callback/git_transport_certificate_check_cb
    */
    return 0;

}
JNIEXPORT void JNICALL J_MAKE_METHOD(LibgitTwo_jniSetCertCheck)(JNIEnv *env, jclass callerJavaClass, jlong remoteCallbacksPtr) {
    ((git_remote_callbacks *)remoteCallbacksPtr) ->certificate_check = passCertCheck;
}

JNIEXPORT jlong JNICALL J_MAKE_METHOD(LibgitTwo_jniSetCertFileAndOrPath)(JNIEnv *env, jclass callerJavaClass, jstring certFile, jstring certPath) {
    if(!certFile) {
        ALOGI("certFile is NULL");
    }
    if(!certPath) {
        ALOGI("certPath is NULL");
    }
    char *c_certFile = j_copy_of_jstring(env, certFile, true);
    char *c_certPath = j_copy_of_jstring(env, certPath, true);

    int error=git_libgit2_opts(GIT_OPT_SET_SSL_CERT_LOCATIONS,c_certFile,c_certPath);
//    ALOGE("errsetgitops::::%d", error);
    free(c_certFile);
    free(c_certPath);
    return error;
}

JNIEXPORT jlong JNICALL J_MAKE_METHOD(LibgitTwo_jniClone)(JNIEnv *env, jclass callerJavaClass, jstring url, jstring local_path, jlong jniOptionsPtr, jboolean allowInsecure) {
    git_repository *cloned_repo=NULL;
    char *c_url=j_copy_of_jstring(env, url, true);
    char *c_local_path=j_copy_of_jstring(env, local_path, true);
    git_clone_options cloneOptions = GIT_CLONE_OPTIONS_INIT;  //jniOptionsPtr是个地址，直接强转一下就行
    git_checkout_options checkout_opts = GIT_CHECKOUT_OPTIONS_INIT;
    checkout_opts.checkout_strategy = GIT_CHECKOUT_SAFE;
    cloneOptions.checkout_opts = checkout_opts;
    char *certfile="/storage/emulated/0/Android/data/com.catpuppyapp.puppygit/files/cafolder/399e7759.0";
    char *capath="/storage/emulated/0/Android/data/com.catpuppyapp.puppygit/files/cafolder";
    char *syscapath="/system/etc/security/cacerts/";
    char *syscapath2="/system/etc/security/cacerts_google";
    char *gitlabca="/storage/emulated/0/Android/data/com.catpuppyapp.puppygit/files/cafolder/gitlab.crt";

    ALOGE("before set cert path in libgit");

    int error = 0;
    //能添加多个证书或证书目录
//    error = git_libgit2_opts(GIT_OPT_SET_SSL_CERT_LOCATIONS,certfile,capath);
//     error = git_libgit2_opts(GIT_OPT_SET_SSL_CERT_LOCATIONS,gitlabca,NULL);
     error=git_libgit2_opts(GIT_OPT_SET_SSL_CERT_LOCATIONS,NULL,syscapath);
//    git_libgit2_opts(GIT_OPT_SET_SSL_CERT_LOCATIONS,gitlabca,NULL);
//    error = git_libgit2_opts(GIT_OPT_SET_SSL_CERT_LOCATIONS,NULL,syscapath2);
    if(error!=0) {
//        ALOGE("jniClonecertifile::ERROR '%s'", certfile);
//        ALOGE("jniClonecertpath::ERROR '%s'", capath);
        ALOGE("jniClonecertpath::ERROR '%d'", error);

    }


    if(allowInsecure){
        cloneOptions.fetch_opts.callbacks.certificate_check = passCertCheck;
    }
    int ret = git_clone(&cloned_repo, c_url, c_local_path, &cloneOptions);
    free(c_url);
    free(c_local_path);
    if(ret!=0) {
//        throwLibgitTwoException(env,"jniClone");
        const git_error *err = git_error_last();
        ALOGE("jniClone::ERROR '%s'", err->message);

        return JNI_ERR;
    }
    return (jlong)cloned_repo;
}

int cred_acquire_cb(git_credential **out, const char *url, const char *username_from_url, unsigned int allowed_types, void *payload) {
//    JNIEnv *env = globalEnv;
//    (*env)->CallObjectMethod(env, (jobject)payload, jniConstants->remote.midAcquireCred, (jstring)jniUrl, (jstring)usernameFromUrl, (jint)allowed_types);
    return git_credential_userpass_plaintext_new(out, "testusername", "testpassword");
}

JNIEXPORT void JNICALL J_MAKE_METHOD(LibgitTwo_jniSetCredentialCbTest)(JNIEnv *env, jclass callerJavaClass, jlong remoteCallbacks) {
    ALOGD("LibgitTwo_jniSetCredentialCbTest::");
    globalEnv = env;
    git_remote_callbacks *ptr = (git_remote_callbacks *)remoteCallbacks;
    ptr->credentials = cred_acquire_cb;
}
JNIEXPORT jlong JNICALL J_MAKE_METHOD(LibgitTwo_jniCreateCloneOptions)(JNIEnv *env, jclass callerJavaClass, jint version) {

    //分配内存
    git_clone_options *clone_options = (git_clone_options *)malloc(sizeof(git_clone_options));
    //初始化options结构体的值
    int ret = git_clone_init_options(clone_options,(unsigned int)version);
    //设置当前对象的字段 jniCRepoPtr 值为clone_options指针的值
    if(ret!=0) {
        //TODO 这个函数找不到对象，检查下类的构造器，然后把libgit2的错误信息附加的字符串里
//        throwLibgitTwoException(env,"jniCreateCloneOptions");
        return JNI_ERR;
    }
    //返回指针给java存上
    return (jlong)clone_options;
}

JNIEXPORT jlong JNICALL J_MAKE_METHOD(LibgitTwo_jniTestClone)(JNIEnv *env, jclass callerJavaClass, jstring url, jstring local_path, jlong jniOptionsPtr) {
    //分配内存
    git_clone_options *clone_options = (git_clone_options *)malloc(sizeof(git_clone_options));
    //初始化options结构体的值
    int ret = git_clone_init_options(clone_options,1);
    //设置当前对象的字段 jniCRepoPtr 值为clone_options指针的值
    if(ret!=0) {
//        throwLibgitTwoException(env,"jniCreateCloneOptions");
        return JNI_ERR;
    }

    git_repository *repo=NULL;
    char *c_url=j_copy_of_jstring(env, url, true);
    char *c_local_path=j_copy_of_jstring(env, local_path, true);

    int ret2 = git_clone(&repo, c_url, c_local_path, clone_options);
    free(c_url);
    free(c_local_path);
    if(ret || ret2) {
//        throwLibgitTwoException(env,"jniClone");
        return JNI_ERR;
    }
    return (jlong)repo;
}

JNIEXPORT jstring JNICALL J_MAKE_METHOD(LibgitTwo_jniLineGetContent)(JNIEnv *env, jclass callerJavaClass, jlong linePtr)
{
    ALOGD("ccode: jniLineGetContent() start\n");
    jstring s = (*env)->NewStringUTF(env, ((git_diff_line *)linePtr)->content);
    ALOGD("ccode: jniLineGetContent() end\n");
    return s;
}


JNIEXPORT void JNICALL J_MAKE_METHOD(LibgitTwo_jniTestAccessExternalStorage)(JNIEnv *env, jclass callerJavaClass)
{
    ALOGD("ccode: LibgitTwo_jniTestAccessExternalStorage() start\n");
    FILE* file = fopen("/sdcard/puppygit-repos/hello.txt","w+");

    if (file != NULL)
    {
        fputs("HELLO WORLD!\n", file);
        fflush(file);
        fclose(file);
    }

    ALOGD("ccode: LibgitTwo_jniTestAccessExternalStorage() end\n");
}
