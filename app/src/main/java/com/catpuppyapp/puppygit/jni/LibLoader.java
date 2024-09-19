package com.catpuppyapp.puppygit.jni;

public class LibLoader {
    static {
        System.loadLibrary("crypto");
        System.loadLibrary("ssl");
//        System.loadLibrary("ssh2");
        System.loadLibrary("git2");
        System.loadLibrary("git24j");
        System.loadLibrary("puppygit");
    }
    public static void load() {
        //象征性的空方法，没必要实现，加载这个类的class的时候就会执行静态代码块加载动态库了
    }
}
