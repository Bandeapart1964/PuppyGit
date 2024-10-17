package com.catpuppyapp.puppygit.compose

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.rememberScrollState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun ScrollableRow(content:@Composable ()->Unit) {
    Row(modifier = Modifier.horizontalScroll(rememberScrollState())) {
        content()
    }
}
