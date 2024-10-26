package com.catpuppyapp.puppygit.screen

import android.annotation.SuppressLint
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
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.FilterAlt
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.VerticalAlignTop
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
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
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.catpuppyapp.puppygit.compose.BottomSheet
import com.catpuppyapp.puppygit.compose.BottomSheetItem
import com.catpuppyapp.puppygit.compose.ConfirmDialog
import com.catpuppyapp.puppygit.compose.ConfirmDialog2
import com.catpuppyapp.puppygit.compose.CredentialSelector
import com.catpuppyapp.puppygit.compose.DomainCredItem
import com.catpuppyapp.puppygit.compose.FilterTextField
import com.catpuppyapp.puppygit.compose.GoToTopAndGoToBottomFab
import com.catpuppyapp.puppygit.compose.LoadingDialog
import com.catpuppyapp.puppygit.compose.LongPressAbleIconBtn
import com.catpuppyapp.puppygit.compose.MyLazyColumn
import com.catpuppyapp.puppygit.compose.ScrollableColumn
import com.catpuppyapp.puppygit.compose.SmallFab
import com.catpuppyapp.puppygit.data.entity.CredentialEntity
import com.catpuppyapp.puppygit.data.entity.DomainCredentialEntity
import com.catpuppyapp.puppygit.data.entity.RemoteEntity
import com.catpuppyapp.puppygit.data.entity.RepoEntity
import com.catpuppyapp.puppygit.dto.DomainCredentialDto
import com.catpuppyapp.puppygit.play.pro.R
import com.catpuppyapp.puppygit.style.MyStyleKt
import com.catpuppyapp.puppygit.utils.AppModel
import com.catpuppyapp.puppygit.utils.Msg
import com.catpuppyapp.puppygit.utils.MyLog
import com.catpuppyapp.puppygit.utils.UIHelper
import com.catpuppyapp.puppygit.utils.changeStateTriggerRefreshPage
import com.catpuppyapp.puppygit.utils.doJobThenOffLoading
import com.catpuppyapp.puppygit.utils.state.mutableCustomStateListOf
import com.catpuppyapp.puppygit.utils.state.mutableCustomStateOf


