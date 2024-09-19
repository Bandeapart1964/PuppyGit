package com.catpuppyapp.puppygit.settings.version

class SettingsVersion {
    companion object {
        //公用
        val commonKey_version = "version"
        val commonStartVer = 0;  //正常来说应该不会有这个版本，除非是之前没加入版本号时创建的设置项

        val appSettingsCurVersion = 1

//        //各自
//        val changeListSettingsCurrentVer = "1";
//        val editorSettingsCurrentVer = "1";
//
//        //other default and current version
//        //在数据库弄了个条目用来当公共的git配置文件，目前只存了邮箱和用户名
//        val commonGitConfigSettingsCurrentVer = "1";
    }
}