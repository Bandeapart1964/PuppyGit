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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Domain
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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextOverflow
import com.catpuppyapp.puppygit.compose.BottomSheet
import com.catpuppyapp.puppygit.compose.BottomSheetItem
import com.catpuppyapp.puppygit.compose.ConfirmDialog
import com.catpuppyapp.puppygit.compose.CredentialItem
import com.catpuppyapp.puppygit.compose.FilterTextField
import com.catpuppyapp.puppygit.compose.LinkOrUnLinkCredentialAndRemoteDialog
import com.catpuppyapp.puppygit.compose.LoadingText
import com.catpuppyapp.puppygit.compose.LongPressAbleIconBtn
import com.catpuppyapp.puppygit.compose.MyLazyColumn
import com.catpuppyapp.puppygit.compose.SmallFab
import com.catpuppyapp.puppygit.constants.Cons
import com.catpuppyapp.puppygit.data.entity.CredentialEntity
import com.catpuppyapp.puppygit.data.entity.RemoteEntity
import com.catpuppyapp.puppygit.data.entity.RepoEntity
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


private val TAG = "CredentialManagerScreen"
private val stateKeyTag = "CredentialManagerScreen"

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun CredentialManagerScreen(
//    context: Context,
//    navController: NavHostController,
//    scope: CoroutineScope,
//    haptic: HapticFeedback,
//    homeTopBarScrollBehavior: TopAppBarScrollBehavior,
    remoteId:String,

    naviUp: () -> Boolean
) {
//    SideEffect {
//        Msg.msgNotifyHost()
//    }

    val homeTopBarScrollBehavior = AppModel.singleInstanceHolder.homeTopBarScrollBehavior
    val navController = AppModel.singleInstanceHolder.navController
    val appContext = AppModel.singleInstanceHolder.appContext
    val scope = rememberCoroutineScope()

    // for link credential to remote
    val isLinkMode = remoteId.isNotBlank()


    val list = StateUtil.getCustomSaveableStateList(keyTag = stateKeyTag, keyName = "list", initValue = listOf<CredentialEntity>() )
    val listState = rememberLazyListState()
    val curCredential =StateUtil.getCustomSaveableState(keyTag = stateKeyTag, keyName = "curCredential", initValue = CredentialEntity(id=""))
    val needRefresh = StateUtil.getRememberSaveableState(initValue = "")
    val showLoadingDialog = StateUtil.getRememberSaveableState(initValue = true)

    val loadingStrRes = stringResource(R.string.loading)
    val loadingText = StateUtil.getRememberSaveableState(initValue = loadingStrRes)
    val loadingOn = {text:String ->
        loadingText.value=text
        showLoadingDialog.value=true
    }
    val loadingOff = {
        showLoadingDialog.value=false
        loadingText.value=""
    }


    val remote =StateUtil.getCustomSaveableState(keyTag = stateKeyTag, keyName = "remote", initValue = RemoteEntity(id=""))
    val curRepo =StateUtil.getCustomSaveableState(keyTag = stateKeyTag, keyName = "curRepo", initValue = RepoEntity(id=""))
    val showLinkOrUnLinkDialog = StateUtil.getRememberSaveableState(initValue = false)
    val onClickCurItem =StateUtil.getCustomSaveableState(keyTag = stateKeyTag, keyName = "onClickCurItem", initValue = CredentialEntity(id=""))  //非点击跳转页面的情况下，点击条目后更新此变量
    val requireDoLink = StateUtil.getRememberSaveableState(initValue = false)
    val targetAll = StateUtil.getRememberSaveableState(initValue = false)
    val remoteDtoForCredential = StateUtil.getCustomSaveableState(keyTag = stateKeyTag, keyName = "remoteDtoForCredential", initValue = RemoteDtoForCredential(remoteId = remoteId))

    val linkOrUnLinkDialogTitle = StateUtil.getRememberSaveableState(initValue = "")

    if(showLinkOrUnLinkDialog.value) {
        LinkOrUnLinkCredentialAndRemoteDialog(
            curItemInPage = onClickCurItem,
            requireDoLink = requireDoLink.value,
            targetAll = targetAll.value,
            title = linkOrUnLinkDialogTitle.value,  //若空字符串，会根据上面的flag自动判断显示link还是unlink
            thisItem = remoteDtoForCredential.value,
            onCancel = { showLinkOrUnLinkDialog.value=false},
            onFinallyCallback = {
                showLinkOrUnLinkDialog.value=false
                //关联模式下，似乎，没必要刷新页面啊？但刷新下其实也没什么
                changeStateTriggerRefreshPage(needRefresh)
            },
            onErrCallback = { e->
                val errMsgPrefix = "${linkOrUnLinkDialogTitle.value} err: remote='${remote.value.remoteName}', credential=${onClickCurItem.value.name}, err="
                Msg.requireShowLongDuration(e.localizedMessage ?: errMsgPrefix)
                createAndInsertError(remote.value.repoId, errMsgPrefix + e.localizedMessage)
                MyLog.e(TAG, "#LinkOrUnLinkCredentialAndRemoteDialog err: $errMsgPrefix${e.stackTraceToString()}")
            },
            onOkCallback = {
                Msg.requireShow(appContext.getString(R.string.success))
            }

        )

    }


    val sheetState = StateUtil.getRememberModalBottomSheetState()
    val showBottomSheet = StateUtil.getRememberSaveableState(initValue = false)
    val doDelete = {
        doJobThenOffLoading {
            try{
                val credentialDb = AppModel.singleInstanceHolder.dbContainer.credentialRepository
                credentialDb.deleteAndUnlink(curCredential.value)
            }finally{
                changeStateTriggerRefreshPage(needRefresh)
            }
        }
    }
    val showDeleteDialog = StateUtil.getRememberSaveableState(initValue = false)
    
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
    val scrollingDown = remember { mutableStateOf(false) }

    val filterListState = StateUtil.getCustomSaveableState(
        keyTag = stateKeyTag,
        keyName = "filterListState"
    ) {
        LazyListState(0,0)
    }
    val enableFilterState = StateUtil.getRememberSaveableState(initValue = false)
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

    //filter相关，开始
    val filterKeyword = StateUtil.getCustomSaveableState(
        keyTag = stateKeyTag,
        keyName = "filterKeyword",
        initValue = TextFieldValue("")
    )
    val filterModeOn = StateUtil.getRememberSaveableState(initValue = false)
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
                                    text = stringResource(id = R.string.credential_manager),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )

                            }
                            if(remote.value.id.isNotEmpty()) {  //隐含 remoteId.isNotEmpty() 为 true
                                Row(modifier = Modifier.horizontalScroll(StateUtil.getRememberScrollState())) {
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
                                tooltipText = stringResource(R.string.domains),
                                icon =  Icons.Filled.Domain,
                                iconContentDesc = stringResource(id = R.string.domains),
                            ) {
                                navController.navigate(Cons.nav_DomainCredentialListScreen)
                            }

                            LongPressAbleIconBtn(
                                tooltipText = stringResource(R.string.create),
                                icon =  Icons.Filled.Add,
                                iconContentDesc = stringResource(id = R.string.create_new_credential),
                            ) {
                                //从这跳是新建，直接传null
                                navController.navigate(Cons.nav_CredentialNewOrEditScreen+"/"+null)
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

        if(showBottomSheet.value) {
            BottomSheet(showBottomSheet, sheetState, curCredential.value.name) {
                BottomSheetItem(sheetState=sheetState, showBottomSheet=showBottomSheet, text=stringResource(R.string.edit)){
                    //跳转到编辑页面
                    //在这里可以直接用state curObj取到当前选中条目，curObjInState在长按条目后会被更新为当前被长按的条目
                    navController.navigate(Cons.nav_CredentialNewOrEditScreen+"/"+curCredential.value.id)

                }

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
//            LoadingDialog()  //这个东西太阴间了，还是用LoadingText吧

            LoadingText(text = loadingText.value,contentPadding = contentPadding)

        }else {

            //根据关键字过滤条目
            val k = filterKeyword.value.text.lowercase()  //关键字
            val enableFilter = filterModeOn.value && k.isNotEmpty()
            val list = if(enableFilter){
                list.value.filter {
                    it.name.lowercase().contains(k) || it.value.lowercase().contains(k)
                            || it.getTypeStr().lowercase().contains(k)
                }
            }else {
                list.value
            }
            val listState = if(enableFilter) StateUtil.getRememberLazyListState() else listState
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
            ) {idx, value->
                CredentialItem(showBottomSheet = showBottomSheet, curCredentialState = curCredential, idx = idx, thisItem = value) {

                    //这里value和it和传给CredentialItem的thisItem值都一样，只是参数传来传去而已

                    if(remoteId.isEmpty()) {  //若remoteId为空，跳转到remote和凭据绑定页面
                        // 点击跳转到关联列表，传1表示显示的是关联列表
                        navController.navigate(Cons.nav_CredentialRemoteListScreen+"/"+it.id+"/1")
                    }else {  //若remoteId不为空，则代表为此remoteId绑定凭据，点击条目弹窗
                        //为弹窗准备参数
                        onClickCurItem.value = it  //虽然这里是被点击的条目，但其实用保存长按条目的状态变量curCredential也可以，不过为了避免混淆，没用那个
                        requireDoLink.value = true
                        targetAll.value=false

                        //remoteDtoForCredential.remoteId 改成在初始化时赋值了，remoteId是常量，初始化一次即可，不需每次都赋值
//                        remoteDtoForCredential.value = RemoteDtoForCredential(remoteId=remoteId)
//                        remoteDtoForCredential.value.remoteId = remoteId  //由于remoteDtoForCredential修改后并不需要刷新页面，所以不用重新赋值，一个对象反复用，把需要使用的字段更新下就行
                        linkOrUnLinkDialogTitle.value = appContext.getString(R.string.link)+" '${it.name}'"
                        //显示弹窗
                        showLinkOrUnLinkDialog.value=true
                    }
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
            doJobThenOffLoading(loadingOn = loadingOn, loadingOff = loadingOff, loadingText = appContext.getString(R.string.loading)) job@{
                val credentialDb = AppModel.singleInstanceHolder.dbContainer.credentialRepository
                list.value.clear()
                list.value.addAll(credentialDb.getAll(includeMatchByDomain = isLinkMode))

                if(isLinkMode) {
                    val remoteFromDb = AppModel.singleInstanceHolder.dbContainer.remoteRepository.getById(remoteId)
                    if(remoteFromDb!=null){
                        remote.value=remoteFromDb
                        val repoFromDb = AppModel.singleInstanceHolder.dbContainer.repoRepository.getById(remoteFromDb.repoId)
                        if(repoFromDb!=null) {
                            curRepo.value = repoFromDb
                        }
                    }
                }
//                list.requireRefreshView()
            }
//            读取配置文件，初始化状态之类的
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