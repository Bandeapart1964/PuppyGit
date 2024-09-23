package com.catpuppyapp.puppygit.screen

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Difference
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.VerticalAlignTop
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DrawerState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableIntState
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import com.catpuppyapp.puppygit.compose.FilterTextField
import com.catpuppyapp.puppygit.compose.LoadingDialog
import com.catpuppyapp.puppygit.compose.LongPressAbleIconBtn
import com.catpuppyapp.puppygit.compose.SmallFab
import com.catpuppyapp.puppygit.constants.Cons
import com.catpuppyapp.puppygit.constants.PageRequest
import com.catpuppyapp.puppygit.data.entity.RepoEntity
import com.catpuppyapp.puppygit.dto.FileItemDto
import com.catpuppyapp.puppygit.dto.FileSimpleDto
import com.catpuppyapp.puppygit.git.StatusTypeEntrySaver
import com.catpuppyapp.puppygit.play.pro.R
import com.catpuppyapp.puppygit.screen.content.homescreen.innerpage.AboutInnerPage
import com.catpuppyapp.puppygit.screen.content.homescreen.innerpage.ChangeListInnerPage
import com.catpuppyapp.puppygit.screen.content.homescreen.innerpage.EditorInnerPage
import com.catpuppyapp.puppygit.screen.content.homescreen.innerpage.FilesInnerPage
import com.catpuppyapp.puppygit.screen.content.homescreen.innerpage.RepoInnerPage
import com.catpuppyapp.puppygit.screen.content.homescreen.innerpage.SubscriptionPage
import com.catpuppyapp.puppygit.screen.content.homescreen.scaffold.actions.ChangeListPageActions
import com.catpuppyapp.puppygit.screen.content.homescreen.scaffold.actions.EditorPageActions
import com.catpuppyapp.puppygit.screen.content.homescreen.scaffold.actions.FilesPageActions
import com.catpuppyapp.puppygit.screen.content.homescreen.scaffold.actions.RepoPageActions
import com.catpuppyapp.puppygit.screen.content.homescreen.scaffold.actions.SubscriptionActions
import com.catpuppyapp.puppygit.screen.content.homescreen.scaffold.drawer.drawerContent
import com.catpuppyapp.puppygit.screen.content.homescreen.scaffold.title.AboutTitle
import com.catpuppyapp.puppygit.screen.content.homescreen.scaffold.title.ChangeListTitle
import com.catpuppyapp.puppygit.screen.content.homescreen.scaffold.title.EditorTitle
import com.catpuppyapp.puppygit.screen.content.homescreen.scaffold.title.FilesTitle
import com.catpuppyapp.puppygit.screen.content.homescreen.scaffold.title.ReposTitle
import com.catpuppyapp.puppygit.screen.content.homescreen.scaffold.title.SettingsTitle
import com.catpuppyapp.puppygit.screen.content.homescreen.scaffold.title.SimpleTitle
import com.catpuppyapp.puppygit.settings.SettingsCons
import com.catpuppyapp.puppygit.settings.SettingsUtil
import com.catpuppyapp.puppygit.style.MyStyleKt
import com.catpuppyapp.puppygit.utils.ActivityUtil
import com.catpuppyapp.puppygit.utils.AppModel
import com.catpuppyapp.puppygit.utils.FsUtils
import com.catpuppyapp.puppygit.utils.Msg
import com.catpuppyapp.puppygit.utils.MyLog
import com.catpuppyapp.puppygit.utils.UIHelper
import com.catpuppyapp.puppygit.utils.changeStateTriggerRefreshPage
import com.catpuppyapp.puppygit.utils.state.StateUtil
import com.github.git24j.core.Repository.StateT
import jp.kaleidot725.texteditor.state.TextEditorState
import jp.kaleidot725.texteditor.view.ScrollEvent
import kotlinx.coroutines.launch


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
//    context: Context,
//    navController: NavController,
    drawerState: DrawerState,
//    scope: CoroutineScope,
//    scrollBehavior: TopAppBarScrollBehavior,
    currentHomeScreen: MutableIntState,
    repoPageListState: LazyListState,
    editorPageLastFilePath: MutableState<String>,

