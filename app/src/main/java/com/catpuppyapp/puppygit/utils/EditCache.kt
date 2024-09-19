package com.catpuppyapp.puppygit.utils

import android.util.Log
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.io.BufferedWriter
import java.io.File
import java.io.FileWriter
import java.io.IOException
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

object EditCache {
    private val TAG = "EditCache"  //debug TAG

    private var enable = true  //是否启用EditCache。（这的赋值只是初始值，实际会在init方法里更新此值，那个值才是真正有效的，而init的值与设置项对应条目关联）

    private val writeLock = Mutex()
    private const val channelBufferSize = 50  //队列设置大一些才有意义，不然跟互斥锁没差，话说kotlin里没公平锁吗？非得这么麻烦
    //溢出则挂起（阻塞）
    private val writeChannel = Channel<String> (capacity = channelBufferSize, onBufferOverflow = BufferOverflow.SUSPEND)

    //    private static String MYLOG_PATH_SDCARD_DIR = "log";// 日志文件在sdcard中的路径
    private var keepInDays = 3 // sd卡中日志文件的最多保存天数
    private const val fileNameTag = "EditCache" // 本类输出的日志文件名称
    private const val fileExt = ".txt"
    private const val fileNameSeparator = "#"
    private val timestampFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss") // 日志的输出格式
    private val fileNameTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd") // 日志文件格式
    private var targetFile:File?=null

    private var writer:BufferedWriter?=null

    //指示当前类是否完成初始化的变量，若未初始化，意味着没设置必须的参数，这时候无法记日志
    var isInited = false
        private set

    private var cacheDir:File?=null
        get() {
            //没初始化
//            if(field==null) {
//                return null
//            }

            //若field不为null，检查目录是否存在，若不存在则创建
            if(field != null) {
                val f= field!!
                //若日志目录不存在则创建
                if(!f.exists()) {
                    f.mkdirs()
                }
            }

            //正常来说这里返回的应该是一定存在的目录或者null，因为上面不存在则创建了
            return field
        }


    //    public Context context;
    fun init(keepInDays: Int, cacheDirPath:String, enableCache:Boolean) {
        try {
            //这两个变量删除过期文件需要用，所以无论是否启用cache，都初始化这两个变量
            this.keepInDays = keepInDays
            this.cacheDir = File(cacheDirPath)

            //只有启用cache才有必要初始化writer，否则根本不会写入文件，自然也不需要初始化writer
            enable = enableCache
            if(enable) {
                initWriter()
                startWriter()
                isInited=true
            }else {
                //禁用cache的情况下，不会初始化writer，isInited应为false从而避免调用writer
                isInited=false
            }
        }catch (e:Exception) {
            isInited=false

            try {
                e.printStackTrace()
                Log.e(TAG, "#init err:"+e.stackTraceToString())
            }catch (e2:Exception) {
                e2.printStackTrace()
            }

        }
    }

    private fun startWriter() {
        doJobThenOffLoading {
            //出错n次则不再执行
            var errCountLimit = 3

            while (errCountLimit > 0) {
                try {
                    writeLock.withLock {

                        val textWillWrite = writeChannel.receive()

                        //如果要写入的文件不存在，重新初始化writer，删除缓存后会发生这种情况，或者用户手动删除了PuppyGit-Data目录
                        if (targetFile?.exists() != true) {
                            initWriter()
                        }

                        writer?.write(textWillWrite+"\n")
                        writer?.flush()
                    }
                } catch (e: Exception) {
                    errCountLimit--
                    MyLog.e(TAG, "write to file err:${e.stackTraceToString()}")
                }
            }

            writeChannel.close()
        }
    }

    //获取随机文件名或者按日期获取文件名，按日期获取的就是一天一个，随机的就是每次启动app都创建一个不同的文件
    private fun getFileName(datePrefix: String = fileNameTimeFormatter.format(LocalDateTime.now()),
                            useRandomName:Boolean = false
    ): String {
        return if(useRandomName) getRandomFileName(datePrefix) else getDailyFileName(datePrefix)
    }

    // 返回值，eg: 20240515#abc123#EditCache.txt
    private fun getRandomFileName(datePrefix:String):String {
        return datePrefix + fileNameSeparator + getShortUUID(len = 6) + fileNameSeparator + fileNameTag + fileExt
    }

