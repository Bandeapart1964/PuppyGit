package com.catpuppyapp.puppygit.dto

import com.catpuppyapp.puppygit.utils.MyLog
import com.catpuppyapp.puppygit.utils.getFileAttributes
import java.io.File
import java.util.concurrent.TimeUnit

private val TAG = "FileSimpleDto"
/**
 * 只包含能粗略判断文件是否修改过的字段，用来在切换编辑器时判断是否需要重载文件
 */
class FileSimpleDto {
    var name=""
    var createTime=0L
    var lastModifiedTime=0L
    var sizeInBytes =0L
    var isFile = true
    var fullPath = ""

    fun toFile():File {
        return File(fullPath)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as FileSimpleDto

        if (name != other.name) return false
        if (createTime != other.createTime) return false
        if (lastModifiedTime != other.lastModifiedTime) return false
        if (sizeInBytes != other.sizeInBytes) return false
        if (isFile != other.isFile) return false
        if (fullPath != other.fullPath) return false

        return true
    }

    override fun hashCode(): Int {
        var result = name.hashCode()
        result = 31 * result + createTime.hashCode()
        result = 31 * result + lastModifiedTime.hashCode()
        result = 31 * result + sizeInBytes.hashCode()
        result = 31 * result + isFile.hashCode()
        result = 31 * result + fullPath.hashCode()
        return result
    }

    override fun toString(): String {
        return "FileSimpleDto(name='$name', createTime=$createTime, lastModifiedTime=$lastModifiedTime, sizeInBytes=$sizeInBytes, isFile=$isFile, fullPath='$fullPath')"
    }


    companion object {

        //appContext for get string source
        fun genByFile(file: File, timeUnit: TimeUnit=TimeUnit.MILLISECONDS):FileSimpleDto {
            val fdto = FileSimpleDto()
            updateDto(fdto, file, timeUnit)
            return fdto
        }

        //这个单位精确到毫秒似乎没意义，后面3位全是0，1000毫秒是一秒，所以实际上只能精确到秒
        fun updateDto(fdto:FileSimpleDto, file: File, timeUnit: TimeUnit=TimeUnit.MILLISECONDS) {
            try {
                fdto.name = file.name
                fdto.fullPath = file.canonicalPath

                fdto.isFile = file.isFile
                fdto.sizeInBytes = file.length()  //对于文件夹，只能读出文件夹本身占的大小(4kb or 8kb之类的)，不会计算内部文件大小总和

                val fileAttributes = getFileAttributes(file.canonicalPath)
//            fdto.lastModifiedTimeInSec = file.lastModified()/1000  //File的lastModified() 默认返回的是毫秒数，需要转换成秒数，不然后面的日期格式化会出问题
                fdto.lastModifiedTime = fileAttributes?.lastModifiedTime()?.to(timeUnit) ?: 0
//            println("fdto.lastModifiedTimeInSec == file.lastModified()/1000:"+(fdto.lastModifiedTimeInSec ==(file.lastModified()/1000) ))  // true
                fdto.createTime = fileAttributes?.creationTime()?.to(timeUnit) ?: 0  // actually on linux, this same as lastModifiedTime ;(

            }catch (e:Exception) {
                MyLog.e(TAG, "#updateDto err: ${e.localizedMessage}")
            }
        }
    }
}
