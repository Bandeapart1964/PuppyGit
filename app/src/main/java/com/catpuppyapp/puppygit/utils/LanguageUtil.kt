package com.catpuppyapp.puppygit.utils

import android.content.Context
import com.catpuppyapp.puppygit.constants.LangCode
import com.catpuppyapp.puppygit.play.pro.R


object LanguageUtil {
    private val TAG="LanguageUtil"

    private val key = PrefMan.Key.lang

    val languageCodeList = listOf(
        LangCode.auto,
        LangCode.en,
        LangCode.zh_cn,
        // other language...
    )


    fun getLangCode(appContext: Context):String {
        return PrefMan.get(appContext, key, "")
    }

    fun setLangCode(appContext: Context, langCode:String) {
        PrefMan.set(appContext, key, langCode)
    }

    fun isAuto(langCode: String):Boolean {
        return langCode == LangCode.auto || langCode.isBlank()
    }

    /**
     * return true if `langCode` is not auto detected(empty string) and is supported
     */
    fun isSupportedLanguage(langCode:String, treatAutoAsUnsupported:Boolean=true):Boolean {
        // auto detected not represented any language, so return false
        if(treatAutoAsUnsupported && isAuto(langCode)) {
            return false
        }

        return languageCodeList.contains(langCode)
    }


    fun getLanguageTextByCode(languageCode:String, appContext: Context):String {
        if(languageCode.isBlank()) {
            return appContext.getString(R.string.auto)
        }

        if(languageCode == LangCode.en) {
            return appContext.getString(R.string.lang_name_english)
        }

        if(languageCode == LangCode.zh_cn) {
            return appContext.getString(R.string.lang_name_chinese_simplified)
        }

        // add other language here


        // should never reach here, if user got unknown, just set to a supported language will resolved
        MyLog.w(TAG, "#getLanguageTextByCode: unknown language code '$languageCode'")
        return appContext.getString(R.string.unsupported)
    }

    /**
     * e.g. input zh-rCN return Pair("zh", "CN")
     */
    fun splitLanguageCode(languageCode:String):Pair<String,String> {
        val codes = languageCode.split("-r")
        if(codes.size>1) {
            return Pair(codes[0], codes[1])
        }else {
            return Pair(codes[0], "")
        }
    }


}
