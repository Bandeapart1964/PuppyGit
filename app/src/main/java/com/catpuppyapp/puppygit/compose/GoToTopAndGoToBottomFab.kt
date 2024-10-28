package com.catpuppyapp.puppygit.compose

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.HideSource
import androidx.compose.material.icons.filled.VerticalAlignBottom
import androidx.compose.material.icons.filled.VerticalAlignTop
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.res.stringResource
import com.catpuppyapp.puppygit.play.pro.R
import com.catpuppyapp.puppygit.style.MyStyleKt
import com.catpuppyapp.puppygit.utils.UIHelper
import kotlinx.coroutines.CoroutineScope


@Composable
fun GoToTopAndGoToBottomFab(
    filterModeOn: MutableState<Boolean>,
    scope: CoroutineScope,
    filterListState: LazyListState,
    listState: LazyListState,
    pageScrolled: MutableState<Boolean>
) {
    Column(modifier = MyStyleKt.Fab.getFabModifier()) {
        //show go to top
        SmallFab(
            icon = Icons.Filled.VerticalAlignTop, iconDesc = stringResource(id = R.string.go_to_top)
        ) {
            if (filterModeOn.value) {
                UIHelper.scrollToItem(scope, filterListState, 0)
            } else {
                UIHelper.scrollToItem(scope, listState, 0)
            }

            // hide fab after scrolled
//            pageScrolled.value = false
        }

        // temporary hide fab
        SmallFab(
            icon = Icons.Filled.HideSource, iconDesc = stringResource(id = R.string.hide)
        ) {
            pageScrolled.value = false
        }

        // go to bottom
        SmallFab(
            icon = Icons.Filled.VerticalAlignBottom, iconDesc = stringResource(id = R.string.go_to_bottom)
        ) {
            if (filterModeOn.value) {
                UIHelper.scrollToItem(scope, filterListState, Int.MAX_VALUE)
            } else {
                UIHelper.scrollToItem(scope, listState, Int.MAX_VALUE)
            }

//            pageScrolled.value = false
        }
    }
}
