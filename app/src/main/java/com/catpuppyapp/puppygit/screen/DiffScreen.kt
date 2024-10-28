package com.catpuppyapp.puppygit.screen

import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import com.catpuppyapp.puppygit.compose.ConfirmDialog
import com.catpuppyapp.puppygit.compose.CopyableDialog
import com.catpuppyapp.puppygit.compose.GoToTopAndGoToBottomFab
import com.catpuppyapp.puppygit.compose.LongPressAbleIconBtn
import com.catpuppyapp.puppygit.compose.MySelectionContainer
import com.catpuppyapp.puppygit.compose.OpenAsDialog
import com.catpuppyapp.puppygit.constants.Cons
import com.catpuppyapp.puppygit.constants.PageRequest
import com.catpuppyapp.puppygit.data.entity.RepoEntity
import com.catpuppyapp.puppygit.git.StatusTypeEntrySaver
import com.catpuppyapp.puppygit.play.pro.R
import com.catpuppyapp.puppygit.screen.content.DiffContent
import com.catpuppyapp.puppygit.screen.content.homescreen.scaffold.actions.DiffPageActions
import com.catpuppyapp.puppygit.screen.content.homescreen.scaffold.title.DiffScreenTitle
import com.catpuppyapp.puppygit.settings.SettingsUtil
import com.catpuppyapp.puppygit.utils.AppModel
import com.catpuppyapp.puppygit.utils.Msg
import com.catpuppyapp.puppygit.utils.cache.Cache
import com.catpuppyapp.puppygit.utils.changeStateTriggerRefreshPage
import com.catpuppyapp.puppygit.utils.getFileNameFromCanonicalPath
import com.catpuppyapp.puppygit.utils.getParentPathEndsWithSeparator
import com.catpuppyapp.puppygit.utils.state.mutableCustomStateOf
import java.io.File