//    filePageListState: LazyListState,
//    haptic: HapticFeedback,
) {
    //for debug
    val TAG = "HomeScreen"
    val stateKeyTag = "HomeScreen"


    val navController = AppModel.singleInstanceHolder.navController
    val scope = rememberCoroutineScope()
    val homeTopBarScrollBehavior = AppModel.singleInstanceHolder.homeTopBarScrollBehavior
//    val appContext = AppModel.singleInstanceHolder.appContext  //这个获取不了Activity!
    val appContext = LocalContext.current  //这个能获取到
    val activity = ActivityUtil.getCurrentActivity()

    val allRepoParentDir = AppModel.singleInstanceHolder.allRepoParentDir

    val settingsSnapshot = StateUtil.getCustomSaveableState(keyTag = stateKeyTag, keyName = "settingsSnapshot", initValue = SettingsUtil.getSettingsSnapshot())
    val showWelcomeToNewUser = StateUtil.getRememberSaveableState(initValue = false)
    val sheetState = StateUtil.getRememberModalBottomSheetState()
    val showBottomSheet = StateUtil.getRememberSaveableState(initValue = false)

    //替换成我的cusntomstateSaver，然后把所有实现parcellzier的类都取消实现parcellzier，改成用我的saver
//    val curRepo = rememberSaveable{ mutableStateOf(RepoEntity()) }
    val repoPageCurRepo = StateUtil.getCustomSaveableState(keyTag = stateKeyTag, keyName = "curRepo", initValue = RepoEntity(id=""))  //id=空，表示无效仓库
    //使用前检查，大于等于0才是有效索引
    val repoPageCurRepoIndex = StateUtil.getRememberIntState(initValue = -1)

//    val repoPageRepoList = StateUtil.getCustomSaveableState(
//        keyTag = stateKeyTag,
//        keyDesc = "repoPageRepoList",
//        initValue =mutableListOf<RepoEntity>()
//    )

    val repoPageRepoList = StateUtil.getCustomSaveableStateList(stateKeyTag, "repoPageRepoList", listOf<RepoEntity>())

    val changeListRefreshRequiredByParentPage= StateUtil.getRememberSaveableState(initValue = "")
    val changeListRequireRefreshFromParentPage = {
        //TODO 显示个loading遮罩啥的
        changeStateTriggerRefreshPage(changeListRefreshRequiredByParentPage)
    }
//    val changeListCurRepo = rememberSaveable{ mutableStateOf(RepoEntity()) }
    val changeListCurRepo = StateUtil.getCustomSaveableState(keyTag = stateKeyTag, keyName = "changeListCurRepo", initValue = RepoEntity(id=""))  //id=空，表示无效仓库
    val changeListIsShowRepoList = StateUtil.getRememberSaveableState(initValue = false)
    val changeListShowRepoList = {
        changeListIsShowRepoList.value=true
    }
    val changeListIsFileSelectionMode = StateUtil.getRememberSaveableState(initValue = false)
    val changeListPageNoRepo = StateUtil.getRememberSaveableState(initValue = false)
    val changeListPageHasNoConflictItems = StateUtil.getRememberSaveableState(initValue = false)
    val changeListPageRebaseCurOfAll = StateUtil.getRememberSaveableState(initValue = "")

    val changeListPageFilterKeyWord = StateUtil.getCustomSaveableState(
        keyTag = stateKeyTag,
        keyName = "changeListPageFilterKeyWord",
        initValue = TextFieldValue("")
    )
    val changeListPageFilterModeOn = StateUtil.getRememberSaveableState(initValue = false)

    val repoPageFilterKeyWord = StateUtil.getCustomSaveableState(
        keyTag = stateKeyTag,
        keyName = "repoPageFilterKeyWord",
        initValue = TextFieldValue("")
    )
    val repoPageFilterModeOn = StateUtil.getRememberSaveableState(initValue = false)
    val repoPageShowImportRepoDialog = StateUtil.getRememberSaveableState(initValue = false)

    val subscriptionPageNeedRefresh = StateUtil.getRememberSaveableState(initValue = "")

    val swapForChangeListPage = StateUtil.getRememberSaveableState(initValue = false)


//    val editorPageRequireOpenFilePath = StateUtil.getRememberSaveableState(initValue = "") // canonicalPath
//    val needRefreshFilesPage = rememberSaveable { mutableStateOf(false) }
    val needRefreshFilesPage = StateUtil.getRememberSaveableState(initValue = "")


    val filesPageIsFileSelectionMode = StateUtil.getRememberSaveableState(initValue = false)
    val filesPageIsPasteMode = StateUtil.getRememberSaveableState(initValue = false)
    val filesPageSelectedItems = StateUtil.getCustomSaveableStateList(keyTag = stateKeyTag, keyName = "filesPageSelectedItems", initValue = listOf<FileItemDto>())


    //这个filter有点重量级，比较适合做成全局搜索之类的功能
    val filesPageFilterMode = StateUtil.getRememberSaveableIntState(initValue = 0)  //0关闭，1正在搜索，显示输入框，2显示搜索结果
    val filesPageFilterKeyword = StateUtil.getCustomSaveableState(
        keyTag = stateKeyTag,
        keyName = "filesPageFilterKeyword",
        initValue = TextFieldValue("")
    )
    val filesPageFilterTextFieldFocusRequester = StateUtil.getRememberStateRawValue(initValue = FocusRequester())
    val filesPageFilterOn = {
        filesPageFilterMode.intValue = 1

        //若不为空，选中关键字
        val text = filesPageFilterKeyword.value.text
        if(text.isNotEmpty()) {
            filesPageFilterKeyword.value = filesPageFilterKeyword.value.copy(
                //这个TextRange，左闭右开，话说kotlin这一会左闭右闭一会左闭右开，有点难受
                selection = TextRange(0, text.length)
            )
        }
    }
    val filesPageFilterOff = {
        filesPageFilterMode.intValue = 0
        changeStateTriggerRefreshPage(needRefreshFilesPage)
    }
    val filesPageGetFilterMode = {
        filesPageFilterMode.intValue
    }
    val filesPageDoFilter= doFilter@{ keyWord:String ->
        //传参的话，优先使用，参数若为空，检查状态变量是否有数据，若有使用，若还是空，中止操作
        var needUpdateFieldState = true  //如果不是从状态变量获取的关键字，则更新状态变量为关键字

        var key = keyWord
        if(key.isEmpty()) {  //注意是empty，不要用blank，不然不能搜索空格，但文件名可能包含空格
            key = filesPageFilterKeyword.value.text
            if(key.isEmpty()) {
                Msg.requireShow(appContext.getString(R.string.keyword_is_empty))
                return@doFilter
            }

            needUpdateFieldState = false  //这时，key本身就是从state获取的，所以不用再更新state了
        }

        if(needUpdateFieldState){
            filesPageFilterKeyword.value = TextFieldValue(key)  //设置关键字
        }

        filesPageFilterMode.intValue=2  // 设置搜索模式为显示结果
        changeStateTriggerRefreshPage(needRefreshFilesPage)  //刷新页面
    }

    val filesPageScrollingDown = StateUtil.getRememberSaveableState(initValue = false)
    val filesPageListState = StateUtil.getCustomSaveableState(stateKeyTag, "filesPageListState", initValue = LazyListState(0,0))

    val filesPageSimpleFilterOn = StateUtil.getRememberSaveableState(initValue = false)
    val filesPageSimpleFilterKeyWord = StateUtil.getCustomSaveableState(
        keyTag = stateKeyTag,
        keyName = "filesPageSimpleFilterKeyWord",
        initValue = TextFieldValue("")
    )

    val filesPageCurrentPath = StateUtil.getRememberSaveableState(initValue = "")
    val showCreateFileOrFolderDialog = StateUtil.getRememberSaveableState(initValue = false)

    val showSetGlobalGitUsernameAndEmailDialog = StateUtil.getRememberSaveableState(initValue = false)


    val changelistFilterListState = StateUtil.getCustomSaveableState(
        keyTag = stateKeyTag,
        keyName = "changelistFilterListState"
    ) {
        LazyListState(0,0)
    }

    val filesFilterListState = StateUtil.getCustomSaveableState(
        keyTag = stateKeyTag,
        keyName = "filesFilterListState"
    ) {
        LazyListState(0,0)
    }
    val repoFilterListState = StateUtil.getCustomSaveableState(
        keyTag = stateKeyTag,
        keyName = "repoFilterListState"
    ) {
        LazyListState(0,0)
    }

    //当前展示的文件的canonicalPath
    val editorPageShowingFilePath = StateUtil.getRememberSaveableState(initValue = "")
    //当前展示的文件是否已经加载完毕
    val editorPageShowingFileIsReady = StateUtil.getRememberSaveableState(initValue = false)
    //TextEditor用的变量
    val editorPageTextEditorState = StateUtil.getCustomSaveableState(
        keyTag = stateKeyTag,
        keyName = "editorPageTextEditorState",
        initValue =TextEditorState.create("")
    )
    val editorPageShowSaveDoneToast = StateUtil.getRememberSaveableState(initValue = false)
//    val needRefreshEditorPage = rememberSaveable { mutableStateOf(false) }
    val needRefreshEditorPage = StateUtil.getRememberSaveableState(initValue = "")
    val editorPageIsSaving = StateUtil.getRememberSaveableState(initValue = false)
    val editorPageIsEdited = StateUtil.getRememberSaveableState(initValue = false)
    val showReloadDialog = StateUtil.getRememberSaveableState(initValue = false)

    val changeListHasIndexItems = StateUtil.getRememberSaveableState(initValue = false)
    val changeListRequirePull = StateUtil.getRememberSaveableState(initValue = false)
    val changeListRequirePush = StateUtil.getRememberSaveableState(initValue = false)
    val changeListRequireDoActFromParent = StateUtil.getRememberSaveableState(initValue = false)
    val changeListRequireDoActFromParentShowTextWhenDoingAct = StateUtil.getRememberSaveableState(initValue = "")
    val changeListEnableAction = StateUtil.getRememberSaveableState(initValue = true)
    val changeListCurRepoState = StateUtil.getRememberSaveableIntState(initValue = StateT.NONE.bit)  //初始状态是NONE，后面会在ChangeListInnerPage检查并更新状态，只要一创建innerpage或刷新（重新执行init），就会更新此状态
    val changeListPageFromTo = Cons.gitDiffFromIndexToWorktree
    val changeListPageItemList = StateUtil.getCustomSaveableStateList(keyTag = stateKeyTag, keyName = "changeListPageItemList", initValue = listOf<StatusTypeEntrySaver>())
    val changeListPageItemListState = StateUtil.getRememberLazyListState()
    val changeListPageSelectedItemList = StateUtil.getCustomSaveableStateList(keyTag = stateKeyTag, keyName = "changeListPageSelectedItemList", initValue = listOf<StatusTypeEntrySaver>())

    val changeListPageDropDownMenuItemOnClick={item:RepoEntity->
        //如果切换仓库，清空选中项列表
        if(changeListCurRepo.value.id != item.id) {
            changeListPageSelectedItemList.value.clear()
//            changeListPageSelectedItemList.requireRefreshView()
        }
        changeListCurRepo.value=item
        changeListRequireRefreshFromParentPage()
    }

    val filesPageRequireImportFile = StateUtil.getRememberSaveableState(initValue = false)
    val importListConsumed = StateUtil.getRememberSaveableState(initValue = false)  //此变量用来确保导入模式只启动一次，避免以导入模式进入app后，进入子页面再返回再次以导入模式进入Files页面
    val filesPageRequireImportUriList = StateUtil.getCustomSaveableStateList(keyTag = stateKeyTag, keyName = "filesPageRequireImportUriList", initValue = listOf<Uri>())
    val filesPageCurrentPathFileList = StateUtil.getCustomSaveableStateList(keyTag = stateKeyTag, keyName = "filesPageCurrentPathFileList", initValue = listOf<FileItemDto>()) //路径字符串，用路径分隔符分隔后的list
    val filesPageRequestFromParent = StateUtil.getRememberSaveableState(initValue = "")

    val initDone = StateUtil.getRememberSaveableState(initValue = false)
    val editorPageShowCloseDialog = StateUtil.getRememberSaveableState(initValue = false)

    val editorPageCloseDialogCallback = StateUtil.getCustomSaveableState(
        keyTag = stateKeyTag,
        keyName = "editorPageCloseDialogCallback",
        initValue = { requireSave:Boolean -> }
    )
    val initLoadingText = appContext.getString(R.string.loading)
    val loadingText = StateUtil.getRememberSaveableState(initValue = initLoadingText)

    val editorPageIsLoading = StateUtil.getRememberSaveableState(initValue = false)
    if(editorPageIsLoading.value) {
        LoadingDialog(loadingText.value)
    }
    val editorPageLoadingOn = {msg:String ->
        loadingText.value = msg
        editorPageIsLoading.value=true
//        Msg.requireShow(msg)
        //这里不需要请求这个东西，否则会无限闪屏
//        changeStateTriggerRefreshPage(needRefreshEditorPage)
    }
    val editorPageLoadingOff = {
        editorPageIsLoading.value=false
        loadingText.value = initLoadingText

//        changeStateTriggerRefreshPage(needRefreshEditorPage)
    }

    val editorPageRequestFromParent = StateUtil.getRememberSaveableState(initValue = "")
    val editorPageShowingFileDto = StateUtil.getCustomSaveableState(keyTag = stateKeyTag, keyName = "editorPageShowingFileDto") { FileSimpleDto() }
    val editorPageSnapshotedFileInfo = StateUtil.getCustomSaveableState(keyTag = stateKeyTag, keyName = "editorPageSnapshotedFileInfo") { FileSimpleDto() }
    val editorPageLastScrollEvent = StateUtil.getRememberState<ScrollEvent?>(initValue = null)  //这个用remember就行，没必要在显示配置改变时还保留这个滚动状态，如果显示配置改变，直接设为null，从配置文件读取滚动位置重定位更好
    val editorPageLazyListState = StateUtil.getRememberLazyListState()
    val editorPageIsInitDone = StateUtil.getRememberState(initValue = false)  //这个也用remember就行，无需在配置改变时保存此状态，直接重置成false就行
    val editorPageIsContentSnapshoted = StateUtil.getRememberState(initValue = false)  //是否已对当前内容创建了快照
    val editorPageSearchMode = StateUtil.getRememberState(initValue = false)
    val editorPageSearchKeyword = StateUtil.getCustomSaveableState(keyTag = stateKeyTag, keyName = "editorPageSearchKeyword") { TextFieldValue("") }
    val editorPageMergeMode = StateUtil.getRememberState(initValue = false)
    val editorReadOnlyMode = StateUtil.getRememberState(initValue = false)

    val settingsTmp = SettingsUtil.getSettingsSnapshot()  //避免状态变量里的设置项过旧，重新获取一个
    val editorShowLineNum = StateUtil.getRememberState(initValue = settingsTmp.editor.showLineNum)
    val editorLineNumFontSize = StateUtil.getRememberIntState(initValue = settingsTmp.editor.lineNumFontSize)
    val editorLastSavedLineNumFontSize = StateUtil.getRememberIntState(initValue = editorLineNumFontSize.intValue)  //用来检查，如果没变，就不执行保存，避免写入硬盘
    val editorFontSize = StateUtil.getRememberIntState(initValue = settingsTmp.editor.fontSize)
    val editorLastSavedFontSize = StateUtil.getRememberIntState(initValue = editorFontSize.intValue)
    val editorAdjustFontSizeMode = StateUtil.getRememberState(initValue = false)
    val editorAdjustLineNumFontSizeMode = StateUtil.getRememberState(initValue = false)
    val editorOpenFileErr = StateUtil.getRememberState(initValue = false)


    //给Files页面点击打开文件用的
    //第2个参数是期望值，只有当文件路径不属于app内置禁止edit的目录时才会使用那个值，否则强制开启readonly模式
    val requireInnerEditorOpenFile = { fullPath:String, expectReadOnly:Boolean ->
        editorPageShowingFileIsReady.value=false
        editorPageShowingFilePath.value = fullPath
        editorPageShowingFileDto.value.fullPath = ""
        currentHomeScreen.intValue = Cons.selectedItem_Editor

        editorPageMergeMode.value = false  //这个页面不负责打开ChangeList页面的diff条目，所以MergeMode状态直接初始化为关即可，用户需要用的时候打开文件后再手动开即可
        //如果路径是app内置禁止修改的目录，则强制开启readonly，否则使用入参值
        editorReadOnlyMode.value = if(FsUtils.isReadOnlyDir(fullPath)) true else expectReadOnly

        changeStateTriggerRefreshPage(needRefreshEditorPage)  //这个其实可有可无，因为一切换页面，组件会重建，必然会执行一次LaunchedEffect，也就起到了刷新的作用
    }

    val needRefreshRepoPage = StateUtil.getRememberSaveableState(initValue = "")
    val doSave: suspend () -> Unit = FsUtils.getDoSaveForEditor(
        editorPageShowingFilePath = editorPageShowingFilePath,
        editorPageLoadingOn = editorPageLoadingOn,
        editorPageLoadingOff = editorPageLoadingOff,
        appContext = appContext,
        editorPageIsSaving = editorPageIsSaving,
        needRefreshEditorPage = needRefreshEditorPage,
        editorPageTextEditorState = editorPageTextEditorState,
        pageTag = TAG,
        editorPageIsEdited = editorPageIsEdited,
        requestFromParent = editorPageRequestFromParent,
        editorPageFileDto = editorPageShowingFileDto,
        isSubPageMode = false,
        isContentSnapshoted = editorPageIsContentSnapshoted,
        snapshotedFileInfo = editorPageSnapshotedFileInfo
    )

    val goToFilesPage = {path:String ->
        filesPageCurrentPath.value = path
        currentHomeScreen.intValue = Cons.selectedItem_Files

        changeStateTriggerRefreshPage(needRefreshFilesPage)
    }

    val goToChangeListPage = { repoWillShowInChangeListPage: RepoEntity ->
        changeListCurRepo.value = repoWillShowInChangeListPage
        currentHomeScreen.intValue = Cons.selectedItem_ChangeList

        changeStateTriggerRefreshPage(changeListRefreshRequiredByParentPage)
    }

    val changelistPageScrollingDown = remember { mutableStateOf(false) }
    val repoPageScrollingDown = remember { mutableStateOf(false) }


    //用不到这段代码了，当初用这个是因为有些地方不能用Toast，后来发现直接withMainContext就可在任何地方用Toast了
    //这一堆判断只是为了确保这代码能被运行
    //不加会漏消息，妈的这个狗屁组件
//    if (needRefreshEditorPage.value != needRefreshFilesPage.value
//        || changeListRefreshRequiredByParentPage.value != needRefreshRepoPage.value
//    ) {
//        SideEffect {
//            MsgQueue.showAndClearAllMsg()
//        }
//    }

    val closeWelcome = {
        //关闭弹窗
        showWelcomeToNewUser.value=false

//        //更新设置项
//        val settingsWillSave = SettingsUtil.getSettingsSnapshot()
//        settingsWillSave.firstUse = false
//        SettingsUtil.updateSettings(settingsWillSave)  //更新配置文件
//
//        //更新设置项状态变量
//        settingsSnapshot.value=settingsWillSave  //更新当前页面的设置项为最新

        //更新配置文件并更新页面存储的app配置状态变量
        settingsSnapshot.value = SettingsUtil.update(true) {
            it.firstUse = false
        }!!
    }
    //显示欢迎弹窗
    if(showWelcomeToNewUser.value) {
        AlertDialog(
            title = {
                Text(stringResource(id = R.string.welcome))
            },
            text = {
                Column {
                    Row {
                        Text(text = stringResource(id = R.string.welcome)+"!")
                    }

                    Row (modifier = Modifier.padding(top = 10.dp)){
                        Text(text = stringResource(id = R.string.tips)+":"+ stringResource(R.string.try_long_press_icon_get_hints))
                    }
                }
            },
            //点击弹框外区域的时候触发此方法，一般设为和OnCancel一样的行为即可
            onDismissRequest = {closeWelcome()},
            dismissButton = {},  //禁用取消按钮，一个按钮就够了
            confirmButton = {
                TextButton(
                    enabled = true,
                    onClick = {
                        //执行用户传入的callback
                        closeWelcome()
                    },
                ) {
                    Text(
                        text = stringResource(id = R.string.ok),
                    )
                }
            },

            )

    }

//    if(editorPageShowSaveDoneToast.value) {
//        showToast(context = appContext, appContext.getString(R.string.file_saved), Toast.LENGTH_SHORT)
//        editorPageShowSaveDoneToast.value=false
//    }
    val drawTextList = listOf(
        stringResource(id = R.string.repos),
        stringResource(id = R.string.files),
        stringResource(id = R.string.editor),
        stringResource(id = R.string.changelist),
        stringResource(id = R.string.about),
//        stringResource(id = R.string.subscription),
    )
    val drawIdList = listOf(
        Cons.selectedItem_Repos,
        Cons.selectedItem_Files,
        Cons.selectedItem_Editor,
        Cons.selectedItem_ChangeList,
        Cons.selectedItem_About,
//        Cons.selectedItem_Subscription,
    )
    val drawIconList = listOf(
        Icons.Filled.Inventory,
        Icons.Filled.Folder,
        Icons.Filled.EditNote,
        Icons.Filled.Difference,
        Icons.Filled.Info,
//        Icons.Filled.Subscriptions
    )
    val refreshPageList = listOf(
        refreshRepoPage@{ changeStateTriggerRefreshPage(needRefreshRepoPage) },
        refreshFilesPage@{ changeStateTriggerRefreshPage(needRefreshFilesPage) },
        refreshEditorPage@{ editorPageShowingFileIsReady.value=false; changeStateTriggerRefreshPage(needRefreshEditorPage) },
        refreshChangeListPage@{changeListRequireRefreshFromParentPage()},
        refreshAboutPage@{}, //About页面静态的，不需要刷新
//        {},  //Subscription页面
    )

    val openDrawer = {  //打开侧栏(抽屉)
        scope.launch {
            drawerState.apply {
                if (isClosed) open()
            }
        }

        Unit
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(
                //侧栏菜单展开占屏幕宽度的比例
                //抽屉会过大或过小，然后闪烁一下变成目标宽度，会闪烁，不太好
//                modifier= if(drawerState.isOpen) Modifier.fillMaxWidth(.8F) else Modifier,
                //之前是250dp，显示不全广告，改成320了，正好能显示全
                modifier= Modifier
                    .fillMaxHeight()
                    .widthIn(max = 320.dp)
                    .verticalScroll(StateUtil.getRememberScrollState())
                ,
                drawerShape = RectangleShape,
                content = drawerContent(
                    currentHomeScreen = currentHomeScreen,
                    scope = scope,
                    drawerState = drawerState,
                    drawerItemShape = RectangleShape,
                    drawTextList = drawTextList,
                    drawIdList = drawIdList,
                    drawIconList = drawIconList,
                    refreshPageList = refreshPageList,
                    showExit = true
                )
            )
        },
    ) {

        Scaffold(
            modifier = Modifier.nestedScroll(homeTopBarScrollBehavior.nestedScrollConnection)
            ,
            topBar = {
                //TODO 这个东西也要根据选择哪个抽屉菜单条目而变化
                //TODO 要能在向上滚动时，隐藏这个topbar，向下滚动时，显示出来
                //TODO Editor时，在右隐藏侧栏显示文件名怎么样？
                TopAppBar(
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        titleContentColor = MaterialTheme.colorScheme.primary,
                    ),
                    title = {
                        //TODO 把app标题放到抽屉里，最好再有个长方形的背景图
                        if(currentHomeScreen.intValue == Cons.selectedItem_Repos){
                            if(repoPageFilterModeOn.value) {
                                FilterTextField(
                                    repoPageFilterKeyWord,
                                )
                            }else {
                                ReposTitle(repoPageListState, scope)
                            }
                        } else if(currentHomeScreen.intValue == Cons.selectedItem_Files){
                            FilesTitle(filesPageCurrentPath, allRepoParentDir, needRefreshFilesPage, filesPageGetFilterMode,
                                filesPageFilterKeyword, filesPageFilterOn,filesPageDoFilter, filesPageRequestFromParent,
                                filesPageFilterTextFieldFocusRequester, filesPageSimpleFilterOn.value, filesPageSimpleFilterKeyWord
                            )
                        } else if (currentHomeScreen.intValue == Cons.selectedItem_Editor) {
                            EditorTitle(editorPageShowingFilePath,editorPageRequestFromParent, editorPageSearchMode.value, editorPageSearchKeyword, editorPageMergeMode.value, editorReadOnlyMode.value, editorOpenFileErr.value)
                        } else if (currentHomeScreen.intValue == Cons.selectedItem_ChangeList) {
                            if(changeListPageFilterModeOn.value) {
                                FilterTextField(
                                    changeListPageFilterKeyWord,
                                )
                            }else{
                                ChangeListTitle(changeListCurRepo,changeListPageDropDownMenuItemOnClick, changeListCurRepoState, changeListIsFileSelectionMode,changeListPageItemListState, scope, changeListEnableAction.value)
                            }
                        } else if (currentHomeScreen.intValue == Cons.selectedItem_Settings) {
                            SettingsTitle()
                        } else if (currentHomeScreen.intValue == Cons.selectedItem_About) {
                            AboutTitle()
                        } else if(currentHomeScreen.intValue == Cons.selectedItem_Subscription) {
                            SimpleTitle(stringResource(R.string.subscription))
                        }else {
                            SimpleTitle()
                        }
                    },
                    navigationIcon = {
                        //如果是Files页面且开启过滤模式，则显示关闭按钮，否则显示菜单按钮
                        if(currentHomeScreen.intValue == Cons.selectedItem_Files
                            && (filesPageGetFilterMode() != 0 || filesPageSimpleFilterOn.value)
                        ) {
                            LongPressAbleIconBtn(
                                tooltipText = stringResource(R.string.close),
                                icon =  Icons.Filled.Close,
                                iconContentDesc = stringResource(R.string.close),

                            ) {
//                                filesPageFilterOff()
                                filesPageSimpleFilterOn.value = false
                            }
                        }else if(currentHomeScreen.intValue == Cons.selectedItem_ChangeList && changeListPageFilterModeOn.value){
                            LongPressAbleIconBtn(
                                tooltipText = stringResource(R.string.close),
                                icon =  Icons.Filled.Close,
                                iconContentDesc = stringResource(R.string.close),

                            ) {
                                changeListPageFilterModeOn.value=false
                            }
                        }else if(currentHomeScreen.intValue == Cons.selectedItem_Repos && repoPageFilterModeOn.value){
                            LongPressAbleIconBtn(
                                tooltipText = stringResource(R.string.close),
                                icon =  Icons.Filled.Close,
                                iconContentDesc = stringResource(R.string.close),

                            ) {
                                repoPageFilterModeOn.value=false
                            }
                        }else if(currentHomeScreen.intValue == Cons.selectedItem_Editor
                            && (editorPageSearchMode.value || editorAdjustFontSizeMode.value || editorAdjustLineNumFontSizeMode.value)
                        ){
                            LongPressAbleIconBtn(
                                tooltipText = stringResource(R.string.close),
                                icon =  Icons.Filled.Close,
                                iconContentDesc = stringResource(R.string.close),

                            ) {
                                if(editorPageSearchMode.value) {
                                    editorPageSearchMode.value = false
                                }else if(editorAdjustFontSizeMode.value) {
                                    editorPageRequestFromParent.value = PageRequest.requireSaveFontSizeAndQuitAdjust
                                }else if(editorAdjustLineNumFontSizeMode.value) {
                                    editorPageRequestFromParent.value = PageRequest.requireSaveLineNumFontSizeAndQuitAdjust
                                }
                            }
                        }else {
                            LongPressAbleIconBtn(
                                tooltipText = stringResource(R.string.menu),
                                icon = Icons.Filled.Menu,
                                iconContentDesc = stringResource(R.string.menu),
                            ) {
                                scope.launch {
                                    drawerState.apply {
                                        if (isClosed) open() else close()
                                    }
                                }
                            }

                        }

                    },
                    //TODO: IconButton不支持长按显示提示信息，我想让用户长按，手机震动，在按钮附近半透明显示按钮功能，就像安卓4.x那样，如果实现不了，必须确保“所有按钮，第一次按下，就能让用户知道这个按钮的功能，并且不会修改任何东西”
                    actions = {
                        if(currentHomeScreen.intValue == Cons.selectedItem_Repos) {
                            if(!repoPageFilterModeOn.value){
                                RepoPageActions(navController, repoPageCurRepo, showSetGlobalGitUsernameAndEmailDialog, needRefreshRepoPage,
                                    repoPageFilterModeOn, repoPageFilterKeyWord,
                                    showImportRepoDialog = repoPageShowImportRepoDialog
                                )
                            }
                        }else if(currentHomeScreen.intValue == Cons.selectedItem_Files) {
                            FilesPageActions(showCreateFileOrFolderDialog,
                                refreshPage = {
                                    changeStateTriggerRefreshPage(needRefreshFilesPage)
                                },
                                filterOn = filesPageFilterOn,
                                filesPageGetFilterMode,
                                filesPageDoFilter,
                                filesPageRequestFromParent,
                                filesPageSimpleFilterOn,
                                filesPageSimpleFilterKeyWord
                            )

                        }else if(currentHomeScreen.intValue == Cons.selectedItem_Editor && !editorOpenFileErr.value) {
                            EditorPageActions(
                                editorPageShowingFilePath,
//                                editorPageRequireOpenFilePath,
                                editorPageShowingFileIsReady,
                                needRefreshEditorPage,
                                editorPageTextEditorState,
//                                editorPageShowSaveDoneToast,
                                isSaving = editorPageIsSaving,
                                isEdited = editorPageIsEdited,
                                showReloadDialog = showReloadDialog,
                                showCloseDialog=editorPageShowCloseDialog,
                                closeDialogCallback = editorPageCloseDialogCallback,
//                                isLoading = editorPageIsLoading,
                                doSave = doSave,
                                loadingOn = editorPageLoadingOn,
                                loadingOff = editorPageLoadingOff,
                                editorPageRequest = editorPageRequestFromParent,
                                editorPageSearchMode=editorPageSearchMode,
                                editorPageMergeMode=editorPageMergeMode,
                                readOnlyMode = editorReadOnlyMode,
                                editorSearchKeyword = editorPageSearchKeyword.value.text,
                                isSubPageMode = false,

                                fontSize=editorFontSize,
                                lineNumFontSize=editorLineNumFontSize,
                                adjustFontSizeMode=editorAdjustFontSizeMode,
                                adjustLineNumFontSizeMode=editorAdjustLineNumFontSizeMode,
                                showLineNum = editorShowLineNum
                            )
                        }else if(currentHomeScreen.intValue == Cons.selectedItem_ChangeList) {
                            if(!changeListPageFilterModeOn.value){
                                ChangeListPageActions(
                                    changeListCurRepo,
                                    changeListRequireRefreshFromParentPage,
                                    changeListHasIndexItems,
    //                                requirePull = changeListRequirePull,
    //                                requirePush = changeListRequirePush,
                                    changeListRequireDoActFromParent,
                                    changeListRequireDoActFromParentShowTextWhenDoingAct,
                                    changeListEnableAction,
                                    changeListCurRepoState,
                                    fromTo = changeListPageFromTo,
                                    changeListPageItemListState,
                                    scope,
                                    changeListPageNoRepo=changeListPageNoRepo,
                                    hasNoConflictItems = changeListPageHasNoConflictItems.value,
                                    changeListPageFilterModeOn= changeListPageFilterModeOn,
                                    changeListPageFilterKeyWord=changeListPageFilterKeyWord,
                                    rebaseCurOfAll = changeListPageRebaseCurOfAll.value
                                )

                            }
                        }else if(currentHomeScreen.intValue == Cons.selectedItem_Settings) {

                        }else if(currentHomeScreen.intValue == Cons.selectedItem_Subscription) {
                            SubscriptionActions { // refreshPage
                                changeStateTriggerRefreshPage(subscriptionPageNeedRefresh)
                            }
                        }
                    },
                    scrollBehavior = homeTopBarScrollBehavior,
                )
            },
            floatingActionButton = {
                if(currentHomeScreen.intValue == Cons.selectedItem_Editor && (editorPageShowingFileIsReady.value && editorPageShowingFilePath.value.isNotBlank() && editorPageIsEdited.value && !editorPageIsSaving.value && !editorReadOnlyMode.value)) {
                    SmallFab(modifier=MyStyleKt.Fab.getFabModifierForEditor(editorPageTextEditorState.value.isMultipleSelectionMode),
                        icon = Icons.Filled.Save, iconDesc = stringResource(id = R.string.save)
                    ) {
                        editorPageRequestFromParent.value = PageRequest.requireSave
                    }
                }else if(currentHomeScreen.intValue == Cons.selectedItem_ChangeList && changelistPageScrollingDown.value) {
                    //向下滑动时显示go to top按钮
                    SmallFab(
                        modifier = MyStyleKt.Fab.getFabModifier(),
                        icon = Icons.Filled.VerticalAlignTop, iconDesc = stringResource(id = R.string.go_to_top)
                    ) {
                        if(changeListPageFilterModeOn.value) {
                            UIHelper.scrollToItem(scope, changelistFilterListState.value, 0)
                        }else{
                            UIHelper.scrollToItem(scope, changeListPageItemListState, 0)
                        }
                    }
                }else if(currentHomeScreen.intValue == Cons.selectedItem_Repos && repoPageScrollingDown.value) {
                    //向下滑动时显示go to top按钮
                    SmallFab(
                        modifier = MyStyleKt.Fab.getFabModifier(),
                        icon = Icons.Filled.VerticalAlignTop, iconDesc = stringResource(id = R.string.go_to_top)
                    ) {
                        if(repoPageFilterModeOn.value) {
                            UIHelper.scrollToItem(scope, repoFilterListState.value, 0)
                        }else{
                            UIHelper.scrollToItem(scope, repoPageListState, 0)
                        }
                    }
                }else if(currentHomeScreen.intValue == Cons.selectedItem_Files && filesPageScrollingDown.value) {
                    //向下滑动时显示go to top按钮
                    SmallFab(
                        modifier = MyStyleKt.Fab.getFabModifier(),
                        icon = Icons.Filled.VerticalAlignTop, iconDesc = stringResource(id = R.string.go_to_top)
                    ) {
                        if(filesPageSimpleFilterOn.value) {
                            UIHelper.scrollToItem(scope, filesFilterListState.value, 0)
                        }else{
                            UIHelper.scrollToItem(scope, filesPageListState.value, 0)
                        }
                    }
                }
            }
        ) { contentPadding ->
            if(currentHomeScreen.intValue == Cons.selectedItem_Repos) {
//                changeStateTriggerRefreshPage(needRefreshRepoPage)
                RepoInnerPage(
                    showBottomSheet,
                    sheetState,
                    repoPageCurRepo,
                    repoPageCurRepoIndex,
                    contentPadding,
                    repoPageListState,
                    showSetGlobalGitUsernameAndEmailDialog,
                    needRefreshRepoPage,
                    changeListCurRepo=changeListCurRepo,
                    currentHomeScreen=currentHomeScreen,
                    changeListNeedRefresh=changeListRefreshRequiredByParentPage,
                    repoPageRepoList,
                    filesPageCurrentPath=filesPageCurrentPath,
                    filesPageNeedRefresh=needRefreshFilesPage,
                    goToFilesPage = goToFilesPage,
                    goToChangeListPage = goToChangeListPage,
                    repoPageScrollingDown=repoPageScrollingDown,
                    repoPageFilterModeOn=repoPageFilterModeOn,
                    repoPageFilterKeyWord= repoPageFilterKeyWord,
                    filterListState = repoFilterListState,
                    openDrawer = openDrawer,
                    showImportRepoDialog = repoPageShowImportRepoDialog
                )

            }
            else if(currentHomeScreen.intValue== Cons.selectedItem_Files) {
//                changeStateTriggerRefreshPage(needRefreshFilesPage)
                FilesInnerPage(
                    contentPadding = contentPadding,
//                    filePageListState = filePageListState,
                    currentHomeScreen=currentHomeScreen,
                    editorPageShowingFilePath = editorPageShowingFilePath,
                    editorPageShowingFileIsReady = editorPageShowingFileIsReady,
                    needRefreshFilesPage = needRefreshFilesPage,
                    currentPath=filesPageCurrentPath,
                    showCreateFileOrFolderDialog=showCreateFileOrFolderDialog,
                    requireImportFile=filesPageRequireImportFile,
                    requireImportUriList=filesPageRequireImportUriList,
                    filesPageGetFilterMode=filesPageGetFilterMode,
                    filesPageFilterKeyword=filesPageFilterKeyword,
                    filesPageFilterOff,
                    filesPageCurrentPathFileList,
                    filesPageRequestFromParent,
                    requireInnerEditorOpenFile,
                    filesPageSimpleFilterOn,
                    filesPageSimpleFilterKeyWord,
                    filesPageScrollingDown,
                    filesPageListState,
                    filterListState = filesFilterListState,
                    openDrawer = openDrawer,
                    isFileSelectionMode= filesPageIsFileSelectionMode,
                    isPasteMode = filesPageIsPasteMode,
                    selectedItems = filesPageSelectedItems,
                )
            }
            else if(currentHomeScreen.intValue == Cons.selectedItem_Editor) {
//                changeStateTriggerRefreshPage(needRefreshEditorPage)

                EditorInnerPage(
                    contentPadding = contentPadding,
                    currentHomeScreen = currentHomeScreen,
//                    editorPageRequireOpenFilePath=editorPageRequireOpenFilePath,
                    editorPageShowingFilePath=editorPageShowingFilePath,
                    editorPageShowingFileIsReady=editorPageShowingFileIsReady,
                    editorPageTextEditorState=editorPageTextEditorState,
//                    editorPageShowSaveDoneToast=editorPageShowSaveDoneToast,
                    needRefreshEditorPage=needRefreshEditorPage,
                    isSaving = editorPageIsSaving,
                    isEdited = editorPageIsEdited,
                    showReloadDialog=showReloadDialog,
                    isSubPageMode = false,
                    showCloseDialog = editorPageShowCloseDialog,
                    closeDialogCallback = editorPageCloseDialogCallback,
//                    isLoading = editorPageIsLoading,
                    loadingOn = editorPageLoadingOn,
                    loadingOff = editorPageLoadingOff,
                    saveOnDispose = true,  //销毁时保存处理起来比切换抽屉处理要好实现一些，所以在这里请求销毁时保存，但如果是子页面之类比较好处理的情况，这个值应该传false，由父页面负责保存
                    doSave=doSave,
                    naviUp = {},  //当前页面是一级页面，不需要传naviUp所有逻辑都包含在backhandler里了；二级页面(子页面)才需要传naviUp，用来在按返回箭头时保存再返回
                    requestFromParent = editorPageRequestFromParent,
                    editorPageShowingFileDto,
                    editorPageLastFilePath,
                    editorPageLastScrollEvent,
                    editorPageLazyListState,
                    editorPageIsInitDone,
                    editorPageIsContentSnapshoted,
                    goToFilesPage,
                    drawerState,
                    editorSearchMode = editorPageSearchMode,
                    editorSearchKeyword = editorPageSearchKeyword,
                    readOnlyMode = editorReadOnlyMode,
                    editorMergeMode = editorPageMergeMode,
                    editorShowLineNum=editorShowLineNum,
                    editorLineNumFontSize=editorLineNumFontSize,
                    editorFontSize=editorFontSize,
                    editorAdjustLineNumFontSizeMode = editorAdjustLineNumFontSizeMode,
                    editorAdjustFontSizeMode = editorAdjustFontSizeMode,
                    editorLastSavedLineNumFontSize = editorLastSavedLineNumFontSize,
                    editorLastSavedFontSize = editorLastSavedFontSize,
                    openDrawer = openDrawer,
                    editorOpenFileErr = editorOpenFileErr

                )

            }
            else if(currentHomeScreen.intValue == Cons.selectedItem_ChangeList) {
//                val commit1OidStr = rememberSaveable { mutableStateOf("") }
//                val commitParentList = remember { mutableStateListOf<String>() }
//                changeListRequireRefreshFromParentPage()

                //从抽屉菜单打开的changelist是对比worktree和index的文件，所以from是worktree
                ChangeListInnerPage(
                    contentPadding,
                    fromTo = changeListPageFromTo,
                    changeListCurRepo,
                    changeListIsFileSelectionMode,

                    changeListRefreshRequiredByParentPage,
                    changeListHasIndexItems,
//                    requirePullFromParentPage = changeListRequirePull,
//                    requirePushFromParentPage = changeListRequirePush,
                    changeListRequireDoActFromParent,
                    changeListRequireDoActFromParentShowTextWhenDoingAct,
                    changeListEnableAction,
                    changeListCurRepoState,
                    naviUp = {},  //主页来的，不用naviUp只用exitApp，所以这里随便填就行
                    itemList = changeListPageItemList,
                    itemListState = changeListPageItemListState,
                    selectedItemList = changeListPageSelectedItemList,
//                    commit1OidStr=commit1OidStr,
//                    commitParentList=commitParentList,
                    changeListPageNoRepo=changeListPageNoRepo,
                    hasNoConflictItems = changeListPageHasNoConflictItems,
                    goToFilesPage = goToFilesPage,
                    changelistPageScrollingDown=changelistPageScrollingDown,
                    changeListPageFilterModeOn= changeListPageFilterModeOn,
                    changeListPageFilterKeyWord=changeListPageFilterKeyWord,
                    filterListState = changelistFilterListState,
                    swap=swapForChangeListPage.value,
                    commitForQueryParents = "",
                    rebaseCurOfAll=changeListPageRebaseCurOfAll,
                    openDrawer = openDrawer

//                    refreshRepoPage = { changeStateTriggerRefreshPage(needRefreshRepoPage) }
                )
                //改用dropdwonmenu了
//                if(changeListIsShowRepoList.value) {
//                    RepoListDialog(curSelectedRepo = changeListCurRepo,
//                        itemOnClick={item:RepoEntity->
//
//
//                        },
//                        onClose={changeListIsShowRepoList.value=false})
//                }
            }else if(currentHomeScreen.intValue == Cons.selectedItem_Settings) {
                Column(modifier = Modifier.padding(contentPadding)) {
                    Text(text = "Settings Page")
                }
            }else if(currentHomeScreen.intValue == Cons.selectedItem_About) {
                //About页面是静态的，无需刷新
                AboutInnerPage(contentPadding, openDrawer = openDrawer)
            }else if(currentHomeScreen.intValue == Cons.selectedItem_Subscription) {
                SubscriptionPage(contentPadding = contentPadding, needRefresh = subscriptionPageNeedRefresh, openDrawer = openDrawer)
            }
        }
    }

    //第一个变量用来控制第一次加载页面时，不会更新配置文件，不然就覆盖成currentPage状态变量的初始值了，
    // 第2个变量让负责渲染页面的线程知道我要用currentPage那个变量，以确保currentPage变化时能执行下面的代码块
    //ps: currentPage.intValue > 0可替换成别的，只要用到currentPage这个状态变量且永远为真即可。
    //ps: initDone 的值应该在初始化完之后就不要再更新了，一直为true即可，这样每次重新渲染页面都检查currentPage是否变化，如果变化就会保存到配置文件了
    if(initDone.value && currentHomeScreen.intValue != Cons.selectedItem_Never && currentHomeScreen.intValue != Cons.selectedItem_Exit) {
        SideEffect {
            //检查下，如果页面变了，并且配置文件里设置为“记住上次退出页面”，就更新配置文件
            if(settingsSnapshot.value.startPageMode == SettingsCons.startPageMode_rememberLastQuit) {  // is remember last quit mode
                val curHomeScreenVal = currentHomeScreen.intValue
//                if(debugModeOn) {
//                    println("curPageVal before update:"+curHomeScreenVal)
//                    println("settingsSnapshot.value.lastQuitPage:"+settingsSnapshot.value.lastQuitHomeScreen)
//                }
                if(curHomeScreenVal != settingsSnapshot.value.lastQuitHomeScreen) {  //currentHomeScreen not same with lastQuitPage, need update
                    //先更新下状态值，避免重复进入此代码块 (或许不能完全避免，不过问题不大，顶多重复更新下配置文件)
                    settingsSnapshot.value.lastQuitHomeScreen = curHomeScreenVal

                    //保存最后切换的页面，然后返回最新配置文件快照
                    settingsSnapshot.value = SettingsUtil.update(requireReturnSnapshotOfUpdatedSettings = true) {
                        it.lastQuitHomeScreen = curHomeScreenVal
                    }!!
                }
            }
        }

    }

    //compose创建时的副作用
