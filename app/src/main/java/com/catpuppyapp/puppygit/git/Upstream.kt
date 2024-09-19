package com.catpuppyapp.puppygit.git

class Upstream {
    //配置文件里的值
    var remote=""  //配置文件key branch.<yourbranchname>.remote，值，举例：origin
    var branchRefsHeadsFullRefSpec=""  //配置文件key branch.<yourbranchname>.merge，值，举例：refs/heads/main

    //方便我使用的值
    var remoteBranchShortRefSpec=""  // 远程分支短名，eg: origin/main
    var remoteBranchRefsRemotesFullRefSpec=""  // 远程分支完整名，eg: refs/remotes/origin/main
    var downstreamLocalBranchShortRefSpec=""  //本地分支短名，eg: main
    var downstreamLocalBranchRefsHeadsFullRefSpec=""  //本地分支完整名，eg: refs/heads/main
    var pushRefSpec=""  // 本地分支和远程分支的完整名组成的refspec。 eg: refs/heads/main:refs/heads/main，冒号左边是本地分支，右边是远程分支，两个名字可以不一样
    var isPublished=false  //分支是否已经发布

    //和BranchNameAndTypeDto.remotePrefixFromShortName有所不同！这里的这个值几乎不会解析失败，因为上游在配置文件中指定了remote，所以除非remote无效，否则，remote不会存在歧义，因此这个值也百分百能解析出来
    var remoteBranchShortRefSpecNoPrefix=""  //移除 origin/main 中 origin/ 之后，余下的部分，例如 remoteBranchShortRefSpec=origin/main，则此变量应为 main（或者是移除远程分支refs/heads/之后余下的部分，例如branchFullRefSpec=refs/heads/main，则此值应为main）

    var localOid:String=""  //本地分支的最新commit hash
    var remoteOid:String=""  //远程分支的最新commit hash

}
