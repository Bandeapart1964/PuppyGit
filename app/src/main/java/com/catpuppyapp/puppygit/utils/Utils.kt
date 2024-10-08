package com.catpuppyapp.puppygit.utils

import android.content.Context
import android.widget.Toast
import androidx.compose.runtime.MutableState
import com.catpuppyapp.puppygit.constants.Cons
import com.catpuppyapp.puppygit.data.entity.ErrorEntity
import com.catpuppyapp.puppygit.data.entity.RepoEntity
import com.catpuppyapp.puppygit.etc.Ret
import com.catpuppyapp.puppygit.play.pro.R
import com.catpuppyapp.puppygit.settings.SettingsUtil
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.net.URI
import java.nio.file.Files
import java.nio.file.Paths
import java.nio.file.attribute.BasicFileAttributes
import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.UUID
import kotlin.io.path.exists

private val TAG = "Utils"


fun showToast(context: Context, text:String, duration:Int=Toast.LENGTH_SHORT) {
    Toast.makeText(context, text, duration).show()
}

fun getRepoNameFromGitUrl(gitUrl: String):String{
    val gitIdx = gitUrl.lastIndexOf(".git")
    val urlSeparatorIdx = gitUrl.lastIndexOf("/")+1

    if(urlSeparatorIdx < gitUrl.length && urlSeparatorIdx<=gitIdx){
        val folderName = gitUrl.substring(urlSeparatorIdx, gitIdx)
        return folderName
    }else {
        return ""
    }
}

//fun rmPathSuffix(path:String, suffix:String = File.separator):String {
//    return path.removeSuffix(suffix)
//}

/**
 *  baseDir可以为null，若为null，相当于 File(subDir)，若baseDir为null，不要用空字符串替代，含义不同
 */
fun isPathExists(baseDir: String?, subDir:String):Boolean {
//    val repoDirNoSeparatorSuffix = rmPathSuffix(repoDir)
//    val subDirNoSeparatorSuffix = rmPathSuffix(subdir)
//    val file = File(repoDirNoSeparatorSuffix + File.separator + subDirNoSeparatorSuffix)
    val file = if(baseDir!=null) File(baseDir, subDir) else File(subDir)  //baseDir可为null，若为null，相当于File(subDir)，注意，这里该为null就为null，不要传空字符串，空字符串和null作为basedir时，行为不同
    return file.exists()
}

fun strHasSpaceChar(str:String):Boolean {
    for(c in str) {
        if(c.isWhitespace()) {
            return true
        }
    }

    return false
}

fun strHasIllegalChars(str:String):Boolean {
    //如果包含这些字符，返回true。p.s. 这些字符来自于windows的创建文件名提示
    if(str.contains("/") || str.contains("\\") ||str.contains('?')|| str.contains(File.separator) || str.contains(File.pathSeparatorChar)
        //%2F 是 路径分隔符/ 的转义字符，如果存在这个字符，文件路径就废了，用java或c打开都可能报错
        || str.contains("*")|| str.contains("<") || str.contains(">") ||  str.contains("|") ||  str.contains("\"")
    ) {

        return true
    }

    return false
}

/**
 * 如果文件名包含“坏”字符或者文件名无法创建（比如包含非法路径字符），则返回true，否则返回false
 */
