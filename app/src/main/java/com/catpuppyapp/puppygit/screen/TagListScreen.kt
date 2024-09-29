package com.catpuppyapp.puppygit.screen

import android.annotation.SuppressLint
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.FilterAlt
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.SelectAll
import androidx.compose.material.icons.filled.Upload
import androidx.compose.material.icons.filled.VerticalAlignTop
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.catpuppyapp.puppygit.compose.BottomBar
import com.catpuppyapp.puppygit.compose.CheckoutDialog
import com.catpuppyapp.puppygit.compose.CheckoutDialogFrom
import com.catpuppyapp.puppygit.compose.CopyableDialog
import com.catpuppyapp.puppygit.compose.CreateTagDialog
import com.catpuppyapp.puppygit.compose.FilterTextField
import com.catpuppyapp.puppygit.compose.ResetDialog
import com.catpuppyapp.puppygit.compose.LoadingDialog
import com.catpuppyapp.puppygit.compose.LongPressAbleIconBtn
import com.catpuppyapp.puppygit.compose.MyLazyColumn
import com.catpuppyapp.puppygit.compose.SmallFab
import com.catpuppyapp.puppygit.compose.TagFetchPushDialog
import com.catpuppyapp.puppygit.compose.TagItem
import com.catpuppyapp.puppygit.constants.Cons
import com.catpuppyapp.puppygit.data.entity.RepoEntity
import com.catpuppyapp.puppygit.git.TagDto
import com.catpuppyapp.puppygit.play.pro.R
import com.catpuppyapp.puppygit.style.MyStyleKt
import com.catpuppyapp.puppygit.ui.theme.Theme
import com.catpuppyapp.puppygit.utils.AppModel
import com.catpuppyapp.puppygit.utils.Libgit2Helper
import com.catpuppyapp.puppygit.utils.Msg
import com.catpuppyapp.puppygit.utils.MyLog
import com.catpuppyapp.puppygit.utils.UIHelper
import com.catpuppyapp.puppygit.utils.cache.Cache
import com.catpuppyapp.puppygit.utils.changeStateTriggerRefreshPage
import com.catpuppyapp.puppygit.utils.createAndInsertError
import com.catpuppyapp.puppygit.utils.doActIfIndexGood
import com.catpuppyapp.puppygit.utils.doJobThenOffLoading
import com.catpuppyapp.puppygit.utils.state.StateUtil
import com.github.git24j.core.Repository

