package com.catpuppyapp.puppygit.screen.content.homescreen.scaffold.title

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.combinedClickable
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import com.catpuppyapp.puppygit.play.pro.R
import com.catpuppyapp.puppygit.utils.UIHelper

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun SettingsTitle(
    listState: ScrollState
) {
    val scope = rememberCoroutineScope()

    Text(
        text = stringResource(id = R.string.settings),
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
        modifier = Modifier.combinedClickable(onDoubleClick = {
            // double click go to top
            UIHelper.scrollTo(scope,listState, 0 )
        }) {  }
    )
}