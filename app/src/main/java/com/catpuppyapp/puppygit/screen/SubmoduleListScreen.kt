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
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DownloadForOffline
import androidx.compose.material.icons.filled.FilterAlt
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.SelectAll
import androidx.compose.material.icons.filled.Update
import androidx.compose.material.icons.filled.VerticalAlignTop
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
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
import com.catpuppyapp.puppygit.compose.ConfirmDialog
import com.catpuppyapp.puppygit.compose.CopyableDialog
import com.catpuppyapp.puppygit.compose.FilterTextField
import com.catpuppyapp.puppygit.compose.LoadingDialog
import com.catpuppyapp.puppygit.compose.LongPressAbleIconBtn
import com.catpuppyapp.puppygit.compose.MyCheckBox
import com.catpuppyapp.puppygit.compose.MyLazyColumn
import com.catpuppyapp.puppygit.compose.ScrollableColumn
import com.catpuppyapp.puppygit.compose.SmallFab
import com.catpuppyapp.puppygit.compose.SubmoduleItem
import com.catpuppyapp.puppygit.data.entity.RepoEntity
import com.catpuppyapp.puppygit.git.CredentialStrategy
import com.catpuppyapp.puppygit.git.SubmoduleDto
import com.catpuppyapp.puppygit.play.pro.R
import com.catpuppyapp.puppygit.style.MyStyleKt
import com.catpuppyapp.puppygit.ui.theme.Theme
import com.catpuppyapp.puppygit.utils.AppModel
import com.catpuppyapp.puppygit.utils.Libgit2Helper
import com.catpuppyapp.puppygit.utils.Msg
import com.catpuppyapp.puppygit.utils.MyLog
import com.catpuppyapp.puppygit.utils.UIHelper
import com.catpuppyapp.puppygit.utils.changeStateTriggerRefreshPage
import com.catpuppyapp.puppygit.utils.createAndInsertError
import com.catpuppyapp.puppygit.utils.doActIfIndexGood
import com.catpuppyapp.puppygit.utils.doJobThenOffLoading
import com.catpuppyapp.puppygit.utils.state.StateUtil
import com.github.git24j.core.Repository