fun checkFileOrFolderNameAndTryCreateFile(nameWillCheck:String, appContext: Context):Ret<String?> {
    val funName="checkFileOrFolderNameAndTryCreateFile"

    //检测文件名，然后尝试创建文件，如果失败，说明存在非法字符
    try{
        //检测文件名是否为空
        if(nameWillCheck.isEmpty()) {
            throw RuntimeException(appContext.getString(R.string.err_name_is_empty))
        }

        //检测是否包含非法字符
        //如果包含这些字符，返回true。p.s. 这些字符来自于windows的创建文件名提示
        if(strHasIllegalChars(nameWillCheck)) {
            throw RuntimeException(appContext.getString(R.string.error_has_illegal_chars))
        }

        //获取缓存目录
        val cacheDir = AppModel.singleInstanceHolder.externalCacheDir
//        val fileNameNeedTest = Cons.createDirTestNamePrefix + str +"_"+ getRandomUUID()  //e.g. prefix_yourreponame_uuid，uuid是为了避免文件夹存在

        //拼接文件名
        val fileNameNeedTest = Cons.createDirTestNamePrefix + nameWillCheck;  //e.g. prefix_yourreponame
        //拼接文件完整路径
        val path = cacheDir.canonicalPath + File.separator + fileNameNeedTest

        //创建文件
        val file = File(path)
        val createSuccess = file.createNewFile()  //若文件已经存在，有可能返回false，用结果判断下，只有是我创建的情况下才删除
        //检测文件是否存在，不管上面创建成功与否，只要存在就说明是合法文件名，所以应返回假。
        if(file.exists()) {
            //如果创建成功，则删除；如果创建失败且能执行到这里，说明文件已存在但不是我创建的，那就不删，当然这样有可能导致缓存目录有无效文件，不过无所谓，顶多清下app缓存就行了，而且即使不这样做也有可能缓存目录存在无效文件，例如执行完创建后，app进程被杀，就存在无效文件了
            if(createSuccess) {  //执行到这，文件存在且是我创建的，删除
                file.delete()
            }else {  //文件存在但不是我这次创建的，可能之前创建的没成功删掉，也可能用户创建的，不删了，显示个警告。注：用户可通过app的清理缓存(20240601，还没实现)或者系统应用程序信息界面的清理缓存清掉这个在cache目录的文件
                MyLog.w(TAG, "#$funName: warn: may has invalid file '${file.name}' in cache dir, try clear app cache if you don't know that file")
            }

            return Ret.createSuccess(null)  //文件存在，说明不包含非法字符
        }

        //上面没返回，可能创建失败
        throw RuntimeException(appContext.getString(R.string.error_cant_create_file_with_this_name))
    }catch (e:Exception) {
        MyLog.e(TAG, "#$funName err: ${e.localizedMessage}")

        return Ret.createError(null, e.localizedMessage ?: appContext.getString(R.string.unknown_err_plz_try_another_name))
    }
}


//返回值示例：includeSeparator=true,36位字符:171885d6-93b5-497a-8b9b-17e58ac99138
//          includeSeparator=false，32位字符:171885d693b5497a8b9b17e58ac99138
fun getRandomUUID(includeSeparator:Boolean=false):String {
    return if(includeSeparator) getRandomUUIDWithSeparator()
            else  getRandomUUIDNoSeparator()
}

//返回值示例(32个字符+4个分隔符=36个字符)：171885d6-93b5-497a-8b9b-17e58ac99138
private fun getRandomUUIDWithSeparator():String {
    return UUID.randomUUID().toString();
}
//返回值示例(32个字符)：171885d693b5497a8b9b17e58ac99138
private fun getRandomUUIDNoSeparator():String {
    return UUID.randomUUID().toString().replace("-","");
}

//返回值示例: 171885d617e58ac99138
//注：这个len实际不能超过32，不然要改代码，改成拼接UUID直到满足长度，太麻烦了也没必要
fun getShortUUID(len:Int=20):String {
    var actuallyLen = len
    if(len>32) {
        actuallyLen=32
    }
    return getRandomUUID(includeSeparator = false).substring(0, actuallyLen)
}

fun dbIntToBool(v:Int):Boolean {
    return v != Cons.dbCommonFalse
}

fun boolToDbInt(b:Boolean):Int {
    return if(b) Cons.dbCommonTrue else Cons.dbCommonFalse
}

//测试了下，这个time参数默认值会在每次调用时函数时重新调用LocalDateTime.now()，和预期一样，无bug
fun getSecFromTime(time:LocalDateTime=LocalDateTime.now(), offset:ZoneOffset=Cons.dbUsedTimeZoneOffset):Long {
//    return Instant.now().epochSecond
//    return ZonedDateTime.now().toEpochSecond()
//    LocalDateTime没有时区，单纯存的时间，存储和读取使用相同的time offset就行
    //20240408：这里统一用的UTC时区，不太确定能不能在所有设备上正确显示时间，
    // 如果不能，在settings表加个时区字段(settings表不用迁移db，而且如果想显示在设置页面让用户能设置，这个表也正合适)，
    // 默认获取用户设备的默认时区（最好用户也可手动设置），然后把getTimeFromSec()函数改下，
    // 改成获取时间时加上时区对应的偏移量，比如 UTC+8，就加上8个小时的偏移量(可能需要把小时转换成分钟或秒)
    return time.toEpochSecond(offset)
}

