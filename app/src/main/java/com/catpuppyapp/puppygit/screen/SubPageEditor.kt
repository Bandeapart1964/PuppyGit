package com.catpuppyapp.puppygit.screen

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.TextFieldValue
import com.catpuppyapp.puppygit.compose.LoadingDialog
import com.catpuppyapp.puppygit.compose.LongPressAbleIconBtn
import com.catpuppyapp.puppygit.compose.SmallFab
import com.catpuppyapp.puppygit.constants.Cons
import com.catpuppyapp.puppygit.constants.PageRequest
import com.catpuppyapp.puppygit.dev.dev_EnableUnTestedFeature
import com.catpuppyapp.puppygit.dev.editorMergeModeTestPassed
import com.catpuppyapp.puppygit.dto.FileSimpleDto
import com.catpuppyapp.puppygit.play.pro.R
import com.catpuppyapp.puppygit.screen.content.homescreen.innerpage.EditorInnerPage
import com.catpuppyapp.puppygit.screen.content.homescreen.scaffold.actions.EditorPageActions
import com.catpuppyapp.puppygit.screen.content.homescreen.scaffold.title.EditorTitle
import com.catpuppyapp.puppygit.settings.SettingsUtil
import com.catpuppyapp.puppygit.style.MyStyleKt
import com.catpuppyapp.puppygit.user.UserUtil
import com.catpuppyapp.puppygit.utils.AppModel
import com.catpuppyapp.puppygit.utils.FsUtils
import com.catpuppyapp.puppygit.utils.cache.Cache
import com.catpuppyapp.puppygit.utils.doJobThenOffLoading
import com.catpuppyapp.puppygit.utils.state.StateUtil
import jp.kaleidot725.texteditor.state.TextEditorState
import jp.kaleidot725.texteditor.view.ScrollEvent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext


