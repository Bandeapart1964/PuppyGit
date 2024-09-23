package com.catpuppyapp.puppygit.compose

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ArrowLeft
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.catpuppyapp.puppygit.utils.state.StateUtil

//下拉单选框，不过好像在弹窗使用会崩溃，可能是谷歌bug
@OptIn(ExperimentalFoundationApi::class)
@Deprecated("may crashed if use this in dialog")
@Composable
fun SingleSelectList(
    outterModifier: Modifier = Modifier.fillMaxWidth(),
    dropDownMenuModifier:Modifier=Modifier.fillMaxWidth(),

    optionsList:List<String>,
    selectedOptionIndex:Int,
    selectedOptionValue:String,

    menuItemOnClick:(index:Int, value:String)->Unit = {index, value->},

    menuItemTrailIcon:ImageVector?=null,
    menuItemTrailIconDescription:String?=null,
    menuItemTrailIconEnable:(index:Int, value:String)->Boolean = {index, value-> true},
    menuItemTrailIconOnClick:(index:Int, value:String) ->Unit = {index, value->},
) {
    val expandDropdownMenu = StateUtil.getRememberSaveableState(initValue = false)

    Card(
        //0.9f 占父元素宽度的百分之90
        modifier = outterModifier
            .clickable {
                expandDropdownMenu.value = !expandDropdownMenu.value
            }
        ,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 6.dp
        )

    ) {
        Row(modifier = Modifier
            .padding(start = 30.dp, end = 30.dp)
            .defaultMinSize(minHeight = 50.dp)
            .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row {
                Text(text = selectedOptionValue)
            }

            Row {
                Icon(imageVector = if(expandDropdownMenu.value) Icons.Filled.ArrowDropDown else Icons.Filled.ArrowLeft
                    , contentDescription = null
                )
            }
        }

        DropdownMenu(
            modifier = dropDownMenuModifier,

            expanded = expandDropdownMenu.value,
            onDismissRequest = { expandDropdownMenu.value=false }
        ) {
            for ((k, optext) in optionsList.toList().withIndex()) {
                //忽略当前显示条目
                //不忽略了，没必要，显示的是选中条目，一点击，展开的菜单里是所有条目，也很合理
//            if(k == selectedOption.intValue) {
//                continue
//            }

                Row (
                    horizontalArrangement = Arrangement.SpaceBetween
                ){
                    //列出其余条目
                    DropdownMenuItem(
                        text = { Text(if(selectedOptionIndex == k ) "*$optext" else optext) },
                        onClick ={
                            expandDropdownMenu.value=false

                            menuItemOnClick(k, optext)
                        },
                        trailingIcon = {
                            if(menuItemTrailIcon!=null) {
                                IconButton(
                                    enabled = menuItemTrailIconEnable(k, optext),
                                    onClick = {
                                        menuItemTrailIconOnClick(k, optext)
                                    }
                                ) {
                                    Icon(
                                        imageVector = menuItemTrailIcon,
                                        contentDescription = menuItemTrailIconDescription
                                    )
                                }
                            }else {
                                null
                            }
                        }
                    )


                }

            }
        }
    }

}
