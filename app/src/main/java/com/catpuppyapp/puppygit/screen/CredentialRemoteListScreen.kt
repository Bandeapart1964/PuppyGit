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
import androidx.compose.material.icons.filled.AddLink
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FilterAlt
import androidx.compose.material.icons.filled.LinkOff
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
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.sp
import com.catpuppyapp.puppygit.compose.ConfirmDialog
import com.catpuppyapp.puppygit.compose.FilterTextField
import com.catpuppyapp.puppygit.compose.LinkOrUnLinkCredentialAndRemoteDialog
import com.catpuppyapp.puppygit.compose.LongPressAbleIconBtn
import com.catpuppyapp.puppygit.compose.MyLazyColumn
import com.catpuppyapp.puppygit.compose.RemoteItemForCredential
import com.catpuppyapp.puppygit.compose.SmallFab
import com.catpuppyapp.puppygit.constants.Cons
import com.catpuppyapp.puppygit.data.entity.CredentialEntity
import com.catpuppyapp.puppygit.dto.RemoteDtoForCredential
import com.catpuppyapp.puppygit.play.pro.R
import com.catpuppyapp.puppygit.style.MyStyleKt
import com.catpuppyapp.puppygit.utils.AppModel
import com.catpuppyapp.puppygit.utils.Msg
import com.catpuppyapp.puppygit.utils.MyLog
import com.catpuppyapp.puppygit.utils.UIHelper
import com.catpuppyapp.puppygit.utils.changeStateTriggerRefreshPage
import com.catpuppyapp.puppygit.utils.createAndInsertError
import com.catpuppyapp.puppygit.utils.doJobThenOffLoading
import com.catpuppyapp.puppygit.utils.state.StateUtil
import com.catpuppyapp.puppygit.utils.state.mutableCustomStateListOf
import com.catpuppyapp.puppygit.utils.state.mutableCustomStateOf

//private val TAG = "CredentialLinkedOrUnLinkedListScreen"
private val TAG = "CredentialRemoteListScreen"

private val stateKeyTag = "CredentialRemoteListScreen"

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun CredentialRemoteListScreen(
    credentialId:String,
    isShowLink:Boolean,
    naviUp: () -> Unit,
) {

    val homeTopBarScrollBehavior = AppModel.singleInstanceHolder.homeTopBarScrollBehavior
    val navController = AppModel.singleInstanceHolder.navController
    val appContext = AppModel.singleInstanceHolder.appContext
    val scope = rememberCoroutineScope()

    //这个页面的滚动状态不用记住，每次点开重置也无所谓
    val listState = rememberLazyListState()
    //如果再多几个"mode"，就改用字符串判断，直接把mode含义写成常量
    val isSearchingMode = rememberSaveable { mutableStateOf(false)}
    val isShowSearchResultMode = rememberSaveable { mutableStateOf(false)}
    val searchKeyword = rememberSaveable { mutableStateOf("")}
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = MyStyleKt.BottomSheet.skipPartiallyExpanded)
    val showBottomSheet = rememberSaveable { mutableStateOf(false)}
    val showUnLinkAllDialog = rememberSaveable { mutableStateOf(false)}
