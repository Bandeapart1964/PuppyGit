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
import androidx.compose.material.icons.filled.ReplayCircleFilled
import androidx.compose.material.icons.filled.SelectAll
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.catpuppyapp.puppygit.compose.BottomBar
import com.catpuppyapp.puppygit.compose.ConfirmDialog2
import com.catpuppyapp.puppygit.compose.CopyableDialog
import com.catpuppyapp.puppygit.compose.FilterTextField
import com.catpuppyapp.puppygit.compose.LoadingDialog
import com.catpuppyapp.puppygit.compose.LongPressAbleIconBtn
import com.catpuppyapp.puppygit.compose.MyCheckBox
import com.catpuppyapp.puppygit.compose.MyLazyColumn
import com.catpuppyapp.puppygit.compose.ScrollableColumn
import com.catpuppyapp.puppygit.compose.SingleSelectList
import com.catpuppyapp.puppygit.compose.SmallFab
import com.catpuppyapp.puppygit.compose.SubmoduleItem
import com.catpuppyapp.puppygit.data.entity.CredentialEntity
import com.catpuppyapp.puppygit.data.entity.RepoEntity
import com.catpuppyapp.puppygit.git.ImportRepoResult
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
import com.catpuppyapp.puppygit.utils.doJobThenOffLoading
import com.catpuppyapp.puppygit.utils.replaceStringResList
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


    val credentialList = StateUtil.getCustomSaveableStateList(stateKeyTag, "credentialList") { listOf<CredentialEntity>() }
    val selectedCredentialIdx = StateUtil.getRememberSaveableIntState(0)


    val showCreateDialog = StateUtil.getRememberSaveableState(initValue = false)
    val remoteUrlForCreate = StateUtil.getRememberSaveableState(initValue = "")
    val pathForCreate = StateUtil.getRememberSaveableState(initValue = "")
    val initCreateDialog = {
        showCreateDialog.value = true
    }

    if(showCreateDialog.value) {
        ConfirmDialog2(
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
            okBtnEnabled = pathForCreate.value.isNotBlank() &&remoteUrlForCreate.value.isNotBlank(),
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
                    MyLog.e(TAG, "#CreateDialog err: path=${pathForCreate.value}, url=${remoteUrlForCreate.value}, err=${e.stackTraceToString()}")
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
        Icons.Filled.ReplayCircleFilled,  //do `git submodule update`, actually is checkout submodule to parent's recorded commit
        Icons.Filled.SelectAll,  //全选
    )
    val iconTextList:List<String> = listOf(
        stringResource(id = R.string.delete),
        stringResource(id = R.string.clone),
        stringResource(R.string.update),
        stringResource(id = R.string.select_all),
    )
    val iconEnableList:List<()->Boolean> = listOf(
        {selectedItemList.value.isNotEmpty()},  // delete
        {selectedItemList.value.isNotEmpty()},  // clone
        {selectedItemList.value.isNotEmpty()},  // update
        {true} // select all
    )


    val getSelectedFilesCount = {
        selectedItemList.value.size
    }

    // BottomBar相关变量，结束

    val containsForSelectedItems = { srcList:List<SubmoduleDto>, curItem:SubmoduleDto ->
        srcList.indexOfFirst { it.name == curItem.name } != -1
    }

    //多选模式相关函数，开始
    val switchItemSelected = { item: SubmoduleDto ->
        //如果元素不在已选择条目列表则添加
        UIHelper.selectIfNotInSelectedListElseRemove(item, selectedItemList.value, contains = containsForSelectedItems)
        //开启选择模式
        multiSelectionMode.value = true
    }

    val selectItem = { item:SubmoduleDto ->
        UIHelper.selectIfNotInSelectedListElseNoop(item, selectedItemList.value, contains = containsForSelectedItems)
    }

    val isItemInSelected= { item:SubmoduleDto ->
        containsForSelectedItems(selectedItemList.value, item)
    }
    // 多选模式相关函数，结束


    val getDetail = { item:SubmoduleDto ->
        val sb = StringBuilder()
        sb.appendLine(appContext.getString(R.string.name)+": "+item.name).appendLine()
            .appendLine(appContext.getString(R.string.url)+": "+item.remoteUrl).appendLine()
            .appendLine(appContext.getString(R.string.path)+": "+item.relativePathUnderParent).appendLine()
            .appendLine(appContext.getString(R.string.full_path)+": "+item.fullPath).appendLine()
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

    val showSetUrlDialog = StateUtil.getRememberSaveableState(initValue = false)
    val urlForSetUrlDialog = StateUtil.getRememberSaveableState(initValue = "")
    val nameForSetUrlDialog = StateUtil.getRememberSaveableState(initValue = "")
    if(showSetUrlDialog.value) {
        ConfirmDialog2(title = appContext.getString(R.string.set_url),
            requireShowTextCompose = true,
            textCompose = {
                ScrollableColumn {
                    TextField(
                        modifier = Modifier.fillMaxWidth(),

                        value = urlForSetUrlDialog.value,
                        singleLine = true,
                        onValueChange = {
                            urlForSetUrlDialog.value = it
                        },
                        label = {
                            Text(stringResource(R.string.url))
                        },
                    )
                }
            },
            onCancel = {showSetUrlDialog.value = false}

        ) {
            showSetUrlDialog.value = false

            doJobThenOffLoading(loadingOn, loadingOff, appContext.getString(R.string.updating)) act@{
                try {
                    Repository.open(curRepo.value.fullSavePath).use { repo->
                        val sm = Libgit2Helper.resolveSubmodule(repo, nameForSetUrlDialog.value)
                        if(sm==null) {
                            Msg.requireShowLongDuration(appContext.getString(R.string.resolve_submodule_failed))
                            return@act
                        }

                        Libgit2Helper.updateSubmoduleUrl(repo, sm, urlForSetUrlDialog.value)
                    }

                    Msg.requireShow(appContext.getString(R.string.success))

                }catch (e:Exception) {
                    Msg.requireShowLongDuration(e.localizedMessage ?: " err")
                }finally {
                    changeStateTriggerRefreshPage(needRefresh)
                }
            }
        }
    }


    val showSyncConfigDialog = StateUtil.getRememberSaveableState(initValue = false)
    val syncParentConfig = StateUtil.getRememberSaveableState(initValue = false)
    val syncSubmoduleConfig = StateUtil.getRememberSaveableState(initValue = false)
    if(showSyncConfigDialog.value) {
        ConfirmDialog2(title = appContext.getString(R.string.sync_configs),
            requireShowTextCompose = true,
            textCompose = {
                ScrollableColumn {
                    Text(stringResource(R.string.will_sync_info_from_gitmodules_to_selected_configs))
                    Spacer(Modifier.height(15.dp))
                    MyCheckBox(text = stringResource(R.string.parent_config), value = syncParentConfig)
                    MyCheckBox(text = stringResource(R.string.submodule_config), value = syncSubmoduleConfig)
                }
            },
            onCancel = {showSyncConfigDialog.value = false},
            okBtnEnabled = syncParentConfig.value || syncSubmoduleConfig.value

        ) {
            showSyncConfigDialog.value=false

            doJobThenOffLoading(loadingOn, loadingOff, appContext.getString(R.string.updating)) {
                try {
                    Repository.open(curRepo.value.fullSavePath).use { repo->
                        selectedItemList.value.toList().forEach {
                            val sm = Libgit2Helper.openSubmodule(repo, it.name)
                            if(sm!=null) {
                                if(syncParentConfig.value) {
                                    try {
                                        sm.init(true)
                                    }catch (_:Exception) {
                                    }
                                }

                                if(syncSubmoduleConfig.value) {
                                    try {
                                        sm.sync()
                                    }catch (_:Exception) {
                                    }
                                }

                            }
                        }
                    }

                    Msg.requireShow(appContext.getString(R.string.success))
                }catch (e:Exception) {
                    Msg.requireShowLongDuration(e.localizedMessage ?: "err")
                }finally {
                    changeStateTriggerRefreshPage(needRefresh)
                }
            }

        }
    }

    val showInitRepoDialog = StateUtil.getRememberSaveableState(false)
    if(showInitRepoDialog.value) {
        ConfirmDialog2(title = appContext.getString(R.string.init_repo),
            requireShowTextCompose = true,
            textCompose = {
                ScrollableColumn {
                    Text(stringResource(R.string.will_do_init_repo_for_selected_submodules), fontWeight = FontWeight.Light)
                    Spacer(Modifier.height(10.dp))
                    Text(stringResource(R.string.most_time_need_not_do_init_repo_by_yourself))
                }
            },
            onCancel = {showInitRepoDialog.value = false},
        ) {
            showInitRepoDialog.value=false

            doJobThenOffLoading(loadingOn, loadingOff, appContext.getString(R.string.loading)) {
                try {
                    Repository.open(curRepo.value.fullSavePath).use { repo ->
                        val repoWorkDirFullPath = Libgit2Helper.getRepoWorkdirNoEndsWithSlash(repo)

                        selectedItemList.value.toList().forEach {
                            val sm = Libgit2Helper.openSubmodule(repo, it.name)
                            if(sm!=null) {
                                try {
                                    Libgit2Helper.submoduleRepoInit(repoWorkDirFullPath, sm)
                                }catch (_:Exception){

                                }
                            }
                        }
                    }

                    Msg.requireShow(appContext.getString(R.string.success))
                }catch (e:Exception) {
                    Msg.requireShowLongDuration(e.localizedMessage ?: "err")
                }finally {
                    changeStateTriggerRefreshPage(needRefresh)
                }
            }
        }
    }

    val showRestoreDotGitFileDialog = StateUtil.getRememberSaveableState(false)
    if(showRestoreDotGitFileDialog.value) {
        ConfirmDialog2(
            title = appContext.getString(R.string.restore_dot_git_file),
            requireShowTextCompose = true,
            textCompose = {
                ScrollableColumn {
                    Text(stringResource(R.string.will_try_restore_git_file_for_selected_submodules), fontWeight = FontWeight.Light)
                    Spacer(Modifier.height(10.dp))
                    Text(stringResource(R.string.most_time_need_not_restore_dot_git_file_by_yourself))
                }
            },
            onCancel = { showRestoreDotGitFileDialog.value = false },

        ) {
            showRestoreDotGitFileDialog.value = false
            doJobThenOffLoading(loadingOn, loadingOff, appContext.getString(R.string.restoring)) {
                try {
                    Repository.open(curRepo.value.fullSavePath).use { repo ->
                        val repoWorkDirFullPath = Libgit2Helper.getRepoWorkdirNoEndsWithSlash(repo)

                        selectedItemList.value.toList().forEach {
                            try {
                                Libgit2Helper.SubmoduleDotGitFileMan.restoreDotGitFileForSubmodule(repoWorkDirFullPath, it.relativePathUnderParent)
                            }catch (_:Exception){

                            }

                        }
                    }

                    Msg.requireShow(appContext.getString(R.string.success))

                }catch (e:Exception) {
                    Msg.requireShowLongDuration(e.localizedMessage ?:"err")
                }finally {
                    changeStateTriggerRefreshPage(needRefresh)
                }
            }
        }
    }

    val showImportToReposDialog = StateUtil.getRememberSaveableState(false)
    if(showImportToReposDialog.value){
        ConfirmDialog2(
            title = appContext.getString(R.string.import_to_repos),
            requireShowTextCompose = true,
            textCompose = {
                ScrollableColumn {
//                    Text(stringResource(R.string.will_import_selected_submodules_to_repos))

                    Text(stringResource(R.string.select_credential)+": ")

                    SingleSelectList(
                        optionsList = credentialList.value,
                        selectedOptionIndex = selectedCredentialIdx,
                        menuItemFormatter = {it.name}
                    )

                    Spacer(Modifier.height(15.dp))

                }
            },
            onCancel = { showImportToReposDialog.value = false },

        ) {
            showImportToReposDialog.value = false

            doJobThenOffLoading(loadingOn, loadingOff, appContext.getString(R.string.importing)) {
                val repoNameSuffix = "_of_${curRepo.value.repoName}"
                val parentRepoId = curRepo.value.id
                val importList = selectedItemList.value.toList().filter { it.cloned }

                val selectedCredentialId = credentialList.value[selectedCredentialIdx.intValue].id

                val repoDb = AppModel.singleInstanceHolder.dbContainer.repoRepository
                val importRepoResult = ImportRepoResult()

                try {
                    importList.forEach {
                        val result = repoDb.importRepos(dir=it.fullPath, isReposParent=false, repoNameSuffix = repoNameSuffix, parentRepoId = parentRepoId, credentialId = selectedCredentialId)
                        importRepoResult.all += result.all
                        importRepoResult.success += result.success
                        importRepoResult.failed += result.failed
                        importRepoResult.existed += result.existed
                    }

                    Msg.requireShowLongDuration(replaceStringResList(appContext.getString(R.string.n_imported), listOf(""+importRepoResult.success)))
                }catch (e:Exception) {
                    //出错的时候，importRepoResult的计数不一定准，有可能比实际成功和失败的少，不过不可能多
                    val errMsg = e.localizedMessage
                    Msg.requireShowLongDuration(errMsg ?: "import err")
                    createAndInsertError(curRepo.value.id, "import repos err: $errMsg")
                    MyLog.e(TAG, "import repos from SubmoduleListPage err: importRepoResult=$importRepoResult, err="+e.stackTraceToString())
                }finally {
                    // because import doesn't change current page, so need not do anything yet
                }
            }

        }
    }

    val moreItemEnableList:List<()->Boolean> = listOf(
        {selectedItemList.value.isNotEmpty()},  // import repo
        {selectedItemList.value.isNotEmpty()},  // update config
        {selectedItemList.value.isNotEmpty()},  // init repo
        {selectedItemList.value.isNotEmpty()},  // restore .git file
        {selectedItemList.value.size == 1},  // set url
        {selectedItemList.value.size == 1},  // copy full path
        {selectedItemList.value.isNotEmpty()},  // details
    )

    val moreItemTextList = listOf(
        stringResource(R.string.import_to_repos),
        stringResource(R.string.sync_configs),
        stringResource(R.string.init_repo),
        stringResource(R.string.restore_dot_git_file),
        stringResource(R.string.set_url),
        stringResource(R.string.copy_full_path),
        stringResource(R.string.details),  //可针对单个或多个条目查看details，多个时，用分割线分割多个条目的信息
    )

    val moreItemOnClickList:List<()->Unit> = listOf(
        importToRepos@{
            showImportToReposDialog.value = true
        },
        syncConfigs@{  // git submodule init, git submodule sync. this is necessary if user's edit .gitmodules by hand
            syncParentConfig.value = true
            syncSubmoduleConfig.value = true

            showSyncConfigDialog.value = true
        },
        initRepo@{ // libgit2's submodule.repoInit
            showInitRepoDialog.value = true
        },
        restoreDotGitFile@{ // most time will auto backup and restore when need
            showRestoreDotGitFileDialog.value = true
        },
        setUrl@{  // if selected one
            try {
                if(selectedItemList.value.isNotEmpty()) {
                    val curItem = selectedItemList.value[0]
                    urlForSetUrlDialog.value = curItem.remoteUrl
                    nameForSetUrlDialog.value = curItem.name
                    showSetUrlDialog.value = true
                }else {
                    Msg.requireShow(appContext.getString(R.string.no_item_selected))
                }
            }catch (e:Exception){
                Msg.requireShow(e.localizedMessage ?: "err")
            }
        },
        copyFullPath@{  // if selected one
            // copy full path of a submodule
            try {
                if(selectedItemList.value.isNotEmpty()) {
                    clipboardManager.setText(AnnotatedString(selectedItemList.value[0].fullPath))
                    Msg.requireShow(appContext.getString(R.string.success))
                }else {
                    Msg.requireShow(appContext.getString(R.string.no_item_selected))
                }
            }catch (e:Exception){
                Msg.requireShow(e.localizedMessage ?: "err")
            }
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


    val deleteConfigForDeleteDialog =StateUtil.getRememberSaveableState(initValue = false)
    val deleteFilesForDeleteDialog =StateUtil.getRememberSaveableState(initValue = false)
    val showDeleteDialog = StateUtil.getRememberSaveableState(initValue = false)

    val initDelDialog = {
        deleteConfigForDeleteDialog.value = false
        deleteFilesForDeleteDialog.value = false

        showDeleteDialog.value = true
    }

    if(showDeleteDialog.value) {
        ConfirmDialog2(title = appContext.getString(R.string.delete),
            requireShowTextCompose = true,
            textCompose = {
                ScrollableColumn {
                    MyCheckBox(text = stringResource(R.string.del_config), value = deleteConfigForDeleteDialog)
                    if(deleteConfigForDeleteDialog.value) {
                        Text(stringResource(R.string.submodule_del_config_info_note), fontWeight = FontWeight.Light)
                    }

                    Spacer(Modifier.height(15.dp))

                    MyCheckBox(text = stringResource(R.string.del_files), value = deleteFilesForDeleteDialog)
                    if(deleteFilesForDeleteDialog.value) {
                        Text(stringResource(R.string.submodule_del_files_on_disk_note), fontWeight = FontWeight.Light)
                    }
                }
            },
            okBtnEnabled = deleteConfigForDeleteDialog.value || deleteFilesForDeleteDialog.value,
            onCancel = {showDeleteDialog.value = false}

        ) {
            showDeleteDialog.value=false
            doJobThenOffLoading(loadingOn, loadingOff, appContext.getString(R.string.deleting)) {
                try {
                    Repository.open(curRepo.value.fullSavePath).use { repo->
                        val repoWorkDirPath = Libgit2Helper.getRepoWorkdirNoEndsWithSlash(repo)
                        selectedItemList.value.toList().forEach { smdto ->
                            try {

                                Libgit2Helper.removeSubmodule(
                                    deleteFiles = deleteFilesForDeleteDialog.value,
                                    deleteConfigs = deleteConfigForDeleteDialog.value,
                                    repo = repo,
                                    repoWorkDirPath = repoWorkDirPath,
                                    submoduleName = smdto.name,
                                    submoduleFullPath = smdto.fullPath,
                                )

                            }catch (e:Exception) {
                                val errPrefix = "del submodule err: delConfig=${deleteConfigForDeleteDialog.value}, delFiles=${deleteFilesForDeleteDialog.value}, err="
                                val errMsg = e.localizedMessage
                                Msg.requireShowLongDuration(errMsg ?: "err")
                                createAndInsertError(curRepo.value.id, errPrefix+errMsg)

                                MyLog.e(TAG, "#DeleteDialog err: delConfig=${deleteConfigForDeleteDialog.value}, delFiles=${deleteFilesForDeleteDialog.value}, err=${e.stackTraceToString()}")
                            }
                        }

                    }

                    Msg.requireShow(appContext.getString(R.string.success))
                }finally {
                    changeStateTriggerRefreshPage(needRefresh)
                }

            }
        }
    }

    val initCloneDialog= {
        recursiveClone.value = false
        showCloneDialog.value = true
    }

    val showUpdateDialog = StateUtil.getRememberSaveableState(false)
    val initUpdateDialog = {
        showUpdateDialog.value=true
    }

    if(showCloneDialog.value) {
        ConfirmDialog2(title = appContext.getString(R.string.clone),
            requireShowTextCompose = true,
            textCompose = {
                ScrollableColumn {
//                    Text(stringResource(R.string.will_clone_selected_submodules_are_you_sure))


                    Text(stringResource(R.string.select_credential)+": ")

                    SingleSelectList(
                        optionsList = credentialList.value,
                        selectedOptionIndex = selectedCredentialIdx,
                        menuItemFormatter = {it.name}
                    )

                    Spacer(Modifier.height(20.dp))

                    MyCheckBox(text = stringResource(R.string.recursive_clone), value = recursiveClone)
                    if(recursiveClone.value) {
                        Text(stringResource(R.string.recursive_clone_submodule_nested_loop_warn), color = MyStyleKt.TextColor.danger)
                    }
                    Spacer(Modifier.height(10.dp))

                }
            },
            onCancel = {showCloneDialog.value = false}

        ) {
            showCloneDialog.value=false

            doJobThenOffLoading(loadingOn, loadingOff, appContext.getString(R.string.cloning)) {
                try {

                    val recursive = recursiveClone.value
                    val selectedList = selectedItemList.value.toList()
//                val allItems = list.value
//                val cloningStr = appContext.getString(R.string.cloning)

                    val credentialDb = AppModel.singleInstanceHolder.dbContainer.credentialRepository
                    val selectedCredential = credentialList.value[selectedCredentialIdx.intValue]
                    val credential = credentialDb.getByIdWithDecrypt(selectedCredential.id)

//                val defaultInvalidIdx = -1

                    //not cloned, and not do other job for submodule
                    val willCloneList = selectedList.filter{!it.cloned && it.tempStatus.isBlank()}

//                val nameIndexMap = mutableMapOf<String, Int>()
                    // set status to cloning for selected items
//                willCloneList.forEach { selectedItem ->
//                    val curItemIdx = allItems.indexOfFirst { selectedItem.name == it.name }
//                    doActIfIndexGood(curItemIdx, allItems) { itemWillUpdate ->
//                        val newItem = itemWillUpdate.copy(tempStatus = cloningStr)
//                        allItems[curItemIdx] = newItem
//                        nameIndexMap.put(selectedItem.name, curItemIdx)
//
//                        // may cause ConcurrentException
//                        try {
//                            val selectedIdx = selectedItemList.value.indexOfFirst { it.name == selectedItem.name }
//                            if(selectedIdx!=-1) {  //current item selected, update it's info in selectedItemList
//                                selectedItemList.value[selectedIdx] = newItem
//                            }
//                        }catch (_:Exception) {
//
//                        }
//                    }
//                }
                    Repository.open(curRepo.value.fullSavePath).use { repo->
                        willCloneList.forEach { selectedItem ->
                            try {

                                // clone submodule
                                Libgit2Helper.cloneSubmodules(repo, recursive, specifiedCredential=credential, submoduleNameList= listOf(selectedItem.name))

                            }catch (e:Exception) {
                                val errPrefix = "clone '${selectedItem.name}' err: "
                                val errMsg = e.localizedMessage ?: "clone submodule err"
                                Msg.requireShow(errMsg)
                                createAndInsertError(curRepo.value.id, errPrefix+errMsg)
                            }

                            // clear status or set err to status
//                        val curItemIdx = nameIndexMap.getOrDefault(selectedItem.name, defaultInvalidIdx)
//                        doActIfIndexGood(curItemIdx, allItems) { itemWillUpdate ->
//                            val newItem = itemWillUpdate.copy(tempStatus = resultMsg, cloned = Libgit2Helper.isValidGitRepo(itemWillUpdate.fullPath))
//                            allItems[curItemIdx] = newItem
//
//                            try {
//                                // make selectedList info more real-time, but may cause ConcurrentException, most time should be fine
//                                val selectedIdx = selectedItemList.value.indexOfFirst { it.name == selectedItem.name }
//                                if(selectedIdx!=-1) {  //current item selected, update it's info in selectedItemList
//                                    selectedItemList.value[selectedIdx] = newItem
//                                }
//                            }catch (_:Exception) {
//
//                            }
//                        }

                        }
                    }

                    Msg.requireShow(appContext.getString(R.string.success))
                }finally {
                    changeStateTriggerRefreshPage(needRefresh)
                }


            }

        }
    }

    if(showUpdateDialog.value) {
        ConfirmDialog2(title = appContext.getString(R.string.update),
            requireShowTextCompose = true,
            textCompose = {
                ScrollableColumn {
                    Text(stringResource(R.string.select_credential) + ": ")

                    SingleSelectList(
                        optionsList = credentialList.value,
                        selectedOptionIndex = selectedCredentialIdx,
                        menuItemFormatter = { it.name }
                    )
                    Spacer(Modifier.height(15.dp))

                }
            },
            onCancel = { showUpdateDialog.value = false }

        ) {
            showUpdateDialog.value=false

            doJobThenOffLoading(loadingOn, loadingOff, appContext.getString(R.string.updating)) {
                try {
                    val selectedCredential = credentialList.value[selectedCredentialIdx.intValue]

                    val credentialDb = AppModel.singleInstanceHolder.dbContainer.credentialRepository
                    val credential = credentialDb.getByIdWithDecrypt(selectedCredential.id)

                    Repository.open(curRepo.value.fullSavePath).use { repo->
                        selectedItemList.value.toList().forEach {
                            try {
                                Libgit2Helper.updateSubmodule(repo, credential, it.name)

                            }catch (e:Exception) {
                                val errPrefix = "update '${it.name}' err: "
                                val errMsg = e.localizedMessage ?: "update submodule err"
                                Msg.requireShow(errMsg)
                                createAndInsertError(curRepo.value.id, errPrefix+errMsg)
                            }
                        }
                    }

                    Msg.requireShow(appContext.getString(R.string.success))

                }finally {
                    changeStateTriggerRefreshPage(needRefresh)
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
            initDelDialog()
        },

        clone@{
            initCloneDialog()
        },
        update@{
            initUpdateDialog()
        },
        selectAll@{
            //impl select all
            val list = if(enableFilterState.value) filterList.value else list.value

            list.forEach {
                UIHelper.selectIfNotInSelectedListElseNoop(it, selectedItemList.value, contains = containsForSelectedItems)
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
                        UIHelper.doSelectSpan(
                            itemIdxOfItemList = idx,
                            item = it,
                            selectedItems = selectedItemList.value,
                            itemList = list,
                            switchItemSelected = switchItemSelected,
                            selectIfNotInSelectedListElseNoop = selectItem
                        )
                    }else {  //非多选模式
                        //启动多选模式
                        switchItemSelected(it)
                    }
                }
                ) {  //onClick
                    if(multiSelectionMode.value) {  //选择模式
                        UIHelper.selectIfNotInSelectedListElseRemove(it, selectedItemList.value, contains = containsForSelectedItems)
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
                list.value.clear()  //先清一下list，然后可能添加也可能不添加
                credentialList.value.clear()

                if(!repoId.isNullOrBlank()) {
                    val repoDb = AppModel.singleInstanceHolder.dbContainer.repoRepository
                    val repoFromDb = repoDb.getById(repoId)
                    if(repoFromDb!=null) {
                        curRepo.value = repoFromDb
                        Repository.open(repoFromDb.fullSavePath).use {repo ->
                            val items = Libgit2Helper.getSubmoduleDtoList(repo);
                            list.value.addAll(items)
                        }
                    }

                    val credentialDb = AppModel.singleInstanceHolder.dbContainer.credentialRepository
                    val credentialListFromDb = credentialDb.getAll(includeNone = true, includeMatchByDomain = false)
                    if(credentialListFromDb.isNotEmpty()) {
                        credentialList.value.addAll(credentialListFromDb)
                    }

                }

                if(list.value.isEmpty()) {  // clear selected list if list is empty
                    selectedItemList.value.clear()
                }else {  // hold and update selected list items which still in list
                    val listCopy = list.value.toList()
                    val selectedListCopy = selectedItemList.value.toList()
                    selectedListCopy.forEachIndexed { idx, oldSelectedItem ->
                        // if list doesn't contains selected item, remove it from selected list
                        val newItemIdx = listCopy.indexOfFirst { newItem -> oldSelectedItem.name==newItem.name }
                        if(newItemIdx == -1) {  // removed from src list, but still exists in selectedItemList, should remove it
                            selectedItemList.value.removeAt(idx)
                        }else {  // update info of selected and still exists items
                            selectedItemList.value[idx]=listCopy[newItemIdx]
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
