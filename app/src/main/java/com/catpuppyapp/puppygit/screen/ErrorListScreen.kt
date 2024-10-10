package com.catpuppyapp.puppygit.screen

import android.annotation.SuppressLint
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FilterAlt
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.VerticalAlignTop
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextOverflow
import com.catpuppyapp.puppygit.compose.BottomSheet
import com.catpuppyapp.puppygit.compose.BottomSheetItem
import com.catpuppyapp.puppygit.compose.ConfirmDialog
import com.catpuppyapp.puppygit.compose.CopyableDialog
import com.catpuppyapp.puppygit.compose.ErrorItem
import com.catpuppyapp.puppygit.compose.FilterTextField
import com.catpuppyapp.puppygit.compose.LongPressAbleIconBtn
import com.catpuppyapp.puppygit.compose.MyLazyColumn
import com.catpuppyapp.puppygit.compose.SmallFab
import com.catpuppyapp.puppygit.constants.Cons
import com.catpuppyapp.puppygit.data.entity.ErrorEntity
import com.catpuppyapp.puppygit.data.entity.RepoEntity
import com.catpuppyapp.puppygit.play.pro.R
import com.catpuppyapp.puppygit.style.MyStyleKt
import com.catpuppyapp.puppygit.utils.AppModel
import com.catpuppyapp.puppygit.utils.Msg
import com.catpuppyapp.puppygit.utils.MyLog
import com.catpuppyapp.puppygit.utils.UIHelper
import com.catpuppyapp.puppygit.utils.changeStateTriggerRefreshPage
import com.catpuppyapp.puppygit.utils.doJobThenOffLoading
import com.catpuppyapp.puppygit.utils.state.StateUtil
import com.catpuppyapp.puppygit.utils.state.mutableCustomStateListOf
import com.catpuppyapp.puppygit.utils.state.mutableCustomStateOf


private val TAG = "ErrorListScreen"
private val stateKeyTag = "ErrorListScreen"

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun ErrorListScreen(
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
    val scope = rememberCoroutineScope()

    //获取假数据
//    val list = MockData.getErrorList(repoId,1,100);
    val list = mutableCustomStateListOf(keyTag = stateKeyTag, keyName = "list", initValue = listOf<ErrorEntity>())
    val curRepo = mutableCustomStateOf(keyTag = stateKeyTag, keyName = "curRepo", initValue = RepoEntity(id=""))
//    val sumPage = MockData.getErrorSum(repoId)

    val requireShowToast:(String)->Unit = Msg.requireShow
//    SideEffect {
//        Msg.msgNotifyHost()
//    }


    //这个页面的滚动状态不用记住，每次点开重置也无所谓
    val lazyListState = rememberLazyListState()
    val needRefresh = rememberSaveable { mutableStateOf("")}
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = MyStyleKt.BottomSheet.skipPartiallyExpanded)
    val showBottomSheet = rememberSaveable { mutableStateOf(false)}
