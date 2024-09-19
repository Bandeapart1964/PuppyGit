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
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.catpuppyapp.puppygit.play.pro.R
import com.catpuppyapp.puppygit.dto.FileItemDto
import com.catpuppyapp.puppygit.ui.theme.Theme
import com.catpuppyapp.puppygit.utils.mime.iconRes


@OptIn(ExperimentalFoundationApi::class)
@Composable
fun FileListItem(
    item: FileItemDto,
    isPasteMode: MutableState<Boolean>,
    menuKeyTextList: List<String>,
    menuKeyActList: List<(FileItemDto)->Unit>,
    switchItemSelected:(FileItemDto)->Unit,
    iconOnClick:()->Unit,
    isItemInSelected:(FileItemDto)->Boolean,
    itemOnLongClick:(FileItemDto)->Unit,
    itemOnClick:(FileItemDto)->Unit,
){
    val inDarkTheme = Theme.inDarkTheme
    val alpha = 0.6f
    val iconColor = if(item.isHidden) LocalContentColor.current.copy(alpha = alpha) else LocalContentColor.current
    val fontColor = if(item.isHidden) {if(inDarkTheme) Color.White.copy(alpha = alpha) else Color.Black.copy(alpha = alpha)} else Color.Unspecified

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = {
                    itemOnClick(item)
                },
                onLongClick = {
                    itemOnLongClick(item)

                }
            )
            .then(
                if (isItemInSelected(item)) Modifier.background(
                    MaterialTheme.colorScheme.primaryContainer
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
//                enabled = fromTo!=Cons.gitDiffFromTreeToTree,  //diff提交时，禁用点击图标启动长按模式，按钮会变灰色，太难看了，弃用
                checked = isItemInSelected(item),
                onCheckedChange = {
                    iconOnClick()
                }) {
                Icon(
//                    imageVector = if (item.isFile) Icons.Outlined.InsertDriveFile else Icons.Filled.Folder,
                    imageVector = item.mime.iconRes,
                    contentDescription = stringResource(R.string.file_or_folder_icon),
                    tint =iconColor
                )
            }
            Spacer(modifier = Modifier.padding(10.dp))
            Column {
                Row {
                    Text(
                            text = item.name,
                            fontSize = 20.sp,
                            color = fontColor
                    )
                }
                Row{
                    Text(item.lastModifiedTime?:"", fontSize = 12.sp,
                        color = fontColor

                        )
                }
                Row {
                    Text(text = item.sizeInHumanReadable,fontSize = 12.sp,
                        color = fontColor

                        )
                }
            }

        }
        //每个条目都有自己的菜单项，这样有点费资源哈，不过实现起来最简单，如果只用一个菜单项也行，但难点在于把菜单项定位到点菜单按钮的地方
        val dropDownMenuExpendState = rememberSaveable { mutableStateOf(false) }

        Row {
            IconButton(onClick = { dropDownMenuExpendState.value = true }) {
                Icon(
                    imageVector = Icons.Filled.MoreVert,
                    contentDescription = stringResource(R.string.file_or_folder_menu)
                )
            }
            DropdownMenu(
                expanded = dropDownMenuExpendState.value,
                onDismissRequest = { dropDownMenuExpendState.value = false }
            ) {
                for ((idx,v) in menuKeyTextList.withIndex()) {
                    //忽略空白选项，这样未启用的feature就可直接用空白替代了，方便
                    if(v.isBlank()) {
                        continue
                    }

                    DropdownMenuItem(
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


