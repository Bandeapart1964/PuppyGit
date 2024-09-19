package com.catpuppyapp.puppygit.screen

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FilterAlt
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.VerticalAlignTop
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.catpuppyapp.puppygit.compose.FilterTextField
import com.catpuppyapp.puppygit.compose.LongPressAbleIconBtn
import com.catpuppyapp.puppygit.compose.SmallFab
import com.catpuppyapp.puppygit.constants.Cons
import com.catpuppyapp.puppygit.data.entity.RepoEntity
import com.catpuppyapp.puppygit.dev.commitsTreeToTreeDiffReverseTestPassed
import com.catpuppyapp.puppygit.dev.dev_EnableUnTestedFeature
import com.catpuppyapp.puppygit.git.StatusTypeEntrySaver
import com.catpuppyapp.puppygit.play.pro.R
import com.catpuppyapp.puppygit.screen.content.homescreen.innerpage.ChangeListInnerPage
import com.catpuppyapp.puppygit.style.MyStyleKt
import com.catpuppyapp.puppygit.user.UserUtil
import com.catpuppyapp.puppygit.utils.AppModel
import com.catpuppyapp.puppygit.utils.Libgit2Helper
import com.catpuppyapp.puppygit.utils.Msg
import com.catpuppyapp.puppygit.utils.UIHelper
import com.catpuppyapp.puppygit.utils.addPrefix
import com.catpuppyapp.puppygit.utils.cache.Cache
import com.catpuppyapp.puppygit.utils.changeStateTriggerRefreshPage
import com.catpuppyapp.puppygit.utils.state.StateUtil
import com.github.git24j.core.Repository

