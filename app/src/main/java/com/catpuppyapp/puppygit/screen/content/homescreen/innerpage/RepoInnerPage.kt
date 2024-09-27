package com.catpuppyapp.puppygit.screen.content.homescreen.innerpage

import android.annotation.SuppressLint
import android.content.Context
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Error
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableIntState
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.catpuppyapp.puppygit.compose.AskGitUsernameAndEmailDialog
import com.catpuppyapp.puppygit.compose.BottomSheet
import com.catpuppyapp.puppygit.compose.BottomSheetItem
import com.catpuppyapp.puppygit.compose.ConfirmDialog
import com.catpuppyapp.puppygit.compose.ErrRepoCard
import com.catpuppyapp.puppygit.compose.LoadingDialog
import com.catpuppyapp.puppygit.compose.MyCheckBox
import com.catpuppyapp.puppygit.compose.MyLazyColumn
import com.catpuppyapp.puppygit.compose.RepoCard
import com.catpuppyapp.puppygit.compose.SystemFolderChooser
import com.catpuppyapp.puppygit.constants.Cons
import com.catpuppyapp.puppygit.data.AppContainer
import com.catpuppyapp.puppygit.data.entity.RepoEntity
import com.catpuppyapp.puppygit.dev.dev_EnableUnTestedFeature
import com.catpuppyapp.puppygit.dev.proFeatureEnabled
import com.catpuppyapp.puppygit.dev.reflogTestPassed
import com.catpuppyapp.puppygit.dev.repoRenameTestPassed
import com.catpuppyapp.puppygit.dev.stashTestPassed
import com.catpuppyapp.puppygit.dev.tagsTestPassed
import com.catpuppyapp.puppygit.etc.Ret
import com.catpuppyapp.puppygit.git.Upstream
import com.catpuppyapp.puppygit.play.pro.R
import com.catpuppyapp.puppygit.style.MyStyleKt
import com.catpuppyapp.puppygit.ui.theme.Theme
import com.catpuppyapp.puppygit.user.UserUtil
import com.catpuppyapp.puppygit.utils.ActivityUtil
import com.catpuppyapp.puppygit.utils.AppModel
import com.catpuppyapp.puppygit.utils.ComposeHelper
import com.catpuppyapp.puppygit.utils.Libgit2Helper
import com.catpuppyapp.puppygit.utils.Msg
import com.catpuppyapp.puppygit.utils.MyLog
import com.catpuppyapp.puppygit.utils.RepoStatusUtil
import com.catpuppyapp.puppygit.utils.boolToDbInt
import com.catpuppyapp.puppygit.utils.changeStateTriggerRefreshPage
import com.catpuppyapp.puppygit.utils.createAndInsertError
import com.catpuppyapp.puppygit.utils.dbIntToBool
import com.catpuppyapp.puppygit.utils.deleteIfFileOrDirExist
import com.catpuppyapp.puppygit.utils.doActIfIndexGood
import com.catpuppyapp.puppygit.utils.doJobThenOffLoading
import com.catpuppyapp.puppygit.utils.getSecFromTime
import com.catpuppyapp.puppygit.utils.getStoragePermission
import com.catpuppyapp.puppygit.utils.replaceStringResList
import com.catpuppyapp.puppygit.utils.showErrAndSaveLog
import com.catpuppyapp.puppygit.utils.state.CustomStateListSaveable
import com.catpuppyapp.puppygit.utils.state.CustomStateSaveable
import com.catpuppyapp.puppygit.utils.state.StateUtil
import com.catpuppyapp.puppygit.utils.strHasIllegalChars
import com.github.git24j.core.Clone
import com.github.git24j.core.Remote
import com.github.git24j.core.Repository
import com.github.git24j.core.Submodule
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.io.File
import java.net.URI

