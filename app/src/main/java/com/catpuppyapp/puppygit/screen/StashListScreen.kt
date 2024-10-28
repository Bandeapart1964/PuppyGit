package com.catpuppyapp.puppygit.screen

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FilterAlt
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.catpuppyapp.puppygit.compose.BottomSheet
import com.catpuppyapp.puppygit.compose.BottomSheetItem
import com.catpuppyapp.puppygit.compose.ConfirmDialog
import com.catpuppyapp.puppygit.compose.CopyableDialog
import com.catpuppyapp.puppygit.compose.FilterTextField
import com.catpuppyapp.puppygit.compose.GoToTopAndGoToBottomFab
import com.catpuppyapp.puppygit.compose.LoadingDialog
import com.catpuppyapp.puppygit.compose.LongPressAbleIconBtn
import com.catpuppyapp.puppygit.compose.MyLazyColumn
import com.catpuppyapp.puppygit.compose.StashItem
import com.catpuppyapp.puppygit.data.entity.RepoEntity
import com.catpuppyapp.puppygit.git.StashDto
import com.catpuppyapp.puppygit.play.pro.R
import com.catpuppyapp.puppygit.settings.SettingsUtil
import com.catpuppyapp.puppygit.style.MyStyleKt
import com.catpuppyapp.puppygit.ui.theme.Theme
import com.catpuppyapp.puppygit.utils.AppModel
import com.catpuppyapp.puppygit.utils.Libgit2Helper
import com.catpuppyapp.puppygit.utils.Msg
import com.catpuppyapp.puppygit.utils.MyLog
import com.catpuppyapp.puppygit.utils.UIHelper
import com.catpuppyapp.puppygit.utils.changeStateTriggerRefreshPage
import com.catpuppyapp.puppygit.utils.createAndInsertError
import com.catpuppyapp.puppygit.utils.doJobThenOffLoading
import com.catpuppyapp.puppygit.utils.state.mutableCustomStateListOf
import com.catpuppyapp.puppygit.utils.state.mutableCustomStateOf
import com.github.git24j.core.Repository
import com.github.git24j.core.Signature

private val TAG = "StashListScreen"
private val stateKeyTag = "StashListScreen"

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun StashListScreen(
    repoId:String,
    naviUp: () -> Boolean,
) {
    val homeTopBarScrollBehavior = AppModel.singleInstanceHolder.homeTopBarScrollBehavior
    val navController = AppModel.singleInstanceHolder.navController
    val appContext = AppModel.singleInstanceHolder.appContext
    val haptic = LocalHapticFeedback.current
    val scope = rememberCoroutineScope()
    val settings = remember { SettingsUtil.getSettingsSnapshot() }

    val inDarkTheme = Theme.inDarkTheme

    //获取假数据
    val list = mutableCustomStateListOf(keyTag = stateKeyTag, keyName = "list", initValue = listOf<StashDto>())


    //这个页面的滚动状态不用记住，每次点开重置也无所谓
    val listState = rememberLazyListState()
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = MyStyleKt.BottomSheet.skipPartiallyExpanded)
    val showBottomSheet = rememberSaveable { mutableStateOf(false)}

    val needRefresh = rememberSaveable { mutableStateOf("")}

    val curObjInPage = mutableCustomStateOf(keyTag = stateKeyTag, keyName = "curObjInPage", initValue =StashDto())
    val curRepo = mutableCustomStateOf(keyTag = stateKeyTag, keyName = "curRepo", initValue = RepoEntity(id=""))


    val defaultLoadingText = stringResource(R.string.loading)
    val loading = rememberSaveable { mutableStateOf(false)}
    val loadingText = rememberSaveable { mutableStateOf(defaultLoadingText)}
    val loadingOn = { text:String ->
        loadingText.value=text
        loading.value=true
    }
    val loadingOff = {
        loadingText.value = appContext.getString(R.string.loading)
        loading.value=false
    }

    //filter相关，开始
    val filterKeyword = mutableCustomStateOf(
        keyTag = stateKeyTag,
        keyName = "filterKeyword",
        initValue = TextFieldValue("")
    )
    val filterModeOn = rememberSaveable { mutableStateOf(false)}
    //filter相关，结束

    // 向下滚动监听，开始
    val pageScrolled = remember { mutableStateOf(settings.showNaviButtons) }

    val filterListState = rememberLazyListState()