fun getTimeFromSec(sec:Long, offset:ZoneOffset=Cons.dbUsedTimeZoneOffset):LocalDateTime {

//    return ZonedDateTime.ofInstant(Instant.ofEpochSecond(sec),ZoneId.systemDefault())
//    return LocalDateTime.ofInstant(Instant.ofEpochSecond(sec),ZoneId.systemDefault())
    return LocalDateTime.ofEpochSecond(sec, 0, offset)
}

fun getFormatTimeFromSec(sec:Long, formatter:DateTimeFormatter = Cons.defaultDateTimeFormatter, offset: ZoneOffset=Cons.dbUsedTimeZoneOffset):String {
    val timeFromSec = getTimeFromSec(sec, offset)
//    val zonedDateTime = ZonedDateTime.from(localDateTimeFromSec)
    return formatter.format(timeFromSec)
}

fun getNowInSecFormatted(formatter:DateTimeFormatter = Cons.defaultDateTimeFormatter):String {
    return getFormatTimeFromSec(getSecFromTime(), formatter)
}

fun getSystemDefaultTimeZoneOffset() :ZoneOffset{
//    return ZoneOffset.of(ZoneOffset.systemDefault().id)
    return OffsetDateTime.now().offset
}

//转换天数到秒数
fun daysToSec(days:Int) :Long{
    return (days * 24 * 60 * 60).toLong()
}

//20240425 测试了下，出错时能显示出Toast，不过不能用Msg.requireShow()，要直接用Toast.make().show()
private fun getDirIfNullThenShowToastAndThrowException(context:Context, dir:File?, errMsg:String):File {
    if(dir==null) {
        showToast(context, errMsg, Toast.LENGTH_LONG)
        throw RuntimeException(errMsg)
    }else {
        if(!dir.exists()) {
            dir.mkdirs()
        }
        return dir;
    }
}

fun getExternalFilesDirOrThrowException(context:Context):File {
    val dir = context.getExternalFilesDir(null)
    return getDirIfNullThenShowToastAndThrowException(context, dir, Cons.errorCantGetExternalDir)
}

fun getExternalCacheDirOrThrowException(context:Context):File {
    val dir = context.externalCacheDir
    return getDirIfNullThenShowToastAndThrowException(context, dir, Cons.errorCantGetExternalCacheDir)
}
fun getInnerDataDirOrThrowException(context:Context):File {
    val dir = context.dataDir
    return getDirIfNullThenShowToastAndThrowException(context, dir, Cons.errorCantGetInnerDataDir)
}

//fun createAllRepoParentDirIfNonexists(baseDir:File, allRepoParentDir:String=Cons.defaultAllRepoParentDirName):File {
//    return createDirIfNonexists(baseDir, allRepoParentDir)
//}

//fun createFileSnapshotDirIfNonexists(baseDir:File, dirName:String=Cons.defaultFileSnapshotDirName):File {
//    return createDirIfNonexists(baseDir, dirName)
//}

//fun createLogDirIfNonexists(baseDir:File, dirName:String=Cons.logDirName):File {
//    return createDirIfNonexists(baseDir, dirName)
//}

fun createDirIfNonexists(baseDir:File, subDirName:String):File {
//    val dir = File(baseDir.canonicalPath + File.separator + subDirName)
    val dir = File(baseDir.canonicalPath, subDirName)
    if(!dir.exists()) {
        dir.mkdirs()
    }
    return dir
}

fun deleteIfFileOrDirExist(f: File):Boolean {
    if(f.exists()) {
        return f.deleteRecursively()
    }
    return true;
}

fun isFileSizeOverLimit(size:Long, limit:Long=SettingsUtil.getSettingsSnapshot().editor.maxFileSizeLimit) :Boolean {
    return isSizeOverLimit(size = size, limitMax = limit)
}

fun isDiffContentSizeOverLimit(size:Long, limit:Long=SettingsUtil.getSettingsSnapshot().diff.diffContentSizeMaxLimit) :Boolean {
    return isSizeOverLimit(size = size, limitMax = limit)
}

