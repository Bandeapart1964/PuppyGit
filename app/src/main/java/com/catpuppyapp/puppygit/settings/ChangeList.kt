package com.catpuppyapp.puppygit.settings

import kotlinx.serialization.Serializable

@Serializable
data class ChangeList (
    //version字段没用了，统一用AppSettings的版本号了
    //default版本号最好和最新的版本号不一样，不然用对应Settings类的fromJson()方法会返回带最新版本号的实例，那迁移的版本号检测就无效了，不过执行迁移时用的是map,所以其实没这个问题
//    var version = SettingsVersion.commonStartVer  // since version 1
    var lastUsedRepoId:String=""  //since version 0
)