private val TAG = "RepoInnerPage"
private val stateKeyTag = "RepoInnerPage"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RepoInnerPage(
    showBottomSheet: MutableState<Boolean>,
    sheetState: SheetState,
    curRepo: CustomStateSaveable<RepoEntity>,
    curRepoIndex: MutableIntState,
    contentPadding: PaddingValues,
    repoPageListState: LazyListState,
    showSetGlobalGitUsernameAndEmailDialog:MutableState<Boolean>,
    needRefreshRepoPage:MutableState<String>,
    changeListCurRepo:CustomStateSaveable<RepoEntity>,
    currentHomeScreen:MutableIntState,
    changeListNeedRefresh:MutableState<String>,
//    repoList:CustomStateSaveable<MutableList<RepoEntity>>
    repoList:CustomStateListSaveable<RepoEntity>,
    filesPageCurrentPath:MutableState<String>,
    filesPageNeedRefresh:MutableState<String>,
    goToFilesPage:(path:String) -> Unit,
    goToChangeListPage:(repoWillShowInChangeListPage: RepoEntity) -> Unit,
    repoPageScrollingDown:MutableState<Boolean>,
    repoPageFilterModeOn:MutableState<Boolean>,
    repoPageFilterKeyWord:CustomStateSaveable<TextFieldValue>,
    filterListState:CustomStateSaveable<LazyListState>,
    openDrawer:()->Unit,
    showImportRepoDialog:MutableState<Boolean>
) {
    val appContext = AppModel.singleInstanceHolder.appContext;
    val exitApp = AppModel.singleInstanceHolder.exitApp;
    val navController = AppModel.singleInstanceHolder.navController;

    val cloningText = stringResource(R.string.cloning)
    val unknownErrWhenCloning = stringResource(R.string.unknown_err_when_cloning)

    val dbContainer = AppModel.singleInstanceHolder.dbContainer;
//    val repoDtoList = remember { mutableStateListOf<RepoEntity>() }

    val activity = ActivityUtil.getCurrentActivity()

    //back handler block start
    val isBackHandlerEnable = StateUtil.getRememberSaveableState(initValue = true)
    val backHandlerOnBack = ComposeHelper.getDoubleClickBackHandler(appContext = appContext, openDrawer = openDrawer, exitApp = exitApp)
    //注册BackHandler，拦截返回键，实现双击返回和返回上级目录
    BackHandler(enabled = isBackHandlerEnable.value, onBack = {
        if(repoPageFilterModeOn.value) {
            repoPageFilterModeOn.value = false
        }else {
            backHandlerOnBack()
        }
    })
    //back handler block end

    val inDarkTheme = Theme.inDarkTheme

    val isLoading = StateUtil.getRememberSaveableState(initValue = true)
    val loadingText = StateUtil.getRememberSaveableState(initValue = appContext.getString(R.string.loading))
    val loadingOn = {text:String->
        loadingText.value = text
        isLoading.value=true
    }
    val loadingOff = {
        isLoading.value=false
        loadingText.value = ""
    }


//    val requireShowToast = { msg:String->
//        showToast.value = true;
//        toastMsg.value = msg
//    }
//    ShowToast(showToast, toastMsg)
    val requireShowToast:(String)->Unit = Msg.requireShowLongDuration

    val errWhenQuerySettingsFromDbStrRes = stringResource(R.string.err_when_querying_settings_from_db)
    val saved = stringResource(R.string.saved)

//    val showSetGlobalGitUsernameAndEmailDialog = rememberSaveable { mutableStateOf(false) }
    val setGlobalGitUsernameAndEmailStrRes = stringResource(R.string.set_global_username_and_email)
    val globalUsername = StateUtil.getRememberSaveableState(initValue = "")
    val globalEmail = StateUtil.getRememberSaveableState(initValue = "")

    // global username and email dialog
    if(showSetGlobalGitUsernameAndEmailDialog.value) {
        AskGitUsernameAndEmailDialog(
            title = stringResource(R.string.username_and_email),
            text=setGlobalGitUsernameAndEmailStrRes,
            username=globalUsername,
            email=globalEmail,
            isForGlobal=true,
            curRepo=curRepo,
            onOk={
                doJobThenOffLoading(
                    //loadingOn = loadingOn, loadingOff=loadingOff,
//                    loadingText=appContext.getString(R.string.saving)
                ){
                    //save email and username
                    Libgit2Helper.saveGitUsernameAndEmailForGlobal(
                        requireShowErr=requireShowToast,
                        errText=errWhenQuerySettingsFromDbStrRes,
                        errCode1="15569470",  // for noticed where caused error
                        errCode2="10405847",
                        username=globalUsername.value,
                        email=globalEmail.value
                    )
                    showSetGlobalGitUsernameAndEmailDialog.value=false
                    requireShowToast(saved)
                }
            },
            onCancel={
                showSetGlobalGitUsernameAndEmailDialog.value=false
                globalUsername.value=""
                globalEmail.value=""
            },

            //字段都可为空，所以确定键总是启用
            enableOk={true},
        )
    }


    val setCurRepoGitUsernameAndEmailStrRes = stringResource(R.string.set_username_and_email_for_repo)

    val showSetCurRepoGitUsernameAndEmailDialog = StateUtil.getRememberSaveableState(initValue = false)
    val curRepoUsername = StateUtil.getRememberSaveableState(initValue = "")
    val curRepoEmail = StateUtil.getRememberSaveableState(initValue = "")
    // repo username and email dialog
    if(showSetCurRepoGitUsernameAndEmailDialog.value) {
        AskGitUsernameAndEmailDialog(
            title=curRepo.value.repoName,
            text=setCurRepoGitUsernameAndEmailStrRes,
            username=curRepoUsername,
            email=curRepoEmail,
            isForGlobal=false,
            curRepo=curRepo,
            onOk={
                // save email and username
                doJobThenOffLoading(
                    //loadingOn = loadingOn, loadingOff=loadingOff,
//                    loadingText=appContext.getString(R.string.saving)
                ){
//                    MyLog.d(TAG, "curRepo.value.fullSavePath::"+curRepo.value.fullSavePath)
                    Repository.open(curRepo.value.fullSavePath).use { repo ->
                        //save email and username
                        Libgit2Helper.saveGitUsernameAndEmailForRepo(
                            repo = repo,
                            requireShowErr=requireShowToast,
                            username=curRepoUsername.value,
                            email=curRepoEmail.value
                        )
                    }
                    showSetCurRepoGitUsernameAndEmailDialog.value=false
                    requireShowToast(saved)
                }

            },
            onCancel={
                showSetCurRepoGitUsernameAndEmailDialog.value=false
                curRepoUsername.value=""
                curRepoEmail.value=""
            },

            //字段都可为空，所以确定键总是启用
            enableOk={true},
        )

    }

    val importRepoPath = StateUtil.getRememberSaveableState("")
    val isReposParentFolderForImport = StateUtil.getRememberSaveableState(false)

    if(showImportRepoDialog.value) {
        ConfirmDialog(
            title = stringResource(R.string.import_repo),
            requireShowTextCompose = true,
            textCompose = {
                Column(modifier = Modifier
                    .verticalScroll(StateUtil.getRememberScrollState())
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

                    SystemFolderChooser(path = importRepoPath)

                    Spacer(Modifier.height(15.dp))

                    MyCheckBox(text = stringResource(R.string.the_path_is_a_repos_parent_dir), value = isReposParentFolderForImport)

                    Spacer(Modifier.height(5.dp))

                    if(isReposParentFolderForImport.value) {
                        Text(stringResource(R.string.will_scan_repos_under_this_folder), fontWeight = FontWeight.Light)
                    }
                }
            },
            okBtnText = stringResource(R.string.ok),
            cancelBtnText = stringResource(R.string.cancel),
            okBtnEnabled = importRepoPath.value.isNotBlank(),
            onCancel = { showImportRepoDialog.value = false },
        ) {

            doJobThenOffLoading(loadingOn, loadingOff, appContext.getString(R.string.importing)) {
                try {
                    val newPath = importRepoPath.value

                    if(newPath.isNotBlank()) {
                        val f = File(newPath)

                        if(!f.canRead()) {
                            Msg.requireShowLongDuration(appContext.getString(R.string.cant_read_path))
                            return@doJobThenOffLoading
                        }

                        if(!f.isDirectory) {
                            Msg.requireShowLongDuration(appContext.getString(R.string.path_is_not_a_dir))
                            return@doJobThenOffLoading
                        }


                        showImportRepoDialog.value = false

                        val importRepoResult = AppModel.singleInstanceHolder.dbContainer.repoRepository.importRepos(dir=newPath, isReposParent=isReposParentFolderForImport.value)

                        // show a result dialog may better?

                        Msg.requireShowLongDuration(replaceStringResList(appContext.getString(R.string.n_imported), listOf(""+importRepoResult.success)))

                    }else {
                        Msg.requireShow(appContext.getString(R.string.invalid_path))
                    }
                }catch (e:Exception) {
                    MyLog.e(TAG, "import repo from ReposPage err: "+e.localizedMessage)
                    Msg.requireShowLongDuration("err:${e.localizedMessage}")
                }finally {
                    changeStateTriggerRefreshPage(needRefreshRepoPage)

                    //这个判断可能不准
//                    if(importRepoResult.success>0) {
//                        changeStateTriggerRefreshPage(needRefreshRepoPage)
//                    }
                }
            }

        }

    }

//    val needRefreshRepoPage = rememberSaveable { mutableStateOf(false) }
//    val needRefreshRepoPage = rememberSaveable { mutableStateOf("") }
    val initRepoPage = getInit(dbContainer, repoList, cloningText, unknownErrWhenCloning, loadingOn, loadingOff, appContext)

    //执行完doFetch/doMerge/doPush/doSync记得刷新页面，刷新页面不会改变列表滚动位置，所以放心刷，不用怕一刷新列表元素又滚动回第一个，让正在浏览仓库列表的用户困扰
    val doFetch:suspend (String?,RepoEntity)->Boolean = doFetch@{remoteNameParam:String?,curRepo:RepoEntity ->  //参数的remoteNameParam如果有效就用参数的，否则自己查当前head分支对应的remote
        //x 废弃，逻辑已经改了) 执行isReadyDoSync检查之前要先do fetch，想象一下，如果远程创建了一个分支，正好和本地的关联，但如果我没先fetch，那我检查时就get不到那个远程分支是否存在，然后就会先执行push，但可能远程仓库已经领先本地了，所以push也可能失败，但如果先fetch，就不会有这种问题了
        //fetch成功返回true，否则返回false
        var retVal = false
        try {
            Repository.open(curRepo.fullSavePath).use { repo ->
                var remoteName = remoteNameParam
                if(remoteName == null || remoteName.isBlank()) {
                    val shortBranchName = Libgit2Helper.getRepoCurBranchShortRefSpec(repo)
                    val upstream = Libgit2Helper.getUpstreamOfBranch(repo, shortBranchName)
                    remoteName = upstream.remote
                    if(remoteName == null || remoteName.isBlank()) {  //fetch不需合并，只需remote有效即可，所以只检查remote
                        throw RuntimeException(appContext.getString(R.string.err_upstream_invalid_plz_go_branches_page_set_it_then_try_again))
//                        return@doFetch false
                    }
                }

                //执行到这，upstream的remote有效，执行fetch
//            只fetch当前分支关联的remote即可，获取仓库当前remote和credential的关联，组合起来放到一个pair里，pair放到一个列表里，然后调用fetch
                val credential = Libgit2Helper.getRemoteCredential(
                    dbContainer.remoteRepository,
                    dbContainer.credentialRepository,
                    curRepo.id,
                    remoteName,
                    trueFetchFalsePush = true
                )

                //执行fetch
                Libgit2Helper.fetchRemoteForRepo(repo, remoteName, credential, curRepo)

            }

            // 更新修改workstatus的时间，只更新时间就行，状态会在查询repo时更新
            val repoDb = AppModel.singleInstanceHolder.dbContainer.repoRepository
            repoDb.updateLastUpdateTime(curRepo.id, getSecFromTime())

            retVal = true
        }catch (e:Exception) {
            //记录到日志
            //显示提示
            //保存数据库(给用户看的，消息尽量简单些)
            showErrAndSaveLog(TAG, "#doFetch() from Repo Page err:"+e.stackTraceToString(), "fetch err:"+e.localizedMessage, requireShowToast, curRepo.id)

            retVal = false
        }

        return@doFetch retVal
    }

    suspend fun doMerge(upstreamParam: Upstream?, curRepo:RepoEntity, trueMergeFalseRebase:Boolean=true):Boolean {
        try {
            //这的repo不能共享，不然一释放就要完蛋了，这repo不是rc是box单指针
            Repository.open(curRepo.fullSavePath).use { repo ->
                var upstream = upstreamParam
                if(Libgit2Helper.isUpstreamInvalid(upstream)) {  //如果调用者没传有效的upstream，查一下
                    val shortBranchName = Libgit2Helper.getRepoCurBranchShortRefSpec(repo)  //获取当前分支短名，例如 main
                    upstream = Libgit2Helper.getUpstreamOfBranch(repo, shortBranchName)  //获取当前分支的上游，例如 remote=origin 和 merge=refs/heads/main，参见配置文件 branch.yourbranchname.remote 和 .merge 字段
                    //如果查出的upstream还是无效，终止操作
                    if(Libgit2Helper.isUpstreamInvalid(upstream)) {
                        throw RuntimeException(appContext.getString(R.string.err_upstream_invalid_plz_go_branches_page_set_it_then_try_again))
//                        return@doMerge false
                    }
                }

                // doMerge
                val remoteRefSpec = Libgit2Helper.getUpstreamRemoteBranchShortNameByRemoteAndBranchRefsHeadsRefSpec(
                    upstream!!.remote,
                    upstream.branchRefsHeadsFullRefSpec
                )
                MyLog.d(TAG, "doMerge: remote="+upstream.remote+", branchFullRefSpec=" + upstream.branchRefsHeadsFullRefSpec +", trueMergeFalseRebase=$trueMergeFalseRebase")
                val (usernameFromConfig, emailFromConfig) = Libgit2Helper.getGitUsernameAndEmail(repo)

                //如果用户名或邮箱无效，无法创建commit，merge无法完成，所以，直接终止操作
                if(Libgit2Helper.isUsernameAndEmailInvalid(usernameFromConfig,emailFromConfig)) {
                    throw RuntimeException(appContext.getString(R.string.plz_set_username_and_email_first))
//                    return@doMerge false
                }

                val mergeResult = if(trueMergeFalseRebase) {
                    Libgit2Helper.mergeOneHead(
                        repo,
                        remoteRefSpec,
                        usernameFromConfig,
                        emailFromConfig
                    )
                }else {
                    Libgit2Helper.mergeOrRebase(
                        repo,
                        targetRefName = remoteRefSpec,
                        username = usernameFromConfig,
                        email = emailFromConfig,
                        requireMergeByRevspec = false,
                        revspec = "",
                        trueMergeFalseRebase = false
                    )
                }

                if (mergeResult.hasError()) {
                    //检查是否存在冲突条目
                    //如果调用者想自己判断是否有冲突，可传showMsgIfHasConflicts为false
                    if (mergeResult.code == Ret.ErrCode.mergeFailedByAfterMergeHasConfilts) {
                        throw RuntimeException(appContext.getString(R.string.has_conflicts))

//                        if(trueMergeFalseRebase) {
//                            throw RuntimeException(appContext.getString(R.string.merge_has_conflicts))
//                        }else {
//                            throw RuntimeException(appContext.getString(R.string.rebase_has_conflicts))
//                        }
                    }

                    //显示错误提示
                    throw RuntimeException(mergeResult.msg)

                    //记到数据库error日志
//                    createAndInsertError(curRepo.id, mergeResult.msg)

//                    return@doMerge false
                }

                //执行到这就合并成功了

                //清下仓库状态
                Libgit2Helper.cleanRepoState(repo)

                //更新db显示通知
                Libgit2Helper.updateDbAfterMergeSuccess(mergeResult, appContext, curRepo.id, {}, trueMergeFalseRebase)  //最后一个参数是合并成功或者不需要合并(uptodate)的信息提示函数，这个页面就不要在成功时提示了，合并完刷新下页面显示在仓库卡片上就行了

                return true
            }
        }catch (e:Exception) {
            //log
            showErrAndSaveLog(
                logTag = TAG,
                logMsg = "#doMerge(trueMergeFalseRebase=$trueMergeFalseRebase) from Repo Page err:"+e.stackTraceToString(),
                showMsg = e.localizedMessage ?:"err",
                showMsgMethod = requireShowToast,
                repoId = curRepo.id,
                errMsgForErrDb = "${if(trueMergeFalseRebase) "merge" else "rebase"} err: "+e.localizedMessage
            )

            return false
        }

    }

    val doPush:suspend (Upstream?,RepoEntity) -> Boolean  = doPush@{upstreamParam:Upstream?,curRepo:RepoEntity ->
        var retVal =false
        try {
//            MyLog.d(TAG, "#doPush: start")
            Repository.open(curRepo.fullSavePath).use { repo ->

                if(repo.headDetached()) {
                    throw RuntimeException(appContext.getString(R.string.push_failed_by_detached_head))
//                    return@doPush false
                }


                var upstream:Upstream? = upstreamParam
                if(Libgit2Helper.isUpstreamInvalid(upstream)) {  //如果调用者没传有效的upstream，查一下
                    val shortBranchName = Libgit2Helper.getRepoCurBranchShortRefSpec(repo)  //获取当前分支短名，例如 main
                    upstream = Libgit2Helper.getUpstreamOfBranch(repo, shortBranchName)  //获取当前分支的上游，例如 remote=origin 和 merge=refs/heads/main，参见配置文件 branch.yourbranchname.remote 和 .merge 字段
                    //如果查出的upstream还是无效，终止操作
                    if(Libgit2Helper.isUpstreamInvalid(upstream)) {
                        throw RuntimeException(appContext.getString(R.string.err_upstream_invalid_plz_go_branches_page_set_it_then_try_again))
//                        return@doPush false
                    }
                }
                MyLog.d(TAG, "#doPush: upstream.remote="+upstream!!.remote+", upstream.branchFullRefSpec="+upstream!!.branchRefsHeadsFullRefSpec)

                //执行到这里，必定有上游，push
                val credential = Libgit2Helper.getRemoteCredential(
                    dbContainer.remoteRepository,
                    dbContainer.credentialRepository,
                    curRepo.id,
                    upstream!!.remote,
                    trueFetchFalsePush = false
                )

                val ret = Libgit2Helper.push(repo, upstream!!.remote, upstream!!.pushRefSpec, credential)
                if(ret.hasError()) {
                    throw RuntimeException(ret.msg)
                }

                // 更新修改workstatus的时间，只更新时间就行，状态会在查询repo时更新
                val repoDb = AppModel.singleInstanceHolder.dbContainer.repoRepository
                repoDb.updateLastUpdateTime(curRepo.id, getSecFromTime())

                retVal =  true
            }
        }catch (e:Exception) {
            //log
            showErrAndSaveLog(TAG, "#doPush() err:"+e.stackTraceToString(), "push error:"+e.localizedMessage, requireShowToast, curRepo.id)

            retVal =  false
        }


//        如果push失败，有必要更新这个时间吗？没，所以我后来把更新时间放到成功代码块里了


        return@doPush retVal

    }

    //sync之前，先执行stage，然后执行提交，如果成功，执行fetch/merge/push (= pull/push = sync)
    val doSync:suspend (RepoEntity)->Unit = doSync@{curRepo:RepoEntity ->
        Repository.open(curRepo.fullSavePath).use { repo ->
            if(repo.headDetached()) {
                throw RuntimeException(appContext.getString(R.string.sync_failed_by_detached_head))
//                return@doSync
            }


            //检查是否有upstream，如果有，do fetch do merge，然后do push,如果没有，请求设置upstream，然后do push
            val hasUpstream = Libgit2Helper.isBranchHasUpstream(repo)
            val shortBranchName = Libgit2Helper.getRepoCurBranchShortRefSpec(repo)
            if (!hasUpstream) {  //不存在上游，提示先去设置
                throw RuntimeException(appContext.getString(R.string.err_upstream_invalid_plz_go_branches_page_set_it_then_try_again))
//                return@doSync

            }

            //存在上游
            try {
                //取出上游
                val upstream = Libgit2Helper.getUpstreamOfBranch(repo, shortBranchName)
                val fetchSuccess = doFetch(upstream.remote, curRepo)
                if(!fetchSuccess) {
                    throw RuntimeException(appContext.getString(R.string.fetch_failed))
//                    return@doSync
                }

                //检查配置文件设置的remote和branch是否实际存在，
                val isUpstreamExistOnLocal = Libgit2Helper.isUpstreamActuallyExistOnLocal(
                    repo,
                    upstream.remote,
                    upstream.branchRefsHeadsFullRefSpec
                )

                //如果存在上游，执行merge 若没冲突则push，否则终止操作直接return
                //如果不存在上游，只需执行push，相当于pc的 git push with -u (--set-upstream)，但上面已经把remote和branch设置到gitconfig里了(执行到这里都能取出来upstream所以肯定设置过了，在 对话框或分支管理页面设置的)，所以这里正常推送即可，不用再设置什么
                MyLog.d(TAG, "@doSync: isUpstreamExistOnLocal="+isUpstreamExistOnLocal)
                if(isUpstreamExistOnLocal) {  //上游分支在本地存在
                    // doMerge
                    val mergeSuccess = doMerge(upstream, curRepo)
                    if(!mergeSuccess) {  //merge 失败，终止操作
                        //如果merge完存在冲突条目，就不要执行push了
                        if(Libgit2Helper.hasConflictItemInRepo(repo)) {  //检查失败原因是否是存在冲突，若是则显示提示
                            throw RuntimeException(appContext.getString(R.string.has_conflicts_abort_sync))
                        }

                        throw RuntimeException(appContext.getString(R.string.merge_failed))
//                        return@doSync
                    }
                }

                //如果执行到这，要么不存在上游，直接push(新建远程分支)；要么存在上游，但fetch/merge成功完成，需要push，所以，执行push
                //doPush
                val pushSuccess = doPush(upstream, curRepo)
                if(!pushSuccess) {
                    throw RuntimeException(appContext.getString(R.string.push_failed))
                }
//                    requireShowToast(appContext.getString(R.string.sync_success))  //这个页面如果成功就不要提示了
            }catch (e:Exception) {
                //log
                showErrAndSaveLog(TAG, "#doSync() err:"+e.stackTraceToString(), "sync err:"+e.localizedMessage, requireShowToast, curRepo.id)

            }


        }

    }

    val doPull:suspend ()->Unit = {
        try {
            val thisRepo = curRepo.value
            val fetchSuccess = doFetch(null,thisRepo)
            if(!fetchSuccess) {

                throw RuntimeException(appContext.getString(R.string.fetch_failed))
            }else {
                val mergeSuccess = doMerge(null,thisRepo)
                if(!mergeSuccess){
                    throw RuntimeException(appContext.getString(R.string.merge_failed))
                }
            }
        }catch (e:Exception){
            showErrAndSaveLog(TAG,"require pull error:"+e.stackTraceToString(), appContext.getString(R.string.pull_err)+":"+e.localizedMessage, requireShowToast,curRepo.value.id)
        }

    }

    val doActAndSetRepoStatus:suspend (Int, String, String, suspend ()->Unit) -> Unit = {idx:Int, repoId:String, status:String, act: suspend ()->Unit ->
        //设置数组中元素的临时状态(重新查列表后消失，但因为上面设置状态到缓存了，所以重查时可通过缓存恢复)，但无需重新加载页面，轻量级操作，就能让用户看到临时状态
        doActIfIndexGood(idx,repoList.value) {
//            it.tmpStatus = status
            //必须copy一下，要不然还得刷新页面才能显示状态（ps：刷新页面显示状态是通过map存临时状态实现的，比这个操作重量级，应能避免则避免）
            repoList.value[idx]=it.copy(tmpStatus = status)
//            repoList.requireRefreshView()
        }
        //设置仓库临时状态(把临时状态设置到缓存里，不退出app都有效，目的是为了使重新查列表后临时状态亦可见)，这样重新加载页面时依然能看到临时状态
        RepoStatusUtil.setRepoStatus(repoId, status)
        //刷新以显示刚设置的状态
//        changeStateTriggerRefreshPage(needRefreshRepoPage)
        //执行操作
        act()

        //test 有时候操作执行太快，但我需要执行慢点来测试某些东西，所以加个delay方便测试
        //delay(3000)
        //test

        //清除缓存中的仓库状态
        RepoStatusUtil.clearRepoStatus(repoId)
        //重查下repo数据
        doActIfIndexGood(idx,repoList.value) {
            //无法确定执行什么操作，也无法确定会影响到什么，所以无法精准更新某字段，干脆在操作成功时，重查下数据，拿到最新状态就行了
            doJobThenOffLoading {
                //重查数据
                val repoDb = dbContainer.repoRepository
                val reQueriedRepoInfo = repoDb.getById(it.id)?:return@doJobThenOffLoading

                //更新卡片条目
                repoList.value[idx] = reQueriedRepoInfo

                //检查下如果当前长按菜单显示的是当前仓库，更新下和菜单项相关的字段。（这里不要赋值curRepo.value，以免并发冲突覆盖用户长按的仓库）
                val curRepoInMenu = curRepo.value  //这样修改的话，即使在下面赋值时用户长按了其他仓库也能正常工作，只是下面的赋值操作失去意义而已，但不会并发冲突，也不会显示或执行错误，但如果直接给curRepo.value赋值，则就有可能出错了，比如可能覆盖用户长按的仓库，发生用户长按了仓库a，但显示的却是仓库b的状态的情况
                if(curRepoInMenu.id == reQueriedRepoInfo.id) {  //如果，当前长按菜单显示的是当前修改的仓库，则更新下其tmpStatus以更新其菜单项的启用/禁用状态，例如执行fetch时，pull/push/fetch都禁用，但操作执行完后应重新启用，若不做这个检测，则只有重新长按菜单才会更新启用/禁用状态，有点不方便
                    //注意这里更新的只有和长按菜单相关的字段而已，其他字段无需更新
                    curRepoInMenu.tmpStatus = reQueriedRepoInfo.tmpStatus  //不出意外的话，这的tmpStatus应为空字符串，但保险起见，还是不要直接用空字符串赋值比较好
                    curRepoInMenu.isDetached = reQueriedRepoInfo.isDetached
                    curRepoInMenu.isShallow = reQueriedRepoInfo.isShallow
                }
//                repoList.requireRefreshView()
            }
        }

    }

    val showDelRepoDialog = StateUtil.getRememberSaveableState(initValue = false)
    val willDeleteRepo = StateUtil.getCustomSaveableState(keyTag = stateKeyTag, keyName = "willDeleteRepo", initValue = RepoEntity(id=""))
    val requireDelFilesOnDisk = StateUtil.getRememberSaveableState(initValue = false)
    val requireDelRepo = {expectDelRepo:RepoEntity ->
        willDeleteRepo.value = expectDelRepo
        requireDelFilesOnDisk.value = false
        showDelRepoDialog.value = true
    }
    if(showDelRepoDialog.value) {
        ConfirmDialog(
            title = stringResource(id = R.string.del_repo),
//            text = stringResource(id = R.string.are_you_sure_to_delete)+": '"+willDeleteRepo.value.repoName+"' ?"+"\n"+ stringResource(R.string.will_delete_repo_and_all_its_files_on_disk),
            requireShowTextCompose = true,
            textCompose = {
                Column {
                    Row {
                        Text(text = stringResource(id = R.string.delete_repo)+":")
                    }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 10.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = willDeleteRepo.value.repoName,
                            fontWeight = FontWeight.ExtraBold,
                            modifier = Modifier.padding(start = 16.dp),
                            //                                color = Color.Unspecified
                        )

                    }

                    Column {
                        Text(text = stringResource(R.string.are_you_sure))
                        Row(
                            Modifier
                                .fillMaxWidth()
                                .height(MyStyleKt.CheckoutBox.height)
                                .toggleable(
                                    enabled = true,
                                    value = requireDelFilesOnDisk.value,
                                    onValueChange = {
                                        requireDelFilesOnDisk.value = !requireDelFilesOnDisk.value
                                    },
                                    role = Role.Checkbox
                                )
                                .padding(horizontal = 16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                enabled = true,
                                checked = requireDelFilesOnDisk.value,
                                onCheckedChange = null // null recommended for accessibility with screenreaders
                            )
                            Text(
                                text = stringResource(R.string.del_files_on_disk),
                                style = MaterialTheme.typography.bodyLarge,
                                //如果根据某些条件禁用这个勾选框，则用这行，把条件替换到enable里
//                                color = if(enable) MyStyleKt.TextColor.enable else if(inDarkTheme) MyStyleKt.TextColor.disable_DarkTheme else MyStyleKt.TextColor.disable,
                                //如果不需要禁用勾选框，则用这行，保持启用的颜色即可
                                color = MyStyleKt.TextColor.enable,

                                modifier = Modifier.padding(start = 16.dp),
                            )
                        }
                        if(requireDelFilesOnDisk.value) {
                            Text(text = "("+stringResource(R.string.will_delete_repo_and_all_its_files_on_disk)+")",
                                color = MyStyleKt.TextColor.danger
                            )
                        }
                    }
                }

            },
            okTextColor = MyStyleKt.TextColor.danger,
            onCancel = { showDelRepoDialog.value=false }
        ) {
            //关闭弹窗
            showDelRepoDialog.value=false

            val willDeleteRepo = willDeleteRepo.value
            val requireDelFilesOnDisk = requireDelFilesOnDisk.value
            val requireTransaction = true

            //执行删除
            doJobThenOffLoading {
                try {
                    val repoDb = AppModel.singleInstanceHolder.dbContainer.repoRepository
                    //删除仓库
                    repoDb.delete(
                        item = willDeleteRepo,
                        requireDelFilesOnDisk = requireDelFilesOnDisk,
                        requireTransaction = requireTransaction
                    )
                }finally {
                    //请求刷新列表
                    changeStateTriggerRefreshPage(needRefreshRepoPage)
                }
            }
        }

    }

    val showRenameDialog = StateUtil.getRememberSaveableState(initValue = false)
    val repoNameForRenameDialog = StateUtil.getRememberSaveableState(initValue = "")
    val errMsgForRenameDialog = StateUtil.getRememberSaveableState(initValue = "")
    if(showRenameDialog.value) {
        val curRepo = curRepo.value

        ConfirmDialog(
            title = stringResource(R.string.rename_repo),
            requireShowTextCompose = true,
            textCompose = {
                Column(modifier = Modifier.verticalScroll(StateUtil.getRememberScrollState())) {
                    TextField(
                        modifier = Modifier
                            .fillMaxWidth()
                        ,
                        value = repoNameForRenameDialog.value,
                        singleLine = true,
                        isError = errMsgForRenameDialog.value.isNotBlank(),
                        supportingText = {
                            if (errMsgForRenameDialog.value.isNotBlank()) {
                                Text(
                                    modifier = Modifier.fillMaxWidth(),
                                    text = errMsgForRenameDialog.value,
                                    color = MaterialTheme.colorScheme.error
                                )
                            }
                        },
                        trailingIcon = {
                            if (errMsgForRenameDialog.value.isNotBlank()) {
                                Icon(imageVector=Icons.Filled.Error,
                                    contentDescription=null,
                                    tint = MaterialTheme.colorScheme.error)
                            }
                        },
                        onValueChange = {
                            repoNameForRenameDialog.value = it

                            // clear err msg
                            errMsgForRenameDialog.value = ""
                        },
                        label = {
                            Text(stringResource(R.string.new_name))
                        }
                    )
                }
            },
            okBtnText = stringResource(R.string.ok),
            cancelBtnText = stringResource(R.string.cancel),
            okBtnEnabled = repoNameForRenameDialog.value != curRepo.repoName,
            onCancel = {showRenameDialog.value = false}
        ) {
            val newName = repoNameForRenameDialog.value
            val repoId = curRepo.id

            doJobThenOffLoading(loadingOn, loadingOff, appContext.getString(R.string.renaming)) {
                try {
                    val repoDb = AppModel.singleInstanceHolder.dbContainer.repoRepository
                    if(strHasIllegalChars(newName)) {
                        errMsgForRenameDialog.value = appContext.getString(R.string.name_has_illegal_chars)
                        return@doJobThenOffLoading
                    }

                    if(repoDb.isRepoNameExist(newName)) {
                        errMsgForRenameDialog.value = appContext.getString(R.string.name_already_exists)
                        return@doJobThenOffLoading
                    }

                    showRenameDialog.value = false

                    repoDb.updateRepoName(repoId, newName)

                    Msg.requireShow(appContext.getString(R.string.success))
                }catch (e:Exception) {
                    val errmsg = e.localizedMessage ?: "rename repo err"
                    Msg.requireShowLongDuration(errmsg)
                    createAndInsertError(curRepo.id, "err: rename repo '${curRepo.repoName}' to ${repoNameForRenameDialog.value} failed, err is $errmsg")
                }finally {
                    changeStateTriggerRefreshPage(needRefreshRepoPage)
                }
            }
        }
    }


    val showUnshallowDialog = StateUtil.getRememberSaveableState(initValue = false)
    if(showUnshallowDialog.value) {
        ConfirmDialog(
            title = stringResource(id = R.string.unshallow),
            requireShowTextCompose = true,
            textCompose = {
                          Column {
                              Row {
                                  Text(text = stringResource(R.string.will_do_unshallow_for_repo)+":")
                              }
                              Row(
                                  modifier = Modifier
                                      .fillMaxWidth()
                                      .padding(vertical = 10.dp),
                                  horizontalArrangement = Arrangement.Center,
                                  verticalAlignment = Alignment.CenterVertically
                              ) {
                                  Text(
                                      text = curRepo.value.repoName,
                                      fontWeight = FontWeight.ExtraBold,
                                      modifier = Modifier.padding(start = 16.dp),
                                      //                                color = Color.Unspecified
                                  )

                              }

                              Column {
                                  Text(text = stringResource(R.string.are_you_sure),
                                  )
                                  Text(text = "("+stringResource(R.string.unshallow_success_cant_back)+")",
                                      color = Color.Red
                                  )
                              }
                          }
            },
            onCancel = { showUnshallowDialog.value=false}) {
            showUnshallowDialog.value=false
            doJobThenOffLoading {
                val curRepoId = curRepo.value.id
                val curRepoIdx = curRepoIndex.intValue
                val curRepoFullPath = curRepo.value.fullSavePath
                val curRepoVal =  curRepo.value
                doActAndSetRepoStatus(curRepoIdx, curRepoId, appContext.getString(R.string.Unshallowing)) {
                    Repository.open(curRepoFullPath).use { repo->
                        val ret = Libgit2Helper.unshallowRepo(repo, curRepoVal,
                            AppModel.singleInstanceHolder.dbContainer.repoRepository,
                            AppModel.singleInstanceHolder.dbContainer.remoteRepository,
                            AppModel.singleInstanceHolder.dbContainer.credentialRepository
                        )
                        if(ret.hasError()) {
                            Msg.requireShow(ret.msg)
                        }

                    }
                }
            }

        }
    }

    //点击某个仓库卡片上的status文案，把仓库存上，方便弹窗执行后续操作
    val statusClickedRepo = StateUtil.getCustomSaveableState(keyTag = stateKeyTag, keyName = "statusClickedRepo") { RepoEntity(id="") }

    val showRequireActionsDialog = StateUtil.getRememberSaveableState(initValue = false)
    if(showRequireActionsDialog.value) {
        val targetRepo = statusClickedRepo.value

        if(targetRepo.id.isBlank()) {
            Msg.requireShow(stringResource(R.string.repo_id_invalid))
        }else {
            ConfirmDialog(title = stringResource(R.string.require_actions),
                text = stringResource(R.string.will_go_to_changelist_then_you_can_continue_or_abort_your_merge_rebase_cherrpick),
                okBtnText = stringResource(id = R.string.ok),
                cancelBtnText = stringResource(id = R.string.no),
                onCancel = { showRequireActionsDialog.value = false }
            ) {
                showRequireActionsDialog.value = false

                //跳转到ChangeList页面
                goToChangeListPage(targetRepo)
            }
        }
    }

    if(showBottomSheet.value) {
        val repoDto = curRepo.value
        val repoStatusGood = repoDto.gitRepoState!=null && !Libgit2Helper.isRepoStatusNotReadyOrErr(repoDto)

        val isDetached = dbIntToBool(curRepo.value.isDetached)
        val hasTmpStatus = curRepo.value.tmpStatus.isNotBlank()  //如果有设临时状态，说明在执行某个操作，比如正在fetching，所以这时应该不允许再执行fetch或pull之类的操作，我做了处理，即使用户去cl页面执行，也无法绕过此限制
        val actionEnabled = !isDetached && !hasTmpStatus
        BottomSheet(showBottomSheet, sheetState, curRepo.value.repoName) {
            if(repoStatusGood) {
                BottomSheetItem(sheetState, showBottomSheet, stringResource(R.string.fetch), textDesc = stringResource(R.string.check_update), enabled = actionEnabled) {
                    //fetch 当前仓库上游的remote
                    doJobThenOffLoading {
                        doActAndSetRepoStatus(curRepoIndex.intValue, curRepo.value.id, appContext.getString(R.string.fetching)) {
                            doFetch(null, curRepo.value)
                        }
                    }
                }
                BottomSheetItem(sheetState, showBottomSheet, stringResource(R.string.pull), enabled = actionEnabled) {
                    doJobThenOffLoading {
                        doActAndSetRepoStatus(curRepoIndex.intValue, curRepo.value.id, appContext.getString(R.string.pulling)) {
                            doPull()
                        }
                    }
                }
                BottomSheetItem(sheetState, showBottomSheet, stringResource(R.string.push), enabled = actionEnabled) {
                    doJobThenOffLoading {
                        doActAndSetRepoStatus(curRepoIndex.intValue, curRepo.value.id, appContext.getString(R.string.pushing)) {
                            doPush(null, curRepo.value)
                        }
                    }
                }
    //            BottomSheetItem(sheetState, showBottomSheet, stringResource(R.string.sync), enabled = actionEnabled) {
    //                doJobThenOffLoading {
    //                    try {
    //                        doSync(curRepo.value)
    //
    //                    }finally {
    //                        changeStateTriggerRefreshPage(needRefreshRepoPage)
    //                    }
    //                }
    //            }
                BottomSheetItem(sheetState, showBottomSheet, stringResource(R.string.remotes)) {
                    //管理remote，右上角有个fetch all可fetch所有remote
                    navController.navigate(Cons.nav_RemoteListScreen+"/"+curRepo.value.id)
                }

                if(dev_EnableUnTestedFeature || tagsTestPassed) {
                    val isPro = UserUtil.isPro()
                    val text = if(isPro) stringResource(R.string.tags) else stringResource(R.string.tags_pro)

                    //非pro用户能看到这个选项但不能用
                    BottomSheetItem(sheetState, showBottomSheet, text, enabled = isPro) {
                        //跳转到tags页面
                        navController.navigate(Cons.nav_TagListScreen + "/" + curRepo.value.id)
                    }

                }

    //            BottomSheetItem(sheetState, showBottomSheet, stringResource(R.string.reflog)) {
    //             //日后实现
    //            }
    //            BottomSheetItem(sheetState, showBottomSheet, stringResource(R.string.tags)) {
    //              日后实现
    //            }
    //            BottomSheetItem(sheetState, showBottomSheet, stringResource(R.string.settings)) {
    //              日后实现
    //            }
                BottomSheetItem(sheetState, showBottomSheet, stringResource(R.string.username_and_email)) {
                    showSetCurRepoGitUsernameAndEmailDialog.value = true
                }
                //对shallow(克隆时设置了depth)的仓库提供一个unshallow选项
                if(dbIntToBool(curRepo.value.isShallow)) {
                    BottomSheetItem(sheetState, showBottomSheet, stringResource(R.string.unshallow),
                        textDesc = stringResource(R.string.cancel_clone_depth),
                        enabled = !hasTmpStatus  //unshallow是针对仓库的行为，会对仓库所有remote执行unshallow fetch，而不管仓库是否detached HEAD，由于仓库remotes总是可用，所以，这里不用判断是否detached
                    ) {
                        showUnshallowDialog.value = true
                    }
                }
                BottomSheetItem(sheetState, showBottomSheet, stringResource(R.string.explorer_files)) {
                    showBottomSheet.value=false  //不知道为什么，常规的关闭菜单不太好使，一跳转页面就废了，所以手动隐藏下菜单
                    goToFilesPage(curRepo.value.fullSavePath)
                }

                //go to changelist，避免侧栏切换到changelist时刚好某个仓库加载很慢导致无法切换其他仓库
                BottomSheetItem(sheetState, showBottomSheet, stringResource(R.string.changelist)) {
                    showBottomSheet.value=false  //不知道为什么，常规的关闭菜单不太好使，一跳转页面就废了，所以手动隐藏下菜单
                    goToChangeListPage(curRepo.value)
                }

                //非pro这两个选项直接不可见，弄成能看不能用有点麻烦，直接非pro隐藏算了
                if(UserUtil.isPro()) {
                    if(dev_EnableUnTestedFeature || stashTestPassed) {
                        BottomSheetItem(sheetState, showBottomSheet, stringResource(R.string.stash)) {
                            showBottomSheet.value=false
                            navController.navigate(Cons.nav_StashListScreen+"/"+curRepo.value.id)
                        }
                    }

                    if(dev_EnableUnTestedFeature || reflogTestPassed){
                        BottomSheetItem(sheetState, showBottomSheet, stringResource(R.string.reflog)) {
                            showBottomSheet.value=false  //不知道为什么，常规的关闭菜单不太好使，一跳转页面就废了，所以手动隐藏下菜单
                            navController.navigate(Cons.nav_ReflogListScreen+"/"+curRepo.value.id)
                        }
                    }
                }
            }

            if(proFeatureEnabled(repoRenameTestPassed)) {
                BottomSheetItem(sheetState, showBottomSheet, stringResource(R.string.rename)) {
                    repoNameForRenameDialog.value = curRepo.value.repoName
                    showRenameDialog.value = true
                }
            }
            BottomSheetItem(sheetState, showBottomSheet, stringResource(R.string.delete), textColor = MyStyleKt.TextColor.danger) {
                requireDelRepo(curRepo.value)
            }
        }
    }

    if(isLoading.value) {

//        Column(
//            modifier = Modifier
//                .fillMaxSize()
//                .padding(contentPadding)
//                .verticalScroll(StateUtil.getRememberScrollState())
//            ,
//            verticalArrangement = Arrangement.Center,
//            horizontalAlignment = Alignment.CenterHorizontally,
//
//        ) {
//            Text(text = loadingText.value)
//        }

        LoadingDialog(loadingText.value)

    }

    if (!isLoading.value && repoList.value.isEmpty()) {  //无仓库，显示添加按钮
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(contentPadding)
                .verticalScroll(StateUtil.getRememberScrollState())

                ,
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,

        ) {
            //interactionSource和indication的作用是隐藏按下时的背景半透明那个按压效果，很难看，所以隐藏
            Column(modifier = Modifier.clickable(interactionSource = remember { MutableInteractionSource() }, indication = null) {
                navController.navigate(Cons.nav_CloneScreen+"/null")  //不传repoId，就是null，等于新建模式
            },
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
            ){
                Row{
                    Icon(modifier = Modifier.size(50.dp),
                        imageVector = Icons.Filled.Add,
                        contentDescription = stringResource(R.string.add),
                        tint = MyStyleKt.IconColor.normal
                    )
                }
                Row {
                    Text(text = stringResource(id = R.string.add_a_repo),
                        style = MyStyleKt.ClickableText.style,
                        color = MyStyleKt.ClickableText.color,
                        fontSize = MyStyleKt.TextSize.default
                    )
                }
            }

        }
    }

    // 向下滚动监听，开始
    val enableFilterState = StateUtil.getRememberSaveableState(initValue = false)
