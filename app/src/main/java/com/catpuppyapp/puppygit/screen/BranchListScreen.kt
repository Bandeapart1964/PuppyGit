package com.catpuppyapp.puppygit.screen

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
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CenterFocusWeak
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.FilterAlt
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.catpuppyapp.puppygit.compose.BottomSheet
import com.catpuppyapp.puppygit.compose.BottomSheetItem
import com.catpuppyapp.puppygit.compose.BranchItem
import com.catpuppyapp.puppygit.compose.CheckoutDialog
import com.catpuppyapp.puppygit.compose.CheckoutDialogFrom
import com.catpuppyapp.puppygit.compose.ConfirmDialog
import com.catpuppyapp.puppygit.compose.ConfirmDialog2
import com.catpuppyapp.puppygit.compose.CopyableDialog
import com.catpuppyapp.puppygit.compose.CreateBranchDialog
import com.catpuppyapp.puppygit.compose.FilterTextField
import com.catpuppyapp.puppygit.compose.GoToTopAndGoToBottomFab
import com.catpuppyapp.puppygit.compose.LoadingDialog
import com.catpuppyapp.puppygit.compose.LongPressAbleIconBtn
import com.catpuppyapp.puppygit.compose.MyCheckBox
import com.catpuppyapp.puppygit.compose.MyLazyColumn
import com.catpuppyapp.puppygit.compose.ResetDialog
import com.catpuppyapp.puppygit.compose.ScrollableColumn
import com.catpuppyapp.puppygit.compose.SetUpstreamDialog
import com.catpuppyapp.puppygit.constants.Cons
import com.catpuppyapp.puppygit.data.entity.CredentialEntity
import com.catpuppyapp.puppygit.data.entity.RepoEntity
import com.catpuppyapp.puppygit.dev.branchListPagePublishBranchTestPassed
import com.catpuppyapp.puppygit.dev.branchRenameTestPassed
import com.catpuppyapp.puppygit.dev.dev_EnableUnTestedFeature
import com.catpuppyapp.puppygit.dev.proFeatureEnabled
import com.catpuppyapp.puppygit.dev.rebaseTestPassed
import com.catpuppyapp.puppygit.dev.resetByHashTestPassed
import com.catpuppyapp.puppygit.etc.Ret
import com.catpuppyapp.puppygit.git.BranchNameAndTypeDto
import com.catpuppyapp.puppygit.play.pro.R
import com.catpuppyapp.puppygit.style.MyStyleKt
import com.catpuppyapp.puppygit.ui.theme.Theme
import com.catpuppyapp.puppygit.user.UserUtil
import com.catpuppyapp.puppygit.utils.AppModel
import com.catpuppyapp.puppygit.utils.Libgit2Helper
import com.catpuppyapp.puppygit.utils.Msg
import com.catpuppyapp.puppygit.utils.MyLog
import com.catpuppyapp.puppygit.utils.StateRequestType
import com.catpuppyapp.puppygit.utils.UIHelper
import com.catpuppyapp.puppygit.utils.cache.Cache
import com.catpuppyapp.puppygit.utils.changeStateTriggerRefreshPage
import com.catpuppyapp.puppygit.utils.createAndInsertError
import com.catpuppyapp.puppygit.utils.dbIntToBool
import com.catpuppyapp.puppygit.utils.doJobThenOffLoading
import com.catpuppyapp.puppygit.utils.getSecFromTime
import com.catpuppyapp.puppygit.utils.replaceStringResList
import com.catpuppyapp.puppygit.utils.showErrAndSaveLog
import com.catpuppyapp.puppygit.utils.state.mutableCustomStateListOf
import com.catpuppyapp.puppygit.utils.state.mutableCustomStateOf
import com.github.git24j.core.Branch
import com.github.git24j.core.Repository

private val TAG = "BranchListScreen"
private val stateKeyTag = "BranchListScreen"

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun BranchListScreen(
//    context: Context,
//    navController: NavHostController,
//    scope: CoroutineScope,
//    haptic:HapticFeedback,
//    homeTopBarScrollBehavior: TopAppBarScrollBehavior,
    repoId:String,
//    branch:String?,
    naviUp: () -> Boolean,
) {
    val homeTopBarScrollBehavior = AppModel.singleInstanceHolder.homeTopBarScrollBehavior
    val navController = AppModel.singleInstanceHolder.navController
    val appContext = AppModel.singleInstanceHolder.appContext
    val haptic = LocalHapticFeedback.current
    val scope = rememberCoroutineScope()

    val inDarkTheme = Theme.inDarkTheme

    //获取假数据
    val list = mutableCustomStateListOf(keyTag = stateKeyTag, keyName = "list", initValue = listOf<BranchNameAndTypeDto>())

    //请求闪烁的条目，用来在定位某条目时，闪烁一下以便用户发现
    val requireBlinkIdx = rememberSaveable{mutableIntStateOf(-1)}

    //这个页面的滚动状态不用记住，每次点开重置也无所谓
    val listState = rememberLazyListState()
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = MyStyleKt.BottomSheet.skipPartiallyExpanded)
    val showBottomSheet = rememberSaveable { mutableStateOf(false)}
    val showCreateBranchDialog = rememberSaveable { mutableStateOf(false)}
    val requireCheckout = rememberSaveable { mutableStateOf(false)}
    val showCheckoutBranchDialog = rememberSaveable { mutableStateOf(false)}
    val forceCheckoutForCreateBranch = rememberSaveable { mutableStateOf(false)}
//    val showCheckoutRemoteBranchDialog = StateUtil.getRememberSaveableState(initValue = false)
    val showSetUpstreamForLocalBranchDialog = rememberSaveable { mutableStateOf(false)}
    val needRefresh = rememberSaveable { mutableStateOf("")}
    val branchName = rememberSaveable { mutableStateOf("")}
//    val curCommit = rememberSaveable{ mutableStateOf(CommitDto()) }
    val curObjInPage = mutableCustomStateOf(keyTag = stateKeyTag, keyName = "curObjInPage", initValue =BranchNameAndTypeDto())  //如果是detached
    val curRepo = mutableCustomStateOf(keyTag = stateKeyTag, keyName = "curRepo", initValue = RepoEntity(id=""))

    val showMergeDialog = rememberSaveable { mutableStateOf(false)}
    val requireRebase = rememberSaveable { mutableStateOf(false)}



