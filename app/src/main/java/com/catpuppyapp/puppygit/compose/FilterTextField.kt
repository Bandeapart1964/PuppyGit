package com.catpuppyapp.puppygit.compose

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.TextFieldValue
import com.catpuppyapp.puppygit.play.pro.R
import com.catpuppyapp.puppygit.utils.state.CustomStateSaveable

@Composable
fun FilterTextField(
    filterKeyWord: CustomStateSaveable<TextFieldValue>,
    placeholderText:String = stringResource(R.string.input_keyword),
    singleLine:Boolean = true,
    modifier: Modifier? = null,
    onValueChange:(newValue:TextFieldValue)->Unit = { filterKeyWord.value = it },
) {
    OutlinedTextField(
        modifier = modifier ?: Modifier.fillMaxWidth(),
        value = filterKeyWord.value,
        onValueChange = { onValueChange(it) },
        placeholder = { Text(placeholderText) },
        singleLine = singleLine,
        // label = {Text(title)}

        //软键盘换行按钮替换成搜索图标且按搜索图标后执行搜索
//        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
//        keyboardActions = KeyboardActions(onSearch = {
//            doFilter(filterKeyWord.value.text)
//        })
    )
}
