package jp.kaleidot725.texteditor.view

import android.view.KeyEvent.KEYCODE_DEL
import android.view.KeyEvent.KEYCODE_DPAD_DOWN
import android.view.KeyEvent.KEYCODE_DPAD_UP
import android.view.KeyEvent.KEYCODE_ENTER
import android.view.KeyEvent.KEYCODE_TAB
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableIntState
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.input.key.KeyEvent
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.sp
import com.catpuppyapp.puppygit.dev.bug_Editor_WrongUpdateEditColumnIdx_Fixed
import com.catpuppyapp.puppygit.ui.theme.Theme
import com.catpuppyapp.puppygit.utils.UIHelper
import jp.kaleidot725.texteditor.state.TextFieldState

@Composable
internal fun TextField(
    textFieldState: TextFieldState,
    enabled: Boolean,
    focusRequester: FocusRequester,
    onUpdateText: (TextFieldValue) -> Unit,
    onContainNewLine: (TextFieldValue) -> Unit,
    onAddNewLine: (TextFieldValue) -> Unit,
    onDeleteNewLine: () -> Unit,
    onFocus: () -> Unit,
    onUpFocus: () -> Unit,
    onDownFocus: () -> Unit,
    modifier: Modifier = Modifier,
    needShowCursorHandle:MutableState<Boolean>,
    lastEditedColumnIndexState:MutableIntState,
    searchMode:Boolean,   //因为更新光标有bug，会在搜索时错误更新编辑列导致搜索卡在原地，所以才传这个参数，否则用不到
    mergeMode:Boolean,
    fontSize:MutableIntState,
) {
    val currentTextField by rememberUpdatedState(newValue = textFieldState.value)
    val inDarkTheme = Theme.inDarkTheme

    LaunchedEffect(textFieldState.isSelected) {
        if (textFieldState.isSelected) {
            focusRequester.requestFocus()
        }
    }

    BasicTextField(
        value = textFieldState.value,
        enabled = enabled,
        onValueChange = {
//            println("start:${it.selection.start}, end:${it.selection.end}")  //test2024081116726433
            //更新最后编辑列
            //废弃，因为检查是否更新了文本浪费性能，反正启动也不定位到列，干脆不更新了，不过维护此值对性能影响不大，所以没删这段代码) 注：search且没更新文本，不更新光标，因为更新光标有bug，例如我搜索后定位到2行3列，但这里可能会自动变成3行4列，然后导致搜索不跳转，很傻逼
//            if((it.selection.start == it.selection.end) && (!searchMode || it.text!=textFieldState.value.text)) {  // start == end 说明不是选中状态而是点击某列，这时更新最后编辑列

            //目前仅在非searchMode时更新最后编辑列，日后修复更新列错误的bug后，改下flag变量即可，这的逻辑不用改。
            //ps: mergeMode也要用到搜索，所以也需要判断
            if((it.selection.start == it.selection.end) && ((!searchMode && !mergeMode) || bug_Editor_WrongUpdateEditColumnIdx_Fixed)) {  // start == end 说明不是选中状态而是点击某列，这时更新最后编辑列
                lastEditedColumnIndexState.intValue = it.selection.start
            }

            //存在选中文本时，显示光标拖手和背景颜色（handle
            needShowCursorHandle.value = it.selection.start != it.selection.end

            if (currentTextField == it) return@BasicTextField
            if (it.text.contains('\n')) onContainNewLine(it) else onUpdateText(it)
        },
        //字体样式:字体颜色、字体大小、背景颜色等
        textStyle = TextStyle.Default.copy(fontSize = fontSize.intValue.sp, color = UIHelper.getFontColor(inDarkTheme)),
        //光标颜色
        cursorBrush = SolidColor(if(inDarkTheme) Color.LightGray else Color.Black),

        modifier = modifier
            .fillMaxWidth()
            .wrapContentHeight()
//            .focusTarget()  //如果加这个，按一次返回会先解除focus，然后才会退出，操作有些繁琐，我感觉不加比较好
            .focusRequester(focusRequester)
            .onFocusChanged { if (it.isFocused) onFocus() }
            .onPreviewKeyEvent { event ->
                val value = textFieldState.value
                val selection = currentTextField.selection

                val b1 = onPreviewDelKeyEvent(event, selection) { onDeleteNewLine() }
                if (b1) return@onPreviewKeyEvent true

                val b2 = onPreviewDownKeyEvent(event, value) { onDownFocus() }
                if (b2) return@onPreviewKeyEvent true

                val b3 = onPreviewUpKeyEvent(event, selection) { onUpFocus() }
                if (b3) return@onPreviewKeyEvent true

                val b4 = onPreviewEnterKeyEvent(event) { onAddNewLine(currentTextField) }
                if (b4) return@onPreviewKeyEvent true

                val b5 = onPreviewTabKeyEvent(event) { onDownFocus() }
                if (b5) return@onPreviewKeyEvent true

                false
            }
    )
}

private fun onPreviewDelKeyEvent(
    event: KeyEvent,
    selection: TextRange,
    invoke: () -> Unit
): Boolean {
    val isKeyDown = event.type == KeyEventType.KeyDown
    if (!isKeyDown) return false

    val isDelKey = event.nativeKeyEvent.keyCode == KEYCODE_DEL
    if (!isDelKey) return false

    val isEmpty = selection == TextRange.Zero
    if (!isEmpty) return false

    invoke()
    return true
}

private fun onPreviewUpKeyEvent(
    event: KeyEvent,
    selection: TextRange,
    invoke: () -> Unit
): Boolean {
    val isKeyDown = event.type == KeyEventType.KeyDown
    if (!isKeyDown) return false

    val isUpKey = event.nativeKeyEvent.keyCode == KEYCODE_DPAD_UP
    if (!isUpKey) return false

    val isEmpty = selection == TextRange.Zero
    if (!isEmpty) return false

    invoke()
    return true
}

private fun onPreviewDownKeyEvent(
    event: KeyEvent,
    value: TextFieldValue,
    invoke: () -> Unit
): Boolean {
    val isKeyDown = event.type == KeyEventType.KeyDown
    if (!isKeyDown) return false

    val isDownKey = event.nativeKeyEvent.keyCode == KEYCODE_DPAD_DOWN
    if (!isDownKey) return false

    val isEmpty = value.selection == TextRange(value.text.count())
    if (!isEmpty) return false

    invoke()
    return true
}

private fun onPreviewTabKeyEvent(
    event: KeyEvent,
    invoke: () -> Unit
): Boolean {
    val isKeyDown = event.type == KeyEventType.KeyDown
    if (!isKeyDown) return false

    val isTabKey = event.nativeKeyEvent.keyCode == KEYCODE_TAB
    if (!isTabKey) return false

    invoke()
    return true
}

private fun onPreviewEnterKeyEvent(
    event: KeyEvent,
    invoke: () -> Unit
): Boolean {
    val isKeyDown = event.type == KeyEventType.KeyDown
    if (!isKeyDown) return false

    val isEnterKey = event.nativeKeyEvent.keyCode == KEYCODE_ENTER
    if (!isEnterKey) return false

    invoke()
    return true
}
