package com.catpuppyapp.puppygit.screen

import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.VerticalAlignTop
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.TextFieldValue
import com.catpuppyapp.puppygit.compose.FilterTextField
import com.catpuppyapp.puppygit.compose.LongPressAbleIconBtn
import com.catpuppyapp.puppygit.compose.SmallFab
import com.catpuppyapp.puppygit.constants.Cons
import com.catpuppyapp.puppygit.data.entity.RepoEntity
import com.catpuppyapp.puppygit.git.StatusTypeEntrySaver
import com.catpuppyapp.puppygit.play.pro.R
import com.catpuppyapp.puppygit.screen.content.homescreen.innerpage.ChangeListInnerPage
import com.catpuppyapp.puppygit.screen.content.homescreen.scaffold.actions.ChangeListPageActions
import com.catpuppyapp.puppygit.screen.content.homescreen.scaffold.title.IndexScreenTitle
import com.catpuppyapp.puppygit.style.MyStyleKt
import com.catpuppyapp.puppygit.utils.AppModel
import com.catpuppyapp.puppygit.utils.Msg
import com.catpuppyapp.puppygit.utils.UIHelper
import com.catpuppyapp.puppygit.utils.changeStateTriggerRefreshPage
import com.catpuppyapp.puppygit.utils.state.StateUtil
import com.github.git24j.core.Repository

