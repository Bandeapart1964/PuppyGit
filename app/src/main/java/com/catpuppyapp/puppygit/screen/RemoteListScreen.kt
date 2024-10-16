package com.catpuppyapp.puppygit.screen

import android.annotation.SuppressLint
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.FilterAlt
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.VerticalAlignTop
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
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.catpuppyapp.puppygit.compose.BottomSheet
import com.catpuppyapp.puppygit.compose.BottomSheetItem
import com.catpuppyapp.puppygit.compose.ConfirmDialog
import com.catpuppyapp.puppygit.compose.CopyableDialog
import com.catpuppyapp.puppygit.compose.CreateRemoteDialog
import com.catpuppyapp.puppygit.compose.FilterTextField
import com.catpuppyapp.puppygit.compose.LoadingText
import com.catpuppyapp.puppygit.compose.LongPressAbleIconBtn
import com.catpuppyapp.puppygit.compose.MyLazyColumn
import com.catpuppyapp.puppygit.compose.RemoteItem
import com.catpuppyapp.puppygit.compose.SetBranchForRemoteDialog
import com.catpuppyapp.puppygit.compose.SmallFab
import com.catpuppyapp.puppygit.compose.UnLinkCredentialAndRemoteDialogForRemoteListPage
import com.catpuppyapp.puppygit.constants.Cons
import com.catpuppyapp.puppygit.constants.SpecialCredential
import com.catpuppyapp.puppygit.data.entity.RemoteEntity
import com.catpuppyapp.puppygit.data.entity.RepoEntity
import com.catpuppyapp.puppygit.dev.createRemoteTestPassed
import com.catpuppyapp.puppygit.dev.dev_EnableUnTestedFeature
import com.catpuppyapp.puppygit.dev.proFeatureEnabled
import com.catpuppyapp.puppygit.dev.shallowAndSingleBranchTestPassed
import com.catpuppyapp.puppygit.dto.RemoteDto
import com.catpuppyapp.puppygit.play.pro.R
import com.catpuppyapp.puppygit.style.MyStyleKt
import com.catpuppyapp.puppygit.user.UserUtil
import com.catpuppyapp.puppygit.utils.AppModel
import com.catpuppyapp.puppygit.utils.Libgit2Helper
import com.catpuppyapp.puppygit.utils.Msg
import com.catpuppyapp.puppygit.utils.MyLog
import com.catpuppyapp.puppygit.utils.UIHelper
import com.catpuppyapp.puppygit.utils.changeStateTriggerRefreshPage
import com.catpuppyapp.puppygit.utils.createAndInsertError
import com.catpuppyapp.puppygit.utils.doJobThenOffLoading
import com.catpuppyapp.puppygit.utils.getSecFromTime
import com.catpuppyapp.puppygit.utils.showErrAndSaveLog
import com.catpuppyapp.puppygit.utils.state.StateUtil
import com.catpuppyapp.puppygit.utils.state.mutableCustomStateListOf
import com.catpuppyapp.puppygit.utils.state.mutableCustomStateOf
import com.github.git24j.core.Remote
import com.github.git24j.core.Repository
import java.net.URI

private val TAG = "RemoteListScreen"
private val stateKeyTag = "RemoteListScreen"

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun RemoteListScreen(
//    context: Context,
//    navController: NavHostController,
//    scope: CoroutineScope,
//    haptic:HapticFeedback,
//    homeTopBarScrollBehavior: TopAppBarScrollBehavior,
    repoId:String,
    naviUp: () -> Boolean,
) {
    val homeTopBarScrollBehavior = AppModel.singleInstanceHolder.homeTopBarScrollBehavior
    val appContext = AppModel.singleInstanceHolder.appContext
    val navController = AppModel.singleInstanceHolder.navController
    val dbContainer = AppModel.singleInstanceHolder.dbContainer
    val scope = rememberCoroutineScope()

    //获取假数据
//    val list = MockData.getErrorList(repoId,1,100);
    val list = mutableCustomStateListOf(
        keyTag = stateKeyTag,
        keyName = "list",
        initValue = listOf<RemoteDto>()
    )

//
//    SideEffect {
//        Msg.msgNotifyHost()
//    }

    //这个页面的滚动状态不用记住，每次点开重置也无所谓
    val lazyListState = rememberLazyListState()
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = MyStyleKt.BottomSheet.skipPartiallyExpanded)
    val showBottomSheet = rememberSaveable { mutableStateOf(false)}
