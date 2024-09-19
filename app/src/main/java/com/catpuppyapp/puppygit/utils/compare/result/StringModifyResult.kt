package com.catpuppyapp.puppygit.utils.compare.result

class StringModifyResult (
    val matched:Boolean,
    val matchedByReverseSearch:Boolean,
    val add:List<StringPart>,
    val del:List<StringPart>

) {

    override fun toString(): String {
        return "StringModifyResult(matched=$matched, matchedByReverseSearch=$matchedByReverseSearch, add=$add, del=$del)"
    }
}
