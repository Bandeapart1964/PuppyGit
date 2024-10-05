package com.catpuppyapp.puppygit.git

import com.catpuppyapp.puppygit.constants.Cons
import com.catpuppyapp.puppygit.utils.getParentPathEndsWithSeparator
import com.catpuppyapp.puppygit.utils.getHumanReadableSizeStr
import com.catpuppyapp.puppygit.utils.mime.MimeType
import com.catpuppyapp.puppygit.utils.mime.guessFromFileName
import java.io.File

class StatusTypeEntrySaver {
    var repoIdFromDb:String="";
    var fileName:String="";
    var relativePathUnderRepo:String="";  //仓库下的相对路径，包含文件名

    //modified, newfile,untracked, etc
    var changeType:String?=null;
    //entry item, for get delta , etc
//    var entry: Status.Entry?=null;  //这个其实不太靠谱，指针有可能被释放，
    //item canonicalPath
    var canonicalPath:String="";
    var fileSizeInBytes:Long=0;
    //file or dir or submodule
    var itemType:Int = Cons.gitItemTypeFile;
    var dirty:Boolean = false  // for submodule, if has uncommited changes, this should set to true

    private var mime:MimeType? = null

//    var subs:MutableList<StatusTypeEntrySaver> = ArrayList();


    fun getMime():MimeType {
        if(mime==null) {
            //注：MimiType依赖路径末尾的/来判断是文件夹还是文件，原本应该判断是文件夹则加个分隔符再guess Mime类型的，但是由于本类全是文件，没目录，所以这里无需做处理
            mime = MimeType.guessFromFileName(fileName)  // guessFromPath() 默认匹配不上就会返回 MimeType.TEXT_PLAIN ，所以这里不用做空值判断，也不用加空值则返回text plain的逻辑
        }
        return mime!!
    }


    fun getSizeStr():String {
        return getHumanReadableSizeStr(fileSizeInBytes)
    }

    /**
     * note: path ends with separator /
     */
    fun getParentDirStr():String{
        return getParentPathEndsWithSeparator(relativePathUnderRepo)
    }


    fun toFile(): File {
        return File(canonicalPath)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as StatusTypeEntrySaver

        if (repoIdFromDb != other.repoIdFromDb) return false
        if (fileName != other.fileName) return false
        if (relativePathUnderRepo != other.relativePathUnderRepo) return false
        if (changeType != other.changeType) return false
        if (canonicalPath != other.canonicalPath) return false
        if (fileSizeInBytes != other.fileSizeInBytes) return false
        if (itemType != other.itemType) return false

        return true
    }

    override fun hashCode(): Int {
        var result = repoIdFromDb.hashCode()
        result = 31 * result + fileName.hashCode()
        result = 31 * result + relativePathUnderRepo.hashCode()
        result = 31 * result + (changeType?.hashCode() ?: 0)
        result = 31 * result + canonicalPath.hashCode()
        result = 31 * result + fileSizeInBytes.hashCode()
        result = 31 * result + itemType
        return result
    }

}