//    val curObjInState = rememberSaveable{ mutableStateOf(ErrorEntity()) }
    val curObjInState = mutableCustomStateOf(keyTag = stateKeyTag, keyName = "curObjInState",initValue = ErrorEntity())
    val showClearAllConfirmDialog = rememberSaveable { mutableStateOf(false)}

    val doClearAll={
        doJobThenOffLoading {
            val errDb = AppModel.singleInstanceHolder.dbContainer.errorRepository
            errDb.deleteByRepoId(repoId)

//            list.clear()
            //x 找到原因了：因为之前用的是 mutableList() 而不是 mutableStateListOf()) 奇怪啊，这个不知道为什么这里的刷新不好使，就非得在上面把list clear一下才行不然列表就还是有东西，但点击刷新后东西就没了，不知道什么原因，以后再排查吧
            changeStateTriggerRefreshPage(needRefresh)
        }
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
                        Column {

                            Row (modifier = Modifier.combinedClickable(onDoubleClick = { UIHelper.scrollToItem(scope, lazyListState, 0) }) {}){
                                Text(
                                    text= stringResource(R.string.error),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )

                            }

                            Row(modifier = Modifier.horizontalScroll(rememberScrollState())) {
                                Text(
                                    text= "[${curRepo.value.repoName}]",
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
                            icon = Icons.Filled.Refresh,
                            iconContentDesc = stringResource(R.string.refresh),

                        ) {
                            changeStateTriggerRefreshPage(needRefresh)
                        }

                        LongPressAbleIconBtn(
                            tooltipText = stringResource(R.string.clear_all),
                            icon =  Icons.Filled.Delete,
                            iconContentDesc = stringResource(R.string.clear_all),

                        ) {
                            //点击垃圾箱按钮，显示清空所有对话框，若确认，删除所有错误，然后继续停留在当前页面，但错误列表被清空
                            showClearAllConfirmDialog.value = true
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

        if(showClearAllConfirmDialog.value) {
            ConfirmDialog(title=stringResource(R.string.clear_all),
                text=stringResource(R.string.clear_all_ask_text),
                okTextColor = MyStyleKt.TextColor.danger,
                onCancel = {showClearAllConfirmDialog.value=false},  //关闭弹窗
                onOk = {
                    showClearAllConfirmDialog.value=false  //关闭弹窗
                    doClearAll()  //执行操作
                }
            )
        }

        if(showBottomSheet.value) {
            BottomSheet(showBottomSheet, sheetState, stringResource(R.string.id)+":"+curObjInState.value.id) {
//                BottomSheetItem(sheetState=sheetState, showBottomSheet=showBottomSheet, text=stringResource(R.string.view_msg)){
//                    //弹窗显示错误信息，可复制
//                }
                BottomSheetItem(sheetState=sheetState, showBottomSheet=showBottomSheet, text=stringResource(R.string.delete), textColor = MyStyleKt.TextColor.danger){
                    // onClick()
                    //在这里可以直接用state curObj取到当前选中条目，curObjInState在长按条目后会被更新为当前被长按的条目
                    // 不用确认，直接删除，可以有撤销的snackbar，但非必须
                    doJobThenOffLoading {
                        val errDb = AppModel.singleInstanceHolder.dbContainer.errorRepository
                        errDb.delete(curObjInState.value)
                        changeStateTriggerRefreshPage(needRefresh)
                    }
                }
            }
        }
        val clipboardManager = LocalClipboardManager.current

        val viewDialogText = rememberSaveable { mutableStateOf("") }
        val showViewDialog = rememberSaveable { mutableStateOf(false) }
        if(showViewDialog.value) {
            CopyableDialog(
                title = stringResource(id = R.string.error_msg),
                text = viewDialogText.value,
                onCancel = {
                    showViewDialog.value=false
                }
            ) { //复制到剪贴板
                showViewDialog.value=false
                clipboardManager.setText(AnnotatedString(viewDialogText.value))
                requireShowToast(appContext.getString(R.string.copied))

            }
        }

        //根据关键字过滤条目
        val k = filterKeyword.value.text.lowercase()  //关键字
        val enableFilter = filterModeOn.value && k.isNotEmpty()
        val list = if(enableFilter){
            list.value.filter {
                it.msg.lowercase().contains(k) || it.date.lowercase().contains(k)
                        || it.id.lowercase().contains(k)
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
        ) { idx, it ->
            //在这个组件里更新了 state curObj，所以长按后直接用curObj就能获取到当前对象了
            ErrorItem(showBottomSheet,curObjInState,idx,it) {
                val sb = StringBuilder()
                sb.append(appContext.getString(R.string.id)).append(": ").appendLine(it.id).appendLine()
                    .append(appContext.getString(R.string.date)).append(": ").appendLine(it.date).appendLine()
                    .append(appContext.getString(R.string.msg)).append(": ").appendLine(it.msg)

                viewDialogText.value = sb.toString()
                showViewDialog.value = true
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
            if (repoId.isNotBlank()) {
                doJobThenOffLoading {
                    //先清下列表
                    list.value.clear()

                    // here only need repo name, no need sync repo info with under git, and if query failed, return a repo with empty id and empty name, but it shouldn't happen though
                    curRepo.value = AppModel.singleInstanceHolder.dbContainer.repoRepository.getByIdNoSyncWithGit(repoId) ?: RepoEntity(id="")

                    //查询错误列表
                    val errDb = AppModel.singleInstanceHolder.dbContainer.errorRepository
                    val errList = errDb.getListByRepoId(repoId)
                    //添加到页面状态列表
                    list.value.addAll(errList)
//                    list.requireRefreshView()

                    //更新数据库
                    val repoDb = AppModel.singleInstanceHolder.dbContainer.repoRepository
                    if (errList.isNotEmpty()) { //如果记录不为空: 清空仓库表的错误信息; 把当前仓库关联的所有错误记录标记成已读
                        repoDb.checkedAllErrById(repoId)
                    }else {  // 如果err list为空，仅清空仓库表的错误信息
                        repoDb.updateErrFieldsById(repoId, Cons.dbCommonFalse, "")
                    }
                }
            }
        } catch (e: Exception) {
            MyLog.e(TAG, "#LaunchedEffect() err: repoId=$repoId , err is:${e.stackTraceToString()}")
        }
    }
    //compose被销毁时执行的副作用
    DisposableEffect(Unit) {
//        println("DisposableEffect: entered main")
        onDispose {
//            println("DisposableEffect: exited main")
        }
    }

}