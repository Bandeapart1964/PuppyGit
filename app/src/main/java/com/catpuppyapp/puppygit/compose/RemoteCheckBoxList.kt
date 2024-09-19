package com.catpuppyapp.puppygit.compose

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.state.ToggleableState
import androidx.compose.ui.unit.dp
import com.catpuppyapp.puppygit.play.pro.R
import com.catpuppyapp.puppygit.utils.UIHelper
import com.catpuppyapp.puppygit.utils.state.StateUtil

@Composable
fun RemoteCheckBoxList(
    itemList:List<String>,
    selectedList:MutableList<String>,
    checkedList:MutableList<Boolean>,
    enabled:Boolean = true,  //是否启用勾选框
) {
    val getSelectedAllState = {
        //test, start
//        val sb = StringBuilder()
//        selectedList.forEach {sb.append(it).append(",")}
//        println("selectedList:${sb}")
        //test, end

        if(selectedList.isEmpty()) {
            ToggleableState.Off
        }else if(selectedList.size == itemList.size) {
            ToggleableState.On
        }else {
            ToggleableState.Indeterminate
        }
    }

    val selectAll = StateUtil.getRememberSaveableState(initValue = getSelectedAllState())

    val checkedListState = checkedList.map { StateUtil.getRememberSaveableState(initValue = it) }

    val selectItem = { name:String->
        UIHelper.selectIfNotInSelectedListElseNoop(name, selectedList)
    }

    val removeItem = {name:String->
        selectedList.remove(name)
    }

    val showChildren = StateUtil.getRememberSaveableState(initValue = false)

    Row (
        verticalAlignment = Alignment.CenterVertically,
    ){
        // checkbox
        Row (modifier = Modifier.fillMaxWidth(0.8f)){
            MyTriCheckBox(text = stringResource(R.string.all)+" (${selectedList.size}/${itemList.size})", state = selectAll.value, enabled=enabled) {
                //如果不是选中所有，添加所有元素到列表，然后切换状态为选中所有；如选中所有，清空选中列表，然后设置状态为未选中任何条目
                if(selectAll.value!=ToggleableState.On) {
                    itemList.forEach { selectItem(it) }
                    checkedListState.forEachIndexed {idx, it -> it.value = true; checkedList[idx]=true }
                    selectAll.value = ToggleableState.On
                }else {
                    selectedList.clear()
                    checkedListState.forEachIndexed {idx, it-> it.value = false; checkedList[idx]=false }
                    selectAll.value = ToggleableState.Off
                }
            }

        }

        //show/hidden btn
        Row(
            horizontalArrangement = Arrangement.End
        ) {
            LongPressAbleIconBtn(
                tooltipText = stringResource(R.string.show_hidden_items),
                icon = if (showChildren.value) Icons.Filled.KeyboardArrowDown else Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                iconContentDesc =stringResource(R.string.show_hidden_items)
            ) {
                showChildren.value = !showChildren.value
            }

        }

    }

    if(showChildren.value) {
        itemList.forEachIndexed {idx, name->
            val v = checkedListState[idx]
            Row(modifier = Modifier.padding(start = 10.dp)) {
                MyCheckBox(text = name, value = v, enabled = enabled) {
                    //更新状态变量值
                    v.value = it
                    checkedList[idx] = it  //更新原始列表的值，这样下次初始化列表就会恢复上次选中的条目了，一定程度上方便一些

                    //添加或移除元素
                    if(it) {
                        selectItem(name)
                    }else {
                        removeItem(name)
                    }

                    selectAll.value = getSelectedAllState()
                }

            }
        }
    }
}