/**
 * @return if limitMax is 0, meant no limit, return false; else return (size > limitMax) 's result
 */
fun isSizeOverLimit(size:Long, limitMax:Long):Boolean {
    // 0 = no limit
    if(limitMax == 0L) {
        return false
    }

    return size > limitMax
}

/**
 * @return e.g.: input "abc" or "/path/to/abc" or "/path/to/abc/" or "path/to/abc"，return "abc";
 *          input "abc//" or other bad path, return origin path;
 *          if err, return origin path
 */
fun getFileNameFromCanonicalPath(path:String, separator:String=File.separator):String {
    try {
        val pathRemovedSuffix = path.removeSuffix(separator)  //为目录移除末尾的/，如果有的话

        val lastSeparatorIndex = pathRemovedSuffix.lastIndexOf(separator)  //找出最后一个/的位置

        //无法取出文件名则返回原字符串
        //没找到/ 或 无效路径格式(上面去了个/，末尾还有个/，说明原字符串末尾至少两个/，所以是无效路径)
        if(lastSeparatorIndex == -1 || lastSeparatorIndex == pathRemovedSuffix.length-1) {
            return path
        }

        //有效路径，返回目录或文件名
        return pathRemovedSuffix.substring(lastSeparatorIndex+1, pathRemovedSuffix.length)
    }catch (e:Exception) {
        MyLog.e(TAG, "#getFileNameFromCanonicalPath err: path=$path, separator=$separator, err=${e.localizedMessage}")
        return path
    }
}

//输入 eg: /sdcard/Android/data/com.pack/files/allRepoDir/Repo/dir/etc
//return eg: allRepoDir/Repo/dir/etc
fun getFilePathStrBasedAllRepoDir(path:String):String {
    var ret = ""

    val allRepoBaseDirParentFullPath = AppModel.singleInstanceHolder.allRepoParentDir.parent?:""  //不知道parent是否以/结尾，如果是，后面解析出的内容会是 非/开头，直接返回即可，否则会是/开头，需要删除开头的/再返回
    val allRepoBaseIndexOf = path.indexOf(allRepoBaseDirParentFullPath)
    if(allRepoBaseIndexOf!=-1) {
        val underAllRepoBaseDirPathStartAt = allRepoBaseIndexOf + allRepoBaseDirParentFullPath.length
        if(underAllRepoBaseDirPathStartAt < path.length) {
            var pathBaseAllRepoDir = path.substring(underAllRepoBaseDirPathStartAt)  //获取 allRepoDir/Repo/dir/etc ，但不知道开头有没有/
//            if(pathBaseAllRepoDir.startsWith(File.separator)) {  //不太确定上面的api的返回值，如果allRepoBaseDirFullPath末尾没"/"，那解析出的字符串则会以"/" 开头，删除一下/。(后来测试了末尾不会包含 /)
//                if(pathBaseAllRepoDir.length>=2) {  //确保不会index out of bounds
//                    pathBaseAllRepoDir =  pathBaseAllRepoDir.substring(1)  // 返回不包含 开头 / 的内容
//                }else {  //解析完，若只有 /，则返回空字符串
//                    pathBaseAllRepoDir =  ""  //解析完只有/，返回空字符串
//                }
//            }
            ret = pathBaseAllRepoDir  //如果返回的字符串不是以/开头，直接返回
        }

    }
    return ret.removePrefix(File.separator).removeSuffix(File.separator)  //移除末尾和开头的 /
}

