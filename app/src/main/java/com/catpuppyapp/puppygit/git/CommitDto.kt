package com.catpuppyapp.puppygit.git

import com.catpuppyapp.puppygit.play.pro.R
import com.catpuppyapp.puppygit.utils.AppModel

class CommitDto (
                 var oidStr: String="",
                 var shortOidStr: String="",
                 var branchShortNameList: MutableList<String> = mutableListOf(),  //分支名列表，master origin/master 之类的，能通过看这个判断出是否把分支推送到远程了
                 var parentOidStrList: MutableList<String> = mutableListOf(),  //父提交id列表，需要的时候可以根据这个oid取出父提交，然后可以取出父提交的树，用来diff
                 var parentShortOidStrList: MutableList<String> = mutableListOf(),  //父提交短id列表
                 var dateTime: String="",
                 var author: String="",  // username
                 var email: String="",
                 var committerUsername:String="",
                 var committerEmail:String="",
                 var shortMsg:String="", //只包含第一行
                 var msg: String="",  //完整commit信息
                 var repoId:String="",  //数据库的repoId，用来判断当前是在操作哪个仓库
                 var treeOidStr:String="",  //提交树的oid和commit的oid不一样哦
                 var isGrafted:Boolean=false,  // shallow root，此值为true，参考pcgit，在打印shallow仓库时，会对shallow root添加grafted标识，不过需要注意，一个提交列表可能有多个isGrafted，这种情况发生于多个grafted是同一个提交的父提交的情况，换句话说，那个子提交是由多个父提交合并来的
                 var tagShortNameList:MutableList<String> = mutableListOf()
) {

    fun hasOther():Boolean {
        return isGrafted
    }

    fun getOther():String {
        val appContext = AppModel.singleInstanceHolder.appContext
        val sb = StringBuilder()
        val suffix = ", "

        sb.append(if(isGrafted) appContext.getString(R.string.is_grafted) else appContext.getString(R.string.not_grafted))

        return sb.toString()
    }

    fun authorAndCommitterAreSame():Boolean {
        return author==committerUsername && email==committerEmail
    }
}
