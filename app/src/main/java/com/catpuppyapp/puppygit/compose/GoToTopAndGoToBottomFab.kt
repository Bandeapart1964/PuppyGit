package com.catpuppyapp.puppygit.compose

import androidx.compose.foundation.ScrollState
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
    filterModeOn: Boolean,
    scope: CoroutineScope,
    filterListState: LazyListState,
    listState: LazyListState,
    showFab: MutableState<Boolean>
) {
    val goToTop = {UIHelper.scrollToItem(scope, listState, 0)}
    val goToTopFiltered = {UIHelper.scrollToItem(scope, filterListState, 0)}
    val goToBottom = {UIHelper.scrollToItem(scope, listState, Int.MAX_VALUE)}
    val goToBottomFiltered = {UIHelper.scrollToItem(scope, filterListState, Int.MAX_VALUE)}
    val hideButton = {showFab.value = false}

    GoToTopAndGoToBottomFab_Internal(
        filterModeOn = filterModeOn,
        scrollToTop = goToTop,
        scrollToTopForFilterState = goToTopFiltered,
        scrollToBottom = goToBottom,
        scrollToBottomForFilterState = goToBottomFiltered,
        hideButton = hideButton
    )
}

@Composable
fun GoToTopAndGoToBottomFab(
    scope: CoroutineScope,
    listState: ScrollState,
    showFab: MutableState<Boolean>
) {
    val goToTop = {UIHelper.scrollTo(scope, listState, 0)}
    val goToBottom = {UIHelper.scrollTo(scope, listState, Int.MAX_VALUE)}
    val hideButton = {showFab.value = false}

    GoToTopAndGoToBottomFab_Internal(
        filterModeOn = false,
        scrollToTop = goToTop,
        scrollToTopForFilterState = {},
        scrollToBottom = goToBottom,
        scrollToBottomForFilterState = {},
        hideButton = hideButton
    )
}



@Composable
private fun GoToTopAndGoToBottomFab_Internal(
    filterModeOn: Boolean,
    scrollToTop:()->Unit,
    scrollToTopForFilterState:()->Unit,
    scrollToBottom:()->Unit,
    scrollToBottomForFilterState:()->Unit,
    hideButton:()->Unit,
) {
    Column(modifier = MyStyleKt.Fab.getFabModifier()) {
        //show go to top
        SmallFab(
            icon = Icons.Filled.VerticalAlignTop, iconDesc = stringResource(id = R.string.go_to_top)
        ) {
            if (filterModeOn) {
                scrollToTopForFilterState()
            } else {
                scrollToTop()
            }

            // hide fab after scrolled
//            pageScrolled.value = false
        }

        // temporary hide fab
        SmallFab(
            icon = Icons.Filled.HideSource, iconDesc = stringResource(id = R.string.hide)
        ) {
            hideButton()
        }

        // go to bottom
        SmallFab(
            icon = Icons.Filled.VerticalAlignBottom, iconDesc = stringResource(id = R.string.go_to_bottom)
        ) {
            if (filterModeOn) {
                scrollToBottomForFilterState()
            } else {
                scrollToBottom()
            }

//            pageScrolled.value = false
        }
    }
}
