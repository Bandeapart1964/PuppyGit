package com.catpuppyapp.puppygit.fileeditor.ui.extension

import com.catpuppyapp.puppygit.utils.isGoodIndexForList
import com.catpuppyapp.puppygit.fileeditor.texteditor.state.TextEditorState
import com.catpuppyapp.puppygit.fileeditor.texteditor.state.TextFieldState

/**
 * 注：若index为无效索引，初始不会选择任何行
 */
fun TextEditorState.createMultipleSelectionModeState(index:Int): TextEditorState {

    //用14431行的近2mb文件简单测试了下，性能还行
    //进入选择模式，数据不变，只是 MultipleSelectionMode 设为true且对应行的isSelected设为true

    //关闭所有条目的选中状态
    val newFieldsList = fields.mapIndexed{idx,it ->
        it.copy(isSelected = idx==index)  //为当前点击的条目开启选中状态。注：若index为-1或其他无效索引值，则不会选中任何行。
    }

//    newFieldsList[index] = fields[index].copy(isSelected = true)    //不需要这个了，直接在重映射（map）的时候选中当前行了
    //debug
//    newFieldsList.forEach{ println(it.isSelected)}
    //debug

    return TextEditorState.create(
        fields = newFieldsList,  //把当前点击而开启选择行模式的那行的选中状态设为真了
        selectedIndices = if(isGoodIndexForList(index, newFieldsList)) listOf(index) else listOf(),  //默认选中的索引包含当前选中行即可，因为肯定是点击某一行开启选中模式的，所以只会有一个索引
        isMultipleSelectionMode = true,
    )
}

fun TextEditorState.createCopiedState(): TextEditorState {
    return TextEditorState.create(
//        fields = fields.map { TextFieldState(it.id, it.value, isSelected = false)},  //对所有选中行解除选中
//        isMultipleSelectionMode = false,  //退出选择模式

        fields = fields,
        selectedIndices = selectedIndices,
        isMultipleSelectionMode = isMultipleSelectionMode,  //拷贝和删除不要退出选择模式(准确来说是不要改变是否处于选择模式，若以后以非多选模式创建CopiedState，也不会自动进入选择模式)，这样用户可间接实现剪切功能，因为选择很多行有时候很废力，所以除非用户明确按取消，否则不要自动解除选择模式
    )
}

fun TextEditorState.createSelectAllState(): TextEditorState {
    //TODO 这里应该可以避免拷贝吧？不过这里是引用拷贝，问题不大
    val selectedIndexList = mutableListOf<Int>()
    val selectedFieldList = mutableListOf<TextFieldState>()
    for((idx, f) in fields.withIndex()) {
        selectedIndexList.add(idx)
        selectedFieldList.add(TextFieldState(f.id,f.value, isSelected = true))
    }
    //我不太确定 data类的copy是深还是浅，但我可以确定这里不需要深拷贝，所以用下面创建浅拷贝的方法创建对象
    return TextEditorState.create(
        fields = selectedFieldList,
        selectedIndices = selectedIndexList,
        isMultipleSelectionMode = true,
    )
}

fun TextEditorState.createDeletedState(): TextEditorState {
    //把没选中的行取出来，作为新的文件内容
    val newFields = fields.filterIndexed { index, _ ->
        !selectedIndices.contains(index)
    }

    //如果选中行列表selectedIndices无重复索引且不可能有错误索引的话，
    // 可简单对比已选中行selectedIndices.size和fields.size来判断有无选中所有行，
    // 但我不确定selectedIndices是否会有重复索引和是否会有无效索引，
    // 所以这里暂时不用size判断是否选中了所有行

    //判断是否删除了所有行，如果新的文件内容列表newFields一个元素都没有，则说明全删了
    val isDeletedAll = newFields.isEmpty()

    //如果是删除所有，创建一个空状态；否则创建删除选中行后的状态
    return if(isDeletedAll) TextEditorState.create("", isMultipleSelectionMode)  //即使全删了，完全创建新状态，也不要影响选择模式，要不然有的情况自动退选择模式，有的不退，容易让人感到混乱
           else TextEditorState.create(fields = newFields, selectedIndices= emptyList(), isMultipleSelectionMode = isMultipleSelectionMode  //一般来说此值在这会是true，不过，这里的语义是“不修改是否选择模式”，所以把这个字段传过去比直接设为true要合适
    )
}

fun TextEditorState.createCancelledState(): TextEditorState {
    return TextEditorState.create(
        fields = fields.map { TextFieldState(it.id, it.value, isSelected = false) },
        selectedIndices = emptyList(),
        isMultipleSelectionMode = false,
    )
}
