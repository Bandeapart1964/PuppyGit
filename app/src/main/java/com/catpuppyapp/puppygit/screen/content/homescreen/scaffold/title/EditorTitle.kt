package com.catpuppyapp.puppygit.screen.content.homescreen.scaffold.title

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.catpuppyapp.puppygit.compose.FilterTextField
import com.catpuppyapp.puppygit.constants.PageRequest
import com.catpuppyapp.puppygit.play.pro.R
import com.catpuppyapp.puppygit.style.MyStyleKt
import com.catpuppyapp.puppygit.utils.FsUtils
import com.catpuppyapp.puppygit.utils.getFileNameFromCanonicalPath
import com.catpuppyapp.puppygit.utils.state.CustomStateSaveable
import com.catpuppyapp.puppygit.utils.state.StateUtil
import java.io.File

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun EditorTitle(editorPageShowingFilePath: MutableState<String>,
                editorPageRequestFromParent:MutableState<String>,
                editorSearchMode:Boolean,
                editorSearchKeyword: CustomStateSaveable<TextFieldValue>,
                editorPageMergeMode:Boolean,
                readOnly:Boolean,
                editorOpenFileErr:Boolean
                ) {
    val haptic = LocalHapticFeedback.current
    val appContext = LocalContext.current

    if(editorPageShowingFilePath.value.isNotBlank()) {
        val fileName = getFileNameFromCanonicalPath(editorPageShowingFilePath.value)
//        val filePath = getFilePathStrBasedRepoDir(editorPageShowingFilePath.value, returnResultStartsWithSeparator = true)
        val filePath = FsUtils.getPathWithInternalOrExternalPrefix(editorPageShowingFilePath.value)

        val filePathNoFileName = filePath.removeSuffix(fileName)  // "/"结尾的路径或者只有"/"
        //如果只剩/，就返回 /，否则把末尾的/移除
        val filePathNoFileNameNoEndSlash = if(filePathNoFileName==File.separator) filePathNoFileName else filePathNoFileName.removeSuffix(File.separator)

        Column(
            //双击标题回到文件顶部；长按可跳转到指定行；点击显示路径
            modifier = Modifier.widthIn(min=MyStyleKt.Title.clickableTitleMinWidth)
                .combinedClickable(
                    enabled = !editorOpenFileErr,  //只有在成功打开文件时才启用点击标题长按标题之类的操作
                    onDoubleClick = {
    //                    val settings = SettingsUtil.getSettingsSnapshot()
    //                    val lastPos = settings.editor.filesLastEditPosition[editorPageShowingFilePath.value]
    //
    //                    //不在第一行，返回第一行；否则返回上次编辑位置（如果有的话，没的话还是返回第一行）
    //                    if(lastPos==null || lastPos.firstVisibleLineIndex != 0) {
    //                        editorPageRequestFromParent.value = PageRequest.goToTop
    //                    }else {
    //                        editorPageRequestFromParent.value = PageRequest.backLastEditedLine
    //                    }

                        editorPageRequestFromParent.value = PageRequest.switchBetweenFirstLineAndLastEditLine
                    },
                    onLongClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        editorPageRequestFromParent.value = PageRequest.goToLine
                    }
                ) {  //onClick
                        //显示仓库开头的相对路径
    //                    Msg.requireShowLongDuration(filePath)

                    //显示文件详情（大小、路径、字数，等）
                    editorPageRequestFromParent.value = PageRequest.showDetails
                    //点按显示文件名
    //                showToast(AppModel.singleInstanceHolder.appContext, fileName)
                }
        ) {
            if(editorSearchMode) {
                    FilterTextField(
                        editorSearchKeyword,
//                        modifier = Modifier, //避免默认的fillMaxWidth()导致显示不出箭头图标
                    )
            }else {
                Row(modifier = Modifier.horizontalScroll(StateUtil.getRememberScrollState()),
                    verticalAlignment = Alignment.CenterVertically
                ) {  //话说这名如果超了，在Row上加个滚动属性让用户能滚动查看，怎么样？（20240411加了，测试了下，勉强能用，还行，好！
                    if(readOnly) {
                        Icon(
                            modifier = Modifier.size(12.dp),
                            imageVector = Icons.Filled.Lock,
                            contentDescription = stringResource(R.string.read_only),
                        )
                    }
                    Text(text =fileName,
                        fontSize = 15.sp,
                        maxLines=1,
                        overflow = TextOverflow.Ellipsis,
                        color = if(editorPageMergeMode) MyStyleKt.TextColor.danger else Color.Unspecified
                    )
                }
                Row(modifier = Modifier.horizontalScroll(StateUtil.getRememberScrollState())) {
                    Text(
                        text = filePathNoFileNameNoEndSlash,
                        fontSize = 11.sp,
                        maxLines=1,
                        overflow = TextOverflow.Ellipsis

                    )
                }

            }

        }

    }else {
        Text(
            text = stringResource(id = R.string.editor),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}