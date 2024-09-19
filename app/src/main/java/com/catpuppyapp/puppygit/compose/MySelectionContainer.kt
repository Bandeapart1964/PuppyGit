package com.catpuppyapp.puppygit.compose

import androidx.compose.foundation.text.selection.LocalTextSelectionColors
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import com.catpuppyapp.puppygit.style.MyStyleKt
import com.catpuppyapp.puppygit.ui.theme.Theme


@Composable
fun MySelectionContainer(content:@Composable ()->Unit) {
    val inDarkMode = Theme.inDarkTheme

    CompositionLocalProvider(
        LocalTextSelectionColors provides (if(inDarkMode) MyStyleKt.TextSelectionColor.customTextSelectionColors_darkMode else MyStyleKt.TextSelectionColor.customTextSelectionColors),
    ) {
        //旧版m3，这个东西有bug，如果结束光标超过开始光标，会直接崩溃，但新版1.2.1已解决！目前用的版本是没问题的，不过好像只能复制，没有翻译之类的选项，可能还是有bug，或者我哪里设置的不对？
        SelectionContainer{
            content()
        }

    }
}