//子页面版本editor
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SubPageEditor(
//    context: Context,
//    navController: NavController,
//    drawerState: DrawerState,
//    scope: CoroutineScope,
//    scrollBehavior: TopAppBarScrollBehavior,
//    currentPage: MutableIntState,
//    repoPageListState: LazyListState,
//    filePageListState: LazyListState,
//    haptic: HapticFeedback,
    filePathKey:String,
    goToLine:Int,  //大于0，打开文件定位到对应行，小于0，打开文件定位上次编辑行（之前的逻辑不变
    initMergeMode:Boolean,
    initReadOnly:Boolean,
    editorPageLastFilePath:MutableState<String>,
    naviUp:()->Unit
) {
    //for debug
    val TAG = "SubPageEditor"
    val stateKeyTag = "SubPageEditor"

//    val isTimeNaviUp = rememberSaveable { mutableStateOf(false) }
//    if(isTimeNaviUp.value) {
//        naviUp()
//    }



    val navController = AppModel.singleInstanceHolder.navController
    val homeTopBarScrollBehavior = AppModel.singleInstanceHolder.homeTopBarScrollBehavior
//    val appContext = AppModel.singleInstanceHolder.appContext  //这个获取不了Activity!
    val appContext = LocalContext.current  //这个能获取到

    val allRepoParentDir = AppModel.singleInstanceHolder.allRepoParentDir


//    val changeListRefreshRequiredByParentPage= StateUtil.getRememberSaveableState(initValue = "")
//    val changeListRequireRefreshFromParentPage = {
//        //TODO 显示个loading遮罩啥的
//        changeStateTriggerRefreshPage(changeListRefreshRequiredByParentPage)
//    }
//    val changeListCurRepo = rememberSaveable{ mutableStateOf(RepoEntity()) }


    // canonicalPath
//    val editorPageRequireOpenFilePath = StateUtil.getRememberSaveableState(initValue = (Cache.getByTypeThenDel<String>(filePathKey))?:"")
//    val needRefreshFilesPage = rememberSaveable { mutableStateOf(false) }

    val editorPageShowingFilePath = StateUtil.getRememberSaveableState(initValue = (Cache.getByTypeThenDel<String>(filePathKey))?:"") //当前展示的文件的canonicalPath
    val editorPageShowingFileIsReady = StateUtil.getRememberSaveableState(initValue =false) //当前展示的文件是否已经加载完毕
    //TextEditor用的变量
    val editorPageTextEditorState = StateUtil.getCustomSaveableState(
        keyTag = stateKeyTag,
        keyName = "editorPageTextEditorState",
        initValue = TextEditorState.create("")
    )
    val needRefreshEditorPage = StateUtil.getRememberSaveableState(initValue ="")
    val editorPageIsSaving = StateUtil.getRememberSaveableState(initValue =false)
    val editorPageIsEdited = StateUtil.getRememberSaveableState(initValue =false)
    val showReloadDialog = StateUtil.getRememberSaveableState(initValue =false)
    val editorPageShowingFileDto = StateUtil.getCustomSaveableState(keyTag = stateKeyTag, keyName = "editorPageShowingFileDto") { FileSimpleDto() }
    val editorPageSnapshotedFileInfo = StateUtil.getCustomSaveableState(keyTag = stateKeyTag, keyName = "editorPageSnapshotedFileInfo") { FileSimpleDto() }

    val editorPageLastScrollEvent = StateUtil.getRememberState<ScrollEvent?>(initValue = null)  //这个用remember就行，没必要在显示配置改变时还保留这个滚动状态，如果显示配置改变，直接设为null，从配置文件读取滚动位置重定位更好
    val editorPageLazyListState = StateUtil.getRememberLazyListState()
    val editorPageIsInitDone = StateUtil.getRememberState(initValue = false)  //这个也用remember就行，无需在配置改变时保存此状态，直接重置成false就行
    val editorPageIsContentSnapshoted = StateUtil.getRememberState(initValue = false)  //是否已对当前内容创建了快照
    val editorPageSearchMode = StateUtil.getRememberState(initValue = false)
    val editorPageSearchKeyword = StateUtil.getCustomSaveableState(keyTag = stateKeyTag, keyName = "editorPageSearchKeyword") { TextFieldValue("") }
    val editorReadOnlyMode = StateUtil.getRememberState(initValue = initReadOnly)

    //如果用户pro且功能测试通过，允许使用url传来的初始值，否则一律false
    val editorPageMergeMode = StateUtil.getRememberState(initValue = if(UserUtil.isPro() && (dev_EnableUnTestedFeature || editorMergeModeTestPassed)) initMergeMode else false)


    val settingsTmp = SettingsUtil.getSettingsSnapshot()  //避免状态变量里的设置项过旧，重新获取一个
    val editorShowLineNum = StateUtil.getRememberState(initValue = settingsTmp.editor.showLineNum)
    val editorLineNumFontSize = StateUtil.getRememberIntState(initValue = settingsTmp.editor.lineNumFontSize)
    val editorFontSize = StateUtil.getRememberIntState(initValue = settingsTmp.editor.fontSize)
    val editorAdjustFontSizeMode = StateUtil.getRememberState(initValue = false)
    val editorAdjustLineNumFontSizeMode = StateUtil.getRememberState(initValue = false)
    val editorLastSavedLineNumFontSize = StateUtil.getRememberIntState(initValue = editorLineNumFontSize.intValue)  //用来检查，如果没变，就不执行保存，避免写入硬盘
    val editorLastSavedFontSize = StateUtil.getRememberIntState(initValue = editorFontSize.intValue)
    val editorOpenFileErr = StateUtil.getRememberState(initValue = false)


    val showCloseDialog = StateUtil.getRememberSaveableState(initValue =false)
    val editorPageRequestFromParent = StateUtil.getRememberSaveableState(initValue = "")

    val closeDialogCallback = StateUtil.getCustomSaveableState<(Boolean)->Unit>(
        keyTag = stateKeyTag,
        keyName = "closeDialogCallback",
        initValue = { requireSave:Boolean -> Unit}
    )

    val initLoadingText = appContext.getString(R.string.loading)
    val loadingText = StateUtil.getRememberSaveableState(initValue = initLoadingText)
    val isLoading = StateUtil.getRememberSaveableState(initValue =false)

    val loadingOn = {msg:String ->
        loadingText.value=msg
        isLoading.value=true
//        Msg.requireShow(msg)
//        changeStateTriggerRefreshPage(needRefreshEditorPage)
    }
    val loadingOff = {
        isLoading.value=false
        loadingText.value=initLoadingText
//        changeStateTriggerRefreshPage(needRefreshEditorPage)
    }

    if(isLoading.value) {
        LoadingDialog(loadingText.value)
    }

    val doSave:suspend ()->Unit = FsUtils.getDoSaveForEditor(
        editorPageShowingFilePath = editorPageShowingFilePath,
        editorPageLoadingOn = loadingOn,
        editorPageLoadingOff = loadingOff,
        appContext = appContext,
        editorPageIsSaving = editorPageIsSaving,
        needRefreshEditorPage = needRefreshEditorPage,
        editorPageTextEditorState = editorPageTextEditorState,
        pageTag = TAG,
        editorPageIsEdited = editorPageIsEdited,
        requestFromParent = editorPageRequestFromParent,
        editorPageFileDto = editorPageShowingFileDto,
        isSubPageMode = true,
        isContentSnapshoted =editorPageIsContentSnapshoted,
        snapshotedFileInfo = editorPageSnapshotedFileInfo
    )

    Scaffold(
        modifier = Modifier.nestedScroll(homeTopBarScrollBehavior.nestedScrollConnection),
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
                    EditorTitle(editorPageShowingFilePath, editorPageRequestFromParent, editorPageSearchMode.value, editorPageSearchKeyword, editorPageMergeMode.value, editorReadOnlyMode.value, editorOpenFileErr.value)

                },
                navigationIcon = {
                    if(editorPageSearchMode.value || editorAdjustFontSizeMode.value || editorAdjustLineNumFontSizeMode.value) {
                        LongPressAbleIconBtn(
                            tooltipText = stringResource(R.string.close),
                            icon =  Icons.Filled.Close,
                            iconContentDesc = stringResource(R.string.close),

                        ) {
                            if(editorPageSearchMode.value){
                                editorPageSearchMode.value = false
                            }else if(editorAdjustFontSizeMode.value) {
                                editorPageRequestFromParent.value = PageRequest.requireSaveFontSizeAndQuitAdjust
                            }else if(editorAdjustLineNumFontSizeMode.value) {
                                editorPageRequestFromParent.value = PageRequest.requireSaveLineNumFontSizeAndQuitAdjust
                            }
                        }
                    }else {
                        LongPressAbleIconBtn(
                            tooltipText = stringResource(R.string.back),
                            icon = Icons.AutoMirrored.Filled.ArrowBack,
                            iconContentDesc = stringResource(R.string.back),
                        ) {
                            doJobThenOffLoading {
//                            isTimeNaviUp.value=false
                                //未保存，先保存，再点击，再返回
                                if(editorPageIsEdited.value && !editorReadOnlyMode.value) {
//                                doSave()这个得改一下，不要在外部保存
                                    editorPageRequestFromParent.value = PageRequest.requireSave
                                    return@doJobThenOffLoading
                                }

                                //返回
//                            isTimeNaviUp.value=true
                                withContext(Dispatchers.Main) {
                                    naviUp()
                                }

//                            changeStateTriggerRefreshPage(needRefreshEditorPage)  //都离开页面了，刷新鸡毛啊

                            }
                        }
                    }

                },
                //TODO: IconButton不支持长按显示提示信息，我想让用户长按，手机震动，在按钮附近半透明显示按钮功能，就像安卓4.x那样，如果实现不了，必须确保“所有按钮，第一次按下，就能让用户知道这个按钮的功能，并且不会修改任何东西”
                actions = {
                    if(!editorOpenFileErr.value) {
                        EditorPageActions(
                            editorPageShowingFilePath,
    //                        editorPageRequireOpenFilePath,
                            editorPageShowingFileIsReady,
                            needRefreshEditorPage,
                            editorPageTextEditorState,
    //                        editorPageShowSaveDoneToast,
                            isSaving = editorPageIsSaving,
                            isEdited = editorPageIsEdited,
                            showReloadDialog = showReloadDialog,
                            showCloseDialog = showCloseDialog,
                            closeDialogCallback=closeDialogCallback,
    //                        isLoading = isLoading,
                            doSave = doSave,
                            loadingOn = loadingOn,
                            loadingOff = loadingOff,
                            editorPageRequest = editorPageRequestFromParent,
                            editorPageSearchMode = editorPageSearchMode,
                            editorPageMergeMode=editorPageMergeMode,
                            readOnlyMode = editorReadOnlyMode,
                            editorSearchKeyword = editorPageSearchKeyword.value.text,
                            isSubPageMode=true,

                            fontSize=editorFontSize,
                            lineNumFontSize=editorLineNumFontSize,
                            adjustFontSizeMode=editorAdjustFontSizeMode,
                            adjustLineNumFontSizeMode=editorAdjustLineNumFontSizeMode,
                            showLineNum = editorShowLineNum
                        )
                    }
                },
                scrollBehavior = homeTopBarScrollBehavior,
            )
        },
        floatingActionButton = {
            if(editorPageShowingFileIsReady.value && editorPageShowingFilePath.value.isNotBlank() && editorPageIsEdited.value && !editorPageIsSaving.value && !editorReadOnlyMode.value) {
                SmallFab(
                    modifier= MyStyleKt.Fab.getFabModifierForEditor(editorPageTextEditorState.value.isMultipleSelectionMode),
                    icon = Icons.Filled.Save, iconDesc = stringResource(id = R.string.save)
                ) {
                    editorPageRequestFromParent.value = PageRequest.requireSave
                }
            }
        }
    ) { contentPadding ->
        EditorInnerPage(
            contentPadding = contentPadding,

            //editor作为子页面时其实不需要这个变量，只是调用的组件需要，又没默认值，所以姑且创建一个
            currentHomeScreen = StateUtil.getRememberIntState(Cons.selectedItem_Repos),

//            editorPageRequireOpenFilePath=editorPageRequireOpenFilePath,
            editorPageShowingFilePath=editorPageShowingFilePath,
            editorPageShowingFileIsReady=editorPageShowingFileIsReady,
            editorPageTextEditorState=editorPageTextEditorState,
//            editorPageShowSaveDoneToast=editorPageShowSaveDoneToast,
            needRefreshEditorPage=needRefreshEditorPage,
            isSaving = editorPageIsSaving,
            isEdited = editorPageIsEdited,
            showReloadDialog=showReloadDialog,
            isSubPageMode=true,
            showCloseDialog=showCloseDialog,
            closeDialogCallback=closeDialogCallback,
//            isLoading = isLoading,
            loadingOn = loadingOn,
            loadingOff = loadingOff,
            saveOnDispose = false,  //这时父页面（当前页面）负责保存，不需要子页面保存，所以传false
            doSave = doSave,  //doSave还是要传的，虽然销毁组件时的保存关闭了，但是返回时的保存依然要用doSave
            naviUp=naviUp,
            editorPageRequestFromParent,
            editorPageShowingFileDto,
            editorPageLastFilePath,
            editorPageLastScrollEvent,
            editorPageLazyListState,
            editorPageIsInitDone,
            editorPageIsContentSnapshoted,
            goToFilesPage = {},  //子页面不支持在Files显示文件，所以传空函数即可，应该由子页面的调用者(应该是个顶级页面)来实现在Files页面显示文件，子页面只负责编辑（暂时先这样，实际上就算想支持也不行，因为子页面无法跳转到顶级的Files页面）
            goToLine=goToLine,
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
            openDrawer = {}, //非顶级页面按返回键不需要打开抽屉
            editorOpenFileErr = editorOpenFileErr

        )
    }




    //compose创建时的副作用
//    LaunchedEffect(currentPage.intValue) {
    LaunchedEffect(Unit) {
    }


    //compose被销毁时执行的副作用
    DisposableEffect(Unit) {
//        ("DisposableEffect: entered main")
        onDispose {
//            ("DisposableEffect: exited main")
        }
    }

}



