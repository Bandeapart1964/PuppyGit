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
        const val lang = "lang"
        /**
         * e: err
         * w: warn
         * i: info
         * d: debug
         * v: verbose
         */
        const val logLevel = "log_level"
        /**
         * log file keep in days
         * 保留几天的日志
         */
        const val logKeepDays = "log_keep_days"
        /**
         * 0 auto; 1 light; 2 dark.
         * reference SettingsInnerPage.themeList, this value should match with the themeList indices
         * 这的值应该和设置页面的themeList索引匹配
         */
        const val theme = "theme"
    }

    fun getPrefs(appContext: Context) = appContext.getSharedPreferences(fileName, MODE_PRIVATE)

    fun get(appContext: Context, key:String, defaultValue:String):String {
        try {
            // 获取 SharedPreferences
            val prefs = getPrefs(appContext)
            val value = prefs.getString(key, defaultValue)
            return value ?: defaultValue
        }catch (_:Exception) {
            return defaultValue
        }
    }

    fun getInt(appContext: Context, key:String, defaultValue:Int):Int {
        try {
            val value = get(appContext, key, ""+defaultValue)
            return value.toInt()
        }catch (_:Exception) {
            return defaultValue
        }
    }

    fun getChar(appContext: Context, key:String, defaultValue:Char):Char {
        try {
            val value = get(appContext, key, ""+defaultValue)
            return value.get(0)
        }catch (_:Exception) {
            return defaultValue
        }
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
