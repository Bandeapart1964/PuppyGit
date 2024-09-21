package com.catpuppyapp.puppygit.settings

import kotlinx.serialization.Serializable

@Serializable
data class Editor (
    //default版本号最好和最新的版本号不一样，不然用对应Settings类的fromJson()方法会返回带最新版本号的实例，那迁移的版本号检测就无效了，不过执行迁移时用的是map,所以其实没这个问题
//    var version = SettingsVersion.commonStartVer  // since version 1
    var lastEditedFilePath:String="",  //since version 0

    // 文件最后编辑位置，注：key为文件完整路径
    //TODO 在设置页面加个选项，可清除此map，否则此map只有在文件不存在时(打开时检查)，才清除打开位置，或者实现一个定期未打开就清除其记录的机制，我已经在FileEditedPos里记录在最后使用条目的时间，可用来实现定期不用删除机制
    val filesLastEditPosition:MutableMap<String,FileEditedPos> = mutableMapOf(),

    //打开文件时是否定位到上次编辑列（或者说恢复光标位置到上次编辑的列）
    //注意：如果有删除文本但没保存，这个定位会不准，但经我测试，没遇到会导致app崩溃的情况，所以问题不大
    //若bug `bug_Editor_GoToColumnCantHideKeyboard_Fixed` 未修复，此值无效，启动editor将强制不定位到上次编辑列
    var restoreLastEditColumn:Boolean=false,  //打开文件时，定位到上次编辑列。注意：会弹出键盘！（我理想中的情况是不会弹出键盘，因为自动弹出键盘有可能会使文本内容被键盘意外读取而泄漏隐私，但没找到合适的方案隐藏键盘。）

    var editCacheKeepInDays:Int = 3,  //编辑缓存文件保存天数，超过天数的文件会在启动app时删除
    var editCacheEnable:Boolean = false,  //是否启用编辑缓存，修改后重启app生效

    var conflictStartStr:String = SettingsCons.defaultConflictStartStr,
    var conflictSplitStr:String = SettingsCons.defaultConfilctSplitStr,
    var conflictEndStr:String = SettingsCons.defaultConflictEndStr,

    var fontSize:Int = SettingsCons.defaultFontSize,  //字体大小，单位sp
    var lineNumFontSize:Int = SettingsCons.defaultLineNumFontSize,  //行号字体大小
    var showLineNum:Boolean = true,  //是否显示行号

    var enableFileSnapshot:Boolean = false,  //是否允许创建文件快照，重启app生效
    var enableContentSnapshot:Boolean = false,  //是否允许创建内容快照，重启app生效。（ps 内容就是编辑文件时在内存中但还未写入到硬盘的内容）
)
