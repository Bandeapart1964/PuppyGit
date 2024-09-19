package com.catpuppyapp.puppygit.compose

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableIntState
import androidx.compose.runtime.MutableState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.catpuppyapp.puppygit.play.pro.R
import com.catpuppyapp.puppygit.git.CommitDto
import com.catpuppyapp.puppygit.style.MyStyleKt
import com.catpuppyapp.puppygit.utils.AppModel
import com.catpuppyapp.puppygit.utils.Libgit2Helper
import com.catpuppyapp.puppygit.utils.UIHelper
import com.catpuppyapp.puppygit.utils.doJobThenOffLoading
import com.catpuppyapp.puppygit.utils.state.CustomStateSaveable
import kotlinx.coroutines.delay


@OptIn(ExperimentalFoundationApi::class)
@Composable
fun CommitItem(
    showBottomSheet: MutableState<Boolean>,
    curCommit: CustomStateSaveable<CommitDto>,
    curCommitIdx:MutableIntState,
    idx:Int,
    commitDto:CommitDto,
    requireBlinkIdx:MutableIntState,  //请求闪烁的索引，会闪一下对应条目，然后把此值设为无效
    onClick:(CommitDto)->Unit={}
) {
    val haptic = AppModel.singleInstanceHolder.haptic
//    println("IDX::::::::::"+idx)
    Column(
        //0.9f 占父元素宽度的百分之90
        modifier = Modifier
            .fillMaxWidth()
//            .defaultMinSize(minHeight = 100.dp)
            .combinedClickable(
                enabled = true,
                onClick = { onClick(commitDto) },
                onLongClick = {  // x 算了)TODO 把长按也改成短按那样，在调用者那里实现，这里只负责把dto传过去，不过好像没必要，因为调用者那里还是要写同样的代码，不然弹窗不知道操作的是哪个对象
                    //震动反馈
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)

                    //设置当前条目
                    curCommit.value = commitDto
                    curCommitIdx.intValue = idx
                    //显示底部菜单
                    showBottomSheet.value = true
                },
            )
            //padding要放到 combinedClickable后面，不然点按区域也会padding
//            .background(if(idx%2==0)  Color.Transparent else CommitListSwitchColor)
            .then(
                //如果是请求闪烁的索引，闪烁一下
                if (requireBlinkIdx.intValue != -1 && requireBlinkIdx.intValue==idx) {
                    val highlightColor = Modifier.background(UIHelper.getHighlightingBackgroundColor())
                    //高亮2s后解除
                    doJobThenOffLoading {
                        delay(UIHelper.getHighlightingTimeInMills())  //解除高亮倒计时
                        requireBlinkIdx.intValue = -1  //解除高亮
                    }
                    highlightColor
                }else {
                    Modifier
                }
            )
            .padding(10.dp)




    ) {
        Row (
            verticalAlignment = Alignment.CenterVertically,

        ){

            Text(text = stringResource(R.string.hash) +":")
            Text(text = commitDto.oidStr,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                fontWeight = FontWeight.Light

            )
        }
//        Row (
//            verticalAlignment = Alignment.CenterVertically,
//
//            ){
//
//            Text(text = stringResource(R.string.email) +":")
//            Text(text = commitDto.email,
//                maxLines = 1,
//                overflow = TextOverflow.Ellipsis,
//                fontWeight = FontWeight.Light
//
//            )
//        }
        Row (
            verticalAlignment = Alignment.CenterVertically,

        ){
            Text(text = stringResource(R.string.author) +":")
            Text(text = Libgit2Helper.getFormattedUsernameAndEmail(commitDto.author, commitDto.email),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                fontWeight = FontWeight.Light

            )
        }

        //如果committer和author不同，显示
        if(!commitDto.authorAndCommitterAreSame()) {
            Row (
                verticalAlignment = Alignment.CenterVertically,
            ){
                Text(text = stringResource(R.string.committer) +":")
                Text(text = Libgit2Helper.getFormattedUsernameAndEmail(commitDto.committerUsername, commitDto.committerEmail),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    fontWeight = FontWeight.Light

                )
            }
        }

        Row (
            verticalAlignment = Alignment.CenterVertically,

            ){

            Text(text = stringResource(R.string.date) +":")
            Text(text = commitDto.dateTime,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                fontWeight = FontWeight.Light

            )
        }
        Row (
            verticalAlignment = Alignment.CenterVertically,
        ){

            Text(text = stringResource(R.string.msg) +":")
            Text(text = commitDto.msg,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                fontWeight = FontWeight.Light

            )
        }
        if(commitDto.branchShortNameList.isNotEmpty()) {
            Row (
                verticalAlignment = Alignment.CenterVertically,

            ){

                Text(text = (if(commitDto.branchShortNameList.size > 1) stringResource(R.string.branches) else stringResource(R.string.branch)) +":")
                Text(text = commitDto.branchShortNameList.toString(),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    fontWeight = FontWeight.Light

                )
            }

        }

        if(commitDto.tagShortNameList.isNotEmpty()) {
            Row (
                verticalAlignment = Alignment.CenterVertically,

            ){

                Text(text = (if(commitDto.tagShortNameList.size > 1) stringResource(R.string.tags) else stringResource(R.string.tag)) +":")
                Text(text = commitDto.tagShortNameList.toString(),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    fontWeight = FontWeight.Light

                )
            }

        }

        if(commitDto.parentShortOidStrList.isNotEmpty()) {
            Row (
                verticalAlignment = Alignment.CenterVertically,

            ){

                Text(text = (if(commitDto.parentShortOidStrList.size > 1) stringResource(R.string.parents) else stringResource(R.string.parent)) +":")
                Text(text = commitDto.parentShortOidStrList.toString(),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    fontWeight = FontWeight.Light

                )
            }

        }

        if(commitDto.hasOther()) {
            Row (
                verticalAlignment = Alignment.CenterVertically,
            ){
                Text(text = stringResource(R.string.other)+":")
                Text(text = commitDto.getOther(),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    fontWeight = FontWeight.Light
                )
            }

        }
    }
}
