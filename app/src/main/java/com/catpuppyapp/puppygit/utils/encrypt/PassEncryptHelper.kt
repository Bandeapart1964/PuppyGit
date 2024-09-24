package com.catpuppyapp.puppygit.utils.encrypt

class PassEncryptHelper {
    companion object {
        //如果修改加密实现，更新这个版本号，并添加对应的key和encryptor
        val passEncryptCurrentVer = 5

        //迁移机制大概就是找到旧版本号的密钥和加密解密器，解密数据；然后使用新版本号的密钥和加密解密器加密数据，最后把加密后的数据写入数据库，就完了。
        //所以，很重要的一点就是：不要删除任何版本的密钥！否则对应版本的用户密码将全部作废！
        //所以，很重要的一点就是：不要删除任何版本的密钥！否则对应版本的用户密码将全部作废！
        //所以，很重要的一点就是：不要删除任何版本的密钥！否则对应版本的用户密码将全部作废！
        //所以，很重要的一点就是：不要删除任何版本的密钥！否则对应版本的用户密码将全部作废！
        //所以，很重要的一点就是：不要删除任何版本的密钥！否则对应版本的用户密码将全部作废！

        //新版本的app包含旧版的所有密钥和加密器
        //key = ver, value = 密钥
        val keyMap:MutableMap<Int,String> = mutableMapOf(
            Pair(1, "3LHLpwTQ9uEyP9MCqgYNqncKxmQJsww9L4A7T7wK"),
            Pair(2, "ffHuzkprZY9b5PbYxaHPgHZ5UJxsqsL5MjqvCn7rQH3q7p7shz"),
            Pair(3, "qaWxActsnqiD2D5CmYroUcMRjYr4KDAiiNYHPs2RVs7DLTcU3y"),
            Pair(4, "C8mNzgW5Pwq3bFcaHP2WrwtZXA9bWniKgz9SeKRHxDbTyJ9LnZ"),
            Pair(5, "yAqg9o9K4vz7ALujvp3eczhJhYwvFZtMswvXn3MHj7TP5zPcj_Yq2KGK"),
            // other...
        )
        //key = ver, value = 加密解密器
        val encryptorMap:MutableMap<Int,Encryptor> = mutableMapOf(
            Pair(1, encryptor_ver_1),
            Pair(2, encryptor_ver_2),
            Pair(3, encryptor_ver_3),
            Pair(4, encryptor_ver_4),
            Pair(5, encryptor_ver_5),
            // other...
        )

        val currentVerKey:String = keyMap[passEncryptCurrentVer]!!
        val currentVerEncryptor:Encryptor = encryptorMap[passEncryptCurrentVer]!!

        fun encryptWithCurrentEncryptor(raw:String):String {
            return currentVerEncryptor.encrypt(raw, currentVerKey)
        }
        fun decryptWithCurrentEncryptor(encryptedStr:String):String {
            return currentVerEncryptor.decrpyt(encryptedStr, currentVerKey)
        }
    }
}