//    LaunchedEffect(currentPage.intValue) {
    LaunchedEffect(Unit) {
        //test
//        delay(30*1000)
//        throw RuntimeException("test save when exception")  // passed, it can save when exception threw, even in android 8, still worked like a charm
        //test

        try {
            //检查是否初次使用，若是，显示欢迎弹窗
            if (settingsSnapshot.value.firstUse) {
                showWelcomeToNewUser.value = true
            }
            //恢复上次退出页面
            val startPageMode = settingsSnapshot.value.startPageMode
            if (startPageMode == SettingsCons.startPageMode_rememberLastQuit) {
//                if (debugModeOn) {
//                    println("startPageMode:" + startPageMode)
//                    println("settingsSnapshot.value.lastQuitPage:" + settingsSnapshot.value.lastQuitHomeScreen)
//                }

                //从配置文件恢复上次退出页面
                currentHomeScreen.intValue = settingsSnapshot.value.lastQuitHomeScreen
                //设置初始化完成，之后就会通过更新页面值的代码来在页面值变化时更新配置文件中的值了
                initDone.value = true
            }


            //检查是否存在intent，如果存在，则切换到导入模式
            if (activity != null) {
                val intent = activity.intent
                if (intent != null) {
                    val extras = intent.extras

                    //importListConsumed 确保每个Activity的文件列表只被消费一次(20240706: 修复以导入模式启动app再进入子页面再返回会再次触发导入模式的bug)
                    if (extras != null && !importListConsumed.value) {
                        //20240706: fixed: if import mode in app then go to any sub page then back, will duplicate import files list
                        //20240706: 修复以导入模式启动app后跳转到任意子页面再返回，导入文件列表重复的bug
                        filesPageRequireImportUriList.value.clear()

                        //获取单文件，对应 action SEND
                        val uri = try {
                            extras.getParcelable<Uri>(Intent.EXTRA_STREAM)  //如果没条目，会警告并打印异常信息，然后返回null
                        } catch (e: Exception) {
                            null
                        }
                        if (uri != null) {
                            filesPageRequireImportUriList.value.add(uri)
                        }

                        //获取多文件，对应 action SEND_MULTIPLE
                        val uriList = try {
                            extras.getParcelableArrayList<Uri>(Intent.EXTRA_STREAM) ?: listOf()
                        } catch (e: Exception) {
                            listOf()
                        }
                        if (uriList.isNotEmpty()) {
                            filesPageRequireImportUriList.value.addAll(uriList)
                        }

                        if (filesPageRequireImportUriList.value.isNotEmpty()) {
                            //把导入文件列表设为已消费，确保导入模式只启动一次，避免由导入模式启动的Activity离开Files页面后再返回再次显示导入栏
                            importListConsumed.value = true

                            //请求Files页面导入文件
                            filesPageRequireImportFile.value = true
                            currentHomeScreen.intValue = Cons.selectedItem_Files  //跳转到Files页面
                        }
                    }
                }
            }
//            if(currentPage.intValue == Cons.selectedItem_Repos) {
//            }else if(currentPage.intValue == Cons.selectedItem_Files) {
//            }else if(currentPage.intValue == Cons.selectedItem_Editor) {
//            }else if(currentPage.intValue == Cons.selectedItem_ChangeList) {
//
//            }else if(currentPage.intValue == Cons.selectedItem_Settings) {
//            }
        } catch (e: Exception) {
            MyLog.e(TAG, "#LaunchedEffect err: "+e.stackTraceToString())
            Msg.requireShowLongDuration("init home err: "+e.localizedMessage)
        }
    }


    //compose被销毁时执行的副作用
    DisposableEffect(Unit) {
//        ("DisposableEffect: entered main")
        onDispose {
//            ("DisposableEffect: exited main")
        }
    }

}



