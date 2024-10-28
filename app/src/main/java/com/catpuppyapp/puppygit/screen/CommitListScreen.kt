package com.catpuppyapp.puppygit.screen

import android.annotation.SuppressLint
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FilterAlt
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.MoveToInbox
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.VerticalAlignTop
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import com.catpuppyapp.puppygit.compose.BottomSheet
import com.catpuppyapp.puppygit.compose.BottomSheetItem
import com.catpuppyapp.puppygit.compose.CheckoutDialog
import com.catpuppyapp.puppygit.compose.CheckoutDialogFrom
import com.catpuppyapp.puppygit.compose.CommitItem
import com.catpuppyapp.puppygit.compose.ConfirmDialog
import com.catpuppyapp.puppygit.compose.ConfirmDialog2
import com.catpuppyapp.puppygit.compose.CopyableDialog
import com.catpuppyapp.puppygit.compose.CreateTagDialog
import com.catpuppyapp.puppygit.compose.DiffCommitsDialog
import com.catpuppyapp.puppygit.compose.FilterTextField
import com.catpuppyapp.puppygit.compose.GoToTopAndGoToBottomFab
import com.catpuppyapp.puppygit.compose.LoadMore
import com.catpuppyapp.puppygit.compose.LoadingDialog
import com.catpuppyapp.puppygit.compose.LongPressAbleIconBtn
import com.catpuppyapp.puppygit.compose.MyCheckBox
import com.catpuppyapp.puppygit.compose.MyLazyColumn
import com.catpuppyapp.puppygit.compose.ResetDialog
import com.catpuppyapp.puppygit.compose.ScrollableColumn
import com.catpuppyapp.puppygit.compose.SingleSelectList
import com.catpuppyapp.puppygit.compose.SmallFab
import com.catpuppyapp.puppygit.constants.Cons
import com.catpuppyapp.puppygit.data.entity.RepoEntity
import com.catpuppyapp.puppygit.dev.cherrypickTestPassed
import com.catpuppyapp.puppygit.dev.commitsDiffCommitsTestPassed
import com.catpuppyapp.puppygit.dev.commitsDiffToLocalTestPassed
import com.catpuppyapp.puppygit.dev.createPatchTestPassed
import com.catpuppyapp.puppygit.dev.dev_EnableUnTestedFeature
import com.catpuppyapp.puppygit.dev.diffToHeadTestPassed
import com.catpuppyapp.puppygit.dev.proFeatureEnabled
import com.catpuppyapp.puppygit.dev.resetByHashTestPassed
import com.catpuppyapp.puppygit.dev.tagsTestPassed
import com.catpuppyapp.puppygit.etc.Ret
import com.catpuppyapp.puppygit.git.CommitDto
import com.catpuppyapp.puppygit.play.pro.R
import com.catpuppyapp.puppygit.settings.SettingsUtil
import com.catpuppyapp.puppygit.style.MyStyleKt
import com.catpuppyapp.puppygit.user.UserUtil
import com.catpuppyapp.puppygit.utils.AppModel
import com.catpuppyapp.puppygit.utils.FsUtils
import com.catpuppyapp.puppygit.utils.Libgit2Helper
import com.catpuppyapp.puppygit.utils.Msg
import com.catpuppyapp.puppygit.utils.MyLog
import com.catpuppyapp.puppygit.utils.StateRequestType
import com.catpuppyapp.puppygit.utils.UIHelper
import com.catpuppyapp.puppygit.utils.boolToDbInt
import com.catpuppyapp.puppygit.utils.cache.Cache
import com.catpuppyapp.puppygit.utils.changeStateTriggerRefreshPage
import com.catpuppyapp.puppygit.utils.createAndInsertError
import com.catpuppyapp.puppygit.utils.doActIfIndexGood
import com.catpuppyapp.puppygit.utils.doJobThenOffLoading
import com.catpuppyapp.puppygit.utils.getRequestDataByState
import com.catpuppyapp.puppygit.utils.isGoodIndexForList
import com.catpuppyapp.puppygit.utils.replaceStringResList
import com.catpuppyapp.puppygit.utils.state.mutableCustomStateListOf
import com.catpuppyapp.puppygit.utils.state.mutableCustomStateOf
import com.catpuppyapp.puppygit.utils.withMainContext
import com.github.git24j.core.Oid
import com.github.git24j.core.Repository
import com.github.git24j.core.Revwalk
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

private val TAG = "CommitListScreen"
private val stateKeyTag = "CommitListScreen"

//TODO 备忘：修改这个页面为可多选的形式，记得加一个 filterList remmeber变量，在过滤模式点击全选或span选择时，操作filterList