//    val curCommit = rememberSaveable{ mutableStateOf(CommitDto()) }
    val curItemInPage = mutableCustomStateOf(keyTag = stateKeyTag, keyName = "curItemInPage", initValue = CredentialEntity())
    val list = mutableCustomStateListOf(keyTag = stateKeyTag, keyName = "list", initValue = listOf<RemoteDtoForCredential>())
    val needOverrideLinkItem = mutableCustomStateOf(keyTag = stateKeyTag, keyName = "needOverrideLinkItem", initValue = RemoteDtoForCredential())
    val showOverrideLinkDialog = rememberSaveable { mutableStateOf(false)}
    val needRefresh = rememberSaveable { mutableStateOf("")}

    val showLinkOrUnLinkDialog = rememberSaveable { mutableStateOf( false)}
    val requireDoLink = rememberSaveable { mutableStateOf(false)}
    val targetAll = rememberSaveable { mutableStateOf(false)}
    val curItem = mutableCustomStateOf(keyTag = stateKeyTag, keyName = "curItem", initValue = RemoteDtoForCredential())
    val linkOrUnlinkDialogTitle = rememberSaveable { mutableStateOf( "")}


    val doLink= { remoteId: String ->
        doJobThenOffLoading {
            val remoteDb = AppModel.singleInstanceHolder.dbContainer.remoteRepository
            remoteDb.linkCredentialIdByRemoteId(remoteId, curItemInPage.value.id)
            changeStateTriggerRefreshPage(needRefresh)

        }
    }
    val doUnLink={ remoteId:String ->
        doJobThenOffLoading {
            val remoteDb = AppModel.singleInstanceHolder.dbContainer.remoteRepository
            remoteDb.unlinkCredentialIdByRemoteId(remoteId)
            changeStateTriggerRefreshPage(needRefresh)
        }
    }

    val doUnLinkAll = {
        //确认后执行此方法
        doJobThenOffLoading {
            val remoteDb = AppModel.singleInstanceHolder.dbContainer.remoteRepository
            remoteDb.unlinkAllCredentialIdByCredentialId(curItemInPage.value.id)
            changeStateTriggerRefreshPage(needRefresh)
        }
    }

    if(showUnLinkAllDialog.value) {
        ConfirmDialog(title = stringResource(id = R.string.unlink_all),
            text = stringResource(id = R.string.unlink_all_ask_text),
            okTextColor = MyStyleKt.TextColor.danger,
            onCancel = {showUnLinkAllDialog.value=false }
        ) {
            showUnLinkAllDialog.value=false
            doUnLinkAll()
        }
    }

    if(showOverrideLinkDialog.value) {
        ConfirmDialog(title = stringResource(id = R.string.override_link),
            text = stringResource(id = R.string.override_link_ask_text),
            okTextColor = MyStyleKt.TextColor.danger,
            onCancel = {showOverrideLinkDialog.value=false }
        ) {
            showOverrideLinkDialog.value=false
            doLink(needOverrideLinkItem.value.remoteId)
        }
    }

    if(showLinkOrUnLinkDialog.value) {
        LinkOrUnLinkCredentialAndRemoteDialog(
            curItemInPage,
            requireDoLink.value,
            targetAll.value,
            linkOrUnlinkDialogTitle.value,
            curItem.value,
            onCancel = {showLinkOrUnLinkDialog.value=false},
            onFinallyCallback = {
                showLinkOrUnLinkDialog.value=false
                changeStateTriggerRefreshPage(needRefresh)
            },
            onErrCallback = { e->
                //不用写仓库名，因为错误会归类到对应仓库id上，所以，会在对应仓库卡片上看到错误信息，因此点记错误时可以通过从哪点进来的得知是哪个仓库的错误信息，再通过错误信息更进一步知道remote关联哪个credential发生的错误
                val errMsgPrefix = "${linkOrUnlinkDialogTitle.value} err: remote='${curItem.value.remoteName}', credential=${curItemInPage.value.name}, err="
                Msg.requireShowLongDuration(e.localizedMessage ?: errMsgPrefix)
                createAndInsertError(curItem.value.repoId, errMsgPrefix + e.localizedMessage)
                MyLog.e(TAG, "#LinkOrUnLinkCredentialAndRemoteDialog err: $errMsgPrefix${e.stackTraceToString()}")
            },
            onOkCallback = {
                Msg.requireShow(appContext.getString(R.string.success))
            }
        )
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
                    }else{
                        Column (modifier = Modifier.combinedClickable(onDoubleClick = { UIHelper.scrollToItem(scope, listState, 0) }) {}){
                            Row(modifier = Modifier.horizontalScroll(rememberScrollState())) {
                                Text(
                                    text= if(isShowLink) stringResource(R.string.linked_remotes) else stringResource(R.string.unlinked_remotes),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )

                            }
                            Row(modifier = Modifier.horizontalScroll(rememberScrollState())) {
                                Text(
                                    text= "["+curItemInPage.value.name+"]",
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    fontSize = 12.sp
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
                    }else{
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
                        Row {
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
                                iconContentDesc = stringResource(id = R.string.refresh),
                            ) {
                                changeStateTriggerRefreshPage(needRefresh)
                            }

                            if (isShowLink) {
                                LongPressAbleIconBtn(
                                    tooltipText = stringResource(R.string.unlink_all),
                                    icon = Icons.Filled.LinkOff,
                                    iconContentDesc = stringResource(R.string.unlink_all),
                                    enabled = list.value.isNotEmpty()
                                ) {
//                                showUnLinkAllDialog.value = true

                                    requireDoLink.value = false
                                    targetAll.value = true
                                    linkOrUnlinkDialogTitle.value = appContext.getString(R.string.unlink_all)
                                    showLinkOrUnLinkDialog.value = true
                                }

                                LongPressAbleIconBtn(
                                    tooltipText = stringResource(R.string.create_link),  //新建关联（显示未关联列表）
                                    icon = Icons.Filled.AddLink,
                                    iconContentDesc = stringResource(R.string.create_link),

                                    ) {
                                    navController.navigate(Cons.nav_CredentialRemoteListScreen + "/" + credentialId + "/0")
                                }

                            }
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

        //根据关键字过滤条目
        val k = filterKeyword.value.text.lowercase()  //关键字
        val enableFilter = filterModeOn.value && k.isNotEmpty()
        val list = if(enableFilter){
            list.value.filter {
                    it.repoName.lowercase().contains(k)
                        || it.remoteName.lowercase().contains(k)
                        || it.getCredentialNameOrNone().lowercase().contains(k)
                        || it.getPushCredentialNameOrNone().lowercase().contains(k)
            }
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
            requirePaddingAtBottom = true
        ) { idx,it->
            RemoteItemForCredential(
                isShowLink=isShowLink,
                idx = idx, thisItem = it,
                actText = if(isShowLink)stringResource(R.string.unlink) else stringResource(R.string.link),
            ){
                curItem.value = it
                requireDoLink.value = !isShowLink
                targetAll.value = false
                linkOrUnlinkDialogTitle.value=if(requireDoLink.value) appContext.getString(R.string.link) else appContext.getString(R.string.unlink)  // (不建议，不方便记Err)若空字符串，将会自动根据requireDoLink的值决定使用link还是unlink作为title
                showLinkOrUnLinkDialog.value=true

//                if(isShowLink) {  //如果是显示已关联条目的页面，点击取关直接执行
//                    doUnLink(it.remoteId)
//                }else{  //如果是显示未关联条目的页面，检查是否已关联其他凭据，如果没有，直接关联，如果关联了，询问是否覆盖
//                    if(it.credentialName==null || it.credentialName!!.isBlank()) {  //没关联其他凭据
//                        doLink(it.remoteId)
//                    }else { //条目已经关联了其他credential，弹窗询问是否覆盖
//                        needOverrideLinkItem.value = it
//                        showOverrideLinkDialog.value = true
//                    }
//                }

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
            doJobThenOffLoading {
                val remoteDb = AppModel.singleInstanceHolder.dbContainer.remoteRepository
                val credentialDb = AppModel.singleInstanceHolder.dbContainer.credentialRepository
                //这个页面用不到密码，所以查询的是加密后的密码，没解密
                curItemInPage.value = credentialDb.getById(credentialId)?:CredentialEntity(id="")
                val listFromDb = if (isShowLink) {
                    remoteDb.getLinkedRemoteDtoForCredentialList(credentialId)
                }else {
                    remoteDb.getUnlinkedRemoteDtoForCredentialList(credentialId)
                }
                list.value.clear()
                list.value.addAll(listFromDb)
//                list.requireRefreshView()
            }
        } catch (cancel: Exception) {
//            println("LaunchedEffect: job cancelled")
        }
    }
    //compose被销毁时执行的副作用
    DisposableEffect(Unit) {
//        println("DisposableEffect: entered main")
        onDispose {
//            println("DisposableEffect: exited main")
        }
    }

}