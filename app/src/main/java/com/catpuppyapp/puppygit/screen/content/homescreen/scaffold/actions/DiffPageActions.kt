package com.catpuppyapp.puppygit.screen.content.homescreen.scaffold.actions

import androidx.compose.foundation.ScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.Compare
import androidx.compose.material.icons.filled.FileOpen
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import com.catpuppyapp.puppygit.compose.LongPressAbleIconBtn
import com.catpuppyapp.puppygit.constants.Cons
import com.catpuppyapp.puppygit.constants.PageRequest
import com.catpuppyapp.puppygit.data.entity.RepoEntity
import com.catpuppyapp.puppygit.dev.detailsDiffTestPassed
import com.catpuppyapp.puppygit.dev.dev_EnableUnTestedFeature
import com.catpuppyapp.puppygit.play.pro.R
import com.catpuppyapp.puppygit.user.UserUtil
import com.catpuppyapp.puppygit.utils.AppModel
import com.catpuppyapp.puppygit.utils.Msg
import com.catpuppyapp.puppygit.utils.MyLog
import com.catpuppyapp.puppygit.utils.UIHelper
import com.catpuppyapp.puppygit.utils.cache.Cache
import com.catpuppyapp.puppygit.utils.state.CustomStateSaveable
import kotlinx.coroutines.CoroutineScope
import java.io.File


@Composable
fun DiffPageActions(
    curRepo: CustomStateSaveable<RepoEntity>,
    fromTo: String,
    changeType: String,
    relativePathUnderRepoState: MutableState<String>,
    refreshPage: () -> Unit,
    listState: ScrollState,
    scope: CoroutineScope,
    request:MutableState<String>,
    fileFullPath:String,
    requireBetterMatchingForCompare:MutableState<Boolean>
) {
    val TAG = "DiffPageActions"

    val navController = AppModel.singleInstanceHolder.navController
    val appContext= LocalContext.current

    val fileChangeTypeIsModified = changeType == Cons.gitStatusModified


    if (fileChangeTypeIsModified && UserUtil.isPro()
        && (dev_EnableUnTestedFeature || detailsDiffTestPassed)
    ){
        LongPressAbleIconBtn(
            tooltipText = stringResource(R.string.better_compare),
            icon = Icons.Filled.Compare,
            iconContentDesc = stringResource(R.string.better_compare),
            iconColor = UIHelper.getIconEnableColorOrNull(requireBetterMatchingForCompare.value),
            enabled = true,
        ) {
            requireBetterMatchingForCompare.value = !requireBetterMatchingForCompare.value

            // show msg: "better but slow compare: ON/OFF"
            Msg.requireShow(
                appContext.getString(R.string.better_but_slow_compare)+": "
                + (if(requireBetterMatchingForCompare.value) appContext.getString(R.string.on_str) else appContext.getString(R.string.off_str))
            )

        }

    }

//    LongPressAbleIconBtn(
//        tooltipText = stringResource(R.string.go_to_top),
//        icon =  Icons.Filled.VerticalAlignTop,
//        iconContentDesc = stringResource(id = R.string.go_to_top),
//        enabled = true,
//
//    ) {
//        UIHelper.scrollTo(scope, listState, 0)
//    }

    LongPressAbleIconBtn(
        tooltipText = stringResource(R.string.refresh),
        icon = Icons.Filled.Refresh,
        iconContentDesc = stringResource(id = R.string.refresh),
    ) {
        refreshPage()
    }

    LongPressAbleIconBtn(
        tooltipText = stringResource(R.string.open),
        icon = Icons.Filled.FileOpen,
        iconContentDesc = stringResource(id = R.string.open),
    ) label@{
        // go editor sub page
//        showToast(appContext,filePath)
        try {
            //如果文件不存在，提示然后返回
            if(!File(fileFullPath).exists()) {
                Msg.requireShowLongDuration(appContext.getString(R.string.file_doesnt_exist))
                return@label
            }

            //跳转到SubEditor页面
            val filePathKey = Cache.setThenReturnKey(fileFullPath)
            val goToLine = "-1"
            val initMergeMode = "0"  //冲突条目无法进入diff页面，所以能预览diff定不是冲突条目，因此跳转到editor时应将mergemode初始化为假
            val initReadOnly = "0"  //diff页面不可能显示app内置目录下的文件，所以一率可编辑

            navController.navigate(Cons.nav_SubPageEditor + "/$filePathKey"+"/$goToLine"+"/$initMergeMode"+"/$initReadOnly")

        }catch (e:Exception) {
            Msg.requireShowLongDuration("err:"+e.localizedMessage)
            MyLog.e(TAG, "'Open' err:"+e.stackTraceToString())
        }

    }

    LongPressAbleIconBtn(
        tooltipText = stringResource(R.string.open_as),
        icon = Icons.AutoMirrored.Filled.OpenInNew,
        iconContentDesc = stringResource(id = R.string.open_as),
    ) label@{
        try {
            if(!File(fileFullPath).exists()) {
                Msg.requireShowLongDuration(appContext.getString(R.string.file_doesnt_exist))
                return@label
            }

            //显示OpenAs弹窗
            request.value = PageRequest.showOpenAsDialog

        }catch (e:Exception) {
            Msg.requireShowLongDuration("err:"+e.localizedMessage)
            MyLog.e(TAG, "'Open As' err:"+e.stackTraceToString())
        }
    }
}

