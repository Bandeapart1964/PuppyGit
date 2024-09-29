package com.catpuppyapp.puppygit.screen.content.homescreen.scaffold.title

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextOverflow
import com.catpuppyapp.puppygit.compose.FilterTextField
import com.catpuppyapp.puppygit.constants.PageRequest
import com.catpuppyapp.puppygit.play.pro.R
import com.catpuppyapp.puppygit.utils.changeStateTriggerRefreshPage
import com.catpuppyapp.puppygit.utils.state.CustomStateSaveable
import java.io.File


@OptIn(ExperimentalFoundationApi::class)
@Composable
fun FilesTitle(
    currentPath: MutableState<String>,
    allRepoParentDir: File,
    needRefreshFilesPage: MutableState<String>,
    filesPageGetFilterMode: ()->Int,
    filterKeyWord:CustomStateSaveable<TextFieldValue>,
    filterModeOn:()->Unit,
    doFilter:(String)->Unit,
    requestFromParent:MutableState<String>,
    filterKeywordFocusRequester: FocusRequester,
    filesPageSimpleFilterOn: Boolean,
    filesPageSimpleFilterKeyWord:CustomStateSaveable<TextFieldValue>
) {
    val haptic = LocalHapticFeedback.current

//    if (filesPageGetFilterMode() == 1) {  //正在搜索，显示输入框
//        OutlinedTextField(
//            modifier = Modifier.fillMaxWidth().focusRequester(filterKeywordFocusRequester),
//            value = filterKeyWord.value,
//            onValueChange = { filterKeyWord.value = it },
//            placeholder = { Text(stringResource(R.string.input_keyword)) },
//            singleLine = true,
//            // label = {Text(title)}
//            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
//            keyboardActions = KeyboardActions(onSearch = {
//                doFilter(filterKeyWord.value.text)
//            })
//        )
//
//    }else if(filesPageGetFilterMode()==2){  //显示搜索结果
//        Text(
//            text= "\""+filterKeyWord.value.text+"\"",
//            maxLines = 1,
//            overflow = TextOverflow.Ellipsis,
//            modifier = Modifier.combinedClickable(onLongClick = {  //长按显示关键字
//                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
//                Msg.requireShow(filterKeyWord.value.text)
//            }
//            ) { //onClick
//                //点按重新输入关键字
//                filterModeOn()
//            }
//        )
//    }
    if(filesPageSimpleFilterOn) {
        FilterTextField(
            filesPageSimpleFilterKeyWord,
        )
    } else {  //filesPageGetFilterMode()==0 , 搜索模式关闭
            //render page
            Text(
                modifier = Modifier
                    .combinedClickable(
                        onDoubleClick = {
                            //双击标题返回列表顶部
                            requestFromParent.value=PageRequest.goToTop
                        },
                        onLongClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)

                            //长按标题回到仓库根目录
                            currentPath.value = allRepoParentDir.canonicalPath
                            changeStateTriggerRefreshPage(needRefreshFilesPage)
                        }
                    ) {  //onClick

                    },
                text = stringResource(id = R.string.files),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

        }

    LaunchedEffect( filesPageGetFilterMode() ) {
        try {
            if(filesPageGetFilterMode()==1) {
                filterKeywordFocusRequester.requestFocus()
            }

        }catch (_:Exception) {
            //顶多聚焦失败，没啥好记的
        }
    }
}
