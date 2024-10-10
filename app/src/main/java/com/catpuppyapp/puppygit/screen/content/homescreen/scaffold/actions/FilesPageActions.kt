package com.catpuppyapp.puppygit.screen.content.homescreen.scaffold.actions

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.FilterAlt
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.TextFieldValue
import com.catpuppyapp.puppygit.compose.LongPressAbleIconBtn
import com.catpuppyapp.puppygit.constants.PageRequest
import com.catpuppyapp.puppygit.play.pro.R
import com.catpuppyapp.puppygit.style.MyStyleKt
import com.catpuppyapp.puppygit.utils.state.CustomStateSaveable
import com.catpuppyapp.puppygit.utils.state.StateUtil


@Composable
fun FilesPageActions(
    showCreateFileOrFolderDialog: MutableState<Boolean>,
    refreshPage: () -> Unit,
    filterOn: () -> Unit,
    filesPageGetFilterMode:()->Int,
    doFilter:(String)->Unit,
    requestFromParent:MutableState<String>,
    filesPageSimpleFilterOn:MutableState<Boolean>,
    filesPageSimpleFilterKeyWord:CustomStateSaveable<TextFieldValue>
) {

    val dropDownMenuExpendState = rememberSaveable { mutableStateOf(false)}

//    if(filesPageGetFilterMode()==0) {
//        LongPressAbleIconBtn(
//            tooltipText = stringResource(R.string.filter_files),
//            icon = Icons.Filled.FilterAlt,
//            iconContentDesc = stringResource(id = R.string.filter_files),
//        ) {
//            filterOn()
//        }
//    }
//
//    if(filesPageGetFilterMode() == 1) {
//        LongPressAbleIconBtn(
//            tooltipText = stringResource(R.string.do_filter),
//            icon = Icons.Filled.Check,
//            iconContentDesc = stringResource(id = R.string.do_filter),
//        ) {
//                //如果为空，尝试从keyword state获取值
//                doFilter("")
//        }
//    }

    if(!filesPageSimpleFilterOn.value) { //没filter的常规模式或显示filter结果模式
        LongPressAbleIconBtn(
            tooltipText = stringResource(R.string.filter_files),
            icon = Icons.Filled.FilterAlt,
            iconContentDesc = stringResource(id = R.string.filter_files),
        ) {
            filesPageSimpleFilterKeyWord.value = TextFieldValue("")
            filesPageSimpleFilterOn.value = true
        }

//        LongPressAbleIconBtn(
//            tooltipText = stringResource(R.string.go_to_top),
//            icon = Icons.Filled.VerticalAlignTop,
//            iconContentDesc = stringResource(id = R.string.go_to_top),
//        ) {
//            requestFromParent.value=PageRequest.goToTop
//        }

        LongPressAbleIconBtn(
            tooltipText = stringResource(R.string.refresh),
            icon = Icons.Filled.Refresh,
            iconContentDesc = stringResource(id = R.string.refresh),
        ) {
            refreshPage()
        }
        LongPressAbleIconBtn(
            tooltipText = stringResource(R.string.create),
            icon = Icons.Filled.Add,
            iconContentDesc = stringResource(id = R.string.create),
        ) {
            requestFromParent.value=PageRequest.createFileOrFolder
        }


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
        Row(modifier = Modifier.padding(top = MyStyleKt.TopBar.dropDownMenuTopPaddingSize)) {
            val enableMenuItem = true
            //菜单列表
            DropdownMenu(
                expanded = dropDownMenuExpendState.value,
                onDismissRequest = { dropDownMenuExpendState.value = false }
            ) {
                DropdownMenuItem(
                    enabled = enableMenuItem,
                    text = { Text(stringResource(R.string.internal_storage)) },
                    onClick = {
                        requestFromParent.value = PageRequest.goToInternalStorage
                        dropDownMenuExpendState.value = false
                    }
                )
                DropdownMenuItem(
                    enabled = enableMenuItem,
                    text = { Text(stringResource(R.string.external_storage)) },
                    onClick = {
                        requestFromParent.value = PageRequest.goToExternalStorage
                        dropDownMenuExpendState.value = false
                    }
                )
                DropdownMenuItem(
                    enabled = enableMenuItem,
                    text = { Text(stringResource(R.string.go_to)) },
                    onClick = {
                        requestFromParent.value = PageRequest.goToPath
                        dropDownMenuExpendState.value = false
                    }
                )

                //拷贝app内相对路径
//                DropdownMenuItem(
//                    enabled = enableMenuItem,
//                    text = { Text(stringResource(R.string.copy_path)) },
//                    onClick = {
//                        requestFromParent.value = PageRequest.copyPath
//                        dropDownMenuExpendState.value = false
//                    }
//                )

                //拷贝真实路径， /storage/em/0/xx/x/x/x/那种，真实的路径
//                DropdownMenuItem(
//                    enabled = enableMenuItem,
//                    text = { Text(stringResource(R.string.copy_real_path)) },
//                    onClick = {
//                        requestFromParent.value = PageRequest.copyRealPath
//                        dropDownMenuExpendState.value = false
//                    }
//                )
            }
        }
    }
}

