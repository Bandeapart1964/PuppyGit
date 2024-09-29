package com.catpuppyapp.puppygit.screen.content.homescreen.scaffold.title

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.sp
import com.catpuppyapp.puppygit.constants.PageRequest
import com.catpuppyapp.puppygit.play.pro.R
import com.catpuppyapp.puppygit.style.MyStyleKt
import com.catpuppyapp.puppygit.utils.UIHelper
import com.catpuppyapp.puppygit.utils.state.StateUtil
import kotlinx.coroutines.CoroutineScope

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun DiffScreenTitle(
    fileName:String,
    filePath:String,
    fileRelativePathUnderRepoState: MutableState<String>,
    listState: ScrollState,
    scope: CoroutineScope,
    request:MutableState<String>,
) {

    if(fileRelativePathUnderRepoState.value.isNotBlank()) {
        val haptic = LocalHapticFeedback.current
        Column(modifier = Modifier.widthIn(min=MyStyleKt.Title.clickableTitleMinWidth)
            .combinedClickable(
                //double click go to top of list
                onDoubleClick = { UIHelper.scrollTo(scope,listState, 0) },
            ) {  //onClick
                //show details , include file name and path
                request.value = PageRequest.showDetails
            }
        ) {
            Row(modifier = Modifier.horizontalScroll(StateUtil.getRememberScrollState())) {
                Text(fileName,
                    fontSize = 15.sp,
                    maxLines=1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Row(modifier = Modifier.horizontalScroll(StateUtil.getRememberScrollState())) {
                Text(
                    text = filePath,
                    fontSize = 11.sp,
                    maxLines=1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

    }else {
        Text(
            text = stringResource(id = R.string.diff_screen_default_title),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}
