package jp.kaleidot725.texteditor.state

import androidx.compose.runtime.Immutable
import com.catpuppyapp.puppygit.utils.doActIfIndexGood
import jp.kaleidot725.texteditor.controller.EditorController.Companion.createInitTextFieldStates

@Immutable
data class TextEditorState(
    val fields: List<TextFieldState>,
    val selectedIndices: List<Int>,
    val isMultipleSelectionMode: Boolean,
) {
    fun getAllText(): String {
        val sb = StringBuilder()
        fields.forEach { sb.append(it.value.text).append("\n") }
        return sb.removeSuffix("\n").toString()

        //below code very slow when file over 1MB
//        return fields.map { it.value.text }.foldIndexed("") { index, acc, s ->
//            if (index == 0) acc + s else acc + "\n" + s
//        }
    }

    fun getSelectedText(): String {
        // 把索引排序，然后取出文本，拼接，返回
        val sb = StringBuilder()
        selectedIndices.toSortedSet().forEach { selectedLineIndex->

            //ps: fields是所有行集合，这段代码的作用是从所有行里根据索引取出当前选中的行，然后追加到StringBuilder中
            // filed 就是fields[selectedLineIndex]，doActIfIndexGood()的作用是仅当索引有效时，才会调用后面的函数，
            // 所以，如果selectedLineIndex是个无效索引，那后面的lambda就不会被执行，这样就避免了索引越界等异常
            doActIfIndexGood(selectedLineIndex, fields) { field ->
                sb.append(field.value.text).append("\n")
            }
        }

        //移除末尾多余的换行符，然后返回
//        return sb.removeSuffix("\n").toString()
        //保留末尾多出来的换行符，就是要让它多一个，不然复制多行时粘贴后会定位到最后一行开头，反直觉，要解决这个问题需要改掉整个行处理机制，太麻烦了，所以暂时这样规避下，其实这样倒合理，在粘贴内容到一行的中间部位时，感觉比之前还合理
        return sb.toString()


//        return targets.foldIndexed("") { index, acc, s ->
//            if (index == 0) acc + s else acc + "\n" + s
//        }
    }

    //获取选择行记数（获取选择了多少行）
    fun getSelectedCount():Int{
        return selectedIndices.toSet().filter{ it>=0 }.size  //toSet()是为了去重，我不确定是否一定没重复，去下保险；filter {it>=0} 是为了避免里面有-1，我记得初始值好像是往selectedIndices里塞个-1。
    }

    companion object {
        fun create(text: String, isMultipleSelectionMode:Boolean = false): TextEditorState {
            return create(text.lines(), isMultipleSelectionMode)

//            return TextEditorState(
//                fields = text.lines().createInitTextFieldStates(),
//                selectedIndices = listOf(-1),
//                isMultipleSelectionMode = false
//            )
        }

        fun create(lines: List<String>, isMultipleSelectionMode:Boolean = false): TextEditorState {
            return TextEditorState(
                fields = lines.createInitTextFieldStates(),
                selectedIndices = listOf(-1),
                isMultipleSelectionMode = isMultipleSelectionMode
            )
        }

        fun create(fields: List<TextFieldState>, selectedIndices: List<Int>, isMultipleSelectionMode: Boolean):TextEditorState {
            return TextEditorState(
                fields = fields,
                selectedIndices = selectedIndices,
                isMultipleSelectionMode = isMultipleSelectionMode
            )
        }
    }
}
