package com.catpuppyapp.puppygit.utils.compare.search

class SearchDirection private constructor(val mode:Int) {
    companion object {
        val FORWARD = SearchDirection(0)  //仅正向搜索
        val REVERSE = SearchDirection(1)  //仅反向搜索

        val FORWARD_FIRST = SearchDirection(2)  //优先正向搜索，若无匹配，反向搜索
        val REVERSE_FIRST = SearchDirection(3)  //优先反向搜索，若无匹配，正向搜索
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as SearchDirection

        return mode == other.mode
    }

    override fun hashCode(): Int {
        return mode
    }

}

