package com.catpuppyapp.puppygit.jni;

import java.nio.charset.StandardCharsets;

public class LibgitTwo {
//    static {
//        System.loadLibrary("crypto");
//        System.loadLibrary("ssl");
//        System.loadLibrary("ssh2");
//        System.loadLibrary("git2");
//        System.loadLibrary("git24j");
//        System.loadLibrary("puppygit");
//    }
    public static int cloneVersion = 1;
    private long jniCloneOptionsPtr;
    //指向clone后产生的git仓库对象的指针，c里一般用int做返回值来指示函数执行是否成功。然后传入一个指针对象用来存储需要返回的对象，例如：int createStudent(Student* ret)，其中，int是返回值，但函数创建的Student结构体存储在ret指向的内存中
    private long jniCRepoPtr;

    /*
     * example:
         @Throws(SyscallException::class) //等于java的throws语句，在c里抛java异常时用得到这个
         external fun access(path: ByteString, mode: Int): Boolean
     */

    public static native String hello(int a,int b);


    //TODO 写public方法调用native方法，并把返回的指针存到实例变量里

    /**
     * jniCRetParamRepoPtr:
     * url: 要clone的url
     * local_path: 仓库本地路径
     * jniOptionsPtr: clone命令的选项，是个指针，指向jni创建的options对象
     *
     * 返回值指向git仓库的指针，正常来说返回值是非0正数
     */
    public static native long jniSetCertFileAndOrPath(String certFile, String certPath);
    public native void jniLibgitTwoInit();
    public native long jniClone(String url, String local_path, long jniCloneOptionsPtr, boolean allowInsecure);
    /**
     * 返回值为指向clone_options结构体的指针
     */
    public native long jniCreateCloneOptions(int version);

    public native long jniTestClone(String url, String local_path, long jniCloneOptionsPtr);

    public static native void jniSetCredentialCbTest(long remoteCallbacks);
    public static native void jniSetCertCheck(long remoteCallbacks);
    public static native String jniLineGetContent(long linePtr);
    public static native void jniTestAccessExternalStorage();

    public static String getContent(int contentLen, String content) {
        // content.length() is "chars count", not "bytes count"!
        // so this code is wrong in some cases! sometimes it will give you more lines than you wanted.
//            if (content.length() >= contentLen) {
//                return content.substring(0, contentLen);
//            }
        byte[] src = content.getBytes(StandardCharsets.UTF_8);
        // bytes.length < contentLen maybe not happen, because contentLen should be a part of content
        if(src.length > contentLen) {  //if content length bigger than contentLen, create a new sub array
            byte[] dest = new byte[contentLen];
            System.arraycopy(src,0,dest,0,contentLen);
            return new String(dest);
        }

        // if content length equals contentLen, just return it, no more operations required
        return content;

    }

}