    // 返回值，eg: 20240515#EditCache.txt，考虑了下，感觉还是一天建一个EditCache就够了，所以不用随机的了
    private fun getDailyFileName(datePrefix:String):String {
        return datePrefix + fileNameSeparator + fileNameTag + fileExt

    }
    //    private static long getSecFromTime(LocalDateTime time) {
    //        return time.toEpochSecond(timeZoneOffset);
    //    }
    //
    //    private static long getNowInSec() {
    //        return getSecFromTime(LocalDateTime.now());
    //    }
    /**
     * @param text  要写入的内容
     */
    suspend fun writeToFile(text: String) {
        try {
            //只有初始化成功且启用了edit cache的情况下，才记录cache
            if(!isInited || !enable) {
                return
            }

            //忽略空行
            if (text.isBlank()) {
                return
            }

            //初始化完毕并且启用cache，则写入内容到cache
            val formattedTimeStr = timestampFormatter.format(LocalDateTime.now())
            val textWillWrite = "-------- $formattedTimeStr :\n$text"

            writeChannel.send(textWillWrite)

            //用lock没法保证写入顺序，所以改用channel，或者用公平锁也行，公平锁和channel内部都有阻塞队列，所以可确保先调用的先执行
//            writeLock.withLock {
//                writer?.write(textWillWrite)
//                writer?.newLine()
//                writer?.flush()
//            }
        } catch (e: IOException) {
            e.printStackTrace()
            Log.e(TAG, "#writeToFile err:"+e.stackTraceToString())
        }
    }

    private fun initWriter(){
        val funName = "initWriter"

//        //这里不要做检测，cacheDir如果是null，应立即报错，避免后续调用writer
//        if(cacheDir==null) {
//            MyLog.e(TAG, "#$funName err:cacheDir is null")
//            return
//        }

        val dirsFile = cacheDir!!
        if (!dirsFile.exists()) {
            dirsFile.mkdirs()
        }


        //Log.i("创建文件","创建文件");
//        var file: File? = null
        val file = File(
            dirsFile.getCanonicalPath(),  // 存储目录
            getFileName() //文件名
        )

        //targetFile更新下，用来检测文件是否存在
        targetFile = file

        //debug
//            System.out.println("file.toString():::"+file.toString());
        //debug
        if (!file.exists()) {
            //在指定的文件夹中创建文件
            file.createNewFile()
        }

        //如果writer不为null，先关流
        try {
            writer?.close()
        }catch (e:Exception) {
            MyLog.e(TAG, "#$funName err:${e.stackTraceToString()}")
        }

        //新开一个writer
        val append = true
        val filerWriter = FileWriter(file, append) // 后面这个参数代表是不是要接上文件中原来的数据，不进行覆盖
        writer = filerWriter.buffered()
    }

    /**
     * 删除过期的日志文件
     */
    fun delExpiredFiles() { // 删除日志文件
        try {
            MyLog.i(TAG, "start: del expired '$fileNameTag' files")

            val dirPath = cacheDir!! //获取日志路径
            //获取1970年1月1日到今天的天数
            val todayInDay = LocalDate.now().toEpochDay()
            val logFileList = dirPath.listFiles()?:return
            for (f in logFileList) {  //Objects.requireNonNull(param) ，param为null则抛异常
                if (f == null) {
                    continue
                }
                try {
                    val dateStrFromFileName =
                        getDateOfFileName(f) //返回值类似 2024-04-08，和 logFileSdf 的格式必须匹配，否则会解析失败
                    val fileCreateDateInDay = LocalDate.from(fileNameTimeFormatter.parse(dateStrFromFileName)).toEpochDay()
                    val diffInDay = todayInDay - fileCreateDateInDay //计算今天和文件名日期相差的天数

                    //debug
//                    System.out.println("diffInDay:::"+diffInDay+", now:"+logfile.format(new Date())+" other:"+dateStrFromFileName);
                    //debug

                    //删除超过天数的日志文件
                    if (diffInDay > keepInDays) {
                        f.delete()
                    }
                } catch (e: Exception) {
                    //日志类初始化完毕之后才执行此方法，所以，这里可以记录日志
                    MyLog.e(TAG, "#delExpiredFiles: in for loop err: "+e.stackTraceToString())
//                    e.printStackTrace()
                    continue
                }
            }

            MyLog.i(TAG, "end: del expired '$fileNameTag' files")
        } catch (e: Exception) {
            MyLog.e(TAG, "#delExpiredFiles: err: "+e.stackTraceToString())

//            e.printStackTrace()
        }
    }

    private fun getDateOfFileName(f: File): String {
        val split = f.getName().split(fileNameSeparator)
        return split[0]
    }

    /**
     * 得到现在时间前的几天日期，用来得到需要删除的日志文件名
     */
    //用不着了，我自己换算成秒来计算哪些日志需要删除了
    //    @Deprecated
    //    private static Date getDateBefore() {
    //        Date nowtime = new Date();
    //        Calendar now = Calendar.getInstance();
    //        now.setTime(nowtime);
    //        now.set(Calendar.DATE, now.get(Calendar.DATE) - LOG_FILE_SAVE_DAYS);
    //        return now.getTime();
    //    }
    //TODO
    fun zipLogDirAndSendToEmail() {
        throw RuntimeException("Not implemented yet")
    }
}
