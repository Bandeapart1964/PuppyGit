package com.catpuppyapp.puppygit.dto

import com.catpuppyapp.puppygit.constants.Cons
import com.catpuppyapp.puppygit.git.BranchNameAndTypeDto
import com.catpuppyapp.puppygit.git.CommitDto
import com.catpuppyapp.puppygit.git.SubmoduleDto
import com.catpuppyapp.puppygit.git.TagDto
import com.catpuppyapp.puppygit.utils.Libgit2Helper
import com.catpuppyapp.puppygit.utils.Libgit2Helper.Companion.isValidGitRepo
import com.catpuppyapp.puppygit.utils.Libgit2Helper.Companion.getParentRecordedTargetHashForSubmodule
import com.github.git24j.core.Commit
import com.github.git24j.core.Oid
import com.github.git24j.core.Repository
import com.github.git24j.core.Submodule

fun createCommitDto(
    commitOid: Oid,
    allBranchList: List<BranchNameAndTypeDto>,
    allTagList:List<TagDto>,
    commit: Commit,
    repoId: String,
    repoIsShallow:Boolean,
    shallowOidList:List<String>
): CommitDto {
    val c = CommitDto()
    /*
             var oidStr: String="",
             var branchShortNameList: MutableList<String> = mutableListOf(),  //分支名列表，master origin/master 之类的，能通过看这个判断出是否把分支推送到远程了
             var parentOidStrList: MutableList<String> = mutableListOf(),  //父提交id列表，需要的时候可以根据这个oid取出父提交，然后可以取出父提交的树，用来diff
             var dateTime: String="",
             var author: String="",
             var email: String="",
             var shortMsg:String="", //只包含第一行
             var msg: String="",  //完整commit信息
             var repoId:String="",  //数据库的repoId，用来判断当前是在操作哪个仓库
             var treeOidStr:String="",  //提交树的oid和commit的oid不一样哦
             */
    c.oidStr = commitOid.toString()  // next.toString() or commit.id() ，两者相同，但用 next.toString() 性能更好，因为Oid纯java实现，不需要jni
    c.shortOidStr = Libgit2Helper.getShortOidStrByFull(c.oidStr)
    val commitOidStr = commit.id().toString()
    //添加分支列表
    for (b in allBranchList) {
        if (b.oidStr == commitOidStr) {
            c.branchShortNameList.add(b.shortName)
        }
    }
    //添加tag列表
    for(t in allTagList) {
        if(t.targetFullOidStr == commitOidStr) {
            c.tagShortNameList.add(t.shortName)
        }
    }

    //添加parent列表，合并的提交就会有多个parent，一般都是1个
    val parentCount = commit.parentCount()
    if (parentCount > 0) {
        var pc = 0
        while (pc < parentCount) {
            val parentOidStr = commit.parentId(pc).toString()
            c.parentOidStrList.add(parentOidStr)
            c.parentShortOidStrList.add(Libgit2Helper.getShortOidStrByFull(parentOidStr))
            pc++
        }
    }
    c.dateTime = Libgit2Helper.getDateTimeStrOfCommit(commit)

    val commitSignature = commit.author()  // git log 命令默认输出的author
    c.author = commitSignature.name
    c.email = commitSignature.email

    val committer = commit.committer()  //实际提交的人
    c.committerUsername = committer.name
    c.committerEmail = committer.email

    c.shortMsg = commit.summary()
    c.msg = commit.message()
    c.repoId = repoId
    c.treeOidStr = commit.treeId().toString()

    if(repoIsShallow && shallowOidList.contains(c.oidStr)) {
        c.isGrafted=true  //当前提交是shallow root
    }

    return c
}


fun updateRemoteDtoList(repo: Repository, remoteDtoList: List<RemoteDto>, onErr:(errRemote: RemoteDto, e:Exception)->Unit={r,e->}) {
    remoteDtoList.forEach {
        try {
            updateRemoteDto(repo, it)
        }catch (e:Exception) {
            onErr(it, e)
        }
    }
}

fun updateRemoteDto(repo: Repository, remoteDto: RemoteDto) {
    val remoteName = remoteDto.remoteName
    val remote = Libgit2Helper.resolveRemote(repo, remoteName) ?: return

    //更新remoteUrl(即git config文件中的url)、pushUrl
    remoteDto.remoteUrl = remote.url().toString()
    remoteDto.pushUrl = remote.pushurl()?.toString()?:""

    // if push url not set, use same as fetch url(remoteUrl)
    if(remoteDto.pushUrl.isBlank()) {
        remoteDto.pushUrl = remoteDto.remoteUrl
        remoteDto.pushUrlTrackFetchUrl = true
    }

    //更新branchMode
    val (isAll, branchNameList) = Libgit2Helper.getRemoteFetchBranchList(remote)
    if(isAll) {
        remoteDto.branchMode = Cons.dbRemote_Fetch_BranchMode_All
        remoteDto.branchListForFetch = emptyList()  //fetch refspec为所有分支的时候用不到分支列表，设个空列表即可
    }else {
        remoteDto.branchMode = Cons.dbRemote_Fetch_BranchMode_CustomBranches
        remoteDto.branchListForFetch = branchNameList  //自定义分支列表，列表值是指定的分支的名字
    }
}


fun createSubmoduleDto(
    sm: Submodule,
    smName: String,
    parentWorkdirPathNoSlashSuffix: String,
    invalidUrlAlertText: String
): SubmoduleDto {
    val smRelativePath = sm.path()
    val smFullPath = parentWorkdirPathNoSlashSuffix + Cons.slash + smRelativePath.removePrefix(Cons.slash)

    // [fixed, the reason was pass NULL to jni StringUTF method in c codes] if call submodule.url() it will crashed when url invalid
    val smUrl = sm.url()?.toString() ?: ""
    //another way to get url from .gitsubmodules, is read info by kotlin, 100% safe
    //                val smUrl = getValueFromGitConfig(parentDotGitModuleFile, "submodule.$name.url")
    val smDto = SubmoduleDto(
        name = smName,
        relativePathUnderParent = smRelativePath,
        fullPath = smFullPath,
        cloned = isValidGitRepo(smFullPath),
        remoteUrl = smUrl,
        targetHash = getParentRecordedTargetHashForSubmodule(sm),
        tempStatus = if (smUrl.isBlank()) invalidUrlAlertText else "",
        location = Libgit2Helper.getSubmoduleLocation(sm)
    )

    return smDto
}
