package com.catpuppyapp.puppygit.dto

import com.catpuppyapp.puppygit.constants.Cons
import com.catpuppyapp.puppygit.play.pro.R
import com.catpuppyapp.puppygit.utils.AppModel

/*
    select rem.remoteName as remoteName, rem.id as remoteId,
    rep.id as repoId, rep.repoName as repoName,
    cre.name as credentialName, rem.credentialId as credentialId
    from remote as rem left join credential as cre on rem.credentialId=cre.id left join
    repo as rep on rep.id = rem.repoId
 */
class RemoteDtoForCredential (var remoteId: String="",
                              var remoteName:String="",
                              var repoId:String="",
                              var repoName:String="",
                              var credentialId:String?="",  // fetch credential，之所以没明确叫fetchCredential是历史遗留问题，最初设计没考虑到把fetch和push凭据分开，因此只有一个credential字段
                              var credentialName:String?="",
                              var credentialType:Int= Cons.dbCredentialTypeHttp,
                              var pushCredentialId:String?="",
                              var pushCredentialName:String?="",
                              var pushCredentialType:Int= Cons.dbCredentialTypeHttp,
)
{
    fun getCredentialNameOrNone():String {
        return getFetchOrPushCredentialNameOrNone(isFetch = true)
    }

    fun getPushCredentialNameOrNone():String {
        return getFetchOrPushCredentialNameOrNone(isFetch = false)
    }

    private fun getFetchOrPushCredentialNameOrNone(isFetch:Boolean):String {
        val appContext = AppModel.singleInstanceHolder.appContext
        val name = if(isFetch) credentialName else pushCredentialName

        //if凭据名为null或空字符串返回 "[None]" else返回凭据名
        return if(name.isNullOrBlank()) "[${appContext.getString(R.string.none)}]" else (""+name)
    }

}
