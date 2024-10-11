package com.catpuppyapp.puppygit.screen.content.homescreen.innerpage

import android.annotation.SuppressLint
import android.content.Context
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.ContentCut
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.SelectAll
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableIntState
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.documentfile.provider.DocumentFile
import com.catpuppyapp.puppygit.compose.ApplyPatchDialog
import com.catpuppyapp.puppygit.compose.BottomBar
import com.catpuppyapp.puppygit.compose.ConfirmDialog
import com.catpuppyapp.puppygit.compose.ConfirmDialog2
import com.catpuppyapp.puppygit.compose.CopyableDialog
import com.catpuppyapp.puppygit.compose.CreateFileOrFolderDialog
import com.catpuppyapp.puppygit.compose.FileListItem
import com.catpuppyapp.puppygit.compose.LoadingText
import com.catpuppyapp.puppygit.compose.MyCheckBox
import com.catpuppyapp.puppygit.compose.MyLazyColumn
import com.catpuppyapp.puppygit.compose.MySelectionContainer
import com.catpuppyapp.puppygit.compose.OpenAsDialog
import com.catpuppyapp.puppygit.constants.Cons
import com.catpuppyapp.puppygit.constants.PageRequest
import com.catpuppyapp.puppygit.data.entity.RepoEntity
import com.catpuppyapp.puppygit.dev.applyPatchTestPassed
import com.catpuppyapp.puppygit.dev.importReposFromFilesTestPassed
import com.catpuppyapp.puppygit.dev.initRepoFromFilesPageTestPassed
import com.catpuppyapp.puppygit.dev.proFeatureEnabled
import com.catpuppyapp.puppygit.dto.FileItemDto
import com.catpuppyapp.puppygit.etc.Ret
import com.catpuppyapp.puppygit.git.ImportRepoResult
import com.catpuppyapp.puppygit.play.pro.R
import com.catpuppyapp.puppygit.settings.AppSettings
import com.catpuppyapp.puppygit.settings.SettingsUtil
import com.catpuppyapp.puppygit.style.MyStyleKt
import com.catpuppyapp.puppygit.utils.ActivityUtil
import com.catpuppyapp.puppygit.utils.AppModel
import com.catpuppyapp.puppygit.utils.FsUtils
import com.catpuppyapp.puppygit.utils.Libgit2Helper
import com.catpuppyapp.puppygit.utils.Msg
import com.catpuppyapp.puppygit.utils.MyLog
import com.catpuppyapp.puppygit.utils.UIHelper
import com.catpuppyapp.puppygit.utils.cache.Cache
import com.catpuppyapp.puppygit.utils.changeStateTriggerRefreshPage
import com.catpuppyapp.puppygit.utils.checkFileOrFolderNameAndTryCreateFile
import com.catpuppyapp.puppygit.utils.createAndInsertError
import com.catpuppyapp.puppygit.utils.doJobThenOffLoading
import com.catpuppyapp.puppygit.utils.getFileNameFromCanonicalPath
import com.catpuppyapp.puppygit.utils.getFilePathStrBasedRepoDir
import com.catpuppyapp.puppygit.utils.getFilePathUnderParent
import com.catpuppyapp.puppygit.utils.getHumanReadableSizeStr
import com.catpuppyapp.puppygit.utils.getSecFromTime
import com.catpuppyapp.puppygit.utils.getShortUUID
import com.catpuppyapp.puppygit.utils.getStoragePermission
import com.catpuppyapp.puppygit.utils.isPathExists
import com.catpuppyapp.puppygit.utils.mime.MimeType
import com.catpuppyapp.puppygit.utils.mime.guessFromFileName
import com.catpuppyapp.puppygit.utils.replaceStringResList
import com.catpuppyapp.puppygit.utils.showToast
import com.catpuppyapp.puppygit.utils.state.CustomStateListSaveable
import com.catpuppyapp.puppygit.utils.state.CustomStateSaveable
import com.catpuppyapp.puppygit.utils.state.mutableCustomStateListOf
import com.catpuppyapp.puppygit.utils.state.mutableCustomStateOf
import com.github.git24j.core.Repository
import java.io.File