private val stateKeyTag = "DiffScreen"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DiffScreen(
    repoId: String,
    fromTo:String,
    changeType:String,  //modification, new, del，之类的只有modification需要diff
    fileSize:Long,
    underRepoPathKey:String,
    treeOid1Str:String,
    treeOid2Str:String,
    isSubmodule:Boolean,
    isDiffToLocal:Boolean,
    diffableItemListKey:String, //可预览diff的条目集合
    curItemIndexAtDiffableItemList:Int,
    naviUp: () -> Boolean,
) {

    //废弃，改用diffContent里获取diffItem时动态计算了
//    val fileSizeOverLimit = isFileSizeOverLimit(fileSize)
    val dbContainer = AppModel.singleInstanceHolder.dbContainer
    val homeTopBarScrollBehavior = AppModel.singleInstanceHolder.homeTopBarScrollBehavior

    val appContext = AppModel.singleInstanceHolder.appContext

    val scope = rememberCoroutineScope()
    val settings = remember { SettingsUtil.getSettingsSnapshot() }

    val clipboardManager = LocalClipboardManager.current

    //这个值存到状态变量里之后就不用管了，与页面共存亡即可，如果旋转屏幕也没事，返回rememberSaveable可恢复
//    val relativePathUnderRepoDecoded = (Cache.Map.getThenDel(Cache.Map.Key.diffScreen_UnderRepoPath) as? String)?:""
    val relativePathUnderRepoState = rememberSaveable { mutableStateOf((Cache.getByTypeThenDel<String>(underRepoPathKey)) ?: "")}

    val diffableItemList = rememberSaveable { (Cache.getByTypeThenDel<List<StatusTypeEntrySaver>>(diffableItemListKey)) ?: listOf()}
    val curItemIndex = rememberSaveable { mutableIntStateOf(curItemIndexAtDiffableItemList) }
    val changeType = rememberSaveable { mutableStateOf(changeType) }
    val fileSize = rememberSaveable { mutableLongStateOf(fileSize) }
    val isSubmodule = rememberSaveable { mutableStateOf(isSubmodule) }



//    val curRepo = rememberSaveable { mutableStateOf(RepoEntity()) }
    val curRepo = mutableCustomStateOf(keyTag = stateKeyTag, keyName = "curRepo", initValue = RepoEntity())
    val fileNameOnly = remember{ derivedStateOf {  getFileNameFromCanonicalPath(relativePathUnderRepoState.value)} }
    val fileUnderRepoRelativePathOnly = remember{ derivedStateOf {getParentPathEndsWithSeparator(relativePathUnderRepoState.value)}}

    //考虑将这个功能做成开关，所以用状态变量存其值
    //ps: 这个值要么做成可在设置页面关闭（当然，其他与预览diff不相关的页面也行，总之别做成只能在正在执行O(nm)的diff页面开关就行），要么就默认app启动后重置为关闭，绝对不能做成只能在预览diff的页面开关，不然万一O(nm)算法太慢卡死导致这个东西关不了就尴尬了
    //20240618:目前临时开启O(nm)算法的机制是在预览diff页面三击屏幕，但app启动时会重置为关闭，日后需要添加相关设置项以方便用户使用
    val requireBetterMatchingForCompare = rememberSaveable { mutableStateOf(false) }


    val loading = rememberSaveable { mutableStateOf(true)}
    val needRefresh = rememberSaveable { mutableStateOf("")}

    val request = rememberSaveable { mutableStateOf("")}

    val listState = rememberScrollState()
    val fileFullPath = remember{ derivedStateOf{curRepo.value.fullSavePath + File.separator + relativePathUnderRepoState.value}}

    val showBackFromExternalAppAskReloadDialog = rememberSaveable { mutableStateOf(false)}
    if(showBackFromExternalAppAskReloadDialog.value) {
        ConfirmDialog(
            title = stringResource(id = R.string.reload_file),
            text = stringResource(R.string.back_editor_from_external_app_ask_reload),
            okBtnText = stringResource(id = R.string.reload),
            onCancel = { showBackFromExternalAppAskReloadDialog.value=false }
        ) {
            //onOk
            showBackFromExternalAppAskReloadDialog.value=false
            changeStateTriggerRefreshPage(needRefresh)
        }
    }


    val showOpenAsDialog = rememberSaveable { mutableStateOf(false)}
    val openAsDialogFilePath = rememberSaveable { mutableStateOf("")}
//    val showOpenInEditor = StateUtil.getRememberSaveableState(initValue = false)
    if(showOpenAsDialog.value) {
        OpenAsDialog(fileName=fileNameOnly.value, filePath = openAsDialogFilePath.value,
            openSuccessCallback = {
                //只有在worktree的diff页面才有必要显示弹窗，在index页面没必要显示，在diff commit的页面更没必要显示，因为若修改，肯定是修改worktree的文件，你在index页面就算重载也看不到修改后的内容，所以没必要提示
                if(fromTo == Cons.gitDiffFromIndexToWorktree) {
                    //如果请求外部打开成功，不管用户有无选择app（想实现成选择才询问是否重新加载，但无法判断）都询问是否重载文件
                    showBackFromExternalAppAskReloadDialog.value=true  // 显示询问是否重载的弹窗
                }
            }
        ) {
            //onClose
            showOpenAsDialog.value=false
        }
    }

    val detailsString = rememberSaveable { mutableStateOf("")}
    val showDetailsDialog = rememberSaveable { mutableStateOf(false)}
    if(showDetailsDialog.value){
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

    if(request.value == PageRequest.showOpenAsDialog) {
        PageRequest.clearStateThenDoAct(request) {
            openAsDialogFilePath.value = fileFullPath.value
            showOpenAsDialog.value=true
        }
    }

    if(request.value == PageRequest.showDetails) {
        PageRequest.clearStateThenDoAct(request) {
            val sb = StringBuilder()
            sb.append(appContext.getString(R.string.file_name)+": ").appendLine(fileNameOnly.value).appendLine()
            sb.append(appContext.getString(R.string.path_under_repo)+": ").appendLine(relativePathUnderRepoState.value).appendLine()

            sb.append(appContext.getString(R.string.path)+": ").appendLine(fileFullPath.value)  // no more append line yet, because is last line

            detailsString.value = sb.toString()
            showDetailsDialog.value=true
        }
    }

    // 向下滚动监听，开始
    val pageScrolled = remember { mutableStateOf(settings.showNaviButtons) }
//    val firstVisible = remember { derivedStateOf { listState.value } }
//    ScrollListener(
//        nowAt = firstVisible.value,
//        onScrollUp = {scrollingDown.value = false}
//    ) { // onScrollDown
//        scrollingDown.value = true
//    }
//
//    val lastAt = remember { mutableIntStateOf(0) }
//    val lastIsScrollDown = remember { mutableStateOf(false) }
//    val forUpdateScrollState = remember {
//        derivedStateOf {
//            val nowAt = listState.value
//
//            val scrolledDown = nowAt > lastAt.intValue  // scroll down
////            val scrolledUp = nowAt < lastAt.intValue
//
//            val scrolled = nowAt != lastAt.intValue  // scrolled
//            lastAt.intValue = nowAt
//
//            // only update state when this scroll down and last is not scroll down, or this is scroll up and last is not scroll up
//            if(scrolled && ((lastIsScrollDown.value && !scrolledDown) || (!lastIsScrollDown.value && scrolledDown))) {
//                pageScrolled.value = true
//            }
//
//            lastIsScrollDown.value = scrolledDown
//        }
//    }.value
    // 向下滚动监听，结束

    val switchItem = {newItem:StatusTypeEntrySaver, newItemIndex:Int->
        changeType.value = newItem.changeType ?:""
        isSubmodule.value = newItem.itemType == Cons.gitItemTypeSubmodule
        fileSize.value = newItem.fileSizeInBytes
        relativePathUnderRepoState.value = newItem.relativePathUnderRepo

        curItemIndex.value = newItemIndex

        changeStateTriggerRefreshPage(needRefresh)
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
                    DiffScreenTitle(
                        fileName = fileNameOnly.value,
                        filePath = fileUnderRepoRelativePathOnly.value,
                        fileRelativePathUnderRepoState = relativePathUnderRepoState,
                        listState,
                        scope,
                        request
                    )
                },
                navigationIcon = {
                    LongPressAbleIconBtn(
                        tooltipText = stringResource(R.string.back),
                        icon = Icons.AutoMirrored.Filled.ArrowBack,
                        iconContentDesc = stringResource(R.string.back),

                        ) {
                        naviUp()
                    }
                },

                actions = {
                    DiffPageActions(curRepo, fromTo = fromTo, changeType=changeType.value,
                        relativePathUnderRepoState, { changeStateTriggerRefreshPage(needRefresh) },
                        listState,
                        scope,
                        request,
                        fileFullPath.value,
                        requireBetterMatchingForCompare
                    )

                },
                scrollBehavior = homeTopBarScrollBehavior,
            )
        },
        floatingActionButton = {
            if(pageScrolled.value) {
                GoToTopAndGoToBottomFab(
                    scope = scope,
                    listState = listState,
                    showFab = pageScrolled
                )
            }
        }
    ) { contentPadding ->
//        if(fileSizeOverLimit) {  // 文件过大不加载
//            Column(
//                modifier = Modifier
//                    .fillMaxSize()
////                    .verticalScroll(rememberScrollState())
//                    .padding(contentPadding)
//                ,
//                horizontalAlignment = Alignment.CenterHorizontally,
//                verticalArrangement = Arrangement.Center,
//            ) {
//                Row {
//                    Text(text = stringResource(R.string.file_size_over_limit)+"("+Cons.editorFileSizeMaxLimitForHumanReadable+")")
//                }
//            }
//        }else {  //文件大小ok

        //改成统一在DiffContent里检查实际diff需要获取的内容的大小了，和文件大小有所不同，有时候文件大小很大，但需要diff的内容大小实际很小，这时其实可以diff，性能不会太差
        MySelectionContainer {
            DiffContent(repoId=repoId,relativePathUnderRepoDecoded=relativePathUnderRepoState.value,
                fromTo=fromTo,changeType=changeType.value,fileSize=fileSize.value, naviUp=naviUp,
                loading=loading,dbContainer=dbContainer,contentPadding, treeOid1Str, treeOid2Str,
                needRefresh = needRefresh, listState = listState, curRepo=curRepo,
                requireBetterMatchingForCompare = requireBetterMatchingForCompare, fileFullPath = fileFullPath.value,
                isSubmodule=isSubmodule.value, isDiffToLocal = isDiffToLocal,diffableItemList= diffableItemList,
                curItemIndex=curItemIndex, switchItem=switchItem
            )
        }

//        }
    }



//    LaunchedEffect(Unit) {
//    }
}

