package com.catpuppyapp.puppygit.utils.snapshot

import com.catpuppyapp.puppygit.constants.Cons
import com.catpuppyapp.puppygit.etc.Ret
import com.catpuppyapp.puppygit.utils.AppModel
import com.catpuppyapp.puppygit.utils.FsUtils
import com.catpuppyapp.puppygit.utils.MyLog
import com.catpuppyapp.puppygit.utils.getNowInSecFormatted
import com.catpuppyapp.puppygit.utils.getShortUUID
import java.io.File

object SnapshotUtil:SnapshotCreator {
    private val TAG = "SnapshotUtil"

    /*
        禁用快照时，返回Ret Success，但由于没创建快照，文件名和路径都是空字符串，但有的地方会用文件名或路径是否为空来判断快照是否创建成功，这样就会误判导致某些方法无法正常执行，
      因此，这里创建几个常量名用来作为禁用创建快照功能时的文件名和路径。
     */
    private val fileSnapshotDisable_FileNamePlaceHolder = "FileSnapshotDisable-name"  //文件快照禁用时，一律返回此文件名
    private val fileSnapshotDisable_FilePathPlaceHolder = "FileSnapshotDisable-path"  //文件快照禁用时，一律返回此路径
    private val contentSnapshotDisable_FileNamePlaceHolder = "ContentSnapshotDisable-name"  //内容快照禁用时，一律返回此文件名
    private val contentSnapshotDisable_FilePathPlaceHolder = "ContentSnapshotDisable-path"  //内容快照禁用时，一律返回此路径


    private var enableFileSnapshot = true
    private var enableContentSnapshot = true

    fun init(enableFileSnapshotInitValue:Boolean, enableContentSnapshotInitValue:Boolean) {
        enableFileSnapshot = enableFileSnapshotInitValue
        enableContentSnapshot = enableContentSnapshotInitValue
    }


    override fun createSnapshotByContentAndGetResult(srcFileName:String, fileContent:String, flag:String): Ret<Pair<String, String>?> {
        if(!enableContentSnapshot) {
            return Ret.createSuccess(Pair(contentSnapshotDisable_FileNamePlaceHolder, contentSnapshotDisable_FilePathPlaceHolder))
        }

        try {
            if(fileContent.isNotEmpty()) {
                val (snapshotFileName, snapFileFullPath, snapFile) = getSnapshotFileNameAndFullPathAndFile(srcFileName, flag)
                MyLog.w(TAG, "#createSnapshotByContentAndGetResult: will save snapFile to:" + snapFileFullPath)
                val snapRet = FsUtils.saveFileAndGetResult(fileFullPath = snapFileFullPath, text = fileContent)
                if(snapRet.hasError()) {
                    MyLog.e(TAG, "#createSnapshotByContentAndGetResult: save snapFile '$snapshotFileName' failed:" + snapRet.msg)
                    return Ret.createError(null, snapRet.msg)
                }else {
                    //把快照文件名和文件完整路径设置到snapRet里
                    return Ret.createSuccess(Pair(snapshotFileName, snapFileFullPath))
                }
            }else {  //文件内容为空
                val msg = "file content is empty, will not create snapshot for it"
                MyLog.w(TAG, "#createSnapshotByContentAndGetResult: $msg")
                return Ret.createSuccess(null, msg, Ret.SuccessCode.fileContentIsEmptyNeedNotCreateSnapshot)
            }
        }catch (e:Exception) {
            MyLog.e(TAG, "#createSnapshotByContentAndGetResult() err:" + e.stackTraceToString())
            return Ret.createError(null, "save file snapshot failed:${e.localizedMessage}", Ret.ErrCode.saveFileErr)
        }
    }


    override fun createSnapshotByFileAndGetResult(srcFile:File, flag:String):Ret<Pair<String,String>?>{
        //如果未开启文件快照功能，直接返回成功
        if(!enableFileSnapshot) {
            return Ret.createSuccess(Pair(fileSnapshotDisable_FileNamePlaceHolder, fileSnapshotDisable_FilePathPlaceHolder))  // 1是file name，2是file path
        }

        try {
            if(!srcFile.exists()) {
                throw NoSuchFileException(file=srcFile, reason="`srcFile` doesn't exist!")
            }

            val srcFileName = srcFile.name
            val (snapshotFileName, snapFileFullPath, snapFile) = getSnapshotFileNameAndFullPathAndFile(srcFileName, flag)

            MyLog.w(TAG, "#createSnapshotByFileAndGetResult: will save snapFile to:" + snapFileFullPath)

//            val snapRet = copyOrMoveOrExportFile(listOf(srcFile), snapFile, requireDeleteSrc = false)
            srcFile.copyTo(snapFile)
            if(!snapFile.exists()) {  //拷贝失败
                MyLog.e(TAG, "#createSnapshotByFileAndGetResult: save snapFile '$snapshotFileName' failed!")
                throw RuntimeException("copy src to snapshot failed!")
            }else{  //拷贝成功
                //把快照文件名设置到snapRet里
                return Ret.createSuccess(Pair(snapshotFileName, snapFileFullPath))
            }

        }catch (e:Exception) {
            MyLog.e(TAG, "#createSnapshotByFileAndGetResult() err:" + e.stackTraceToString())
            return Ret.createError(null, "save file snapshot failed:${e.localizedMessage}", Ret.ErrCode.saveFileErr)
        }
    }

    fun createSnapshotByContentWithRandomFileName(fileContent:String, flag:String): Ret<Pair<String, String>?> {
        return createSnapshotByContentAndGetResult(srcFileName = getShortUUID(10), fileContent=fileContent, flag = flag)
    }

    private fun getSnapshotFileNameAndFullPathAndFile(
        srcFileName: String,
        flag: String
    ): Triple<String, String, File> {
        val snapshotFileName = getANonexistsSnapshotFileName(srcFileName, flag)
        val snapDir = AppModel.singleInstanceHolder.getOrCreateFileSnapshotDir()
        val snapFile = File(snapDir.canonicalPath, snapshotFileName)
        //返回 filename, fileFullPath, file
        return Triple(snapshotFileName, snapFile.canonicalPath, snapFile)
    }


    fun getANonexistsSnapshotFileName(srcFileName:String, flag: String):String {
        while(true) {
            val fileName = genSnapshotFileName(srcFileName, flag)
            val file = File(fileName)
            if(!file.exists()) {
                return fileName
            }
        }
    }

    /**
    获取快照文件名

    目标文件名（快照文件名）格式为：“srcNameIncludeExt(包含后缀名）-flag-年月日时分秒-6位随机uid.bak”
    例如："abc.txt-content_saveErrFallback-20240421012203-ace123"
     */
    private fun genSnapshotFileName(srcFileName:String, flag: String, uidLen:Int=6):String {
        //目标文件名（快照文件名）格式为：“srcNameIncludeExt(包含后缀名）-flag-年月日时分秒-6位随机uid.bak”
        val sb = StringBuilder(srcFileName)
        return sb.append("-").append(flag).append("-").append(getNowInSecFormatted(Cons.dateTimeFormatterCompact)).append("-").append(getShortUUID(len = uidLen)).append(".bak").toString()
    }

}
