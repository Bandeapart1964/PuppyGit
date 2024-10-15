package com.catpuppyapp.puppygit.utils

import android.app.Activity.MODE_PRIVATE
import android.content.Context
import android.content.SharedPreferences

object PrefMan {
    private val fileName = "settings"

    object Key {
        /**
         * the value of this key:
         * empty string: auto detect language
         * country code: will use locale of country code, e.g. us will use english
         *
         * 自动检测或指定国家代码
         */
        val lang = "lang"
    }

    fun getPrefs(appContext: Context) = appContext.getSharedPreferences(fileName, MODE_PRIVATE)

    fun get(appContext: Context, key:String, defaultValue:String):String {
        // 获取 SharedPreferences
        val prefs = getPrefs(appContext)
        val languageCode = prefs.getString(key, defaultValue)
        return languageCode ?: defaultValue
    }


    fun set(appContext: Context, key:String, value:String) {
        // 获取 SharedPreferences 实例
        val prefs: SharedPreferences = getPrefs(appContext)
        // 获取编辑器
        val editor = prefs.edit()


        // 更新语言代码
        editor.putString(key, value)


        // 提交更改
        // apply is async, commit is sync
        editor.apply() // 或者 editor.commit();
    }

}
