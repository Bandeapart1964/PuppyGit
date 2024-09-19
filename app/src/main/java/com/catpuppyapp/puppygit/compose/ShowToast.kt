package com.catpuppyapp.puppygit.compose

import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.res.stringResource
import com.catpuppyapp.puppygit.play.pro.R
import com.catpuppyapp.puppygit.utils.AppModel
import com.catpuppyapp.puppygit.utils.showToast

@Composable
fun ShowToast(
    showToast:MutableState<Boolean>,
    msg:MutableState<String>,
) {
    val appContext = AppModel.singleInstanceHolder.appContext

    if(showToast.value) {
        //显示提示信息：
        showToast(appContext, msg.value)


        //reset msg
        showToast.value=false
        msg.value=""
    }
}
