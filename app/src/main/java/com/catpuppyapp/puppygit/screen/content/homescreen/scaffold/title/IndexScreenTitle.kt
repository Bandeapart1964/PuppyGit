package com.catpuppyapp.puppygit.screen.content.homescreen.scaffold.title

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableIntState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.catpuppyapp.puppygit.play.pro.R
import com.catpuppyapp.puppygit.data.entity.RepoEntity
import com.catpuppyapp.puppygit.style.MyStyleKt
import com.catpuppyapp.puppygit.utils.Libgit2Helper
import com.catpuppyapp.puppygit.utils.Msg
import com.catpuppyapp.puppygit.utils.UIHelper
import com.catpuppyapp.puppygit.utils.state.CustomStateSaveable
import com.catpuppyapp.puppygit.utils.state.StateUtil
import com.github.git24j.core.Repository
import kotlinx.coroutines.CoroutineScope

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun IndexScreenTitle(
    curRepo: CustomStateSaveable<RepoEntity>,
    repoState: MutableIntState,
    scope: CoroutineScope,
    changeListPageItemListState: LazyListState
) {
    val haptic = LocalHapticFeedback.current
    val appContext = LocalContext.current

    val needShowRepoState = StateUtil.getRememberSaveableState(initValue = false)
    val repoStateText = StateUtil.getRememberSaveableState(initValue = "")

    //设置仓库状态，主要是为了显示merge
    Libgit2Helper.setRepoStateText(repoState.intValue, needShowRepoState, repoStateText, appContext)

    val getTitleColor = {
        if(repoState.intValue == Repository.StateT.MERGE.bit || repoState.intValue == Repository.StateT.REBASE_MERGE.bit || repoState.intValue==Repository.StateT.CHERRYPICK.bit) Color.Red else Color.Unspecified
    }

    Column(modifier = Modifier
        .widthIn(min = MyStyleKt.Title.clickableTitleMinWidth)
        .combinedClickable(
            onDoubleClick = { UIHelper.scrollToItem(scope, changeListPageItemListState, 0) },
            onLongClick = {  //长按显示仓库名
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                Msg.requireShow("'${curRepo.value.repoName}'")
            }
        ) { // onClick
            //点按暂无操作
        }
        //外面的标题宽180.dp，这里的比外面的宽点，因为这个页面顶栏actions少
        .widthIn(max = 200.dp)
    ) {
        Row(modifier = Modifier.horizontalScroll(StateUtil.getRememberScrollState())) {

            Text(
                text = curRepo.value.repoName,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                fontSize = 18.sp,
                color = getTitleColor()
            )
        }
        Row(modifier = Modifier.horizontalScroll(StateUtil.getRememberScrollState())) {
            //"[Index]|Merging" or "[Index]"
            Text(text = "["+stringResource(id = R.string.index)+"]" + (if(needShowRepoState.value) "|"+repoStateText.value else ""),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                fontSize = MyStyleKt.Title.secondLineFontSize,
                color = getTitleColor()
            )
        }

    }
}
