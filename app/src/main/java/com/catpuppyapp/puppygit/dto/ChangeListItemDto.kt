package com.catpuppyapp.puppygit.dto

@Deprecated("好像用不着这个了，改用 StatusTypeEntrySaver 了")
class ChangeListItemDto {
    var fileFullPath:String = ""
    var fileName:String = ""
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ChangeListItemDto

        //只比较fullpath就够了
        if (fileFullPath != other.fileFullPath) return false
//        if (fileName != other.fileName) return false

        return true
    }

    override fun hashCode(): Int {
        return fileFullPath.hashCode()
    }


}