private val TAG = "FilesInnerPage"
private val stateKeyTag = "FilesInnerPage"

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun FilesInnerPage(
    contentPadding: PaddingValues,
//    filePageListState: LazyListState,
    currentHomeScreen: MutableIntState,
    editorPageShowingFilePath: MutableState<String>,
    editorPageShowingFileIsReady: MutableState<Boolean>,
    needRefreshFilesPage: MutableState<String>,
    currentPath: MutableState<String>,
    showCreateFileOrFolderDialog: MutableState<Boolean>,
    requireImportFile: MutableState<Boolean>,
    requireImportUriList: CustomStateListSaveable<Uri>,
    filesPageGetFilterMode:()->Int,
    filesPageFilterKeyword:CustomStateSaveable<TextFieldValue>,
    filesPageFilterModeOff:()->Unit,
    currentPathFileList:CustomStateListSaveable<FileItemDto>,
    filesPageRequestFromParent:MutableState<String>,
    requireInnerEditorOpenFile:(filePath:String, expectReadOnly:Boolean)->Unit,
    filesPageSimpleFilterOn:MutableState<Boolean>,
    filesPageSimpleFilterKeyWord:CustomStateSaveable<TextFieldValue>,
    filesPageScrollingDown:MutableState<Boolean>,
    curListState:CustomStateSaveable<LazyListState>,
    filterListState:CustomStateSaveable<LazyListState>,

    openDrawer:()->Unit,
    isFileSelectionMode:MutableState<Boolean>,
    isPasteMode:MutableState<Boolean>,
    selectedItems:CustomStateListSaveable<FileItemDto>,
    checkOnly:MutableState<Boolean>,
    selectedRepo:CustomStateSaveable<RepoEntity>,

    goToRepoPage:(targetIdIfHave:String)->Unit,
    goToChangeListPage:(repoWillShowInChangeListPage:RepoEntity)->Unit,
) {
    val allRepoParentDir = AppModel.singleInstanceHolder.allRepoParentDir;
//    val appContext = AppModel.singleInstanceHolder.appContext;
    val appContext = LocalContext.current;
    val exitApp = AppModel.singleInstanceHolder.exitApp
    val haptic = AppModel.singleInstanceHolder.haptic

    val scope = rememberCoroutineScope()
    val activity = ActivityUtil.getCurrentActivity()


    val settingsSnapshot = mutableCustomStateOf(keyTag = stateKeyTag, keyName = "settingsSnapshot", initValue = SettingsUtil.getSettingsSnapshot())



    //文件管理器三个点的菜单项
//    val menuKeyRename = "rename"
//    val menuValueRename = stringResource(R.string.rename)
//    val menuKeyInfo = "info"
//    val menuValueInfo = stringResource(R.string.info)
//    val fileMenuMap = remember{ mutableMapOf<String, String>(
//        menuKeyRename to menuValueRename,
//        menuKeyInfo to menuValueInfo,
//    ) }
//    val filesPageSelectionBarHeight = 60.dp
//    val filesPageSelectionBarBackgroundColor = MaterialTheme.colorScheme.primaryContainer

    val filterList = mutableCustomStateListOf(keyTag = stateKeyTag, keyName = "filterList", listOf<FileItemDto>())

//    val selFilePathListJsonObjStr = rememberSaveable{ mutableStateOf("{}") }  //key是文件名，所以这个列表只能存储相同目录下的文件，不同目录有可能名称冲突，但由于选择模式只能在当前目录选择，所以这个缺陷可以接受。json格式:{fileName:canonicalPath}
//    val opType = remember{ mutableStateOf("") }
//    val opCodeMove = "mv"
//    val opCodeCopy = "cp"
//    val opCodeDelete = "del"
////    val selFilePathListJsonObj = JSONObject()  //想着复用同一个对象呢，不过没那个api，看下如果性能还行，就这样吧，不行的话，换别的json库

    val fileAlreadyExistStrRes = stringResource(R.string.file_already_exists)
    val successStrRes = stringResource(R.string.success)
    val errorStrRes = stringResource(R.string.error)

    val repoList = mutableCustomStateListOf(keyTag = stateKeyTag, keyName = "repoList", initValue = listOf<RepoEntity>())


//    val currentPathBreadCrumbList = remember{ mutableStateListOf<FileItemDto>() }
    val currentPathBreadCrumbList = mutableCustomStateListOf(keyTag = stateKeyTag, keyName = "currentPathBreadCrumbList", initValue = listOf<FileItemDto>())

    //实现了点击更新list并且避免并发修改异常，但我发现，只要每次在切换路径后重新生成一下面包屑就行了，虽然代码执行起来可能有点麻烦，效率可能差一点点，但是逻辑更简单而且，总之没必要整这么麻烦，废弃这个方案了
//    val currentPathBreadCrumbList = remember{ mutableIntStateOf(1) }  //为0时刚进页面，初始化，后续为1时读取list1修改list2，为2时读取list2修改list1，避免并发修改异常
//    val currentPathBreadCrumbList1 = remember{ mutableStateListOf<FileItemDto>() }  // key 1
//    val currentPathBreadCrumbList2 = remember{ mutableStateListOf<FileItemDto>() }  // key 2


    val containsForSelected = { srcList:List<FileItemDto>, item:FileItemDto ->
        srcList.indexOfFirst { it.equalsForSelected(item) } != -1
    }

    val filesPageQuitSelectionMode = {
        selectedItems.value.clear()  //清空选中文件列表
//        selectedItems.requireRefreshView()
        isFileSelectionMode.value=false  //关闭选择模式
        isPasteMode.value=false
    }

    val switchItemSelected = { item: FileItemDto ->
        isFileSelectionMode.value = true
        UIHelper.selectIfNotInSelectedListElseRemove(item, selectedItems.value, contains = containsForSelected)
    }

    val selecteItem = {item:FileItemDto ->
        isFileSelectionMode.value = true
        UIHelper.selectIfNotInSelectedListElseNoop(item, selectedItems.value, contains = containsForSelected)
    }

    val getSelectedFilesCount = {
        selectedItems.value.size
    }

    val isItemInSelected = { f:FileItemDto->
//        selectedItems.value.contains(f)
        selectedItems.value.indexOfFirst { it.equalsForSelected(f) } != -1
    }


    val renameFileItemDto = mutableCustomStateOf(
        keyTag = stateKeyTag,
        keyName = "renameFileItemDto",
        initValue = FileItemDto()
    )
    val renameFileName = mutableCustomStateOf(
        keyTag = stateKeyTag,
        keyName = "renameFileName",
        initValue = TextFieldValue("")
    )
    val renameHasErr = rememberSaveable { mutableStateOf(false)}
    val renameErrText = rememberSaveable { mutableStateOf( "")}
    val showRenameDialog = rememberSaveable { mutableStateOf(false)}
    val updateRenameFileName:(TextFieldValue)->Unit = {
        val newVal = it
        val oldVal = renameFileName.value

        //只有当值改变时，才解除输入框报错
        if(oldVal.text != newVal.text) {
            //用户一改名，就取消字段错误设置，允许点击克隆按钮，点击后再次检测，有错再设置为真
            renameHasErr.value = false
        }

        //这个变量必须每次都更新，不能只凭text是否相等来判断是否更新此变量，因为选择了哪些字符、光标在什么位置 等信息也包含在这个TextFieldValue对象里
        renameFileName.value = newVal

    }


    val goToPath = {path:String ->
        currentPath.value = path
        changeStateTriggerRefreshPage(needRefreshFilesPage)
    }

    val showGoToPathDialog = rememberSaveable { mutableStateOf(false)}
    val pathToGo = rememberSaveable { mutableStateOf("")}
    if(showGoToPathDialog.value) {
        ConfirmDialog(
            requireShowTextCompose = true,
            textCompose = {
                Column {
                    TextField(
                        modifier = Modifier.fillMaxWidth(),
                        value = pathToGo.value,
//                        singleLine = true,
                        onValueChange = {
                            pathToGo.value = it
                        },
                        label = {
                            Text(stringResource(R.string.path))
                        },
                    )
                }
            },
            okBtnEnabled = pathToGo.value.isNotBlank(),
            okBtnText = stringResource(id = R.string.ok),
            cancelBtnText = stringResource(id = R.string.cancel),
            title = stringResource(R.string.go_to),
            onCancel = { showGoToPathDialog.value = false }
        ) {
            showGoToPathDialog.value = false

            doJobThenOffLoading {
//                val slash = File.separator
//                val repoBaseDirPath = AppModel.singleInstanceHolder.allRepoParentDir.canonicalPath

//                val finallyPath = if(pathGoTo.value.removePrefix(slash).startsWith(repoBaseDirPath.removePrefix(slash))) { // real path，直接跳转
//                    pathGoTo.value  //如果用户输入真实路径但没以 / 开头，后面会报无效路径，问题不大且合理，不处理
//                }else {  //in app path，需要拼接上路径前缀
//                    // 拼接：repoBaseDirPath/pathGoTo
//                    repoBaseDirPath.removeSuffix(slash)+slash+pathGoTo.value.removePrefix(slash)
//                }

                // handle path to absolute path, btw: internal path must before external path, because internal actually starts with external, if swap order, code block of internal path will ignore ever
                val finallyPath = (if(pathToGo.value.startsWith(FsUtils.internalPathPrefix)) {
                        FsUtils.getInternalStorageRootPathNoEndsWithSeparator()+"/"+FsUtils.removeInternalStoragePrefix(pathToGo.value)
                    }else if(pathToGo.value.startsWith(FsUtils.externalPathPrefix)) {
                        FsUtils.getExternalStorageRootPathNoEndsWithSeparator()+"/"+FsUtils.removeExternalStoragePrefix(pathToGo.value)
                    }else {  // absolute path like "/storage/emulate/0/abc"
                        pathToGo.value
                    }).trim('\n')

                val f = File(finallyPath)
                if(f.canRead()) {
                    goToPath(finallyPath)
                }else { // can't read path: usually by path non-exists or no permission to read
                    Msg.requireShow(appContext.getString(R.string.cant_read_path))
                }

            }
        }
    }



    val showApplyAsPatchDialog = rememberSaveable { mutableStateOf(false)}
    val fileFullPathForApplyAsPatch =  rememberSaveable { mutableStateOf("")}

    if(showApplyAsPatchDialog.value) {
        ApplyPatchDialog(
            showDialog = showApplyAsPatchDialog,
            checkOnly = checkOnly,
            selectedRepo=selectedRepo,
            patchFileFullPath = fileFullPathForApplyAsPatch.value,
            repoList = repoList.value,
            onCancel={showApplyAsPatchDialog.value=false},
            onErrCallback={ e, selectedRepoId->
                val errMsgPrefix = "apply patch err: err="
                Msg.requireShowLongDuration(e.localizedMessage ?: errMsgPrefix)
                createAndInsertError(selectedRepoId, errMsgPrefix + e.localizedMessage)
                MyLog.e(TAG, "#ApplyPatchDialog err: $errMsgPrefix${e.stackTraceToString()}")
            },
            onFinallyCallback={
                showApplyAsPatchDialog.value=false
                changeStateTriggerRefreshPage(needRefreshFilesPage)
            },
            onOkCallback={
                Msg.requireShow(appContext.getString(R.string.success))
            }
        )
    }


    val showOpenAsDialog = rememberSaveable { mutableStateOf( false)}
    val openAsDialogFilePath = rememberSaveable { mutableStateOf( "")}
    val showOpenInEditor = rememberSaveable { mutableStateOf(false)}
    val fileNameForOpenAsDialog = remember{ derivedStateOf { getFileNameFromCanonicalPath(openAsDialogFilePath.value) } }

    if(showOpenAsDialog.value) {
        OpenAsDialog(fileName = fileNameForOpenAsDialog.value, filePath = openAsDialogFilePath.value, showOpenInEditor = showOpenInEditor.value,
            openInEditor = {expectReadOnly:Boolean ->
                requireInnerEditorOpenFile(openAsDialogFilePath.value, expectReadOnly)
            }
        ) {
            showOpenAsDialog.value=false
        }
    }

//    if(needRefreshFilesPage.value) {
//        initFilesPage()
//        needRefreshFilesPage.value=false
//    }
    //如果apply patch测试通过，则启用包含apply as patch的菜单，否则使用不包含此选项的菜单
    //注意：会忽略值为空的选项！可用此特性来对用户隐藏未测试特性
    val fileMenuKeyTextList = listOf(
        stringResource(R.string.open),  //用内部编辑器打开，得加个这个选项，因为如果自动判断文件该用外部程序打开，但又没对应的外部程序，用户又想用内部编辑器打开，但文件管理器又判断文件该用内部文件打开，就死局了，所以得加这个选项
        stringResource(R.string.open_as),
//        stringResource(R.string.edit_with),
//        stringResource(R.string.view_with),
        stringResource(R.string.rename),
        if(proFeatureEnabled(applyPatchTestPassed)) stringResource(R.string.apply_as_patch) else "",  //应用patch，弹窗让用户选仓库，然后可对仓库应用patch
//        这列表如果添加元素最好往头或尾加，别往中间加，不然改关联的数组可能容易改错位置
//        stringResource(R.string.copy_path),
        stringResource(R.string.copy_real_path),

//        stringResource(R.string.export),  //改到批量操作里了
//        stringResource(id = R.string.delete)  //改用长按菜单的删除功能了
    )

    //目录条目菜单没有open with
    val dirMenuKeyTextList = listOf(
        stringResource(R.string.rename),
//        stringResource(R.string.copy_path),
        stringResource(R.string.copy_real_path),
//        stringResource(id = R.string.delete)  //改用长按菜单的删除功能了
    )

    val clipboardManager = LocalClipboardManager.current

    // 复制app内路径到剪贴板，显示提示copied
    val copyPath = {realFullPath:String ->
        clipboardManager.setText(AnnotatedString(getFilePathStrBasedRepoDir(realFullPath, returnResultStartsWithSeparator = true)))
        Msg.requireShow(appContext.getString(R.string.copied))
    }
    // 复制真实路径到剪贴板，显示提示copied
    val copyRealPath = {realFullPath:String ->
        clipboardManager.setText(AnnotatedString(realFullPath))
        Msg.requireShow(appContext.getString(R.string.copied))
    }

    val renameFile = {item:FileItemDto ->
        renameFileItemDto.value = item  // 旧item
        renameFileName.value = TextFieldValue(item.name)  //旧文件名

        renameHasErr.value = false  //初始化为没错误，不然会显示上次报的错，比如“文件名已存在！”
        renameErrText.value = ""  //初始化错误信息为空，理由同上

        showRenameDialog.value = true  // 显示弹窗
    }
    val fileMenuKeyActList = listOf(
        open@{ item:FileItemDto ->
            val expectReadOnly = false
            requireInnerEditorOpenFile(item.fullPath, expectReadOnly)
            Unit
        },

        openAs@{ item:FileItemDto ->
            openAsDialogFilePath.value = item.fullPath
            showOpenInEditor.value=false
            showOpenAsDialog.value=true
            Unit
        },
//        editWith@{ item:FileItemDto ->
//            val isSuccess = FsUtils.openFileAsEditMode(appContext, File(item.fullPath))
//            if(!isSuccess) {
//                Msg.requireShow(appContext.getString(R.string.open_file_edit_mode_err_maybe_try_view))
//                changeStateTriggerRefreshPage(needRefreshFilesPage)
//                //如果后面还有代码，需要在这return
//            }
//            Unit
//        },
//        viewWith@{ item:FileItemDto ->
//            val isSuccess = FsUtils.openFileAsViewMode(appContext, File(item.fullPath))
//            if(!isSuccess) {
//                Msg.requireShow(appContext.getString(R.string.open_file_with_view_mode_err))
//                changeStateTriggerRefreshPage(needRefreshFilesPage)
//                //如果后面还有代码，需要在这return
//            }
//            Unit
//        },
        renameFile ,
        applyAsPatch@{ item:FileItemDto ->
            if(repoList.value.isEmpty()) {
                Msg.requireShowLongDuration(appContext.getString(R.string.repo_list_is_empty))
                return@applyAsPatch
            }

            // if selectedRepo not in list, select first
            if(repoList.value.indexOfFirst { selectedRepo.value.id == it.id } == -1) {
                selectedRepo.value = repoList.value[0]
            }

            fileFullPathForApplyAsPatch.value = item.fullPath
            showApplyAsPatchDialog.value = true
        },
//        copyPath@{
//            copyPath(it.fullPath)
//        },
        copyRealPath@{
            copyRealPath(it.fullPath)
        }
//        export@{ item:FileItemDto ->
//            val ret = FsUtils.getAppDirUnderPublicDocument()
//            if(ret.hasError()) {
//                Msg.requireShow(appContext.getString(R.string.open_public_folder_err))
//                return@export
//            }
//
//            // 测试结果，成功！）这个如果成功，替换成批量复制，把那个函数抽出来，加两个参数，一个目标路径，一个是否移动，就行了
//            val src = File(item.fullPath)
//            val target = File(ret.data!!.canonicalPath, src.name)
//            src.copyTo(target, false)
//            Msg.requireShow(appContext.getString(R.string.success))
//        }
    )
    val dirMenuKeyActList = listOf(
        renameFile,
//        copyPath@{
//            copyPath(it.fullPath)
//        },
        copyRealPath@{
            copyRealPath(it.fullPath)
        }
    )

    val isImportMode = rememberSaveable { mutableStateOf(false)}
    val showImportResultDialog = rememberSaveable { mutableStateOf(false)}
//    val successImportList = rememberSaveable { mutableStateListOf<String>() }
//    val failedImportList = rememberSaveable { mutableStateListOf<String>() }
    val failedImportListStr = rememberSaveable { mutableStateOf("")}
    val successImportCount = rememberSaveable{mutableIntStateOf(0)}
    val failedImportCount = rememberSaveable{mutableIntStateOf(0)}

    val defaultLoadingText = appContext.getString(R.string.loading)
    val isLoading = rememberSaveable { mutableStateOf(false)}
    val loadingText = rememberSaveable { mutableStateOf(defaultLoadingText)}
    val loadingOn = { msg:String ->
        loadingText.value=msg
        isLoading.value=true
    }
    val loadingOff = {
        isLoading.value=false
        loadingText.value=defaultLoadingText
    }

    val openDirErr = rememberSaveable { mutableStateOf("")}

    val getListState:(String)->LazyListState = { path:String ->
        // key有点太长了
        val key = "FilesPageListState:"+path
        val restoreListState = Cache.getByType<LazyListState>(key)
        if(restoreListState==null){
            val newListState = LazyListState(0,0)
            Cache.set(key, newListState)
            newListState
        }else{
            restoreListState
        }
    }
    val breadCrumbListState = rememberLazyListState()

    //back handler block start
    val isBackHandlerEnable = rememberSaveable { mutableStateOf(true)}

    val backHandlerOnBack = getBackHandler(
        appContext,
        isFileSelectionMode,
        filesPageQuitSelectionMode,
        currentPath,
        allRepoParentDir,
        needRefreshFilesPage,
        exitApp,
        filesPageGetFilterMode,
        filesPageFilterModeOff,
        filesPageSimpleFilterOn,
        openDrawer

    )

    //注册BackHandler，拦截返回键，实现双击返回和返回上级目录
    BackHandler(enabled = isBackHandlerEnable.value, onBack = {backHandlerOnBack()})
    //back handler block end



    //导入失败则显示这个对话框，可以复制错误信息
    if(showImportResultDialog.value) {
        CopyableDialog(
            cancelBtnText = stringResource(id = R.string.close),
            okBtnText = stringResource(id = R.string.ok),
            title = stringResource(R.string.import_has_err),
            requireShowTextCompose = true,
            textCompose = {
                Column {
                    Row {
                        Text(text = stringResource(R.string.import_success)+":"+successImportCount.value)
                    }
                    Row {
                        Text(text = stringResource(R.string.import_failed)+":"+failedImportCount.value)
                    }
                    Row (modifier = Modifier.clickable {
                        clipboardManager.setText(AnnotatedString(failedImportListStr.value))
                        Msg.requireShow(appContext.getString(R.string.copied))  //这里如果用 Msg.requierShow() 还得刷新页面才能看到信息，这个操作没必要刷新页面，不如直接用Toast，不过Toast怎么实现的？不用刷新页面吗？
                        //test x能) 测试下刷新页面是否就能看到信息且不影响弹窗（当然不会影响，因为显示弹窗的状态变量还是真啊！只要状态没变，页面还是一样）
//                        Msg.requireShow(appContext.getString(R.string.copied))  //这里如果用 Msg.requierShow() 还得刷新页面才能看到信息，不如直接用Toast
//                        changeStateTriggerRefreshPage(needRefreshFilesPage)
                        //test
                    }
                    ){
                        Text(text = stringResource(R.string.you_can_click_here_copy_err_msg),
                            style = MyStyleKt.ClickableText.style,
                            color = MyStyleKt.ClickableText.color,
                            )
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    Row {
                        Text(text = stringResource(R.string.err_msg)+":")
                    }
                    Row {
                        Text(text = failedImportListStr.value)
                    }

//                    Column(modifier = Modifier
//                        .heightIn(max = 300.dp)
//                        .verticalScroll(rememberScrollState())) {
//                        Text(text = failedImportListStr.value)
//                    }
                }
            },
            onCancel = { showImportResultDialog.value = false }
        ){
            showImportResultDialog.value=false
        }
    }


    if(showRenameDialog.value) {
        ConfirmDialog(
            okBtnEnabled = !renameHasErr.value,
            cancelBtnText = stringResource(id = R.string.cancel),
            okBtnText = stringResource(id = R.string.ok),
            title = stringResource(R.string.rename),
            requireShowTextCompose = true,
            textCompose = {
                  Column {
                      TextField(
                          modifier = Modifier
                              .fillMaxWidth()
                              .padding(10.dp)
                          ,
                          value = renameFileName.value,
                          singleLine = true,
                          isError = renameHasErr.value,
                          supportingText = {
                              if (renameHasErr.value) {
                                  Text(
                                      modifier = Modifier.fillMaxWidth(),
                                      text = renameErrText.value,
                                      color = MaterialTheme.colorScheme.error
                                  )
                              }
                          },
                          trailingIcon = {
                              if (renameHasErr.value) {
                                  Icon(imageVector=Icons.Filled.Error,
                                      contentDescription=renameErrText.value,
                                      tint = MaterialTheme.colorScheme.error)
                                  }
                          },
                          onValueChange = {
                              updateRenameFileName(it)
                          },
                          label = {
                              Text(stringResource(R.string.file_name))
                          },
                          placeholder = {
                              Text(stringResource(R.string.input_new_file_name))
                          }
                      )
                  }
            },
            onCancel = { showRenameDialog.value = false }
        ) {
            try {
                val newFileName = renameFileName.value.text
                val fileOrFolderNameCheckRet = checkFileOrFolderNameAndTryCreateFile(newFileName, appContext)
                if(fileOrFolderNameCheckRet.hasError()) {  // 检测是否有坏字符，例如路径分隔符
                    renameHasErr.value = true
                    renameErrText.value = fileOrFolderNameCheckRet.msg

                    //检测新旧文件名是否相同 以及 新文件名是否已经存在，两者都视为文件已存在
                }else if( newFileName == renameFileItemDto.value.name || isPathExists(File(renameFileItemDto.value.fullPath).parent, newFileName)) {
                    renameHasErr.value=true
                    renameErrText.value = appContext.getString(R.string.file_already_exists)
                }else {  //执行重命名文件
                    showRenameDialog.value = false  //关闭弹窗
                    //执行重命名
                    doJobThenOffLoading(loadingOn = loadingOn, loadingOff=loadingOff) {
                        try {
                            val oldFile = File(renameFileItemDto.value.fullPath)
                            val newFile = File(File(renameFileItemDto.value.fullPath).parent, newFileName)
                            val renameSuccess = oldFile.renameTo(newFile)
                            if(renameSuccess) {
                                //重命名成功，把重命名之前的旧条目从选中列表移除，然后把改名后的新条目添加到列表（要不要改成：不管执行重命名失败还是成功，都一律移除？没必要啊，如果失败，名字又没变，移除干嘛？）
                                if(selectedItems.value.remove(renameFileItemDto.value)) {  //移除旧条目。如果返回true，说明存在，则添加重命名后的新条目进列表；如果返回false，说明旧文件不在选中列表，不执行操作
                                    val newNameDto = FileItemDto.genFileItemDtoByFile(newFile, AppModel.singleInstanceHolder.appContext)
                                    selectedItems.value.add(newNameDto)
                                }
//                            selectedItems.requireRefreshView()
                                Msg.requireShow(appContext.getString(R.string.success))
                            }else {
                                Msg.requireShow(appContext.getString(R.string.error))
                            }
                        }catch (e:Exception) {
                            Msg.requireShowLongDuration("rename failed:"+e.localizedMessage)
                        }finally {
                            //刷新页面
                            changeStateTriggerRefreshPage(needRefreshFilesPage)
                        }

                    }
                }
            }catch (outE:Exception) {
                renameHasErr.value = true
                renameErrText.value = outE.localizedMessage ?: errorStrRes
                MyLog.e(TAG, "RenameDialog in Files Page err:"+outE.stackTraceToString())
            }
        }
    }


    val createFileOrFolderErrMsg = rememberSaveable { mutableStateOf("")}
    if (showCreateFileOrFolderDialog.value) {
        CreateFileOrFolderDialog(
            errMsg = createFileOrFolderErrMsg,
            onOk = f@{ fileOrFolderName: String, type: Int ->
                //do create file or folder
                try {
                    // if current path already deleted, then show err and abort create
                    if(!File(currentPath.value).exists()) {
                        throw RuntimeException(appContext.getString(R.string.current_dir_doesnt_exist_anymore))
                    }

                    val fileOrFolderNameCheckRet = checkFileOrFolderNameAndTryCreateFile(fileOrFolderName, appContext)
                    if(fileOrFolderNameCheckRet.hasError()){
//                        Msg.requireShowLongDuration(pathCheckRet.msg)
                        createFileOrFolderErrMsg.value = fileOrFolderNameCheckRet.msg
                        return@f false
                    }else {  //文件名ok，检查文件是否存在
                        val file = File(currentPath.value, fileOrFolderName)
                        if (file.exists()) {  //文件存在
                            createFileOrFolderErrMsg.value = fileAlreadyExistStrRes
                            return@f false
                        }else {  //文件不存在（且文件名ok
                            var isCreateSuccess = false
                            if (type == Cons.fileTypeFile) {  // create file
                                isCreateSuccess = file.createNewFile()
                            } else {  // create dir
                                isCreateSuccess = file.mkdir()  //应该不需要mkdirs()，用户肯定是在当前目录创建一层目录，所以mkdir()就够用了
                            }

                            //检测创建是否成功并显示提醒
                            if (isCreateSuccess) {  //创建成功
                                Msg.requireShow(successStrRes)  //提示成功
                                createFileOrFolderErrMsg.value=""  //清空错误信息
                                //刷新Files页面
                                changeStateTriggerRefreshPage(needRefreshFilesPage)
                                return@f true
                            } else { //创建失败但原因不明
                                Msg.requireShow(errorStrRes) //提示错误
                                createFileOrFolderErrMsg.value=errorStrRes  //设置错误信息为err，不过没有具体信息，用户虽然不知道出了什么错，但知道出错了，而且仍可点取消关闭弹窗，所以问题不大
                                return@f false
                            }
                        }
                    }
                } catch (e: Exception) {
                    Msg.requireShowLongDuration(errorStrRes + ":" + e.localizedMessage)
                    MyLog.e(TAG, "CreateFileOrFolderDialog in Files Page err:"+e.stackTraceToString())
                    createFileOrFolderErrMsg.value=e.localizedMessage ?: errorStrRes
                    return@f false
                }
            },
            onCancel = {
                showCreateFileOrFolderDialog.value = false
                createFileOrFolderErrMsg.value=""
            }
        )
    }

    // 向下滚动监听，开始
    val enableFilterState = rememberSaveable { mutableStateOf(false)}
//    val firstVisible = remember { derivedStateOf { if(enableFilterState.value) filterListState.value.firstVisibleItemIndex else curListState.value.firstVisibleItemIndex } }
//    ScrollListener(
//        nowAt = firstVisible.value,
//        onScrollUp = {filesPageScrollingDown.value = false}
//    ) { // onScrollDown
//        filesPageScrollingDown.value = true
//    }
    @SuppressLint("UnrememberedMutableState")
    val lastAt = mutableIntStateOf(0)
    filesPageScrollingDown.value = remember {
        derivedStateOf {
            val nowAt = if(enableFilterState.value) {
                filterListState.value.firstVisibleItemIndex
            } else {
                curListState.value.firstVisibleItemIndex
            }
            val scrolldown = nowAt > lastAt.intValue
            lastAt.intValue = nowAt
            scrolldown
        }
    }.value
    // 向下滚动监听，结束


    // import as repo variables block start
    val showImportAsRepoDialog = rememberSaveable { mutableStateOf(false)}
    val importAsRepoList = mutableCustomStateListOf(stateKeyTag, "importAsRepoList", listOf<String>())
    val isReposParentFolderForImport = rememberSaveable { mutableStateOf(false)}
    val initImportAsRepoDialog = { fullPathList:List<String> ->
        importAsRepoList.value.clear()
        importAsRepoList.value.addAll(fullPathList)
        isReposParentFolderForImport.value = false
        showImportAsRepoDialog.value = true
    }
    // import as repo variables block end


    // init repo dialog variables block start
    val showInitRepoDialog = rememberSaveable { mutableStateOf(false)}
    val initRepoList = mutableCustomStateListOf(stateKeyTag, "initRepoList", listOf<String>())
    val initInitRepoDialog = {pathList:List<String> ->
        initRepoList.value.clear()
        initRepoList.value.addAll(pathList)
        showInitRepoDialog.value = true
    }
    // init repo dialog variables block end


    // details variables block start
    val showDetailsDialog = rememberSaveable { mutableStateOf(false)}
    val details_ItemsSize = rememberSaveable { mutableLongStateOf(0L) }  // this is not items count, is file size. this size is a recursive count
    val details_AllCount = rememberSaveable{mutableIntStateOf(0)}  // selected items count(folder + files). note: this is not a recursive count
    val details_FilesCount = rememberSaveable{mutableIntStateOf(0)}  // files count in selected items. not recursive count
    val details_FoldersCount = rememberSaveable{mutableIntStateOf(0)}  // folders count in selected items. not recursive count
    val details_CountingItemsSize = rememberSaveable { mutableStateOf(false)}  // indicate is calculating file size or finished
    val details_itemList = mutableCustomStateListOf(stateKeyTag, "details_itemList", listOf<FileItemDto>())

    val initDetailsDialog = {list:List<FileItemDto> ->
        details_FoldersCount.intValue = list.count { it.isDir }
        details_FilesCount.intValue = list.size - details_FoldersCount.intValue
        details_AllCount.intValue = list.size

        //count files/folders size
        doJobThenOffLoading {
            //prepare
            details_CountingItemsSize.value = true
            details_ItemsSize.longValue = 0

            //count
            list.forEach {
                //ps: 因为已经在函数中追加了size，所以if(it.isDir)的代码块返回0即可
                if(it.isDir) {
                    FsUtils.calculateFolderSize(it.toFile(), details_ItemsSize)
                } else {
                    details_ItemsSize.longValue += it.sizeInBytes
                }
            }

            //done
            details_CountingItemsSize.value = false
        }


        details_itemList.value.clear()
        details_itemList.value.addAll(list)

        showDetailsDialog.value=true
    }

    // details variables block end


    val findRepoThenGoToReposOrChangList = { fullPath:String, trueGoToReposFalseGoToChangeList:Boolean ->
        doJobThenOffLoading job@{
            try {
                if(repoList.value.isEmpty()) {
                    Msg.requireShowLongDuration(appContext.getString(R.string.repo_list_is_empty))
                    return@job
                }

                val repo = Libgit2Helper.findRepoByPath(fullPath)
                if(repo==null) {
                    Msg.requireShow(appContext.getString(R.string.not_found))
                }else{
                    val repoWorkDir = Libgit2Helper.getRepoWorkdirNoEndsWithSlash(repo)
                    val target = repoList.value.find { it.fullSavePath == repoWorkDir }
                    if(target==null) {
                        Msg.requireShow(appContext.getString(R.string.not_found))
                    }else {
                        if(trueGoToReposFalseGoToChangeList) {
                            goToRepoPage(target.id)
                        }else {
                            goToChangeListPage(target)
                        }
                    }
                }

            }catch (e:Exception) {
                Msg.requireShowLongDuration(e.localizedMessage ?:"err")
                MyLog.e(TAG, "#findRepoThenGoToReposOrChangList err: fullPath=$fullPath, trueGoToReposFalseGoToChangeList=$trueGoToReposFalseGoToChangeList, err=${e.localizedMessage}")
            }
        }
    }


    val showInRepos = { fullPath:String ->
        findRepoThenGoToReposOrChangList(fullPath, true)
    }

    val showInChangeList = { fullPath:String ->
        findRepoThenGoToReposOrChangList(fullPath, false)
    }


    if(isLoading.value) {
//        LoadingDialog(loadingText.value)        //这个页面不适合用Dialog，页面会闪。

        LoadingText(loadingText.value, contentPadding)
    }else {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(contentPadding)
//            .verticalScroll(StateUtil.getRememberScrollState())  //和LazyColumn不能共用
        ) {
            // bread crumb
            if(currentPathBreadCrumbList.value.isEmpty()) {
                Row (modifier = Modifier
                    .padding(5.dp)
                    .horizontalScroll(rememberScrollState())
                ){
                    // noop
                }
            }else {
                LazyRow(modifier = Modifier.padding(5.dp),
                    state = breadCrumbListState
                ) {
//            var breadCrumbList = currentPathBreadCrumbList1
//            if(currentPathBreadCrumbList.intValue==2) {
//                breadCrumbList = currentPathBreadCrumbList2
//            }
                    //面包屑 (breadcrumb)
                    val breadList = currentPathBreadCrumbList.value.toList()
                    val lastIndex = breadList.size - 1
                    breadList.forEachIndexed { idx, it ->
                        item {
                            val breadCrumbDropDownMenuExpendState = rememberSaveable { mutableStateOf(false)}

                            //如果是所有仓库的根目录，返回 "/"，否则返回 "路径+/"
                            Text(text = File.separator)
                            Text(text =it.name,
                                fontWeight = if(idx==lastIndex) FontWeight.Bold else FontWeight.Normal,
                                modifier = Modifier.combinedClickable (
                                    onLongClick = {  //long press will show menu for pressed path
                                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                        breadCrumbDropDownMenuExpendState.value = true

//                                        // copy full path
//                                        clipboardManager.setText(AnnotatedString(it.fullPath))
//                                        Msg.requireShow(appContext.getString(R.string.path_copied))
                                    }
                                ){ //onClick

                                    //                        //点击面包屑跳转路径
                                    //返回没在使用的列表
                                    //                        val willUpdateList = if(currentPathBreadCrumbList.intValue==1) currentPathBreadCrumbList2 else currentPathBreadCrumbList1
                                    //                        val clickedItemIndex = willUpdateList.indexOf(it)
                                    //                        val subListEndIndex = clickedItemIndex+1
                                    //                        if(subListEndIndex < willUpdateList.size) {  //避免越界
                                    //                            val subList = willUpdateList.subList(0, subListEndIndex)  //返回起始地址到点击位置的子列表
                                    //                            willUpdateList.clear()
                                    //                            willUpdateList.addAll(subList)
                                    //                            currentPathBreadCrumbList.intValue = if(currentPathBreadCrumbList.intValue==1) 2 else 1
                                    //                        }

                                    //更新当前路径
                                    currentPath.value = it.fullPath
                                    filesPageSimpleFilterKeyWord.value = TextFieldValue("")  //清空过滤关键字
                                    //刷新页面（然后面包屑也会重新生成）
                                    changeStateTriggerRefreshPage(needRefreshFilesPage)

                                })


                            if(breadCrumbDropDownMenuExpendState.value){
                                Column {
                                    val enableMenuItem = true
                                    //菜单列表
                                    DropdownMenu(
                                        offset = DpOffset(x=0.dp, y=20.dp),
                                        expanded = breadCrumbDropDownMenuExpendState.value,
                                        onDismissRequest = { breadCrumbDropDownMenuExpendState.value = false }
                                    ) {

                                        DropdownMenuItem(
                                            enabled = enableMenuItem,
                                            text = { Text(stringResource(R.string.copy_real_path)) },
                                            onClick = {
                                                breadCrumbDropDownMenuExpendState.value = false
                                                copyRealPath(it.fullPath)
                                            }
                                        )
                                        DropdownMenuItem(
                                            enabled = enableMenuItem,
                                            text = { Text(stringResource(R.string.import_as_repo)) },
                                            onClick = {
                                                breadCrumbDropDownMenuExpendState.value = false
                                                initImportAsRepoDialog(listOf(it.fullPath))
                                            }
                                        )
                                        DropdownMenuItem(
                                            enabled = enableMenuItem,
                                            text = { Text(stringResource(R.string.init_repo)) },
                                            onClick = {
                                                breadCrumbDropDownMenuExpendState.value = false
                                                initInitRepoDialog(listOf(it.fullPath))
                                            }
                                        )
                                        DropdownMenuItem(
                                            enabled = enableMenuItem,
                                            text = { Text(stringResource(R.string.show_in_repos)) },
                                            onClick = {
                                                breadCrumbDropDownMenuExpendState.value = false
                                                showInRepos(it.fullPath)
                                            }
                                        )
                                        DropdownMenuItem(
                                            enabled = enableMenuItem,
                                            text = { Text(stringResource(R.string.show_in_changelist)) },
                                            onClick = {
                                                breadCrumbDropDownMenuExpendState.value = false
                                                showInChangeList(it.fullPath)
                                            }
                                        )
                                        DropdownMenuItem(
                                            enabled = enableMenuItem,
                                            text = { Text(stringResource(R.string.details)) },
                                            onClick = {
                                                breadCrumbDropDownMenuExpendState.value = false
                                                initDetailsDialog(listOf(it))
                                            }
                                        )
                                    }
                                }
                            }

                        }
                    }
                }

                //make breadCrumb always scroll to end for show current path
                val scrollToLast = remember{
                    derivedStateOf {
                        UIHelper.scrollToItem(scope, breadCrumbListState, currentPathBreadCrumbList.value.size-1)
                    }
                }.value
            }

            // file list
            // if has err, show err, else show file list
            if(openDirErr.value.isNotBlank()){
                Column(
                    modifier = Modifier
                        //fillMaxSize 必须在最上面！要不然，文字不会显示在中间！
                        .fillMaxSize()
                        .padding(contentPadding)
                        .padding(10.dp)
                        .verticalScroll(rememberScrollState())
                    ,
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text(openDirErr.value, color=MyStyleKt.TextColor.error)
                }
            }else {
                val k = filesPageSimpleFilterKeyWord.value.text.lowercase()  //关键字
                val enableFilter = filesPageSimpleFilterOn.value && k.isNotEmpty()
                val currentPathFileList = if(enableFilter){
                    val fl = currentPathFileList.value.filter {
                        it.name.lowercase().contains(k) || it.lastModifiedTime.lowercase().contains(k) || it.createTime.lowercase().contains(k)
                    }
                    filterList.value.clear()
                    filterList.value.addAll(fl)
                    fl
                }else {
                    currentPathFileList.value
                }


                val listState = if(enableFilter) rememberLazyListState() else curListState.value
                if(enableFilter) {  //更新filter列表state
                    filterListState.value = listState
                }
                //更新是否启用filter
                enableFilterState.value = enableFilter

                MyLazyColumn(
                    contentPadding = PaddingValues(0.dp),  //外部padding了
                    list = currentPathFileList,
                    listState = listState,
                    requireForEachWithIndex = true,
                    requirePaddingAtBottom =true
                ) {index, it ->
                    // 没测试，我看其他文件管理器针对目录都没open with，所以直接隐藏了) 需要测试：能否针对目录执行openwith？如果不能，对目录移除openwith选项
                    FileListItem(
                        item = it,
                        isPasteMode = isPasteMode,
                        menuKeyTextList = if(it.isFile) fileMenuKeyTextList else dirMenuKeyTextList,
                        menuKeyActList = if(it.isFile) fileMenuKeyActList else dirMenuKeyActList,
                        iconOnClick={  //点击文件或文件夹图标时的回调函数
                            if (!isPasteMode.value && !isImportMode.value) {
                                switchItemSelected(it)
                            }
                        },
                        switchItemSelected = switchItemSelected,
                        isItemInSelected=isItemInSelected,
                        itemOnLongClick = {
                            //如果不是选择模式或粘贴模式，切换为选择模式
                            if (!isFileSelectionMode.value && !isPasteMode.value && !isImportMode.value) {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                switchItemSelected(it)

                                //如果处于选择模式，长按执行连续选择
                            }else if(isFileSelectionMode.value && !isPasteMode.value && !isImportMode.value) {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                UIHelper.doSelectSpan(index, it,
                                    //这里调用 toList() 是为了拷贝下源list，避免并发修改异常
                                    selectedItems.value.toList(), currentPathFileList.toList(),
                                    switchItemSelected,
                                    selecteItem
                                )
                            }
                        }
                    ) itemOnClick@{  //itemOnClick
                        if (isFileSelectionMode.value) {  //选择模式，切换选择
                            switchItemSelected(it)
                        } else {  //非选择模式，若文件则在编辑器打开，否则在当前页面打开目录

                            //关闭过滤模式的逻辑：如果是目录，一律关闭；如果是文件，判断是否用内部Editor打开，如果是关闭，否则不关闭。
                            if (it.isFile) {
                                //粘贴或导入模式下点击文件无效，除非先退出对应模式(不过没禁止通过三个点的菜单打开文件)
                                if(isPasteMode.value || isImportMode.value) {
                                    return@itemOnClick
                                }

                                //检查文件大小，如果太大，拒绝打开，考虑下大小设多少合适。
                                // 如果是文本类型，用文本编辑器打开，其他类型弹窗提示用外部程序打开还是用文本编辑器打开
                                //goto editor page with file path

                                //如果是文件，只有使用内部Editor打开文件才退出过滤模式，否则不退出（请求使用外部程序打开文件时退出过滤模式的话感觉很奇怪）
                                if(MimeType.guessFromFileName(it.name).value == MimeType.TEXT_PLAIN.value) {  //如果是文本类型，直接打开
                                    //关闭过滤模式
                                    //20240516: 改成跳转到编辑器不关过滤模式了，感觉更符合直觉
//                                filesPageFilterModeOff()


                                    //请求打开文件
                                    val expectReadOnly = false
                                    requireInnerEditorOpenFile(it.fullPath, expectReadOnly)
                                }else {  //非文本类型，尝试用外部软件打开
                                    openAsDialogFilePath.value = it.fullPath
                                    showOpenInEditor.value=true
                                    showOpenAsDialog.value=true
//                                        val file = File(it.fullPath)
//                                        val ret = FsUtils.openFileEditFirstIfFailedThenTryView(appContext, file)
//                                        if(ret.hasError()) {
//                                            Msg.requireShow(appContext.getString(R.string.open_file_err_not_supported_type_may_try_change_name_to_txt))
//                                            changeStateTriggerRefreshPage(needRefreshFilesPage)
//
//                                        }
                                }
                            } else/* if (item.isDirectory) */ {  //点击目录，直接打开
                                //粘贴模式下点击被选中的文件夹无效，以免出现无限递归复制
                                //导入模式不会选中PuppyGit app中的文件夹且所有文件夹都可点击，所以无需判断导入模式
                                if(isPasteMode.value && isItemInSelected(it)) {
                                    return@itemOnClick
                                }

                                //关闭过滤模式
//                            filesPageFilterModeOff()

                                filesPageSimpleFilterKeyWord.value = TextFieldValue("")  //清空过滤关键字

                                //打开目录
                                currentPath.value = it.fullPath
                                //更新面包屑，重组吧还是
//                                val willUpdateList = if(currentPathBreadCrumbList.intValue == 1) currentPathBreadCrumbList1 else currentPathBreadCrumbList2
//                                willUpdateList.add(it)
                                //刷新页面
                                changeStateTriggerRefreshPage(needRefreshFilesPage)
                            }
                        }
                    }

                    HorizontalDivider()
                }
            }

        }
    }


    //这个应该用remember，因为屏幕一旋转，选中列表会被清空，所以，就算显示删除对话框，也不知道该删什么
    val showRemoveFromGitDialog = rememberSaveable { mutableStateOf(false)}
    if(showRemoveFromGitDialog.value) {
        ConfirmDialog(
            title = stringResource(id = R.string.remove_from_git),
            text = stringResource(R.string.will_remove_selected_items_from_git_are_u_sure),
            onCancel = { showRemoveFromGitDialog.value=false }
        ) {
            //关闭弹窗
            showRemoveFromGitDialog.value=false
            //执行删除
            doJobThenOffLoading (loadingOn = loadingOn, loadingOff=loadingOff) {
                if(selectedItems.value.isEmpty()) {  //例如，我选择了文件，然后对文件执行了重命名，导致已选中条目被移除，就会发生选中条目列表为空或缺少了条目的情况
                    Msg.requireShow(appContext.getString(R.string.no_item_selected))
                    //退出选择模式和刷新页面
                    filesPageQuitSelectionMode()
                    changeStateTriggerRefreshPage(needRefreshFilesPage)
                    return@doJobThenOffLoading  // 结束操作
                }

                var repoWillUse = Libgit2Helper.findRepoByPath(selectedItems.value[0].fullPath)
                if(repoWillUse == null) {
                    Msg.requireShow(appContext.getString(R.string.err_dir_is_not_a_git_repo))
                    //退出选择模式和刷新页面
                    filesPageQuitSelectionMode()
                    changeStateTriggerRefreshPage(needRefreshFilesPage)
                    return@doJobThenOffLoading  // 结束操作
                }

                val repoWorkDirFullPath = repoWillUse.workdir().toFile().canonicalPath
                MyLog.d(TAG, "#RemoveFromGitDialog: will remove files from repo: '${repoWorkDirFullPath}'")

                repoWillUse.use { repo ->
                    val repoIndex = repo.index()

                    //开始循环，删除所有选中文件
                    selectedItems.value.toList().forEach {
//                        val (repoFullPath, relativePathUnderRepo) = getFilePathStrUnderRepoByFullPath(it.fullPath)
                          val relativePathUnderRepo = getFilePathUnderParent(repoWorkDirFullPath, it.fullPath)
//                        repoWillUse = repoFullPath  //因为只可能同一时间删除同一仓库下的文件，所以其实只有一个仓库且这个值只需要赋值一次，但我不知道判断仓库是否为空和赋值哪个更快，所以索性每次都赋值了

                        //存在有效仓库，且文件的仓库内相对路径不为空，且不是.git目录本身，且不是.git目录下的文件
                        if(relativePathUnderRepo.isNotEmpty() && relativePathUnderRepo !=".git" && !relativePathUnderRepo.startsWith(".git/")) {  //starts with ".git/" 既可用于.git/目录名，也可用于其下的目录名，不过我测试过，如果是选中的.git目录，末尾没/，所以在判断路径是否等于.git那里就已经短路了
                            Libgit2Helper.removeFromIndexThenWriteToDisk(
                                repoIndex,
                                Pair(it.isFile, relativePathUnderRepo),
                                requireWriteToDisk = false  //这里不保存修改，等删完后统一保存修改
                            )  //最后一个值表示不希望调用的函数执行 index.write()，我删完列表后自己会执行，不需要每个条目都执行，所以传false请求调用的函数别执行index.write()

                        }
                    }

                    //保存修改
                    repoIndex.write()

                }

                Msg.requireShow(appContext.getString(R.string.success))

                //退出选择模式并刷新目录
                filesPageQuitSelectionMode()
                changeStateTriggerRefreshPage(needRefreshFilesPage)

            }
        }
    }

    val showDelFileDialog = rememberSaveable { mutableStateOf(false)}
    if(showDelFileDialog.value) {
        ConfirmDialog(
            title = stringResource(id = R.string.delete),
            text = stringResource(R.string.will_delete_selected_items_are_u_sure),
            onCancel = { showDelFileDialog.value=false }
        ) {
            //关闭弹窗
            showDelFileDialog.value=false
            //执行删除
            doJobThenOffLoading (loadingOn = loadingOn, loadingOff=loadingOff) {
                if(selectedItems.value.isEmpty()) {  //例如，我选择了文件，然后对文件执行了重命名，导致已选中条目被移除，就会发生选中条目列表为空或缺少了条目的情况
                    Msg.requireShow(appContext.getString(R.string.no_item_selected))
                    //退出选择模式和刷新页面
                    filesPageQuitSelectionMode()
                    changeStateTriggerRefreshPage(needRefreshFilesPage)
                    return@doJobThenOffLoading  // 结束操作
                }

                selectedItems.value.toList().forEach {
                    val file = File(it.fullPath)
                    // 如果要删除的路径包含.git，加个警告，但不阻止，用户非要删，我不管
//                    if(file.canonicalPath.contains(".git")) {
//                        MyLog.w(TAG, "#DelFileDialog: may delete file under '.git' folder, fullPath will del is: '${file.canonicalPath}'")
//                    }

                    //不管是目录还是文件，直接梭哈
                    file.deleteRecursively()

                }


                Msg.requireShow(appContext.getString(R.string.success))
                //退出选择模式并刷新目录
                filesPageQuitSelectionMode()
                changeStateTriggerRefreshPage(needRefreshFilesPage)
            }
        }
    }
    val copyOrMoveOrExportFile = copyOrMoveOrExportFile@{ srcList:List<FileItemDto>, targetFullPath:String, requireDeleteSrc:Boolean ->
//                if(pastMode.intValue == pastMode_Copy) {
//                    //执行拷贝
//                }else if(pastMode.intValue == pastMode_Move) {
//                    // 执行移动
//                }
//                fileNeedOverrideList.clear()
        //其实不管拷贝还是移动都要先拷贝，区别在于移动后需要删除源目录
        //如果发现同名，添加到同名列表，弹窗询问是否覆盖。
        doJobThenOffLoading (loadingOn = loadingOn, loadingOff=loadingOff) {
            val ret = FsUtils.copyOrMoveOrExportFile(srcList.map { it.toFile() }, File(targetFullPath), requireDeleteSrc)
            if(ret.hasError()) {
                if(ret.code == Ret.ErrCode.srcListIsEmpty) {
                    Msg.requireShow(appContext.getString(R.string.no_item_selected))
                }else if(ret.code == Ret.ErrCode.targetIsFileButExpectDir) {
                    Msg.requireShow(appContext.getString(R.string.err_target_is_file_but_expect_dir))
                }

                //退出选择模式和刷新页面
                filesPageQuitSelectionMode()
                changeStateTriggerRefreshPage(needRefreshFilesPage)
                return@doJobThenOffLoading
            }

            //执行到这就说明操作成功完成了
            //显示成功提示
            Msg.requireShow(appContext.getString(R.string.success))

            //退出选择模式和刷新页面
            filesPageQuitSelectionMode()
            changeStateTriggerRefreshPage(needRefreshFilesPage)
        }

    }

    val pasteMode_Move = 1
    val pasteMode_Copy = 2
    val pasteMode_None = 0  //不执行任何操作
    val pasteMode = rememberSaveable{mutableIntStateOf(pasteMode_None)}
    val setPasteModeThenShowPasteBar = { pastModeVal:Int ->
        pasteMode.intValue = pastModeVal
        isFileSelectionMode.value=false
        isPasteMode.value=true
    }

    val showExportDialog = rememberSaveable { mutableStateOf(false)}

//    val userChosenExportDirUri = StateUtil.getRememberSaveableState<Uri?>(initValue = null)
    //ActivityResultContracts.OpenMultipleDocuments() 多选文件，这个应该可用来导入，不过现在有分享，足够了，虽然分享不能导入目录
    val exportErrorMsg = rememberSaveable { mutableStateOf("")}
    val showExportErrorDialog = rememberSaveable { mutableStateOf(false)}
    val chooseDirLauncher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocumentTree()) exportSaf@{ uri ->
        //执行导出
        if(uri!=null) {
            doJobThenOffLoading(loadingOn, loadingOff, appContext.getString(R.string.exporting)) {
                val chosenDir = DocumentFile.fromTreeUri(appContext, uri)
                if(chosenDir==null) {
                    Msg.requireShow(appContext.getString(R.string.err_get_export_dir_failed))
                    return@doJobThenOffLoading
                }
//            appContext.contentResolver.openOutputStream(chosenDir?.createFile("*/*", "test.txt")?.uri!!)
                try {
                    FsUtils.recursiveExportFiles_Saf(appContext.contentResolver, chosenDir, selectedItems.value.map<FileItemDto, File> { it.toFile() })
                    // throw RuntimeException("测试异常！")  passed
                    Msg.requireShow(appContext.getString(R.string.export_success))
                }catch (e:Exception) {
                    MyLog.e(TAG, "#exportSaf@ err:"+e.stackTraceToString())
                    val exportErrStrRes = appContext.getString(R.string.export_err)
                    Msg.requireShow(exportErrStrRes)
                    exportErrorMsg.value = "$exportErrStrRes: "+e.localizedMessage
                    showExportErrorDialog.value = true
                }
            }

        }else {  //用户如果没选目录，uri就会等于null
            Msg.requireShow(appContext.getString(R.string.export_canceled))
        }
    }

    if(showInitRepoDialog.value) {
        val selctedDirs = initRepoList.value

        if(selctedDirs.isEmpty()) {
            showInitRepoDialog.value = false
            Msg.requireShow(stringResource(R.string.no_dir_selected))
        }else {
            ConfirmDialog(
                title = stringResource(R.string.init_repo),
                text = stringResource(R.string.will_init_selected_folders_to_git_repos_are_you_sure),
                okBtnEnabled = selctedDirs.isNotEmpty(),
                onCancel = { showInitRepoDialog.value = false}
            ) {
                showInitRepoDialog.value=false
                doJobThenOffLoading(loadingOn, loadingOff, appContext.getString(R.string.loading)) {
                    try {
                        var successCnt = 0
                        selctedDirs.forEach { dirPath ->
                            try {
                                Libgit2Helper.initGitRepo(dirPath)
                                successCnt++
                            }catch (e:Exception) {
//                            Msg.requireShowLongDuration(e.localizedMessage ?: "err")
                                MyLog.e(TAG, "init repo in FilesPage err: path=${dirPath}, err=${e.localizedMessage}")
                            }
                        }

                        Msg.requireShowLongDuration(replaceStringResList(appContext.getString(R.string.n_inited), listOf(""+successCnt)))
                    }finally {
                        changeStateTriggerRefreshPage(needRefreshFilesPage)
                    }
                }
            }
        }

    }


    if(showImportAsRepoDialog.value) {
        val selctedDirs = importAsRepoList.value

        if(selctedDirs.isEmpty()) {
            showImportAsRepoDialog.value = false
            Msg.requireShow(stringResource(R.string.no_dir_selected))
        }else {
            ConfirmDialog(
                title = stringResource(R.string.import_repo),
                requireShowTextCompose = true,
                textCompose = {
                    Column(modifier = Modifier
                        .verticalScroll(rememberScrollState())
                        .fillMaxWidth()
                        .padding(5.dp)
                    ) {
                        Row(modifier = Modifier.padding(bottom = 15.dp)) {
                            Text(
                                text = stringResource(R.string.please_grant_permission_before_import_repo),
                                style = MyStyleKt.ClickableText.style,
                                color = MyStyleKt.ClickableText.color,
                                overflow = TextOverflow.Visible,
                                fontWeight = FontWeight.Light,
                                modifier = MyStyleKt.ClickableText.modifier.clickable {
                                    // grant permission for read/write external storage
                                    if (activity == null) {
                                        Msg.requireShowLongDuration(appContext.getString(R.string.please_go_to_settings_allow_manage_storage))
                                    } else {
                                        activity!!.getStoragePermission()
                                    }
                                },
                            )

                        }

                        Spacer(Modifier.height(15.dp))

                        MyCheckBox(text = stringResource(R.string.paths_are_repo_parent_dir), value = isReposParentFolderForImport)

                        Spacer(Modifier.height(5.dp))

                        if(isReposParentFolderForImport.value) {
                            Text(stringResource(R.string.will_scan_repos_under_folders), fontWeight = FontWeight.Light)
                        }
                    }
                },
                okBtnText = stringResource(R.string.ok),
                cancelBtnText = stringResource(R.string.cancel),
                okBtnEnabled = selctedDirs.isNotEmpty(),
                onCancel = { showImportAsRepoDialog.value = false },
            ) {
                doJobThenOffLoading(loadingOn, loadingOff, appContext.getString(R.string.importing)) {
                    val importRepoResult = ImportRepoResult()
                    try {
                        selctedDirs.forEach { dirPath ->
                            val result = AppModel.singleInstanceHolder.dbContainer.repoRepository.importRepos(dir=dirPath, isReposParent=isReposParentFolderForImport.value)
                            importRepoResult.all += result.all
                            importRepoResult.success += result.success
                            importRepoResult.failed += result.failed
                            importRepoResult.existed += result.existed
                        }

                        showImportAsRepoDialog.value = false

                        Msg.requireShowLongDuration(replaceStringResList(appContext.getString(R.string.n_imported), listOf(""+importRepoResult.success)))
                    }catch (e:Exception) {
                        //出错的时候，importRepoResult的计数不一定准，有可能比实际成功和失败的少，不过不可能多
                        MyLog.e(TAG, "import repo from FilesPage err: importRepoResult=$importRepoResult, err="+e.stackTraceToString())
                        Msg.requireShowLongDuration("err:${e.localizedMessage}")
                    }finally {
                        // because import doesn't change Files page, so need not do anything yet
                    }
                }

            }
        }
    }


    if(showDetailsDialog.value) {
        val itemList = details_itemList.value
        ConfirmDialog2(
            title = stringResource(id = R.string.details),
            requireShowTextCompose = true,
            //用compose，这样就可仅更新大小记数，size(counting...): 变化的size
            textCompose = {
                MySelectionContainer {
                    Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                        Row {
                            Text(text = replaceStringResList(stringResource(R.string.items_n1_n2_folders_n3_files), listOf(""+details_AllCount.intValue, ""+details_FoldersCount.intValue, ""+details_FilesCount.intValue)))
                        }

                        Spacer(modifier = Modifier.height(15.dp))

                        Row {
                            // if counting not finished: "123MB..." else "123MB"
                            Text(text = replaceStringResList(stringResource(R.string.size_n), listOf(getHumanReadableSizeStr(details_ItemsSize.longValue))) + (if (details_CountingItemsSize.value) "..." else ""))
                        }


                        //when only selected 1 item, show it's name and path
                        if(itemList.size==1) {
                            val item = itemList[0]

                            Spacer(modifier = Modifier.height(15.dp))

                            Row {
                                Text(text = stringResource(R.string.name)+": "+item.name)
                            }

                            Spacer(modifier = Modifier.height(15.dp))

                            Row {
                                Text(text = stringResource(R.string.path)+": "+item.fullPath)
                            }

//                            Spacer(modifier = Modifier.height(15.dp))

                        }

                    }
                }
            },

            //隐藏取消按钮，点击ok和点击弹窗外区域关闭弹窗
            showCancel = false,  //隐藏取消按钮
            onCancel = {},  //因为隐藏了取消按钮，所以执行操作传空即可
            onDismiss = {showDetailsDialog.value = false},  //点击弹窗外区域执行的操作
            okBtnText = stringResource(R.string.close),
        ) {  // onOk
            showDetailsDialog.value = false
        }
    }


    val showSelectedItemsShortDetailsDialog = rememberSaveable { mutableStateOf(false)}
    val selectedItemsShortDetailsStr = rememberSaveable { mutableStateOf("")}
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

    val countNumOnClickForSelectAndPasteModeBottomBar = {
        val list = selectedItems.value.toList()
        val sb = StringBuilder()
        list.toList().forEach {
            sb.appendLine("${it.name}, ${if(it.isDir) "dir" else "file"}, ${it.fullPath}").appendLine()
        }
        selectedItemsShortDetailsStr.value = sb.removeSuffix("\n").toString()
        showSelectedItemsShortDetailsDialog.value = true
    }

    //Bottom bar，一个是选择模式，一个是粘贴模式
    if (isFileSelectionMode.value) {
        val selectionModeIconList = listOf(
            Icons.Filled.Delete,
            Icons.Filled.ContentCut,
            Icons.Filled.ContentCopy,
            Icons.Filled.SelectAll,  //全选
        )
        val selectionModeIconTextList = listOf(
            stringResource(R.string.delete),
            stringResource(R.string.move),
            stringResource(R.string.copy),
            stringResource(R.string.select_all),
        )
        val selectionModeIconOnClickList = listOf<()->Unit>(
            delete@{
                showDelFileDialog.value = true
            },
            move@{
                setPasteModeThenShowPasteBar(pasteMode_Move)
            },
            copy@{
                setPasteModeThenShowPasteBar(pasteMode_Copy)
            },
            selectAll@{
                val list = if(enableFilterState.value) filterList.value else currentPathFileList.value

                selectedItems.value.clear()
                selectedItems.value.addAll(list)
                Unit
            }
        )
        val selectionModeIconEnableList = listOf(
            {getSelectedFilesCount()>0},  //是否启用delete
            {getSelectedFilesCount()>0},  //是否启用move
            {getSelectedFilesCount()>0},  //是否启用copy
            {true},  //是否启用全选
        )
        val selectionModeMoreItemTextList = listOf(
            stringResource(R.string.export),
            stringResource(id = R.string.remove_from_git),  //列表显示顺序就是这里的排序，上到下
            stringResource(id = R.string.details),
            if(proFeatureEnabled(importReposFromFilesTestPassed)) stringResource(id = R.string.import_as_repo) else "",  // empty string will be ignore when display menu items
            if(proFeatureEnabled(initRepoFromFilesPageTestPassed)) stringResource(id = R.string.init_repo) else "",
        )
        val selectionModeMoreItemOnClickList = listOf(
            export@{
//                showExportDialog.value = true
                //显示选择导出目录的文件选择界面
                chooseDirLauncher.launch(null)
            },
            removeFromGit@{
                showRemoveFromGitDialog.value = true
            },
            details@{
                initDetailsDialog(selectedItems.value.toList())
            },

            importAsRepo@{
                initImportAsRepoDialog(selectedItems.value.filter { it.isDir }.map { it.fullPath })
            },
            initRepo@{
                initInitRepoDialog(selectedItems.value.filter { it.isDir }.map { it.fullPath })
            }
        )
        val selectionModeMoreItemEnableList = listOf(
            {getSelectedFilesCount()>0}, //是否启用export
            {getSelectedFilesCount()>0}, //是否启用remove from git
            {getSelectedFilesCount()>0}, //是否启用details
//            {selectedItems.value.indexOfFirst{it.isDir} != -1}  //enable import as repo. (if has dirs in selected items, then enable else disbale) (after clicked then check better than check at every time selected list change)
            {getSelectedFilesCount()>0},  // import as repos
            {getSelectedFilesCount()>0}, // init repo
        )


        if(!isLoading.value) {
            BottomBar(
                quitSelectionMode=filesPageQuitSelectionMode,
                iconList=selectionModeIconList,
                iconTextList=selectionModeIconTextList,
                iconDescTextList=selectionModeIconTextList,
                iconOnClickList=selectionModeIconOnClickList,
                iconEnableList=selectionModeIconEnableList,
                enableMoreIcon=true,
                moreItemTextList=selectionModeMoreItemTextList,
                moreItemOnClickList=selectionModeMoreItemOnClickList,
                getSelectedFilesCount = getSelectedFilesCount,
                moreItemEnableList = selectionModeMoreItemEnableList,
                countNumOnClickEnabled = true,
                countNumOnClick = countNumOnClickForSelectAndPasteModeBottomBar,
            )
        }
    }








    //导入模式

    val quitImportMode = {
        requireImportUriList.value.clear();
//        requireImportUriList.requireRefreshView()
        isImportMode.value=false
    }
    val getRequireUriFilesCount = {requireImportUriList.value.size}
    if(isImportMode.value) {
        val selectionModeIconList = listOf(
            Icons.Filled.FileDownload,
        )
        val selectionModeIconTextList = listOf(
            stringResource(R.string.import_files),
        )
        val selectionModeIconOnClickList = listOf(
            importFiles@{
                doJobThenOffLoading (loadingOn = loadingOn, loadingOff=loadingOff, loadingText=appContext.getString(R.string.importing)) {
//                    val successList = mutableListOf<String>()
//                    val failedList = mutableListOf<String>()
                    val sb = StringBuilder()
                    var succCnt = 0
                    var failedCnt = 0

                    val dest = currentPath.value
                    var previousFilePath=""
                    var curTarget:File?=null

                    requireImportUriList.value.toList().forEach { it:Uri? ->
                        try {
                            if(it!=null && it.path!=null && it.path!!.length>0) {
                                //从这到更新curTarget，curTarget和perviousFilePath都应该相同

                                //有的文件管理器提供的路径即使执行到这里也是转码过的，
                                // 所以，File(Uri)没法获取到对应文件或目录的真实信息，文件名也依然是转码过的，
                                // 如果对应条目是个文件，依然能成功拷贝，不过文件名是带url编码的，例如 “%32abc”之类的，
                                //这里不能尝试再解码，因为有可能真实文件名本身就包含%等字符；
                                //如果带编码的文件是个目录，则有些特殊，上面说了，file无法获取带url编码的文件信息，
                                // 也无法判断对应条目是目录还是文件，所以会执行拷贝，然后因为对应条目是个目录而获取InputStream失败，最后进入异常代码块
                                //不过改用DocumentFile来判断的话，应该比File要更准确一些，判断错的可能性也更小一些(打脸了，经过我的测试，并不准，甚至会把目录判断成文件，还不如用file判断呢)
                                //经过我的测试：file判断有的文件既不是目录又不是文件，但基本可以确保如果isDirectory返回true就真的是目录，isFile返回true就真的是文件，而DocumentFile则有可能即使isFile返回true，也不是个文件而是目录。
//                                val srcDocumentFile = FsUtils.getDocumentFileFromUri(appContext, it)

                                val src = File(it.path!!)
                                if(src.isDirectory) {  //不支持拷贝目录
                                    failedCnt++
                                    sb.appendLine("'${it.path}'"+": is a directory, only support import files!")
                                    return@forEach
                                }

                                //x 作废)执行到这有可能获取srcDocumentFile失败，其值为null；也有可能成功，且判断其是文件，下面不再做判断，直接拷贝，反正有异常捕获兜底

                                //尝试获取真实文件名
                                var srcFileName = FsUtils.getFileRealNameFromUri(appContext, it)
                                if(srcFileName==null) {  //获取真实文件名失败
                                    //尝试获取uri中的文件名，可能和真实文件名不符
                                    srcFileName = src.name
                                    MyLog.w(TAG, "#importFiles@: getFileRealNameFromUri() return null, will use src.name:${srcFileName}")
                                    if(srcFileName.isNullOrEmpty()) {  //获取文件名失败
                                        //真实文件名和src.name都获取失败，生成个随机文件名，不过如果代码执行到这，我估计多半拷贝不了文件，连文件名都没有，一般是哪里出问题了才会这样
                                        val randomName = getShortUUID()
                                        srcFileName = randomName
                                        MyLog.w(TAG, "#importFiles@:src.name is null or empty, will use a random name:${srcFileName}")

                                    }
                                }
                                //拷贝
                                val target = FsUtils.getANonExistsTarget(File(dest, srcFileName))  //获得一个不重名的文件
                                //从这里到操作成功，curTarget和previousFilePath都应该不同，操作成功后，两者相同
                                curTarget = File(target.canonicalPath)  //获得target后，立刻创建一个拷贝，用来在异常时判断是否更新了target来决定是否删除target
//                                println("target.canonicalPath:::"+target.canonicalPath)
                                val inputStream = appContext.contentResolver.openInputStream(it)
                                if(inputStream==null) {
                                    failedCnt++
                                    sb.appendLine("'${it.path}'"+": can't read!")
                                    return@forEach
                                }
                                //拷贝并自动关流
                                inputStream.use { input ->
                                    target.outputStream().use { output ->
                                        input.copyTo(output)
                                    }
                                }
//                                inputStream.copyTo(target.outputStream())  //还得手动关流，麻烦
                                succCnt++
                                //只有操作成功后才更新上一文件path，这样的话，如果拷贝失败，上一路径和当前路径就会不同，就能判断是否换了target了，从而避免误删之前的target
                                previousFilePath == curTarget?.canonicalPath?:""
                            }else {
                                failedCnt++
                                sb.appendLine("uri is null!")
                            }

                        }catch (e:Exception) {
                            //检查目标文件是否存在，如果存在，删除 。 算了，不删了，让用户自己看着办吧！如果拷贝失败，有可能依然创建了文件，但文件大小为0，但如果不为0呢？我还得判断，算了，不管了，让用户自己看着办吧！算了，还是删一下吧！
                            failedCnt++
                            sb.appendLine((it?.path?:"/fileNameIsNull/") + ": error:"+e.localizedMessage)

                            //检查，确保target更新了而不是上个文件，避免target更新前就异常导致删除之前成功复制的target，如果target确实更新了且执行失败，且文件大小为0，删除文件
                            try {
                                if (curTarget != null) {
                                    val curFilePath = curTarget!!.canonicalPath
                                    if (curFilePath.isNotBlank() && curFilePath != previousFilePath && curTarget!!.exists() && curTarget!!.length() <= 0) {
                                        curTarget!!.delete()
                                    }
                                }
                            } catch (e2: Exception) {
                                // 不记录日志了，万一用户拷贝大量文件且出错，会记一堆，不太好
//                                e2.printStackTrace()
                            }
                        }
                    }


                    //更新计数和错误条目字符串状态变量
                    successImportCount.intValue = succCnt
                    failedImportCount.intValue = failedCnt
                    failedImportListStr.value = sb.toString()

                    if(failedCnt<1) {  //成功显示提示信息
                        Msg.requireShow(appContext.getString(R.string.import_success))
                    }else {  //失败显示弹窗，可复制失败结果
//                        successImportList.clear()
//                        successImportList.addAll(successList)
//                        failedImportList.clear()
//                        failedImportList.addAll(failedList)

                        showImportResultDialog.value = true
                    }

                    quitImportMode()
                    changeStateTriggerRefreshPage(needRefreshFilesPage)

                }

                Unit
            },
        )
        val selectionModeIconEnableList = listOf(
            {requireImportUriList.value.isNotEmpty()},
        )

        val countNumOnClickForImportMode = {
            val list = requireImportUriList.value.toList()
            val sb = StringBuilder()
            list.toList().forEach {
                sb.appendLine(it.path).appendLine()
            }
            selectedItemsShortDetailsStr.value = sb.removeSuffix("\n").toString()
            showSelectedItemsShortDetailsDialog.value = true

        }

        if(!isLoading.value) {
            BottomBar(
                quitSelectionMode=quitImportMode,
                iconList=selectionModeIconList,
                iconTextList=selectionModeIconTextList,
                iconDescTextList=selectionModeIconTextList,
                iconOnClickList=selectionModeIconOnClickList,
                iconEnableList=selectionModeIconEnableList,
                enableMoreIcon=false,
                moreItemTextList= listOf(),
                moreItemOnClickList= listOf(),
                getSelectedFilesCount = getRequireUriFilesCount,
                moreItemEnableList = listOf(),
                countNumOnClickEnabled = true,
                countNumOnClick=countNumOnClickForImportMode
            )
        }
    }



    if(showExportErrorDialog.value) {
        //显示导出失败弹窗，包含错误信息且可拷贝
        CopyableDialog(
            title = stringResource(R.string.export_err),
            text = exportErrorMsg.value,
            onCancel = {showExportErrorDialog.value=false; exportErrorMsg.value=""}
        ) {
                //onOk
                showExportErrorDialog.value=false
                clipboardManager.setText(AnnotatedString(exportErrorMsg.value))
                Msg.requireShow(appContext.getString(R.string.copied))
                exportErrorMsg.value=""  //清空错误信息，节省内存？其实清不清都行，不过不清对用户也不可见了，索性清了吧
        }
    }


    if(showExportDialog.value) {
        ConfirmDialog(title= stringResource(id = R.string.export),
            requireShowTextCompose = true,
            textCompose = {
                          Column {
                              Row {
                                Text(text = stringResource(id = R.string.will_export_files_to)+":")

                              }
                              Spacer(modifier = Modifier.height(10.dp))
                              Row(
                                  horizontalArrangement = Arrangement.Center,
                                  verticalAlignment = Alignment.CenterVertically
                              ) {
                                  Text(text = FsUtils.appExportFolderNameUnderDocumentsDirShowToUser,
                                      fontWeight = FontWeight.ExtraBold
                                      )
                              }
                              Spacer(modifier = Modifier.height(10.dp))
                              Row {
                                  Text(text = stringResource(id = R.string.are_you_sure))
                              }

                          }
            },
            onCancel = { showExportDialog.value = false }
        ) onOk@{
            showExportDialog.value=false
            val ret = FsUtils.getExportDirUnderPublicDocument()
            if(ret.hasError() || ret.data==null || !ret.data!!.exists()) {
                Msg.requireShowLongDuration(appContext.getString(R.string.get_default_export_folder_failed_plz_choose_one))
                //如果获取默认export目录失败，弹出文件选择器，让用户选个目录
                chooseDirLauncher.launch(null)  //这里传的null是弹出的界面的起始文件夹
                return@onOk
            }

            copyOrMoveOrExportFile(selectedItems.value, ret.data!!.canonicalPath, false)
        }

    }





    if(isPasteMode.value) {
        val iconList = listOf(
            Icons.Filled.ContentPaste,
        )
        val iconTextList = listOf(
            stringResource(R.string.paste),
        )
        val iconOnClickList = listOf(
            paste@{
                copyOrMoveOrExportFile(selectedItems.value, currentPath.value, pasteMode.intValue == pasteMode_Move)  //最后一个参数代表是否删除源，如果是move，则删除
                Unit
            },
        )

        val iconEnableList = listOf(
            {getSelectedFilesCount()>0},  //是否启用paste
        )

        if(!isLoading.value) {
            BottomBar(
                quitSelectionMode=filesPageQuitSelectionMode,
                iconList=iconList,
                iconTextList=iconTextList,
                iconDescTextList=iconTextList,
                iconOnClickList=iconOnClickList,
                iconEnableList=iconEnableList,
                enableMoreIcon=false,
                moreItemTextList= listOf(),
                moreItemOnClickList= listOf(),
                getSelectedFilesCount = getSelectedFilesCount,
                moreItemEnableList = listOf(),
                countNumOnClickEnabled = true,
                countNumOnClick = countNumOnClickForSelectAndPasteModeBottomBar
            )
        }

    }

