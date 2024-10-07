package com.catpuppyapp.puppygit.utils.fileopenhistory

import com.catpuppyapp.puppygit.settings.FileEditedPos
import com.catpuppyapp.puppygit.settings.SettingsUtil
import com.catpuppyapp.puppygit.utils.JsonUtil
import com.catpuppyapp.puppygit.utils.MyLog
import com.catpuppyapp.puppygit.utils.doJobThenOffLoading
import com.catpuppyapp.puppygit.utils.getSecFromTime
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.encodeToStream
import java.io.File


object FileOpenHistoryMan {
    private val TAG = "FileOpenHistoryMan"

    private var _limit = 50  // will update by settings value
    private val fileName = "file_open_history.json"

    private lateinit var _file: File
    private lateinit var _saveDir: File

    private lateinit var curHistory: FileOpenHistory
    private val lock = Mutex()

    /**
     *
     * should run this method after AppModel and Settings and MyLog init done
     * @param limit how many history will remembered
     * @param requireClearOldSettingsEditedHistory if true, will clear settings remembered file edited position, caller should check before pass this value to avoid unnecessary clear
     */
    fun init(saveDir:File, limit:Int, requireClearOldSettingsEditedHistory:Boolean) {
        _limit = limit

        val saveDirPath = saveDir.canonicalPath
        _saveDir = File(saveDirPath)
        _file = File(saveDirPath, fileName)

        readHistoryFromFile()

        if(requireClearOldSettingsEditedHistory) {
            clearOldSettingsHistory()
        }
    }

    private fun getFile():File {
        if(_saveDir.exists().not()) {
            _saveDir.mkdirs()
        }

        if(_file.exists().not()) {
            _file.createNewFile()
        }

        return _file
    }

    private fun readHistoryFromFile() {
        val f = getFile()
        try {
            curHistory = JsonUtil.j.decodeFromString<FileOpenHistory>(f.readText())
        }catch (e:Exception) {
            // err is ok, just return a new one, when set, will overwrite old file
            curHistory = FileOpenHistory()
            MyLog.e(TAG, "#readHistoryFromFile: read err, file content empty or corrupted, will return a new history, err is: ${e.localizedMessage}")
        }
    }

    fun getHistory():FileOpenHistory {
        return curHistory.copy()
    }

    fun get(path:String):FileEditedPos {
        return getHistory().storage.get(path)?.copy() ?: FileEditedPos()
    }

    fun set(path:String, lastEditedPos: FileEditedPos) {
        lastEditedPos.lastUsedTime = getSecFromTime()

        val h = getHistory()
        h.storage.set(path, lastEditedPos)

        if(h.storage.size > _limit) {
            removeOldHistory(h)
        }

        saveHistory(h)
    }

    @OptIn(ExperimentalSerializationApi::class)
    private fun saveHistory(newHistory:FileOpenHistory) {
        doJobThenOffLoading {
            try {
                curHistory = newHistory
                lock.withLock {
                    JsonUtil.j.encodeToStream(newHistory, getFile().outputStream())
                }

            }catch (e:Exception) {
                MyLog.e(TAG, "#saveHistory: save file opened history err: ${e.localizedMessage}")
            }
        }
    }

    private fun removeOldHistory(history: FileOpenHistory) {
        val copy:Map<String, FileEditedPos> = history.storage.toMap()
        val copy2 = copy.toMap()
        val sorted = copy2.toSortedMap {k1, k2 ->
            try {
                val v1 = copy[k1]
                val v2 = copy[k2]

                // make bigger number before smaller number, is a DESC order
                if(v1==null) {
                    1
                }else if(v2== null) {
                    -1
                }else {
                    -(v1.lastUsedTime.compareTo(v2.lastUsedTime))
                }
            }catch (_:Exception) {
                0
            }

        }

        var count = 0
        val newStorage = mutableMapOf<String, FileEditedPos>()
        for(k in sorted.keys) {
            if(++count > _limit) {
                break
            }

            val v = copy[k]
            if(v!=null) {
                newStorage.set(k, v)
            }
        }

        history.storage = newStorage
    }

    /**
     * clear file last edited positions in settings, the filed is `Settings.Editor.filesLastEditPosition`
     */
    private fun clearOldSettingsHistory() {
        try {
            SettingsUtil.update {
                it.editor.filesLastEditPosition.clear()
            }
        }catch (e:Exception) {
            MyLog.e(TAG, "#clearOldSettingsHistory err: ${e.stackTraceToString()}")
        }
    }
}