//    val checkoutRemoteOptionDetachHead=0
//    val checkoutRemoteOptionCreateBranch=1
//    val checkoutRemoteOptionDefault=1  //默认选中创建分支，detach head如果没reflog，有可能丢数据
//    val checkoutRemoteOptions = listOf(appContext.getString(R.string.detach_head),
//        appContext.getString(R.string.new_branch)+"("+appContext.getString(R.string.recommend)+")")
//    val checkoutRemoteSelectedOption = StateUtil.getRememberSaveableIntState(initValue = checkoutRemoteOptionDefault)
//    val checkoutRemoteCreateBranchName = StateUtil.getRememberSaveableState(initValue = "")

    //这个变量代表当前仓库的“活跃分支”，不要用来干别的，只是用来在创建分支的时候让用户知道是基于哪个分支创建的。
    val repoCurrentActiveBranchOrShortDetachedHashForShown = rememberSaveable { mutableStateOf("")}  //用来显示给用户看的短分支名或提交号
    val repoCurrentActiveBranchFullRefForDoAct = rememberSaveable { mutableStateOf("")}  //分支长引用名，只有在非detached时，才用到这个变量
    val repoCurrentActiveBranchOrDetachedHeadFullHashForDoAct = rememberSaveable { mutableStateOf("")}  //合并detached head时用这个变量
    val curRepoIsDetached = rememberSaveable { mutableStateOf(false)}  //当前仓库是否detached

    val defaultLoadingText = stringResource(R.string.loading)
    val loading = rememberSaveable { mutableStateOf(false)}
    val loadingText = rememberSaveable { mutableStateOf(defaultLoadingText)}
    val loadingOn = { text:String ->
        loadingText.value=text
        loading.value=true
    }
    val loadingOff = {
        loadingText.value = appContext.getString(R.string.loading)
        loading.value=false
    }


    val requireShowToast:(String)->Unit = Msg.requireShowLongDuration





    suspend fun doMerge(trueMergeFalseRebase:Boolean=true):Ret<Unit?>{
        //如果选中条目和仓库当前活跃分支一样，则不用合并
        if(curObjInPage.value.oidStr == repoCurrentActiveBranchOrDetachedHeadFullHashForDoAct.value) {
//            requireShowToast(appContext.getString(R.string.merge_failed_src_and_target_same))
            requireShowToast(appContext.getString(R.string.already_up_to_date))
            return Ret.createSuccess(null)  //源和目标一样不算错误，返回true
        }

        Repository.open(curRepo.value.fullSavePath).use { repo ->
            val (usernameFromConfig, emailFromConfig) = Libgit2Helper.getGitUsernameAndEmail(repo)

            //如果用户名或邮箱无效，无法创建commit，merge无法完成，所以，直接终止操作
            if(Libgit2Helper.isUsernameAndEmailInvalid(usernameFromConfig,emailFromConfig)) {
                return Ret.createError(null, appContext.getString(R.string.plz_set_username_and_email_first))
            }


            val targetRefName= curObjInPage.value.fullName  //如果是detached这个值是空，会用后面的hash来进行合并，如果非detached，即使这个传长引用名在冲突文件里显示的依然是短引用名
            val username= usernameFromConfig
            val email= emailFromConfig
            val requireMergeByRevspec= curRepoIsDetached.value  //如果是detached head，没当前分支，用下面的revspec（commit hash）进行合并，否则用上面的targetRefName(分支短或全名）进行合并
            val revspec= curObjInPage.value.oidStr

            val mergeResult = if(trueMergeFalseRebase) {
                Libgit2Helper.mergeOneHead(
                    repo = repo,
                    targetRefName = targetRefName,
                    username = username,
                    email = email,
                    requireMergeByRevspec = requireMergeByRevspec,
                    revspec = revspec,
                )
            } else {
                Libgit2Helper.mergeOrRebase(
                    repo,
                    targetRefName = targetRefName,
                    username = username,
                    email = email,
                    requireMergeByRevspec = requireMergeByRevspec,
                    revspec = revspec,
                    trueMergeFalseRebase = false
                )
            }

            if (mergeResult.hasError()) {
                //检查是否存在冲突条目
                //如果调用者想自己判断是否有冲突，可传showMsgIfHasConflicts为false
                val errMsg = if (mergeResult.code == Ret.ErrCode.mergeFailedByAfterMergeHasConfilts) {
                    appContext.getString(R.string.has_conflicts)
//                    if(trueMergeFalseRebase) {
//                        appContext.getString(R.string.merge_has_conflicts)
//                    }else {
//                        appContext.getString(R.string.rebase_has_conflicts)
//                    }
                }else{
                    //显示错误提示
                    mergeResult.msg
                }

                //记到数据库error表(应由调用者负责记）
//                createAndInsertError(curRepo.value.id, mergeResult.msg)

                return Ret.createError(null, errMsg)
            }


            //执行到这，既没冲突，又没出错，要么 into 的那个分支已经是最新，要么就合并成功创建了新提交

            //这段代码废弃，改用 Libgit2Helper.updateDbAfterMergeSuccess() 了
            //如果操作成功，显示下成功提示
//            if(mergeResult.code == Ret.SuccessCode.upToDate) {  //合并成功，但什么都没改，因为into的那个分支已经领先或者和mergeTarget拥有相同的最新commit了(换句话说：接收合并的那个分支要么比请求合并的分支新，要么和它一样)
//                // up to date 时 hash没变，所以不用更新db，只显示下提示即可
//                requireShowToast(appContext.getString(R.string.already_up_to_date))
//            }else {  //合并成功且创建了新提交
//                //合并完了，创建了新提交，需要更新db
//                val repoDB = AppModel.singleInstanceHolder.dbContainer.repoRepository
//                val shortNewCommitHash = mergeResult.data.toString().substring(Cons.gitShortCommitHashRange)
//                //更新db
//                repoDB.updateCommitHash(
//                    repoId=curRepo.value.id,
//                    lastCommitHash = shortNewCommitHash,
//                )
//
//                //显示成功通知
//                requireShowToast(appContext.getString(R.string.merge_success))
//
//            }
            //合并成功清下仓库状态，要不然可能停留在Merging
            Libgit2Helper.cleanRepoState(repo)
            //合并完成后更新db，显示通知
            Libgit2Helper.updateDbAfterMergeSuccess(mergeResult,appContext,curRepo.value.id, requireShowToast, trueMergeFalseRebase)
        }


        return Ret.createSuccess(null)
    }

    if (showCreateBranchDialog.value) {
        CreateBranchDialog(
            branchName = branchName,
            curRepo = curRepo.value,
            curBranchName = repoCurrentActiveBranchOrShortDetachedHashForShown.value,
            requireCheckout = requireCheckout,
            forceCheckout=forceCheckoutForCreateBranch,
            loadingOn=loadingOn,
            loadingOff = loadingOff,
            loadingText = stringResource(R.string.creating_branch),
            onCancel = {showCreateBranchDialog.value=false},
            onErr = {e->
                val branchName = branchName.value
                val errSuffix = " -(at create branch dialog, branch name=$branchName)"
                Msg.requireShowLongDuration(e.localizedMessage ?:"create branch err")
                createAndInsertError(repoId, ""+e.localizedMessage+errSuffix)
                MyLog.e(TAG, "create branch err: name=$branchName, requireCheckout=${requireCheckout.value}, forceCheckout=${forceCheckoutForCreateBranch.value}, err="+e.stackTraceToString())
            },
            onFinally = {
                changeStateTriggerRefreshPage(needRefresh)
            }
        )
    }

    val checkoutLocalBranch = rememberSaveable { mutableStateOf(false)}
    if(showCheckoutBranchDialog.value) {
        //注意：这种写法，如果curObjInPage.value被重新赋值，本代码块将会被重复调用！不过实际不会有问题，因为显示弹窗时无法再长按条目进而无法改变本对象。
        // 另外如果在onOk里取对象也会有此问题，假如显示弹窗后对象被改变，那视图会更新，变成新对象的值，onOk最终执行时取出的对象自然也会和“最初”弹窗显示的不一致 (onOk取出的和“现在”视图显示的对象是一致的，都是修改后的值，“最初”的值已被覆盖)，
        // 如果用户在按弹窗的确定按钮前的一瞬间改变了此对象，那就会造成视图显示的对象和onOk取出的对象不一致的问题，不过这种问题几乎不会发生。
        //避免方法：给每个弹窗设置独立的变量，并仅在onClick之类的不会自动执行的callback里为其赋值，但这样每个组件都要有自己的状态，还要有专门的初始化函数，代码更繁琐，也更费内存。
        val item = curObjInPage.value

        CheckoutDialog(
            showCheckoutDialog=showCheckoutBranchDialog,
            from = CheckoutDialogFrom.BRANCH_LIST,
            showJustCheckout=checkoutLocalBranch.value,
            expectCheckoutType = if(checkoutLocalBranch.value) Cons.checkoutType_checkoutRefThenUpdateHead else Cons.checkoutType_checkoutRefThenDetachHead,
            shortName = item.shortName,
            fullName=item.fullName,
            curRepo = curRepo.value,
            curCommitOid = item.oidStr,
            curCommitShortOid = item.shortOidStr,
            requireUserInputCommitHash = false,
            loadingOn = loadingOn,
            loadingOff = loadingOff,
            onlyUpdateCurItem = false,
            updateCurItem = {curItemIdx, fullOid-> },  //不需要更新当前条目
            refreshPage = {
                changeStateTriggerRefreshPage(needRefresh)
            },
            curCommitIndex = -1,  //不需要更新条目，自然不需要有效索引
            findCurItemIdxInList = { fullOid->
                -1  //无效id，不需要更新条目
            }
        )
    }

    val upstreamRemoteOptionsList = mutableCustomStateListOf(
        keyTag = stateKeyTag,
        keyName = "upstreamRemoteOptionsList",
        initValue = listOf<String>()
    )  //初始化页面时更新这个列表
    val upstreamSelectedRemote = rememberSaveable{mutableIntStateOf( 0)}  //默认选中第一个remote，每个仓库至少有一个origin remote，应该不会出错
    //默认选中为上游设置和本地分支相同名
    val upstreamBranchSameWithLocal =rememberSaveable { mutableStateOf(true)}
    //把远程分支名设成当前分支的完整名
    val upstreamBranchShortRefSpec = rememberSaveable { mutableStateOf("")}

    if(showSetUpstreamForLocalBranchDialog.value) {
        SetUpstreamDialog(
            remoteList = upstreamRemoteOptionsList.value,
            curBranch = curObjInPage.value.shortName,  //供显示的，让用户知道在为哪个分支设置上游
            selectedOption = upstreamSelectedRemote,
            branch = upstreamBranchShortRefSpec,
            branchSameWithLocal = upstreamBranchSameWithLocal,
            onCancel = {
                //隐藏弹窗就行，相关状态变量会在下次弹窗前初始化
                showSetUpstreamForLocalBranchDialog.value = false
//                changeStateTriggerRefreshPage(needRefresh)  //取消操作，没必要刷新页面
            },
            onOk = onOk@{
                showSetUpstreamForLocalBranchDialog.value = false
                val curBranchFullName = curObjInPage.value.fullName
                val curBranchShortName = curObjInPage.value.shortName
                val repoFullPath = curRepo.value.fullSavePath
                val upstreamSameWithLocal = upstreamBranchSameWithLocal.value
                val remoteList = upstreamRemoteOptionsList.value
                val selectedRemoteIndex = upstreamSelectedRemote.intValue
                val upstreamShortName = upstreamBranchShortRefSpec.value
                val isCurrentBranchOfRepo = curObjInPage.value.isCurrent

                //直接索引取值即可
                val remote = try {
                    remoteList[selectedRemoteIndex]
                } catch (e: Exception) {
                    MyLog.e(TAG,"err when get remote by index from remote list: remoteIndex=$selectedRemoteIndex, remoteList=$remoteList\nerr info:${e.stackTraceToString()}")
                    Msg.requireShowLongDuration(appContext.getString(R.string.err_selected_remote_is_invalid))
                    return@onOk
                }


                // update git config
                doJobThenOffLoading(
                    loadingOn,
                    loadingOff,
                    appContext.getString(R.string.setting_upstream)
                ) {
                    try {


                        Repository.open(repoFullPath).use { repo ->
                            var branch = ""
                            if (upstreamSameWithLocal) {  //勾选了使用和本地同名的分支，创建本地同名远程分支
                                //取出repo的当前选中的分支
                                branch = curBranchFullName
                            } else {  //否则取出用户输入的远程分支短名，然后生成长名
                                branch =
                                    Libgit2Helper.getRefsHeadsBranchFullRefSpecFromShortRefSpec(
                                        upstreamShortName
                                    )
                            }
                            MyLog.d(
                                TAG,
                                "set upstream dialog #onOk(): will write to git config: remote=$remote, branch=$branch"
                            )

                            //把分支的upstream信息写入配置文件
                            val setUpstreamSuccess =
                                Libgit2Helper.setUpstreamForBranchByRemoteAndRefspec(
                                    repo,
                                    remote,
                                    branch,
                                    targetBranchShortName = curBranchShortName
                                )

                            //如果是当前活跃分支，更新下db，否则不用更新
                            if (isCurrentBranchOfRepo) {
                                //更新数据库
                                val repoDb =
                                    AppModel.singleInstanceHolder.dbContainer.repoRepository
                                val upstreamBranchShortName =
                                    Libgit2Helper.getUpstreamRemoteBranchShortNameByRemoteAndBranchRefsHeadsRefSpec(
                                        remote,
                                        branch
                                    )
                                MyLog.d(
                                    TAG,
                                    "set upstream dialog #onOk(): upstreamBranchShortName=$upstreamBranchShortName"
                                )
                                repoDb.updateUpstream(repoId, upstreamBranchShortName)
                            }

                            if (setUpstreamSuccess) {
                                requireShowToast(appContext.getString(R.string.set_upstream_success))
                            } else {
                                requireShowToast(appContext.getString(R.string.set_upstream_error))
                            }
                        }
                    } catch (e: Exception) {
                        //显示通知
                        requireShowToast("set upstream err:" + e.localizedMessage)
                        //给用户看到错误
                        createAndInsertError(
                            repoId,
                            "set upstream for '$curBranchShortName' err:" + e.localizedMessage
                        )
                        //给开发者debug看的错误
                        MyLog.e(
                            TAG,
                            "set upstream for '$curBranchShortName' err! user input branch is '$upstreamShortName', selected remote is $remote, user checked use same name with local is '$upstreamSameWithLocal'\nerr:" + e.stackTraceToString()
                        )

                    } finally {
                        changeStateTriggerRefreshPage(needRefresh)
                    }

                }


            },
        )
    }

    if(showMergeDialog.value) {
        ConfirmDialog2(title = stringResource(if(requireRebase.value) R.string.rebase else R.string.merge),
            requireShowTextCompose = true,
            textCompose = {
                          ScrollableColumn {
//                              Row {
//                                  Text(text = appContext.getString(R.string.warn_please_commit_your_change_before_checkout_or_merge),
//                                      color= Color.Red
//                                  )
//                              }
//                              Spacer(modifier = Modifier.padding(5.dp))
//                              Row {
//                                  Text(text = stringResource(if(requireRebase.value) R.string.will_rebase else R.string.will_merge)+":")
//                              }
//                              Spacer(modifier = Modifier.padding(5.dp))
//                              //if branch show "merge branch_a into branch_b"
                              Row(
                                  modifier = Modifier.fillMaxWidth(),
                                  horizontalArrangement = Arrangement.Center,
                                  verticalAlignment = Alignment.CenterVertically
                              ) {
                                  val left = if(!requireRebase.value) curObjInPage.value.shortName else if(curRepoIsDetached.value) Cons.gitDetachedHead else repoCurrentActiveBranchOrShortDetachedHashForShown.value
                                  val right = if(requireRebase.value) curObjInPage.value.shortName else if(curRepoIsDetached.value) Cons.gitDetachedHead else repoCurrentActiveBranchOrShortDetachedHashForShown.value
                                  val text = if(requireRebase.value) {
                                      replaceStringResList(stringResource(R.string.rebase_left_onto_right), listOf(left, right))
                                  }else{
                                      replaceStringResList(stringResource(R.string.merge_left_into_right), listOf(left, right))
                                  }

                                  Text(text = text,
                                      softWrap = true,
                                      overflow = TextOverflow.Visible
                                  )

//                                  if(curRepoIsDetached.value) {
//                                      Text(text = " "+stringResource(id = R.string.into))
//                                      Text(text = " HEAD",
//                                          fontWeight = FontWeight.ExtraBold,
//                                          )
//                                  }
                              }

                          }
            },
            onCancel = { showMergeDialog.value = false }
        ) {  //onOk
            showMergeDialog.value=false
            doJobThenOffLoading(
                loadingOn = loadingOn,
                loadingOff = loadingOff,
                loadingText = if(requireRebase.value) appContext.getString(R.string.rebasing) else appContext.getString(R.string.merging),
            )  job@{
                try {
                    val mergeRet = doMerge(trueMergeFalseRebase = !requireRebase.value)
                    if(mergeRet.hasError()) {
                        throw RuntimeException(mergeRet.msg)
                    }
                }catch (e:Exception) {
                    MyLog.e(TAG, "MergeDialog#doMerge(trueMergeFalseRebase=${!requireRebase.value}) err: "+e.stackTraceToString())

                    requireShowToast(e.localizedMessage ?: "err")

                    val errMsg = "${if(requireRebase.value) "rebase" else "merge"} failed: "+e.localizedMessage
                    createAndInsertError(curRepo.value.id, errMsg)
                }finally {
                    //别忘了刷新页面！
                    changeStateTriggerRefreshPage(needRefresh)
                }

            }
        }
    }

    val showLocalBranchDelDialog = rememberSaveable { mutableStateOf(false)}
    val delUpstreamToo = rememberSaveable { mutableStateOf(false)}  //如果没上游，禁用“删除上游”勾选框，注意，这个勾选框控制的是删除本地的上游分支本地与否，配置文件中为当前本地分支配置的上游设置是一定会删除的(libgit2负责)，不管是否勾选这个选项。
    val delUpstreamPush = rememberSaveable { mutableStateOf(false)}
    val showRemoteBranchDelDialog = rememberSaveable { mutableStateOf(false)}
    val userSpecifyRemoteName = rememberSaveable { mutableStateOf("")}  //删除远程分支时，如果remote有歧义，让用户指定一个具体remote名字
    val curRequireDelRemoteNameIsAmbiguous = rememberSaveable { mutableStateOf(false)}

    if(showLocalBranchDelDialog.value) {
        ConfirmDialog(title = stringResource(R.string.delete_branch),
        requireShowTextCompose = true,
        textCompose = {
                      Column {
                          Row {
                              Text(text = stringResource(id = R.string.del_branch)+":")
                          }
                          Row(modifier = Modifier.padding(5.dp)){
                              // spacer
                          }
                          Row(
                              modifier = Modifier.fillMaxWidth(),
                              horizontalArrangement = Arrangement.Center,
                              verticalAlignment = Alignment.CenterVertically
                          ) {
                              Text(text = curObjInPage.value.shortName,
                                  fontWeight = FontWeight.ExtraBold,
                                  overflow = TextOverflow.Visible
                              )
                          }
                          Row{
                              Text(text = appContext.getString(R.string.are_you_sure))
                          }

                          //若已设置上游且已发布，则可删除远程，否则不可
                          if(curObjInPage.value.isUpstreamValid()) {
                              Row(modifier = Modifier.padding(5.dp)) {

                              }
                              MyCheckBox(text = stringResource(R.string.del_upstream_too), value = delUpstreamToo)
                              if(delUpstreamToo.value) {  //如果能勾选这个选项其实基本就可以断定存在有效上游了
                                  Row (modifier = Modifier.padding(horizontal = 16.dp)){
                                      Text(text = stringResource(id = R.string.upstream)+": ")
                                      Text(text = curObjInPage.value.upstream?.remoteBranchShortRefSpec?:"",  //其实如果通过上面的判断，基本就能断定存在有效上游了，这里的?:空值判断只是以防万一
                                          fontWeight = FontWeight.ExtraBold
                                      )
                                  }

                                  MyCheckBox(text = stringResource(R.string.push), value = delUpstreamPush)
                              }
                          }
                      }
        },
            onCancel = {showLocalBranchDelDialog.value=false}
        ) {
            showLocalBranchDelDialog.value=false
            doJobThenOffLoading(
                loadingOn = loadingOn,
                loadingOff = loadingOff,
                loadingText = appContext.getString(R.string.deleting_branch),
            )  job@{
                try {
                    //删除本地分支
                    Repository.open(curRepo.value.fullSavePath).use { repo ->

                        val deleteBranchRet = Libgit2Helper.deleteBranch(repo, curObjInPage.value.fullName);
                        if(deleteBranchRet.hasError()) {
                            requireShowToast(appContext.getString(R.string.del_branch_err_operation_abort))
                            return@job
                        }
                        requireShowToast(appContext.getString(R.string.del_local_branch_success))

                        //检查当前选中对象是否有上游，如果有，检查用户是否勾选了删除上游，如果是执行删除上游
                        if (delUpstreamToo.value) {  //用户勾选了删除远程分支，检查下有没有有效的远程分支可以删
                            if (curObjInPage.value.isUpstreamValid()) {
                                //进入到这说明用户勾选了一并删除上游并且存在有效上游，提示正在删除远程分支
                                requireShowToast(appContext.getString(R.string.deleting_upstream))

                                //通过上面的判断，upstream不可能是null
                                val upstream = curObjInPage.value.upstream!!
                                //先删除本地的远程分支
                                val delBranchRet = Libgit2Helper.deleteBranch(
                                    repo,
                                    upstream.remoteBranchRefsRemotesFullRefSpec
                                )
                                if (delBranchRet.hasError()) {
                                    requireShowToast(delBranchRet.msg)
                                    return@job
                                }

                                //删除本地的远程分支成功后才会push远程，若失败，不会push

                                //后删除服务器的远程分支
                                if(delUpstreamPush.value) {  //若勾选push，删除远程的分支
                                    //查询凭据
                                    val remoteDb = AppModel.singleInstanceHolder.dbContainer.remoteRepository
                                    val remoteFromDb = remoteDb.getByRepoIdAndRemoteName(
                                        curRepo.value.id,
                                        upstream.remote
                                    )
                                    if (remoteFromDb == null) {
                                        requireShowToast(appContext.getString(R.string.query_remote_error))
                                        return@job
                                    }
                                    var credential: CredentialEntity? = null
                                    if (!remoteFromDb.pushCredentialId.isNullOrBlank()) {
                                        val credentialDb = AppModel.singleInstanceHolder.dbContainer.credentialRepository
//                                        credential = credentialDb.getByIdWithDecrypt(remoteFromDb.pushCredentialId)
                                        credential = credentialDb.getByIdWithDecryptAndMatchByDomain(id = remoteFromDb.pushCredentialId, url = remoteFromDb.pushUrl)
                                    }

                                    //执行删除(push)
                                    val delRemotePushRet = Libgit2Helper.deleteRemoteBranchByRemoteAndRefsHeadsBranchRefSpec(
                                            repo,
                                            upstream.remote,
                                            upstream.branchRefsHeadsFullRefSpec,
                                            credential
                                        )
                                    if (delRemotePushRet.hasError()) {
//                                        requireShowToast(appContext.getString(R.string.push_del_upstream_branch_to_remote_failed))
                                        requireShowToast(delRemotePushRet.msg)
                                        return@job
                                    }
                                }

                                //执行到这，本地分支和其上游都已经删除并推送到服务器（未勾选push则不会推送）了
                                requireShowToast(appContext.getString(R.string.del_upstream_success))
                            }else {  //进入这里说明没有有效的上游，提示下，不用执行删除
                                requireShowToast(appContext.getString(R.string.del_upstream_failed_upstream_is_invalid))
                            }
                        }


                    }
                    //删除远程分支不一定成功，因为名字可能有歧义，这时，提示即可，不弹窗让用户输分支名，如果用户还是想删，可找到对应的远程分支手动删除，那个删除时如果发现remote名歧义就会提示用户输入一个具体的remote名
                }catch (e:Exception) {
                    MyLog.e(TAG, "delLocalBranchDialog err:"+e.stackTraceToString())

                    val errMsg = "del branch failed:"+e.localizedMessage
                    requireShowToast(errMsg)
                    createAndInsertError(curRepo.value.id, errMsg)
                }finally {
                    //别忘了刷新页面！
                    changeStateTriggerRefreshPage(needRefresh)
                }

            }
        }
    }

    val pushCheckBoxForRemoteBranchDelDialog = rememberSaveable { mutableStateOf(false)}
    if(showRemoteBranchDelDialog.value) {
        ConfirmDialog(title = stringResource(R.string.delete_branch),
            okBtnEnabled = !pushCheckBoxForRemoteBranchDelDialog.value || !curRequireDelRemoteNameIsAmbiguous.value || userSpecifyRemoteName.value.isNotBlank(),
            requireShowTextCompose = true,
            textCompose = {
                Column {
                    Row {
                        Text(text = stringResource(id = R.string.del_remote_branch)+":")
                    }
                    Row(modifier = Modifier.padding(5.dp)){
                        // spacer
                    }

                    //可选，方便复制remote名
                    SelectionContainer {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = curObjInPage.value.shortName,
                                fontWeight = FontWeight.ExtraBold,
                                overflow = TextOverflow.Visible
                            )
                        }
                    }
                    Row(modifier = Modifier.padding(5.dp)) {

                    }
                    Row{
                        Text(text = appContext.getString(R.string.are_you_sure))
                    }
                    Row(modifier = Modifier.padding(5.dp)) {

                    }

                    MyCheckBox(text = appContext.getString(R.string.push), value = pushCheckBoxForRemoteBranchDelDialog)

                    //若勾选push，则删除远程分支，删除远程分支时有可能无法从分支名中取出remote，这时需要用户输入
                    if (pushCheckBoxForRemoteBranchDelDialog.value) {
//                        Row {
//                            Text(text = "(" + stringResource(id = R.string.del_remote_branch_require_network_connection) + ")")
//                        }

                        // 如果取不出remote name，说明分支名有歧义，这时，弹出一个输入框，让用户输入分支的remote名字
                        if (curRequireDelRemoteNameIsAmbiguous.value) {
                            Row(modifier = Modifier.padding(5.dp)) {

                            }
                            Row {
                                Text(text = stringResource(R.string.remote_name_ambiguous_plz_specify_remote_name))
                            }
                            Row(modifier = Modifier.padding(5.dp)) {

                            }
                            TextField(
                                modifier = Modifier.fillMaxWidth(),

                                value = userSpecifyRemoteName.value,
                                singleLine = true,
                                onValueChange = {
                                    userSpecifyRemoteName.value = it
                                },
                                label = {
                                    Text(stringResource(R.string.specify_remote_name))
                                },
                                placeholder = {
                                    Text(stringResource(R.string.remote_name))
                                }
                            )
                        }
                    }

                }
            },
            onCancel = { showRemoteBranchDelDialog.value=false}
        ) {
            showRemoteBranchDelDialog.value=false
            doJobThenOffLoading(
                loadingOn = loadingOn,
                loadingOff = loadingOff,
                loadingText = appContext.getString(R.string.deleting_branch),
            )  job@{
                try {
                    Repository.open(curRepo.value.fullSavePath).use { repo ->
                        //先删除本地的远程分支
                        val delBranchRet = Libgit2Helper.deleteBranch(
                            repo,
                            curObjInPage.value.fullName
                        )
                        if (delBranchRet.hasError()) {
                            requireShowToast(appContext.getString(R.string.del_remote_branch_err_operation_abort))
                            return@job
                        }

                        //若勾选push，删除远程
                        if(pushCheckBoxForRemoteBranchDelDialog.value) {
                            //或者把逻辑改成只要用户输入了remote名，就用用户输入的？
                            val remote = if(curRequireDelRemoteNameIsAmbiguous.value) userSpecifyRemoteName.value else curObjInPage.value.remotePrefixFromShortName

                            //如果remote无效，返回
                            if(remote.isNullOrBlank()) {
                                requireShowToast(appContext.getString(R.string.remote_name_is_invalid))
                                return@job
                            }

                            //再删除服务器的远程分支
                            //查询凭据
                            val remoteDb = AppModel.singleInstanceHolder.dbContainer.remoteRepository
                            val remoteFromDb = remoteDb.getByRepoIdAndRemoteName(
                                curRepo.value.id,
                                remote
                            )
                            if (remoteFromDb == null) {
                                requireShowToast(appContext.getString(R.string.query_remote_error))
                                return@job
                            }
                            var credential: CredentialEntity? = null
                            if (!remoteFromDb.pushCredentialId.isNullOrBlank()) {
                                val credentialDb = AppModel.singleInstanceHolder.dbContainer.credentialRepository
//                                credential = credentialDb.getByIdWithDecrypt(remoteFromDb.pushCredentialId)
                                credential = credentialDb.getByIdWithDecryptAndMatchByDomain(id = remoteFromDb.pushCredentialId, url = remoteFromDb.pushUrl)
                            }

                            //例如：移除 origin/main 中的 origin/，然后拼接成 refs/heads/main
                            val branchRefsHeadsFullRefSpec = "refs/heads/"+Libgit2Helper.removeGitRefSpecPrefix(remote+"/", curObjInPage.value.shortName)  // remote值形如：origin/，shortName值形如 origin/main

                            //执行删除
                            val delRemotePushRet = Libgit2Helper.deleteRemoteBranchByRemoteAndRefsHeadsBranchRefSpec(repo, remote, branchRefsHeadsFullRefSpec, credential)
                            if (delRemotePushRet.hasError()) {
                                requireShowToast(appContext.getString(R.string.push_del_remote_branch_failed))
                                return@job
                            }
                        }


                        //执行到这，本地远程分支删除了，且远程服务器上的分支也删除了(push到服务器了)
                        requireShowToast(appContext.getString(R.string.del_remote_branch_success))

                    }

                }catch (e:Exception) {
                    MyLog.e(TAG, "delRemoteBranchDialog err:"+e.stackTraceToString())

                    val errMsg = "del remote branch failed:"+e.localizedMessage
                    requireShowToast(errMsg)
                    createAndInsertError(curRepo.value.id, errMsg)
                }finally {
                    //别忘了刷新页面！
                    changeStateTriggerRefreshPage(needRefresh)
                }

            }
        }
    }

