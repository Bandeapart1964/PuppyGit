package com.catpuppyapp.puppygit.dto

import android.content.Context
import com.catpuppyapp.puppygit.play.pro.R
import com.catpuppyapp.puppygit.utils.MyLog
import com.catpuppyapp.puppygit.utils.getFileAttributes
import com.catpuppyapp.puppygit.utils.getFormatTimeFromSec
import com.catpuppyapp.puppygit.utils.getHumanReadableSizeStr
import com.catpuppyapp.puppygit.utils.getSystemDefaultTimeZoneOffset
import com.catpuppyapp.puppygit.utils.mime.MimeType
import com.catpuppyapp.puppygit.utils.mime.guessFromFile
import java.io.File
import java.util.concurrent.TimeUnit

private val TAG = "FileItemDto"

class FileItemDto {
    var name=""
    var createTime=""
    var createTimeInSec=0L
    var lastModifiedTime=""
    var lastModifiedTimeInSec=0L
    var sizeInBytes =0L
    var sizeInHumanReadable =""
    var isFile = false
    var isDir = false
    var fullPath = ""
    var mime=MimeType.TEXT_PLAIN
    var isHidden = false

    fun toFile():File {
        return File(fullPath)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as FileItemDto

        if (name != other.name) return false
        if (createTime != other.createTime) return false
        if (createTimeInSec != other.createTimeInSec) return false
        if (lastModifiedTime != other.lastModifiedTime) return false
        if (lastModifiedTimeInSec != other.lastModifiedTimeInSec) return false
        if (sizeInBytes != other.sizeInBytes) return false
        if (sizeInHumanReadable != other.sizeInHumanReadable) return false
        if (isFile != other.isFile) return false
        if (isDir != other.isDir) return false
        if (fullPath != other.fullPath) return false
        if (mime != other.mime) return false
        if (isHidden != other.isHidden) return false

        return true
    }

    override fun hashCode(): Int {
        var result = name.hashCode()
        result = 31 * result + createTime.hashCode()
        result = 31 * result + createTimeInSec.hashCode()
        result = 31 * result + lastModifiedTime.hashCode()
        result = 31 * result + lastModifiedTimeInSec.hashCode()
        result = 31 * result + sizeInBytes.hashCode()
        result = 31 * result + sizeInHumanReadable.hashCode()
        result = 31 * result + isFile.hashCode()
        result = 31 * result + isDir.hashCode()
        result = 31 * result + fullPath.hashCode()
        result = 31 * result + mime.hashCode()
        result = 31 * result + isHidden.hashCode()
        return result
    }

    override fun toString(): String {
        return "FileItemDto(name='$name', createTime='$createTime', createTimeInSec=$createTimeInSec, lastModifiedTime='$lastModifiedTime', lastModifiedTimeInSec=$lastModifiedTimeInSec, sizeInBytes=$sizeInBytes, sizeInHumanReadable='$sizeInHumanReadable', isFile=$isFile, isDir=$isDir, fullPath='$fullPath', mime=$mime, isHidden=$isHidden)"
    }

    companion object {

        //appContext for get string source
        fun genFileItemDtoByFile(file: File, appContext:Context):FileItemDto {
            val fdto = FileItemDto()
            updateFileItemDto(fdto, file, appContext)
            return fdto
        }

        fun updateFileItemDto(fdto:FileItemDto, file: File, appContext: Context) {
            try {
                fdto.name = file.name
                fdto.fullPath = file.canonicalPath

                fdto.isFile = file.isFile
                fdto.isDir = file.isDirectory
                fdto.sizeInBytes = file.length()  //对于文件夹，只能读出文件夹本身占的大小(4kb or 8kb之类的)，不会计算内部文件大小总和
//            fdto.mime = MimeType.guessFromPath(if(fdto.isDir) fdto.fullPath+MimeType.separator else fdto.fullPath)  //这个MimeType类依赖路径末尾的分隔符来判断类型，但默认我存的路径末尾都没分隔符，所以判断下，如果是目录，给它加上分隔符
                fdto.mime = MimeType.guessFromFile(file)
//            fdto.mime = MimeType.guessFromFileName(fdto.name)

                fdto.isHidden = file.isHidden

                if(fdto.isDir) {  // 目录，没有大小，暂不递归计算文件大小，先显示："[Folder]"
                    fdto.sizeInHumanReadable = "["+ appContext.getString(R.string.folder) +"]"  //文件夹大小不是递归显示内部文件总和，所以显示没意义，改成Folder，指示条目是个目录，让界面看着不那么空，就行了
                }else {  //读文件大小，但不读目录大小
                    fdto.sizeInHumanReadable = getHumanReadableSizeStr(fdto.sizeInBytes)
                }


                val fileAttributes = getFileAttributes(file.canonicalPath)
//            fdto.lastModifiedTimeInSec = file.lastModified()/1000  //File的lastModified() 默认返回的是毫秒数，需要转换成秒数，不然后面的日期格式化会出问题
                fdto.lastModifiedTimeInSec = fileAttributes?.lastModifiedTime()?.to(TimeUnit.SECONDS) ?: 0
//            println("fdto.lastModifiedTimeInSec == file.lastModified()/1000:"+(fdto.lastModifiedTimeInSec ==(file.lastModified()/1000) ))  // true
                fdto.createTimeInSec = fileAttributes?.creationTime()?.to(TimeUnit.SECONDS) ?: 0
                fdto.lastModifiedTime = getFormatTimeFromSec(fdto.lastModifiedTimeInSec, offset = getSystemDefaultTimeZoneOffset())
                fdto.createTime = getFormatTimeFromSec(fdto.createTimeInSec, offset = getSystemDefaultTimeZoneOffset())

            }catch (e:Exception) {
                MyLog.e(TAG, "#updateFileItemDto err: ${e.localizedMessage}")
            }
        }
    }
}