private val TAG = "IndexScreen"
private val stateKeyTag = "IndexScreen"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IndexScreen(
//    context: Context,
//    navController: NavController,
//    scope: CoroutineScope,
//    scrollBehavior: TopAppBarScrollBehavior,
//    repoPageListState: LazyListState,
//    filePageListState: LazyListState,
//    haptic: HapticFeedback,
    naviUp: () -> Unit
) {


    val navController = AppModel.singleInstanceHolder.navController
    val homeTopBarScrollBehavior = AppModel.singleInstanceHolder.homeTopBarScrollBehavior

    val allRepoParentDir = AppModel.singleInstanceHolder.allRepoParentDir
    val scope = rememberCoroutineScope()


    //替换成我的cusntomstateSaver，然后把所有实现parcellzier的类都取消实现parcellzier，改成用我的saver
//    val curRepo = rememberSaveable{ mutableStateOf(RepoEntity()) }
//    val curRepo = mutableCustomStateOf(value = RepoEntity())

    val changeListRefreshRequiredByParentPage = rememberSaveable { mutableStateOf("") }
    val changeListRequireRefreshFromParentPage = {
        //TODO 显示个loading遮罩啥的
        changeStateTriggerRefreshPage(changeListRefreshRequiredByParentPage)
    }
//    val changeListCurRepo = rememberSaveable{ mutableStateOf(RepoEntity()) }
    val changeListCurRepo = StateUtil.getCustomSaveableState(keyTag = stateKeyTag, keyName = "changeListCurRepo", initValue = RepoEntity(id=""))
    val changeListIsShowRepoList = StateUtil.getRememberSaveableState(initValue = false)
    val changeListPageHasIndexItem = StateUtil.getRememberSaveableState(initValue = false)
    val changeListShowRepoList = {
        changeListIsShowRepoList.value = true
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

    val changelistFilterListState = StateUtil.getCustomSaveableState(
        keyTag = stateKeyTag,
        keyName = "changelistFilterListState"
    ) {
        LazyListState(0,0)
    }

    val swap = StateUtil.getRememberSaveableState(initValue = false)

//    val editorPageRequireOpenFilePath = rememberSaveable{ mutableStateOf("") } // canonicalPath
////    val needRefreshFilesPage = rememberSaveable { mutableStateOf(false) }
//    val needRefreshFilesPage = rememberSaveable { mutableStateOf("") }
//    val currentPath = rememberSaveable { mutableStateOf(allRepoParentDir.canonicalPath) }
//    val showCreateFileOrFolderDialog = rememberSaveable{ mutableStateOf(false) }
//
//    val showSetGlobalGitUsernameAndEmailDialog = rememberSaveable { mutableStateOf(false) }
//
//    val editorPageShowingFilePath = rememberSaveable{ mutableStateOf("") } //当前展示的文件的canonicalPath
//    val editorPageShowingFileIsReady = rememberSaveable{ mutableStateOf(false) } //当前展示的文件是否已经加载完毕
//    //TextEditor用的变量
//    val editorPageTextEditorState = remember { mutableStateOf(TextEditorState.create("")) }
//    val editorPageShowSaveDoneToast = rememberSaveable { mutableStateOf(false) }
////    val needRefreshEditorPage = rememberSaveable { mutableStateOf(false) }
//    val needRefreshEditorPage = rememberSaveable { mutableStateOf("") }
//    val changeListRequirePull = rememberSaveable { mutableStateOf(false) }
//    val changeListRequirePush = rememberSaveable { mutableStateOf(false) }
    val requireDoActFromParent = StateUtil.getRememberSaveableState(initValue = false)
    val requireDoActFromParentShowTextWhenDoingAct = StateUtil.getRememberSaveableState(initValue = "")
    val enableAction = StateUtil.getRememberSaveableState(initValue = true)
    val repoState = StateUtil.getRememberSaveableIntState(initValue = Repository.StateT.NONE.bit)  //初始状态是NONE，后面会在ChangeListInnerPage检查并更新状态，只要一创建innerpage或刷新（重新执行init），就会更新此状态
    val fromTo = Cons.gitDiffFromHeadToIndex
    val changeListPageItemList = StateUtil.getCustomSaveableStateList(keyTag = stateKeyTag, keyName = "changeListPageItemList", initValue = listOf<StatusTypeEntrySaver>())
    val changeListPageItemListState = StateUtil.getRememberLazyListState()
    val changeListPageSelectedItemList = StateUtil.getCustomSaveableStateList(keyTag = stateKeyTag, keyName = "changeListPageSelectedItemList", initValue = listOf<StatusTypeEntrySaver>())
    val changelistPageScrollingDown = remember { mutableStateOf(false) }

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
                    if(changeListPageFilterModeOn.value) {
                        FilterTextField(
                            changeListPageFilterKeyWord,
                        )
                    }else {
                        IndexScreenTitle(changeListCurRepo, repoState, scope, changeListPageItemListState)
                    }
                },
                navigationIcon = {
                    if(changeListPageFilterModeOn.value) {
                        LongPressAbleIconBtn(
                            tooltipText = stringResource(R.string.close),
                            icon = Icons.Filled.Close,
                            iconContentDesc = stringResource(R.string.close),

                        ) {
                            changeListPageFilterModeOn.value = false
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
                    if(!changeListPageFilterModeOn.value) {
                        ChangeListPageActions(
                            changeListCurRepo,
                            changeListRequireRefreshFromParentPage,
                            changeListPageHasIndexItem,
                            requireDoActFromParent,
                            requireDoActFromParentShowTextWhenDoingAct,
                            enableAction,
                            repoState,
                            fromTo,
                            changeListPageItemListState,
                            scope,
                            changeListPageNoRepo=changeListPageNoRepo,
                            hasNoConflictItems = changeListPageHasNoConflictItems.value,
                            changeListPageFilterModeOn=changeListPageFilterModeOn,
                            changeListPageFilterKeyWord=changeListPageFilterKeyWord,
                            rebaseCurOfAll = changeListPageRebaseCurOfAll.value

                        )

                    }
                },
                scrollBehavior = homeTopBarScrollBehavior,
            )
        },
        floatingActionButton = {
            if(changelistPageScrollingDown.value) {
                //向下滑动时显示go to top按钮
                SmallFab(
                    modifier = MyStyleKt.Fab.getFabModifier(),
                    icon = Icons.Filled.VerticalAlignTop, iconDesc = stringResource(id = R.string.go_to_top)
                ) {
                    if(changeListPageFilterModeOn.value) {
                        UIHelper.scrollToItem(scope, changelistFilterListState.value, 0)
                    }else {
                        UIHelper.scrollToItem(scope, changeListPageItemListState, 0)
                    }
                }
            }
        }
    ) { contentPadding ->
//        val commit1OidStr = rememberSaveable { mutableStateOf("") }
//        val commitParentList = remember { mutableStateListOf<String>() }

        ChangeListInnerPage(
            contentPadding,
            fromTo,
            changeListCurRepo,
            changeListIsFileSelectionMode,

            changeListRefreshRequiredByParentPage,
            changeListPageHasIndexItem,
//                requirePullFromParentPage = changeListRequirePull,
//                requirePushFromParentPage = changeListRequirePush,
            requireDoActFromParent,
            requireDoActFromParentShowTextWhenDoingAct,
            enableAction,
            repoState,
            naviUp=naviUp,
            itemList = changeListPageItemList,
            itemListState = changeListPageItemListState,
            selectedItemList = changeListPageSelectedItemList,
            changeListPageNoRepo=changeListPageNoRepo,
            hasNoConflictItems = changeListPageHasNoConflictItems,
            changelistPageScrollingDown=changelistPageScrollingDown,
            changeListPageFilterModeOn= changeListPageFilterModeOn,
            changeListPageFilterKeyWord=changeListPageFilterKeyWord,
            filterListState = changelistFilterListState,
            swap=swap.value,
            commitForQueryParents = "",
            rebaseCurOfAll = changeListPageRebaseCurOfAll,
            openDrawer = {}, //非顶级页面按返回键不需要打开抽屉
//            commit1OidStr=commit1OidStr,
//            commitParentList=commitParentList
        )

    }
//    }

    //compose创建时的副作用
//    LaunchedEffect(currentPage.intValue) {
//    LaunchedEffect(Unit) {
//        try {
//
//        } catch (cancel: Exception) {
////            ("LaunchedEffect: job cancelled")
//        }
//    }
//
//
//    //compose被销毁时执行的副作用
//    DisposableEffect(Unit) {
////        ("DisposableEffect: entered main")
//        onDispose {
////            ("DisposableEffect: exited main")
//        }
//    }

}



