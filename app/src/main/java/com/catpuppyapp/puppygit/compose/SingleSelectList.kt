package com.catpuppyapp.puppygit.compose

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowLeft
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableIntState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import com.catpuppyapp.puppygit.utils.UIHelper
import com.catpuppyapp.puppygit.utils.isGoodIndexForList
import com.catpuppyapp.puppygit.utils.state.StateUtil

//下拉单选框，不过好像在弹窗使用会崩溃，可能是谷歌bug(20241003 fixed)
//@OptIn(ExperimentalFoundationApi::class)
//@Deprecated("may crashed if use this in dialog")  // 20241003 update: new version of jetpack compose are fixed this bug
@Composable
fun<T> SingleSelectList(
//    outterModifier: Modifier = Modifier.fillMaxWidth(),
//    dropDownMenuModifier:Modifier=Modifier.fillMaxWidth(),
    outterModifier: Modifier = Modifier,
    dropDownMenuModifier:Modifier=Modifier,

    optionsList:List<T>,   // empty list will show "null" and no item for select
    selectedOptionIndex:MutableIntState?,
    selectedOptionValue:T? = if(selectedOptionIndex!=null && isGoodIndexForList(selectedOptionIndex.intValue, optionsList)) optionsList[selectedOptionIndex.intValue] else null,

    // 显示已选中条目时，会传selectedOptionIndex作为index值，其值可能为null
    // when show selected item, will passing selectedOptionIndex as index, the value maybe null
    // for the formatter, index will be null if value not in the list, and index maybe invalid, like -1,-2 something... usually happened when you was selected a item, but remove it from list later, the the selected item will haven't index of the list, should handle this case, if you overwrite this formatter
    menuItemFormatter:(index:Int?, value:T?)->String = {index, value-> value?.toString() ?: ""},
    menuItemOnClick:(index:Int, value:T)->Unit = {index, value-> selectedOptionIndex?.intValue = index},
    menuItemSelected:(index:Int, value:T) -> Boolean = {index, value -> selectedOptionIndex?.intValue == index},

    menuItemTrailIcon:ImageVector?=null,
    menuItemTrailIconDescription:String?=null,
    menuItemTrailIconEnable:(index:Int, value:T)->Boolean = {index, value-> true},
    menuItemTrailIconOnClick:(index:Int, value:T) ->Unit = {index, value->},
) {
    val expandDropdownMenu = StateUtil.getRememberSaveableState(initValue = false)

    val containerSize = remember { mutableStateOf(IntSize.Zero) }

    Card(
        //0.9f 占父元素宽度的百分之90
        modifier = outterModifier
            .clickable {
                expandDropdownMenu.value = !expandDropdownMenu.value
            }
            .onSizeChanged {
                containerSize.value = it
            }
        ,
        colors = CardDefaults.cardColors(
            containerColor = UIHelper.defaultCardColor(),
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 3.dp
        )

    ) {
        Box(modifier = Modifier
            .padding(start = 5.dp, end = 5.dp)
            .defaultMinSize(minHeight = 50.dp)
//            .fillMaxWidth(),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth(.92f)
                    .align(Alignment.CenterStart)

            ) {
                Text(text = menuItemFormatter(selectedOptionIndex?.intValue, selectedOptionValue))
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth(.08f)
                    .align(Alignment.CenterEnd)
            ) {
                Icon(imageVector = if(expandDropdownMenu.value) Icons.Filled.ArrowDropDown else Icons.AutoMirrored.Filled.ArrowLeft
                    , contentDescription = null
                )
            }
        }


        DropdownMenu(
            modifier = dropDownMenuModifier.width((containerSize.value.width/2).dp),

            expanded = expandDropdownMenu.value,
            onDismissRequest = { expandDropdownMenu.value=false }
        ) {
            for ((index, value) in optionsList.toList().withIndex()) {
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
                        text = { Text(if(menuItemSelected(index, value)) "*${menuItemFormatter(index, value)}" else menuItemFormatter(index, value)) },
                        onClick ={
                            expandDropdownMenu.value=false

                            menuItemOnClick(index, value)
                        },
                        trailingIcon = {
                            if(menuItemTrailIcon!=null) {
                                IconButton(
                                    enabled = menuItemTrailIconEnable(index, value),
                                    onClick = {
                                        menuItemTrailIconOnClick(index, value)
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