//输入全路径，返回仓库下路径，但不包含仓库名。
// 输入输出举例：
// 例1：输入：/sdcard/Android/data/com.pack/AllRepoBaseDir/Repo1/file1 输出：("Repo1", "file1")
// 例2：输入：/sdcard/Android/data/com.pack/AllRepoBaseDir/Repo1 输出：("Repo1",")
// 例3：输入：/sdcard/Android/data/com.pack/AllRepoBaseDir/Repo1/ 输出：("Repo1",")
// 例4：输入：/sdcard/Android/data/com.pack/files/allRepoDir/Repo/dir/etc ，返回 ("Repo","dir/etc")
fun getFilePathStrUnderRepoByFullPath(fullPath:String):Pair<String,String> {
    //假设输入：/sdcard/Android/data/com.pack/files/allRepoDir/Repo/dir/etc

    var repoFullPath = ""
    var relativePathUnderRepo = ""
    val filePathStrBasedAllRepoDir = getFilePathStrBasedAllRepoDir(fullPath)  //获取 allRepoDir/Repo/dir/etc
    if(filePathStrBasedAllRepoDir.isNotBlank()) {
        val firstSeparatorIndex = filePathStrBasedAllRepoDir.indexOf(File.separator)
        val cutAllRepoDirIndex = firstSeparatorIndex + 1  //计算 Repo/dir/etc 在 allRepoDir/Repo/dir/etc 中的起始索引
        if(cutAllRepoDirIndex!=0 && cutAllRepoDirIndex < filePathStrBasedAllRepoDir.length) {  //索引加了1，如果还是0，说明没找到对应字符串
            val repoPathUnderAllRepoBase = filePathStrBasedAllRepoDir.substring(cutAllRepoDirIndex)  //获取 Repo/dir/etc
            val finallyStrIndex = repoPathUnderAllRepoBase.indexOf(File.separator)+1  //计算 dir/etc 在 Repo/dir/etc 中的起始索引
            if(finallyStrIndex!=0 && finallyStrIndex < repoPathUnderAllRepoBase.length) {
                val repoNameEndsWithSeparator = repoPathUnderAllRepoBase.substring(0, finallyStrIndex)  //取出 Repo/
                repoFullPath = File(AppModel.singleInstanceHolder.allRepoParentDir.canonicalPath, repoNameEndsWithSeparator).canonicalPath  //canonicalPath 返回的结果末尾就没 / 了，结果应为 Repo
                relativePathUnderRepo = repoPathUnderAllRepoBase.substring(finallyStrIndex).removePrefix(File.separator).removeSuffix(File.separator)  //返回 dir/etc
            }
        }


    }

    return Pair(repoFullPath, relativePathUnderRepo)
}

//移除子目录的父目录前缀
//输入： /a/b/c/, /a/b/c/d/e ，返回 d/e (注意：会移除开头和末尾的 /，不管是否是目录，返回的结果一律开头和末尾都没/，调用者应该自己知道传入的路径是个目录还是文件，不需要通过这里的返回值末尾是否包含/来判断)
//输入： /a/b/c, /a/b/c/d/e/ ，返回 d/e
//输入： /a/b/c, /a/b/c ，返回 空字符串
//输入： /a/b/c, /a/b/c/ ，返回 空字符串
//输入： a/b/c, /a/b/c/d/e/ ，返回空字符串""，因为开头不匹配，本函数不对入参开头的 / 做处理
fun getFilePathUnderParent(parentFullPath:String, subFullPath:String) :String {
    if(parentFullPath.isBlank() || subFullPath.isBlank()
        ||subFullPath.length <= parentFullPath.length
        ) {
        return ""
    }

    val indexOf = subFullPath.indexOf(parentFullPath)

    //等于-1代表没找到，后面的不等于0代表在子中找到了父，但不是在开头
    if(indexOf==-1 || indexOf!=0) {  //子目录必须包含父目录且开头匹配
        return ""
    }
//    val startIndex = indexOf + parentFullPath.length  //因为上面indexOf不等于0就直接返回了，所以执行到这indexOf肯定是0，所以就不必和父路径长度相加了，直接用父长度即可
    val startIndex = parentFullPath.length
    if(startIndex >= subFullPath.length){
        return ""
    }

    return subFullPath.substring(startIndex).removePrefix(File.separator).removeSuffix(File.separator)
}

//输入 eg: /sdcard/Android/data/youpackagename/file/AllRepoBaseDir/Repo/dir/etc
//输出 eg: Repo/dir/etc
//如果输入是allRepoDir，则返回 空字符串
fun getFilePathStrBasedRepoDir(path:String, returnResultStartsWithSeparator:Boolean=false):String {
    val path2 = getFilePathStrBasedAllRepoDir(path)
    val firstIdxOfSeparator = path2.indexOf(File.separator)  //不如直接用 /
    val isAllRepoDir = firstIdxOfSeparator==-1
    var result= if(isAllRepoDir) "" else path2.substring(firstIdxOfSeparator+1)

    //检查是否期望结果以/开头，如果期望，则检查是否以/开头，不是则添加；若不期望则检查是否以/开头，是则移除
    if(returnResultStartsWithSeparator) {
        if(!result.startsWith(File.separator)) {
            result = File.separator+result
        }
    }else {
        if(result.startsWith(File.separator)) {
            result = result.removePrefix(File.separator)
        }
    }

    return result
}