//for debug
private val TAG = "TreeToTreeChangeListScreen"
private val stateKeyTag = "TreeToTreeChangeListScreen"

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun TreeToTreeChangeListScreen(
//    context: Context,
//    navController: NavController,
//    scope: CoroutineScope,
//    scrollBehavior: TopAppBarScrollBehavior,
//    repoPageListState: LazyListState,
//    filePageListState: LazyListState,
//    haptic: HapticFeedback,
    repoId:String,

    // show differences of commit1 to commit2 (git cmd format: 'commit1..commit2', or 'left..right')
    commit1OidStr:String,  // left
    commit2OidStr:String,  // right

    titleDescKey:String,
    commitForQueryParents:String,  // commit for query parents, if empty ,will not query parents for commits. ps: only need this param when compare to parents, other cases, should pass empty string
    naviUp: () -> Unit
) {
    //避免导航出现 "//" 导致导航失败
    //因为title要改变这个值，所以用State
    val commit1OidStrState = StateUtil.getRememberSaveableState(initValue = commit1OidStr)
    if(commit1OidStrState.value.isBlank()) {
        commit1OidStrState.value = Cons.allZeroOid.toString()
    }
    val commit2OidStr = commit2OidStr.ifBlank { Cons.allZeroOid.toString() }

    val commitParentList = StateUtil.getCustomSaveableStateList(
        keyTag = stateKeyTag,
        keyName = "commitParentList",
        initValue = listOf<String>()
    )

    val haptic = LocalHapticFeedback.current
    val scope = rememberCoroutineScope()

//    SideEffect {
//        Msg.msgNotifyHost()
//    }

    val navController = AppModel.singleInstanceHolder.navController
    val homeTopBarScrollBehavior = AppModel.singleInstanceHolder.homeTopBarScrollBehavior

    val allRepoParentDir = AppModel.singleInstanceHolder.allRepoParentDir
    val appContext = AppModel.singleInstanceHolder.appContext

    //取出title desc，存到状态变量里，与页面共存亡就行
    val titleDesc = StateUtil.getRememberSaveableState(initValue = (Cache.getByTypeThenDel<String>(titleDescKey))?:"")

    //替换成我的cusntomstateSaver，然后把所有实现parcellzier的类都取消实现parcellzier，改成用我的saver
//    val curRepo = rememberSaveable{ mutableStateOf(RepoEntity()) }
//    val curRepo = mutableCustomStateOf(value = RepoEntity())

    val changeListRefreshRequiredByParentPage =StateUtil.getRememberSaveableState(initValue = "")
    val changeListRequireRefreshFromParentPage = {
        //TODO 显示个loading遮罩啥的
        changeStateTriggerRefreshPage(changeListRefreshRequiredByParentPage)
    }
//    val changeListCurRepo = rememberSaveable{ mutableStateOf(RepoEntity()) }
    val changeListCurRepo = StateUtil.getCustomSaveableState(
        keyTag = stateKeyTag,
        keyName = "changeListCurRepo",
        initValue = RepoEntity(id="")
    )
    val changeListIsShowRepoList = StateUtil.getRememberSaveableState(initValue = false)
    val changeListPageHasIndexItem = StateUtil.getRememberSaveableState(initValue = false)
    val changeListShowRepoList = {
        changeListIsShowRepoList.value = true
    }
    val changeListIsFileSelectionMode = StateUtil.getRememberSaveableState(initValue = false)
    val changeListPageNoRepo = StateUtil.getRememberSaveableState(initValue = false)
    val changeListPageHasNoConflictItems = StateUtil.getRememberSaveableState(initValue = false)

    val swap = StateUtil.getRememberSaveableState(initValue = false)
//    val isDiffToHead = StateUtil.getRememberSaveableState(initValue = false)


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
    val fromTo = Cons.gitDiffFromTreeToTree
    val changeListPageItemList = StateUtil.getCustomSaveableStateList(keyTag = stateKeyTag, keyName = "changeListPageItemList", initValue = listOf<StatusTypeEntrySaver>())
    val changeListPageItemListState = StateUtil.getRememberLazyListState()
    val changeListPageSelectedItemList = StateUtil.getCustomSaveableStateList(keyTag = stateKeyTag, keyName = "changeListPageSelectedItemList", initValue = listOf<StatusTypeEntrySaver>())
    val changelistPageScrollingDown = remember { mutableStateOf(false) }

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


    val showParentListDropDownMenu = StateUtil.getRememberSaveableState(initValue = false)

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
                    }else{
                        val titleText = Libgit2Helper.getShortOidStrByFull(commit1OidStrState.value)+".."+Libgit2Helper.getShortOidStrByFull(commit2OidStr)
                        Column(modifier = Modifier
                            //外面的标题宽180.dp，这里的比外面的宽点，因为这个页面顶栏actions少
                            .widthIn(max = 200.dp)
                            .combinedClickable(onLongClick = {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                Msg.requireShow(titleText)
                            }) { //onClick

//                                当比较模式为比较指定的两个提交时(无parents)，点击不会展开下拉菜单(和parents比较才会展开)
                                if (Libgit2Helper.CommitUtil.mayGoodCommitHash(commitForQueryParents)) {
                                    showParentListDropDownMenu.value = true
                                }

                            }
                        ) {
                            Row {
                                Text(
                                    text = titleText,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    fontSize = 18.sp
                                )
                            }
                            Row {
                                Text(text = "["+changeListCurRepo.value.repoName+"]",
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    fontSize = MyStyleKt.Title.secondLineFontSize
                                )
                            }

                        }

                        DropdownMenu(
                            expanded = showParentListDropDownMenu.value,
                            onDismissRequest = { showParentListDropDownMenu.value = false }
                        ) {
                            commitParentList.value.toList().forEach {
                                val itemText = if(it == commit1OidStrState.value) addPrefix(Libgit2Helper.getShortOidStrByFull(it)) else Libgit2Helper.getShortOidStrByFull(it)
                                DropdownMenuItem(text = { Text(text = itemText)},
                                    onClick = {
                                        //切换父提交则退出选择模式(现在20240420没用，但日后可能在TreeToTree页面也添加多选功能，比如可选择文件checkout or hard reset到worktree之类的，所以这里先把需要退出选择模式的逻辑写上)(20240818有用了)
                                        if(commit1OidStrState.value != it) {
                                            changeListIsFileSelectionMode.value=false  //退出选择模式
                                            changeListPageSelectedItemList.value.clear() //清空已选条目
                                        }

                                        showParentListDropDownMenu.value=false
                                        commit1OidStrState.value=it

                                        changeStateTriggerRefreshPage(changeListRefreshRequiredByParentPage)
                                    }
                                )
                            }
                        }
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
                    if(!changeListPageFilterModeOn.value) {
                        Row {
                            LongPressAbleIconBtn(
                                enabled = enableAction.value && !changeListPageNoRepo.value,

                                tooltipText = stringResource(R.string.filter),
                                icon =  Icons.Filled.FilterAlt,
                                iconContentDesc = stringResource(id = R.string.filter),

                            ) {
                                changeListPageFilterKeyWord.value=TextFieldValue("")
                                changeListPageFilterModeOn.value = true
                            }

                            //go to top
//                            LongPressAbleIconBtn(
//                                tooltipText = stringResource(R.string.go_to_top),
//                                icon =  Icons.Filled.VerticalAlignTop,
//                                iconContentDesc = stringResource(id = R.string.go_to_top),
//                                enabled = true,
//
//                            ) {
//                                UIHelper.scrollToItem(scope, changeListPageItemListState, 0)
//                            }

                            LongPressAbleIconBtn(
                                tooltipText = stringResource(R.string.refresh),
                                icon = Icons.Filled.Refresh,
                                iconContentDesc = stringResource(R.string.refresh),
                            ) {
                                changeStateTriggerRefreshPage(changeListRefreshRequiredByParentPage)
                            }

                            if(UserUtil.isPro() && (dev_EnableUnTestedFeature || commitsTreeToTreeDiffReverseTestPassed)) {
                                LongPressAbleIconBtn(
                                    tooltipText = stringResource(R.string.swap_commits),
                                    icon = Icons.Filled.SwapHoriz,
                                    iconContentDesc = stringResource(R.string.swap_commits),
                                    iconColor = UIHelper.getIconEnableColorOrNull(swap.value)
                                ) {
//                                作用是交换比较的和被比较的提交号(交换左右提交)
                                    swap.value = !swap.value
                                    Msg.requireShow(appContext.getString(if (swap.value) R.string.swap_commits_on else R.string.swap_commits_off))

                                    //swap值不在cl页面的LaunchedEffects key中，所以得刷新下
                                    changeListRequireRefreshFromParentPage()
                                }
                            }
                        }

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
                    }else{
                        UIHelper.scrollToItem(scope, changeListPageItemListState, 0)
                    }
                }
            }
        }
    ) { contentPadding ->
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
            commit1OidStr=commit1OidStrState.value,
            commit2OidStr=commit2OidStr,
            commitParentList = commitParentList,
            repoId=repoId,
            changeListPageNoRepo=changeListPageNoRepo,
            hasNoConflictItems = changeListPageHasNoConflictItems,  //这选项是worktree和Index页面用的，TreeToTree其实用不到这个选项，只是占位
            changelistPageScrollingDown=changelistPageScrollingDown,
            changeListPageFilterModeOn= changeListPageFilterModeOn,
            changeListPageFilterKeyWord=changeListPageFilterKeyWord,
            filterListState = changelistFilterListState,
            swap=swap.value,
            commitForQueryParents = commitForQueryParents,
            openDrawer = {}, //非顶级页面按返回键不需要打开抽屉

//            isDiffToHead=isDiffToHead

        )

    }
//    }

    //compose创建时的副作用
//    LaunchedEffect(currentPage.intValue) {
//    LaunchedEffect(commit1OidStrState.value + commit2OidStr) {
//        try {
//            doJobThenOffLoading {
//                Repository.open(curRepo)
//            }
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



