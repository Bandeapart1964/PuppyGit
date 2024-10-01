package com.catpuppyapp.puppygit.git

import com.catpuppyapp.puppygit.play.pro.R
import com.catpuppyapp.puppygit.utils.AppModel

data class SubmoduleDto (
    val name:String,
    val remoteUrl:String,
    val relativePathUnderParent:String,
    val fullPath:String,
    val cloned:Boolean,

    var tempStatus:String = "",  // cloning... etc

) {
    private fun getClonedText():String{
        val appContext = AppModel.singleInstanceHolder.appContext

        return if(cloned) appContext.getString(R.string.cloned) else appContext.getString(R.string.not_clone)

    }

    fun getStatus():String {
        return tempStatus.ifBlank { getClonedText() }
    }
}
