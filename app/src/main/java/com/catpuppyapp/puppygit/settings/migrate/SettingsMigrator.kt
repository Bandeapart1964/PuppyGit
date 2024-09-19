package com.catpuppyapp.puppygit.settings.migrate

//弃用了

//import com.catpuppyapp.puppygit.constants.Cons
//import com.catpuppyapp.puppygit.data.entity.SettingsEntity
//import com.catpuppyapp.puppygit.settings.migrate.changelist.ChangeListSettingsFrom0To1
//import com.catpuppyapp.puppygit.settings.version.SettingsVersion
//import com.catpuppyapp.puppygit.utils.MoshiUtil
//
//class SettingsMigrator {
//    companion object {
//        //为不同的设置类建各自的迁移方法列表，按序存储，然后调用
//        private val changeListSettingsMigratorList:List<FromVToV> = listOf(
////            ChangeListSettingsFromNullTo0(),  //因为在执行迁移方法前会先检查是否存在version，如果不存在会put一个起始版本号进去，所以就不需要null to 0的迁移类了
//            //一定要按from的顺序从小到大排
//            ChangeListSettingsFrom0To1(),
//
//        )
//        private val commonGitConfigSettingsMigratorList:List<FromVToV> = listOf(
//            //一定要按from的顺序从小到大排
//
//        )
//        private val editorSettingsMigratorList:List<FromVToV> = listOf(
//            //一定要按from的顺序从小到大排
//
//        )
//
//        fun migrateSettingsIfNeed(settings: SettingsEntity):Boolean {
//            val settingsMap:MutableMap<String,String> = MoshiUtil.mMapAdapter.fromJson(settings.jsonVal)?:throw RuntimeException("bad `Settings` `jsonVal` to `Map`, usedFor:"+settings.usedFor+",jsonVal:"+settings.jsonVal)
//            val key_version = SettingsVersion.commonKey_version
////            changeListSettings?:throw RuntimeException("bad `ChangeListSettings`, `usedFor`:"+settings.usedFor+", `jsonVal`:"+settings.jsonVal)
//
//            //如果已经是最新版本号，不需要迁移
//            if(settingsMap[key_version] == SettingsVersion.changeListSettingsCurrentVer) {
//                return false;
//            }else { //版本号不一致，执行迁移
//                //最初的类没版本号，需要先put默认版本号作为起始版本号，要不然map取version会取出null，那样的话还得写个null to 0的migrator，专门更新版本号，不如直接在这初始化一下
//                settingsMap.putIfAbsent(key_version, SettingsVersion.commonStartVer)
//
//                //根据不同的usedFor调用不同的迁移方法链，if else判断所有usedFor，最后else兜底，如果没匹配到usedFor，抛出异常
//                val usedFor = settings.usedFor
//                if(usedFor == Cons.dbSettingsUsedForChangeList) {
//                    //链式调用迁移方法
//                    forEachExecuteMigratorsLink(settingsMap, changeListSettingsMigratorList)
//                    //检查迁移后的版本号是否是最新
//                    checkVersionIfBadThrow(settingsMap[key_version]?:"", SettingsVersion.changeListSettingsCurrentVer, usedFor, settingsMap)
//                }// else if other usedFor，其他迁移方法只需要把上面的`迁移链表`(changeListSettingsMigratorList)和`设置类当前版本`(changeListSettingsCurrentVer) 换成对应设置类的链表和当前版本就行
//                else if(usedFor == Cons.dbSettingsUsedForCommonGitConfig) {
//                    forEachExecuteMigratorsLink(settingsMap, commonGitConfigSettingsMigratorList)
//                    checkVersionIfBadThrow(settingsMap[key_version]?:"", SettingsVersion.commonGitConfigSettingsCurrentVer, usedFor, settingsMap)
//                }
//                else if(usedFor == Cons.dbSettingsUsedForEditor) {
//                    forEachExecuteMigratorsLink(settingsMap, editorSettingsMigratorList)
//                    checkVersionIfBadThrow(settingsMap[key_version]?:"", SettingsVersion.editorSettingsCurrentVer, usedFor, settingsMap)
//                }
//                else {  // if non-matched usedFor, throw exception
//                    throw RuntimeException("No matched migrator for Settings, `usedFor`:"+usedFor+", `jsonVal`:"+settings.jsonVal)
//                }
//
//                //更新settings的jsonVal
//                settings.jsonVal = MoshiUtil.mMapAdapter.toJson(settingsMap)
//                //返回true表示需要更新数据库
//                return true;
//            }
//
////            throw RuntimeException("bad `ChangeListSettings` `version`! `jsonVal`:"+settings.jsonVal)
//
//        }
//
//        private fun checkVersionIfBadThrow(
//            ver: String,
//            expectVer: String,
//
//            //下面变量只是为了打印错误信息
//            usedFor: Int,
//            settingsMap: MutableMap<String, String>
//        ) {
//            if(ver != expectVer) {
//                throw RuntimeException("Settings ver!=expectVer: "+ver+"!="+expectVer+"; usedFor:"+usedFor+",after migrated jsonVal:"+MoshiUtil.mMapAdapter.toJson(settingsMap))
//            }
//        }
//
//        private fun forEachExecuteMigratorsLink(settingsMap: MutableMap<String, String>, migratorsList: List<FromVToV>) {
//            for (migrator in migratorsList) {
//                //这个方法会改变map
//                //这个方法内部会检测如果from不匹配，不会修改map，所以这里的循环不需要再判断版本号，直接调用即可
//                migrator.migration(settingsMap)
//            }
//        }
//    }
//}
