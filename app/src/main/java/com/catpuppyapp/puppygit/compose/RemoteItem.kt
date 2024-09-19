package com.catpuppyapp.puppygit.compose

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.catpuppyapp.puppygit.play.pro.R
import com.catpuppyapp.puppygit.constants.Cons
import com.catpuppyapp.puppygit.dto.RemoteDto
import com.catpuppyapp.puppygit.utils.state.CustomStateSaveable


@OptIn(ExperimentalFoundationApi::class)
@Composable
fun RemoteItem(
    showBottomSheet: MutableState<Boolean>,
    curObjInState: CustomStateSaveable<RemoteDto>,
    idx:Int,
    curObj: RemoteDto,
    onClick:()->Unit
) {
    val haptic = LocalHapticFeedback.current
    val appContext = LocalContext.current

    val noneCredentialStr = "["+stringResource(id = R.string.none)+"]"

    Column(
        //0.9f 占父元素宽度的百分之90
        modifier = Modifier
            .fillMaxWidth()
//            .defaultMinSize(minHeight = 100.dp)
            .combinedClickable(
                enabled = true,
                onClick = {
                    onClick()
                },
                onLongClick = {
                    //震动反馈
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)

                    //设置当前条目
                    curObjInState.value = curObj

                    //显示底部菜单
                    showBottomSheet.value = true
                },
            )
            //padding要放到 combinedClickable后面，不然点按区域也会padding
//            .background(if(idx%2==0)  Color.Transparent else CommitListSwitchColor)
            .padding(10.dp)



    ) {
        Row (
            verticalAlignment = Alignment.CenterVertically,

        ){

            Text(text = stringResource(R.string.name) +": ")
            Text(text = curObj.remoteName,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                fontWeight = FontWeight.Light

            )
        }
        Row (
            verticalAlignment = Alignment.CenterVertically,

        ){

            Text(text = stringResource(R.string.url) +": ")
            Text(text = curObj.remoteUrl,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                fontWeight = FontWeight.Light

            )
        }
        Row (
            verticalAlignment = Alignment.CenterVertically,

        ){
            Text(text = stringResource(R.string.push_url) +": ")
            Text(text = curObj.pushUrl.ifEmpty { stringResource(id = R.string.use_url) },
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                fontWeight = FontWeight.Light

            )
        }
        Row (
            verticalAlignment = Alignment.CenterVertically,

        ){
            Text(text = stringResource(R.string.fetch_credential) +": ")
            Text(text = curObj.credentialName?:noneCredentialStr,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                fontWeight = FontWeight.Light

            )
        }
        Row (
            verticalAlignment = Alignment.CenterVertically,

        ){
            Text(text = stringResource(R.string.push_credential) +": ")
            Text(text = curObj.pushCredentialName?:noneCredentialStr,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                fontWeight = FontWeight.Light

            )
        }

        Row (
            verticalAlignment = Alignment.CenterVertically,

            ){
            Text(text = stringResource(R.string.branch_mode) +": ")
            Text(text = if(curObj.branchMode == Cons.dbRemote_Fetch_BranchMode_All) appContext.getString(R.string.all) else appContext.getString(R.string.custom),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                fontWeight = FontWeight.Light

            )
        }
        if(curObj.branchMode != Cons.dbRemote_Fetch_BranchMode_All) {
            Row (
                verticalAlignment = Alignment.CenterVertically,

                ){
                Text(text = (if(curObj.branchListForFetch.size > 1) stringResource(R.string.branches) else stringResource(R.string.branch)) +": ")
                Text(text = curObj.branchListForFetch.toString(),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    fontWeight = FontWeight.Light

                )
            }
        }


    }
}
