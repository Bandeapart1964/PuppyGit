package com.catpuppyapp.puppygit.git

data class SubmoduleDto (
    val name:String,
    val relativePathUnderParent:String,
    val fullPath:String,
    val cloned:Boolean

)
