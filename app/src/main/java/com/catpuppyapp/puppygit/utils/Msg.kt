package com.catpuppyapp.puppygit.utils

import android.widget.Toast
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class Msg {
    companion object {
        val defaultErrCallback:suspend (String, String)->Unit = { msg:String, repoId:String,  ->
            MsgQueue.addToTail(msg)
            createAndInsertError(repoId,msg)
            //Log就不在这记了，Log对用户不可见，所以我其实想在哪记都可以
        }

        //显示时间长短都行，就用这个，否则用具体的showLongDuration和showShortDuration
        val requireShow = { msg:String ->
//            MsgQueue.addToTail(msg)
//            doJobThenOffLoading {
//                //toast 必须在主线程才能正常显示
//                withContext(Dispatchers.Main) {
//                    showToast(AppModel.singleInstanceHolder.appContext, msg)
//                }
//
//            }

            doJobWithMainContext {
                showToast(AppModel.singleInstanceHolder.appContext, msg)
            }

        }

        val requireShowShortDuration = { msg:String ->
            doJobWithMainContext {
                showToast(AppModel.singleInstanceHolder.appContext, msg, Toast.LENGTH_SHORT)
            }

        }

        val requireShowLongDuration = { msg:String ->
            doJobWithMainContext {
                showToast(AppModel.singleInstanceHolder.appContext, msg, Toast.LENGTH_LONG)
            }

        }

        val msgNotifyHost = {
            MsgQueue.showAndClearAllMsg()
        }
    }
}

