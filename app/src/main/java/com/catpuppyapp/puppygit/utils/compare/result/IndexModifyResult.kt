package com.catpuppyapp.puppygit.utils.compare.result

class IndexModifyResult (
    val matched:Boolean,
    val matchedByReverseSearch:Boolean,
    val add:List<IndexStringPart>,
    val del:List<IndexStringPart>

) {

    fun toStringModifyResult(addSrc:String, delSrc:String): StringModifyResult {
        val addList = mutableListOf<StringPart>()
        val delList = mutableListOf<StringPart>()

        for(s in add) {
            addList.add(s.toStringPart(addSrc))
        }

        for(s in del) {
            delList.add(s.toStringPart(delSrc))
        }

        return StringModifyResult(matched, matchedByReverseSearch, addList, delList)
    }

    fun toStringModifyResult(addSrc:CharArray, delSrc:CharArray): StringModifyResult {
        val addList = mutableListOf<StringPart>()
        val delList = mutableListOf<StringPart>()

        for(s in add) {
            addList.add(s.toStringPart(addSrc))
        }

        for(s in del) {
            delList.add(s.toStringPart(delSrc))
        }

        return StringModifyResult(matched, matchedByReverseSearch, addList, delList)
    }

    override fun toString(): String {
        return "IndexModifyResult(matched=$matched, matchedByReverseSearch=$matchedByReverseSearch, add=$add, del=$del)"
    }

}