//    val filterListState = mutableCustomStateOf(
//        keyTag = stateKeyTag,
//        keyName = "filterListState",
//        LazyListState(0,0)
//    )
    val enableFilterState = rememberSaveable { mutableStateOf(false)}
//    val firstVisible = remember { derivedStateOf { if(enableFilterState.value) filterListState.value.firstVisibleItemIndex else listState.firstVisibleItemIndex } }
//    ScrollListener(
//        nowAt = firstVisible.value,
//        onScrollUp = {scrollingDown.value = false}
//    ) { // onScrollDown
//        scrollingDown.value = true
//    }
//
//    val lastAt = remember { mutableIntStateOf(0) }
//    val lastIsScrollDown = remember { mutableStateOf(false) }
//    val forUpdateScrollState = remember {
//        derivedStateOf {
//            val nowAt = if(enableFilterState.value) {
//                filterListState.firstVisibleItemIndex
//            } else {
//                listState.firstVisibleItemIndex
//            }
//            val scrolledDown = nowAt > lastAt.intValue  // scroll down
////            val scrolledUp = nowAt < lastAt.intValue
//
//            val scrolled = nowAt != lastAt.intValue  // scrolled
//            lastAt.intValue = nowAt
//
//            // only update state when this scroll down and last is not scroll down, or this is scroll up and last is not scroll up
//            if(scrolled && ((lastIsScrollDown.value && !scrolledDown) || (!lastIsScrollDown.value && scrolledDown))) {
//                pageScrolled.value = true
//            }
//
//            lastIsScrollDown.value = scrolledDown
//        }
//    }.value
    // 向下滚动监听，结束

    //Details弹窗，开始
    val clipboardManager = LocalClipboardManager.current
    val showDetailsDialog = rememberSaveable { mutableStateOf(false)}
    val detailsString = rememberSaveable { mutableStateOf("")}
    if(showDetailsDialog.value) {
        CopyableDialog(
            title = stringResource(id = R.string.details),
            text = detailsString.value,
            onCancel = { showDetailsDialog.value = false }
        ) {
            showDetailsDialog.value = false
            clipboardManager.setText(AnnotatedString(detailsString.value))
            Msg.requireShow(appContext.getString(R.string.copied))
        }
    }
    //Details弹窗，结束

    val showPopDialog = rememberSaveable { mutableStateOf(false)}
    val showApplyDialog = rememberSaveable { mutableStateOf( false)}
    val showDelDialog = rememberSaveable { mutableStateOf( false)}
    val showCreateDialog = rememberSaveable { mutableStateOf( false)}

    val stashMsgForCreateDialog = rememberSaveable { mutableStateOf( "")}

    val gitUsername = rememberSaveable { mutableStateOf("")}
    val gitEmail = rememberSaveable { mutableStateOf( "")}

    if(showPopDialog.value) {
        ConfirmDialog(
            title = stringResource(R.string.pop),
            text = stringResource(R.string.will_apply_then_delete_item_are_you_sure),
            okTextColor = MyStyleKt.TextColor.danger,
            onCancel = { showPopDialog.value=false}
        ) {
            showPopDialog.value=false

            doJobThenOffLoading(loadingOn, loadingOff, appContext.getString(R.string.loading)) {
                try {
                    Repository.open(curRepo.value.fullSavePath).use { repo->
                        Libgit2Helper.stashPop(repo, curObjInPage.value.index)
                    }
                    Msg.requireShow(appContext.getString(R.string.success))
                }catch (e:Exception) {
                    val errPrefix = "pop stash err: index=${curObjInPage.value.index}, stashId=${curObjInPage.value.stashId}, err="
                    Msg.requireShowLongDuration(e.localizedMessage?:"err")
                    createAndInsertError(curRepo.value.id, errPrefix+e.localizedMessage)
                    MyLog.e(TAG, errPrefix+e.stackTraceToString())
                }finally {
                    changeStateTriggerRefreshPage(needRefresh)
                }
            }

        }
    }

    if(showApplyDialog.value) {
        ConfirmDialog(
            title = stringResource(R.string.apply),
            text = stringResource(R.string.will_apply_item_are_you_sure),
            okTextColor = MyStyleKt.TextColor.danger,
            onCancel = { showApplyDialog.value=false}
        ) {
            showApplyDialog.value=false

            doJobThenOffLoading(loadingOn, loadingOff, appContext.getString(R.string.loading)) {
                try {
                    Repository.open(curRepo.value.fullSavePath).use { repo->
                        Libgit2Helper.stashApply(repo, curObjInPage.value.index)
                    }
                    Msg.requireShow(appContext.getString(R.string.success))
                }catch (e:Exception) {
                    val errPrefix = "apply stash err: index=${curObjInPage.value.index}, stashId=${curObjInPage.value.stashId}, err="
                    Msg.requireShowLongDuration(e.localizedMessage?:"err")
                    createAndInsertError(curRepo.value.id, errPrefix+e.localizedMessage)
                    MyLog.e(TAG, errPrefix+e.stackTraceToString())
                }finally {
                    changeStateTriggerRefreshPage(needRefresh)
                }
            }

        }
    }

    if(showDelDialog.value) {
        ConfirmDialog(
            title = stringResource(R.string.apply),
            text = stringResource(R.string.will_delete_item_are_you_sure),
            okTextColor = MyStyleKt.TextColor.danger,
            onCancel = { showDelDialog.value=false}
        ) {
            showDelDialog.value=false

            doJobThenOffLoading(loadingOn, loadingOff, appContext.getString(R.string.loading)) {
                try {
                    Repository.open(curRepo.value.fullSavePath).use { repo->
                        Libgit2Helper.stashDrop(repo, curObjInPage.value.index)
                    }
                    Msg.requireShow(appContext.getString(R.string.success))
                }catch (e:Exception) {
                    val errPrefix = "delete stash err: index=${curObjInPage.value.index}, stashId=${curObjInPage.value.stashId}, err="
                    Msg.requireShowLongDuration(e.localizedMessage?:"err")
                    createAndInsertError(curRepo.value.id, errPrefix+e.localizedMessage)
                    MyLog.e(TAG, errPrefix+e.stackTraceToString())
                }finally {
                    changeStateTriggerRefreshPage(needRefresh)
                }
            }

        }
    }

    if(showCreateDialog.value) {
        ConfirmDialog(
            title = stringResource(R.string.create),
            okBtnText = stringResource(R.string.ok),
            requireShowTextCompose = true,
            textCompose = {
                Column {
                    TextField(
                        modifier = Modifier.fillMaxWidth(),

                        value = stashMsgForCreateDialog.value,
                        onValueChange = {
                            stashMsgForCreateDialog.value = it
                        },
                        label = {
                            Text(stringResource(R.string.msg))
                        },
                        placeholder = {
                        }
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Row {
                        Text(text = "(" + appContext.getString(R.string.you_can_leave_msg_empty_will_auto_gen_one) + ")",
                            color = MyStyleKt.TextColor.highlighting_green
                            )
                    }
                }
            },
            onCancel = { showCreateDialog.value=false}
        ) onOk@{
            showCreateDialog.value=false

            val username = gitUsername.value
            val email = gitEmail.value
            if(username.isBlank() || email.isBlank()) {
                Msg.requireShowLongDuration(appContext.getString(R.string.plz_set_git_username_and_email_first))
                return@onOk
            }

            doJobThenOffLoading(loadingOn, loadingOff, appContext.getString(R.string.loading)) {
                try {
                    val msg = stashMsgForCreateDialog.value.ifEmpty { Libgit2Helper.stashGenMsg() }
                    Repository.open(curRepo.value.fullSavePath).use { repo->
                        Libgit2Helper.stashSave(repo, stasher = Signature.create(username, email), msg=msg)
                    }
                    Msg.requireShow(appContext.getString(R.string.success))
                }catch (e:Exception) {
                    val errPrefix = "create stash err: "
                    Msg.requireShowLongDuration(e.localizedMessage?:"err")
                    createAndInsertError(curRepo.value.id, errPrefix+e.localizedMessage)
                    MyLog.e(TAG, errPrefix+e.stackTraceToString())
                }finally {
                    changeStateTriggerRefreshPage(needRefresh)
                }
            }

        }
    }

    Scaffold(
        modifier = Modifier.nestedScroll(homeTopBarScrollBehavior.nestedScrollConnection),
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.primary,
                ),
                title = {
                    if(filterModeOn.value) {
                        FilterTextField(
                            filterKeyword,
                        )
                    }else {
                        val repoAndBranch = Libgit2Helper.getRepoOnBranchOrOnDetachedHash(curRepo.value)
                        Column (modifier = Modifier.combinedClickable (
                            onDoubleClick = {UIHelper.scrollToItem(scope, listState,0)},  // go to top
                            onLongClick = {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                Msg.requireShow(repoAndBranch)
                            }
                        ){  //onClick
    //                        Msg.requireShow(repoAndBranch)
                        }){
                            Row(modifier = Modifier.horizontalScroll(rememberScrollState())) {
                                Text(
                                    text= stringResource(R.string.stash),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                            Row(modifier = Modifier.horizontalScroll(rememberScrollState())) {
                                Text(
                                    text= repoAndBranch,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    fontSize = MyStyleKt.Title.secondLineFontSize
                                )
                            }
                        }

                    }
                },
                navigationIcon = {
                    if(filterModeOn.value) {
                        LongPressAbleIconBtn(
                            tooltipText = stringResource(R.string.close),
                            icon = Icons.Filled.Close,
                            iconContentDesc = stringResource(R.string.close),

                        ) {
                            filterModeOn.value = false
                        }
                    }else {
                        LongPressAbleIconBtn(
                            tooltipText = stringResource(R.string.back),
                            icon = Icons.AutoMirrored.Filled.ArrowBack,
                            iconContentDesc = stringResource(R.string.back),

                        ) {
                            naviUp()
                        }

                    }
                },
                actions = {
                    if(!filterModeOn.value) {
                        LongPressAbleIconBtn(
                            tooltipText = stringResource(R.string.filter),
                            icon =  Icons.Filled.FilterAlt,
                            iconContentDesc = stringResource(R.string.filter),
                        ) {
                            // filter item
                            filterKeyword.value = TextFieldValue("")
                            filterModeOn.value = true
                        }

                        LongPressAbleIconBtn(
                            tooltipText = stringResource(R.string.refresh),
                            icon =  Icons.Filled.Refresh,
                            iconContentDesc = stringResource(R.string.refresh),
                        ) {
                            changeStateTriggerRefreshPage(needRefresh)
                        }

                        LongPressAbleIconBtn(
                            tooltipText = stringResource(R.string.create),
                            icon =  Icons.Filled.Add,
                            iconContentDesc = stringResource(R.string.create),
                        ) {
                            if(gitEmail.value.isBlank() || gitUsername.value.isBlank()) {
                                Msg.requireShowLongDuration(appContext.getString(R.string.plz_set_git_username_and_email_first))
                            }else{
                                showCreateDialog.value=true
                            }
                        }
                    }
                },
                scrollBehavior = homeTopBarScrollBehavior,
            )
        },
        floatingActionButton = {
            if(pageScrolled.value) {

                GoToTopAndGoToBottomFab(
                    filterModeOn = enableFilterState.value,
                    scope = scope,
                    filterListState = filterListState,
                    listState = listState,
                    showFab = pageScrolled
                )

            }
        }
    ) { contentPadding ->
        if (loading.value) {
//            LoadingText(text = loadingText.value, contentPadding = contentPadding)
            LoadingDialog(text = loadingText.value)
        }

        if(showBottomSheet.value) {
            // index@shortOid, e.g. 0@abc1234
            val sheetTitle = ""+curObjInPage.value.index+"@"+Libgit2Helper.getShortOidStrByFull(curObjInPage.value.stashId.toString())
            BottomSheet(showBottomSheet, sheetState, sheetTitle) {
                //merge into current 实际上是和HEAD进行合并，产生一个新的提交
                //x 对当前分支禁用这个选项，只有其他分支才能用
                BottomSheetItem(sheetState, showBottomSheet, stringResource(R.string.pop),
                ){
                    //弹出确认框，如果确定，执行merge，否则不执行
                    showPopDialog.value = true
                }
                BottomSheetItem(sheetState, showBottomSheet, stringResource(R.string.apply),
                ){
                    showApplyDialog.value = true
                }
                BottomSheetItem(sheetState, showBottomSheet, stringResource(R.string.delete), textColor = MyStyleKt.TextColor.danger,
                ){
                    showDelDialog.value = true
                }

            }
        }


        //根据关键字过滤条目
        val k = filterKeyword.value.text.lowercase()  //关键字
        val enableFilter = filterModeOn.value && k.isNotEmpty()
        val list = if(enableFilter){
            list.value.filter {
                it.index.toString().lowercase().contains(k)
                    || it.stashId.toString().lowercase().contains(k)
                    || it.msg.lowercase().contains(k)
            }
        }else {
            list.value
        }


        val listState = if(enableFilter) filterListState else listState
//        if(enableFilter) {  //更新filter列表state
//            filterListState.value = listState
//        }
        //更新是否启用filter
        enableFilterState.value = enableFilter


        MyLazyColumn(
            contentPadding = contentPadding,
            list = list,
            listState = listState,
            requireForEachWithIndex = true,
            requirePaddingAtBottom = true,
            forEachCb = {},
        ){idx, it->
            //长按会更新curObjInPage为被长按的条目
            StashItem(showBottomSheet, curObjInPage, idx, it) {  //onClick
                val sb = StringBuilder()
                sb.append(appContext.getString(R.string.index)).append(": ").append(it.index).appendLine().appendLine()
                sb.append(appContext.getString(R.string.stash_id)).append(": ").append(it.stashId).appendLine().appendLine()
                sb.append(appContext.getString(R.string.msg)).append(": ").append(it.msg).appendLine().appendLine()


                detailsString.value = sb.toString()
                showDetailsDialog.value = true
            }

            HorizontalDivider()
        }

    }

    BackHandler {
        if(filterModeOn.value) {
            filterModeOn.value = false
        } else {
            naviUp()
        }
    }

    //compose创建时的副作用
    LaunchedEffect(needRefresh.value) {
        try {
            doJobThenOffLoading(
                loadingOn = loadingOn,
                loadingOff = loadingOff,
                loadingText = appContext.getString(R.string.loading),
            ) {
                list.value.clear()  //先清一下list，然后可能添加也可能不添加

                if(!repoId.isNullOrBlank()) {
                    val repoDb = AppModel.singleInstanceHolder.dbContainer.repoRepository
                    val repoFromDb = repoDb.getById(repoId)
                    if(repoFromDb!=null) {
                        curRepo.value = repoFromDb
                        Repository.open(repoFromDb.fullSavePath).use {repo ->
                            Libgit2Helper.stashList(repo, list.value)
                            val (username, email) = Libgit2Helper.getGitUsernameAndEmail(repo)
                            gitUsername.value = username
                            gitEmail.value = email
                        }
                    }
                }
            }
        } catch (e: Exception) {
            MyLog.e(TAG, "BranchListScreen#LaunchedEffect() err:"+e.stackTraceToString())
//            ("LaunchedEffect: job cancelled")
        }
    }

}
