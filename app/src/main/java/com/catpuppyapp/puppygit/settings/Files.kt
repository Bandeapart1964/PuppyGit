package com.catpuppyapp.puppygit.settings

import kotlinx.serialization.Serializable

@Serializable
data class Files (
    //Files页面最后打开的路径
    var lastOpenedPath:String="",
)
