package com.catpuppyapp.puppygit.git

import com.catpuppyapp.puppygit.constants.Cons
import com.catpuppyapp.puppygit.play.pro.R
import com.catpuppyapp.puppygit.utils.AppModel
import com.catpuppyapp.puppygit.utils.Libgit2Helper
import java.time.OffsetDateTime

class TagDto (
    var name:String="",
    var shortName:String="",
    var fullOidStr:String="",   // see below annotation of `targetFullOidStr`
    var targetFullOidStr:String="",  // if "isAnnotated" is false, this equals fullOidStr, else this is commit's oid, fullOidStr is Tag's oid
    var isAnnotated:Boolean=false,

    // below only make sense for annotated tags
    var taggerName:String="",
    var taggerEmail:String="",
    var date:OffsetDateTime?=null,
    var msg:String=""
) {
    fun getFormattedTaggerNameAndEmail():String {
        return Libgit2Helper.getFormattedUsernameAndEmail(taggerName, taggerEmail)
    }

    fun getFormattedDate():String {
        return date?.format(Cons.defaultDateTimeFormatter) ?: ""
    }

    fun getType():String {
        val appContext = AppModel.singleInstanceHolder.appContext

        return if(isAnnotated) appContext.getString(R.string.annotated) else appContext.getString(R.string.lightweight)
    }
}
