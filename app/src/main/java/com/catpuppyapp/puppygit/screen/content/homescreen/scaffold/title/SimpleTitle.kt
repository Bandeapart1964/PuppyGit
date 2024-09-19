package com.catpuppyapp.puppygit.screen.content.homescreen.scaffold.title

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import com.catpuppyapp.puppygit.play.pro.R

@Composable
fun SimpleTitle(text:String = stringResource(R.string.app_name)) {
    Text(
        text = text,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis
    )
}