/**
 * 注意！这个页面有点特殊，因为需要实现加载更多，所以需要记住下一个commit的oid，但是，列表本身不能实现rememberSaveable，所以一切换屏幕，如果oidStr用rememberSaveable存储，实际上就不是之前的列表了，而是之前列表点击加载更多
 * 后的列表，可以用我写的自定义状态存储器解决，但是，那个状态存储器有可能会造成内存泄漏！因为它无法得知什么时候作为key的rememberSaveable变量被释放！所以会一直占用内存，万一用户有1万个commit，反复加载，就会返回占用内存，早晚
 * 会溢出！所以，这个页面暂时全部用remember实现，虽然无法记住状态，但更稳定，日后解决了我的状态存储器的内存泄漏之后，再说。
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun CommitListScreen(
//    context: Context,
//    navController: NavHostController,
//    scope: CoroutineScope,
//    haptic:HapticFeedback,
//    homeTopBarScrollBehavior: TopAppBarScrollBehavior,
    repoId: String,

    useFullOid:Boolean,
    fullOidKey:String,
    shortBranchNameKey:String,
    isCurrent:Boolean, // HEAD是否指向当前分支

    naviUp: () -> Boolean,
) {
    //已处理这种情况，传参时传有效key，但把value设为空字符串，就解决了
//    println("fullOidKey.isEmpty()="+fullOidKey.isEmpty())  //expect true when nav from repoCard, result: is not empty yet
//    println("fullOidKey="+fullOidKey)  //expect true when nav from repoCard

    val homeTopBarScrollBehavior = AppModel.singleInstanceHolder.homeTopBarScrollBehavior
    val appContext = AppModel.singleInstanceHolder.appContext
    val navController = AppModel.singleInstanceHolder.navController
    val scope = rememberCoroutineScope()
    val haptic = LocalHapticFeedback.current

    val fullOidValue =  Cache.getByTypeThenDel(fullOidKey) ?: ""
    val shortBranchName = Cache.getByTypeThenDel(shortBranchNameKey) ?: ""

    //"main" or "origin/main", get by ref#shorthand(), don't use full branchName, such as "refs/remotes/origin/main", will cause resolve branch failed
    val fullOid = rememberSaveable { mutableStateOf(fullOidValue)}  //这个值需要更新，但最终是否使用，取决于常量 useFullOidParam
    val branchShortNameOrShortHashByFullOid =rememberSaveable { mutableStateOf(shortBranchName)}  //如果checkout会改变此状态的值
    val branchShortNameOrShortHashByFullOidForShowOnTitle = rememberSaveable { mutableStateOf(shortBranchName)}  //显示在标题上的 "branch of repo" 字符串，当刷新页面时会更新此变量，此变量依赖branchShortNameOrShortHashByFullOid的值，所以，必须在checkout成功后更新其值（已更新），不然会显示过时信息

    //测试旋转屏幕是否能恢复getThendel的值。测试结果：能
//    println("fullOid: "+fullOid.value)
//    println("branchShortNameOrShortHashByFullOid: "+branchShortNameOrShortHashByFullOid.value)
//    assert(fullOid.value.isNotBlank())

    val loadChannel = remember { Channel<Int>() }

//    val sumPage = MockData.getCommitSum(repoId,branch)
    //获取假数据
//    val list = remember { mutableStateListOf<CommitDto>() };
//    val list = StateUtil.getCustomSaveableState(keyTag = stateKeyTag, keyDesc = "list", initValue = mutableStateListOf<CommitDto>())
    val list = mutableCustomStateListOf(
        keyTag = stateKeyTag,
        keyName = "list",
        initValue = listOf<CommitDto>()
    )
//    val list = rememberSaveable(
//        stateSaver = getSaver()
//    ) {
//        mutableStateOf(getHolder(stateKeyTag, "list",  mutableListOf<CommitDto>()))
//    }
    val settings = remember { SettingsUtil.getSettingsSnapshot() }
    //page size for load more
    val pageSize = rememberSaveable{ mutableStateOf(settings.commitHistoryPageSize) }
    val rememberPageSize = rememberSaveable { mutableStateOf(false) }

    val nextCommitOid = mutableCustomStateOf<Oid>(
        keyTag = stateKeyTag,
        keyName = "nextCommitOid",
        initValue = Cons.allZeroOid
    )

    //这个页面的滚动状态不用记住，每次点开重置也无所谓
    val listState = rememberLazyListState()
    //如果再多几个"mode"，就改用字符串判断，直接把mode含义写成常量
    val showTopBarMenu = rememberSaveable { mutableStateOf(false)}
    val showDiffCommitDialog = rememberSaveable { mutableStateOf(false)}
    val isSearchingMode = rememberSaveable { mutableStateOf(false)}
    val isShowSearchResultMode = rememberSaveable { mutableStateOf(false)}
    val searchKeyword = rememberSaveable { mutableStateOf( "")}
    val repoOnBranchOrDetachedHash = rememberSaveable { mutableStateOf( "")}
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = MyStyleKt.BottomSheet.skipPartiallyExpanded)
    val showBottomSheet = rememberSaveable { mutableStateOf(false)}
//    val curCommit = rememberSaveable{ mutableStateOf(CommitDto()) }
    val curCommit = mutableCustomStateOf(
        keyTag = stateKeyTag,
        keyName = "curCommit",
        initValue = CommitDto()
    )
    val curRepo = mutableCustomStateOf(
        keyTag = stateKeyTag,
        keyName = "curRepo",
        initValue = RepoEntity(id = "")
    )

    // 两个用途：1点击刷新按钮后回到列表顶部 2放到刷新按钮旁边，用户滚动到底部后，想回到顶部，可点击这个按钮
    val goToTop = {
        UIHelper.scrollToItem(scope, listState, 0)
    }

    val doSearch = {
        isShowSearchResultMode.value = true;
        isSearchingMode.value = false;
        // TODO 搜索提交时判断 “如果包含 / 或者 非hex字符” 就只搜分支列表（然后判断分支是否符号引用若是peel commit，若否直接取出id）。
        // do search with keyword, may need async and give user a loading anime when querying data
        // use "searchKeyword.value" do search
//        println("doSearch with:::"+searchKeyword.value)
    }
    val requireShowToast: (String) -> Unit = Msg.requireShow


    val loadMoreLoading = rememberSaveable { mutableStateOf(false)}
    val loadMoreText = rememberSaveable { mutableStateOf("")}
    val hasMore = rememberSaveable { mutableStateOf(false)}


    val needRefresh = rememberSaveable { mutableStateOf("")}


    val loadingStrRes = stringResource(R.string.loading)
    val loadingText = rememberSaveable { mutableStateOf(loadingStrRes)}
    val showLoadingDialog = rememberSaveable { mutableStateOf(false)}

    val loadingOn = { msg:String->
        loadingText.value = msg
        showLoadingDialog.value=true
    }
    val loadingOff = {
        loadingText.value=loadingStrRes
        showLoadingDialog.value=false
    }

    if (showLoadingDialog.value) {
        LoadingDialog(loadingText.value)
    }

//    val loadingMore = StateUtil.getRememberSaveableState(initValue = false)
//    val hasMore = {
//        nextCommitOid.value != null &&
//    }

    val revwalk = mutableCustomStateOf<Revwalk?>(stateKeyTag, "revwalk", null)
    val repositoryForRevWalk = mutableCustomStateOf<Repository?>(stateKeyTag, "repositoryForRevWalk", null)
    val loadLock = mutableCustomStateOf<Mutex>(stateKeyTag, "loadLock", Mutex())

    val doLoadMore = doLoadMore@{ repoFullPath: String, oid: Oid, firstLoad: Boolean, forceReload: Boolean, loadToEnd:Boolean ->
        //第一次查询的时候是用head oid查询的，所以不会在这里返回
        //用全0oid替代null
        if (oid.isNullOrEmptyOrZero) {  //已经加载到最后一个元素了，其实正常来说，如果加载到最后一个元素，应该已经赋值给nextCommitOid了，然后加载更多按钮也会被禁用，所以多半不会再调用这个方法，这的判断只是为了以防万一
            nextCommitOid.value = oid
            return@doLoadMore
        }
        //无效仓库储存路径
        if (repoFullPath.isBlank()) {
            return@doLoadMore
        }
        //恢复数据
        if (firstLoad && list.value.isNotEmpty() && !forceReload) {
//            if(debugModeOn) {
//                //如果列表能恢复，那上次的oid应该也能恢复，问题不大
//                println("nextCommitOid.value="+nextCommitOid.value)
//            }
//            list.requireRefreshView()
            return@doLoadMore
        }

        //加载更多
        //这个用scope，似乎会随页面释放而取消任务？不知道是否需要我检查CancelException？
        doJobThenOffLoading job@{
            loadLock.value.withLock {
                loadMoreLoading.value = true
                loadMoreText.value = appContext.getString(R.string.loading)

                try {
                    if (firstLoad || repositoryForRevWalk.value==null || revwalk.value==null) {
                        // do reset: clear list and release old repo instance
                        //如果是第一次加载或刷新页面（重新初始化页面），清下列表
                        // if is first load or refresh page, clear list
                        list.value.clear()

                        // close old repo, release resource
                        repositoryForRevWalk.value?.close()
                        repositoryForRevWalk.value = null  // if don't set to null, when assign new instance to state, implicitly call equals(), the closed repo will thrown an err


                        // do init: create new repo instance
                        val repo = Repository.open(repoFullPath)
                        //get new revwalk instance
                        val newRevwalk = Libgit2Helper.createRevwalk(repo, oid)
                        if(newRevwalk == null) {
                            val oidStr = oid.toString()
                            Msg.requireShowLongDuration(replaceStringResList(appContext.getString(R.string.create_revwalk_failed_oid), listOf(Libgit2Helper.getShortOidStrByFull(oidStr))))
                            createAndInsertError(repoId, "create Revwalk failed, oid=$oidStr")
                            return@job
                        }

//                    println("repo.equals(repositoryForRevWalk.value):${repo.equals(repositoryForRevWalk.value)}")  // expect: false, output: false

                        // the revwalk must use with the repo instance which created it, else will throw an err "signed...prefix -..." something
                        // revwalk必须与创建它的仓库一起使用，否则会报错，报什么"signed...prefix -..."之类的错误
                        repositoryForRevWalk.value = repo
                        revwalk.value = newRevwalk
                        nextCommitOid.value = newRevwalk.next() ?: Cons.allZeroOid

//                    println("oldRepoInstance == repositoryForRevWalk.value:${oldRepoInstance == repositoryForRevWalk.value}")  // expect:false, output:false
                        // release memory
//                    oldRepoInstance?.close()
                    }

                    val repo = repositoryForRevWalk.value ?: throw RuntimeException("repo for revwalk is null")

                    if(nextCommitOid.value.isNullOrEmptyOrZero) {
                        //更新变量
                        hasMore.value = false
                        loadMoreText.value = appContext.getString(R.string.end_of_the_list)
                    }else {
                        //start travel commit history
                        Libgit2Helper.getCommitList(
                            repo,
                            revwalk.value!!,
                            nextCommitOid.value,
                            repoId,
                            if(loadToEnd) Int.MAX_VALUE else pageSize.value,
                            retList = list.value,  //直接赋值给状态列表了，若性能差，可实现一个批量添加机制，比如查出50个条目添加一次，之类的
                            loadChannel = loadChannel,
                            checkChannelFrequency = settings.commitHistoryLoadMoreCheckAbortSignalFrequency
                        )

                        //update state
                        nextCommitOid.value = revwalk.value!!.next() ?: Cons.allZeroOid
                        hasMore.value = !nextCommitOid.value.isNullOrEmptyOrZero
                        loadMoreText.value = if (hasMore.value) appContext.getString(R.string.load_more) else appContext.getString(R.string.end_of_the_list)

                    }

                    loadMoreLoading.value = false

                }catch (e:Exception) {
                    val errMsg = e.localizedMessage ?: "unknown err"
                    Msg.requireShowLongDuration(errMsg)
                    createAndInsertError(repoId, "err: $errMsg")
                    MyLog.e(TAG, "#doLoadMore: err: ${e.stackTraceToString()}")
                }
            }
        }
    }

    val clipboardManager = LocalClipboardManager.current
    val showViewDialog = rememberSaveable { mutableStateOf(false)}
    val viewDialogText = rememberSaveable { mutableStateOf("")}
    val viewDialogTitle = rememberSaveable { mutableStateOf("")}

    val requireShowViewDialog = { title: String, text: String ->
        viewDialogTitle.value = title
        viewDialogText.value = text
        showViewDialog.value = true
    }

    if (showViewDialog.value) {
        ConfirmDialog(
            title = viewDialogTitle.value,
            requireShowTextCompose = true,
            textCompose = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                ) {
                    Text(
                        text = viewDialogText.value
                    )
                }
            },
            cancelBtnText = stringResource(id = R.string.close),
            okBtnText = stringResource(id = R.string.copy),
            onCancel = {
                showViewDialog.value = false

            }
        ) { //复制到剪贴板
            showViewDialog.value = false
            clipboardManager.setText(AnnotatedString(viewDialogText.value))
            requireShowToast(appContext.getString(R.string.copied))

        }
    }

    //参数1，要创建的本地分支名；2是否基于HEAD创建分支，3如果不基于HEAD，提供一个引用名
    //只有在basedHead为假的时候，才会使用baseRefSpec
//    val doCreateBranch: (String, String, Boolean) -> Ret<Triple<String, String, String>?> = doCreateBranch@{ branchNamePram: String, baseRefSpec: String, overwriteIfExists:Boolean ->
//            Repository.open(curRepo.value.fullSavePath).use { repo ->
//
//                //第4个参数是base head，在提交页面创建，肯定不base head，base head是在分支页面用顶栏的按钮创建分支的默认选项
//                val ret = Libgit2Helper.doCreateBranch(
//                    repo,
//                    repoId,
//                    branchNamePram,
//                    false,
//                    baseRefSpec,
//                    false,
//                    overwriteIfExists
//                )
//
//                return@doCreateBranch ret
//            }
//        }
//
//    val doCheckoutBranch: suspend (String, String, String, Boolean, Boolean,Boolean, Int) -> Ret<Oid?> =
//        doCheckoutLocalBranch@{ shortBranchNameOrHash: String, fullBranchNameOrHash: String, upstreamBranchShortNameParam: String, isDetachCheckout: Boolean , force:Boolean, updateHead:Boolean, checkoutType:Int->
//            Repository.open(curRepo.value.fullSavePath).use { repo ->
//                val ret = Libgit2Helper.doCheckoutBranchThenUpdateDb(
//                    repo,
//                    repoId,
//                    shortBranchNameOrHash,
//                    fullBranchNameOrHash,
//                    upstreamBranchShortNameParam,
//                    checkoutType,
//                    force,
//                    updateHead
//                )
//
//                return@doCheckoutLocalBranch ret
//            }
//        }
//
//    val checkoutOptionDontUpdateHead = 0
//    val checkoutOptionDetachHead = 1
//    val checkoutOptionCreateBranch = 2
//    val checkoutOptionDefault = checkoutOptionCreateBranch  //默认选中创建分支，detach head如果没reflog，有可能丢数据
//    val checkoutRemoteOptions = listOf(
//        appContext.getString(R.string.dont_update_head),
//        appContext.getString(R.string.detach_head),
//        appContext.getString(R.string.new_branch) + "(" + appContext.getString(R.string.recommend) + ")"
//    )

//    val checkoutSelectedOption = StateUtil.getRememberSaveableIntState(initValue = checkoutOptionDefault)
//    val checkoutRemoteCreateBranchName = StateUtil.getRememberSaveableState(initValue = "")
//    val checkoutUserInputCommitHash = StateUtil.getRememberSaveableState(initValue = "")
    val requireUserInputCommitHash = rememberSaveable { mutableStateOf(false)}
//    val forceCheckout = StateUtil.getRememberSaveableState(initValue = false)
    val showCheckoutDialog = rememberSaveable { mutableStateOf(false)}
    //当前长按commit在列表中的索引，用来更新单个条目时使用，为-1时无效，不要执行操作
    val curCommitIndex = rememberSaveable{mutableIntStateOf(-1)}
//    val initCheckoutDialog = { requireUserInputHash:Boolean ->
//        checkoutSelectedOption.intValue = checkoutOptionDefault
//        requireUserInputCommitHash.value = requireUserInputHash
//        forceCheckout.value = false
//        showCheckoutDialog.value = true
//    }


    val updateCurCommitInfo = {repoFullPath:String, curCommitIdx:Int, commitOid:String, list:MutableList<CommitDto> ->
        doActIfIndexGood(curCommitIdx, list) {
            Repository.open(repoFullPath).use { repo ->
                val reQueriedCommitInfo = Libgit2Helper.getSingleCommit(repo, repoId, commitOid)
                list[curCommitIdx] = reQueriedCommitInfo
            }
        }

    }


    //filter相关，开始
    val filterKeyword = mutableCustomStateOf(
        keyTag = stateKeyTag,
        keyName = "filterKeyword",
        initValue = TextFieldValue("")
    )
    val filterModeOn = rememberSaveable { mutableStateOf(false)
}
    //存储符合过滤条件的条目在源列表中的真实索引。本列表索引对应filter list条目索引，值对应原始列表索引
    val filterIdxList = mutableCustomStateListOf(
        keyTag = stateKeyTag,
        keyName = "filterIdxList",
        listOf<Int>()
    )

    //filter相关，结束


    val nameOfNewTag = rememberSaveable { mutableStateOf("")}
    val overwriteIfNameExistOfNewTag = rememberSaveable { mutableStateOf(false)}  // force
    val showDialogOfNewTag = rememberSaveable { mutableStateOf(false)}
    val hashOfNewTag = rememberSaveable { mutableStateOf( "")}
    val msgOfNewTag = rememberSaveable { mutableStateOf( "")}
//    val requireUserInputHashOfNewTag = StateUtil.getRememberSaveableState(initValue = false)
    val annotateOfNewTag = rememberSaveable { mutableStateOf(false)}
    val initNewTagDialog = { hash:String ->
        hashOfNewTag.value = hash  //把hash设置为当前选中的commit的hash

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
            force = overwriteIfNameExistOfNewTag,
        ) success@{newTagOidStr ->
            if(newTagOidStr.isBlank()) {  //should never into here
                Msg.requireShowLongDuration(appContext.getString(R.string.tag_oid_invalid))
                return@success
            }

            // update item
            val curOidStr = curCommit.value.oidStr
            val curIdx = curCommitIndex.intValue

            //如果没开filter模式且最终创建的tag和长按条目一致，直接更新长按条目；若开了filter模式，则必须更新原始列表，而这里设置的长按条目索引是filterList的，所以无效，需要重新从原始列表查找对应条目索引，然后更新原始列表以显示最新条目
            if(!filterModeOn.value && newTagOidStr == curOidStr) {
                //更新当前条目以显示新创建的tag(仅适用于更新当前长按条目)
                updateCurCommitInfo(curRepo.value.fullSavePath, curIdx, curOidStr, list.value)
            }else {  //最终创建的tag和长按条目不一致，查找并更新对应条目
                //x 无需处理过滤列表，我测试了下，过滤模式创建分支和tag都没问题，能正常显示新tag和分支，因为只要更新原始列表相关代码就会重新执行，filterList也会重新生成) 更新过滤列表
//                if(enableFilter) {
//                    //更新filter列表
//                }

                //更新普通列表
                val list = list.value
                val idx = list.toList().indexOfFirst { it.oidStr==newTagOidStr }
                if(idx != -1) {  //不等于-1代表找到了
                    updateCurCommitInfo(curRepo.value.fullSavePath, idx, newTagOidStr, list)
                }

            }

//            changeStateTriggerRefreshPage(needRefresh)  //创建tag后没必要刷新整个页面，更新对应commit即可
        }
    }

//    val getCheckoutOkBtnEnabled:()->Boolean = getCheckoutOkBtnEnabled@{
//        //请求checkout时创建分支但没填分支，返回假
//        if(checkoutSelectedOption.intValue == checkoutOptionCreateBranch && checkoutRemoteCreateBranchName.value.isBlank()) {
//            return@getCheckoutOkBtnEnabled false
//        }
//
//        //请求checkout to hash但没填hash，返回假
//        if(requireUserInputCommitHash.value && checkoutUserInputCommitHash.value.isBlank()) {
//            return@getCheckoutOkBtnEnabled false
//        }
//
//        return@getCheckoutOkBtnEnabled true
//    }


    //初始化组件版本的checkout对话框
    val initCheckoutDialogComposableVersion = { requireUserInputHash:Boolean ->
        requireUserInputCommitHash.value = requireUserInputHash
        showCheckoutDialog.value = true
    }

    if(showCheckoutDialog.value) {
        CheckoutDialog(
            showCheckoutDialog=showCheckoutDialog,
            from = CheckoutDialogFrom.OTHER,
            expectCheckoutType = Cons.checkoutType_checkoutCommitThenDetachHead,
            curRepo = curRepo.value,
            shortName = curCommit.value.shortOidStr,
            fullName = curCommit.value.oidStr,
            curCommitOid = curCommit.value.oidStr,
            curCommitShortOid = curCommit.value.shortOidStr,
            requireUserInputCommitHash = requireUserInputCommitHash.value,
            loadingOn = loadingOn,
            loadingOff = loadingOff,
            onlyUpdateCurItem = useFullOid,
            updateCurItem = {curItemIdx, fullOid-> updateCurCommitInfo(curRepo.value.fullSavePath, curItemIdx, fullOid, list.value)},
            refreshPage = { changeStateTriggerRefreshPage(needRefresh, StateRequestType.forceReload) },
            curCommitIndex = if(filterModeOn.value) -1 else curCommitIndex.intValue,  //若开了filter模式，则一律在原始列表重新查找条目索引（传无效索引-1即可触发查找），不然可能会更新错条目
            findCurItemIdxInList = { fullOid->
                list.value.toList().indexOfFirst { it.oidStr == fullOid }
            }
        )
    }


    val resetOid = rememberSaveable { mutableStateOf("")}
//    val acceptHardReset = StateUtil.getRememberSaveableState(initValue = false)
    val showResetDialog = rememberSaveable { mutableStateOf(false)}
    val closeResetDialog = {
        showResetDialog.value = false
    }

    if (showResetDialog.value) {
        ResetDialog(
            fullOidOrBranchOrTag = resetOid,
            closeDialog=closeResetDialog,
            repoFullPath = curRepo.value.fullSavePath,
            repoId=curRepo.value.id,
            refreshPage = { oldHeadCommitOid, isDetached ->
                //顺便更新下仓库的detached状态，因为若从分支条目进来，不怎么常刷新页面，所以仓库状态可能过时
                curRepo.value.isDetached = boolToDbInt(isDetached)

                //如果从仓库卡片点击提交号进入 或 从分支列表点击分支条目进入但点的是当前HEAD指向的分支，则刷新页面，重载列表
                if(!useFullOid || isCurrent) {  // from repoCard tap commit hash in this page or from branch list tap branch of HEAD, will in this if block
                    //从分支页面进这个页面，不会强制重刷列表，但如果当前显示的分支是仓库的当前分支(被HEAD指向)，那如果reset hard 成功，就得更新下提交列表，于是，就通过更新fullOid来重设当前页面的起始hash
                    fullOid.value=curCommit.value.oidStr  // only make sense when come this page from branch list page with condition `useFullOid==true && isCurrent==true`,update it for show latest commit list of current branch of repo when hard reset success.
                    changeStateTriggerRefreshPage(needRefresh, StateRequestType.forceReload)
                }else {  //useFullOid==true && isCurrent==false, from branch list page tap a branch item which is not pointed by HEAD(isCurrent==false), will in this block
                    val curCommitIdx = if(filterModeOn.value) {
                        try {  //取出当前长按条目在源列表中的索引
                            filterIdxList.value[curCommitIndex.intValue]
                        }catch (_:Exception) {
                            -1
                        }
                    } else {  //非过滤模式，此值直接就是源列表索引
                        curCommitIndex.intValue
                    }

                    val repoFullPath = curRepo.value.fullSavePath
                    val commitList = list.value
                    //需要更新两个提交：一个是当前hard reset的目标提交，需要更新信息以使其显示刚才HEAD关联的分支名；一个是HEAD在reset之前指向的提交，需更新以使其删除HEAD当前关联的分支名。
                    //执行到这，代表在分支页面点击分支条目进入提交列表，然后长按某个commit执行了reset，这时，如果当前非detached HEAD，则当前HEAD指向的分支会指向当前提交，所以应当更新当前选中的提交信息以显示新指向它的分支
                    //遍历当前列表，若包含上个提交号，则更新其信息（目的是为了移除HEAD之前指向的分支的首个提交上的分支名，现在那个分支头已经被Hard Reset到新分支上了，不应再在旧提交上显示)
                    if(!isDetached) { //如果当前非detached，遍历commits条目，把当前分支从旧commit的分支列表移除；if is detached, Hard Reset will not update any branch, so need not update commit info
                        //更新新提交以显示HEAD指向的分支

                        //非过滤模式，直接根据长按条目的索引更新条目信息；若是过滤模式，则会在下边遍历列表更新旧条目信息时顺便更新原始列表中当前长按条目的信息

                        val curCommitOidStr= curCommit.value.oidStr  //当前长按触发reset的条目的full oid

                        // 触发当前reset的被长按的条目是否被更新
                        var curItemUpdated = if(isGoodIndexForList(curCommitIdx, commitList)) {
                            updateCurCommitInfo(repoFullPath, curCommitIdx, curCommitOidStr, commitList)
                            true  //非过滤模式，直接用长按条目索引更新此条目即可
                        }else{
                            false  //后续在循环中更新旧head关联的提交时顺便更新此条目
                        }

                        var oldUpdated = false  //之前指向被更新的分支的条目信息是否已刷新

                        //更新旧提交以删除HEAD指向的提交
                        for((idx, commit) in commitList.toList().withIndex()) {
                            if(oldUpdated && curItemUpdated) {
                                break
                            }

                            if(!curItemUpdated && commit.oidStr == curCommitOidStr) {
                                updateCurCommitInfo(repoFullPath, idx, commit.oidStr, commitList)
                                curItemUpdated = true
                            }

                            //有个小小缺陷，若当前条目和旧条目相同，会更新两次，不过问题不大
                            if(!oldUpdated && commit.oidStr == oldHeadCommitOid) {
                                updateCurCommitInfo(repoFullPath, idx, commit.oidStr, commitList)
                                oldUpdated = true
                            }
                        }
                    }
                }
            }
        )

    }

    val showDetailsDialog = rememberSaveable { mutableStateOf( false)}
    val detailsString = rememberSaveable { mutableStateOf( "")}
    if(showDetailsDialog.value) {
        CopyableDialog(
            title = "'${curCommit.value.shortOidStr}'"+ " " +stringResource(id = R.string.details),
            text = detailsString.value,
            onCancel = { showDetailsDialog.value = false }
        ) {
            showDetailsDialog.value = false
            clipboardManager.setText(AnnotatedString(detailsString.value))
            Msg.requireShow(appContext.getString(R.string.copied))
        }
    }

    // 向下滚动监听，开始
    val pageScrolled = remember { mutableStateOf(settings.showNaviButtons) }

    val requireBlinkIdx = rememberSaveable{mutableIntStateOf(-1)}

//    val filterListState =mutableCustomStateOf(keyTag = stateKeyTag, keyName = "filterListState", LazyListState(0,0))
    val filterListState = rememberLazyListState()
    val enableFilterState = rememberSaveable { mutableStateOf(false)}
//    val firstVisible = remember { derivedStateOf { if(enableFilterState.value) filterListState.value.firstVisibleItemIndex else listState.firstVisibleItemIndex } }
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
//            val nowAt = if(enableFilterState.value) {
//                filterListState.firstVisibleItemIndex
//            } else {
//                listState.firstVisibleItemIndex
//            }
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


    val diffCommitsDialogCommit1 = rememberSaveable { mutableStateOf("")}
    val diffCommitsDialogCommit2 = rememberSaveable { mutableStateOf("")}
    if(showDiffCommitDialog.value) {
        DiffCommitsDialog(
            showDiffCommitDialog,
            diffCommitsDialogCommit1,
            diffCommitsDialogCommit2,
            curRepo.value
        )
    }


    val savePatchPath= rememberSaveable { mutableStateOf("")}
    val showSavePatchSuccessDialog = rememberSaveable { mutableStateOf(false)}

    if(showSavePatchSuccessDialog.value) {
        val path = savePatchPath.value

        CopyableDialog(
            title = stringResource(R.string.success),
            text = replaceStringResList(stringResource(R.string.export_path_ph1_you_can_go_to_files_page_found_this_file), listOf(path)),
            okBtnText = stringResource(R.string.copy_path),
            onCancel = { showSavePatchSuccessDialog.value = false }
        ) {
            showSavePatchSuccessDialog.value = false

            clipboardManager.setText(AnnotatedString(path))
            Msg.requireShow(appContext.getString(R.string.copied))
        }
    }



    val showCreatePatchDialog = rememberSaveable { mutableStateOf(false)}
    val createPatchTargetHash = rememberSaveable { mutableStateOf("")}
    val createPatchParentHash = rememberSaveable { mutableStateOf("")}
    val createPatchParentList = mutableCustomStateListOf(keyTag = stateKeyTag, keyName = "createPatchParentList", listOf<String>())

    val initCreatePatchDialog = { targetFullHash:String, defaultParentFullHash:String, parentList:List<String> ->
        createPatchParentList.value.clear()
        createPatchParentList.value.addAll(parentList)

        createPatchTargetHash.value = targetFullHash
        createPatchParentHash.value = defaultParentFullHash


        showCreatePatchDialog.value = true
    }


    if(showCreatePatchDialog.value) {
        val shortTarget = Libgit2Helper.getShortOidStrByFull(createPatchTargetHash.value)
        val shortParent = Libgit2Helper.getShortOidStrByFull(createPatchParentHash.value)

        val padding=10.dp

        ConfirmDialog(
            okBtnText = stringResource(R.string.ok),
            cancelBtnText = stringResource(R.string.cancel),
            title = stringResource(R.string.create_patch),
            requireShowTextCompose = true,
            textCompose = {
                Column{
                    Text(text =  buildAnnotatedString {
                        append(stringResource(R.string.target)+": ")
                        withStyle(style = SpanStyle(fontWeight = FontWeight.ExtraBold)) {
                            append(shortTarget)
                        }
                    },
                        modifier = Modifier.padding(horizontal = padding)
                    )

                    Row(modifier = Modifier.padding(padding)) {
                        Text(text = stringResource(R.string.select_a_parent_for_find_changes)+":")
                    }

                    SingleSelectList(
                        optionsList = createPatchParentList.value,
                        selectedOptionIndex = null,
                        selectedOptionValue = createPatchParentHash.value,
                        menuItemSelected = {_, value-> value==createPatchParentHash.value},
                        menuItemOnClick = {idx, value ->
                            createPatchParentHash.value = value
                        },
                        menuItemFormatter = {_, value ->
                            Libgit2Helper.getShortOidStrByFull(value?:"")
                        }
                    )

//
//                    MyLazyColumn(
//                        modifier = Modifier.heightIn(max=150.dp),
//                        requireUseParamModifier = true,
//                        contentPadding = PaddingValues(0.dp),
//                        list = createPatchParentList.value,
//                        listState = StateUtil.getRememberLazyListState(),
//                        requireForEachWithIndex = true,
//                        requirePaddingAtBottom =false
//                    ) {k, optext ->
//                        Row(
//                            Modifier
//                                .fillMaxWidth()
//                                .heightIn(min = MyStyleKt.RadioOptions.minHeight)
//
//                                .selectable(
//                                    selected = createPatchParentHash.value == optext,
//                                    onClick = {
//                                        //更新选择值
//                                        createPatchParentHash.value = optext
//                                    },
//                                    role = Role.RadioButton
//                                )
//                                .padding(horizontal = padding),
//                            verticalAlignment = Alignment.CenterVertically
//                        ) {
//                            RadioButton(
//                                selected = createPatchParentHash.value == optext,
//                                onClick = null // null recommended for accessibility with screenreaders
//                            )
//                            Text(
//                                text = Libgit2Helper.getShortOidStrByFull(optext),
//                                style = MaterialTheme.typography.bodyLarge,
//                                modifier = Modifier.padding(start = padding)
//                            )
//                        }
//
//                    }
                }
            },
            onCancel = { showCreatePatchDialog.value = false }
        ) {
            showCreatePatchDialog.value = false

            doJobThenOffLoading(
                loadingOn,
                loadingOff,
                appContext.getString(R.string.creating_patch)
            ) {
                try {
                    val left = createPatchParentHash.value
                    val right = createPatchTargetHash.value

                    Repository.open(curRepo.value.fullSavePath).use { repo->
                        val tree1 = Libgit2Helper.resolveTree(repo, left) ?: throw RuntimeException("resolve left tree failed, 10137466")
                        val tree2 = Libgit2Helper.resolveTree(repo, right) ?: throw RuntimeException("resolve right tree failed, 11015534")

                        // 注意应该是：parent..target，parent在左
                        val outFile = FsUtils.Patch.newPatchFile(curRepo.value.repoName, left, right)

                        val ret = Libgit2Helper.savePatchToFileAndGetContent(
                            outFile=outFile,
                            repo = repo,
                            tree1 = tree1,
                            tree2 = tree2,
                            fromTo = Cons.gitDiffFromTreeToTree,
                            reverse = false,
                            treeToWorkTree = false,
                            returnDiffContent = false  //是否返回输出的内容，若返回，可在ret中取出字符串
                        )

                        if(ret.hasError()) {
                            Msg.requireShowLongDuration(ret.msg)
                            if(ret.code != Ret.ErrCode.alreadyUpToDate) {  //如果错误码不是 Already up-to-date ，就log下

                                //选提交时记日志把files改成commit用来区分
                                createAndInsertError(repoId, "create patch of '$shortParent..$shortTarget' err:"+ret.msg)
                            }
                        }else {
                            //输出格式： /puppygitDataDir/patch/xxxx..xxxx，可前往Files页面通过Go To功能跳转到对应目录并选中文件
//                            savePatchPath.value = getFilePathStrBasedRepoDir(outFile.canonicalPath, returnResultStartsWithSeparator = true)
                            savePatchPath.value = outFile.canonicalPath
                            showSavePatchSuccessDialog.value = true
                        }
                    }
                }catch (e:Exception) {
                    val errPrefix = "create patch err:"
                    Msg.requireShowLongDuration(e.localizedMessage ?: errPrefix)
                    createAndInsertError(curRepo.value.id, errPrefix+e.localizedMessage)
                }

            }
        }
    }





    val showCherrypickDialog = rememberSaveable { mutableStateOf(false)}
    val cherrypickTargetHash = rememberSaveable { mutableStateOf("")}
    val cherrypickParentHash = rememberSaveable { mutableStateOf("")}
    val cherrypickParentList = mutableCustomStateListOf(keyTag = stateKeyTag, keyName = "cherrypickParentList", listOf<String>())
    val cherrypickAutoCommit = rememberSaveable { mutableStateOf(false)}

    val initCherrypickDialog = { targetFullHash:String, defaultParentFullHash:String, parentList:List<String> ->
        cherrypickParentList.value.clear()
        cherrypickParentList.value.addAll(parentList)

        cherrypickTargetHash.value = targetFullHash
        cherrypickParentHash.value = defaultParentFullHash

        cherrypickAutoCommit.value = false

        showCherrypickDialog.value = true
    }

    if(showCherrypickDialog.value) {
        val shortTarget = Libgit2Helper.getShortOidStrByFull(cherrypickTargetHash.value)
        val shortParent = Libgit2Helper.getShortOidStrByFull(cherrypickParentHash.value)

        val padding=10.dp

        ConfirmDialog(
            okBtnText = stringResource(R.string.ok),
            cancelBtnText = stringResource(R.string.cancel),
            title = stringResource(R.string.cherrypick),
            requireShowTextCompose = true,
            textCompose = {
                Column{
                    Text(text =  buildAnnotatedString {
                        append(stringResource(R.string.target)+": ")
                        withStyle(style = SpanStyle(fontWeight = FontWeight.ExtraBold)) {
                            append(shortTarget)
                        }
                    },
                        modifier = Modifier.padding(horizontal = padding)
                    )

                    Row(modifier = Modifier.padding(padding)) {
                        Text(text = stringResource(R.string.select_a_parent_for_find_changes)+":")
                    }


                    SingleSelectList(
                        optionsList = cherrypickParentList.value,
                        selectedOptionIndex = null,
                        selectedOptionValue = cherrypickParentHash.value,
                        menuItemSelected = {_, value-> value==cherrypickParentHash.value},
                        menuItemOnClick = {idx, value ->
                            cherrypickParentHash.value = value
                        },
                        menuItemFormatter = {_, value ->
                            Libgit2Helper.getShortOidStrByFull(value?:"")
                        }
                    )

//
//                    MyLazyColumn(
//                        modifier = Modifier.heightIn(max=150.dp),
//                        requireUseParamModifier = true,
//                        contentPadding = PaddingValues(0.dp),
//                        list = cherrypickParentList.value,
//                        listState = StateUtil.getRememberLazyListState(),
//                        requireForEachWithIndex = true,
//                        requirePaddingAtBottom =false
//                    ) {k, optext ->
//                        Row(
//                            Modifier
//                                .fillMaxWidth()
//                                .heightIn(min = MyStyleKt.RadioOptions.minHeight)
//
//                                .selectable(
//                                    selected = cherrypickParentHash.value == optext,
//                                    onClick = {
//                                        //更新选择值
//                                        cherrypickParentHash.value = optext
//                                    },
//                                    role = Role.RadioButton
//                                )
//                                .padding(horizontal = padding),
//                            verticalAlignment = Alignment.CenterVertically
//                        ) {
//                            RadioButton(
//                                selected = cherrypickParentHash.value == optext,
//                                onClick = null // null recommended for accessibility with screenreaders
//                            )
//                            Text(
//                                text = Libgit2Helper.getShortOidStrByFull(optext),
//                                style = MaterialTheme.typography.bodyLarge,
//                                modifier = Modifier.padding(start = padding)
//                            )
//                        }
//
//                    }



                    Spacer(modifier = Modifier.height(padding))

                    MyCheckBox(text = stringResource(R.string.auto_commit), value = cherrypickAutoCommit)
                }
            },
            onCancel = { showCherrypickDialog.value = false }
        ) {
            showCherrypickDialog.value = false

            doJobThenOffLoading(
                loadingOn,
                loadingOff,
                appContext.getString(R.string.cherrypicking)
            ) {
                Repository.open(curRepo.value.fullSavePath).use { repo->
                    val ret = Libgit2Helper.cherrypick(
                        repo,
                        targetCommitFullHash = cherrypickTargetHash.value,
                        parentCommitFullHash = cherrypickParentHash.value,
                        autoCommit = cherrypickAutoCommit.value
                    )

                    if(ret.hasError()) {
                        Msg.requireShowLongDuration(ret.msg)
                        if(ret.code != Ret.ErrCode.alreadyUpToDate) {  //如果错误码不是 Already up-to-date ，就log下

                            //选提交时记日志把files改成commit用来区分
                            createAndInsertError(repoId, "cherrypick commit changes of '$shortParent..$shortTarget' err:"+ret.msg)
                        }
                    }else {
                        Msg.requireShow(appContext.getString(R.string.success))
                    }
                }
            }
        }
    }



    val invalidPageSize = -1
    val minPageSize = 1  // make sure it bigger than `invalidPageSize`

    val isInvalidPageSize = { ps:Int ->
        ps < minPageSize
    }

    val showSetPageSizeDialog = rememberSaveable { mutableStateOf(false) }
    val pageSizeForDialog = rememberSaveable { mutableStateOf(""+pageSize.value) }

    if(showSetPageSizeDialog.value) {
        ConfirmDialog2(
            title = stringResource(R.string.page_size),
            requireShowTextCompose = true,
            textCompose = {
                ScrollableColumn {
                    TextField(
                        modifier = Modifier.fillMaxWidth(),

                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),

                        value = pageSizeForDialog.value,
                        singleLine = true,
                        onValueChange = {
                            pageSizeForDialog.value = it
                        },
                        label = {
                            Text(stringResource(R.string.page_size))
                        },
                    )

                    Spacer(Modifier.height(10.dp))

                    MyCheckBox(text= stringResource(R.string.remember), rememberPageSize)
                }
            },
            onCancel = {showSetPageSizeDialog.value=false}
        ) {
            showSetPageSizeDialog.value=false

            try {
                val newPageSize = try {
                    pageSizeForDialog.value.toInt()
                }catch (_:Exception) {
                    Msg.requireShow(appContext.getString(R.string.invalid_number))
                    invalidPageSize
                }

                if(!isInvalidPageSize(newPageSize)) {
                    pageSize.value = newPageSize

                    if(rememberPageSize.value) {
                        SettingsUtil.update {
                            it.commitHistoryPageSize = newPageSize
                        }
                    }
                }

            }catch (e:Exception) {
                MyLog.e(TAG, "#SetPageSizeDialog err: ${e.localizedMessage}")
            }
        }
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
                    }else{
                        val repoAndBranchText = if(useFullOid) branchShortNameOrShortHashByFullOidForShowOnTitle.value else repoOnBranchOrDetachedHash.value
                        Column(
                            modifier = Modifier.combinedClickable(
                                onDoubleClick = {
                                    //双击返回列表顶部
                                    goToTop()
                                },
                                onLongClick = {
                                    //长按显示仓库和分支信息
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
//                                    Msg.requireShow(repoAndBranchText)
                                    // show loaded how many items
                                    Msg.requireShow("loaded: ${list.value.size}")
                                }
                            ) { // onClick

                            }
                        ) {
                            Row(
                                modifier = Modifier
                                    .horizontalScroll(rememberScrollState()),
                            ) {
                                Text(
                                    text = stringResource(R.string.commit_history),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )

                            }
                            Row(
                                modifier = Modifier
                                    .horizontalScroll(rememberScrollState()),
                            ) {
                                Text(
                                    text = repoAndBranchText,
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
                    } else {
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
                                icon = Icons.Filled.FilterAlt,
                                iconContentDesc = stringResource(R.string.filter),
                            ) {
                                // filter item
                                filterKeyword.value = TextFieldValue("")
                                filterModeOn.value = true
                            }

                            //刷新按钮
                            LongPressAbleIconBtn(
                                tooltipText = stringResource(R.string.refresh),
                                icon = Icons.Filled.Refresh,
                                iconContentDesc = stringResource(id = R.string.refresh),
                                enabled = true,

                            ) {
                                goToTop()
                                changeStateTriggerRefreshPage(
                                    needRefresh,
                                    StateRequestType.forceReload
                                )
                            }
                            LongPressAbleIconBtn(
                                tooltipText = stringResource(R.string.checkout_to),
                                icon = Icons.Filled.MoveToInbox,
                                iconContentDesc = stringResource(id = R.string.checkout_to),
                                enabled = true,

                            ) {
                                val requireUserInputHash = true
                                initCheckoutDialogComposableVersion(
                                    requireUserInputHash
                                )
                            }

                            if((proFeatureEnabled(commitsDiffCommitsTestPassed) || proFeatureEnabled(resetByHashTestPassed))) {
    //                            显示more三点菜单
                                LongPressAbleIconBtn(
                                    tooltipText = stringResource(R.string.menu),
                                    icon = Icons.Filled.MoreVert,
                                    iconContentDesc = stringResource(id = R.string.menu),
                                    enabled = true,
                                ) {
                                    showTopBarMenu.value = true
                                }

                            }
                        }

                        if(showTopBarMenu.value) {
                            Row (modifier = Modifier.padding(top = MyStyleKt.TopBar.dropDownMenuTopPaddingSize)) {
                                DropdownMenu(
                                    expanded = showTopBarMenu.value,
                                    onDismissRequest = { showTopBarMenu.value=false }
                                ) {
    //                                    选项“diff commits”，点击弹窗，让用户输入两个提交号，跳转到tree to tree页面比较这两个提交
                                    if(proFeatureEnabled(commitsDiffCommitsTestPassed)) {
                                        DropdownMenuItem(
                                            text = { Text(stringResource(R.string.diff_commits)) },
                                            onClick = {
                                                showDiffCommitDialog.value = true

                                                //关闭顶栏菜单
                                                showTopBarMenu.value = false
                                            }
                                        )
                                    }

                                    if(proFeatureEnabled(resetByHashTestPassed)){
                                        DropdownMenuItem(
                                            text = { Text(stringResource(R.string.reset)) },
                                            onClick = {
                                                showResetDialog.value = true

                                                //关闭顶栏菜单
                                                showTopBarMenu.value = false
                                            }
                                        )
                                    }

                                    DropdownMenuItem(
                                        text = { Text(stringResource(R.string.page_size)) },
                                        onClick = {
                                            pageSizeForDialog.value = ""+pageSize.value
                                            showSetPageSizeDialog.value = true

                                            //关闭顶栏菜单
                                            showTopBarMenu.value = false
                                        }
                                    )
                                }



                            }

                        }
                    }
                },
                scrollBehavior = homeTopBarScrollBehavior,
            )
        },
        floatingActionButton = {
            if(pageScrolled.value) {

                GoToTopAndGoToBottomFab(
                    filterModeOn = enableFilterState,
                    scope = scope,
                    filterListState = filterListState,
                    listState = listState,
                    pageScrolled = pageScrolled
                )

            }
        }
    ) { contentPadding ->
//        val commitLen = 10;
        if (showBottomSheet.value) {
//            var commitOid = curCommit.value.oidStr
//            if(commitOid.length > Cons.gitShortCommitHashRangeEndInclusive) {  //避免commitOid不够长导致抛异常，正常来说commitOid是40位，不会有问题，除非哪里出了问题
//                commitOid = commitOid.substring(Cons.gitShortCommitHashRange)+"..."
//            }
            BottomSheet(showBottomSheet, sheetState, curCommit.value.shortOidStr) {
                BottomSheetItem(sheetState, showBottomSheet, stringResource(R.string.checkout)) {
                    // onClick()
                    // 弹出确认框，询问是否确定执行checkout，可detach head，可创建分支，类似checkout remote branch
                    //初始化弹窗默认选项
                    val requireUserInputHash = false
                    initCheckoutDialogComposableVersion(requireUserInputHash)
                }
                if(dev_EnableUnTestedFeature || tagsTestPassed) {
                    BottomSheetItem(sheetState, showBottomSheet, stringResource(R.string.new_tag)) {
                        // onClick()
                        // 弹出确认框，询问是否确定执行checkout，可detach head，可创建分支，类似checkout remote branch
                        //初始化弹窗默认选项
                        initNewTagDialog(curCommit.value.oidStr)
                    }
                }
                if(proFeatureEnabled(resetByHashTestPassed)) {
                    BottomSheetItem(sheetState, showBottomSheet, stringResource(R.string.reset)) {
                        // onClick()
                        //初始化弹窗默认选项
//                    acceptHardReset.value = false
                        resetOid.value = curCommit.value.oidStr
                        showResetDialog.value = true
                    }
                }
                BottomSheetItem(sheetState, showBottomSheet, stringResource(R.string.details)) {
                    // onClick()
//                    requireShowViewDialog(appContext.getString(R.string.view_hash), curCommit.value.oidStr)
                    val sb = StringBuilder()
                    sb.appendLine("${appContext.getString(R.string.hash)}: "+curCommit.value.oidStr)
                    sb.appendLine()
                    sb.appendLine("${appContext.getString(R.string.author)}: "+ Libgit2Helper.getFormattedUsernameAndEmail(curCommit.value.author, curCommit.value.email))
                    sb.appendLine()
                    sb.appendLine("${appContext.getString(R.string.committer)}: "+ Libgit2Helper.getFormattedUsernameAndEmail(curCommit.value.committerUsername, curCommit.value.committerEmail))
                    sb.appendLine()
                    sb.appendLine("${appContext.getString(R.string.date)}: "+curCommit.value.dateTime)
                    sb.appendLine()
                    sb.appendLine("${appContext.getString(R.string.msg)}: "+curCommit.value.msg)
                    sb.appendLine()
                    if(curCommit.value.branchShortNameList.isNotEmpty()){
                        sb.appendLine((if(curCommit.value.branchShortNameList.size > 1) appContext.getString(R.string.branches) else appContext.getString(R.string.branch)) +": "+curCommit.value.branchShortNameList.toString())
                        sb.appendLine()
                    }
                    if(curCommit.value.tagShortNameList.isNotEmpty()) {
                        sb.appendLine((if(curCommit.value.tagShortNameList.size > 1) appContext.getString(R.string.tags) else appContext.getString(R.string.tag)) +": "+curCommit.value.tagShortNameList.toString())
                        sb.appendLine()
                    }
                    if(curCommit.value.parentOidStrList.isNotEmpty()) {
                        sb.appendLine((if(curCommit.value.parentOidStrList.size > 1) appContext.getString(R.string.parents) else appContext.getString(R.string.parent)) +": "+curCommit.value.parentOidStrList.toString())
                        sb.appendLine()
                    }

                    if(curCommit.value.hasOther()) {
                        sb.appendLine("${appContext.getString(R.string.other)}: ${curCommit.value.getOther()}")
                        sb.appendLine()
                    }

                    detailsString.value = sb.toString()
                    showDetailsDialog.value = true
                }

                if(UserUtil.isPro() && (dev_EnableUnTestedFeature || commitsDiffToLocalTestPassed)) {
                    BottomSheetItem(sheetState, showBottomSheet, stringResource(R.string.diff_to_local)) {
    //                    diff to local，点击跳转到tree to tree页面，然后diff
                        //当前比较的描述信息的key，用来在界面显示这是在比较啥，值例如“和父提交比较”或者“比较两个提交”之类的
                        val descKey = Cache.setThenReturnKey(appContext.getString(R.string.compare_to_local))
                        //这里需要传当前commit，然后cl页面会用当前commit查出当前commit的parents
                        val commit2 = Cons.gitLocalWorktreeCommitHash
                        val commitForQueryParents = Cons.allZeroOidStr
                        // url 参数： 页面导航id/repoId/treeoid1/treeoid2/desckey
                        navController.navigate(
                            //注意是 parentTreeOid to thisObj.treeOid，也就是 旧提交to新提交，相当于 git diff abc...def，比较的是旧版到新版，新增或删除或修改了什么，反过来的话，新增删除之类的也就反了
                            "${Cons.nav_TreeToTreeChangeListScreen}/${curRepo.value.id}/${curCommit.value.oidStr}/$commit2/$descKey/$commitForQueryParents"
                        )
                    }
                }

                if(proFeatureEnabled(diffToHeadTestPassed)) {
                    BottomSheetItem(sheetState, showBottomSheet, stringResource(R.string.diff_to_head)) {
                        doJobThenOffLoading job@{
                            Repository.open(curRepo.value.fullSavePath).use { repo->
                                //这里需要传当前commit，然后cl页面会用当前commit查出当前commit的parents
                                val commit2Ret = Libgit2Helper.getHeadCommit(repo)
                                if(commit2Ret.hasError()) {
                                    Msg.requireShowLongDuration(commit2Ret.msg)
                                    return@job
                                }

                                val commit2 = commit2Ret.data!!.id().toString()
                                val commit1 = curCommit.value.oidStr
                                if(commit2 == commit1) {  //避免 Compare HEAD to HEAD
                                    Msg.requireShowLongDuration(appContext.getString(R.string.num2_commits_same))
                                    return@job
                                }

                                //当前比较的描述信息的key，用来在界面显示这是在比较啥，值例如“和父提交比较”或者“比较两个提交”之类的
                                val descKey = Cache.setThenReturnKey(appContext.getString(R.string.compare_to_head))
                                val commitForQueryParents = Cons.allZeroOidStr

                                withMainContext {
                                    // url 参数： 页面导航id/repoId/treeoid1/treeoid2/desckey
                                    navController.navigate(
                                        //注意是 parentTreeOid to thisObj.treeOid，也就是 旧提交to新提交，相当于 git diff abc...def，比较的是旧版到新版，新增或删除或修改了什么，反过来的话，新增删除之类的也就反了
                                        "${Cons.nav_TreeToTreeChangeListScreen}/${curRepo.value.id}/$commit1/$commit2/$descKey/$commitForQueryParents"
                                    )
                                }

                            }

                        }
                    }
                }

                if(proFeatureEnabled(cherrypickTestPassed)) {
                    BottomSheetItem(sheetState, showBottomSheet, stringResource(R.string.cherrypick)) {
                        //弹窗，让选parent，默认选中第一个
                        if(curCommit.value.parentOidStrList.isEmpty()) {
                            Msg.requireShowLongDuration(appContext.getString(R.string.no_parent_for_find_changes_for_cherrypick))
                        }else {
                            //默认选中第一个parent
                            initCherrypickDialog(curCommit.value.oidStr, curCommit.value.parentOidStrList[0], curCommit.value.parentOidStrList)
                        }
                    }
                }

                if(proFeatureEnabled(createPatchTestPassed)) {
                    BottomSheetItem(sheetState, showBottomSheet, stringResource(R.string.create_patch)) {
                        //弹窗，让选parent，默认选中第一个
                        if(curCommit.value.parentOidStrList.isEmpty()) {
                            Msg.requireShowLongDuration(appContext.getString(R.string.no_parent_for_find_changes_for_create_patch))
                        }else {
                            //默认选中第一个parent
                            initCreatePatchDialog(curCommit.value.oidStr, curCommit.value.parentOidStrList[0], curCommit.value.parentOidStrList)
                        }
                    }
                }


                //如果是filter模式，显示show in list以在列表揭示filter条目以查看前后提交（或者说上下文）
                if(enableFilterState.value) {
                    BottomSheetItem(sheetState, showBottomSheet, stringResource(R.string.show_in_list)) {
                        filterModeOn.value = false
                        showBottomSheet.value = false

                        doJobThenOffLoading {
//                            delay(100)  // wait rendering, may unnecessary yet
                            val curItemIndex = curCommitIndex.intValue  // 被长按的条目在 filterlist中的索引
                            val idxList = filterIdxList.value  //取出存储filter索引和源列表索引的 index list，条目索引对应filter list条目索引，条目值对应的是源列表的真实索引

                            doActIfIndexGood(curItemIndex, idxList) {  // it为当前被长按的条目在源列表中的真实索引
                                UIHelper.scrollToItem(scope, listState, it)  //在源列表中定位条目
                                requireBlinkIdx.intValue = it  //设置条目闪烁以便用户发现
                            }
                        }
                    }

                }
//                BottomSheetItem(sheetState, showBottomSheet, stringResource(R.string.create_branch)){
//                // TODO (日后再考虑是否实现这个) (这个选项个特点是“仅创建分支，但不checkout”，感觉意义不是很大，而且可以通过“先detach head检出commit，在去分支页面创建分支并不勾选checkout”来曲线实现)弹出确认框，提示基于当前commit创建分支，并有一个checkout勾选框，如果确定，则创建分支（并checkout(如果勾选了的话)）
//                }
//                BottomSheetItem(sheetState, showBottomSheet, stringResource(R.string.view_hash)){
//                    // onClick()
//                    requireShowViewDialog(appContext.getString(R.string.view_hash), curCommit.value.oidStr)
//
//                }
//                BottomSheetItem(sheetState, showBottomSheet, stringResource(R.string.view_messages)){
//                    // 弹窗显示分支列表
//                    requireShowViewDialog(appContext.getString(R.string.view_messages), curCommit.value.msg)
//                }
//                BottomSheetItem(sheetState, showBottomSheet, stringResource(R.string.view_branches)){
//                    // 弹窗显示分支列表
//                    requireShowViewDialog(appContext.getString(R.string.view_branches), curCommit.value.branchShortNameList.toString())
//
//                }
//                BottomSheetItem(sheetState, showBottomSheet, stringResource(R.string.view_parents)){
//                    // 弹窗显示父提交长id列表
//                    requireShowViewDialog(appContext.getString(R.string.view_parents), curCommit.value.parentOidStrList.toString())
//
//                }

            }
        }

        //根据关键字过滤条目
        val k = filterKeyword.value.text.lowercase()  //关键字
        val enableFilter = filterModeOn.value && k.isNotEmpty()
        val list = if(enableFilter){
            filterIdxList.value.clear()

            list.value.filterIndexed {idx, it ->
                val found = it.oidStr.lowercase().contains(k)
                        || it.email.lowercase().contains(k)
                        || it.author.lowercase().contains(k)
                        || it.committerEmail.lowercase().contains(k)
                        || it.committerUsername.lowercase().contains(k)
                        || it.dateTime.lowercase().contains(k)
                        || it.branchShortNameList.toString().lowercase().contains(k)
                        || it.tagShortNameList.toString().lowercase().contains(k)
                        || it.parentOidStrList.toString().lowercase().contains(k)
                        || it.treeOidStr.lowercase().contains(k)
                        || it.msg.lowercase().contains(k)
                        || it.getOther().lowercase().contains(k)

                // for "show in list"
                if(found) {
                    filterIdxList.value.add(idx)
                }

                found
            }
        }else {
            list.value
        }

        val listState = if(enableFilter) filterListState else listState
//        if(enableFilter) {  //更新filter列表state
//            filterListState.value = listState
//        }

        //更新是否启用filter
        enableFilterState.value = enableFilter

        MyLazyColumn(
            contentPadding = contentPadding,
            list = list,
            listState = listState,
            requireForEachWithIndex = true,
            requirePaddingAtBottom = false,
            requireCustomBottom = true,
            customBottom = {
                LoadMore(
                    pageSize=pageSize,
                    rememberPageSize=rememberPageSize,
                    showSetPageSizeDialog=showSetPageSizeDialog,
                    pageSizeForDialog=pageSizeForDialog,
                    text = loadMoreText.value,
                    enableLoadMore = !loadMoreLoading.value && hasMore.value, enableAndShowLoadToEnd = !loadMoreLoading.value && hasMore.value,
                    loadToEndOnClick = {
                        val firstLoad = false
                        val forceReload = false
                        val loadToEnd = true
                        doLoadMore(
                            curRepo.value.fullSavePath,
                            nextCommitOid.value,
                            firstLoad,
                            forceReload,
                            loadToEnd
                        )
                    }
                ) {
                    val firstLoad = false
                    val forceReload = false
                    val loadToEnd = false
                    doLoadMore(
                        curRepo.value.fullSavePath,
                        nextCommitOid.value,
                        firstLoad,
                        forceReload,
                        loadToEnd
                    )

                }
            }
        ) { idx, it ->
            CommitItem(showBottomSheet, curCommit, curCommitIndex, idx, it, requireBlinkIdx) { thisObj ->
                val parents = thisObj.parentOidStrList
                if (parents.isEmpty()) {  // 如果没父提交，例如最初的提交就没父提交，提示没parent可比较
                    //TODO 改成没父提交时列出当前提交的所有文件
                    requireShowToast(appContext.getString(R.string.no_parent_for_compare))
                } else {  //有父提交，取出第一个父提交和当前提交进行比较
                    //当前比较的描述信息的key，用来在界面显示这是在比较啥，值例如“和父提交比较”或者“比较两个提交”之类的
                    val descKey =
                        Cache.setThenReturnKey(appContext.getString(R.string.compare_to_parent))
                    //这里需要传当前commit，然后cl页面会用当前commit查出当前commit的parents
                    val commit1 = parents[0]
                    val commit2 = thisObj.oidStr
                    val commitForQueryParents = commit2
                    // url 参数： 页面导航id/repoId/treeoid1/treeoid2/desckey
                    navController.navigate(
                        //注意是 parentTreeOid to thisObj.treeOid，也就是 旧提交to新提交，相当于 git diff abc...def，比较的是旧版到新版，新增或删除或修改了什么，反过来的话，新增删除之类的也就反了
                        "${Cons.nav_TreeToTreeChangeListScreen}/${curRepo.value.id}/$commit1/$commit2/$descKey/$commitForQueryParents"
                    )

                }
            }
            HorizontalDivider()
        }

        // filter mode 有可能查无条目，但是可继续加载更多，这时也应显示加载更多按钮
        if(filterModeOn.value && list.isEmpty()) {
            Column(
                modifier = Modifier
                    .padding(contentPadding)
                    .verticalScroll(rememberScrollState())
                ,
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Spacer(Modifier.height(50.dp))
                Text(stringResource(R.string.no_matched_item), fontWeight = FontWeight.Light)

                LoadMore(
                    modifier = Modifier.padding(top = 30.dp),
                    pageSize=pageSize,
                    rememberPageSize=rememberPageSize,
                    showSetPageSizeDialog=showSetPageSizeDialog,
                    pageSizeForDialog=pageSizeForDialog,
                    text = loadMoreText.value,
                    enableLoadMore = !loadMoreLoading.value && hasMore.value, enableAndShowLoadToEnd = !loadMoreLoading.value && hasMore.value,
                    loadToEndOnClick = {
                        val firstLoad = false
                        val forceReload = false
                        val loadToEnd = true
                        doLoadMore(
                            curRepo.value.fullSavePath,
                            nextCommitOid.value,
                            firstLoad,
                            forceReload,
                            loadToEnd
                        )
                    }
                ) {
                    val firstLoad = false
                    val forceReload = false
                    val loadToEnd = false
                    doLoadMore(
                        curRepo.value.fullSavePath,
                        nextCommitOid.value,
                        firstLoad,
                        forceReload,
                        loadToEnd
                    )

                }
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

        doJobThenOffLoading job@{
            //这里只用来获取是否需要forceReload的值，且这个值只需获取一次，所以getThenDel设置为true（其实多次获取也没事，只是会导致无意义查询）
            val (requestType, data) = getRequestDataByState<Any?>(
                needRefresh.value,
                getThenDel = true
            )

            //从db查数据
            val repoDb = AppModel.singleInstanceHolder.dbContainer.repoRepository
            val repoFromDb = repoDb.getById(repoId)
            if (repoFromDb == null) {
                MyLog.w(TAG, "#LaunchedEffect: query repo info from db error! repoId=$repoId}")
                return@job
            }
            curRepo.value = repoFromDb
            val repoFullPath = repoFromDb.fullSavePath
            val repoName = repoFromDb.repoName
//            val isDetached = dbIntToBool(repoFromDb.isDetached)
            var oid:Oid? = null

            Repository.open(repoFullPath).use { repo ->
                oid = if(!useFullOid) {  // resolve head
                    val head = Libgit2Helper.resolveHEAD(repo)
                    if (head == null) {
                        MyLog.w(TAG, "#LaunchedEffect: head is null! repoId=$repoId}")
                        return@job
                    }
                    val headOid = head.id()
                    if (headOid == null || headOid.isNullOrEmptyOrZero) {
                        MyLog.w(
                            TAG,
                            "#LaunchedEffect: head oid is null or invalid! repoId=$repoId}, headOid=${headOid.toString()}"
                        )
                        return@job
                    }

                    repoOnBranchOrDetachedHash.value = Libgit2Helper.getRepoOnBranchOrOnDetachedHash(repoFromDb)

                    headOid
                }else {  // resolve branch to commit
//                    val ref = Libgit2Helper.resolveRefByName(repo, fullOid.value, trueUseDwimFalseUseLookup = true)  // useDwim for get direct ref, which is point to a valid commit
                    val commit = Libgit2Helper.resolveCommitByHash(repo, fullOid.value)
                    val commitOid = commit?.id() ?: throw RuntimeException("resolve commit err!")
                    //注：虽然这个变量名是分支短名和短hash名blabala，但实际上，如果通过分支条目进入，只会有短分支名，不会有短提交号，短提交号是之前考虑欠佳即使分支条目点进来的提交历史也一checkout就刷新页面更新标题而残留下的东西
                    branchShortNameOrShortHashByFullOidForShowOnTitle.value = Libgit2Helper.getBranchNameOfRepoName(repoName, branchShortNameOrShortHashByFullOid.value)

                    commitOid
                }


                //第一次查询，指向headOid，NO！不要这么做，不然compose销毁又重建，恢复数据时，指向原本列表之后的commit就又重新指向head了，就乱了
                //不要在这给nexCommitOid和条目列表赋值！要在doLoadMore里给它们赋值！
//                nextCommitOid.value = headOid

            }


            // do first load
            val firstLoad = true
            val forceReload = (requestType == StateRequestType.forceReload)
            val loadToEnd = false
            //传repoFullPath是用来打开git仓库的
            doLoadMore(repoFullPath, oid!!, firstLoad, forceReload, loadToEnd)
        }
    }

    //compose被销毁时执行的副作用(SideEffect)
    DisposableEffect(Unit) {  // param changed or DisposableEffect destroying will run onDispose
        onDispose {
            doJobThenOffLoading {
                loadChannel.close()
            }
        }
    }

}

