package com.catpuppyapp.puppygit.utils.compare.param

interface CompareParam {
    fun getLen() : Int
    fun getChar(index:Int) : Char
    fun isEmpty():Boolean
    fun isOnlyLineSeparator():Boolean
}
