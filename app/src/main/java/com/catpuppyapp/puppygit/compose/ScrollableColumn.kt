package com.catpuppyapp.puppygit.compose

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.catpuppyapp.puppygit.utils.state.StateUtil

@Composable
fun ScrollableColumn(content:@Composable ()->Unit) {
    Column(modifier = Modifier.verticalScroll(StateUtil.getRememberScrollState())) {
        content()
    }
}