private val TAG = "DomainCredentialListScreen"
private val stateKeyTag = "DomainCredentialListScreen"

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun DomainCredentialListScreen(
//    context: Context,
//    navController: NavHostController,
//    scope: CoroutineScope,
//    haptic: HapticFeedback,
//    homeTopBarScrollBehavior: TopAppBarScrollBehavior,
//    remoteId:String,

    naviUp: () -> Boolean
) {

    val homeTopBarScrollBehavior = AppModel.singleInstanceHolder.homeTopBarScrollBehavior
    val navController = AppModel.singleInstanceHolder.navController
    val appContext = AppModel.singleInstanceHolder.appContext
    val scope = rememberCoroutineScope()


    val list = mutableCustomStateListOf(keyTag = stateKeyTag, keyName = "list", initValue = listOf<DomainCredentialDto>() )
    val listState = rememberLazyListState()
    val curCredential = mutableCustomStateOf(keyTag = stateKeyTag, keyName = "curCredential", initValue = DomainCredentialDto())
    val needRefresh = rememberSaveable { mutableStateOf("")}
    val showLoadingDialog = rememberSaveable { mutableStateOf(true)}

    val loadingStrRes = stringResource(R.string.loading)
    val loadingText = rememberSaveable { mutableStateOf( loadingStrRes)}
    val loadingOn = {text:String ->
        loadingText.value=text
        showLoadingDialog.value=true
    }
    val loadingOff = {
        showLoadingDialog.value=false
        loadingText.value=""
    }

    val remote =mutableCustomStateOf(keyTag = stateKeyTag, keyName = "remote", initValue = RemoteEntity(id=""))
    val curRepo =mutableCustomStateOf(keyTag = stateKeyTag, keyName = "curRepo", initValue = RepoEntity(id=""))


    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = MyStyleKt.BottomSheet.skipPartiallyExpanded)
    val showBottomSheet = rememberSaveable { mutableStateOf(false)}
    val doDelete = {
        doJobThenOffLoading {
            try{
                val dcDb = AppModel.singleInstanceHolder.dbContainer.domainCredentialRepository
                dcDb.delete(DomainCredentialEntity(id=curCredential.value.domainCredId))
            }finally{
                changeStateTriggerRefreshPage(needRefresh)
            }
        }
    }


    val credentialList = mutableCustomStateListOf(stateKeyTag, "credentialList", listOf<CredentialEntity>())
    val selectedCredentialIdx = rememberSaveable{mutableIntStateOf(0)}


    val showCreateOrEditDialog = rememberSaveable { mutableStateOf( false)}
    val isCreate = rememberSaveable { mutableStateOf(false)}
    val curDomainNameErr = rememberSaveable { mutableStateOf("")}
    val curDomainName = rememberSaveable { mutableStateOf("")}
    val curId = rememberSaveable { mutableStateOf("")}  // current edit item id

    fun initCreateOrEditDialog(isCreateParam:Boolean, curDomainParam:String, curIdParam:String, curCredentialId:String){
        isCreate.value = isCreateParam
        curDomainName.value = curDomainParam
        curDomainNameErr.value=""
        curId.value = curIdParam

        val indexOf = credentialList.value.indexOfFirst { it.id == curCredentialId }
        selectedCredentialIdx.intValue = indexOf.coerceAtLeast(0)  // if not found, set to 0, else use found index

        showCreateOrEditDialog.value = true
    }

    if(showCreateOrEditDialog.value) {
        ConfirmDialog2(
            title = stringResource(if(isCreate.value) R.string.create else R.string.edit),
            requireShowTextCompose = true,
            textCompose = {
                ScrollableColumn {

                    TextField(
                        modifier = Modifier.fillMaxWidth(),

                        value = curDomainName.value,
                        onValueChange = {
                            curDomainName.value = it
                            curDomainNameErr.value=""
                        },
                        label = {
                            Text(stringResource(R.string.domain))
                        },

                        isError = curDomainNameErr.value.isNotEmpty(),
                        supportingText = {
                            if(curDomainNameErr.value.isNotEmpty()) {
                                Text(
                                    modifier = Modifier.fillMaxWidth(),
                                    text = curDomainNameErr.value,
                                    color = MaterialTheme.colorScheme.error
                                )

                            }
                        },
                        trailingIcon = {
                            if(curDomainNameErr.value.isNotEmpty()) {
                                Icon(imageVector= Icons.Filled.Error,
                                    contentDescription="err icon",
                                    tint = MaterialTheme.colorScheme.error)
                            }
                        },
                    )

                    Spacer(Modifier.height(15.dp))

                    CredentialSelector(credentialList.value, selectedCredentialIdx)

                }
            },
            okBtnText = stringResource(R.string.save),
            okBtnEnabled = curDomainName.value.isNotBlank() && curDomainNameErr.value.isEmpty(),
            onCancel = { showCreateOrEditDialog.value = false }
        ) {
            doJobThenOffLoading {
                try {
                    val newDomain = curDomainName.value
                    val newCredentialId = credentialList.value[selectedCredentialIdx.intValue].id
                    val dcDb = AppModel.singleInstanceHolder.dbContainer.domainCredentialRepository
                    if(isCreate.value) {
                        dcDb.insert(
                            DomainCredentialEntity(
                                domain = newDomain,
                                credentialId = newCredentialId
                            )
                        )
                    }else {
                        val old = dcDb.getById(curId.value) ?: throw RuntimeException("invalid id for update")
                        old.domain = newDomain
                        old.credentialId =newCredentialId

                        dcDb.update(old)
                    }

                    showCreateOrEditDialog.value=false

                    Msg.requireShow(appContext.getString(R.string.success))

                    changeStateTriggerRefreshPage(needRefresh)
                }catch (e:Exception) {
                    curDomainNameErr.value = e.localizedMessage ?:"err"
                }
            }
        }
    }


    val showDeleteDialog = rememberSaveable { mutableStateOf(false)}

    if(showDeleteDialog.value) {
        ConfirmDialog(
            title = stringResource(R.string.delete_credential),
            text = stringResource(R.string.are_you_sure),
            okTextColor = MyStyleKt.TextColor.danger,
            onCancel = { showDeleteDialog.value = false }
        ) {   // onOk
            showDeleteDialog.value=false
            doDelete()
        }
    }
    // 向下滚动监听，开始
    val pageScrolled = remember { mutableStateOf(false) }

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
    val lastAt = remember { mutableIntStateOf(0) }
    val lastIsScrollDown = remember { mutableStateOf(false) }
    val forUpdateScrollState = remember {
        derivedStateOf {
            val nowAt = if(enableFilterState.value) {
                filterListState.firstVisibleItemIndex
            } else {
                listState.firstVisibleItemIndex
            }
            val scrolledDown = nowAt > lastAt.intValue  // scroll down
//            val scrolledUp = nowAt < lastAt.intValue

            val scrolled = nowAt != lastAt.intValue  // scrolled
            lastAt.intValue = nowAt

            // only update state when this scroll down and last is not scroll down, or this is scroll up and last is not scroll up
            if(scrolled && ((lastIsScrollDown.value && !scrolledDown) || (!lastIsScrollDown.value && scrolledDown))) {
                pageScrolled.value = true
            }

            lastIsScrollDown.value = scrolledDown
        }
    }.value
    // 向下滚动监听，结束

    //filter相关，开始
    val filterKeyword = mutableCustomStateOf(
        keyTag = stateKeyTag,
        keyName = "filterKeyword",
        initValue = TextFieldValue("")
    )
    val filterModeOn = rememberSaveable { mutableStateOf(false)}
    //filter相关，结束

    Scaffold(
        modifier = Modifier.nestedScroll(homeTopBarScrollBehavior.nestedScrollConnection),
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,  //标题栏背景色
                    titleContentColor = MaterialTheme.colorScheme.primary,  //标题栏文字颜色
                ),
                title = {
                    if(filterModeOn.value) {
                        FilterTextField(
                            filterKeyword,
                        )
                    }else{
                        Column {
                            Row(modifier = Modifier.combinedClickable(onDoubleClick = { UIHelper.scrollToItem(scope, listState, 0) }) {}
                            ) {
                                Text(
                                    text = stringResource(id = R.string.domains),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )

                            }
                            if(remote.value.id.isNotEmpty()) {  //隐含 remoteId.isNotEmpty() 为 true
                                Row(modifier = Modifier.horizontalScroll(rememberScrollState())) {
                                    Text(
                                        text= stringResource(id = R.string.link_mode)+": ["+remote.value.remoteName+":${curRepo.value.repoName}]",
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        fontSize = MyStyleKt.Title.secondLineFontSize
                                    )
                                }

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

                            LongPressAbleIconBtn(
                                tooltipText = stringResource(R.string.create),
                                icon =  Icons.Filled.Add,
                                iconContentDesc = stringResource(id = R.string.create_new_credential),
                            ) {
                                initCreateOrEditDialog(isCreateParam = true, curDomainParam = "", curIdParam = "", curCredentialId="")
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
                    filterModeOn = enableFilterState,
                    scope = scope,
                    filterListState = filterListState,
                    listState = listState,
                    pageScrolled = pageScrolled
                )

            }
        }
    ) { contentPadding ->

        if(showBottomSheet.value) {
            BottomSheet(showBottomSheet, sheetState, curCredential.value.domain) {
                //改成在关联页面有这个功能了，在这就不显示了
    //            BottomSheetItem(sheetState=sheetState, showBottomSheet=showBottomSheet, text=stringResource(R.string.unlink_all)){
    //                //显示弹窗，询问将会与所有remotes解除关联，是否确定？
    //            }

                BottomSheetItem(sheetState=sheetState, showBottomSheet=showBottomSheet, text=stringResource(R.string.delete), textColor = MyStyleKt.TextColor.danger){
                    showDeleteDialog.value=true
                }
            }

        }

        if (showLoadingDialog.value) {
            LoadingDialog(text = loadingText.value)  //这个东西太阴间了，还是用LoadingText吧
//
//            LoadingText(text = loadingText.value,contentPadding = contentPadding)

        }else {

            //根据关键字过滤条目
            val k = filterKeyword.value.text.lowercase()  //关键字
            val enableFilter = filterModeOn.value && k.isNotEmpty()
            val list = if(enableFilter){
                list.value.filter {
                    it.domain.lowercase().contains(k) || (it.credName?.lowercase()?.contains(k) == true)
                }
            }else {
                list.value
            }
            val listState = if(enableFilter) filterListState else listState
//            if(enableFilter) {  //更新filter列表state
//                filterListState.value = listState
//            }
            //更新是否启用filter
            enableFilterState.value = enableFilter

            MyLazyColumn(
                contentPadding = contentPadding,
                list = list,
                listState = listState,
                requireForEachWithIndex = true,
                requirePaddingAtBottom = true
            ) {idx, value->
                DomainCredItem (showBottomSheet = showBottomSheet, curCredentialState = curCredential, idx = idx, thisItem = value) {
                    initCreateOrEditDialog(isCreateParam = false, curDomainParam = value.domain, curIdParam = value.domainCredId, curCredentialId=value.credId?:"")
                }

                HorizontalDivider()

            }
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
//        println("LaunchedEffect: entered main")
        // Just an example of coroutines usage
        // don't use this way to track screen disappearance
        // DisposableEffect is better for this
        try {
            doJobThenOffLoading(loadingOn = loadingOn, loadingOff = loadingOff, loadingText=appContext.getString(R.string.loading)) job@{
                list.value.clear()
                credentialList.value.clear()

                val dcDb = AppModel.singleInstanceHolder.dbContainer.domainCredentialRepository
                list.value.addAll(dcDb.getAllDto())

                val credentialDb = AppModel.singleInstanceHolder.dbContainer.credentialRepository
                // link to none = no link
                val credentialListFromDb = credentialDb.getAll(includeNone = true, includeMatchByDomain = false)
                if(credentialListFromDb.isNotEmpty()) {
                    credentialList.value.addAll(credentialListFromDb)
                }
//                list.requireRefreshView()
            }
//            读取配置文件，初始化状态之类的
        } catch (cancel: Exception) {
            MyLog.e(TAG, "#LaunchedEffect: ${cancel.localizedMessage}")
//            println("LaunchedEffect: job cancelled")
        }
    }
//
//    //compose被销毁时执行的副作用
//    DisposableEffect(Unit) {
////        println("DisposableEffect: entered main")
//        onDispose {
////            println("DisposableEffect: exited main")
//        }
//    }
}