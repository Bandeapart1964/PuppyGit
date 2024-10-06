package com.catpuppyapp.puppygit.screen

import android.annotation.SuppressLint
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FilterAlt
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.VerticalAlignTop
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.mutableIntStateOf
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
import com.catpuppyapp.puppygit.compose.BottomSheet
import com.catpuppyapp.puppygit.compose.BottomSheetItem
import com.catpuppyapp.puppygit.compose.CheckoutDialog
import com.catpuppyapp.puppygit.compose.CheckoutDialogFrom
import com.catpuppyapp.puppygit.compose.CopyableDialog
import com.catpuppyapp.puppygit.compose.FilterTextField
import com.catpuppyapp.puppygit.compose.LoadingDialog
import com.catpuppyapp.puppygit.compose.LongPressAbleIconBtn
import com.catpuppyapp.puppygit.compose.MyLazyColumn
import com.catpuppyapp.puppygit.compose.ReflogItem
import com.catpuppyapp.puppygit.compose.ResetDialog
import com.catpuppyapp.puppygit.compose.SmallFab
import com.catpuppyapp.puppygit.constants.Cons
import com.catpuppyapp.puppygit.data.entity.RepoEntity
import com.catpuppyapp.puppygit.dev.proFeatureEnabled
import com.catpuppyapp.puppygit.dev.resetByHashTestPassed
import com.catpuppyapp.puppygit.git.ReflogEntryDto
import com.catpuppyapp.puppygit.play.pro.R
import com.catpuppyapp.puppygit.style.MyStyleKt
import com.catpuppyapp.puppygit.ui.theme.Theme
import com.catpuppyapp.puppygit.utils.AppModel
import com.catpuppyapp.puppygit.utils.Libgit2Helper
import com.catpuppyapp.puppygit.utils.Msg
import com.catpuppyapp.puppygit.utils.MyLog
import com.catpuppyapp.puppygit.utils.UIHelper
import com.catpuppyapp.puppygit.utils.changeStateTriggerRefreshPage
import com.catpuppyapp.puppygit.utils.doJobThenOffLoading
import com.catpuppyapp.puppygit.utils.state.StateUtil
import com.catpuppyapp.puppygit.utils.state.mutableCustomStateListOf
import com.catpuppyapp.puppygit.utils.state.mutableCustomStateOf
import com.github.git24j.core.Repository