//    val acceptHardReset = StateUtil.getRememberSaveableState(initValue = false)
    val showResetDialog = rememberSaveable { mutableStateOf(false)}
    val resetDialogOid = rememberSaveable { mutableStateOf("")}
//    val resetDialogShortOid = StateUtil.getRememberSaveableState(initValue = "")
    val closeResetDialog = {
        showResetDialog.value = false
    }

    if (showResetDialog.value) {
        ResetDialog(
            fullOidOrBranchOrTag = resetDialogOid,
            closeDialog=closeResetDialog,
            repoFullPath = curRepo.value.fullSavePath,
            repoId=curRepo.value.id,
            refreshPage = {_, _ ->
                changeStateTriggerRefreshPage(needRefresh, StateRequestType.forceReload)
            }
        )

    }

    val clipboardManager = LocalClipboardManager.current

    val showDetailsDialog = rememberSaveable { mutableStateOf(false)}
    val detailsString = rememberSaveable { mutableStateOf("")}
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


    val showRenameDialog = rememberSaveable { mutableStateOf(false)}
    val nameForRenameDialog = rememberSaveable { mutableStateOf("")}
    val forceForRenameDialog = rememberSaveable { mutableStateOf(false)}
    val errMsgForRenameDialog = rememberSaveable { mutableStateOf("")}
    if(showRenameDialog.value) {
        val curItem = curObjInPage.value

        ConfirmDialog(
            title = stringResource(R.string.rename),
            requireShowTextCompose = true,
            textCompose = {
                Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                    TextField(
                        modifier = Modifier
                            .fillMaxWidth()
                        ,
                        value = nameForRenameDialog.value,
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
                            nameForRenameDialog.value = it

                            // clear err msg
                            errMsgForRenameDialog.value = ""
                        },
                        label = {
                            Text(stringResource(R.string.new_name))
                        }
                    )

                    Spacer(Modifier.height(10.dp))

                    MyCheckBox(text = stringResource(R.string.overwrite_if_exist), value = forceForRenameDialog)
                }
            },
            okBtnText = stringResource(R.string.ok),
            cancelBtnText = stringResource(R.string.cancel),
            okBtnEnabled = nameForRenameDialog.value != curItem.shortName,
            onCancel = {showRenameDialog.value = false}
        ) {
            val newName = nameForRenameDialog.value
            val branchShortName = curItem.shortName
            doJobThenOffLoading(loadingOn, loadingOff, appContext.getString(R.string.renaming)) {
                try {
                    Repository.open(curRepo.value.fullSavePath).use { repo->
                        val renameRet = Libgit2Helper.renameBranch(repo, branchShortName, newName, forceForRenameDialog.value)
                        if(renameRet.hasError()) {
                            errMsgForRenameDialog.value = renameRet.msg
                            return@doJobThenOffLoading
                        }

                        showRenameDialog.value=false
                    }

                    Msg.requireShow(appContext.getString(R.string.success))
                }catch (e:Exception) {
                    val errmsg = e.localizedMessage ?: "rename branch err"
                    Msg.requireShowLongDuration(errmsg)
                    createAndInsertError(curRepo.value.id, "err: rename branch '${curObjInPage.value.shortName}' to ${nameForRenameDialog.value} failed, err is $errmsg")
                }finally {
                    changeStateTriggerRefreshPage(needRefresh)
                }
            }
        }
    }



    //filter相关，开始
    val filterKeyword = mutableCustomStateOf(
        keyTag = stateKeyTag,
        keyName = "filterKeyword",
        initValue = TextFieldValue("")
    )
    val filterModeOn = rememberSaveable { mutableStateOf(false)}
    //filter相关，结束

    // 向下滚动监听，开始
    val pageScrolled = remember { mutableStateOf(false) }

