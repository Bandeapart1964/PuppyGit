package com.catpuppyapp.puppygit.utils.compare.result

class IndexStringPart (
    var start:Int,
    var end:Int,  //exclusive
    var modified:Boolean
) {
    fun toStringPart(src:String): StringPart {
        return StringPart(src.substring(start, end), modified)
    }

    fun toStringPart(src:CharArray): StringPart {
        return StringPart(String(src.copyOfRange(start, end)), modified)
    }

    override fun toString(): String {
        return "IndexStringPart(start=$start, end=$end, modified=$modified)"
    }

}
