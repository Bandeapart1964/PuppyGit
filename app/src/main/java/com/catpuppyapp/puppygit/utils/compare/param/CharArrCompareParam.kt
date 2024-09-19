package com.catpuppyapp.puppygit.utils.compare.param

class CharArrCompareParam(
    val chars:CharArray,
): CompareParam {
    override fun getLen(): Int {
        return chars.size
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
