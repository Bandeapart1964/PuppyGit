package com.catpuppyapp.puppygit.settings.migrate

import com.catpuppyapp.puppygit.settings.version.SettingsVersion

abstract class FromVToV(var from: String, var to: String) {
    //子类需要重写这个方法执行迁移操作，不需要更新版本号，父类会帮你更新
    abstract fun doMigration(settings:MutableMap<String,String>);

    //供外部调用的公开接口，执行实际的迁移操作
    fun migration(s: MutableMap<String,String>) {
        //先检查版本号是否匹配
        val key_version = SettingsVersion.commonKey_version
        //如果from和入参的version不同，可能误调用，直接返回
        //加""是为了处理null的情况，因为最初的设置项没version字段
        if(""+s[key_version] != from) {
            return
        }

        //执行子类重写的迁移方法
        doMigration(s)

        //更新版本，注意这里应该更新成to而不是最新的版本，不然会影响其他迁移器的执行
        s[key_version] = to
    }


    //供子类使用的更新字段的方法
    protected fun getValueOrEmptyStr(s:MutableMap<String,String>, key:String):String {
        return s.get(key)?:""
    }

    protected fun updateField(s:MutableMap<String,String>, key:String, newValue:String):String {
        return s.put(key,newValue)?:""
    }

    protected fun removedField(s:MutableMap<String,String>, key:String):String {
        return s.remove(key)?:""
    }

    protected fun renameField(s:MutableMap<String,String>, key:String, newKey:String):String {
        val tmpV = getValueOrEmptyStr(s, key)
        updateField(s, newKey, tmpV)
        return removedField(s, key)
    }
}
