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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.catpuppyapp.puppygit.git.SubmoduleDto
import com.catpuppyapp.puppygit.play.pro.R


@OptIn(ExperimentalFoundationApi::class)
@Composable
fun SubmoduleItem(
    thisObj:SubmoduleDto,
    isItemInSelected:(SubmoduleDto) -> Boolean,
    onLongClick:(SubmoduleDto)->Unit,
    onClick:(SubmoduleDto)->Unit
) {

    val haptic = LocalHapticFeedback.current

    Column(
        //0.9f 占父元素宽度的百分之90
        modifier = Modifier
            .fillMaxWidth()
//            .defaultMinSize(minHeight = 100.dp)
            .combinedClickable(
                enabled = true,
                onClick = {
                    onClick(thisObj)
                },
                onLongClick = {
                    //震动反馈
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)

                    onLongClick(thisObj)
                },
            )
            .then(
                //如果条目被选中，切换高亮颜色
                if (isItemInSelected(thisObj)) Modifier.background(
                    MaterialTheme.colorScheme.primaryContainer

                    //then 里传 Modifier不会有任何副作用，还是当前的Modifier(即调用者自己：this)，相当于什么都没改，后面可继续链式调用其他方法
                ) else Modifier
            )
            //padding要放到 combinedClickable后面，不然点按区域也会padding；
            // padding要放到背景颜色后面，不然padding的区域不会着色
            .padding(10.dp)




    ) {
        Row (
            verticalAlignment = Alignment.CenterVertically,

        ){
            Text(text = stringResource(R.string.name) +":")
            Text(text = thisObj.name,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                fontWeight = FontWeight.Light

            )
        }

        Row (
            verticalAlignment = Alignment.CenterVertically,

        ){
            Text(text = stringResource(R.string.url) +":")
            Text(text = thisObj.remoteUrl,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                fontWeight = FontWeight.Light

            )
        }

        Row (
            verticalAlignment = Alignment.CenterVertically,

        ){
            Text(text = stringResource(R.string.path) +":")
            Text(text = thisObj.relativePathUnderParent,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                fontWeight = FontWeight.Light
            )
        }
        Row (
            verticalAlignment = Alignment.CenterVertically,

        ){

            Text(text = stringResource(R.string.status) +":")
            Text(text = thisObj.getStatus(),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                fontWeight = FontWeight.Light
            )
        }

     }
}