//    val filterListState = mutableCustomStateOf(keyTag = stateKeyTag, keyName = "filterListState", LazyListState(0,0))
    val filterListState = rememberLazyListState()
    val enableFilterState = rememberSaveable { mutableStateOf(false)}
//    val firstVisible = remember { derivedStateOf { if(enableFilterState.value) filterListState.value.firstVisibleItemIndex else listState.firstVisibleItemIndex } }
//    ScrollListener(
//        nowAt = firstVisible.value,
//        onScrollUp = {scrollingDown.value = false}
//    ) { // onScrollDown
//        scrollingDown.value = true
//    }
    val lastAt = remember { mutableIntStateOf(0) }
    val lastIsScrollDown = remember { mutableStateOf(false) }
    val forUpdateScrollState = remember {
        derivedStateOf {
            val nowAt = if(enableFilterState.value) {
                filterListState.firstVisibleItemIndex
            } else {
                listState.firstVisibleItemIndex
            }
            val scrolledDown = nowAt > lastAt.intValue  // scroll down
//            val scrolledUp = nowAt < lastAt.intValue

            val scrolled = nowAt != lastAt.intValue  // scrolled
            lastAt.intValue = nowAt

            // only update state when this scroll down and last is not scroll down, or this is scroll up and last is not scroll up
            if(scrolled && ((lastIsScrollDown.value && !scrolledDown) || (!lastIsScrollDown.value && scrolledDown))) {
                pageScrolled.value = true
            }

            lastIsScrollDown.value = scrolledDown
        }
    }.value
    // 向下滚动监听，结束

    val showPublishDialog = rememberSaveable { mutableStateOf(false)}
    val forcePublish = rememberSaveable { mutableStateOf(false)}
    if(showPublishDialog.value) {
        val curBranch = curObjInPage.value
        val upstream = curBranch.upstream
        val remoteBranchShortRefSpec = upstream?.remoteBranchShortRefSpec ?: ""  //其实不用检测这个，upstream.isUpstreamAlreadySet()为真的话，这个值就不会为空

        //publish 应仅对local分支启用，但因为设置的是此选项仅对local分支启用，所以这里不用再检测分支是否local，不过，由于弹窗未使用独立状态变量，所以并不能说一定不会错误地对远程分支执行publish，但弹窗会遮盖用户对下层条目的操作，所以实际上弹窗出现后，当前长按条目就不会再被用户改变了，因此，代码执行到这里便可以断言当前分支一定是local

        //本质上是push实现，若没设置上游，提示先设置，若设置了，无论是否已发布都显示发布弹窗，因为即使发布了也有需要用本地覆盖远程的情况
        if(remoteBranchShortRefSpec.isBlank() || !curBranch.isUpstreamAlreadySet()) {  //没设置上游
            showPublishDialog.value = false  //关弹窗，不然页面重新渲染时会反复执行这里的代码块
            Msg.requireShowLongDuration(stringResource(R.string.plz_set_upstream_first))
        }else {
            ConfirmDialog(title = stringResource(R.string.publish),
                requireShowTextCompose = true,
                textCompose = {
                    ScrollableColumn {
                        Row {
                            Text(text = stringResource(R.string.local) +": ")
                            Text(text = curBranch.shortName,
                                fontWeight = FontWeight.ExtraBold
                                )
                        }

                        Row {
                            Text(text = stringResource(R.string.remote) +": ")
                            Text(text = remoteBranchShortRefSpec,
                                fontWeight = FontWeight.ExtraBold
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))
                        Text(text = stringResource(R.string.will_push_local_branch_to_remote_are_you_sure))
                        Spacer(modifier = Modifier.height(10.dp))

                        MyCheckBox(text = stringResource(id = R.string.force), value = forcePublish)

                        if(forcePublish.value) {
                            Text(text = stringResource(R.string.will_force_overwrite_remote_branch_even_it_is_ahead_to_local),
                                color = MyStyleKt.TextColor.danger)
                        }
                    }
                },
                onCancel = { showPublishDialog.value=false}
            ) {
                showPublishDialog.value=false
                val force = forcePublish.value

                doJobThenOffLoading(loadingOn, loadingOff, appContext.getString(R.string.pushing)) {
                    try {

                        val dbContainer = AppModel.singleInstanceHolder.dbContainer
                        Repository.open(curRepo.value.fullSavePath).use { repo->
                            val credential = Libgit2Helper.getRemoteCredential(
                                dbContainer.remoteRepository,
                                dbContainer.credentialRepository,
                                curRepo.value.id,
                                upstream!!.remote,
                                trueFetchFalsePush = false
                            )

                            val ret = Libgit2Helper.push(repo, upstream!!.remote, upstream!!.pushRefSpec, credential, force)
                            if(ret.hasError()) {
                                throw RuntimeException(ret.msg)
                            }

                            // 更新修改workstatus的时间，只更新时间就行，状态会在查询repo时更新
                            val repoDb = AppModel.singleInstanceHolder.dbContainer.repoRepository
                            repoDb.updateLastUpdateTime(curRepo.value.id, getSecFromTime())

                            Msg.requireShow(appContext.getString(R.string.success))
                        }
                    }catch (e:Exception) {
                        showErrAndSaveLog(TAG, "#PublishBranchDialog(force=$force) err:"+e.stackTraceToString(), "Publish branch error:"+e.localizedMessage, requireShowToast, curRepo.value.id)
                    }finally {
                        changeStateTriggerRefreshPage(needRefresh)
                    }

                }

            }

        }

    }

    val filterList = mutableCustomStateListOf(keyTag = stateKeyTag, keyName = "filterList", initValue = listOf<BranchNameAndTypeDto>())

    val getActuallyListState = {
        if(enableFilterState.value) filterListState else listState
    }

    val getActuallyList = {
        if(enableFilterState.value) filterList.value else list.value
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
                                }
                        ){
                            Row(modifier = Modifier.horizontalScroll(rememberScrollState())) {
                                Text(
                                    text= stringResource(R.string.branches),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                            Row(modifier = Modifier.horizontalScroll(rememberScrollState())) {
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
                            tooltipText = stringResource(R.string.focus_current_branch),
                            icon =  Icons.Filled.CenterFocusWeak,
                            iconContentDesc = stringResource(R.string.focus_current_branch),

                            //非detached HEAD 则启用（注：detached HEAD无当前分支（活跃分支），所以没必要启用）
                            enabled = !dbIntToBool(curRepo.value.isDetached)
                        ) {
                            doJobThenOffLoading(loadingOn, loadingOff, appContext.getString(R.string.loading)) {
                                val indexOfCurrent = list.value.toList().indexOfFirst {
                                    it.isCurrent
                                }

                                if(indexOfCurrent == -1) {
                                    Msg.requireShow(appContext.getString(R.string.not_found))
                                }else {  // found
                                    // 注：直接在源list的list state跳转即可，不需要考虑filter模式是否开启，因为只有当filter模式关闭时才显示此按钮，显示此按钮才有可能被用户按，所以，正常情况下仅能在filter模式关闭时才能用此功能

                                    //跳转到对应条目
                                    UIHelper.scrollToItem(scope, listState, indexOfCurrent)
                                    //设置条目闪烁一下以便用户发现
                                    requireBlinkIdx.intValue = indexOfCurrent
                                }
                            }
                        }

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
                            tooltipText = stringResource(R.string.create_branch),
                            icon =  Icons.Filled.Add,
                            iconContentDesc = stringResource(R.string.create_branch),
                        ) {
                            forceCheckoutForCreateBranch.value=false
                            // 显示添加分支的弹窗
                            showCreateBranchDialog.value=true
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
        if (loading.value) {
//            LoadingText(text = loadingText.value, contentPadding = contentPadding)
            LoadingDialog(text = loadingText.value)
        }

        if(showBottomSheet.value) {
            BottomSheet(showBottomSheet, sheetState, curObjInPage.value.shortName) {
                BottomSheetItem(sheetState, showBottomSheet, stringResource(R.string.checkout),textDesc= stringResource(R.string.switch_branch),
                    enabled = curObjInPage.value.shortName != repoCurrentActiveBranchOrShortDetachedHashForShown.value
                ){
                    checkoutLocalBranch.value = curObjInPage.value.type==Branch.BranchType.LOCAL
                    showCheckoutBranchDialog.value = true
                }
                //merge into current 实际上是和HEAD进行合并，产生一个新的提交
                //x 对当前分支禁用这个选项，只有其他分支才能用
                BottomSheetItem(sheetState, showBottomSheet, stringResource(R.string.merge),
//                        textDesc = repoCurrentActiveBranchOrShortDetachedHashForShown.value+(if(curRepoIsDetached.value) "[Detached]" else ""),
//                    textDesc = replaceStringResList(stringResource(id = R.string.merge_branch1_into_branch2), listOf(getStrShorterThanLimitLength(curObjInPage.value.shortName), (if(curRepoIsDetached.value) "Detached HEAD" else getStrShorterThanLimitLength(repoCurrentActiveBranchOrShortDetachedHashForShown.value)))) ,
                    textDesc = stringResource(R.string.merge_into_current),
                    enabled = curObjInPage.value.shortName != repoCurrentActiveBranchOrShortDetachedHashForShown.value
                ){
                    requireRebase.value = false
                    //弹出确认框，如果确定，执行merge，否则不执行
                    showMergeDialog.value = true

                }

                if(UserUtil.isPro() && (dev_EnableUnTestedFeature || rebaseTestPassed)) {
                    BottomSheetItem(sheetState, showBottomSheet, stringResource(R.string.rebase),
//                        textDesc = repoCurrentActiveBranchOrShortDetachedHashForShown.value+(if(curRepoIsDetached.value) "[Detached]" else ""),
                        textDesc = stringResource(R.string.rebase_current_onto),
                        enabled = curObjInPage.value.shortName != repoCurrentActiveBranchOrShortDetachedHashForShown.value
                    ){
                        requireRebase.value = true
                        //弹出确认框，如果确定，执行merge，否则不执行
                        showMergeDialog.value = true
                    }
                }

                if(curObjInPage.value.type == Branch.BranchType.LOCAL) {
                    BottomSheetItem(sheetState, showBottomSheet, stringResource(R.string.set_upstream),
                        enabled = curObjInPage.value.type == Branch.BranchType.LOCAL
                    ){
                        // onClick()
                        // 弹出确认框，为分支设置上游
                        if(curObjInPage.value.type == Branch.BranchType.REMOTE) {  // remote分支不能设置上游
                            requireShowToast(appContext.getString(R.string.cant_set_upstream_for_remote_branch))
                        }else { //为本地分支设置上游
                            //设置默认值
                            var remoteIdx = 0   //默认选中第一个元素
                            var shortBranch = curObjInPage.value.shortName  //默认分支名为当前选中的分支短名
                            var sameWithLocal = true  //默认勾选和本地分支同名，除非用户的上游不为空且有值

//                            查询旧值，如果有的话
                            val upstream = curObjInPage.value.upstream
                            if(upstream!=null) {
                                MyLog.d(TAG,"set upstream menu item #onClick(): upstream is not null, old remote in config is: ${upstream.remote}, old branch in config is:${upstream.branchRefsHeadsFullRefSpec}")

                                val oldRemote = upstream.remote
                                //查询之前的remote
                                if(oldRemote.isNotBlank()) {
                                    //检查 remote 是否在列表里，万一remote被删或者无效，就依然默认选中第一个remote
                                    for((idx, value) in upstreamRemoteOptionsList.value.toList().withIndex()) {
                                        if(value == oldRemote) {
                                            MyLog.d(TAG,"set upstream menu item #onClick(): found old remote: ${value}, idx in remote list is: $idx")
                                            remoteIdx = idx
                                            break
                                        }
                                    }
                                }
                                val oldUpstreamShortBranchNameNoPrefix = upstream.remoteBranchShortRefSpecNoPrefix
                                //查询之前的分支
                                if(!oldUpstreamShortBranchNameNoPrefix.isNullOrBlank()) {
                                    MyLog.d(TAG,"set upstream menu item #onClick(): found old branch full refspec: ${upstream.branchRefsHeadsFullRefSpec}, short refspec: $oldUpstreamShortBranchNameNoPrefix")
                                    shortBranch = oldUpstreamShortBranchNameNoPrefix
                                    sameWithLocal = false  //有有效的分支值，就不勾选 same with local 了
                                }

                            }

                            upstreamSelectedRemote.intValue = remoteIdx
                            upstreamBranchShortRefSpec.value = shortBranch
                            upstreamBranchSameWithLocal.value = sameWithLocal

                            MyLog.d(TAG, "set upstream menu item #onClick(): after read old settings, finally, default select remote idx is:${upstreamSelectedRemote.intValue}, branch name is:${upstreamBranchShortRefSpec.value}, check 'same with local branch` is:${upstreamBranchSameWithLocal.value}")

                            //显示弹窗
                            showSetUpstreamForLocalBranchDialog.value = true
                        }
                    }

                    if(proFeatureEnabled(branchListPagePublishBranchTestPassed)) {
                        BottomSheetItem(sheetState, showBottomSheet, stringResource(R.string.publish),
                            enabled = curObjInPage.value.type == Branch.BranchType.LOCAL
                        ){
                            forcePublish.value=false
                            showPublishDialog.value = true
                        }
                    }

                    BottomSheetItem(sheetState, showBottomSheet, stringResource(R.string.diff_to_upstream),
                        enabled = curObjInPage.value.type == Branch.BranchType.LOCAL
                    ){
                        val curObj = curObjInPage.value

                        if(!curObj.isUpstreamValid()) {  // invalid upstream
                            Msg.requireShowLongDuration(appContext.getString(R.string.upstream_not_set_or_not_published))
                        }else {
                            val upOid = curObj.upstream?.remoteOid ?: ""
                            if(upOid.isBlank()) {  // invalid upstream oid
                                Msg.requireShowLongDuration(appContext.getString(R.string.upstream_oid_is_invalid))
                            }else {
                                val commit1 = curObj.oidStr
                                val commit2 = upOid

                                if(commit1 == commit2) {  // local and upstream are the same, no need compare
                                    Msg.requireShow(appContext.getString(R.string.both_are_the_same))
                                }else {   // necessary things are ready and local vs upstream ain't same, then , should go to diff page
                                    val descKey = Cache.setThenReturnKey(appContext.getString(R.string.compare_to_upstream))
                                    val commitForQueryParents = Cons.allZeroOidStr

                                    // url 参数： 页面导航id/repoId/treeoid1/treeoid2/desckey
                                    navController.navigate(
                                        //注意是 parentTreeOid to thisObj.treeOid，也就是 旧提交to新提交，相当于 git diff abc...def，比较的是旧版到新版，新增或删除或修改了什么，反过来的话，新增删除之类的也就反了
                                        "${Cons.nav_TreeToTreeChangeListScreen}/${curRepo.value.id}/$commit1/$commit2/$descKey/$commitForQueryParents"
                                    )
                                }

                            }
                        }
                    }

                    BottomSheetItem(sheetState, showBottomSheet, stringResource(R.string.go_upstream),
                        enabled = curObjInPage.value.type == Branch.BranchType.LOCAL
                    ){
                        val curObj = curObjInPage.value

                        doJobThenOffLoading(loadingOn, loadingOff, appContext.getString(R.string.loading)) {
                            if(!curObj.isUpstreamValid()) {
                                Msg.requireShowLongDuration(appContext.getString(R.string.upstream_not_set_or_not_published))
                            }else {
                                val upstreamFullName = curObj.getUpstreamFullName()
                                val actuallyList = getActuallyList()
                                val actuallyListState = getActuallyListState()
                                val targetIdx = actuallyList.toList().indexOfFirst { it.fullName ==  upstreamFullName }
                                if(targetIdx == -1) {  //未在当前实际展示的列表找到条目，尝试在源列表查找
                                    //如果开启过滤模式且未在过滤列表找到，尝试在源列表查找
                                    if(filterModeOn.value) {
                                        //从源列表找
                                        val indexInOriginList = list.value.toList().indexOfFirst { it.fullName ==  upstreamFullName }

                                        if(indexInOriginList != -1){  // found in origin list
                                            filterModeOn.value = false  //关闭过滤模式
                                            showBottomSheet.value = false  //关闭菜单

                                            //定位条目
                                            UIHelper.scrollToItem(scope, listState, indexInOriginList)
                                            requireBlinkIdx.intValue = indexInOriginList  //设置条目闪烁以便用户发现
                                        }else {
                                            Msg.requireShow(appContext.getString(R.string.upstream_not_found))
                                        }
                                    }else {  //非filter mode且没找到，说明源列表根本没有，直接提示没找到
                                        Msg.requireShow(appContext.getString(R.string.upstream_not_found))
                                    }

                                }else {  //在当前实际展示的列表（filter或源列表）找到了，直接跳转
                                    UIHelper.scrollToItem(scope, actuallyListState, targetIdx)
                                    requireBlinkIdx.intValue = targetIdx  //设置条目闪烁以便用户发现
                                }
                            }
                        }
                    }

                }

                if(proFeatureEnabled(resetByHashTestPassed)) {
                    BottomSheetItem(sheetState, showBottomSheet, stringResource(R.string.reset),
                    ){
    //                    resetDialogShortOid.value = curObjInPage.value.shortOidStr
                        resetDialogOid.value = curObjInPage.value.oidStr
    //                    acceptHardReset.value = false
                        showResetDialog.value = true
                    }
                }

                BottomSheetItem(sheetState, showBottomSheet, stringResource(R.string.details)){
                    val sb = StringBuilder()
                    val it = curObjInPage.value
                    sb.append(appContext.getString(R.string.name)).append(": ").append(it.shortName).appendLine().appendLine()
                    sb.append(appContext.getString(R.string.full_name)).append(": ").append(it.fullName).appendLine().appendLine()
                    sb.append(appContext.getString(R.string.last_commit)).append(": ").append(it.shortOidStr).appendLine().appendLine()
                    sb.append(appContext.getString(R.string.last_commit_full_oid)).append(": ").append(it.oidStr).appendLine().appendLine()
                    sb.append(appContext.getString(R.string.type)).append(": ").append(it.getTypeString()).appendLine().appendLine()
                    if(it.type==Branch.BranchType.LOCAL) {
                        sb.append(appContext.getString(R.string.upstream)).append(": ").append(it.getUpstreamShortName()).appendLine().appendLine()
                        if(it.isUpstreamValid()) {
                            sb.append(appContext.getString(R.string.upstream_full_name)).append(": ").append(it.getUpstreamFullName()).appendLine().appendLine()
                            sb.append(appContext.getString(R.string.status)).append(": ").append(it.getAheadBehind()).appendLine().appendLine()
                        }
                    }

                    if(it.isSymbolic) {
                        sb.append(appContext.getString(R.string.symbolic_target)).append(": ").append(it.symbolicTargetShortName).appendLine().appendLine()
                        sb.append(appContext.getString(R.string.symbolic_target_full_name)).append(": ").append(it.symbolicTargetFullName).appendLine().appendLine()
                    }

                    sb.append(appContext.getString(R.string.other)).append(": ").append(it.getOther()).appendLine().appendLine()

                    detailsString.value = sb.toString()

                    showDetailsDialog.value = true
                }

                if(curObjInPage.value.type == Branch.BranchType.LOCAL && proFeatureEnabled(branchRenameTestPassed)) {
                    BottomSheetItem(sheetState, showBottomSheet, stringResource(R.string.rename)) {
                        nameForRenameDialog.value = curObjInPage.value.shortName
                        forceForRenameDialog.value= false
                        showRenameDialog.value = true
                    }
                }

                BottomSheetItem(sheetState, showBottomSheet, stringResource(R.string.delete), textColor = MyStyleKt.TextColor.danger,
                    //只能删除非当前分支，不过如果是detached，所有分支都能删。这个不做检测了，因为就算界面出了问题，用户针对当前分支执行了删除操作，libgit2也会抛异常，所以还是不会被执行。
                    enabled = curObjInPage.value.shortName != repoCurrentActiveBranchOrShortDetachedHashForShown.value
                ){
                    // onClick()
//                        该写删除了，别忘了删除上游的逻辑和提示删除远程分支需要联网
                    // 弹出确认框，删除分支
                    if(curObjInPage.value.type == Branch.BranchType.REMOTE) {
                        curRequireDelRemoteNameIsAmbiguous.value = curObjInPage.value.isRemoteNameAmbiguous()
                        userSpecifyRemoteName.value=""
                        pushCheckBoxForRemoteBranchDelDialog.value = false
                        showRemoteBranchDelDialog.value = true
                    }else {
                        //如果没上游或上游无效(例如没发布或者配置文件中没设置相关字段)，禁用删除上游勾选框，否则启用
//                        isUpstreamValidForDelLocalBranch.value = curObjInPage.value.isUpstreamValid()
                        delUpstreamToo.value = false
                        delUpstreamPush.value = false
                        showLocalBranchDelDialog.value = true

                    }
                }

            }
        }


        //根据关键字过滤条目
        val k = filterKeyword.value.text.lowercase()  //关键字
        val enableFilter = filterModeOn.value && k.isNotEmpty()
        val list = if(enableFilter){
            val ret = list.value.filter {
                it.fullName.lowercase().contains(k)
                        || it.oidStr.lowercase().contains(k)
                        || it.symbolicTargetFullName.lowercase().contains(k)
                        || it.getUpstreamShortName().lowercase().contains(k)
                        || it.getUpstreamFullName().lowercase().contains(k)
                        || it.getOther().lowercase().contains(k)
                        || it.getTypeString().lowercase().contains(k)
                        || it.getAheadBehind().lowercase().contains(k)
            }

            //更新filterList
            filterList.value.clear()
            filterList.value.addAll(ret)

            ret
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
            requirePaddingAtBottom = true,
            forEachCb = {},
        ){idx, it->
            //长按会更新curObjInPage为被长按的条目
            BranchItem(showBottomSheet, curObjInPage, idx, it, requireBlinkIdx) {  //onClick
                //点击条目跳转到分支的提交历史记录页面
                val fullOidKey:String = Cache.setThenReturnKey(it.oidStr)
                val shortBranchNameKey:String = Cache.setThenReturnKey(it.shortName)
                val useFullOid = "1"
                val isCurrent = if(it.isCurrent) "1" else "0"
                navController.navigate(Cons.nav_CommitListScreen + "/" + repoId +"/" +useFullOid + "/" + fullOidKey +"/" +shortBranchNameKey +"/" +isCurrent)
            }

            HorizontalDivider()
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
        try {
            doJobThenOffLoading(
                loadingOn = loadingOn,
                loadingOff = loadingOff,
                loadingText = appContext.getString(R.string.loading),
            ) {
                list.value.clear()  //先清一下list，然后可能添加也可能不添加

                if(!repoId.isNullOrBlank()) {
                    val repoDb = AppModel.singleInstanceHolder.dbContainer.repoRepository
                    val repoFromDb = repoDb.getById(repoId)
                    if(repoFromDb!=null) {
                        curRepo.value = repoFromDb
                        Repository.open(repoFromDb.fullSavePath).use {repo ->
                            curRepoIsDetached.value = repo.headDetached()
                            //更新用来显示的值
                            repoCurrentActiveBranchOrShortDetachedHashForShown.value = if(curRepoIsDetached.value) repoFromDb.lastCommitHash else repoFromDb.branch;
                            if(!curRepoIsDetached.value) { //分支长引用名，只有在非detached时，才用到这个变量
                                repoCurrentActiveBranchFullRefForDoAct.value = Libgit2Helper.resolveHEAD(repo)?.name()?:""
                            }
                            //更新实际用来执行操作的oid
                            repoCurrentActiveBranchOrDetachedHeadFullHashForDoAct.value = repo.head()?.id().toString()
                            val listAllBranch = Libgit2Helper.getBranchList(repo)
                            list.value.addAll(listAllBranch)
//                            list.requireRefreshView()

                            //更新remote列表，设置upstream时用
                            val remoteList = Libgit2Helper.getRemoteList(repo)
                            upstreamRemoteOptionsList.value.clear()
                            upstreamRemoteOptionsList.value.addAll(remoteList)

//                            upstreamRemoteOptionsList.requireRefreshView()

                        }
                    }
                }
            }
        } catch (e: Exception) {
            MyLog.e(TAG, "#LaunchedEffect() err:"+e.stackTraceToString())
//            ("LaunchedEffect: job cancelled")
        }
    }

    //compose被销毁时执行的副作用。准确来说，这个组件会在它自己被销毁时执行某些操作。放到某组件根目录下，就间接实现了某个组件被销毁时执行某些操作
//    DisposableEffect(Unit) {
////        ("DisposableEffect: entered main")
//        onDispose {
//
//        }
//    }
}