private val TAG = "SubmoduleListScreen"
private val stateKeyTag = "SubmoduleListScreen"

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun SubmoduleListScreen(
    repoId:String,
    naviUp: () -> Boolean,
) {
    val homeTopBarScrollBehavior = AppModel.singleInstanceHolder.homeTopBarScrollBehavior
    val navController = AppModel.singleInstanceHolder.navController
    val appContext = AppModel.singleInstanceHolder.appContext
    val haptic = LocalHapticFeedback.current
    val scope = rememberCoroutineScope()

    val inDarkTheme = Theme.inDarkTheme

    //获取假数据
    val list = StateUtil.getCustomSaveableStateList(keyTag = stateKeyTag, keyName = "list", initValue = listOf<SubmoduleDto>())

    val filterList = StateUtil.getCustomSaveableStateList(keyTag = stateKeyTag, keyName = "filterList", initValue = listOf<SubmoduleDto>())

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



    val showCreateDialog = StateUtil.getRememberSaveableState(initValue = false)
    val remoteUrlForCreate = StateUtil.getRememberSaveableState(initValue = "")
    val pathForCreate = StateUtil.getRememberSaveableState(initValue = "")
    val initCreateDialog = {
        showCreateDialog.value = true
    }

    if(showCreateDialog.value) {
        ConfirmDialog(
            title = stringResource(R.string.create),
            requireShowTextCompose = true,
            textCompose = {
                ScrollableColumn{
                    //remoteUrl
                    TextField(
                        modifier = Modifier.fillMaxWidth(),

                        value = remoteUrlForCreate.value,
                        singleLine = true,
                        onValueChange = {
                            remoteUrlForCreate.value = it
                        },
                        label = {
                            Text(stringResource(R.string.url))
                        },
                    )

                    Spacer(Modifier.height(10.dp))

                    //path
                    TextField(
                        modifier = Modifier.fillMaxWidth(),

                        value = pathForCreate.value,
                        singleLine = true,
                        onValueChange = {
                            pathForCreate.value = it
                        },
                        label = {
                            Text(stringResource(R.string.path))
                        },
                    )
                }
            },

            onCancel = {showCreateDialog.value = false}
        ) {
            showCreateDialog.value = false
            doJobThenOffLoading(loadingOn, loadingOff, appContext.getString(R.string.creating)) {
                try {
                    Repository.open(curRepo.value.fullSavePath).use { repo->
                        Libgit2Helper.addSubmodule(
                            repo = repo,
                            remoteUrl = remoteUrlForCreate.value,
                            relativePathUnderParentRepo = pathForCreate.value
                        )
                    }

                    Msg.requireShow(appContext.getString(R.string.success))
                }catch (e:Exception) {
                    val errPrefix = "create submodule '${pathForCreate.value}' err: "
                    val errMsg = e.localizedMessage
                    Msg.requireShowLongDuration(errMsg ?: errPrefix)
                    createAndInsertError(curRepo.value.id, "$errPrefix$errMsg")
                    MyLog.e(TAG, "#CreateDialog err: path=$pathForCreate, url=$remoteUrlForCreate, err=${e.stackTraceToString()}")
                }finally {
                    changeStateTriggerRefreshPage(needRefresh)
                }
            }
        }
    }

    // BottomBar相关变量，开始
    val multiSelectionMode = StateUtil.getRememberSaveableState(initValue = false)
    val selectedItemList = StateUtil.getCustomSaveableStateList(keyTag = stateKeyTag, keyName = "selectedItemList") { listOf<SubmoduleDto>() }
    val quitSelectionMode = {
        selectedItemList.value.clear()  //清空选中文件列表
        multiSelectionMode.value=false  //关闭选择模式
    }
    val iconList:List<ImageVector> = listOf(
        Icons.Filled.Delete,  //删除
        Icons.Filled.DownloadForOffline,  //clone
        Icons.Filled.Update,  //update
        Icons.Filled.SelectAll,  //全选
    )
    val iconTextList:List<String> = listOf(
        stringResource(id = R.string.delete),
        stringResource(id = R.string.clone),
        stringResource(id = R.string.update),
        stringResource(id = R.string.select_all),
    )
    val iconEnableList:List<()->Boolean> = listOf(
        {selectedItemList.value.isNotEmpty()},  // delete
        {selectedItemList.value.isNotEmpty()},  // clone
        {selectedItemList.value.isNotEmpty()},  // update
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
    val switchItemSelected = { item: SubmoduleDto ->
        //如果元素不在已选择条目列表则添加
        UIHelper.selectIfNotInSelectedListElseRemove(item, selectedItemList.value)
        //开启选择模式
        multiSelectionMode.value = true
    }

    val selectItem = { item:SubmoduleDto ->
        UIHelper.selectIfNotInSelectedListElseNoop(item, selectedItemList.value)
    }

    val isItemInSelected= { item:SubmoduleDto ->
        selectedItemList.value.contains(item)
    }
    // 多选模式相关函数，结束


    val getDetail = { item:SubmoduleDto ->
        val sb = StringBuilder()
        sb.appendLine(appContext.getString(R.string.name)+": "+item.name).appendLine()
            .appendLine(appContext.getString(R.string.url)+": "+item.remoteUrl).appendLine()
            .appendLine(appContext.getString(R.string.path_under_repo)+": "+item.relativePathUnderParent).appendLine()
            .appendLine(appContext.getString(R.string.path)+": "+item.fullPath).appendLine()
            .appendLine(appContext.getString(R.string.status)+": "+item.getStatus())

        sb.toString()

    }

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
        importToRepos@{
        },
        restoreDotGitFile@{
            // 提示用户，将尝试恢复submodule的.git文件，当克隆仓库成功但update失败时，可能有帮助

        },
        copyFullPath@{
            // copy full path of submodule
        },
        details@{
            val sb = StringBuilder()
            selectedItemList.value.forEach {
                sb.appendLine(getDetail(it))
                sb.appendLine("------------------------------").appendLine()
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

    val recursiveClone = StateUtil.getRememberSaveableState(initValue = false)
    val showCloneDialog = StateUtil.getRememberSaveableState(initValue = false)

    val initDelTagDialog = {
        requireDel.value = true
        requireDelRemoteChecked.value = false  //默认不要勾选同时删除远程分支，要不然容易误删
        trueFetchFalsePush.value = false
        fetchPushDialogTitle.value = appContext.getString(R.string.delete_tags)
        showForce.value = false

        loadingTextForFetchPushDialog.value = appContext.getString(R.string.deleting)

        showTagFetchPushDialog.value = true

    }

    val initCloneDialog= {
        recursiveClone.value = false
        showCloneDialog.value = true
    }

    val initFetchTagDialog = {
        requireDel.value = false
        trueFetchFalsePush.value = true
        fetchPushDialogTitle.value = appContext.getString(R.string.fetch_tags)
        showForce.value = true

        loadingTextForFetchPushDialog.value = appContext.getString(R.string.fetching)

        showTagFetchPushDialog.value = true

    }

    if(showCloneDialog.value) {
        ConfirmDialog(title = appContext.getString(R.string.clone),
            requireShowTextCompose = true,
            textCompose = {
                Column(modifier = Modifier.verticalScroll(StateUtil.getRememberScrollState())
                ) {
                    Text(stringResource(R.string.will_clone_selected_submodules_are_you_sure))

                    Spacer(Modifier.height(10.dp))

                    MyCheckBox(text = stringResource(R.string.recursive), value = recursiveClone)
                    if(recursiveClone.value) {
                        Text(stringResource(R.string.recursive_clone_submodule_nested_loop_warn), color = MyStyleKt.TextColor.danger)
                    }
                }
            },
            onCancel = {showCloneDialog.value = false}

        ) {
            showCloneDialog.value=false

            doJobThenOffLoading {
                val recursive = recursiveClone.value
                val selectedList = selectedItemList.value.toList()
                val allItems = list.value
                val cloningStr = appContext.getString(R.string.cloning)
                val credentialDb = AppModel.singleInstanceHolder.dbContainer.credentialRepository

                val defaultInvalidIdx = -1

                //not cloned, and not do other job for submodule
                val willCloneList = selectedList.filter{!it.cloned && it.tempStatus.isBlank()}

                val nameIndexMap = mutableMapOf<String, Int>()
                // set status to cloning for selected items
                willCloneList.forEach { selectedItem ->
                    val curItemIdx = allItems.indexOfFirst { selectedItem.name == it.name }
                    doActIfIndexGood(curItemIdx, allItems) { itemWillUpdate ->
                        allItems[curItemIdx] = itemWillUpdate.copy(tempStatus = cloningStr)
                        nameIndexMap.put(selectedItem.name, curItemIdx)
                    }
                }

                Repository.open(curRepo.value.fullSavePath).use { repo->
                    selectedList.forEach { selectedItem ->
                        val resultMsg = try {
                            //test
                            val credential = credentialDb.getByIdWithDecrypt("ffffffffffffffffffffffffffffffffffff")
                            //test

                            // clone submodule
                            Libgit2Helper.cloneSubmodules(repo, recursive, specifiedCredential=credential, credentialStrategy= CredentialStrategy.SPECIFIED, submoduleNameList= listOf(selectedItem.name))


                            // all ok, return empty str
                            ""
                        }catch (e:Exception) {
                            e.localizedMessage ?: "clone err"
                        }

                        // clear status or set err to status
                        val curItemIdx = nameIndexMap.getOrDefault(selectedItem.name, defaultInvalidIdx)
                        doActIfIndexGood(curItemIdx, allItems) { itemWillUpdate ->
                            allItems[curItemIdx] = itemWillUpdate.copy(tempStatus = resultMsg, cloned = Libgit2Helper.isValidGitRepo(itemWillUpdate.fullPath))
                        }

                    }
                }


            }

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

    val iconOnClickList:List<()->Unit> = listOf(  //index页面的底栏选项
        delete@{
            initDelTagDialog()
        },

        clone@{
            initCloneDialog()
        },
        update@{
            //TODO 为待更新的条目设置临时状态
            doJobThenOffLoading {
                val credentialDb = AppModel.singleInstanceHolder.dbContainer.credentialRepository
                val credential = credentialDb.getByIdWithDecrypt("ffffffffffffffffff")
                Repository.open(curRepo.value.fullSavePath).use { repo->
                    selectedItemList.value.toList().forEach {
                        Libgit2Helper.updateSubmodule(repo,credential, CredentialStrategy.SPECIFIED, it.name)
                        Msg.requireShow(appContext.getString(R.string.success))

                    }
                }

            }
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
            sb.appendLine(it.name).appendLine()
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
                                    text= stringResource(R.string.submodules),
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
                            tooltipText = stringResource(R.string.create),
                            icon =  Icons.Filled.Add,
                            iconContentDesc = stringResource(R.string.create),
                        ) {
                            initCreateDialog()
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
                        text = stringResource(R.string.no_submodules_found),
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
                        text =  stringResource(R.string.create),
                        color = MyStyleKt.ClickableText.color,
                        style = MyStyleKt.ClickableText.style,
                        modifier = MyStyleKt.ClickableText.modifierNoPadding
                            .clickable {
                                initCreateDialog()
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
                    it.name.lowercase().contains(k)
                            || it.remoteUrl.contains(k)
                            || it.getStatus().lowercase().contains(k)
                            || it.fullPath.lowercase().contains(k)
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
                SubmoduleItem(it, isItemInSelected, onLongClick = {
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
                    }else {  //非多选模式，点击显示详情
                        detailsString.value = getDetail(it)
                        showDetailsDialog.value = true
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
                            val items = Libgit2Helper.getSubmoduleDtoList(repo);
                            list.value.clear()
                            list.value.addAll(items)
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
