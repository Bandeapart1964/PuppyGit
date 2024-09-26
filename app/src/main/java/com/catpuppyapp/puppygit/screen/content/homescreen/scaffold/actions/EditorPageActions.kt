package com.catpuppyapp.puppygit.screen.content.homescreen.scaffold.actions

import FontSizeAdjuster
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.CheckBox
import androidx.compose.material.icons.filled.CheckBoxOutlineBlank
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableIntState
import androidx.compose.runtime.MutableState
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import com.catpuppyapp.puppygit.compose.LongPressAbleIconBtn
import com.catpuppyapp.puppygit.constants.PageRequest
import com.catpuppyapp.puppygit.dev.dev_EnableUnTestedFeature
import com.catpuppyapp.puppygit.dev.editorEnableLineSelecteModeFromMenuTestPassed
import com.catpuppyapp.puppygit.dev.editorFontSizeTestPassed
import com.catpuppyapp.puppygit.dev.editorHideOrShowLineNumTestPassed
import com.catpuppyapp.puppygit.dev.editorLineNumFontSizeTestPassed
import com.catpuppyapp.puppygit.dev.editorMergeModeTestPassed
import com.catpuppyapp.puppygit.dev.editorSearchTestPassed
import com.catpuppyapp.puppygit.dev.proFeatureEnabled
import com.catpuppyapp.puppygit.play.pro.R
import com.catpuppyapp.puppygit.settings.SettingsCons
import com.catpuppyapp.puppygit.settings.SettingsUtil
import com.catpuppyapp.puppygit.style.MyStyleKt
import com.catpuppyapp.puppygit.user.UserUtil
import com.catpuppyapp.puppygit.utils.AppModel
import com.catpuppyapp.puppygit.utils.FsUtils
import com.catpuppyapp.puppygit.utils.state.CustomStateSaveable
import com.catpuppyapp.puppygit.utils.state.StateUtil
import jp.kaleidot725.texteditor.state.TextEditorState

