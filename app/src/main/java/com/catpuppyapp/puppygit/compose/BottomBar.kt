package com.catpuppyapp.puppygit.compose

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.catpuppyapp.puppygit.play.pro.R
import com.catpuppyapp.puppygit.style.MyStyleKt
import com.catpuppyapp.puppygit.utils.state.StateUtil

@Composable
fun BottomBar(
    modifier: Modifier=Modifier,
    getSelectedFilesCount:()->Int,
    height: Dp = MyStyleKt.BottomBar.height,
    color: Color = MaterialTheme.colorScheme.primaryContainer,
    quitSelectionMode: () -> Unit,
    iconList:List<ImageVector>,
    iconTextList:List<String>,
    iconDescTextList:List<String>,
    iconOnClickList:List<()->Unit>,
    iconEnableList:List<()->Boolean>,
    enableMoreIcon:Boolean,
    moreItemTextList:List<String>,
    moreItemOnClickList:List<()->Unit>,
    moreItemEnableList:List<()->Boolean>,
    iconVisibleList:List<()->Boolean> = listOf(),
    moreItemVisibleList:List<()->Boolean> = listOf()
) {
    val dropDownMenuExpendState = rememberSaveable { mutableStateOf(false) }
    val showDropDownMenu = {
        dropDownMenuExpendState.value=true
    }
    val closeDropDownMenu = {
        dropDownMenuExpendState.value=false
    }
    val switchDropDownMenu = {
        dropDownMenuExpendState.value = !dropDownMenuExpendState.value
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .systemBarsPadding()
            .then(modifier),
        verticalArrangement = Arrangement.Bottom,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier
                //禁用对BottomBar本身的点击，不然会穿透而点击到被BottomBar覆盖的条目，
                // 虽然禁用了对BottomBar本身的点击，不过Bar上按钮依然可以点击
                .clickable(enabled = false) {

                }
                .fillMaxWidth()
                .height(height)
                .background(color),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                //关闭按钮
                LongPressAbleIconBtn(
                    modifier = Modifier
                        .padding(10.dp)
                        .size(40.dp),
                    tooltipText = stringResource(R.string.close),
                    icon = Icons.Filled.Close,
                    iconContentDesc = stringResource(R.string.quit_selection_files_mode),
                    onClick = {
                        //退出选择模式
                        quitSelectionMode()
                    },
                )
                //选择的条目数
                Text(text = ""+getSelectedFilesCount())

            }

            Row(
                modifier = Modifier.horizontalScroll(StateUtil.getRememberScrollState()),
                horizontalArrangement = Arrangement.End  //加这个为了让图标靠右边，使列表从最右往左排，和最左边的关闭按钮分开
            ){
                Row (
                    verticalAlignment = Alignment.CenterVertically
                ){
                    for((idx, text) in iconTextList.withIndex()) {
                        //显示逻辑：显示列表为空，一律显示，不为空，取出对应索引的值，为true则显示
                        //实现代码：如果显示列表不为空 且 显示对应条目为假，continue，不显示条目
                        if(iconVisibleList.isNotEmpty() && !iconVisibleList[idx]()) {
                            continue
                        }

                        LongPressAbleIconBtn(
                            enabled = iconEnableList[idx](),
    //                        enabled = selectedFilePathNameMapList.value.isNotEmpty(),
                            tooltipText = text,
                            icon = iconList[idx],
                            iconContentDesc = iconDescTextList[idx],
                            onClick = {
                               iconOnClickList[idx]()
                            }
                        )
                    }
                }
//                x 废弃，没必要，随列表滚动足矣）把more改成固定在右边不要随列表滚动怎么样？
                //题外话：当时设计这个组件的时候没想到，其实用列表是否不为空来决定是否显示菜单图标就够了，不需要多一个开关变量
                //menu
                if (enableMoreIcon && moreItemTextList.isNotEmpty()) {
                    //菜单得单独开一行，不然DropdownMenu就定位到外部菜单的最左边了，就偏离菜单图标了，单独开一行就可以定位到菜单图标那里，完美
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {

                        //菜单图标
                        LongPressAbleIconBtn(
                            tooltipText = stringResource(R.string.menu),
                            icon = Icons.Filled.MoreVert,
                            iconContentDesc = stringResource(R.string.menu),
                            onClick = {
                                switchDropDownMenu()
                            }
                        )
                        //菜单列表
                        DropdownMenu(
                            expanded = dropDownMenuExpendState.value,
                            onDismissRequest = { closeDropDownMenu() }
                        ) {
                            for ((idx, text) in moreItemTextList.withIndex()) {
                                if(moreItemVisibleList.isNotEmpty() && !moreItemVisibleList[idx]()) {
                                    continue
                                }

                                DropdownMenuItem(
                                    enabled = moreItemEnableList[idx](),
                                    text = { Text(text) },
                                    onClick = {
                                        moreItemOnClickList[idx]()
                                        closeDropDownMenu()
                                    }
                                )

                            }
                        }
                    }

                }

            }


        }

    }
}