//input就是git status输出的那种仓库内相对路径+文件名，例如 dir1/dir2/file.txt ，输出则返回 dir1/dir2/；如果输入是file.txt，则返回 /
//第2个参数传true适用于解析仓库相对路径的场景，因为仓库相对路径下仓库根目录下的文件没有/但其归属于/，这里的/代表仓库根目录；为false则会在查找不到路径分隔符时返回原path（入参1），这种情况暂无应用场景
/**
 * @param path src path, will try get parent path for it
 * @param trueWhenNoParentReturnSeparatorFalseReturnPath true, when parent path is empty will return separator like "/" ; else return `path`. will ignore this param if `trueWhenNoParentReturnEmpty` is true
 * @param trueWhenNoParentReturnEmpty when parent path is empty return empty, if false, return what depend by `trueWhenNoParentReturnSeparatorFalseReturnPath`
 */
fun getParentPathEndsWithSeparator(path:String, trueWhenNoParentReturnSeparatorFalseReturnPath:Boolean=true, trueWhenNoParentReturnEmpty:Boolean=false):String {
    try {
        val separator = File.separator
        val lastIndexOfSeparator = path.lastIndexOf(separator)
        if(lastIndexOfSeparator != -1) {  // found "/", has a parent path
            return path.substring(0, lastIndexOfSeparator+1)  // +1把/本身包含上
        }else {  // not found "/", no parent path yet
            //没/，可能是根目录？话说我当初为什么没找到让它返回/？
            // 啊，对了，因为是根据仓库根目录设置的，如果有个文件在仓库根目录，
            // 其仓库相对路径就是 filename，这时如果找不到/，说明是根目录
            if(trueWhenNoParentReturnEmpty) {
                return ""
            }

            return if(trueWhenNoParentReturnSeparatorFalseReturnPath) separator else path
        }

    }catch (e:Exception) {
        MyLog.e(TAG, "#getParentPathEndsWithSeparator err: path=$path, trueWhenNoParentReturnSeparatorFalseReturnPath=$trueWhenNoParentReturnSeparatorFalseReturnPath, trueWhenNoParentReturnEmpty=$trueWhenNoParentReturnEmpty, err=${e.localizedMessage}")
        //发生异常一律return path合适吗？，没什么不合适的，虽然可能会有些奇怪，但在界面能看出问题 且 用户也感觉没太大异常，嗯，就这样吧
        //发生异常return 原path
        return path
    }
}

//fun encodeStrUri(input:String):String {
//    return input.replace("/",Cons.separatorReplaceStr);  //encodeURIComponent("/"), return "%2F"
//}
//fun decodeStrUri(input:String):String {
//    return input.replace(Cons.separatorReplaceStr,"/")
//}

fun isRepoReadyAndPathExist(r: RepoEntity?): Boolean {
    if(r==null) {
        return false
    }

    if (r.workStatus != Cons.dbRepoWorkStatusNotReadyNeedClone
        && r.workStatus != Cons.dbRepoWorkStatusNotReadyNeedInit
        && r.isActive == Cons.dbCommonTrue
        && (r.fullSavePath ?: "").isNotBlank()
    ) {
        val f = File(r.fullSavePath)
        if (f.exists()) {
            return true;
        }
    }
    return false
}

fun setErrMsgForTriggerNotify(hasErrState:MutableState<Boolean>,errMsgState:MutableState<String>,errMsg:String) {
    hasErrState.value=true;
    errMsgState.value=errMsg;
}

