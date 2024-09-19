package com.catpuppyapp.puppygit.compose

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconToggleButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.catpuppyapp.puppygit.play.pro.R
import com.catpuppyapp.puppygit.constants.Cons
import com.catpuppyapp.puppygit.dev.proFeatureEnabled
import com.catpuppyapp.puppygit.dev.treeToTreeBottomBarActAtLeastOneTestPassed
import com.catpuppyapp.puppygit.git.StatusTypeEntrySaver
import com.catpuppyapp.puppygit.utils.AppModel
import com.catpuppyapp.puppygit.utils.Libgit2Helper
import com.catpuppyapp.puppygit.utils.mime.iconRes


@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ChangeListItem(
    item: StatusTypeEntrySaver,
//    selectedItemList:SnapshotStateList<StatusTypeEntrySaver>,
    isFileSelectionMode: MutableState<Boolean>,
//    filesPageAddFileToSelectedListIfAbsentElseRemove: (StatusTypeEntrySaver) -> Unit,
    menuKeyTextList: List<String>,
    menuKeyActList: List<(StatusTypeEntrySaver)->Unit>,
    menuKeyEnableList: List<(StatusTypeEntrySaver)->Boolean>,
    menuKeyVisibleList: List<(StatusTypeEntrySaver)->Boolean> = listOf(),
    fromTo:String,
    switchItemSelected:(StatusTypeEntrySaver)->Unit,
    isItemInSelected:(StatusTypeEntrySaver)->Boolean,
//    treeOid1Str:String,
//    treeOid2Str:String,
    onLongClick:(StatusTypeEntrySaver)->Unit,
    onClick:(StatusTypeEntrySaver) -> Unit
){
    val navController = AppModel.singleInstanceHolder.navController
    val appContext = AppModel.singleInstanceHolder.appContext
    val haptic = AppModel.singleInstanceHolder.haptic


    Row(
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(
                onLongClick = {
                    onLongClick(item)
                }
            ){  //onClick
                onClick(item)
            }
            .then(
                //如果条目被选中，切换高亮颜色
                if (isItemInSelected(item)) Modifier.background(
                    MaterialTheme.colorScheme.primaryContainer

                    //then 里传 Modifier不会有任何副作用，还是当前的Modifier(即调用者自己：this)，相当于什么都没改，后面可继续链式调用其他方法
                ) else Modifier
            )
            ,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Start
    ) {
        Row(
            modifier = Modifier
                .defaultMinSize(minWidth = Dp.Unspecified, minHeight = 50.dp)
                .padding(5.dp)
                .fillMaxWidth(.9F),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Start

        ) {
            //在左侧加个复选框，会影响布局，有缺陷，别用了
//                                    if(isFileSelectionMode.value) {
//                                        IconToggleButton(checked = JSONObject(selFilePathListJsonObjStr.value).has(item.name), onCheckedChange = {
//                                            addIfAbsentElseRemove(item)
//                                        }) {
//                                            Icon(imageVector = Icons.Outlined.CheckCircle, contentDescription = stringResource(R.string.file_checked_indicator_icon))
//                                        }
//                                    }

            IconToggleButton(
//                enabled = fromTo!=Cons.gitDiffFromTreeToTree,  //diff提交时，禁用点击图标启动长按模式，按钮会变灰色，太难看了，弃用，改成点击后判断是否需要执行操作了，若不需要直接返回
                checked = isItemInSelected(item),
                onCheckedChange = cc@{
//                    if(fromTo!=Cons.gitDiffFromTreeToTree) {
//                    }

                    // tree to tree页面且底栏功能未测试通过，直接返回，不显示底栏
                    if(fromTo == Cons.gitDiffFromTreeToTree && !proFeatureEnabled(treeToTreeBottomBarActAtLeastOneTestPassed())) {
                        return@cc
                    }

                    switchItemSelected(item)

                }) {
                Icon(
//                    imageVector = Icons.Outlined.InsertDriveFile,
                    imageVector = item.getMime().iconRes,
                    contentDescription = stringResource(R.string.files_icon)
                )
            }
            Spacer(modifier = Modifier.padding(10.dp))
            Column {
                Row {
                    Text(
                            text = item.fileName,
                            fontSize = 20.sp,
                    )
                }
                val changeTypeColor = Libgit2Helper.getChangeTypeColor(item.changeType ?: "")
                val fontSize = 12.sp
                Row{
                    Text(item.changeType?:"", fontSize = fontSize, color = changeTypeColor)
                }
                Row {
                    Text(text = item.getSizeStr(),fontSize = fontSize, )
                }
                Row{
                    Text(text = item.getParentDirStr() ,fontSize = fontSize)
                }
            }

        }

        //每个条目都有自己的菜单项，这样有点费资源哈，不过实现起来最简单，如果只用一个菜单项也行，但难点在于把菜单项定位到点菜单按钮的地方
        val dropDownMenuExpendState = rememberSaveable { mutableStateOf(false) }  // typo: "Expend" should be "Expand"

        Row {
            //三点图标
            IconButton(onClick = { dropDownMenuExpendState.value = true }) {
                Icon(
                    imageVector = Icons.Filled.MoreVert,
                    contentDescription = stringResource(R.string.file_or_folder_menu)
                )
            }

            //菜单项，open/openAs/showInFiles之类的选项
            DropdownMenu(
                expanded = dropDownMenuExpendState.value,
                onDismissRequest = { dropDownMenuExpendState.value = false }
            ) {
                for((idx,v) in menuKeyTextList.withIndex()) {
                    if(menuKeyVisibleList.isNotEmpty() && !menuKeyVisibleList[idx](item)) {
                        continue
                    }

                    DropdownMenuItem(
                        enabled = menuKeyEnableList[idx](item),
                        text = { Text(v) },
                        onClick = {
                            //调用onClick()
                            menuKeyActList[idx](item)
                            //关闭菜单
                            dropDownMenuExpendState.value = false
                        }
                    )

                }
            }
        }

    }
}


