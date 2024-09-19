package com.catpuppyapp.puppygit.utils.snapshot

object SnapshotFileFlag {
    //其中file_ 代表快照是由文件创建的，content_代表文件是由用户修改过的内容创建的（就是在app内编辑还没保存到硬盘上的内容）
    //注：file_一般代表的是要写入的目标文件
    const val file_BeforeSave =  "file_BS" //file_BeforeSave, 保存之前为源文件创建的备份，一般在保存文件前发生源文件可能被外部修改过的时候会创建
    const val file_SimpleSafeFastSave =  "file_SSFS" //调用FsUtil#SimpleSafeFastSave时创建的源文件快照
    const val file_NormalDoSave =  "file_NDS" //NormalDoSave，正常情况下按保存按钮保存文件时创建的文件快照
    const val file_OnPause =  "file_OP" //在Activity#OnPause事件被触发时创建的快照，一般是在app切到后台或者compose出错app崩溃时创建的快照

    const val content_SaveErrFallback="ctnt_SEF"  //content_SaveErrFallback, 保存文件失败，把用户在app内修改的内容保存到快照目录
    const val content_CreateSnapshotForExternalModifiedFileErrFallback="ctnt_CMEF"  //保存时发现源文件被外部修改，于是对源文件创建拷贝，结果失败，于是放弃保存，转而为当前content创建快照，就是这个flag
    const val content_InstantSnapshot="ctnt_IS"  //content_InstantSnapshot, 即时保存的用户编辑内容
    const val content_FileNonExists_Backup="ctnt_FNEB"  //content_FileNonExistsBackup, 为已打开但后来不存在（比如被外部程序删除）的文件当前在app显示的内容创建的快照
    const val content_FilePathEmptyWhenSave_Backup="ctnt_PEB"  // 保存的时候，文件路径为空，但content不为空
    const val content_SimpleSafeFastSave="ctnt_SSFS"  // 调用FsUtil#SimpleSafeFastSave时创建的未保存内容快照
    const val content_NormalDoSave="ctnt_NDS"  // NormalDoSave，正常情况下按保存按钮保存文件时创建的内容快照
    const val content_OnPause="ctnt_OP"  //在Activity#OnPause事件被触发时创建的快照，一般是在app切到后台或者compose出错app崩溃时创建的快照
    const val content_BeforeReloadFoundSrcFileChanged="ctnt_BRFC"  //Reload之前发现源文件改变了！常见的发生情形是内部Editor打开了文件，然后用外部打开，然后点重载，就会发生这种情况，一般不用保存快照，但保存也没什么损失
    const val content_BeforeReloadFoundSrcFileChanged_ReloadByBackFromExternalDialog="ctnt_BRBE"  //在弹窗“文件被外部改变了，你可能想重载文件...”那个弹窗点击reload时，创建的内容快照，和顶栏的reload有所区分
}