//如果只传job，则没loading，单纯执行job
//x 20240426修改，即使发生异常，也可解除loading)发生异常会无法解除loading，考虑到避免用户在发生错误后继续操作，所以没做处理
fun doJobThenOffLoading(
    loadingOn: (String)->Unit={},
    loadingOff: ()->Unit={},
    loadingText: String="Loading...",  //这个最好别使用appContext.getString(R.string.loading)，万一appContext都还没初始化就调用此方法，会报错，不过目前20240426为止，只有在appContext赋值给AppModel对应字段后才会调用此方法，所以实际上没我担心的这个问题，根本不会发生
    coroutineDispatcher: CoroutineDispatcher = Dispatchers.IO,
    job: suspend ()->Unit
): Job? {
    return try {
        CoroutineScope(coroutineDispatcher).launch {
            //开启loading
            try {
                loadingOn(loadingText)
            }catch (e:Exception) {
                Msg.requireShowLongDuration("loading on err:"+e.localizedMessage)
                MyLog.e(TAG, "#doJobThenOffLoading(): #loadingOn error!\n" + e.stackTraceToString())
            }finally {
                //执行操作
                try {
                    job()
                }catch (e:Exception) {
                    Msg.requireShowLongDuration("do job err:"+e.localizedMessage)
                    MyLog.e(TAG, "#doJobThenOffLoading(): #job error!\n" + e.stackTraceToString())
                }finally {
                    try {
                        //最后解除loading
                        loadingOff()  //x 20240426job被trycatch包裹，实际上这个已经是百分百会解除了，索性放到finally里，百分百解除的意义更明确)这个要不要放到finally里？要不然一出异常，loading就无法解除了，不过解除不了也好，省得用户误操作
                    }catch (e:Exception) {
                        Msg.requireShowLongDuration("loading off err:"+e.localizedMessage)
                        MyLog.e(TAG, "#doJobThenOffLoading(): #loadingOff error!\n" + e.stackTraceToString())
                    }
                }
            }
        }
    }catch (e:Exception) {
        Msg.requireShowLongDuration("coroutine err:"+e.localizedMessage)
        MyLog.e(TAG, "#doJobThenOffLoading(): #launch error!\n" + e.stackTraceToString())
        null
    }

}
//fun doJobThenOffLoadingWith1Param(loadingOn:()->Unit={},loadingOff: ()->Unit={},job:suspend (Any)->Any, param:Any) {
//    CoroutineScope(Dispatchers.IO).launch {
//        try {
//            //开启loading
//            loadingOn()
//
//            //执行操作
//            job(param)
//
//            //最后解除loading
//            loadingOff()
//        }catch (e:Exception){
//            e.printStackTrace()
//            MyLog.e(TAG, "#doJobThenOffLoading():" + e.stackTraceToString)
//        }
//    }
//}

//替换string resource 中的placeholder为目标字符
private fun replaceStringRes(strRes:String, placeHolderCount:Int, strWillReplaced:String):String {
    return strRes.replace(Cons.placeholderPrefixForStrRes+placeHolderCount, strWillReplaced)
}
//替换string resource 中的placeholder为目标字符
fun replaceStringResList(strRes:String, strWillReplacedList:List<String>):String {
    var ret=""
    for((idx, str) in strWillReplacedList.withIndex()) {
        val idxPlus1 = idx+1
        //第一次循环时，使用初始的字符串
        if(idxPlus1 == 1){
            ret = replaceStringRes(strRes,idxPlus1,str)
        }else { //后续循环使用已赋值的字符串
            ret = replaceStringRes(ret,idxPlus1,str)
        }
    }

    return ret;
}

fun getStrShorterThanLimitLength(src:String, limit:Int=12):String {
    return if(src.length<limit) src else src.substring(0, limit)+"..."
}

suspend fun createAndInsertError(repoId:String, errMsg: String) {
    if(repoId.isBlank() || errMsg.isEmpty()) {
        return
    }

    val errDb = AppModel.singleInstanceHolder.dbContainer.errorRepository
    errDb.insert(
        ErrorEntity(
            msg=errMsg,
            repoId = repoId,
            date = getNowInSecFormatted()
        )
    )

    //更新repo表相关字段
    val repoDb = AppModel.singleInstanceHolder.dbContainer.repoRepository
    repoDb.setNewErrMsg(repoId, errMsg)
}

//做3件事：1记录错误信息到日志文件 2显示错误信息 3保存错误信息到数据库
suspend fun showErrAndSaveLog(logTag:String, logMsg:String, showMsg:String, showMsgMethod:(String)->Unit, repoId:String, errMsgForErrDb:String = showMsg) {
    //记录到日志
    MyLog.e(logTag,logMsg)
    //显示提示
    showMsgMethod(showMsg)
    //保存数据库(给用户看的，消息尽量简单些)
    createAndInsertError(repoId, errMsgForErrDb)

}