//    val firstVisible = remember { derivedStateOf { if(enableFilterState.value) filterListState.value.firstVisibleItemIndex else repoPageListState.firstVisibleItemIndex } }
//    ScrollListener(
//        nowAt = firstVisible.value,
//        onScrollUp = {repoPageScrollingDown.value = false}
//    ) { // onScrollDown
//        repoPageScrollingDown.value = true
//    }
    @SuppressLint("UnrememberedMutableState")
    val lastAt = mutableIntStateOf(0)
    repoPageScrollingDown.value = remember {
        derivedStateOf {
            val nowAt = if(enableFilterState.value) {
                filterListState.value.firstVisibleItemIndex
            } else {
                repoPageListState.firstVisibleItemIndex
            }
            val scrolldown = nowAt > lastAt.intValue
            lastAt.intValue = nowAt
            scrolldown
        }
    }.value
    // 向下滚动监听，结束


    if(!isLoading.value && repoList.value.isNotEmpty()) {  //有仓库

        //根据关键字过滤条目
        val k = repoPageFilterKeyWord.value.text.lowercase()  //关键字
        val enableFilter = repoPageFilterModeOn.value && k.isNotEmpty()
        val filteredList = if(enableFilter){
            repoList.value.filter {
                it.repoName.lowercase().contains(k)
                        || it.branch.lowercase().contains(k)
                        || it.cloneUrl.lowercase().contains(k)
                        || it.lastCommitHash.lowercase().contains(k)
                        || it.latestUncheckedErrMsg.lowercase().contains(k)
                        || it.pullRemoteName.lowercase().contains(k)
                        || it.pushRemoteName.lowercase().contains(k)
                        || it.pullRemoteUrl.lowercase().contains(k)
                        || it.pushRemoteUrl.lowercase().contains(k)
                        || it.tmpStatus.lowercase().contains(k)
                        || it.upstreamBranch.lowercase().contains(k)
                        || it.createErrMsg.lowercase().contains(k)
            }
        }else {
            repoList.value
        }

        val listState = if(enableFilter) StateUtil.getRememberLazyListState() else repoPageListState
        if(enableFilter) {  //更新filter列表state
            filterListState.value = listState
        }
        //更新是否启用filter
        enableFilterState.value = enableFilter

        MyLazyColumn(
            contentPadding = contentPadding,
            list = filteredList,
            listState = listState,
            requireForEachWithIndex = true,
            requirePaddingAtBottom = true
        ) {idx, element ->
            //状态小于errValStart意味着一切正常；状态大于等于errValStart，意味着出错，禁用长按功能，直接把可以执行的操作例如删除仓库和编辑仓库之类的显示在卡片上，方便用户处置出错的仓库
            // 如果有必要细分状态，可以改成这样: if(it.workStatus==cloningStatus) go cloningCard, else if other status, go other card, else go normal RepoCard
            if (Libgit2Helper.isRepoStatusNoErr(element)) {
                //未出错的仓库
                RepoCard(
                    showBottomSheet,
                    curRepo,
                    curRepoIndex,
                    repoDto = element,
                    repoDtoIndex = idx,
                    goToFilesPage = goToFilesPage,
                ) workStatusOnclick@{ clickedRepo, status ->  //这个是点击status的callback，这个status其实可以不传，因为这里的lambda能捕获到数组的元素，就是当前仓库

                    //把点击状态的仓库存下来
                    statusClickedRepo.value = clickedRepo  //其实这个clickedRepo直接用这里element替代也可，但用回调里参数感觉更合理

                    //目前status就三种状态：up-to-date/has conflicts/need sync，第1种不用处理
                    if (status == Cons.dbRepoWorkStatusHasConflicts) {
                        //导航到changelist并定位到当前仓库
                        goToChangeListPage(clickedRepo)
                    }else if(status == Cons.dbRepoWorkStatusMerging || status==Cons.dbRepoWorkStatusRebasing || status==Cons.dbRepoWorkStatusCherrypicking){ //merge/rebase/cherrypick弹窗提示需要continue或abort
                        showRequireActionsDialog.value = true
                    } else if (status == Cons.dbRepoWorkStatusNeedSync) {
                        // do sync
                        doJobThenOffLoading {
                            doActAndSetRepoStatus(idx, clickedRepo.id, appContext.getString(R.string.syncing)) {
                                doSync(clickedRepo)
                            }
                        }
                    }
                }
            } else {
                //show Clone error repo card，显示克隆错误，有重试和编辑按钮，编辑可重新进入克隆页面编辑当前仓库的信息，然后重新克隆
                ErrRepoCard(
//                        showBottomSheet = showBottomSheet,
//                        curRepo = curRepo,
                    repoDto = element,
                    repoDtoList = repoList.value,
                    idx = idx,
                    needRefreshList = needRefreshRepoPage,
                    requireDelRepo = requireDelRepo
                )
                //                            if(it.workStatus == Cons.dbRepoWorkStatusCloneErr){  //克隆错误
                //                            } // else if(other type err happened) ，显示其他类型的ErrRepoCard ,这里还能细分不同的错误显示不同的界面，例如克隆错误和初始化错误可以显示不同界面，后面加else if 即可
            }

        }


    }
    //没换页面，但需要刷新页面，这时LaunchedEffect不会执行，就靠这个变量控制刷新页面了