//    if(showOverrideFilesDialog.value) {
//        ConfirmDialog(
//            title = stringResource(id = R.string.override),
//            requireShowTextCompose = true,
//            textCompose = {
//                Row {
//                    Text(text = stringResource(R.string.file_override_ask_text))
//                }
//              Column(modifier = Modifier
//                        .verticalScroll(rememberScrollState())
//              ) {
//
//                  fileNeedOverrideList.forEach {
//                      Row {
//                          Text(text = it.name+(if(it.isDirectory) File.separator else ""))  //如果是目录，后面加个 "/"
//                      }
//                  }
//
//              }
//            },
//            onCancel = { showOverrideFilesDialog.value=false }
//        ) {
//            //关闭弹窗
//            showOverrideFilesDialog.value=false
//            //执行删除
//            doJobThenOffLoading {
//                selectedItems.forEach {
//                    fileNeedOverrideList.forEach {
//                        val target = File(it.canonicalPath)
//                        if(target.exists()) {
//                            target.de  //这里本来想如果存在则删除，但是，不行，如果是目录的话，不行，得合并而不是覆盖！算了，如果重名直接自动重命名好了，覆盖太麻烦了
//                        }else {
//                            src.copyRecursively(target, false)  //false，禁用覆盖，不过，只有文件存在时才需要覆盖，而上面其实已经判断过了，所以执行到这，target肯定不存在，也用不着覆盖，但以防万一，这个值传false，避免错误覆盖文件
//                            if(pastMode.intValue == pastMode_Move) {  //如果是“移动(又名“剪切”)“，则删除源
//                                src.deleteRecursively()
//                            }
//                        }
//                    }
//
//                }
//
//                Msg.requireShow(appContext.getString(R.string.success))
//                //退出选择模式并刷新目录
//                filesPageQuitSelectionMode()
//                changeStateTriggerRefreshPage(needRefreshFilesPage)
//            }
//        }
//    }

    //有从标题页面请求执行的操作，执行一下
    //判断请求执行什么操作，然后执行
    if(filesPageRequestFromParent.value==PageRequest.goToTop) {
        PageRequest.clearStateThenDoAct(filesPageRequestFromParent) {
            UIHelper.scrollToItem(scope, curListState.value, 0)
        }
    }

    if(filesPageRequestFromParent.value==PageRequest.createFileOrFolder) {
        PageRequest.clearStateThenDoAct(filesPageRequestFromParent) {
            //显示新建文件或文件夹的弹窗，弹窗里可选择是创建文件还是文件夹
            createFileOrFolderErrMsg.value=""  //初始化错误信息为空
            showCreateFileOrFolderDialog.value = true  //显示弹窗
        }
    }

    //注：匹配带数据的request应该用startsWith
    if(filesPageRequestFromParent.value.startsWith(PageRequest.DataRequest.goToIndexWithDataSplit)) {
        val index = try {
            PageRequest.DataRequest.getDataFromRequest(filesPageRequestFromParent.value).toInt()
        }catch (e:Exception) {
            0
        }

        PageRequest.clearStateThenDoAct(filesPageRequestFromParent) {
            UIHelper.scrollToItem(scope, curListState.value, index)
        }
    }


    if(filesPageRequestFromParent.value==PageRequest.goToPath) {
        PageRequest.clearStateThenDoAct(filesPageRequestFromParent) {
//            显示弹窗，输入路径，跳转
            showGoToPathDialog.value = true
        }
    }

    if(filesPageRequestFromParent.value==PageRequest.copyPath) {
        PageRequest.clearStateThenDoAct(filesPageRequestFromParent) {
            copyPath(currentPath.value)
        }
    }

    if(filesPageRequestFromParent.value==PageRequest.copyRealPath) {
        PageRequest.clearStateThenDoAct(filesPageRequestFromParent) {
            copyRealPath(currentPath.value)
        }
    }
    if(filesPageRequestFromParent.value==PageRequest.goToInternalStorage) {
        PageRequest.clearStateThenDoAct(filesPageRequestFromParent) {
            goToPath(FsUtils.getInternalStorageRootPathNoEndsWithSeparator())
        }
    }
    if(filesPageRequestFromParent.value==PageRequest.goToExternalStorage) {
        PageRequest.clearStateThenDoAct(filesPageRequestFromParent) {
            goToPath(FsUtils.getExternalStorageRootPathNoEndsWithSeparator())
        }
    }


    LaunchedEffect(needRefreshFilesPage.value) {
        try {
            //只有当目录改变时(需要刷新页面)，才需要执行initFilesPage，选择文件之类的操作不需要执行此操作
            doInit(currentPath, currentPathFileList, currentPathBreadCrumbList,settingsSnapshot,
                filesPageGetFilterMode,
                filesPageFilterKeyword,
                curListState,
                getListState,
                loadingOn,
                loadingOff,
                appContext,
                requireImportFile=requireImportFile,
                requireImportUriList = requireImportUriList,
                filesPageQuitSelectionMode = filesPageQuitSelectionMode,
                isImportedMode = isImportMode,
                selecteItem=selecteItem,
                filesPageRequestFromParent = filesPageRequestFromParent,
                openDirErr=openDirErr,
                repoList=repoList,
            )


        } catch (cancel: Exception) {
//            ("LaunchedEffect: job cancelled")
        }
    }
}




