package com.catpuppyapp.puppygit.utils.compare.param

class StringCompareParam(
    val chars:String,
): CompareParam {
    override fun getLen(): Int {
        return chars.length
    }

    override fun getChar(index:Int): Char {
        return chars.get(index)
    }

    override fun isEmpty(): Boolean {
        return chars.isEmpty()
    }

    override fun isOnlyLineSeparator(): Boolean {
        return getLen()==1 && getChar(0)=='\n'
    }
}