fun getHumanReadableSizeStr(size:Long):String {
    var s:Double=0.0;
    var unit = ""
    if(size >= Cons.sizeTB) {  //1TB
        s=size.toDouble()/Cons.sizeTB
        unit = Cons.sizeTBHumanRead
    }else if(size >= Cons.sizeGB) {
        s=size.toDouble()/Cons.sizeGB
        unit = Cons.sizeGBHumanRead
    }else if(size >= Cons.sizeMB) {
        s=size.toDouble()/Cons.sizeMB
        unit = Cons.sizeMBHumanRead
    }else if(size >= Cons.sizeKB) {
        s=size.toDouble()/Cons.sizeKB
        unit = Cons.sizeKBHumanRead
    }else {
        //整字节
        return size.toString()+Cons.sizeBHumanRead
    }

    //大于1000字节
    return "%.2f".format(s) + unit
}

fun getFileAttributes(pathToFile:String): BasicFileAttributes? {
    try {
        val filePath = Paths.get(pathToFile)
        // if file doesn't exist, unable to read file attributes, so just return null
        if(!filePath.exists()) return null  //may the filePath.exist() will not follow symbolic link? I am not sure

        val attributes: BasicFileAttributes = Files.readAttributes(filePath, BasicFileAttributes::class.java)
        return attributes
    }catch (e:Exception) {
        MyLog.e(TAG, "#getFileAttributes err: pathToFile=$pathToFile, err=${e.localizedMessage}")
        return null
    }
}

fun doJobWithMainContext(job:()->Unit) {
    doJobThenOffLoading(coroutineDispatcher = Dispatchers.Main) {
        job()
    }
}

//这个和 doJobWithMainContext 的区别在于，你得自己创建个协程，然后执行job，适合已经存在协程只需要在Main上下文里执行某些操作的场景
//注：naviUp和showToast之类依赖Main线程上下文的操作应该在Dispatchers.Main里执行
suspend fun withMainContext(job:()->Unit) {
    withContext(Dispatchers.Main) {
        job()
    }
}

//对字符串添加前缀，应用场景举例：对下拉列表已选中条目进行区分(比如changelist，点击切换仓库，给目前正在使用的仓库名前面加个星号)
fun addPrefix(str: String, prefix:String="*"):String {
    return prefix+str
}

//根据索引取出list中对应元素执行操作(入参act)，如果操作成功，返回包含修改后(如果act修改了元素的话)的元素的Ret对象，否则返回包含错误信息的Ret
fun<T> doActIfIndexGood(idx:Int, list:List<T>, act:(T)-> Unit):Ret<T?> {
    try {
        if(idx>=0 && idx<list.size) {
            val item = list[idx]
            act(item)
            return Ret.createSuccess(item)
        }
        return Ret.createError(null, "err:invalid index for list", Ret.ErrCode.invalidIdxForList)
    }catch (e:Exception) {
        MyLog.e(TAG, "#doActIfIndexGood() err:"+e.stackTraceToString())
        return Ret.createError(null, "err:"+e.localizedMessage, Ret.ErrCode.doActForItemErr)
    }

}

//获取一个安全索引或-1。如果list为空，返回-1；否则返回一个不会越界的索引
fun getSafeIndexOfListOrNegativeOne(indexWillCheck:Int, listSize:Int):Int {
    //list为空，返回-1
    if(listSize<=0) {
        return -1
    }

    //确保index不小于0 且 不大于listSize-1
    return indexWillCheck.coerceAtLeast(0).coerceAtMost(listSize - 1)
}

fun <T> isGoodIndexForList(index:Int, list:List<T>):Boolean {
    return index >= 0 && index < list.size
}
fun isGoodIndexForStr(index:Int, str:String):Boolean {
    return index >= 0 && index < str.length
}

fun getDomainByUrl(url:String):String {
    try {
        return URI.create(url).host ?: ""
    }catch (e:Exception) {
        MyLog.e(TAG, "#getDomainByUrl err: url=$url, err=${e.localizedMessage}")
        return ""
    }
}