private val TAG = "TagListScreen"
private val stateKeyTag = "TagListScreen"

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun TagListScreen(
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
    val list = StateUtil.getCustomSaveableStateList(keyTag = stateKeyTag, keyName = "list", initValue = listOf<TagDto>())

    val filterList = StateUtil.getCustomSaveableStateList(keyTag = stateKeyTag, keyName = "filterList", initValue = listOf<TagDto>())

    //这个页面的滚动状态不用记住，每次点开重置也无所谓
    val listState = StateUtil.getRememberLazyListState()
    val needRefresh = StateUtil.getRememberSaveableState(initValue = "")
    val curRepo = StateUtil.getCustomSaveableState(keyTag = stateKeyTag, keyName = "curRepo", initValue = RepoEntity(id=""))

    val defaultLoadingText = stringResource(R.string.loading)
    val loading = StateUtil.getRememberSaveableState(initValue = false)
    val loadingText = StateUtil.getRememberSaveableState(initValue = defaultLoadingText)
    val loadingOn = { text:String ->
        loadingText.value=text
        loading.value=true
    }
    val loadingOff = {
        loadingText.value = appContext.getString(R.string.loading)
        loading.value=false
    }



    val nameOfNewTag = StateUtil.getRememberSaveableState(initValue = "")
    val overwriteIfNameExistOfNewTag = StateUtil.getRememberSaveableState(initValue = false)
    val showDialogOfNewTag = StateUtil.getRememberSaveableState(initValue = false)
    val hashOfNewTag = StateUtil.getRememberSaveableState(initValue = "")
    val msgOfNewTag = StateUtil.getRememberSaveableState(initValue = "")
//    val requireUserInputHashOfNewTag = StateUtil.getRememberSaveableState(initValue = false)
    val annotateOfNewTag = StateUtil.getRememberSaveableState(initValue = false)
    val initNewTagDialog = { hash:String ->
//        hashOfNewTag.value = hash  //这里不重置hash值了，感觉不重置用户体验更好？

        overwriteIfNameExistOfNewTag.value = false
        showDialogOfNewTag.value = true
    }

    if(showDialogOfNewTag.value) {
        CreateTagDialog(
            showDialog = showDialogOfNewTag,
            curRepo = curRepo.value,
            tagName = nameOfNewTag,
            commitHashShortOrLong = hashOfNewTag,
            annotate = annotateOfNewTag,
            tagMsg = msgOfNewTag,
            force = overwriteIfNameExistOfNewTag
        ) {
            changeStateTriggerRefreshPage(needRefresh)
        }
    }

    // BottomBar相关变量，开始
    val multiSelectionMode = StateUtil.getRememberSaveableState(initValue = false)
    val selectedItemList = StateUtil.getCustomSaveableStateList(keyTag = stateKeyTag, keyName = "selectedItemList") { listOf<TagDto>() }
    val quitSelectionMode = {
        selectedItemList.value.clear()  //清空选中文件列表
        multiSelectionMode.value=false  //关闭选择模式
    }
    val iconList:List<ImageVector> = listOf(
        Icons.Filled.Delete,  //删除
        Icons.Filled.Upload,  //上传（push）
        Icons.Filled.SelectAll,  //全选
    )
    val iconTextList:List<String> = listOf(
        stringResource(id = R.string.delete),
        stringResource(id = R.string.push),
        stringResource(id = R.string.select_all),
    )
    val iconEnableList:List<()->Boolean> = listOf(
        {selectedItemList.value.isNotEmpty()},  // delete
        {selectedItemList.value.isNotEmpty()},  // push
        {true} // select all
    )

    val moreItemTextList = listOf(
        stringResource(R.string.checkout),
        stringResource(R.string.reset),  //日后改成reset并可选模式 soft/mixed/hard
        stringResource(R.string.details),  //可针对单个或多个条目查看details，多个时，用分割线分割多个条目的信息
    )

    val getSelectedFilesCount = {
        selectedItemList.value.size
    }
    val moreItemEnableList:List<()->Boolean> = listOf(
        {selectedItemList.value.size==1},  // checkout
        {selectedItemList.value.size==1},  // hardReset
        {selectedItemList.value.isNotEmpty()}  // details
    )
    // BottomBar相关变量，结束

    //多选模式相关函数，开始
    val switchItemSelected = { item: TagDto ->
        //如果元素不在已选择条目列表则添加
        UIHelper.selectIfNotInSelectedListElseRemove(item, selectedItemList.value)
        //开启选择模式
        multiSelectionMode.value = true
    }

    val selectItem = { item:TagDto ->
        UIHelper.selectIfNotInSelectedListElseNoop(item, selectedItemList.value)
    }

    val isItemInSelected= { item:TagDto ->
        selectedItemList.value.contains(item)
    }
    // 多选模式相关函数，结束




    // hardReset start
//    val acceptHardReset = StateUtil.getRememberSaveableState(initValue = false)
    val resetOid = StateUtil.getRememberSaveableState(initValue = "")
    val showResetDialog = StateUtil.getRememberSaveableState(initValue = false)
    val closeResetDialog = {
        showResetDialog.value = false
    }
    val initResetDialog = { resetOidParam:String ->
        //初始化弹窗默认选项
//        acceptHardReset.value = false
        resetOid.value = resetOidParam
        showResetDialog.value = true
    }

    if (showResetDialog.value) {
        //调用者需确保至少选中一个条目，不然会报错，这里由界面“没选中任何条目则禁用选项”的逻辑来控制，所以不需要判断
        val item = selectedItemList.value.first()

        ResetDialog(
            fullOidOrBranchOrTag = resetOid,
            closeDialog=closeResetDialog,
            repoFullPath = curRepo.value.fullSavePath,
            repoId=repoId,
            refreshPage = { oldHeadCommitOid, isDetached ->
                //更新下仓库信息以使title在仓库为detached HEAD时显示出reset后的hash。非detached HEAD时只是更新分支指向的提交号分支本身不变，所以不用更新
                if(isDetached) {
                    curRepo.value = curRepo.value.copy(isDetached= Cons.dbCommonTrue, lastCommitHash = Libgit2Helper.getShortOidStrByFull(item.targetFullOidStr))
                }
            }
        )
    }
    // hardReset end


    //checkout start
    val showCheckoutDialog = StateUtil.getRememberSaveableState(initValue = false)
    val invalidCurItemIndex = -1  //本页面不要更新被选中执行checkout的条目，所以设个无效id即可

    //初始化 checkout对话框
    val initCheckoutDialogComposableVersion = {
        showCheckoutDialog.value = true
    }

    if(showCheckoutDialog.value) {
        val item = selectedItemList.value.first()

        CheckoutDialog(
            showCheckoutDialog=showCheckoutDialog,
            from = CheckoutDialogFrom.OTHER,
//            expectCheckoutType = Cons.checkoutTypeCommit,  //用这个reflog不会包含tag名
            expectCheckoutType = Cons.checkoutType_checkoutRefThenDetachHead,  //用这个会包含tag名
            shortName = item.shortName,
            fullName = item.name,
            curRepo = curRepo.value,
            curCommitOid = item.targetFullOidStr,
            curCommitShortOid = Libgit2Helper.getShortOidStrByFull(item.targetFullOidStr),
            requireUserInputCommitHash = false,
            loadingOn = loadingOn,
            loadingOff = loadingOff,
            onlyUpdateCurItem = false,
            updateCurItem = {curItemIdx, fullOid-> },  //不需要更新当前条目
            refreshPage = {
               //更新当前仓库信息即可，目的是在title显示出最新的分支或提交信息
                doJobThenOffLoading job@{
                    curRepo.value = AppModel.singleInstanceHolder.dbContainer.repoRepository.getById(repoId) ?: return@job
                }
            },
            curCommitIndex = invalidCurItemIndex,  //不需要更新条目，自然不需要有效索引
            findCurItemIdxInList = { fullOid->
                invalidCurItemIndex  //无效id，不需要更新条目
            }
        )
    }
    //checkout end

    val clipboardManager = LocalClipboardManager.current

    val showDetailsDialog = StateUtil.getRememberSaveableState(initValue = false)
    val detailsString = StateUtil.getRememberSaveableState(initValue = "")
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

    val moreItemOnClickList:List<()->Unit> = listOf(
        checkout@{
            initCheckoutDialogComposableVersion()
        },
        hardReset@{
            doActIfIndexGood(0, selectedItemList.value) { item ->
                initResetDialog(item.targetFullOidStr)
            }
        },
        details@{
            val sb = StringBuilder()
            selectedItemList.value.forEach {
                sb.append(appContext.getString(R.string.name)).append(": ").append(it.shortName).appendLine().appendLine()
                sb.append(appContext.getString(R.string.full_name)).append(": ").append(it.name).appendLine().appendLine()
                sb.append(appContext.getString(R.string.target)).append(": ").append(it.targetFullOidStr).appendLine().appendLine()
                sb.append(appContext.getString(R.string.type)).append(": ").append(it.getType()).appendLine().appendLine()
                if(it.isAnnotated) {
                    sb.append(appContext.getString(R.string.tag_oid)).append(": ").append(it.fullOidStr).appendLine().appendLine()
                    sb.append(appContext.getString(R.string.author)).append(": ").append(it.getFormattedTaggerNameAndEmail()).appendLine().appendLine()
                    sb.append(appContext.getString(R.string.date)).append(": ").append(it.getFormattedDate()).appendLine().appendLine()
                    sb.append(appContext.getString(R.string.msg)).append(": ").append(it.msg).appendLine().appendLine()
                }

                sb.append("------------------------------").appendLine().appendLine()
            }

            detailsString.value = sb.toString()

            showDetailsDialog.value = true
        },
    )

    val filterKeyword = StateUtil.getCustomSaveableState(
        keyTag = stateKeyTag,
        keyName = "filterKeyword",
        initValue = TextFieldValue("")
    )
    val filterModeOn = StateUtil.getRememberSaveableState(initValue = false)


    val showTagFetchPushDialog = StateUtil.getRememberSaveableState(initValue = false)
    val showForce = StateUtil.getRememberSaveableState(initValue = false)
    val remoteList = StateUtil.getCustomSaveableStateList(
        keyTag = stateKeyTag,
        keyName = "remoteList"
    ) {
        listOf<String>()
    }
    val selectedRemoteList = StateUtil.getCustomSaveableStateList(
        keyTag = stateKeyTag,
        keyName = "selectedRemoteList"
    ) {
        listOf<String>()
    }

    val remoteCheckedList = StateUtil.getCustomSaveableStateList(
        keyTag = stateKeyTag,
        keyName = "remoteCheckedList"
    ) {
        listOf<Boolean>()
    }

    val fetchPushDialogTitle = StateUtil.getRememberSaveableState(initValue = "")

    val trueFetchFalsePush = StateUtil.getRememberSaveableState(initValue = true)
    val requireDel = StateUtil.getRememberSaveableState(initValue = false)
    val requireDelRemoteChecked = StateUtil.getRememberSaveableState(initValue = false)

    val loadingTextForFetchPushDialog = StateUtil.getRememberSaveableState(initValue = "")

    if(showTagFetchPushDialog.value) {
        TagFetchPushDialog(
            title = fetchPushDialogTitle.value,
            remoteList = remoteList.value,
            selectedRemoteList = selectedRemoteList.value,
            remoteCheckedList = remoteCheckedList.value,
            enableOk = if(requireDel.value) true else selectedRemoteList.value.isNotEmpty(),   //如果是删除模式，可能只删本地也可能删本地和远程，而显示此弹窗有必须至少选中一个条目的前置判断，所以执行到这里一律启用ok即可；如果是fetch/push，则必须至少选一个remote，否则禁用ok
            showForce = showForce.value,
            requireDel = requireDel.value,
            requireDelRemoteChecked = requireDelRemoteChecked,
            trueFetchFalsePush = trueFetchFalsePush.value,
            showTagFetchPushDialog=showTagFetchPushDialog,
            loadingOn=loadingOn,
            loadingOff=loadingOff,
            loadingTextForFetchPushDialog=loadingTextForFetchPushDialog,
            curRepo=curRepo.value,
            selectedTagsList=selectedItemList.value,
            allTagsList= list.value,
            onCancel = { showTagFetchPushDialog.value=false },
            onSuccess = {
                Msg.requireShow(appContext.getString(R.string.success))
            },
            onErr = { e->
                val errMsgPrefix = "${fetchPushDialogTitle.value} err: "
                Msg.requireShowLongDuration(e.localizedMessage ?: errMsgPrefix)
                createAndInsertError(curRepo.value.id, errMsgPrefix + e.localizedMessage)
                MyLog.e(TAG, "#TagFetchPushDialog onOK error when '${fetchPushDialogTitle.value}': ${e.stackTraceToString()}")
            },
            onFinally = {
                changeStateTriggerRefreshPage(needRefresh)
            }
        )
    }

    val initDelTagDialog = {
        requireDel.value = true
        requireDelRemoteChecked.value = false  //默认不要勾选同时删除远程分支，要不然容易误删
        trueFetchFalsePush.value = false
        fetchPushDialogTitle.value = appContext.getString(R.string.delete_tags)
        showForce.value = false

        loadingTextForFetchPushDialog.value = appContext.getString(R.string.deleting)

        showTagFetchPushDialog.value = true

    }

    val initPushTagDialog= {
        requireDel.value = false
        trueFetchFalsePush.value = false
        fetchPushDialogTitle.value = appContext.getString(R.string.push_tags)
        showForce.value = true

        loadingTextForFetchPushDialog.value = appContext.getString(R.string.pushing)

        showTagFetchPushDialog.value = true
    }

    val initFetchTagDialog = {
        requireDel.value = false
        trueFetchFalsePush.value = true
        fetchPushDialogTitle.value = appContext.getString(R.string.fetch_tags)
        showForce.value = true

        loadingTextForFetchPushDialog.value = appContext.getString(R.string.fetching)

        showTagFetchPushDialog.value = true

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

    val iconOnClickList:List<()->Unit> = listOf(  //index页面的底栏选项
        delete@{
            initDelTagDialog()
        },

        push@{
            initPushTagDialog()
        },
        selectAll@{
            //impl select all
            val list = if(enableFilterState.value) filterList.value else list.value

            list.forEach {
                UIHelper.selectIfNotInSelectedListElseNoop(it, selectedItemList.value)
            }
        },
    )


    val showSelectedItemsShortDetailsDialog = StateUtil.getRememberSaveableState { false }
    val selectedItemsShortDetailsStr = StateUtil.getRememberSaveableState("")
    if(showSelectedItemsShortDetailsDialog.value) {
        CopyableDialog(
            title = stringResource(id = R.string.selected_str),
            text = selectedItemsShortDetailsStr.value,
            onCancel = { showSelectedItemsShortDetailsDialog.value = false }
        ) {
            showSelectedItemsShortDetailsDialog.value = false
            clipboardManager.setText(AnnotatedString(selectedItemsShortDetailsStr.value))
            Msg.requireShow(appContext.getString(R.string.copied))
        }
    }

    val countNumOnClickForBottomBar = {
        val list = selectedItemList.value.toList()
        val sb = StringBuilder()
        list.toList().forEach {
            sb.appendLine(it.shortName).appendLine()
        }
        selectedItemsShortDetailsStr.value = sb.removeSuffix("\n").toString()
        showSelectedItemsShortDetailsDialog.value = true
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
                            Row(modifier = Modifier.horizontalScroll(StateUtil.getRememberScrollState())) {
                                Text(
                                    text= stringResource(R.string.tags),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                            Row(modifier = Modifier.horizontalScroll(StateUtil.getRememberScrollState())) {
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
                            tooltipText = stringResource(R.string.fetch_tags),
                            icon =  Icons.Filled.Download,
                            iconContentDesc = stringResource(R.string.fetch_tags),
                        ) {
                            initFetchTagDialog()
                        }

                        LongPressAbleIconBtn(
                            tooltipText = stringResource(R.string.create_tag),
                            icon =  Icons.Filled.Add,
                            iconContentDesc = stringResource(R.string.create_tag),
                        ) {
                            val hash = ""
                            initNewTagDialog(hash)
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


        if(list.value.isEmpty()) {  //无条目，显示可创建或fetch
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(contentPadding)
//                        .padding(bottom = 80.dp)  //不要在这加padding，如果想加，应在底部加个padding row
                    .verticalScroll(StateUtil.getRememberScrollState())
                ,
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,

            ) {
                Row(
                    modifier = Modifier
                        .padding(top = 10.dp)
                    ,
                ) {
                    Text(
                        text = stringResource(R.string.no_tags_found),
                    )
                }
                Row(
                    modifier = Modifier
                        .padding(top = 10.dp)
                    ,
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(R.string.fetch),
                        color = MyStyleKt.ClickableText.color,
                        style = MyStyleKt.ClickableText.style,
                        modifier = MyStyleKt.ClickableText.modifierNoPadding
                            .clickable {
                                initFetchTagDialog()
                            },
                    )
                    Text(
                        text =  " "+stringResource(R.string.or_str)+" ",
                    )
                    Text(
                        text =  stringResource(R.string.create),
                        color = MyStyleKt.ClickableText.color,
                        style = MyStyleKt.ClickableText.style,
                        modifier = MyStyleKt.ClickableText.modifierNoPadding
                            .clickable {
                                val hash = ""
                                initNewTagDialog(hash)
                            }
                        ,
                    )
                }
            }

        }else {  //有条目
            //根据关键字过滤条目
            val k = filterKeyword.value.text.lowercase()  //关键字
            val enableFilter = filterModeOn.value && k.isNotEmpty()
            val list = if(enableFilter){
                val fl = list.value.filter {
                    it.shortName.lowercase().contains(k)
                            || it.name.lowercase().contains(k)
                            || it.msg.lowercase().contains(k)
                            || it.targetFullOidStr.lowercase().contains(k)
                            || it.taggerName.lowercase().contains(k)
                            || it.taggerEmail.lowercase().contains(k)
                            || it.fullOidStr.lowercase().contains(k)  // annotated tag对象的oid；非annotated tag此值和targetFullOidStr一样
                            || it.getType().lowercase().contains(k)
                }
                filterList.value.clear()
                filterList.value.addAll(fl)
                fl
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
                requirePaddingAtBottom = true,
                forEachCb = {},
            ){idx, it->
                //长按会更新curObjInPage为被长按的条目
                TagItem(it, isItemInSelected, onLongClick = {
                    if(multiSelectionMode.value) {  //多选模式
                        //在选择模式下长按条目，执行区域选择（连续选择一个范围）
                        UIHelper.doSelectSpan(idx, it,
                            selectedItemList.value, list,
                            switchItemSelected,
                            selectItem
                        )
                    }else {  //非多选模式
                        //启动多选模式
                        switchItemSelected(it)
                    }
                }
                ) {  //onClick
                    if(multiSelectionMode.value) {  //选择模式
                        UIHelper.selectIfNotInSelectedListElseRemove(it, selectedItemList.value)
                    }else {  //非选择模式
                        //点击条目跳转到分支的提交历史记录页面
                        val fullOidKey:String = Cache.setThenReturnKey(it.targetFullOidStr)
                        val shortBranchNameKey:String = Cache.setThenReturnKey(it.shortName)  //actually is "tag name" at here
                        val useFullOid = "1"
                        val isCurrent = "0"
                        navController.navigate(Cons.nav_CommitListScreen + "/" + repoId +"/" +useFullOid + "/" + fullOidKey +"/" +shortBranchNameKey +"/" +isCurrent)
                    }
                }

                HorizontalDivider()
            }

            if (multiSelectionMode.value) {
                BottomBar(
                    quitSelectionMode=quitSelectionMode,
                    iconList=iconList,
                    iconTextList=iconTextList,
                    iconDescTextList=iconTextList,
                    iconOnClickList=iconOnClickList,
                    iconEnableList=iconEnableList,
                    enableMoreIcon=true,
                    moreItemTextList=moreItemTextList,
                    moreItemOnClickList=moreItemOnClickList,
                    getSelectedFilesCount = getSelectedFilesCount,
                    moreItemEnableList = moreItemEnableList,
                    countNumOnClickEnabled = true,
                    countNumOnClick = countNumOnClickForBottomBar
                )
            }
        }

    }

    BackHandler {
        if(filterModeOn.value) {
          filterModeOn.value = false
        } else if(multiSelectionMode.value) {
            quitSelectionMode()
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
                selectedItemList.value.clear()  //清下已选中条目列表
                list.value.clear()  //先清一下list，然后可能添加也可能不添加

                if(!repoId.isNullOrBlank()) {
                    val repoDb = AppModel.singleInstanceHolder.dbContainer.repoRepository
                    val repoFromDb = repoDb.getById(repoId)
                    if(repoFromDb!=null) {
                        curRepo.value = repoFromDb
                        Repository.open(repoFromDb.fullSavePath).use {repo ->
                            val tags = Libgit2Helper.getAllTags(repo);
                            list.value.clear()
                            list.value.addAll(tags)

                            //查询remotes，fetch/push/del用
                            val remotes = Libgit2Helper.getRemoteList(repo)
                            selectedRemoteList.value.clear()
                            remoteCheckedList.value.clear()
                            remoteList.value.clear()


                            //test, start
//                            remotes = remotes.toMutableList()
//                            for(i in 1..50) {
//                                remotes.add(remotes[0]+i)
//                            }
                            //test, end

                            remotes.forEach { remoteCheckedList.value.add(false) }  //有几个remote就创建几个Boolean
                            remoteList.value.addAll(remotes)
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
