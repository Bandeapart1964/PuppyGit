package com.catpuppyapp.puppygit.settings.migrate.changelist

import com.catpuppyapp.puppygit.settings.migrate.FromVToV
import com.catpuppyapp.puppygit.settings.version.SettingsVersion

class ChangeListSettingsFrom0To1: FromVToV(from = "0", to="1") {
    override fun doMigration(s: MutableMap<String,String>) {
        //把当前版本key先列出来
        val key_version = SettingsVersion.commonKey_version  // since version 1
        val key_lastUsedRepoId = "lastUsedRepoId";  // since version 0

        //执行迁移操作，之后父类会自动更新版本号，因为本次迁移只需要更新版本号，所以实际上不需要执行任何操作
        //如果要迁移的话，可以调用父类的方法
        //e.g.
        //renameField(s,key_old, key_new)
    }
}
