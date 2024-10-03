package com.catpuppyapp.puppygit.utils

object RegexUtil {

    /**
     * e.g. input abc.txt, pattern *.txt, will matched
     */
    fun matchWildcard(input: String, pattern: String): Boolean {
        // 将通配符转换为正则表达式
        val regexPattern = pattern
            .replace(".", "\\.")  // 转义点号
            .replace("*", ".*")   // 将 * 替换为 .*
            .replace("?", ".")    // 将 ? 替换为 .
        return Regex(regexPattern).matches(input)
    }

    fun matchForIgnoreFile(input:String, pattern:String):Boolean {
        if(input.startsWith(pattern)) {
            return true
        }

        return matchWildcard(input, pattern)
    }

}

