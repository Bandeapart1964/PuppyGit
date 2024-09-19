package com.catpuppyapp.puppygit.dto

import androidx.room.Ignore
import com.catpuppyapp.puppygit.constants.Cons

class RemoteDto {
    var remoteId=""
    var remoteName=""
    var remoteUrl=""
    var credentialId:String?=""
    var credentialName:String?=""
    var credentialVal:String?=""
    var credentialPass:String?=""
    var credentialType:Int = Cons.dbCredentialTypeHttp
    var pushUrl=""
    var pushCredentialId:String?=""
    var pushCredentialName:String?=""
    var pushCredentialVal:String?=""
    var pushCredentialPass:String?=""
    var pushCredentialType:Int = Cons.dbCredentialTypeHttp
    var repoId=""
    var repoName=""

    //Ignore的作用是从数据库查dto的时候忽略此字段
    @Ignore
    var branchMode:Int= Cons.dbRemote_Fetch_BranchMode_All
    @Ignore
    var branchListForFetch:List<String> = listOf()

}