//    if(needRefreshRepoPage.value) {
//        initRepoPage()
//        needRefreshRepoPage.value=false
//    }
    //compose创建时的副作用
    LaunchedEffect(needRefreshRepoPage.value) {
        // TODO 仓库页面检查仓库状态，对所有状态为notReadyNeedClone的仓库执行clone，卡片把所有状态为notReadyNeedClone的仓库都设置成不可操作，显示正在克隆loading信息
        try {
            initRepoPage()

        } catch (cancel: Exception) {
//            println("LaunchedEffect: job cancelled")
        }
    }
}

@Composable
private fun getInit(
    dbContainer: AppContainer,
    repoDtoList: CustomStateListSaveable<RepoEntity>,
    cloningText: String,
    unknownErrWhenCloning: String,
    loadingOn:(String)->Unit,
    loadingOff:()->Unit,
    appContext:Context
): () -> Unit = {
    doJobThenOffLoading(loadingOn, loadingOff, appContext.getString(R.string.loading)) {
        //执行仓库页面的初始化操作
        val repoRepository = dbContainer.repoRepository
        //貌似如果用Flow，后续我更新数据库，不需要再次手动更新State数据就会自动刷新，也就是Flow会观测数据，如果改变，重新执行sql获取最新的数据，但最好还是手动更新，避免资源浪费
        val repoListFromDb = repoRepository.getAll();
        repoDtoList.value.clear()
        repoDtoList.value.addAll(repoListFromDb)
//        repoDtoList.requireRefreshView()
//        repoDtoList.requireRefreshView()
        for ((idx,item) in repoListFromDb.toList().withIndex()) {
            //对需要克隆的仓库执行克隆
            if (item.workStatus == Cons.dbRepoWorkStatusNotReadyNeedClone) {
                //设置临时状态为 正在克隆...
                //我严重怀疑这里不需要拷贝元素赋值就可立即看到修改后的元素状态是因为上面addAll和这里的赋值操作刚好在视图的同一个刷新间隔里，所以到下一个刷新操作时，可看到在这修改后的状态
                repoDtoList.value[idx].tmpStatus = cloningText
//                repoDtoList.requireRefreshView()

                doJobThenOffLoading {
                    val key = item.id
                    val repoLock = Cons.repoLockMap.getOrPut(key) {
                        //如果get不到，put这个进去
                        Mutex()
                    }
                    //避免多个协程同时执行克隆，所以需要lock
                    repoLock.withLock {
                        //重新查询一次数据，其他协程会在克隆成功后更新数据库，这里如果发现状态有变，就不用执行克隆了
                        // 获取到对象返回对象，如果null，结束锁定代码块
                        val repo2ndQuery = repoRepository.getById(key) ?: return@withLock
                        val repoDir = File(repo2ndQuery.fullSavePath)
                        if (repo2ndQuery.workStatus == Cons.dbRepoWorkStatusNotReadyNeedClone) {
                            deleteIfFileOrDirExist(repoDir)

                            val cloneUrl = repo2ndQuery.cloneUrl
                            val savePath = repo2ndQuery.fullSavePath
                            val branch = repo2ndQuery.branch
                            val depth = repo2ndQuery.depth
                            val hasDepth = depth > 0

                            val options = Clone.Options.defaultOpts()
                            if (branch.isNotBlank()) {
                                options.checkoutBranch = branch
                                if (dbIntToBool(repo2ndQuery.isSingleBranch)) {
                                    // 这里branch要用具名参数，不然就传成remote了
                                    options.setRemoteCreateCb { repo, name, url ->
                                        // name = "origin", url= "xxxx.git"。 这的url其实就是用户设置的那个克隆url，而name其实也只有 origin 一种可能(？不太确定，但应该是）
                                        MyLog.d(TAG, "in cloneOptions.setRemoteCreateCb: name=$name, url=$url")
                                        //这里的 `name`，其实就是 "origin"，基本不会有其他可能
                                        val singleBranchRefSpec = Libgit2Helper.getGitRemoteFetchRefSpec(remote = name, branch = branch)
                                        MyLog.d(TAG, "in cloneOptions.setRemoteCreateCb: singleBranchRefSpec=$singleBranchRefSpec")
                                        //测试能否通过repo获取当前仓库名，如果能，即使用户没填分支，也可启用singlebranch
//                                        MyLog.d(TAG, "RemoteCreateCb: test get current branch name:"+Libgit2Helper.resolveHEAD(repo)?.name())  // 期望能获得分支名，结果是null，这时 repo.headUnborun() 为true，head还没出生呢！不过也有其他办法实现不填branch也可以singlebranch那就是先成功克隆仓库，然后取出head指向的分支，然后再删除仓库再重新克隆，设置之前取出的分支为singlebranch的分支就行了，但没必要，不如让用户填一个，不填就别开single branch
                                        Remote.createWithFetchspec(
                                            repo,
                                            name,
                                            URI.create(url),
                                            singleBranchRefSpec
                                        )
                                    }
                                }
                            }

                            //TODO 这个depth好使不好使，有待测试，如果加了depth就只能遍历到对应深度的commit的话，就没问题，否则可能需要进一步修改，暂时先不管了，写commit列表的时候再说，如果到时候加了depth的仓库能遍历出多于depth的记录数，就有问题，到时再回头改这里
                            if (hasDepth) {
                                options.fetchOpts.depth = depth
                            }

                            /*
                            (?和这个好像没什么关系)fetch时设置网络重定向
                                如果手机克隆出错和这个有关把pull push 那个页面的也设置上
                                但好像和这个没什么关系，问题好像出在credential callback
                            */
                            //  Libgit2Helper.setRedirectForFetchOpts(options.fetchOpts)

                            //TEST，测试通过，如果git24j设置凭据回调有bug，可改用这个方法
//                            LibgitTwo.jniSetCredentialCbTest(options.fetchOpts.callbacks.rawPointer)
                            //TEST

                            val credentialId = repo2ndQuery.credentialIdForClone
                            //do clone
                            if (credentialId.isNotBlank()) {
                                //先查询下credential信息，如果不为null，检查credential类型，如果是http，用httpauth方式克隆，如果是ssh，用ssh方式克隆
                                val credentialDb = AppModel.singleInstanceHolder.dbContainer.credentialRepository
                                val credentialFromDb = credentialDb.getByIdWithDecrypt(credentialId)
                                if (credentialFromDb != null) {
                                    val credentialType = credentialFromDb.type
                                    val usernameOrPrivateKey = credentialFromDb.value;
                                    val passOrPassphrase = credentialFromDb.pass;
                                    //设置验证凭据的回调
                                    Libgit2Helper.setCredentialCbForRemoteCallbacks(options.fetchOpts.callbacks, credentialType, usernameOrPrivateKey, passOrPassphrase)



//                                    Libgit2Helper.setCredentialForFetchOptions(options.fetchOpts, credentialType, usernameOrPrivateKey, passOrPassphrase)
//                                    options.fetchOpts.callbacks.setCredAcquireCb { url: String, usernameFromUrl: String, allowedTypes: Int ->
//                                        if (credentialType == Cons.dbCredentialTypeHttp) {  //type http
//                                            Credential.userpassPlaintextNew(
//                                                usernameOrPrivateKey,
//                                                passOrPassphrase
//                                            )
//                                        } else {  //type SSH
//                                            //如果没能从url取出用户名，则设置成常用的用户名
//                                            val usernameForSsh =
//                                                usernameFromUrl.ifBlank { Cons.gitWellKnownSshUserName }
//                                            val passphraseOrNull =
//                                                passOrPassphrase.ifBlank { null }  // pass?pass:null
//
//                                            //params: username, publickey, privatekey, passphrase。其中username和privatekey必须有，passphrase如果设置了就有，否则没有，publickey在客户端不需要。
//                                            Credential.sshKeyMemoryNew(
//                                                usernameForSsh, null,
//                                                usernameOrPrivateKey, passphraseOrNull
//                                            )
//                                        }
//                                    }
                                }
                                //test
//                                if(debugModeOn) {
                                    //测试设置回调能否放行找不到know_host的ssh连接，结论是不能
                                    //failed，libgit2，安卓找不到known_host文件，设置证书检测回调也没用，这机制不太好，应该改成如果设了回调，完全由回调控制，算了，不弄了，放弃了，暂不支持ssh了
//                                    options.fetchOpts.callbacks.setCertificateCheckCb{ cert:Cert,  valid:Boolean, host:String ->
//                                        println("cert:"+cert.toString())
//                                        println("valid:"+valid)
//                                        println("host:"+host)
//                                        0
//                                    }

//                                    LibgitTwo.jniSetCertCheck(options.fetchOpts.callbacks.rawPointer)
//                                }
                                //test
                            }

                            //开始克隆
                            try {
                                // use 可自动关流，这里自动关的是仓库对象，因为引用了c内存，需要释放
                                Clone.cloneRepo(cloneUrl, savePath, options).use { clonedRepo ->

                                    //克隆成功

                                    //克隆子模块，如果勾选了递归克隆的话（20240418，当前版本隐藏了递归克隆选项，所以这里永假）
                                    if (dbIntToBool(repo2ndQuery.isRecursiveCloneOn)) {
                                        //疑惑：这个东西能共享吗？
                                        val submoduleUpdateOptions =
                                            Submodule.UpdateOptions.createDefault()
                                        /*
                                        * Allow fetching from the submodule's default remote if the target
                                        * commit isn't found. Enabled by default.
                                         */
                                        //                                        submoduleUpdateOptions.allowFetch = true  //默认就开了，不用再开

                                        //疑惑：submodule 递归克隆，不确定是不是这样用
                                        Submodule.foreach(clonedRepo) { submodule, submoduleName ->
                                            //我看了下libgit2源码，默认是RecurseT.NO，也就是不会递归克隆子仓库的子仓库？我看着应该是
                                            //这个设置是针对（父）仓库的，这里是clonedRepo
                                            Submodule.setFetchRecurseSubmodules(
                                                clonedRepo,
                                                submoduleName,
                                                Submodule.RecurseT.YES
                                            ) //递归克隆子仓库的子仓库

                                            //克隆子仓库，默认应该在主仓库内
                                            //疑惑：这操作对不对？
                                            /*
                                            git命令应该类似下面这样：
                                            `git -C dist/git24j/ submodule sync --recursive`
                                            `git -C dist/git24j/ submodule update --init --recursive`
                                             */
                                            submodule.clone(submoduleUpdateOptions)  //克隆好像执行这个就行了，后续检查更新(fetch)的时候可能需要执行下面的update和sync方法
                                            //                                                    submodule.sync()
                                            //                                                    submodule.update(true, submoduleUpdateOptions)
                                            //                                                    submodule.init(true)  //true强制覆盖已存在  //这个好像不用执行，以后检查更新(fetch)应该也不需要执行这个
                                            0
                                        }
                                    }


                                    //更新数据库信息

//                                    var fullBranchRefStr: String? = null;
//                                    var fullCommitOid: Oid? = null;
                                    // set branch if user no filled branch when setting clone info
                                    //用户如果没填branch，获取 HEAD关联的完整ref名和commitOid对象；否则获取用户填的branch对应的完整ref名和commitOid对象
//                                    if (branch.isBlank()) {  //用户没填branch的情况，不对，其实不管用户填没填branch，这里都该获取下实际的branch
                                    //更新分支信息
                                    val headRef = Libgit2Helper.resolveHEAD(clonedRepo)
                                    //HEAD应该不会为null，但还是要判断下以防万一
                                    //若HEAD不为null，取出分支名和最新提交号
                                    if (headRef != null) {
//                                            fullBranchRefStr = headRef.name()
                                        //分支短名
                                        repo2ndQuery.branch = headRef.shorthand()
                                        //提交短id
                                        repo2ndQuery.lastCommitHash = Libgit2Helper.getShortOidStrByFull(headRef.id().toString())
                                    }
//                                    }  //如果用户填了branch且克隆成功，那branch是绝对正确的，这里就不需要更新repo2ndQuery的branch字段了
//                                    else {  //用户填了branch的情况
//                                        //println("走这了：：："+ Cons.gitDefaultRemoteOriginStartStrPrefix+repo2ndQuery.branch)
//                                        val branchRef = Reference.lookup(clonedRepo, Cons.gitDefaultRemoteOriginStartStrPrefix+repo2ndQuery.branch)
//                                        if (branchRef != null) {
//                                            if (branchRef.type() == Reference.ReferenceType.SYMBOLIC) {
//                                                fullBranchRefStr = branchRef.symbolicTarget()  //branch name
//                                            } else if (branchRef.type() == Reference.ReferenceType.DIRECT) {
//                                                fullCommitOid = branchRef.target()
//                                            }
//                                        }
//                                    }

                                    //从克隆后的仓库查询出：
                                    //      lastCommitHash、branch、
                                    //      pullRemoteName、pushRemoteName、pullRemoteUrl、pushRemoteUrl，
                                    //      然后更新repo2ndQuery对象
                                    //latest commit hash
//                                    val revWalk = Revwalk.create(clonedRepo)
//                                    revWalk.sorting(
//                                        //按时间降序，然后取第一个
//                                        EnumSet.of(
//                                            SortT.TOPOLOGICAL,
//                                            SortT.TIME,
////                                            SortT.REVERSE  //如果加这个，就是从最旧的commit开始找了，也就是升序
//                                        )
//                                    )
//                                    //获取当前分支的引用；如果当前分支为空，则可能是detached HEAD，这时获取commitOid；否则获取HEAD兜底
////                                    if(repo2ndQuery.branch.isNotBlank() && fullBranchRefStr!=null) {
////                                        revWalk.pushRef(fullBranchRefStr)
////                                    }else if(fullCommitOid != null) {
//                                    // "refs/remotes/origin/HEAD"
////                                        revWalk.push(fullCommitOid)
////                                    }else {
//                                    revWalk.pushHead()
////                                    }

//                                    val curBranchLatestCommitHash = revWalk.next()
//                                        .toString()  // commit hash is Oid(Git Object ID)

//                                    test_checkAndPrintDepth(clonedRepo)



                                    //set remoteName and remoteUrl fields
//                                    val defaultRemoteName = Remote.list(clonedRepo)[0]  // remote "origin"
                                    val defaultRemoteName = Cons.gitDefaultRemoteOrigin  //"origin"就是默认的名字，根本不用执行上面的查询
                                    repo2ndQuery.pullRemoteName = defaultRemoteName;
                                    repo2ndQuery.pushRemoteName = defaultRemoteName;
                                    repo2ndQuery.pullRemoteUrl = repo2ndQuery.cloneUrl
                                    repo2ndQuery.pushRemoteUrl = repo2ndQuery.cloneUrl

                                    //更新isShallow的值，检查仓库有没有shallow文件就可以
                                    val isRepoShallow = Libgit2Helper.isRepoShallow(clonedRepo)
                                    repo2ndQuery.isShallow = boolToDbInt(isRepoShallow)
                                    if(isRepoShallow) {
                                        //创建shallow文件备份，目前20240509 libgit2有bug
                                        Libgit2Helper.ShallowManage.createShallowBak(repo2ndQuery.fullSavePath+File.separator+".git")
                                    }

                                    //设置当前分支关联的上游分支，例如 main 关联的默认是 origin/main。下面两种方法都行，因为刚克隆的分支肯定有上游，所以用哪个都能取出值
                                    //方法1：这个方法有缺陷，如果配置文件有，但没发布，查不出来
//                                    repo2ndQuery.upstreamBranch = Libgit2Helper.getUpstreamRemoteBranchShortRefSpecByLocalBranchShortName(clonedRepo, repo2ndQuery.branch)?:""
                                    //方法2：这个方法没缺陷，只要配置文件有，即使没发布也能查出来
                                    repo2ndQuery.upstreamBranch = Libgit2Helper.getUpstreamOfBranch(clonedRepo, repo2ndQuery.branch).remoteBranchShortRefSpec


                                    //更新数据库状态
                                    repo2ndQuery.workStatus = Cons.dbRepoWorkStatusUpToDate
                                    repo2ndQuery.createErrMsg = ""

                                }

                            } catch (e: Exception) {
                                repo2ndQuery.workStatus = Cons.dbRepoWorkStatusCloneErr
                                repo2ndQuery.createErrMsg =
                                    e.localizedMessage ?: unknownErrWhenCloning
                                //如果出错，删除仓库目录
                                deleteIfFileOrDirExist(repoDir)
                                MyLog.e(TAG, "cloneErr:"+e.stackTraceToString())
                            }

                            repo2ndQuery.baseFields.baseUpdateTime = getSecFromTime()
                            repo2ndQuery.lastUpdateTime = getSecFromTime()

                            try {
                                //更新数据库
                                if (repo2ndQuery.workStatus == Cons.dbRepoWorkStatusUpToDate) {
                                    //克隆成功，更新仓库，创建Remote
                                    repoRepository.cloneDoneUpdateRepoAndCreateRemote(repo2ndQuery)
                                } else {  // if err, don't create remote, only update repo
                                    repoRepository.update(repo2ndQuery)
                                }
                            }catch (e:Exception) {
                                MyLog.e(TAG, "clone success but update db err:"+e.stackTraceToString())
                            }



                            //清空临时状态
                            repo2ndQuery.tmpStatus = ""
                            //更新state
                            //这里的repo是从数据库重查的，直接赋值即可看到最新状态，不用拷贝
                            repoDtoList.value[idx] = repo2ndQuery
                            //请求刷新视图
//                            repoDtoList.requireRefreshView()
//                            refreshState(repoDtoList)
                        }

                        //克隆代码块外的代码块，不需克隆，就啥也不做就行了

                    }

                }
            } else {
                //TODO: check git status with lock of every repo, get lock then query repo info from db,
                // if updatetime field changed, then update item in repodtolist, else do git status,
                // then update db and repodtolist
            }
        }


        //在这clear()很可能不管用，因为上面的闭包捕获了repoDtoList当时的值，而当时是有数据的，也就是数据在这被清，然后在上面的闭包被回调的时候，又被填充上了闭包创建时的数据，同时加上了闭包执行后的数据，所以，在这清这个list就“不管用”了，实际不是不管用，只是清了又被填充了
//                repoDtoList.clear()
    }
}

