package com.catpuppyapp.puppygit.screen.content.homescreen.scaffold.title

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowLeft
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableIntState
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.sp
import com.catpuppyapp.puppygit.data.entity.RepoEntity
import com.catpuppyapp.puppygit.play.pro.R
import com.catpuppyapp.puppygit.style.MyStyleKt
import com.catpuppyapp.puppygit.ui.theme.Theme
import com.catpuppyapp.puppygit.utils.AppModel
import com.catpuppyapp.puppygit.utils.Libgit2Helper
import com.catpuppyapp.puppygit.utils.Msg
import com.catpuppyapp.puppygit.utils.UIHelper
import com.catpuppyapp.puppygit.utils.addPrefix
import com.catpuppyapp.puppygit.utils.changeStateTriggerRefreshPage
import com.catpuppyapp.puppygit.utils.dbIntToBool
import com.catpuppyapp.puppygit.utils.doJobThenOffLoading
import com.catpuppyapp.puppygit.utils.state.CustomStateListSaveable
import com.catpuppyapp.puppygit.utils.state.CustomStateSaveable
import com.catpuppyapp.puppygit.utils.state.StateUtil
import com.github.git24j.core.Repository
import kotlinx.coroutines.CoroutineScope

//private val stateKeyTag = "ChangeListTitle"

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ChangeListTitle(
    changeListCurRepo: CustomStateSaveable<RepoEntity>,
    dropDownMenuItemOnClick: (RepoEntity) -> Unit,
    repoState: MutableIntState,
    isSelectionMode: MutableState<Boolean>,
    listState: LazyListState,
    scope: CoroutineScope,
    enableAction:Boolean,
    repoList:CustomStateListSaveable<RepoEntity>,
    needReQueryRepoList:MutableState<String>
)
{
    val haptic = LocalHapticFeedback.current
    val appContext = LocalContext.current

    val inDarkTheme = Theme.inDarkTheme

    val dropDownMenuExpendState = rememberSaveable { mutableStateOf(false)}


    val needShowRepoState = rememberSaveable { mutableStateOf(false)}
    val repoStateText = rememberSaveable { mutableStateOf("")}

    //设置仓库状态，主要是为了显示merge
    Libgit2Helper.setRepoStateText(repoState.intValue, needShowRepoState, repoStateText, appContext)

    val closeDropDownMenu={
        dropDownMenuExpendState.value=false
    }
    val openDropDownMenu={
        changeStateTriggerRefreshPage(needReQueryRepoList)
        dropDownMenuExpendState.value=true
    }
    val switchDropDownMenu={
        if(dropDownMenuExpendState.value) {
            closeDropDownMenu()
        }else{
            openDropDownMenu()
        }

    }

    val getTitleColor={
            if(enableAction) {
                if(repoState.intValue == Repository.StateT.MERGE.bit || repoState.intValue == Repository.StateT.REBASE_MERGE.bit || repoState.intValue==Repository.StateT.CHERRYPICK.bit) Color.Red else Color.Unspecified
            } else {
                UIHelper.getDisableBtnColor(inDarkTheme)
            }
    }

    if(repoList.value.isEmpty()) {
        Text(
            text = stringResource(id = R.string.changelist),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }else {
        Box(
            modifier = Modifier
                .fillMaxWidth()
//            .widthIn(min = MyStyleKt.Title.clickableTitleMinWidth, max = 180.dp)
                .combinedClickable(
                    enabled = enableAction,
//                onDoubleClick = {UIHelper.scrollToItem(scope, listState, 0)},
                    onLongClick = {  //长按显示仓库名和分支名
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)

                        Msg.requireShowLongDuration("'${changeListCurRepo.value.repoName}' on branch '${changeListCurRepo.value.branch}'")
                    }
                ) { // onClick
                    switchDropDownMenu()  //切换下拉菜单显示隐藏
                },
//        horizontalArrangement = Arrangement.Center,
//        verticalAlignment = Alignment.CenterVertically
        ) {
            Column( modifier = Modifier
                .fillMaxWidth(.8f)
                .align(Alignment.CenterStart)
            ) {
                Row(
                    modifier = Modifier
                        .horizontalScroll(rememberScrollState())

                    ,
//                    .heightIn(max=MyStyleKt.Title.lineHeight)  //弃用，因为会使字体上下部分都显示不全！
                    //限制下宽度，不然仓库名太长就看不到箭头按钮了，用户可能就不知道能点击仓库名切换仓库了
                ) {
                    Text(
                        text = changeListCurRepo.value.repoName,
//                    style=MyStyleKt.clickableText.style,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        fontSize = 18.sp,
                        //如果是在合并（或者有冲突），仓库名变成红色，否则变成默认颜色
                        color = getTitleColor()
                    )

                }
                Row(
                    modifier = Modifier
                        .horizontalScroll(rememberScrollState()),
                ){

                    Text(
                        //  判断仓库是否处于detached，然后显示在这里(例如： "abc1234(detached)" )
                        // "main|StateT" or "main", eg, when merging show: "main|Merging", when 仓库状态正常时 show: "main"；如果是detached HEAD状态，则显示“提交号(Detached)|状态“，例如：abc2344(Detached) 或 abc2344(Detached)|Merging
                        text = (if(dbIntToBool(changeListCurRepo.value.isDetached)) {changeListCurRepo.value.lastCommitHash+"(Detached)"} else {changeListCurRepo.value.branch+":"+changeListCurRepo.value.upstreamBranch}) + (if(needShowRepoState.value) {"|"+repoStateText.value} else {""}),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        fontSize = MyStyleKt.Title.secondLineFontSize,
//                        color = if(enableAction) Color.Unspecified else UIHelper.getDisableBtnColor(inDarkTheme)
                        color = getTitleColor()
                    )
                }
            }

            Column(modifier = Modifier
                .fillMaxWidth(.2f)
                .align(Alignment.CenterEnd)
            ){
                Icon(imageVector = if(dropDownMenuExpendState.value) Icons.Filled.ArrowDropDown else Icons.AutoMirrored.Filled.ArrowLeft,
                    contentDescription = stringResource(R.string.switch_repo),
                    tint = if(enableAction) LocalContentColor.current else UIHelper.getDisableBtnColor(inDarkTheme)
                )
            }
        }
        DropdownMenu(
            expanded = dropDownMenuExpendState.value,
            onDismissRequest = { closeDropDownMenu() }
        ) {
            for (r in repoList.value.toList()) {
                //忽略当前仓库
                //不忽略了，没必要，显示的是选中条目，一点击，展开的菜单里是所有条目，也很合理
//            if(r.repoName == changeListCurRepo.value.repoName) {
//                continue
//            }

                //如果是当前条目，名字前加个星号
                val repoNameMayHavePrefix = if(r.repoName == changeListCurRepo.value.repoName) addPrefix(r.repoName) else r.repoName
                //列出其余仓库
                DropdownMenuItem(
                    text = { Text(repoNameMayHavePrefix) },
                    onClick = {
                        //如果点击其他仓库(切换仓库)，则退出选择模式
                        if(changeListCurRepo.value.repoName != r.repoName) {
                            isSelectionMode.value=false
                        }

                        dropDownMenuItemOnClick(r)
                        closeDropDownMenu()
                    }
                )

            }
        }

    }

    LaunchedEffect(needReQueryRepoList.value) {
        try {
            doJobThenOffLoading {
                val repoDb = AppModel.singleInstanceHolder.dbContainer.repoRepository
                val readyRepoListFromDb = repoDb.getReadyRepoList()
                repoList.value.clear()
                repoList.value.addAll(readyRepoListFromDb)
//                repoList.requireRefreshView()
            }
        } catch (cancel: Exception) {
//            println("LaunchedEffect: job cancelled")
        }
    }
}
