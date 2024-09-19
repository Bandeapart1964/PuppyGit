package jp.kaleidot725.sample.ui.composable

import android.annotation.SuppressLint
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableIntState
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.catpuppyapp.puppygit.compose.ConfirmDialog
import com.catpuppyapp.puppygit.constants.PageRequest
import com.catpuppyapp.puppygit.play.pro.R
import com.catpuppyapp.puppygit.settings.FileEditedPos
import com.catpuppyapp.puppygit.style.MyStyleKt
import com.catpuppyapp.puppygit.ui.theme.Theme
import com.catpuppyapp.puppygit.utils.AppModel
import com.catpuppyapp.puppygit.utils.Msg
import com.catpuppyapp.puppygit.utils.replaceStringResList
import com.catpuppyapp.puppygit.utils.state.CustomStateSaveable
import jp.kaleidot725.sample.ui.composable.editor.EditorMenus
import jp.kaleidot725.sample.ui.composable.editor.FieldIcon
import jp.kaleidot725.sample.ui.extension.createCancelledState
import jp.kaleidot725.sample.ui.extension.createCopiedState
import jp.kaleidot725.sample.ui.extension.createDeletedState
import jp.kaleidot725.sample.ui.extension.createMultipleSelectionModeState
import jp.kaleidot725.sample.ui.extension.createSelectAllState
import jp.kaleidot725.texteditor.controller.rememberTextEditorController
import jp.kaleidot725.texteditor.state.TextEditorState
import jp.kaleidot725.texteditor.view.ScrollEvent
import jp.kaleidot725.texteditor.view.TextEditor

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun FileEditor(requestFromParent:MutableState<String>,
               fileFullPath:String,
               lastEditedPos:FileEditedPos,
               textEditorState:CustomStateSaveable<TextEditorState>,
               onChanged:(TextEditorState)->Unit,
               contentPadding:PaddingValues,
               isContentChanged:MutableState<Boolean>,
               editorLastScrollEvent:MutableState<ScrollEvent?>,
               editorListState: LazyListState,
               editorPageIsInitDone:MutableState<Boolean>,
               editorPageIsContentSnapshoted:MutableState<Boolean>,
               goToLine:Int,
               readOnlyMode:Boolean,
               searchMode:MutableState<Boolean>,
               searchKeyword:String,
               mergeMode:Boolean,
               showLineNum:MutableState<Boolean>,
               lineNumFontSize:MutableIntState,
               fontSize:MutableIntState,
) {
    val appContext = LocalContext.current
    val haptic = AppModel.singleInstanceHolder.haptic

    val inDarkTheme = Theme.inDarkTheme

    val clipboardManager = LocalClipboardManager.current
    val keyboardController = LocalSoftwareKeyboardController.current

    val showDeleteDialog = remember { mutableStateOf(false) }
    val editableController by rememberTextEditorController(textEditorState.value, onChanged = { onChanged(it) }, isContentChanged, editorPageIsContentSnapshoted)

//    val bottomPadding = if (textEditorState.value.isMultipleSelectionMode) 100.dp else 0.dp  //如果使用这个padding，开启选择模式时底栏会有背景，否则没有，没有的时候就像直接浮在编辑器上
//    val bottomPadding = 0.dp

//    val contentBottomPaddingValue = with(LocalDensity.current) { WindowInsets.ime.getBottom(this).toDp() }  //获取键盘高度？用来做padding以在显示键盘时能看到最后一行内容？
//    val contentPaddingValues = PaddingValues(
//        start=contentPadding.calculateStartPadding(LayoutDirection.Ltr),
//        top=contentPadding.calculateTopPadding(),
//        end=contentPadding.calculateEndPadding(LayoutDirection.Rtl),
//        bottom = contentPadding.calculateBottomPadding()+contentBottomPaddingValue+300.dp  // make bottom higher for better view
////        bottom = contentPadding.calculateBottomPadding()+contentBottomPaddingValue  // make bottom higher for better view
//    )

//    val contentPaddingValues = PaddingValues(bottom = contentBottomPaddingValue)
    val contentPaddingValues = contentPadding

    val enableSelectMode = { index:Int ->
        //隐藏键盘
        keyboardController?.hide()

        // 非行选择模式，启动行选择模式 (multiple selection mode on)
        //注：索引传-1或其他无效索引即可在不选中任何行的情况下启动选择模式，从顶栏菜单开启选择模式默认不选中任何行，所以这里传-1
        textEditorState.value = textEditorState.value.createMultipleSelectionModeState(index)
    }

    //切换行选择模式
    if(requestFromParent.value == PageRequest.editorSwitchSelectMode) {
        PageRequest.clearStateThenDoAct(requestFromParent) {
            //如果已经是选择模式，退出；否则开启选择模式
            if(textEditorState.value.isMultipleSelectionMode) {  //退出选择模式
                textEditorState.value = textEditorState.value.createCancelledState()
            }else {  //开启选择模式
                enableSelectMode(-1)
            }

        }
    }



    if(showDeleteDialog.value) {
        ConfirmDialog(
            title = stringResource(R.string.delete_lines),
            text = replaceStringResList(stringResource(R.string.will_delete_n_lines_ask), listOf(textEditorState.value.getSelectedCount().toString())) ,
            okTextColor = MyStyleKt.TextColor.danger,
            onCancel = { showDeleteDialog.value=false }
        ) {
            //关弹窗
            showDeleteDialog.value=false

            //删除选中行
            textEditorState.value = textEditorState.value.createDeletedState()

            //删除行，改变内容flag设为真
            isContentChanged.value=true
            editorPageIsContentSnapshoted.value=false
            //显示通知
            Msg.requireShow(appContext.getString(R.string.deleted))
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
//            .systemBarsPadding()  //用脚手架的contentPadding就不需要这个了
        ,
    ) {
        TextEditor(
            requestFromParent,
            fileFullPath,
            lastEditedPos = lastEditedPos,
            textEditorState = textEditorState.value,
            editableController = editableController,
            onChanged = { onChanged(it) },
            contentPaddingValues = contentPaddingValues,
            editorLastScrollEvent =editorLastScrollEvent,
            editorListState =editorListState,
            editorPageIsInitDone = editorPageIsInitDone,
            goToLine=goToLine,
            readOnlyMode=readOnlyMode,
            searchMode = searchMode,
            searchKeyword =searchKeyword,
            mergeMode=mergeMode,
            fontSize=fontSize,

//            modifier = Modifier.padding(bottom = bottomPadding)
//            modifier = Modifier.fillMaxSize()
        ) { index, isSelected, innerTextField ->
            // TextLine
            Row(
//                horizontalArrangement = Arrangement.spacedBy(4.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        getBackgroundColor(
                            isSelected,
                            textEditorState.value.isMultipleSelectionMode
                        )
                    )
                    .bottomBorder(
                        strokeWidth = 1.dp,
                        color = if (inDarkTheme) Color.DarkGray.copy(alpha=0.2f) else Color.LightGray.copy(alpha = 0.2f)
                    )
            ) {
                if(showLineNum.value) {
                    Box (
                        //让行号和选择图标居中
//                    horizontalAlignment = Alignment.CenterHorizontally
                    ){
                        // TextLine Number
                        Text(
                            modifier = Modifier.align(Alignment.TopCenter),
                            text = getLineNumber(index),
                            color = if(inDarkTheme) MyStyleKt.TextColor.lineNum_forEditorInDarkTheme else MyStyleKt.TextColor.lineNum_forEditorInLightTheme,
                            fontSize = lineNumFontSize.intValue.sp,
                            //行号居中
                            //                    modifier = Modifier.align(Alignment.CenterVertically)
                        )

                        // TextField Menu Icon
                        FieldIcon(
                            isMultipleSelection = textEditorState.value.isMultipleSelectionMode,
                            isSelected = isSelected,
                            modifier = Modifier
                                .size(12.dp)
                                .padding(top = 1.dp)
                                .align(Alignment.BottomCenter)
                                .focusable(false)  //不知道这个focusable(false)有什么用
                                .combinedClickable(
                                    onLongClick = {
                                        if (textEditorState.value.isMultipleSelectionMode) {
                                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)

                                            editableController.selectFieldSpan(targetIndex = index)
                                        }
                                    }
                                ) {
                                    //如果是行选择模式，选中当前点击的行如果不是行选择模式；进入行选择模式
                                    if (textEditorState.value.isMultipleSelectionMode) {
                                        //选中/取消选中 当前点击的行
                                        editableController.selectField(targetIndex = index)

                                    } else { // 非行选择模式，启动行选择模式 (multiple selection mode on)
                                        enableSelectMode(index)
                                    }
                                }

                        )

                    }
                }

                // TextField
                innerTextField(
                    Modifier
                        .weight(0.9f, true)
                        .align(Alignment.CenterVertically)
                )


            }
        }

        // Multiple Selection Menu
        if (textEditorState.value.isMultipleSelectionMode) {
            EditorMenus(
                modifier = Modifier
                    .systemBarsPadding()
                    .padding(8.dp)
                    .height(MyStyleKt.Editor.bottomBarHeight)
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                ,

                selectedLinesCount = textEditorState.value.getSelectedCount(),
                onDelete = onDelete@{
                    if (readOnlyMode) {
                        Msg.requireShow(appContext.getString(R.string.readonly_cant_edit))
                        return@onDelete
                    }

                    val selectedLinesNum = textEditorState.value.getSelectedCount();
                    if (selectedLinesNum < 1) {
                        Msg.requireShow(appContext.getString(R.string.no_line_selected))
                        return@onDelete
                    }

                    showDeleteDialog.value = true
                },
                onCopy = onCopy@{
                    val selectedLinesNum = textEditorState.value.getSelectedCount();
                    if (selectedLinesNum < 1) {
                        Msg.requireShow(appContext.getString(R.string.no_line_selected))
                        return@onCopy
                    }

                    clipboardManager.setText(AnnotatedString(textEditorState.value.getSelectedText()))
                    Msg.requireShow(replaceStringResList(appContext.getString(R.string.n_lines_copied), listOf(selectedLinesNum.toString())), )
                    textEditorState.value = textEditorState.value.createCopiedState()
                },

                onSelectAll = {
                    textEditorState.value = textEditorState.value.createSelectAllState()
                },
                onCancel = {
                    textEditorState.value = textEditorState.value.createCancelledState()
                },

            )
        }