@Composable
fun EditorPageActions(
    editorPageShowingFilePath: MutableState<String>,
//    editorPageRequireOpenFilePath: MutableState<String>,
    editorPageShowingFileIsReady: MutableState<Boolean>,
    needRefreshEditorPage: MutableState<String>,
    editorPageTextEditorState: CustomStateSaveable<TextEditorState>,
//    editorPageShowSaveDoneToast: MutableState<Boolean>,
    isSaving: MutableState<Boolean>,
    isEdited: MutableState<Boolean>,
    showReloadDialog: MutableState<Boolean>,
    showCloseDialog: MutableState<Boolean>,
    closeDialogCallback:CustomStateSaveable<(Boolean)->Unit>,
    doSave:suspend ()->Unit,
    loadingOn:(String)->Unit,
    loadingOff:()->Unit,
    editorPageRequest:MutableState<String>,
    editorPageSearchMode:MutableState<Boolean>,
    editorPageMergeMode:MutableState<Boolean>,
    readOnlyMode:MutableState<Boolean>,
    editorSearchKeyword:String,
    isSubPageMode:Boolean,
    fontSize:MutableIntState,
    lineNumFontSize:MutableIntState,
    adjustFontSizeMode:MutableState<Boolean>,
    adjustLineNumFontSizeMode:MutableState<Boolean>,
    showLineNum:MutableState<Boolean>
) {
    /*
        注意：如果以后需要同一个EditorInnerPage配合多个title，就不要在这执行操作了，把这里的action逻辑放到EditorInnerPage执行，在这只发request，类似ChangeList页面请求执行pull/push那样
     */

    val haptic = AppModel.singleInstanceHolder.haptic

    val hasGoodKeyword = editorSearchKeyword.isNotEmpty()

    //这几个模式互斥，其实可以做成枚举
    if(editorPageSearchMode.value) {
        LongPressAbleIconBtn(
            enabled = hasGoodKeyword,
            tooltipText = stringResource(R.string.find_previous),
            icon = Icons.Filled.ArrowUpward,
            iconContentDesc = stringResource(R.string.find_previous),
        ) {
            editorPageRequest.value = PageRequest.findPrevious
        }

        LongPressAbleIconBtn(
            enabled = hasGoodKeyword,
            tooltipText = stringResource(R.string.find_next),
            icon = Icons.Filled.ArrowDownward,
            iconContentDesc = stringResource(R.string.find_next),

            onLongClick = {
                //震动反馈
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)

                //显示功能提示和所有记数“FindNext(all:40)”，其中40是全文查找到的匹配关键字数量，文案尽量紧凑，避免toast显示不全
                editorPageRequest.value = PageRequest.showFindNextAndAllCount

            }
        ) {
            editorPageRequest.value = PageRequest.findNext
        }

        return  //返回，以免显示菜单项
    }else if(adjustFontSizeMode.value) {
        FontSizeAdjuster(fontSize = fontSize, resetValue = SettingsCons.defaultFontSize)

        return
    }else if(adjustLineNumFontSizeMode.value) {
        FontSizeAdjuster(fontSize = lineNumFontSize, resetValue = SettingsCons.defaultLineNumFontSize)

        return
    }

    val appContext = LocalContext.current

    val dropDownMenuExpendState = StateUtil.getRememberSaveableState(initValue = false)

    val closeMenu = {dropDownMenuExpendState.value = false}

    val enableMenuItem = editorPageShowingFilePath.value.isNotBlank()

    //是否显示三点菜单图标，以后可能会增加其他判断因素，所以单独弄个变量
    val showMenuIcon = enableMenuItem

    if(enableMenuItem && editorPageMergeMode.value) {
        LongPressAbleIconBtn(
            tooltipText = stringResource(R.string.previous_conflict),
            icon = Icons.Filled.ArrowUpward,
            iconContentDesc = stringResource(R.string.previous_conflict),
        ) {
            editorPageRequest.value = PageRequest.previousConflict
        }

        LongPressAbleIconBtn(
            tooltipText = stringResource(R.string.next_conflict),
            icon = Icons.Filled.ArrowDownward,
            iconContentDesc = stringResource(R.string.next_conflict),

            onLongClick = {
                //震动反馈
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)

                //显示功能提示和所有记数“NextConflict(all:40)”，其中40是全文查找到的所有冲突数量，文案尽量紧凑，避免toast显示不全
                editorPageRequest.value = PageRequest.showNextConflictAndAllConflictsCount

            }
        ) {
            editorPageRequest.value = PageRequest.nextConflict
        }
    }

    if(showMenuIcon) {
        //菜单图标
        LongPressAbleIconBtn(
            //这种需展开的菜单，禁用内部的选项即可
//        enabled = enableAction.value,

            tooltipText = stringResource(R.string.menu),
            icon = Icons.Filled.MoreVert,
            iconContentDesc = stringResource(R.string.menu),
            onClick = {
                //切换菜单展开状态
                dropDownMenuExpendState.value = !dropDownMenuExpendState.value
            }
        )
    }

    //菜单列表
    Row(modifier = Modifier.padding(top = MyStyleKt.TopBar.dropDownMenuTopPaddingSize)) {
//        val enableMenuItem = enableAction.value && !changeListPageNoRepo.value  && !hasTmpStatus

        //菜单列表
        DropdownMenu(
            expanded = dropDownMenuExpendState.value,
            onDismissRequest = { closeMenu() }
        ) {
            DropdownMenuItem(
                enabled = enableMenuItem,

                text = { Text(stringResource(R.string.close)) },
                onClick = {
                    showCloseDialog.value=true

                    closeMenu()
                }
            )

            DropdownMenuItem(
                enabled = enableMenuItem,

                text = { Text(stringResource(R.string.reload_file)) },
                onClick = {
                    showReloadDialog.value = true

                    closeMenu()
                }
            )

            DropdownMenuItem(
                enabled = enableMenuItem && editorPageShowingFileIsReady.value && isEdited.value && !isSaving.value && !readOnlyMode.value,  //文件未就绪时不能保存,

                text = { Text(stringResource(R.string.save)) },
                onClick = {
                    editorPageRequest.value = PageRequest.requireSave

                    closeMenu()
                }
            )

            DropdownMenuItem(
                enabled = enableMenuItem,

                text = { Text(stringResource(R.string.open_as)) },
                onClick = {
                    editorPageRequest.value = PageRequest.requireOpenAs

                    closeMenu()
                }
            )

            if(UserUtil.isPro() && (dev_EnableUnTestedFeature || editorSearchTestPassed)){
                DropdownMenuItem(
                    enabled = enableMenuItem,

//                    text = { Text(stringResource(R.string.search)) },  //纠结一番，感觉search不如find合适
                    text = { Text(stringResource(R.string.find)) },
                    onClick = {
//                        editorPageSearchMode.value = true  //需要初始化搜索位置，所以不能简单设为true开启，不过可以简单设为false来关闭搜索模式
                        editorPageRequest.value = PageRequest.requireSearch  //发请求，由TextEditor组件开启搜索模式
                        closeMenu()
                    }
                )
            }

            if(UserUtil.isPro() && (dev_EnableUnTestedFeature || editorMergeModeTestPassed)){
                DropdownMenuItem(
                    enabled = enableMenuItem,
                    text = { Text(stringResource(R.string.merge_mode)) },
                    trailingIcon = {
                        Icon(
                            imageVector = if(editorPageMergeMode.value) Icons.Filled.CheckBox else Icons.Filled.CheckBoxOutlineBlank,
                            contentDescription = null
                        )
                    },
                    onClick = {
                        editorPageMergeMode.value = !editorPageMergeMode.value

                        closeMenu()
                    }

                )
            }

            DropdownMenuItem(
                //非readOnly目录才允许开启或关闭readonly状态，否则强制启用readonly状态且不允许关闭
                enabled = enableMenuItem && !FsUtils.isReadOnlyDir(editorPageShowingFilePath.value),
                text = { Text(stringResource(R.string.read_only)) },
                trailingIcon = {
                    Icon(
                        imageVector = if(readOnlyMode.value) Icons.Filled.CheckBox else Icons.Filled.CheckBoxOutlineBlank,
                        contentDescription = null
                    )
                },
                onClick = {
                    //如果是从非readonly mode切换到readonly mode，则执行一次保存，然后再切换readonly mode
                    editorPageRequest.value = PageRequest.doSaveIfNeedThenSwitchReadOnly

                    closeMenu()
                }

            )

            if(!isSubPageMode) {
                DropdownMenuItem(
                    //非readOnly目录才允许开启或关闭readonly状态，否则强制启用readonly状态且不允许关闭
                    enabled = enableMenuItem,
                    text = { Text(stringResource(R.string.show_in_files)) },
                    onClick = {
                        editorPageRequest.value = PageRequest.showInFiles

                        closeMenu()
                    }

                )

            }

            if(proFeatureEnabled(editorFontSizeTestPassed)) {
                DropdownMenuItem(
                    enabled = enableMenuItem,
                    text = { Text(stringResource(R.string.font_size)) },
                    onClick = {
                        closeMenu()

                        adjustFontSizeMode.value = true
                    }
                )
            }

            if(proFeatureEnabled(editorLineNumFontSizeTestPassed)) {
                DropdownMenuItem(
                    enabled = enableMenuItem && showLineNum.value,
                    text = { Text(stringResource(R.string.line_num_size)) },
                    onClick = {
                        closeMenu()

                        adjustLineNumFontSizeMode.value = true
                    }

                )
            }

            if(proFeatureEnabled(editorHideOrShowLineNumTestPassed)) {
                DropdownMenuItem(
                    //非readOnly目录才允许开启或关闭readonly状态，否则强制启用readonly状态且不允许关闭
                    enabled = enableMenuItem,
                    text = { Text(stringResource(R.string.show_line_num)) },
                    trailingIcon = {
                        Icon(
                            imageVector = if(showLineNum.value) Icons.Filled.CheckBox else Icons.Filled.CheckBoxOutlineBlank,
                            contentDescription = null
                        )
                    },
                    onClick = {
                        closeMenu()

                        //切换
                        showLineNum.value = !showLineNum.value

                        //保存
                        SettingsUtil.update {
                            it.editor.showLineNum = showLineNum.value
                        }
                    }
                )
            }

            if(proFeatureEnabled(editorEnableLineSelecteModeFromMenuTestPassed)) {
                val selectModeOn = editorPageTextEditorState.value.isMultipleSelectionMode

                DropdownMenuItem(
                    //非readOnly目录才允许开启或关闭readonly状态，否则强制启用readonly状态且不允许关闭
                    enabled = enableMenuItem,
                    text = { Text(stringResource(R.string.select_mode)) },
                    trailingIcon = {
                        Icon(
                            imageVector = if(selectModeOn) Icons.Filled.CheckBox else Icons.Filled.CheckBoxOutlineBlank,
                            contentDescription = null
                        )
                    },
                    onClick = {
                        closeMenu()

                        //如果是从非readonly mode切换到readonly mode，则执行一次保存，然后再切换readonly mode
                        editorPageRequest.value = PageRequest.editorSwitchSelectMode
                    }

                )
            }

        }
    }
}