private fun doInit(
    currentPath: MutableState<String>,
    currentPathFileList: CustomStateListSaveable<FileItemDto>,
    currentPathBreadCrumbList: CustomStateListSaveable<FileItemDto>,
    settingsSnapshot:CustomStateSaveable<AppSettings>,
    filesPageGetFilterModeOn:()->Int,
    filesPageFilterKeyword:CustomStateSaveable<TextFieldValue>,
    curListState: CustomStateSaveable<LazyListState>,
    getListState:(String)->LazyListState,
    loadingOn: (String) -> Unit,
    loadingOff: () -> Unit,
    appContext: Context,
    requireImportFile:MutableState<Boolean>,
    requireImportUriList: CustomStateListSaveable<Uri>,
    filesPageQuitSelectionMode:()->Unit,
    isImportedMode:MutableState<Boolean>,
    selecteItem:(FileItemDto) ->Unit,
    filesPageRequestFromParent:MutableState<String>,
    openDirErr:MutableState<String>,
    repoList:CustomStateListSaveable<RepoEntity>,
//    currentPathBreadCrumbList: MutableIntState,
//    currentPathBreadCrumbList1: SnapshotStateList<FileItemDto>,
//    currentPathBreadCrumbList2: SnapshotStateList<FileItemDto>
){
    doJobThenOffLoading(loadingOn, loadingOff, appContext.getString(R.string.loading)) {
        //如果路径为空，从配置文件读取上次打开的路径
        if(currentPath.value.isBlank()) {
            currentPath.value = settingsSnapshot.value.files.lastOpenedPath
        }

        //先清下列表，感觉在开头清比较好，如果加载慢，先白屏，然后出东西；放后面清的话，如果加载慢，会依然显示旧条目列表，感觉没变化，像卡了一样，用户可能重复点击
        currentPathFileList.value.clear()

        // make path canonical first, elst dir/ will return once dir, that make must press 2 times back for back to parent dir
        currentPath.value = File(currentPath.value).canonicalPath

        //更新当前目录的文件列表
        var currentDir = File(currentPath.value)
        val repoBaseDirPath = AppModel.singleInstanceHolder.allRepoParentDir.canonicalPath
        var currentFile:File? = null

        //无论对应文件是否存在，只要currentDir不是文件夹（其实也可能不是文件），都尝试取出其上级目录作为即将打开的目录
        // 之所以不判断文件是否存在是想在文件不存在时也尽量定位到其所在目录，不过只尝试定位一层上级目录，不会递归查找存在的上级目录，一层不存在就直接返回app根目录了
        if(!currentDir.isDirectory) {  //注：若文件不存在 isDirectory和isFile 都为假，所以这个判断并不能确定目标路径就一定是个存在的文件

            //当curpath不是文件夹时，尝试取出其上级，若是目录，则定位并有可能选中对应文件，若不是目录，则忽略，然后在后面的代码中会定位到app根目录
            val parent = currentDir.parentFile
            if(parent!=null && parent.exists() && parent.isDirectory) {
                currentFile = currentDir
                currentDir = parent
                currentPath.value = currentDir.canonicalPath
            }
        }

        //最终决定currentPath值的判断
        //xxx.contains(xxxxxx)判断是为了避免用户修改json文件中的lastOpenedPath越狱访问 allRepoParentDir 之外的目录
        //如果进入过上面的parent不等于null和是否目录和存在的判断，则执行到这里，只有判断路径是否越狱的条件可能为真
//        if(!currentDir.exists() || !currentDir.isDirectory || !currentDir.canonicalPath.startsWith(repoBaseDirPath)) {  //如果当前目录不存在，将路径设置为仓库根目录

        //because now(2024-09-23) support external path, so doesn't check startsWith repoBaseDirPath anymore
        if(!currentDir.exists() || !currentDir.isDirectory) {  //如果当前目录不存在，将路径设置为仓库根目录
//            Msg.requireShow(appContext.getString(R.string.invalid_path))  一边自己跳转到主页，一边提示无效path，产生一种主页是无效path的错觉，另人迷惑，故废弃

            currentFile=null  // 如果进入这个判断，currentFile已无意义，设为null，方便后面判断快速得到结果而不用继续比较path
            currentDir = File(repoBaseDirPath)
            currentPath.value = repoBaseDirPath
        }


        //执行到这，路径一定存在（要么路径存在，要么不存在被替换成了所有仓库的父目录，然后路径存在）
        //更新配置文件，避免卡顿，可开个协程，但其实没必要，因为最有可能造成卡顿的io操作其实已经放到协程里执行了
        SettingsUtil.update {  //这里并不是把页面的settingsSnapshot状态变量完全写入到配置文件，而是获取一份当下最新设置项的拷贝，然后修改我在这个代码块里修改的变量，再写入文件，所以，在这修改的设置项其实和页面的设置项可能会有出入，但我只需要currentPath关联的一个值而已，所以有差异也无所谓
            it.files.lastOpenedPath = currentPath.value
        }

        //按时间降序
        val comparator = { o1:FileItemDto, o2:FileItemDto -> if((o1.lastModifiedTimeInSec - o2.lastModifiedTimeInSec).toInt() > 0)  -1 else 1 }  //不能让比较器返回0，不然就等于“去重”了，就会少文件
        val fileSortedSet = sortedSetOf<FileItemDto>(comparator)
        val dirSortedSet = sortedSetOf<FileItemDto>(comparator)

        //注意，如果keyword为empty，正常来说不会进入搜索模式，不过如果进入，也会显示所有文件，因为任何字符串都包含空字符串
        //注意：只有当filterMode==2，也就是“显示根据关键字过滤的结果”的时候，才执行过滤，如果只是打开输入框，不会执行过滤。这个值不要改成不等于0，不然，打开输入框，输入内容，然后执行会触发刷新页面的操作（例如新建文件），就会执行过滤了，那样就会没点确定就开始过滤，会感觉有点混乱。比较好的交互逻辑是：要么就需要确认才过滤，要么就不需要确认边输入边过滤，不要两者混合。
        val isFilterModeOn = filesPageGetFilterModeOn() == 2
        val filterKeywordText = filesPageFilterKeyword.value.text

        // 当请求打开的curpath是个文件时会用到这几个变量 开始
        val needSelectFile = currentFile!=null && currentFile.exists()
        val curFilePath = currentFile?.canonicalPath
        var curFileFromCurPathAlreadySelected = false
        var curFileFromCurPathFileDto:FileItemDto? = null  //用来存匹配的dto，然后在列表查找index，然后滚动到指定位置
        // 当请求打开的curpath是个文件时会用到这几个变量 结束

        // 遍历文件列表
        currentDir.listFiles()?.let {
            it.forEach { file ->
                val fdto = FileItemDto.genFileItemDtoByFile(file, appContext)
                //过滤模式开启 且 文件名不包含关键字则忽略当前条目。（TODO 做点高级的东西？如果输入特定关键字就开启指定模式，比如可根据文件大小或后缀过滤？匹配模式直接抄Everything这个软件的即可，比自己发明格式要好，你自己发明的太小众，别人用不惯，已经广泛受用的，则有可能别人已经知道，用着就会感觉有亲切感）
//                if(isFilterModeOn && !fdto.name.lowercase().contains(filterKeywordText.lowercase())) {
//                    return@forEach
//                }

                if(fdto.isFile) {
                    //如果从请求打开的路径带来的文件存在，且和当前遍历的文件重合，选中
                    if(needSelectFile && !curFileFromCurPathAlreadySelected && curFilePath == fdto.fullPath) {
//                        清空已选中条目列表
                        filesPageQuitSelectionMode()
//                        把当前条目添加进已选中列表并开启选择模式
                        selecteItem(fdto)
                        curFileFromCurPathFileDto=fdto
                        //因为路径只有可能代表一个文件，所以此判断代码块只需执行一次，设置flag为已执行，这样下次就会跳过不必要的判断了
                        curFileFromCurPathAlreadySelected = true
                    }

                    fileSortedSet.add(fdto)
                }else {
                    dirSortedSet.add(fdto)
                }
            }
        }
//    println("dirSortedSet:"+dirSortedSet)
//    println("fileSortedSet:"+fileSortedSet)

        //清文件列表。（如果在开头清了，这里就不用再清了）
//    currentPathFileList.value.clear()
        //添加文件列表
        //一级顺序：文件夹在上，文件在下；二级顺序：各自按时间降序排列
        currentPathFileList.value.addAll(dirSortedSet)
        currentPathFileList.value.addAll(fileSortedSet)
        //恢复或新建当前路径的list state
        curListState.value = getListState(currentPath.value)
//    currentPathFileList.requireRefreshView()
        //当curpath是文件时，如果文件确实存在并且已选中，则跳转到对应文件的索引
        if(curFileFromCurPathAlreadySelected && curFileFromCurPathFileDto!=null) {
            var indexForScrollTo = currentPathFileList.value.indexOf(curFileFromCurPathFileDto)

            //这个判断是可选的，考虑下要不要注释掉
            //这个判断是为了使文件滚动时避免把选中条目放到最顶端，因为那样看着不太舒服，不过，这样有个弊端，如果当前选中条目的上一个条目文件名无敌长，那当前条目可能就被顶到下面的不可见范围了
            if(indexForScrollTo>0) {
                indexForScrollTo-=1
            }

            //下次渲染时请求滚动页面到对应条目
            filesPageRequestFromParent.value = PageRequest.DataRequest.build(PageRequest.DataRequest.goToIndexWithDataSplit, ""+indexForScrollTo)
        }

        //设置面包屑
        //每一层都显示自己的路径不就行了？

        //列出从仓库目录往后的目录，删掉前面的/storage/emu...之类的，返回结果形如[repo1,repo1Inside,otherDirs...]
//    currentPathStrList.addAll(getFilePathStrBasedRepoDir(curDirPath).split(File.separator))

        //这个函数其实只有在第一次进入Files页面且开启了记住上次退出路径的时候才有必要执行，这是一个针对当前路径初始化面包屑的操作，正常来说，更新面包屑不用执行这个函数，直接在点击文件夹时把路径添加到面包屑列表中即可
        //添加包含文件夹名和文件夹完整路径的对象到列表用来做面包屑路径导航

        //每次在这里更新面包屑其实也行，但不如点击目录时增量更新省事
        //值等于0，需要初始化
//    if(currentPathBreadCrumbList.intValue==0) {  //刚创建页面的时候，面包屑列表为空，需要初始化一下，后续由点击目录的onClick函数维护面包屑状态

//        val willUpdateList = if(currentPathBreadCrumbList.intValue==1) currentPathBreadCrumbList1 else currentPathBreadCrumbList2  //初始化时更新列表1，列表2由后续点击面包屑后的onClick函数更新
        val curDirPath = currentDir.canonicalPath
//        val isInternalStoragePath = curDirPath.startsWith(repoBaseDirPath)
//        val splitPath = (if(isInternalStoragePath) getFilePathStrBasedRepoDir(curDirPath) else curDirPath.removePrefix("/")).split(File.separator)  //获得一个分割后的目录列表
//        val root = if(isInternalStoragePath) repoBaseDirPath else "/"
        val splitPath = curDirPath.removePrefix("/").split(File.separator)  //获得一个分割后的目录列表
        val root = "/"
        currentPathBreadCrumbList.value.clear()  //避免和之前的路径拼接在一起，先清空下列表
//        if(splitPath.isNotEmpty()) {  //啥也没分割出来的话，就没必要填东西了，不过应该不会出现这种情况
        var lastPathName=StringBuilder()
        for(s in splitPath) {  //更新面包屑
            lastPathName.append(s).append(File.separator)  //拼接列表路径为仓库下的 完整相对路径
            val pathDto = FileItemDto()  //这里其实只需要 fullPath 和 name两个参数

            //breadCrumb must dir, if is file, will replace at up code
            //面包屑肯定是目录，如果是文件，在上面的代码中会被替换成目录
            pathDto.isFile=false
            pathDto.isDir=true

            pathDto.fullPath = File(root, lastPathName.toString()).canonicalPath  //把仓库下完整相对路径和仓库路径拼接，得到一个绝对路径
            pathDto.name = s
            currentPathBreadCrumbList.value.add(pathDto)
        }

//        currentPathBreadCrumbList.requireRefreshView()
//        }

//    }



        val repoDb = AppModel.singleInstanceHolder.dbContainer.repoRepository
        val listFromDb = repoDb.getReadyRepoList()

        repoList.value.clear()
        repoList.value.addAll(listFromDb)



        //检查是否请求打开文件
        if(requireImportFile.value) {
            requireImportFile.value = false
            if(requireImportUriList.value.isEmpty()) {
//                    不开启导入模式，提示下用户导入条目为空即可
                Msg.requireShow(appContext.getString(R.string.require_import_files_list_is_empty))
            }else {
                //退出选择模式
                filesPageQuitSelectionMode()
                //切换到导入模式，显示导入栏
                isImportedMode.value=true
            }
            //刷新页面，改了状态应该会自动刷新，不需再刷新
//            changeStateTriggerRefreshPage(needRefreshFilesPage)
        }


        // set err if has
        if(!File(currentPath.value).canRead()) {  // can't read dir, usually no permission for dir or dir doesn't exist
            openDirErr.value = appContext.getString(R.string.err_read_folder_failed)
        }else {
            openDirErr.value = ""
        }
    }

}

