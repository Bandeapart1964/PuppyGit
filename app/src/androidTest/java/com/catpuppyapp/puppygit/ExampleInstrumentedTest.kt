package com.catpuppyapp.puppygit

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.catpuppyapp.puppygit.utils.encrypt.EncryptUtil
import junit.framework.TestCase.assertEquals
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Instrumented test, which will execute on an Android device.
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
@RunWith(AndroidJUnit4::class)
class ExampleInstrumentedTest {
    @Test
    fun useAppContext() {
        // Context of the app under test.
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
//        assertEquals("com.catpuppyapp.puppygit", appContext.packageName)
        println("appContext.packageName="+appContext.packageName)

//        testMapNullValue()
//        generateAESKey()
    }
}
fun generateAESKey(keySize: Int = 256) {
//    val keyGenerator = KeyGenerator.getInstance("AES")
//    keyGenerator.init(keySize)
//    val generateKey = keyGenerator.generateKey()
//    val pass = "abcdef1234_-"
    val pass = ""  //空字符串也能加密
    val key = "12345"
    val encryptString = EncryptUtil.encryptString(pass, key)
    println("encryptString:::"+encryptString)
    val decryptString = EncryptUtil.decryptString(encryptString, key)
    println("decryptString:::"+decryptString)
    assert(decryptString == pass)
    val e2 = EncryptUtil.encryptString(decryptString, key)
    println("encryptString:::"+ e2)
    println("decryptString:::"+EncryptUtil.decryptString(e2, key))
    println("decryptString:::"+EncryptUtil.decryptString(e2, key))
    assert(EncryptUtil.decryptString(e2, key) == pass)
//    assert(EncryptExample.decryptString("NFL/xXsIXtGVr4nPMDECVhH2Nd6C5V2f2wuwLBDGJZYXdL8TVipB/rEf6HfD1dx4Zj8XDLfsJUI=", key) == pass)
//    assert(EncryptExample.decryptString("NFL/xXsIXtGVr4nPMDECVhH2Nd6C5V2f2wuwLBDGJZYXdL8TVipB/rEf6HfD1dx4Zj8XDLfsJUI=", key) == pass)
}

fun testMapNullValue(){
    val m = mutableMapOf(Pair<String, String?>("1", null))
    m.put("2",null)
}
