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


//网上拷来的，来源: https://www.cnblogs.com/changyiqiang/p/11225350.html
object MyLog {
    private val TAG = "MyLog"  //debug TAG

    private const val MYLOG_SWITCH = true // 日志文件总开关
    private const val MYLOG_WRITE_TO_FILE = true // 日志写入文件开关
    private var myLogLevel = 'i' // 日志等级，w代表只输出告警信息等，v代表输出所有信息, log level is err>warn>info>debug>verbose, low level include high level output
    private val writeLock = Mutex()
    private const val channelBufferSize = 50  //队列设置大一些才有意义，不然跟互斥锁没差，话说kotlin里没公平锁吗？非得这么麻烦
    private val writeChannel = Channel<String> (capacity = channelBufferSize, onBufferOverflow = BufferOverflow.SUSPEND) { /* onUndeliveredElement, 未交付的元素会调用此方法，一般用来执行关流之类的善后操作，不过我这用不上 */ }

    //    private static String MYLOG_PATH_SDCARD_DIR = "log";// 日志文件在sdcard中的路径
    private var LOG_FILE_SAVE_DAYS = 3 // sd卡中日志文件的最多保存天数
    private const val fileNameTag = "Log" // 本类输出的日志文件名称
    private const val fileExt = ".txt"
    private const val LOG_NAME_SEPARATOR = "#"
//    private val timeZoneOffset = ZoneOffset.UTC
    private val myLogSdf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss") // 日志的输出格式
    private val logFileSdf = DateTimeFormatter.ofPattern("yyyy-MM-dd") // 日志文件格式

    private var logWriter:BufferedWriter?=null
    private var logFile:File?=null

    //指示当前类是否完成初始化的变量，若未初始化，意味着没设置必须的参数，这时候无法记日志
    var isInited = false
        private set

    private var logDir:File?=null
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


    /**
     * this method should only do once
     */

    //    public Context context;
    fun init(saveDays: Int, logLevel: Char, logDirPath:String) {
        try {
            LOG_FILE_SAVE_DAYS = saveDays
            myLogLevel = logLevel
            logDir = File(logDirPath)
            initLogWriter()
            startWriter()
            isInited=true
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
            var errCountLimit = 3

            while (errCountLimit > 0) {
                try {
                    //channel外面加互斥锁，这样就相当于一个公平锁了（带队列的互斥锁）,即使误开多个writer也不用担心冲突了
                    writeLock.withLock {
                        val textWillWrite = writeChannel.receive()

                        if (logFile?.exists() != true) {
                            initLogWriter()
                        }

                        logWriter?.write(textWillWrite + "\n")
                        logWriter?.flush()
                    }
                } catch (e: Exception) {
                    errCountLimit--

                    Log.e(TAG, "write to file err:${e.stackTraceToString()}")
                }
            }

            writeChannel.close()
        }
    }

    fun w(tag: String, msg: Any) { // 警告信息
        log(tag, msg.toString(), 'w')
    }

    fun e(tag: String, msg: Any) { // 错误信息
        log(tag, msg.toString(), 'e')
    }

    fun d(tag: String, msg: Any) { // 调试信息
        log(tag, msg.toString(), 'd')
    }

    fun i(tag: String, msg: Any) { //info
        log(tag, msg.toString(), 'i')
    }

    fun v(tag: String, msg: Any) {  //详细
        log(tag, msg.toString(), 'v')
    }

    fun w(tag: String, text: String) {
        log(tag, text, 'w')
    }

    fun e(tag: String, text: String) {
        log(tag, text, 'e')
    }

    fun d(tag: String, text: String) {
        log(tag, text, 'd')
    }

    fun i(tag: String, text: String) {
        log(tag, text, 'i')
    }

    fun v(tag: String, text: String) {
        log(tag, text, 'v')
    }

    /**
     * 根据tag, msg和等级，输出日志
     * @param tag
     * @param msg
     * @param level
     */
    private fun log(tag: String, msg: String, level: Char) {
        try {
            //如果未初始化MyLog，无法记日志，用安卓官方Log类打印下，然后返回
            if(!isInited) {
                if(level == 'e') {
                    Log.e(tag, msg)
                }else if(level == 'w') {
                    Log.w(tag, msg)
                }else if(level == 'i') {
                    Log.i(tag, msg)
                }else if(level == 'd') {
                    Log.d(tag, msg)
                }else if(level == 'v') {
                    Log.v(tag, msg)
                }else {  // should not in here if everything ok
                    Log.d(tag, msg)
                }

                return
            }

            if (MYLOG_SWITCH) { //日志文件总开关
                var isGoodLevel = true
                if ('e' == myLogLevel && 'e' == level) { // 输出错误信息
                    Log.e(tag, msg)
                } else if ('w' == myLogLevel && ('w' == level || 'e' == level)) {
                    if ('w' == level) {
                        Log.w(tag, msg)
                    } else {
                        Log.e(tag, msg)
                    }
                } else if ('i' == myLogLevel && ('w' == level || 'e' == level || 'i' == level)) {
                    if ('w' == level) {
                        Log.w(tag, msg)
                    } else if ('e' == level) {
                        Log.e(tag, msg)
                    }else {
                        Log.i(tag, msg)
                    }
                } else if ('d' == myLogLevel && ('w' == level || 'e' == level || 'd' == level || 'i' == level)) {
                    if ('w' == level) {
                        Log.w(tag, msg)
                    } else if ('e' == level) {
                        Log.e(tag, msg)
                    } else if ('d' == level) {
                        Log.d(tag, msg)
                    } else {
                        Log.i(tag, msg)
                    }
                } else if ('v' == myLogLevel && ('w' == level || 'e' == level || 'd' == level || 'i' == level || 'v' == level)) {
                    if ('w' == level) {
                        Log.w(tag, msg)
                    } else if ('e' == level) {
                        Log.e(tag, msg)
                    } else if ('d' == level) {
                        Log.d(tag, msg)
                    } else if ('i' == level) {
                        Log.i(tag, msg)
                    } else {
                        Log.v(tag, msg)
                    }
                } else {
                    // ignore the log msg if isn't against the log level
                    //例如：日志等级设置为 e，但请求输出的是 w 类型的日志，就会执行到这里，既不打印日志，也不保存日志到文件
                    isGoodLevel = false
                }

                //如果等级正确且写入文件开关为打开，写入日志到文件
                if (isGoodLevel && MYLOG_WRITE_TO_FILE) { //日志写入文件开关
                    doJobThenOffLoading {
                        writeLogToFile(level.toString(), tag, msg)
                    }
                }
            }
        }catch (e:Exception) {
            e.printStackTrace()
        }
    }