private val TAG = "ReflogListScreen"
private val stateKeyTag = "ReflogListScreen"

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun ReflogListScreen(
//    context: Context,
//    navController: NavHostController,
//    scope: CoroutineScope,
//    haptic:HapticFeedback,
//    homeTopBarScrollBehavior: TopAppBarScrollBehavior,
    repoId:String,
//    branch:String?,
    naviUp: () -> Boolean,
) {
    val homeTopBarScrollBehavior = AppModel.singleInstanceHolder.homeTopBarScrollBehavior
    val navController = AppModel.singleInstanceHolder.navController
    val appContext = AppModel.singleInstanceHolder.appContext
    val haptic = LocalHapticFeedback.current
    val scope = rememberCoroutineScope()

    val inDarkTheme = Theme.inDarkTheme

    //获取假数据
    val curClickItem = mutableCustomStateOf(keyTag = stateKeyTag, keyName = "curClickItem", initValue = ReflogEntryDto())
    val curLongClickItem = mutableCustomStateOf(keyTag = stateKeyTag, keyName = "curLongClickItem", initValue = ReflogEntryDto())

    val list = mutableCustomStateListOf(keyTag = stateKeyTag, keyName = "list", initValue = listOf<ReflogEntryDto>())

    val filterList = mutableCustomStateListOf(keyTag = stateKeyTag, keyName = "filterList", initValue = listOf<ReflogEntryDto>())

    //这个页面的滚动状态不用记住，每次点开重置也无所谓
    val listState = rememberLazyListState()
    val needRefresh = rememberSaveable { mutableStateOf("")}
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
    val filterKeyword = mutableCustomStateOf(
        keyTag = stateKeyTag,
        keyName = "filterKeyword",
        initValue = TextFieldValue("")
    )
    val filterModeOn = rememberSaveable { mutableStateOf(false)}


    // 向下滚动监听，开始
    val scrollingDown = remember { mutableStateOf(false) }

    val filterListState = mutableCustomStateOf(
        keyTag = stateKeyTag,
        keyName = "filterListState",
        LazyListState(0,0)
    )
    val enableFilterState = rememberSaveable { mutableStateOf(false)}
//    val firstVisible = remember { derivedStateOf { if(enableFilterState.value) filterListState.value.firstVisibleItemIndex else listState.firstVisibleItemIndex } }
//    ScrollListener(
//        nowAt = firstVisible.value,
//        onScrollUp = {scrollingDown.value = false}
//    ) { // onScrollDown
//        scrollingDown.value = true
//    }
    @SuppressLint("UnrememberedMutableState")
    val lastAt = mutableIntStateOf(0)
    scrollingDown.value = remember {
        derivedStateOf {
            val nowAt = if(enableFilterState.value) {
                filterListState.value.firstVisibleItemIndex
            } else {
                listState.firstVisibleItemIndex
            }
            val scrolldown = nowAt > lastAt.intValue
            lastAt.intValue = nowAt
            scrolldown
        }
    }.value
    // 向下滚动监听，结束


    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = MyStyleKt.BottomSheet.skipPartiallyExpanded)
    val showBottomSheet = rememberSaveable { mutableStateOf(false)}

    val checkoutNew = rememberSaveable { mutableStateOf( false)}
    val requireUserInputCommitHash = rememberSaveable { mutableStateOf(false)}
    val showCheckoutDialog = rememberSaveable { mutableStateOf(false)}
    //初始化组件版本的checkout对话框
    val initCheckoutDialog = { requireUserInputHash:Boolean ->
        requireUserInputCommitHash.value = requireUserInputHash
        showCheckoutDialog.value = true
    }

    if(showCheckoutDialog.value) {
        val id = (if(checkoutNew.value) curLongClickItem.value.idNew else curLongClickItem.value.idOld) ?: Cons.allZeroOid

        if(id.isNullOrEmptyOrZero) {  //id无效，关弹窗，显提示
            showCheckoutDialog.value = false
            Msg.requireShow(stringResource(R.string.invalid_oid))
        }else {
            val fullOidStr = id.toString()
            val shortOidStr = Libgit2Helper.getShortOidStrByFull(fullOidStr)

            CheckoutDialog(
                showCheckoutDialog=showCheckoutDialog,
                from = CheckoutDialogFrom.OTHER,
                expectCheckoutType = Cons.checkoutType_checkoutCommitThenDetachHead,
                curRepo = curRepo.value,
                shortName = shortOidStr,
                fullName = fullOidStr,
                curCommitOid = fullOidStr,
                curCommitShortOid = shortOidStr,
                requireUserInputCommitHash = requireUserInputCommitHash.value,
                loadingOn = loadingOn,
                loadingOff = loadingOff,
                onlyUpdateCurItem = false,  //设为false，将会执行refreshPage刷新页面
                updateCurItem = {curItemIdx, fullOid-> },
                refreshPage = { changeStateTriggerRefreshPage(needRefresh) },
                curCommitIndex = -1,
                findCurItemIdxInList = { fullOid->  //无需更新当前条目
                    -1
                }
            )
        }

    }


    val resetOid = rememberSaveable { mutableStateOf("")}
    val resetNew = rememberSaveable { mutableStateOf( false)}
    val showResetDialog = rememberSaveable { mutableStateOf(false)}
    val closeResetDialog = {
        showResetDialog.value = false
    }

    if (showResetDialog.value) {
        val id = (if(resetNew.value) curLongClickItem.value.idNew else curLongClickItem.value.idOld) ?: Cons.allZeroOid

        if(id.isNullOrEmptyOrZero) {  //id无效，关弹窗，显提示
            showResetDialog.value = false
            Msg.requireShow(stringResource(R.string.invalid_oid))
        }else{
            resetOid.value = id.toString()

            ResetDialog(
                fullOidOrBranchOrTag = resetOid,
                closeDialog=closeResetDialog,
                repoFullPath = curRepo.value.fullSavePath,
                repoId=curRepo.value.id,
                refreshPage = { oldHeadCommitOid, isDetached ->
                    changeStateTriggerRefreshPage(needRefresh)
                }
            )
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
                                    text= stringResource(R.string.reflog),
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
                    }
                },
                scrollBehavior = homeTopBarScrollBehavior,
            )
        },
        floatingActionButton = {
            if(scrollingDown.value) {
                //向下滑动时显示go to top按钮
                SmallFab(
                    modifier = MyStyleKt.Fab.getFabModifier(),
                    icon = Icons.Filled.VerticalAlignTop, iconDesc = stringResource(id = R.string.go_to_top)
                ) {
                    if(enableFilterState.value) {
                        UIHelper.scrollToItem(scope, filterListState.value, 0)
                    }else {
                        UIHelper.scrollToItem(scope, listState, 0)
                    }
                }
            }
        }
    ) { contentPadding ->
        if (loading.value) {
//            LoadingText(text = loadingText.value, contentPadding = contentPadding)
            LoadingDialog(text = loadingText.value)
        }

        if (showBottomSheet.value) {
            // title form: oldOid..newOid, means oldOid to newOid, eg abc1234..def1234
            val title = Libgit2Helper.getShortOidStrByFull((curLongClickItem.value.idOld ?: Cons.allZeroOid).toString())+".."+Libgit2Helper.getShortOidStrByFull((curLongClickItem.value.idNew ?: Cons.allZeroOid).toString())
            BottomSheet(showBottomSheet, sheetState, title) {
                BottomSheetItem(sheetState, showBottomSheet, stringResource(R.string.checkout_new)) {
                    // onClick()
                    // 弹出确认框，询问是否确定执行checkout，可detach head，可创建分支，类似checkout remote branch
                    //初始化弹窗默认选项
                    checkoutNew.value = true

                    val requireUserInputHash = false
                    initCheckoutDialog(requireUserInputHash)
                }
                BottomSheetItem(sheetState, showBottomSheet, stringResource(R.string.checkout_old)) {
                    // onClick()
                    // 弹出确认框，询问是否确定执行checkout，可detach head，可创建分支，类似checkout remote branch
                    //初始化弹窗默认选项
                    checkoutNew.value = false

                    val requireUserInputHash = false
                    initCheckoutDialog(requireUserInputHash)
                }

                if(proFeatureEnabled(resetByHashTestPassed)) {
                    BottomSheetItem(sheetState, showBottomSheet, stringResource(R.string.reset_new)) {
                        resetNew.value=true
                        showResetDialog.value = true
                    }
                    BottomSheetItem(sheetState, showBottomSheet, stringResource(R.string.reset_old)) {
                        resetNew.value=false
                        showResetDialog.value = true
                    }
                }

            }
        }



        //有条目
        //根据关键字过滤条目
        val k = filterKeyword.value.text.lowercase()  //关键字
        val enableFilter = filterModeOn.value && k.isNotEmpty()
        val list = if(enableFilter){
            val fl = list.value.filter {
                it.username.lowercase().contains(k)
                        || it.email.lowercase().contains(k)
                        || it.date.lowercase().contains(k)
                        || it.msg.lowercase().contains(k)
                        || it.idNew.toString().lowercase().contains(k)
                        || it.idOld.toString().lowercase().contains(k)
            }
            filterList.value.clear()
            filterList.value.addAll(fl)
            fl
        }else {
            list.value
        }

        val listState = if(enableFilter) rememberLazyListState() else listState
        if(enableFilter) {  //更新filter列表state
            filterListState.value = listState
        }
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
            ReflogItem(showBottomSheet, curLongClickItem,it,
            ) {  //onClick
                val sb = StringBuilder()
                sb.append(appContext.getString(R.string.new_oid)).append(": ").append(it.idNew).appendLine().appendLine()
                sb.append(appContext.getString(R.string.old_oid)).append(": ").append(it.idOld).appendLine().appendLine()
                sb.append(appContext.getString(R.string.date)).append(": ").append(it.date).appendLine().appendLine()
                sb.append(appContext.getString(R.string.author)).append(": ").append(Libgit2Helper.getFormattedUsernameAndEmail(it.username, it.email)).appendLine().appendLine()
                sb.append(appContext.getString(R.string.msg)).append(": ").append(it.msg).appendLine().appendLine()


                detailsString.value = sb.toString()

                curClickItem.value = it
                showDetailsDialog.value=true
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
                            Libgit2Helper.getReflogList(repo, out=list.value);
                        }
                    }
                }


            }
        } catch (e: Exception) {
            MyLog.e(TAG, "$TAG#LaunchedEffect() err:"+e.stackTraceToString())
//            ("LaunchedEffect: job cancelled")
        }
    }

}