//        SmallFab(modifier = Modifier.align(Alignment.BottomEnd), icon = Icons.Filled.Save, iconDesc = stringResource(id = R.string.save)) {
//
//        }
    }
}

private fun getLineNumber(index: Int): String {
//    return (index + 1).toString().padStart(3, '0')
    return (index + 1).toString()
}

@Composable
private fun getBackgroundColor(isSelected: Boolean, isMultiSelectionMode:Boolean): Color {
//    return if (isSelected) Color(0x806456A5) else Color.White
//    return if (isSelected) MaterialTheme.colorScheme.primaryContainer else Color.Unspecified
    return if (isMultiSelectionMode  &&  isSelected) {
        MaterialTheme.colorScheme.primaryContainer
    } else {
        Color.Unspecified
    }
//    return Color.Unspecified
}

@SuppressLint("ModifierFactoryUnreferencedReceiver")
private fun Modifier.bottomBorder(strokeWidth: Dp, color: Color) = composed(
    factory = {
        val density = LocalDensity.current
        val strokeWidthPx = density.run { strokeWidth.toPx() }

        Modifier.drawBehind {
            val width = size.width
            val height = size.height - strokeWidthPx / 2

            drawLine(
                color = color,
                start = Offset(x = 0f, y = height),
                end = Offset(x = width, y = height),
                strokeWidth = strokeWidthPx
            )
        }
    }
)