    private fun getLogFileName(): String {
        val datePrefix = logFileSdf.format(LocalDateTime.now())
        //eg: 2024-05-15#Log.txt
        return datePrefix + LOG_NAME_SEPARATOR + fileNameTag + fileExt
    }
    //    private static long getSecFromTime(LocalDateTime time) {
    //        return time.toEpochSecond(timeZoneOffset);
    //    }
    //
    //    private static long getNowInSec() {
    //        return getSecFromTime(LocalDateTime.now());
    //    }
    /**
     * 打开日志文件并写入日志
     * @param mylogtype
     * @param tag
     * @param text
     */
    private suspend fun writeLogToFile(mylogtype: String, tag: String, text: String) { // 新建或打开日志文件
        try {
            val nowtime = LocalDateTime.now()
            val needWriteMessage = myLogSdf.format(nowtime) + "    " + mylogtype + "    " + tag + "    " + text

            writeChannel.send(needWriteMessage)

//            writeLock.withLock {
//                logWriter?.write(needWriteMessage)
//                logWriter?.newLine()
//                logWriter?.flush()
//            }
        } catch (e: IOException) {
            e.printStackTrace()
            Log.e(TAG, "#writeLogToFile err:"+e.stackTraceToString())
        }
    }

    private fun initLogWriter(){
        val funName = "initLogWriter"

        val dirsFile = logDir!!
        if (!dirsFile.exists()) {
            dirsFile.mkdirs()
        }


        //Log.i("创建文件","创建文件");
//        var file: File? = null
        val file = File(
            dirsFile.getCanonicalPath(),
            getLogFileName()
        ) // MYLOG_PATH_SDCARD_DIR

        logFile = file
        //debug
//            System.out.println("file.toString():::"+file.toString());
        //debug
        if (!file.exists()) {
            //在指定的文件夹中创建文件
            file.createNewFile()
        }

        //如果writer不为null，先关流
        try {
            logWriter?.close()
        }catch (e:Exception) {
            Log.e(TAG, "#$funName err:${e.stackTraceToString()}")
        }

        //新开一个writer
        val append = true
        val filerWriter = FileWriter(file, append) // 后面这个参数代表是不是要接上文件中原来的数据，不进行覆盖
        logWriter = filerWriter.buffered()
    }

    /**
     * 删除过期的日志文件
     */
    fun delExpiredLogs() { // 删除日志文件
        try {
            MyLog.i(TAG, "start: del expired '$fileNameTag' files")

            val dirPath = logDir!! //获取日志路径

            //获取1970年1月1日到今天的天数
            val todayInDay = LocalDate.now().toEpochDay()
            val logFileList = dirPath.listFiles()?:return
            for (f in logFileList) {  //Objects.requireNonNull(param) ，param为null则抛异常
                if (f == null) {
                    continue
                }
                try {
                    val dateStrFromFileName =
                        getDateOfLogFileName(f) //返回值类似 2024-04-08，和 logFileSdf 的格式必须匹配，否则会解析失败
                    val logCreateDateInDay =
                        LocalDate.from(logFileSdf.parse(dateStrFromFileName)).toEpochDay()
                    val diffInDay = todayInDay - logCreateDateInDay //计算今天和文件名日期相差的天数

                    //debug
//                    System.out.println("diffInDay:::"+diffInDay+", now:"+logfile.format(new Date())+" other:"+dateStrFromFileName);
                    //debug

                    //删除超过天数的日志文件
                    if (diffInDay > LOG_FILE_SAVE_DAYS) {
                        f.delete()
                    }
                } catch (e: Exception) {
                    //日志类初始化完毕之后才执行此方法，所以，这里可以记录日志
                    MyLog.e(TAG, "#delExpiredLogs: in for loop err: "+e.stackTraceToString())
//                    e.printStackTrace()
                    continue
                }
            }

            MyLog.i(TAG, "end: del expired '$fileNameTag' files")

        } catch (e: Exception) {
            MyLog.e(TAG, "#delExpiredLogs: err: "+e.stackTraceToString())

//            e.printStackTrace()
        }
    }

    private fun getDateOfLogFileName(f: File): String {
        val split = f.getName().split(LOG_NAME_SEPARATOR)
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

//    fun zipLogDirAndSendToEmail() {
//        throw RuntimeException("Not implemented yet")
//    }
}