@Composable
private fun getBackHandler(
    appContext: Context,
    isFileSelectionMode: MutableState<Boolean>,
    filesPageQuitSelectionMode: () -> Unit,
    currentPath: MutableState<String>,
    allRepoParentDir: File,
    needRefreshFilesPage: MutableState<String>,
    exitApp: () -> Unit,
    getFilterMode:()->Int,
    filesPageFilterModeOff:()->Unit,
    filesPageSimpleFilterOn: MutableState<Boolean>,
    openDrawer:()->Unit

): () -> Unit {
    val backStartSec =  rememberSaveable { mutableLongStateOf(0) }
    val pressBackAgainForExitText = stringResource(R.string.press_back_again_to_exit);
    val showTextAndUpdateTimeForPressBackBtn = {
        openDrawer()
        showToast(appContext, pressBackAgainForExitText, Toast.LENGTH_SHORT)
        backStartSec.longValue = getSecFromTime() + Cons.pressBackDoubleTimesInThisSecWillExit
    }

    val backHandlerOnBack:()->Unit = {
        if(filesPageSimpleFilterOn.value) {
          filesPageSimpleFilterOn.value = false
        } else if (isFileSelectionMode.value) {
            filesPageQuitSelectionMode()
        }else if(getFilterMode() != 0) {
            filesPageFilterModeOff()
        }else if (currentPath.value.startsWith(FsUtils.getExternalStorageRootPathNoEndsWithSeparator()+"/")) { //如果在文件管理器页面且不在仓库根目录
            //返回上级目录
            currentPath.value = currentPath.value.substring(0, currentPath.value.lastIndexOf(File.separator))
            //刷新页面
            changeStateTriggerRefreshPage(needRefreshFilesPage)
        } else {
            //如果在两秒内按返回键，就会退出，否则会提示再按一次可退出程序
            if (backStartSec.longValue > 0 && getSecFromTime() <= backStartSec.longValue) {  //大于0说明不是第一次执行此方法，那检测是上次获取的秒数，否则直接显示“再按一次退出app”的提示
                exitApp()
            } else {
                showTextAndUpdateTimeForPressBackBtn()
            }
        }
    }
    return backHandlerOnBack
}