//    val curObjInState = rememberSaveable{ mutableStateOf(ErrorEntity()) }
    val curRepo = mutableCustomStateOf(keyTag = stateKeyTag, keyName = "curRepo", initValue = RepoEntity(id="") )
    val curObjInState = mutableCustomStateOf(keyTag = stateKeyTag, keyName = "curObjInState", initValue = RemoteDto())
//    val showClearAllConfirmDialog = StateUtil.getRememberSaveableState(initValue = false)
//    val userIsPro = UserInfo.isPro()


    val needRefresh = rememberSaveable { mutableStateOf("")}
    val showFetchAllDialog = rememberSaveable { mutableStateOf(false)}

    val showSetUrlDialog = rememberSaveable { mutableStateOf(false)}
    val isPushUrl = rememberSaveable { mutableStateOf(false)}
    val urlTextForSetUrlDialog = rememberSaveable { mutableStateOf("")}
    val oldUrlTextForSetUrlDialog = rememberSaveable { mutableStateOf("")}
    val urlErrMsg = rememberSaveable { mutableStateOf( "")}

    val showUnlinkCredentialDialog = rememberSaveable { mutableStateOf(false)}
    val showSetBranchDialog = rememberSaveable { mutableStateOf( false)}

    val defaultLoadingText = stringResource(R.string.loading)
    val isLoading = rememberSaveable { mutableStateOf( false)}
    val loadingText = rememberSaveable { mutableStateOf(defaultLoadingText)}
    val loadingOn = {msg:String ->
        loadingText.value=msg
        isLoading.value=true
    }
    val loadingOff = {
        isLoading.value=false
        loadingText.value = defaultLoadingText
    }

    val showCreateRemoteDialog = rememberSaveable { mutableStateOf(false)}
    val remoteNameForCreate = rememberSaveable { mutableStateOf( "")}
    val remoteUrlForCreate = rememberSaveable { mutableStateOf("")}

    if(showCreateRemoteDialog.value) {
        CreateRemoteDialog(
            show = showCreateRemoteDialog,
            curRepo = curRepo.value,
            remoteName = remoteNameForCreate,
            remoteUrl = remoteUrlForCreate,
            loadingOn = loadingOn,
            loadingOff = loadingOff,
            onErr = { e->
                val actionDesc = "create remote"
                val errMsgPrefix = "$actionDesc err: remoteName='${remoteNameForCreate.value}', remoteUrl=${remoteUrlForCreate.value}, err="
                Msg.requireShowLongDuration(e.localizedMessage ?: errMsgPrefix)
                createAndInsertError(curRepo.value.id, errMsgPrefix + e.localizedMessage)
                MyLog.e(TAG, "#CreateRemoteDialog: $errMsgPrefix${e.stackTraceToString()}")
            },
            onFinally = {
                changeStateTriggerRefreshPage(needRefresh)
            }
        )
    }

    val showDelRemoteDialog = rememberSaveable { mutableStateOf(false)}
    if(showDelRemoteDialog.value) {
        val remoteWillDel = curObjInState.value
        val remoteNameWillDel = remoteWillDel.remoteName
        ConfirmDialog(
            title = stringResource(id = R.string.delete) +" '$remoteNameWillDel'",
            text = stringResource(id = R.string.are_you_sure),
            okTextColor = MyStyleKt.TextColor.danger,
            onCancel = { showDelRemoteDialog.value = false }
        ) {
            showDelRemoteDialog.value = false

            doJobThenOffLoading(loadingOn, loadingOff, appContext.getString(R.string.deleting)) {
                try {
                    Repository.open(curRepo.value.fullSavePath).use { repo->
                        //删git仓库中的remote
                        val ret = Libgit2Helper.delRemote(repo, remoteNameWillDel)

                        val noExist = ret.msg.startsWith("remote") && ret.msg.endsWith("does not exist")
                        //如果从git仓库删除remote成功或者错误信息是 “remote 'xxx' does not exist”，则删除db中的remote
                        if(ret.success() || noExist) {
                            //删db中的remote
                            val remoteDb = AppModel.singleInstanceHolder.dbContainer.remoteRepository
                            remoteDb.delete(RemoteEntity(id=remoteWillDel.remoteId))  // delete其实是根据id删除的，所以只设id即可
                        }else{
                            throw ret.exception ?: Exception(ret.msg)
                        }

                        if(noExist){  //remote在git仓库不存在，但同时悄悄删除了db中的remote
                            Msg.requireShowLongDuration(ret.msg)
                        }else{ //成功
                            Msg.requireShow(appContext.getString(R.string.success))
                        }
                    }

                }catch (e:Exception) {
                    val actionDesc = "delete remote"
                    val errMsgPrefix = "$actionDesc err: remoteName='$remoteNameWillDel', err="
                    Msg.requireShowLongDuration(e.localizedMessage ?: errMsgPrefix)
                    createAndInsertError(curRepo.value.id, errMsgPrefix + e.localizedMessage)
                    MyLog.e(TAG, "$errMsgPrefix${e.stackTraceToString()}")
                }finally {
                    changeStateTriggerRefreshPage(needRefresh)
                }
            }
        }
    }

    val showViewDialog = rememberSaveable { mutableStateOf(false)}
    val viewDialogText = rememberSaveable { mutableStateOf("")}
    val clipboardManager = LocalClipboardManager.current
    if(showViewDialog.value) {
        CopyableDialog(
            title = stringResource(id = R.string.remote_info),
            text = viewDialogText.value,
            onCancel = {
                showViewDialog.value=false
            }
        ) { //复制到剪贴板
            showViewDialog.value=false
            clipboardManager.setText(AnnotatedString(viewDialogText.value))
            Msg.requireShow(appContext.getString(R.string.copied))

        }
    }

    if(showSetBranchDialog.value) {
        SetBranchForRemoteDialog(
            curRepo = curRepo.value,
            remoteName = curObjInState.value.remoteName,
            isAllInitValue = curObjInState.value.branchMode == Cons.dbRemote_Fetch_BranchMode_All,
            onCancel = {showSetBranchDialog.value=false},
        ) { remoteName:String, isAll: Boolean, branchCsvStr: String ->
            showSetBranchDialog.value=false

            doJobThenOffLoading onOk@{
                Repository.open(curRepo.value.fullSavePath).use { repo->
                    val config = Libgit2Helper.getRepoConfigForWrite(repo)
                    val ret = if(isAll) {
                        Libgit2Helper.setRemoteFetchRefSpecToGitConfig(
                            config = config,
                            fetch_BranchMode = Cons.dbRemote_Fetch_BranchMode_All,
                            remote = remoteName,
                            branchOrBranches = Cons.gitFetchAllBranchSign,
                            branchListSeparator = Cons.stringListSeparator,
                            appContext = appContext
                            )
                    }else {
                        Libgit2Helper.setRemoteFetchRefSpecToGitConfig(
                            config=config,
                            fetch_BranchMode = Cons.dbRemote_Fetch_BranchMode_CustomBranches,
                            remote = remoteName,
                            branchOrBranches = branchCsvStr,
                            branchListSeparator = Cons.stringListSeparator,
                            appContext = appContext
                        )

                    }

                    if(ret.hasError()) {
                        Msg.requireShowLongDuration(ret.msg)
                    }else{
                        Msg.requireShow(appContext.getString(R.string.saved))
                    }

                    changeStateTriggerRefreshPage(needRefresh)

                }

            }
        }
    }

    if(showFetchAllDialog.value) {
        ConfirmDialog(
            title = stringResource(id = R.string.fetch_all),
            text = stringResource(id = R.string.fetch_all_are_u_sure),
            onCancel = { showFetchAllDialog.value = false }
        ) {
            showFetchAllDialog.value=false
            doJobThenOffLoading(loadingOn, loadingOff, appContext.getString(R.string.fetching_all)) {
                try {
                    if(list.value.isNotEmpty()) {  //remote列表如果是空就不用fetch all了
                        //remote名和凭据组合的列表
                        val remoteCredentialList = Libgit2Helper.genRemoteCredentialPairList(list.value, AppModel.singleInstanceHolder.dbContainer.credentialRepository,
                            requireFetchCredential = true, requirePushCredential = false)

                        Repository.open(curRepo.value.fullSavePath).use { repo ->
                            //fetch all
                            Libgit2Helper.fetchRemoteListForRepo(repo, remoteCredentialList, curRepo.value)
                            //显示成功通知
                            Msg.requireShow(appContext.getString(R.string.fetch_all_success))
                        }
                    }else {  // remotes列表为空，无需执行操作
                        Msg.requireShowLongDuration(appContext.getString(R.string.err_remote_list_is_empty))
                    }

                }catch (e:Exception){
                    val errMsg = "fetch all err:"+e.localizedMessage
                    Msg.requireShowLongDuration(errMsg)
                    createAndInsertError(curRepo.value.id, errMsg)

                    MyLog.e(TAG, "fetch all err:"+e.stackTraceToString())

                }finally {
                    changeStateTriggerRefreshPage(needRefresh)
                }


            }

        }
    }
    if(showSetUrlDialog.value) {
        ConfirmDialog(
            okBtnEnabled = isPushUrl.value || urlTextForSetUrlDialog.value.isNotEmpty(),
            title = if(isPushUrl.value) stringResource(id = R.string.set_push_url) else stringResource(id = R.string.set_url),
            okBtnText = stringResource(id = R.string.save),
            cancelBtnText = stringResource(id = R.string.cancel),
            requireShowTextCompose = true,
            textCompose = {
                Column {
                    Row {
                        Text(text = stringResource(id = R.string.text_for)+": ")
                        Text(text = curObjInState.value.remoteName,
                            fontWeight = FontWeight.ExtraBold,
                            overflow = TextOverflow.Visible
                        )
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    TextField(
                        modifier = Modifier.fillMaxWidth(),

                        value = urlTextForSetUrlDialog.value,
                        singleLine = true,
                        isError = urlErrMsg.value.isNotEmpty(),
                        supportingText = {
                            if (urlErrMsg.value.isNotEmpty()) {
                                Text(
                                    modifier = Modifier.fillMaxWidth(),
                                    text = urlErrMsg.value,
                                    color = MaterialTheme.colorScheme.error
                                )
                            }
                        },
                        trailingIcon = {
                            if (urlErrMsg.value.isNotEmpty()) {
                                Icon(imageVector=Icons.Filled.Error,
                                    contentDescription=urlErrMsg.value,
                                    tint = MaterialTheme.colorScheme.error)
                            }
                        },
                        onValueChange = {
                            urlTextForSetUrlDialog.value = it
                            urlErrMsg.value="" //值一改变就清空错误信息，再点保存再检测，若有错误会再设置上
                        },
                        label = {
                            Text(if(isPushUrl.value) stringResource(id = R.string.push_url) else stringResource(R.string.url))
                        }
                    )

                    if(isPushUrl.value) {
                        Text(text = stringResource(R.string.leave_it_empty_will_use_url), color=MyStyleKt.TextColor.highlighting_green)
                    }
                }
            },
            onCancel = { showSetUrlDialog.value = false }
        ) onOk@{
            val remoteName = curObjInState.value.remoteName
            val remoteId = curObjInState.value.remoteId
            val newUrl = urlTextForSetUrlDialog.value
            val oldUrl = oldUrlTextForSetUrlDialog.value
            val repoFullPath = curRepo.value.fullSavePath

            //log use
            val setUrlPrefix = if(isPushUrl.value) "set pushUrl" else "set url"

            try {
                //pushUrl可为空，代表使用url；但如果非pushUrl，则强制不可为空
                if(!isPushUrl.value && newUrl.isBlank()) {
                    urlErrMsg.value = appContext.getString(R.string.err_url_is_empty)
                    return@onOk
                }

                //新旧url相同，直接不保存就行了，但也不用报错
                if(newUrl == oldUrl) {
                    Msg.requireShowLongDuration(appContext.getString(R.string.url_not_changed))
                    urlErrMsg.value=""
                    showSetUrlDialog.value=false
                    return@onOk
                }

                //清空pushUrl
                if(isPushUrl.value && newUrl.isBlank()) {
                    //弹窗可关了
                    showSetUrlDialog.value=false

                    //删除配置文件pushUrl字段
                    //设数据库pushUrl字段为空
                    doJobThenOffLoading {
                        try {
                            Repository.open(repoFullPath).use { repo->
                                //删除配置文件中的pushurl字段，若出错且错误不是key不存在，会抛异常
                                Libgit2Helper.deletePushUrl(Libgit2Helper.getRepoConfigForWrite(repo), remoteName)
                            }

                            // 更新数据库
                            val remoteDb = AppModel.singleInstanceHolder.dbContainer.remoteRepository
                            remoteDb.updatePushUrlById(remoteId, "")  //清空pushUrl

                            //操作完成

                            //提示成功
                            Msg.requireShow(appContext.getString(R.string.success))

                        }catch (e:Exception) {
                            val err1 = e.localizedMessage ?: appContext.getString(R.string.unknown_err)
                            Msg.requireShowLongDuration(err1)
                            val errWillSave = "$setUrlPrefix for remote '$remoteName' err (delete pushUrl): $err1"
                            createAndInsertError(repoId, errWillSave)
                        }finally {
                            //刷新页面
                            changeStateTriggerRefreshPage(needRefresh)
                        }

                    }

                    return@onOk
                }

//                执行到这，不管是设置url还是pushUrl，newUrl百分百都不为空或全空白字符串

                //设置非空的url或pushUrl
                //解析url，捎带检测url是否是有效uri
                var newUrlUri:URI? = null
                var newUrlUriStr = ""
                try {
                    //准备url，若url格式有问题，会在URI.create()时报错
                    newUrlUri = URI.create(newUrl)  // 如果url无效，在这会抛异常，数据库和配置文件都不会更新
                    newUrlUriStr = newUrlUri.toString()

                }catch (uriException:Exception) {
                    urlErrMsg.value = appContext.getString(R.string.err_invalid_url)
                    MyLog.e(TAG, "$setUrlPrefix in RemoteList err: parse url err, url=$newUrl, err is: ${uriException.localizedMessage}")
                    return@onOk
                }

                //正常来说应该不会进入这个代码块，这里只是以防万一
                if(newUrlUri==null || newUrlUriStr.isBlank()) {
                    urlErrMsg.value = appContext.getString(R.string.err_invalid_url)+", errcode=17049037"  //后面的errcode是为了方便我定位错误发生在哪部分代码，我一看到errcode，我就知道“啊，是在这发生的错误”
                    return@onOk
                }

                //执行到这里，解析url成功且url不为空

                //关闭弹窗
                showSetUrlDialog.value=false

                //执行到这，url修改了，且不为空，且和旧值不同，需要执行保存
                //执行保存
                doJobThenOffLoading {
                    try {
                        //先更新配置文件，然后再更新db，因为在这里db相当于缓存，而配置文件是正式数据，应确保正式数据成功更新后再更新缓存
                        Repository.open(repoFullPath).use { repo ->
                            if(isPushUrl.value){
                                Remote.setPushurl(repo, remoteName, newUrlUri)
                            }else{
                                Remote.setUrl(repo, remoteName, newUrlUri)
                            }
                        }

                        //如果上面配置文件的代码抛异常，会抛异常，也就不会更新数据库了

                        // 更新数据库
                        val remoteDb = AppModel.singleInstanceHolder.dbContainer.remoteRepository
                        if(isPushUrl.value){
                            remoteDb.updatePushUrlById(remoteId, newUrlUriStr)
                        }else {
                            //这里不用开事务，因为里面就一个dao操作而那个dao已经开了事务，只有多个db操作弄到一起时才有必要在外部开事务
                            //为了和配置文件一致，数据库保存的是 用户输入转成的url再转成的uri再转成的字符串，可能包含需编码的内容时这么转有意义？
                            remoteDb.updateRemoteUrlById(remoteId, newUrlUriStr, requireTransaction=false)
                        }

                        //操作完成

                        //提示成功
                        Msg.requireShow(appContext.getString(R.string.success))

                    }catch (e:Exception) {
                        val err1 = (e.localizedMessage ?: appContext.getString(R.string.unknown_err))
                        Msg.requireShowLongDuration(err1)
                        val errWillSave = "$setUrlPrefix for remote '$remoteName' err: $err1"
                        createAndInsertError(repoId, errWillSave)
                    }finally {
                        //刷新页面
                        changeStateTriggerRefreshPage(needRefresh)
                    }
                }
            }catch (e:Exception) {
                MyLog.e(TAG, "$setUrlPrefix in RemoteList err: remoteName=${remoteName}, newUrl=$newUrl, oldUrl=$oldUrl, err is:\n${e.stackTraceToString()}")
                val err1 = e.localizedMessage ?: appContext.getString(R.string.unknown_err)

                //如果弹窗没关，在弹窗显示错误，如果关了，记到数据库
                if(showSetUrlDialog.value) {  //弹窗开着，直接在弹窗显示错误信息
                    urlErrMsg.value = err1  //设置错误信息
                }else { //弹窗已关闭，出了错误
                    urlErrMsg.value=""  //弹窗已经关了，所以清下错误信息

                    //显示下错误
                    Msg.requireShowLongDuration(err1)

                    //记录错误到数据库
                    val errWillSave = "$setUrlPrefix for remote '$remoteName' err: $err1"
                    doJobThenOffLoading {
                        createAndInsertError(repoId, errWillSave)
                    }

                    //刷新下页面(话说，同一个地方出错应该确保只刷新一次吗？如果需要，可在出错的代码块里获取一个随机id，然后刷新的时候指定那个id，这样就能确保在这个代码块出错时，只请求刷新一次了）
                    changeStateTriggerRefreshPage(needRefresh)
                }
            }
        }
    }

    if(showUnlinkCredentialDialog.value) {
        UnLinkCredentialAndRemoteDialogForRemoteListPage(
            remoteId = curObjInState.value.remoteId,
            remoteName = curObjInState.value.remoteName,
            onCancel = { showUnlinkCredentialDialog.value = false }
        ) {
            showUnlinkCredentialDialog.value=false
            changeStateTriggerRefreshPage(needRefresh)
        }
    }

    val doFetch:suspend (String?,RepoEntity)->Boolean = doFetch@{remoteNameParam:String?, curRepo:RepoEntity ->  //参数的remoteNameParam如果有效就用参数的，否则自己查当前head分支对应的remote
        //x 废弃，逻辑已经改了) 执行isReadyDoSync检查之前要先do fetch，想象一下，如果远程创建了一个分支，正好和本地的关联，但如果我没先fetch，那我检查时就get不到那个远程分支是否存在，然后就会先执行push，但可能远程仓库已经领先本地了，所以push也可能失败，但如果先fetch，就不会有这种问题了
        var fetchSuccessRetVal = false
        try {
            Repository.open(curRepo.fullSavePath).use { repo ->
                //fetch成功返回true，否则返回false
                val remoteName = remoteNameParam
                if (remoteName.isNullOrBlank()) {
                    throw RuntimeException(appContext.getString(R.string.remote_name_is_invalid))
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

                Libgit2Helper.fetchRemoteForRepo(repo, remoteName, credential, curRepo)
            }

            // 更新修改workstatus的时间，只更新时间就行，状态会在查询repo时更新
            val repoDb = AppModel.singleInstanceHolder.dbContainer.repoRepository
            repoDb.updateLastUpdateTime(curRepo.id, getSecFromTime())

            Msg.requireShow(appContext.getString(R.string.success))

            fetchSuccessRetVal = true
        } catch (e: Exception) {
            //记录到日志
            //显示提示
            //保存数据库(给用户看的，消息尽量简单些)
            showErrAndSaveLog(
                TAG,
                "#doFetch() from RemoteList Page err:" + e.stackTraceToString(),
                "fetch err:" + e.localizedMessage,
                Msg.requireShowLongDuration,
                curRepo.id
            )

            fetchSuccessRetVal = false
        }

        return@doFetch fetchSuccessRetVal
    }

    val filterKeyword = mutableCustomStateOf(
        keyTag = stateKeyTag,
        keyName = "filterKeyword",
        initValue = TextFieldValue("")
    )
    val filterModeOn = rememberSaveable { mutableStateOf(false)}


    // 向下滚动监听，开始
    val scrollingDown = remember { mutableStateOf(false) }

    val filterListState = mutableCustomStateOf(
        keyTag = stateKeyTag,
        keyName = "filterListState",
        LazyListState(0,0)
    )
    val enableFilterState = rememberSaveable { mutableStateOf(false)}
//    val firstVisible = remember { derivedStateOf { if(enableFilterState.value) filterListState.value.firstVisibleItemIndex else lazyListState.firstVisibleItemIndex } }
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
                lazyListState.firstVisibleItemIndex
            }
            val scrolldown = nowAt > lastAt.intValue
            lastAt.intValue = nowAt
            scrolldown
        }
    }.value
    // 向下滚动监听，结束

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
                        val repoName = curRepo.value.repoName
                        Column (modifier = Modifier.combinedClickable(onDoubleClick = { UIHelper.scrollToItem(scope, lazyListState, 0) }
                        ) {
                            //onClick
                            Msg.requireShow(repoName)
                        }){
                            Text(
                                text= stringResource(R.string.remotes),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )

                            //"[Index]|Merging" or "[Index]"
                            Text(text = "[$repoName]",
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                fontSize = MyStyleKt.Title.secondLineFontSize
                            )
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
                           //刷新
                            changeStateTriggerRefreshPage(needRefresh)
                        }

                        LongPressAbleIconBtn(
                            tooltipText = stringResource(R.string.fetch_all),
                            icon =  Icons.Filled.CloudDownload,
                            iconContentDesc = stringResource(R.string.fetch_all),

                        ) {
                           //刷新
                            showFetchAllDialog.value = true
                        }

                        // add more remotes is a pro feature
                        if(proFeatureEnabled(createRemoteTestPassed)) {
                            LongPressAbleIconBtn(
                                tooltipText = stringResource(R.string.create),
                                icon =  Icons.Filled.Add,
                                iconContentDesc = stringResource(R.string.create),
                            ) {
                                showCreateRemoteDialog.value = true
                            }
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
                        UIHelper.scrollToItem(scope, lazyListState, 0)
                    }
                }
            }
        }
    ) { contentPadding ->

        if(showBottomSheet.value) {
            BottomSheet(showBottomSheet, sheetState, curObjInState.value.remoteName) {
                BottomSheetItem(sheetState=sheetState, showBottomSheet=showBottomSheet, text= stringResource(R.string.fetch)){
                    //e.g. "fetching origin..."
                    val fetchingxxxLoadingText = appContext.getString(R.string.fetching_no_dots)+" "+curObjInState.value.remoteName+"..."
                    //执行fetch
                    doJobThenOffLoading(loadingOn, loadingOff, fetchingxxxLoadingText) {
                        doFetch(curObjInState.value.remoteName, curRepo.value)
                        //刷新页面
                        changeStateTriggerRefreshPage(needRefresh)
                    }
                }
                if(dev_EnableUnTestedFeature || shallowAndSingleBranchTestPassed) {
                    val isPro = UserUtil.isPro()
                    BottomSheetItem(sheetState=sheetState, showBottomSheet=showBottomSheet, enabled = isPro,
                        text=if(isPro) stringResource(R.string.set_branch_mode) else stringResource(R.string.set_branch_mode_pro_only)
                    ){
                        showSetBranchDialog.value=true
                    }
                }
                BottomSheetItem(sheetState=sheetState, showBottomSheet=showBottomSheet, text=stringResource(R.string.set_url)){
                    //设置remote url
                    //设置回显上次的url
                    val oldUrl = curObjInState.value.remoteUrl
                    urlTextForSetUrlDialog.value = oldUrl  //用来存储用户输入的url的状态变量
                    oldUrlTextForSetUrlDialog.value = oldUrl //保存旧值，在这设置后就不会更新了，这个变量用来在保存前检查新旧url是否相同，若相同就不更新了
                    isPushUrl.value = false

                    //清空错误信息
                    urlErrMsg.value=""
                    //显示弹窗
                    showSetUrlDialog.value=true
                }

                //set push url和link credential为pro功能，正好这两个选项挨在一起，所以放一个if判断里了
                if(UserUtil.isPro()) {
                    BottomSheetItem(sheetState=sheetState, showBottomSheet=showBottomSheet, text=stringResource(R.string.set_push_url)){
                        //设置pushurl
                        //设置回显上次的url
                        val oldUrl = if(curObjInState.value.pushUrlTrackFetchUrl) "" else curObjInState.value.pushUrl
                        urlTextForSetUrlDialog.value = oldUrl  //用来存储用户输入的url的状态变量
                        oldUrlTextForSetUrlDialog.value = oldUrl //保存旧值，在这设置后就不会更新了，这个变量用来在保存前检查新旧url是否相同，若相同就不更新了
                        isPushUrl.value = true

                        //清空错误信息
                        urlErrMsg.value=""
                        //显示弹窗
                        showSetUrlDialog.value=true
                    }

                    BottomSheetItem(sheetState=sheetState, showBottomSheet=showBottomSheet, text=stringResource(R.string.link_credential)){
                        //跳转到凭据列表页面
                        //curObjInState当初应命名为“LongPressedItem”之类的名字，现在这我经常分不清哪个状态变量存储长按条目
                        navController.navigate(Cons.nav_CredentialManagerScreen+"/${curObjInState.value.remoteId}")
                    }
                }

                BottomSheetItem(sheetState=sheetState, showBottomSheet=showBottomSheet, text=stringResource(R.string.unlink_credential)){
                    //解除关联凭据
                    showUnlinkCredentialDialog.value=true
                }

                //添加remote和删除remote配套，都是pro feature，不能只开放其中一个，不然会有问题，想像一下，你就一个remote，而你删了它，但没法创建，是不是很不合理？所以删除和添加应该配套，一个是pro feature另一个也必须是
                if(proFeatureEnabled(createRemoteTestPassed)) {
                    BottomSheetItem(sheetState=sheetState, showBottomSheet=showBottomSheet, text=stringResource(R.string.delete), textColor = MyStyleKt.TextColor.danger){
                        showDelRemoteDialog.value=true
                    }
                }

            }
        }

        if(isLoading.value) {
            LoadingText(contentPadding = contentPadding, text = loadingText.value)
        }else {
            //根据关键字过滤条目
            val k = filterKeyword.value.text.lowercase()  //关键字
            val enableFilter = filterModeOn.value && k.isNotEmpty()
            val list = if(enableFilter){
                list.value.filter {
                    it.remoteName.lowercase().contains(k)
                            || it.remoteUrl.lowercase().contains(k)
                            || it.pushUrl.lowercase().contains(k)
                            || it.credentialName?.lowercase()?.contains(k) == true
                            || it.pushCredentialName?.lowercase()?.contains(k) == true
                            || it.branchListForFetch.toString().lowercase().contains(k)
                }
            }else {
                list.value
            }
            val listState = if(enableFilter) rememberLazyListState() else lazyListState
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
                requirePaddingAtBottom = true
            ) {idx,it->
                //在这个组件里更新了 state curObj，所以长按后直接用curObj就能获取到当前对象了
                RemoteItem(showBottomSheet,curObjInState,idx,it){ //onClick
                    //生成要显示的字符串
                    val sb = StringBuilder()
                    sb.append(appContext.getString(R.string.name)+": "+it.remoteName)
                    sb.appendLine()
                    sb.appendLine()
                    sb.append(appContext.getString(R.string.url)+": "+it.remoteUrl)
                    sb.appendLine()
                    sb.appendLine()

//                    sb.append(appContext.getString(R.string.push_url)+": "+(it.pushUrl.ifEmpty { it.remoteUrl }))  // no more check pushUrl empty need after 20241007
                    // after 20241007, if need, pushUrl will replaced to fetch url when querying, so reached here, directly show pushUrl is ok
                    sb.append(appContext.getString(R.string.push_url)+": "+it.pushUrl)

                    sb.appendLine()
                    sb.appendLine()
                    sb.append(appContext.getString(R.string.fetch_credential)+": "+(if(it.credentialId== SpecialCredential.MatchByDomain.credentialId) SpecialCredential.MatchByDomain.name else (it.credentialName?:"")))
                    sb.appendLine()
                    sb.appendLine()
                    sb.append(appContext.getString(R.string.push_credential)+": "+(if(it.pushCredentialId== SpecialCredential.MatchByDomain.credentialId) SpecialCredential.MatchByDomain.name else (it.pushCredentialName?:"")))
                    sb.appendLine()
                    sb.appendLine()
                    sb.append(appContext.getString(R.string.branch_mode)+": "+(if(it.branchMode == Cons.dbRemote_Fetch_BranchMode_All) appContext.getString(R.string.all) else appContext.getString(R.string.custom)))
                    if(it.branchMode != Cons.dbRemote_Fetch_BranchMode_All) {
                        sb.appendLine()
                        sb.appendLine()
                        sb.append((if(it.branchListForFetch.size > 1) appContext.getString(R.string.branches) else appContext.getString(R.string.branch)) +": ${it.branchListForFetch}")
                    }


                    //更新状态，然后显示弹窗
                    viewDialogText.value = sb.toString()
                    showViewDialog.value = true
                }
                HorizontalDivider()
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
        try {
            if(repoId.isNotBlank()) {
                doJobThenOffLoading {
                    list.value.clear()
                    val repoDb = AppModel.singleInstanceHolder.dbContainer.repoRepository
                    val repoFromDb = repoDb.getById(repoId)
                    if(repoFromDb==null) {
                        return@doJobThenOffLoading
                    }
                    curRepo.value = repoFromDb

                    val remoteDb = AppModel.singleInstanceHolder.dbContainer.remoteRepository
                    val listFromDb = remoteDb.getRemoteDtoListByRepoId(repoId)

                    //改成在数据库查询函数里更新了
//                    Repository.open(repoFromDb.fullSavePath).use { repo->
//                        updateRemoteDtoList(repo, listFromDb)
//                    }

                    list.value.addAll(listFromDb)
                }

            }
        } catch (e: Exception) {
            MyLog.e(TAG, "$TAG#LaunchedEffect() err:"+e.stackTraceToString())
        }
    }


}