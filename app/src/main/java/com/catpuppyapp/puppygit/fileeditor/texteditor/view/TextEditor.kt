package com.catpuppyapp.puppygit.fileeditor.texteditor.view

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.selection.LocalTextSelectionColors
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableIntState
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.LocalTextInputService
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.catpuppyapp.puppygit.compose.AcceptButtons
import com.catpuppyapp.puppygit.compose.ConfirmDialog
import com.catpuppyapp.puppygit.compose.ConfirmDialog2
import com.catpuppyapp.puppygit.compose.CopyableDialog
import com.catpuppyapp.puppygit.constants.LineNum
import com.catpuppyapp.puppygit.constants.PageRequest
import com.catpuppyapp.puppygit.dev.bug_Editor_GoToColumnCantHideKeyboard_Fixed
import com.catpuppyapp.puppygit.dev.bug_Editor_SelectColumnRangeOfLine_Fixed
import com.catpuppyapp.puppygit.play.pro.R
import com.catpuppyapp.puppygit.settings.AppSettings
import com.catpuppyapp.puppygit.settings.FileEditedPos
import com.catpuppyapp.puppygit.settings.SettingsUtil
import com.catpuppyapp.puppygit.style.MyStyleKt
import com.catpuppyapp.puppygit.ui.theme.Theme
import com.catpuppyapp.puppygit.utils.AppModel
import com.catpuppyapp.puppygit.utils.EditCache
import com.catpuppyapp.puppygit.utils.Msg
import com.catpuppyapp.puppygit.utils.MyLog
import com.catpuppyapp.puppygit.utils.UIHelper
import com.catpuppyapp.puppygit.utils.doJobThenOffLoading
import com.catpuppyapp.puppygit.utils.fileopenhistory.FileOpenHistoryMan
import com.catpuppyapp.puppygit.utils.getFormatTimeFromSec
import com.catpuppyapp.puppygit.utils.getHumanReadableSizeStr
import com.catpuppyapp.puppygit.utils.getSystemDefaultTimeZoneOffset
import com.catpuppyapp.puppygit.utils.replaceStringResList
import com.catpuppyapp.puppygit.utils.state.mutableCustomStateOf
import com.catpuppyapp.puppygit.fileeditor.texteditor.controller.EditorController
import com.catpuppyapp.puppygit.fileeditor.texteditor.controller.FindDirection
import com.catpuppyapp.puppygit.fileeditor.texteditor.state.TextEditorState
import java.io.File
import java.util.Date

private val TAG ="TextEditor"
private val stateKeyTag ="TextEditor"

class ExpectConflictStrDto(
    var settings: AppSettings = SettingsUtil.getSettingsSnapshot()
) {
    var curConflictStr: String = settings.editor.conflictStartStr
    var curConflictStrMatched: Boolean = false

    fun reset() {
        curConflictStr = settings.editor.conflictStartStr
        curConflictStrMatched = false
    }

    fun getNextExpectConflictStr():String{
        return if(curConflictStr == settings.editor.conflictStartStr) {
            settings.editor.conflictSplitStr
        }else if(curConflictStr == settings.editor.conflictSplitStr) {
            settings.editor.conflictEndStr
        }else { // curStr == settings.editor.conflictEndStr
            settings.editor.conflictStartStr
        }
    }

    fun getCurAndNextExpect():Pair<Int,Int> {
        val curExpect = if(curConflictStr.startsWith(settings.editor.conflictStartStr)){
            0
        }else if(curConflictStr.startsWith(settings.editor.conflictSplitStr)) {
            1
        }else {
            2
        }

        val nextExcept = if(curExpect + 1 > 2) 0 else curExpect+1

        return Pair(curExpect, nextExcept)
    }
}

typealias DecorationBoxComposable = @Composable (
    index: Int,
    isSelected: Boolean,
    innerTextField: @Composable (modifier: Modifier) -> Unit
) -> Unit

//光标handle和选中文本颜色设置
private val customTextSelectionColors = MyStyleKt.TextSelectionColor.customTextSelectionColors
private val customTextSelectionColors_darkMode = MyStyleKt.TextSelectionColor.customTextSelectionColors_darkMode

//隐藏光标，没选中文本时使用这个
private val customTextSelectionColors_hideCursorHandle = MyStyleKt.TextSelectionColor.customTextSelectionColors_hideCursorHandle


private val conflictOursBlockBgColor = UIHelper.getConflictOursBlockBgColor()
private val conflictTheirsBlockBgColor = UIHelper.getConflictTheirsBlockBgColor()
private val conflictStartLineBgColor = UIHelper.getConflictStartLineBgColor()
private val conflictSplitLineBgColor = UIHelper.getConflictSplitLineBgColor()
private val conflictEndLineBgColor = UIHelper.getConflictEndLineBgColor()



@OptIn(ExperimentalFoundationApi::class)
@Composable
fun TextEditor(
    requestFromParent:MutableState<String>,
    fileFullPath:String,
    lastEditedPos: FileEditedPos,
    textEditorState: TextEditorState,
    editableController: EditorController,
    onChanged: (TextEditorState) -> Unit,
    modifier: Modifier = Modifier,
    contentPaddingValues: PaddingValues = PaddingValues(),
    editorLastScrollEvent:MutableState<ScrollEvent?>,
    editorListState: LazyListState,
    editorPageIsInitDone:MutableState<Boolean>,
    goToLine:Int,  // is line number, not line index
    readOnlyMode:Boolean,
    searchMode:MutableState<Boolean>,
    mergeMode:Boolean,
    searchKeyword:String,
    fontSize: MutableIntState,

    decorationBox: DecorationBoxComposable = { _, _, innerTextField -> innerTextField(Modifier) },
) {
    val haptic = AppModel.singleInstanceHolder.haptic
    val appContext = AppModel.singleInstanceHolder.appContext

    val scope = rememberCoroutineScope()
    val keyboardController = LocalSoftwareKeyboardController.current
    val inDarkTheme = Theme.inDarkTheme
//    val textEditorState by rememberUpdatedState(newValue = textEditorState)
    val (virtualWidth, virtualHeight) = UIHelper.Size.editorVirtualSpace()

    var lastScrollEvent by editorLastScrollEvent
    val lazyColumnState = editorListState
    val focusRequesters = remember { mutableStateMapOf<Int, FocusRequester>() }
//    val focusRequesters = remember { mutableStateListOf<FocusRequester>() }  //不能用list，滚动两下页面就会报错

    val settings = remember { SettingsUtil.getSettingsSnapshot() }
    val conflictKeyword = remember { mutableStateOf(settings.editor.conflictStartStr) }




    //最后显示屏幕范围的第一行的索引
//    var lastFirstVisibleLineIndexState  by remember { mutableIntStateOf(lastEditedPos.firstVisibleLineIndex) }

    val showGoToLineDialog  = remember { mutableStateOf(false) }
    val goToLineValue  = remember { mutableStateOf("") }

    //是否显示光标拖手(cursor handle
    val needShowCursorHandle = remember { mutableStateOf(false) }

    //这俩值会在组件销毁时写入配置文件以记录滚动位置(当前画面第一个可见行)和最后编辑位置
    val lastEditedLineIndexState  = remember { mutableIntStateOf(lastEditedPos.lineIndex) }
    val lastEditedColumnIndexState = remember { mutableIntStateOf(lastEditedPos.columnIndex) }

    //此值为假或readOnly为真则不显示键盘
    //没找到合适的方法手动启用，因此默认启用，暂时没更改的场景
    val allowKeyboard = remember { mutableStateOf(true) }

    val expectConflictStrDto = mutableCustomStateOf(stateKeyTag, "expectConflict", ExpectConflictStrDto(settings=settings))


    val nextSearchPos = mutableCustomStateOf(
        keyTag = stateKeyTag,
        keyName = "nextSearchPos",
        initValue = SearchPos.NotFound
    )

    val lastFoundPos = mutableCustomStateOf(
        keyTag = stateKeyTag,
        keyName = "lastFoundPos",
        initValue = SearchPos.NotFound
    )

    val initSearchPos = {
        //把起始搜索位置设置为当前第一个可见行的第一列
//        lastSearchPos.value = SearchPos(lazyColumnState.firstVisibleItemIndex, 0)

        //从上次编辑位置开始搜索
        if(!searchMode.value || lastFoundPos.value== SearchPos.NotFound  //没开搜索模式，或没匹配到关键字，一律使用上次编辑行+列
            || lastFoundPos.value.lineIndex!=lastEditedLineIndexState.intValue  //搜索并匹配后，用户点了其他行
            || lastFoundPos.value.columnIndex!=lastEditedColumnIndexState.intValue  //搜索并匹配后，用户点了其他列
        ) {
            nextSearchPos.value = SearchPos(lastEditedLineIndexState.intValue, lastEditedColumnIndexState.intValue)
        }
//        println("lasteditcis:"+lastEditedColumnIndexState.intValue)  //test1791022120240812
//        println("startPos:"+nextSearchPos.value) //test1791022120240812
    }

    fun jumpToLineIndex(lineIndex:Int, goColumn: Boolean=false, columnStartIndex:Int=0, columnEndIndexExclusive:Int=columnStartIndex){
        lastScrollEvent = ScrollEvent(lineIndex, forceGo = true,
            goColumn = goColumn, columnStartIndexInclusive = columnStartIndex, columnEndIndexExclusive = columnEndIndexExclusive)
    }

    suspend fun doSearch(key:String, toNext:Boolean, startPos: SearchPos) {
        val keyLen = key.length

        val posResult = editableController.doSearch(key.lowercase(), toNext = toNext, startPos = startPos)
        val foundPos = posResult.foundPos
        if(foundPos == SearchPos.NotFound) {
            if(!searchMode.value && mergeMode) {
                Msg.requireShow(appContext.getString(R.string.no_conflict_found))
            }else {
                Msg.requireShow(appContext.getString(R.string.not_found))
            }
        }else {  //查找到了关键字

//            println("found:$foundPos")//test1791022120240812

            //显示选中文本背景颜色
            needShowCursorHandle.value = true

            //跳转到对应行并选中关键字
            jumpToLineIndex(foundPos.lineIndex, goColumn = true, foundPos.columnIndex, foundPos.columnIndex+keyLen)

            lastFoundPos.value = posResult.foundPos.copy()

            //更新最后编辑行和列
            lastEditedLineIndexState.intValue = lastFoundPos.value.lineIndex
            lastEditedColumnIndexState.intValue = lastFoundPos.value.columnIndex
            //选中关键字(有缺陷)
//            editableController.selectTextInLine(foundPos.lineIndex, foundPos.columnIndex, endIndexExclusive)

            //更新下次搜索起点
            nextSearchPos.value = posResult.nextPos.copy()
        }
    }

    //每次计算依赖的状态变化就会重新计算这个变量的值
//    val firstLineIndexState = remember { derivedStateOf {
//        val visibleItems= lazyColumnState.layoutInfo.visibleItemsInfo
//        if(visibleItems.isEmpty()) {
//            0
//        }else {
//            //按索引排序，取出索引最小的元素的索引
//            visibleItems.minBy { it.index }.index
//        }
//    } }

    //执行一次LaunchedEffected后，此值才应为真，放到LaunchedEffect末尾修改为true即可。（目前 20240419 用来判断是否需要保存可见的第一行索引，刚打开文件要么默认0，要么恢复上次滚动位置，都不需要保存，只有打开文件后（初始化后）再滚动才需要保存）
    val isInitDone = editorPageIsInitDone

    editableController.syncState(textEditorState)

    val clipboardManager = LocalClipboardManager.current

    val showDetailsDialog = remember { mutableStateOf(false) }
    val detailsStr = remember { mutableStateOf("") }

    if(showDetailsDialog.value) {
        CopyableDialog(
            title = stringResource(R.string.details),
            text = detailsStr.value,
            onCancel = { showDetailsDialog.value = false }
        ) {
            showDetailsDialog.value = false
            clipboardManager.setText(AnnotatedString(detailsStr.value))
            Msg.requireShow(appContext.getString(R.string.copied))
        }
    }

    //上级页面发来的request，请求执行某些操作
    //20240507: 这个其实已经没用了，改用在第一行和最后编辑位置切换了，不过，暂且先留着这代码
    if(requestFromParent.value==PageRequest.goToTop) {
        PageRequest.clearStateThenDoAct(requestFromParent) {
            lastScrollEvent = ScrollEvent(0, forceGo = true)
        }
    }
    if(requestFromParent.value==PageRequest.goToLine) {
        PageRequest.clearStateThenDoAct(requestFromParent) {
            showGoToLineDialog.value=true
        }
    }
    if(requestFromParent.value==PageRequest.requireSearch) {
        PageRequest.clearStateThenDoAct(requestFromParent) {
            //初始化搜索位置
//            initSearchPos()
            //开启搜索模式
            searchMode.value = true
        }
    }
    if(requestFromParent.value==PageRequest.findPrevious) {
        PageRequest.clearStateThenDoAct(requestFromParent) {
            doJobThenOffLoading {
                initSearchPos()
                doSearch(searchKeyword, toNext = false, nextSearchPos.value)
            }
        }
    }
    if(requestFromParent.value==PageRequest.findNext) {
        PageRequest.clearStateThenDoAct(requestFromParent) {
            doJobThenOffLoading {
                initSearchPos()
                doSearch(searchKeyword, toNext = true, nextSearchPos.value)
            }
        }
    }
    //显示总共有多少关键字（关键字计数）
    if(requestFromParent.value==PageRequest.showFindNextAndAllCount) {
        PageRequest.clearStateThenDoAct(requestFromParent) {
            doJobThenOffLoading {
                val allCount = editableController.getKeywordCount(searchKeyword)
                Msg.requireShow(replaceStringResList(appContext.getString(R.string.find_next_all_count), listOf(allCount.toString())))
            }
        }
    }


    if(requestFromParent.value==PageRequest.previousConflict) {
        PageRequest.clearStateThenDoAct(requestFromParent) {
            doJobThenOffLoading {
                initSearchPos()

                val nextSearchLine = textEditorState.fields.get(nextSearchPos.value.lineIndex).value.text
                if(nextSearchLine.startsWith(settings.editor.conflictStartStr)) {
                    conflictKeyword.value = settings.editor.conflictStartStr
                }else if(nextSearchLine.startsWith(settings.editor.conflictSplitStr)) {
                    conflictKeyword.value = settings.editor.conflictSplitStr
                }else if(nextSearchLine.startsWith(settings.editor.conflictEndStr)) {
                    conflictKeyword.value = settings.editor.conflictEndStr
                }

                val previousKeyWord = getPreviousKeyWordForConflict(conflictKeyword.value, settings)
                conflictKeyword.value = previousKeyWord
                doSearch(previousKeyWord, toNext = false, nextSearchPos.value)
            }
        }
    }
    if(requestFromParent.value==PageRequest.nextConflict) {
        PageRequest.clearStateThenDoAct(requestFromParent) {
            doJobThenOffLoading {
                initSearchPos()

                // update cur conflict keyword, if cursor on conflict str line, if dont do this, UX bad, e.g. I clicked conflict splict line, then click prev conflict, expect is go conflict start line, but if last search is start line, this time will go to end line, anti-intuition
                // 如果光标在冲突开始、分割、结束行之一，更新搜索关键字，如果不这样做，会出现一些反直觉的bug：我点击了conflict split line，然后点上，期望是查找conflict start line，但如果上次搜索状态是start line，那这次就会去搜索end line，反直觉
                val nextSearchLine = textEditorState.fields.get(nextSearchPos.value.lineIndex).value.text
                if(nextSearchLine.startsWith(settings.editor.conflictStartStr)) {
                    conflictKeyword.value = settings.editor.conflictStartStr
                }else if(nextSearchLine.startsWith(settings.editor.conflictSplitStr)) {
                    conflictKeyword.value = settings.editor.conflictSplitStr
                }else if(nextSearchLine.startsWith(settings.editor.conflictEndStr)) {
                    conflictKeyword.value = settings.editor.conflictEndStr
                }

                val nextKeyWord = getNextKeyWordForConflict(conflictKeyword.value, settings)
                conflictKeyword.value = nextKeyWord
                doSearch(nextKeyWord, toNext = true, nextSearchPos.value)
            }
        }
    }
    if(requestFromParent.value==PageRequest.showNextConflictAndAllConflictsCount) {
        PageRequest.clearStateThenDoAct(requestFromParent) {
            doJobThenOffLoading {
//                val allCount = editableController.getKeywordCount(conflictKeyword.value)  // this keyword is dynamic change when press next or previous, used for count conflict is ok though, but use conflict start str count can be better
                val allCount = editableController.getKeywordCount(settings.editor.conflictStartStr)
                Msg.requireShow(replaceStringResList(appContext.getString(R.string.next_conflict_all_count), listOf(allCount.toString())))
            }
        }
    }
    //20240507: 这个其实已经没用了，改用在第一行和最后编辑位置切换了，所以先注释了
//    if(requestFromParent.value==PageRequest.backLastEditedLine) {
//        PageRequest.clearStateThenDoAct(requestFromParent) {
//            val settings = SettingsUtil.getSettingsSnapshot()
//            val lastPos = settings.editor.filesLastEditPosition[fileFullPath]
////            if(debugModeOn) {
////                println("back to lastLineIndex: "+lastPos?.lineIndex)
////            }
//            lastScrollEvent = ScrollEvent(lastPos?.lineIndex?:0, forceGo = true)
////            lastScrollEvent = ScrollEvent(23, forceGo = true)  //测试跳转是否好使，结果：passed
//        }
//    }
    if(requestFromParent.value==PageRequest.switchBetweenFirstLineAndLastEditLine) {
        PageRequest.clearStateThenDoAct(requestFromParent) {
//            println("firstline:"+firstLineIndexState.value)
//            println("lastEditedLineIndexState:"+lastEditedLineIndexState)

            val notAtTop = lazyColumnState.firstVisibleItemIndex != 0
            // if 不在顶部，go to 顶部 else go to 上次编辑位置
            val position = if(notAtTop) 0 else lastEditedLineIndexState.intValue
            lastScrollEvent = ScrollEvent(position, forceGo = true)
        }
    }


    if(requestFromParent.value==PageRequest.showDetails) {
        PageRequest.clearStateThenDoAct(requestFromParent) {
            val file = File(fileFullPath)
            val fileSize = getHumanReadableSizeStr(file.length())
            val (charsCount, linesCount) = editableController.getCharsAndLinesCount()
            val lastModifiedTimeStr = getFormatTimeFromSec(sec=file.lastModified()/1000, offset = getSystemDefaultTimeZoneOffset())
            val sb = StringBuilder()

            sb.appendLine(appContext.getString(R.string.file_name)+": "+file.name).appendLine()
//            .appendLine(appContext.getString(R.string.path)+": "+ getFilePathStrBasedRepoDir(fileFullPath, returnResultStartsWithSeparator=true)).appendLine()
                .appendLine(appContext.getString(R.string.path)+": "+ fileFullPath).appendLine()
                .appendLine(appContext.getString(R.string.chars)+": "+charsCount).appendLine()
                .appendLine(appContext.getString(R.string.lines) +": "+linesCount).appendLine()

                .appendLine(appContext.getString(R.string.file_size)+": "+fileSize).appendLine()
                .appendLine(appContext.getString(R.string.last_modified)+": "+lastModifiedTimeStr)



            detailsStr.value = sb.toString()
            showDetailsDialog.value = true
        }
    }

    // get line number(line index+1)
    val getLineVal = {i:String ->
        try {
            //删下首尾空格，增加容错率，然后尝试转成int
            val line = i.trim().toInt()
            if(line==LineNum.EOF.LINE_NUM) {  // if is EOF, return last line number, then can go to end of file
                textEditorState.fields.size
            }else{
                Math.max(1, line)
            }
        }catch (e:Exception) {
            // parse int failed, then go first line
            1
        }

    }
    val doGoToLine = {
        //x 会报错，提示index必须为非负数) 测试下如果是-1会怎样？是否会报错？
//        val lineIntVal = -1
        val lineIntVal = getLineVal(goToLineValue.value)
        //行号减1即要定位行的索引
        lastScrollEvent = ScrollEvent(index = lineIntVal-1, forceGo=true)
    }

    if(showGoToLineDialog.value) {
        val firstLine = "1"
        val lastLine = ""+textEditorState.fields.size
        val lineNumRange = "$firstLine-$lastLine"
        ConfirmDialog(title = stringResource(R.string.go_to_line),
            requireShowTextCompose = true,
            textCompose = {
                Column(
                    modifier= Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                    ,
                ) {

                    androidx.compose.material3.TextField(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(10.dp)
                        ,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Go),
                        keyboardActions = KeyboardActions(onGo = {
                            showGoToLineDialog.value = false
                            doGoToLine()
                        }),
                        singleLine = true,

                        value = goToLineValue.value,
                        onValueChange = {
                            goToLineValue.value=it
                        },
                        label = {
                            Text(stringResource(R.string.line_number)+"($lineNumRange)")
                        },
                        placeholder = {
                            //显示行号范围，例如："Range: 1-123"
                            Text(stringResource(R.string.range) + ": $lineNumRange")
                        }
                    )

                    Column(
                        modifier= Modifier
                            .fillMaxWidth()
                            .padding(end = 10.dp)
                        ,
                        horizontalAlignment = Alignment.End
                    ) {

                        Text(
                            text = stringResource(R.string.first_line),
                            style = MyStyleKt.ClickableText.style,
                            color = MyStyleKt.ClickableText.color,
                            modifier = MyStyleKt.ClickableText.modifier.clickable {
                                goToLineValue.value = firstLine
                            },
                            fontWeight = FontWeight.Light
                        )

                        Spacer(Modifier.height(15.dp))

                        Text(
                            text = stringResource(R.string.last_line),
                            style = MyStyleKt.ClickableText.style,
                            color = MyStyleKt.ClickableText.color,
                            modifier = MyStyleKt.ClickableText.modifier.clickable {
                                goToLineValue.value = lastLine
                            },
                            fontWeight = FontWeight.Light
                        )

                        Spacer(Modifier.height(10.dp))

                    }
                }


            },
            okBtnEnabled = goToLineValue.value.isNotBlank(),
            okBtnText = stringResource(id = R.string.go),
            cancelBtnText = stringResource(id = R.string.cancel),
            onCancel = { showGoToLineDialog.value = false }
        ) {
            showGoToLineDialog.value = false
            doGoToLine()
        }
    }

//    val scrollTo = { lineIndex:Int ->
//        scope.launch {
//            lazyColumnState.scrollToItem(Math.max(0, lineIndex))
//        }
//        Unit
//    }


    // actually only need know which lines will delete, no need know which will kept
//    val keepStartIndex = remember { mutableStateOf(-1) }
//    val keepEndIndex = remember { mutableStateOf(-1) }

    val delStartIndex = remember { mutableStateOf(-1) }
    val delEndIndex = remember { mutableStateOf(-1) }
    val delSingleIndex = remember { mutableStateOf(-1) }
    val acceptOursState = remember { mutableStateOf(false) }
    val acceptTheirsState = remember { mutableStateOf(false) }
    val showAcceptConfirmDialog = remember { mutableStateOf(false) }

    /**
     * find accept block indecies,
     * find direction:
     * if curLineText starts with conflict start str: conflict start -> split -> end (all go down)
     * if starts with split str: split -> start -> end (go down, then go up)
     * if starts with end str: end -> split -> start (all go up)
     */
    val prepareAcceptBlock= label@{acceptOurs: Boolean, acceptTheirs:Boolean, index: Int, curLineText: String ->
//        println("index=$index, curLine=$curLineText")

        val curStartsWithStart = curLineText.startsWith(settings.editor.conflictStartStr)
        val curStartsWithSplit = curLineText.startsWith(settings.editor.conflictSplitStr)
        val curStartsWithEnd = curLineText.startsWith(settings.editor.conflictEndStr)
        if(!(curStartsWithStart || curStartsWithSplit || curStartsWithEnd)) {
            Msg.requireShow(appContext.getString(R.string.invalid_conflict_block))
            return@label
        }


        val firstFindDirection = if(curStartsWithStart) {
            FindDirection.DOWN
        }else {
            FindDirection.UP
        }

        val firstExpectStr = if(curStartsWithStart) {
            settings.editor.conflictSplitStr
        }else if(curStartsWithSplit) {
            settings.editor.conflictStartStr
        }else {  // conflict end str like ">x7 branchName"
            settings.editor.conflictSplitStr
        }


        val (firstIndex, _) = editableController.indexAndValueOf(startIndex=index, direction=firstFindDirection, predicate={it.startsWith(firstExpectStr)}, includeStartIndex = false)

        if(firstIndex == -1) {
            Msg.requireShow(appContext.getString(R.string.invalid_conflict_block))
            return@label
        }

        val secondFindDirection = if(curStartsWithEnd) {
            FindDirection.UP
        }else {
            FindDirection.DOWN
        }

        val secondExpectStr = if(curStartsWithEnd) {
            settings.editor.conflictStartStr
        }else {
            settings.editor.conflictEndStr
        }

        val secondStartFindIndexAt =if(curStartsWithSplit) {
            index
        }else {
            firstIndex
        }
        val (secondIndex, _) = editableController.indexAndValueOf(startIndex=secondStartFindIndexAt, direction=secondFindDirection, predicate={it.startsWith(secondExpectStr)}, includeStartIndex = false)

        if(secondIndex==-1) {
            Msg.requireShow(appContext.getString(R.string.invalid_conflict_block))
            return@label
        }

        val startConflictLineIndex = if(curStartsWithStart) index else if(curStartsWithSplit) firstIndex else secondIndex  //this is start conflict str index
        val splitConflictLineIndex = if(curStartsWithStart || curStartsWithEnd) firstIndex else index  // this is split conflict str index
        val endConflictLineIndex = if(curStartsWithStart || curStartsWithSplit) secondIndex else index  // this is end conflict str index

        // special case: start may larger than end index, e.g. will keep 30 to 20, but, only shown wrong, will not err when deleting
        // 特殊情况：start index可能大于end index，例如：30 到 20，不过只是显示有误，实际执行无误
        if(acceptOurs && acceptTheirs.not()) {
            acceptOursState.value = true
            acceptTheirsState.value = false

            // remove single index and range [start, end]
            delSingleIndex.value = startConflictLineIndex
            delStartIndex.value = splitConflictLineIndex
            delEndIndex.value = endConflictLineIndex

//            keepStartIndex.value = startConflictLineIndex + 1  // this is startIndex+1
//            keepEndIndex.value = splitConflictLineIndex - 1  // this is splitIndex-1
        }else if(acceptOurs.not() && acceptTheirs) {
            acceptOursState.value = false
            acceptTheirsState.value = true

            // remove single index and range [start, end]
            delSingleIndex.value = endConflictLineIndex
            delStartIndex.value = startConflictLineIndex
            delEndIndex.value = splitConflictLineIndex

//            keepStartIndex.value = splitConflictLineIndex + 1  //this is split index +1
//            keepEndIndex.value = endConflictLineIndex - 1  // this is end index -1
        }else if(acceptOurs && acceptTheirs) {  // accept both
            acceptOursState.value = true
            acceptTheirsState.value = true

            // remove 3 indices, no range
            delSingleIndex.value = startConflictLineIndex
            delStartIndex.value= splitConflictLineIndex
            delEndIndex.value = endConflictLineIndex
        }else { // reject both
            acceptOursState.value = false
            acceptTheirsState.value = false

            // remove range [start, end]
            delStartIndex.value = startConflictLineIndex
            delEndIndex.value = endConflictLineIndex
        }

        // show dialog, make sure user confirm
        showAcceptConfirmDialog.value = true
    }

    if(showAcceptConfirmDialog.value) {
        ConfirmDialog2(
            title = if(acceptOursState.value && acceptTheirsState.value.not()) stringResource(R.string.accept_ours)
            else if(acceptOursState.value.not() && acceptTheirsState.value) stringResource(R.string.accept_theirs)
            else if(acceptOursState.value && acceptTheirsState.value) stringResource(R.string.accept_both)
            else stringResource(R.string.reject_both),

            text = if(acceptOursState.value && acceptTheirsState.value.not()) replaceStringResList(stringResource(R.string.will_accept_ours_and_delete_lines_line_indexs), listOf(""+(delSingleIndex.value + 1), ""+(delStartIndex.value + 1), ""+(delEndIndex.value + 1)))
            else if(acceptOursState.value.not() && acceptTheirsState.value) replaceStringResList(stringResource(R.string.will_accept_theirs_and_delete_lines_line_indexs), listOf(""+(delSingleIndex.value + 1), ""+(delStartIndex.value + 1), ""+(delEndIndex.value + 1)))
            else if(acceptOursState.value && acceptTheirsState.value) replaceStringResList(stringResource(R.string.will_accept_both_and_delete_lines_line_indexs), listOf(""+(delSingleIndex.value + 1), ""+(delStartIndex.value + 1), ""+(delEndIndex.value + 1)))
            else replaceStringResList(stringResource(R.string.will_reject_both_and_delete_lines_line_indexs), listOf(""+(delStartIndex.value + 1), ""+(delEndIndex.value + 1))),

            onCancel = {showAcceptConfirmDialog.value = false}
        ) {
            showAcceptConfirmDialog.value=false

            doJobThenOffLoading{
                val indicesWillDel = if((acceptOursState.value && acceptTheirsState.value.not()) || (acceptOursState.value.not() && acceptTheirsState.value)){
                    //accept ours/theirs
                    val tmp = mutableListOf(delSingleIndex.value)
                    tmp.addAll(IntRange(start = delStartIndex.value, endInclusive = delEndIndex.value).toList())
                    tmp
                }else if(acceptOursState.value && acceptTheirsState.value) {  // accept both
                    val tmp = mutableListOf(delSingleIndex.value)
                    tmp.add(delStartIndex.value)
                    tmp.add(delEndIndex.value)
                    tmp
                }else {  // reject both
                    IntRange(start = delStartIndex.value, endInclusive = delEndIndex.value).toList()
                }

                editableController.deleteLineByIndices(indicesWillDel)
            }
        }
    }


    LaunchedEffect(lastScrollEvent) TextEditorLaunchedEffect@{
        try {
//        if(debugModeOn) {
            //还行不是很长
//            println("lastScrollEvent.toString() + firstLineIndexState.value:"+(lastScrollEvent.toString() + firstLineIndexState.value))
//        }

//        if(debugModeOn) {
//            println("滚动事件更新了："+lastScrollEvent)
////            println("最后编辑行："+lastEditedPos)
//            println("第一个可见行："+firstLineIndexState.value)
//        }
            //如果值不是-1将会保存
//        var maybeWillSaveEditedLineIndex = -1  //最后编辑行
//        var maybeWillSaveFirstVisibleLineIndex = -1  //首个可见行

            //初始化（组件创建，第一次执行LaunchedEffect）之后，更新最新可见行
//        if(isInitDone.value) {
//            lastFirstVisibleLineIndexState = Math.max(0, firstLineIndexState.value)
//        }

            //刚打开文件，定位到上次记录的行，这个滚动只在初始化时执行一次
            if(lastScrollEvent==null && !isInitDone.value) {
                //放到第一行是为了避免重入
                isInitDone.value=true

                //goToLine触发场景：预览diff，发现某行需要改，点击行号，就会直接定位到对应行了
                //会用goToLine的值减1得到索引，所以0也不行
                val useLastEditPos = LineNum.shouldRestoreLastPosition(goToLine)

                //滚动一定要放到scope里执行，不然这个东西一滚动，整个LaunchedEffect代码块后面就不执行了
                //如果goToLine大于0，把行号减1换成索引；否则跳转到上次退出前的第一可见行
                UIHelper.scrollToItem(
                    coroutineScope = scope,
                    listState = lazyColumnState,
                    index = if(useLastEditPos) lastEditedPos.firstVisibleLineIndex else if(goToLine==LineNum.EOF.LINE_NUM) textEditorState.fields.size-1 else goToLine-1
                )

                //如果定位到上次退出位置，进一步检查是否需要定位到最后编辑列
                //因为定位column会弹出键盘所以暂时不定位了，我不想一打开编辑器自动弹出键盘，因为键盘会自动读取上下文，可能意外获取屏幕上的文本泄漏隐私
                if(bug_Editor_GoToColumnCantHideKeyboard_Fixed && useLastEditPos) {
                    //是否需要定位到上次编辑的列，若否，只定位到最后退出前的首个可见行
                    val restoreLastEditColumn = SettingsUtil.getSettingsSnapshot().editor.restoreLastEditColumn
    //                val restoreLastEditColumn = true  //test2024081116726433

                    //如果是readOnly模式，就没必要定位到对应列了，就算定位了也无效，多此一举
                    if(!readOnlyMode && useLastEditPos && restoreLastEditColumn) {
                        //定位到指定列。注意：会弹出键盘！没找到好的不弹键盘的方案，所以我把定位列功能默认禁用了
                        editableController.selectField(
                            lastEditedPos.lineIndex,
                            option = EditorController.SelectionOption.CUSTOM,
                            columnStartIndexInclusive = lastEditedPos.columnIndex
                        )
                    }

                }

                return@TextEditorLaunchedEffect


                //只有当滚动事件不为null且isConsumed为假时，才执行下面的代码块
            }else if(lastScrollEvent?.isConsumed == false) {  //编辑了文件，行号有更新，更新配置文件记录的行并定位到对应的行
                //消费以避免重复执行（设置isConsumed为true）
                lastScrollEvent?.consume()

                //检查是否不检查行是否可见，直接强制跳转
                val forceGo = lastScrollEvent?.forceGo == true
                lastScrollEvent?.index?.let { index ->
//                val safeIndex = Math.max(0, index)
//                maybeWillSaveEditedLineIndex = safeIndex

                    //先跳转，然后更新配置文件
                    //跳转
                    //强制跳转，无论是否可见
                    if(forceGo) {
                        //强制跳转不应该更新最后编辑行，因为跳转后并不会自动focus跳转到的那行，最后编辑行其实还是之前的那个，所以，只更新可见行即可
//                    maybeWillSaveEditedLineIndex = -1

                        //强制跳转的话，第一个可见行就是跳转的那行
//                    lastFirstVisibleLineIndexState = index
                        //定位行
                        UIHelper.scrollToItem(scope, lazyColumnState, index)

//                        println("lastScrollEvent!!.columnStartIndexInclusive:${lastScrollEvent!!.columnStartIndexInclusive}")  //test1791022120240812
                        //定位列，如果请求定位列的话
                        if(lastScrollEvent?.goColumn==true) {
                            //选中关键字
                            editableController.selectField(
                                targetIndex = index,
                                option = EditorController.SelectionOption.CUSTOM,
                                columnStartIndexInclusive = lastScrollEvent!!.columnStartIndexInclusive,
                                //如果选中某行子字符串的功能修复了，就使用正常的endIndex；否则使用startIndex，定位光标到关键字出现的位置但不选中关键字
                                columnEndIndexExclusive = if(bug_Editor_SelectColumnRangeOfLine_Fixed) lastScrollEvent!!.columnEndIndexExclusive else lastScrollEvent!!.columnStartIndexInclusive,
                                requireSelectLine = false
                            )
                        }
                    }else {
                        //更新最后编辑行状态
                        lastEditedLineIndexState.intValue = index
//                        println("index="+index)
                        //检查一下，如果对应索引不可见则跳转
                        // list.minBy(item.index) 用元素的index排序，取出index最小的元素，后面跟.index，即取出最小index元素的index,maxBy和minBy异曲同工
//                    val first = lazyColumnState.layoutInfo.visibleItemsInfo.minBy { it.index }.index
//                    lastFirstVisibleLineIndexState = first

                        val first = lazyColumnState.firstVisibleItemIndex
//                    if(debugModeOn) { //期望 true，结果 true，测试通过
//                        println("firstLineIndexState.value == first:"+(firstLineIndexState.value == first))
//                    }
                        val end = lazyColumnState.layoutInfo.visibleItemsInfo.maxBy { it.index }.index
                        //如果指定行不在可见范围内，滚动到指定行以使其可见
                        if (index < first || index > end) {
                            //滚动到指定行
                            UIHelper.scrollToItem(scope, lazyColumnState, index)
                        }
                    }
                }
            }

        }catch (e:Exception) {
            MyLog.e(TAG, "#TextEditorLaunchedEffect@ , err: "+e.stackTraceToString())
        }

//        if(debugModeOn) {
//            println("maybeSaveEditedLineIndex=$maybeWillSaveEditedLineIndex")
//            println("maybeSaveFirstVisibleLineIndex=$maybeWillSaveFirstVisibleLineIndex")
//            println("lastEditedLineIndexState=$lastEditedLineIndexState")
//            println("lastFirstVisibleLineIndexState=$lastFirstVisibleLineIndexState")
//        }

    }

    DisposableEffect(Unit) {
        onDispose TextEditorOnDispose@{
            try {

                //更新配置文件记录的滚动位置（当前屏幕可见第一行）和最后编辑行
                //如果当前索引不是上次定位的索引，更新页面状态变量和配置文件中记录的最后编辑行索引，不管是否需要滚动，反正先记上
                //先比较一下，Settings对象从内存取不用读文件，很快，如果没变化，就不用更新配置文件了，省了磁盘IO，所以有必要检查下
                val oldLinePos = FileOpenHistoryMan.get(fileFullPath)
                val needUpdateLastEditedLineIndex = oldLinePos?.lineIndex != lastEditedLineIndexState.intValue
                val currentFirstVisibleIndex = lazyColumnState.firstVisibleItemIndex
                val needUpdateFirstVisibleLineIndex = oldLinePos?.firstVisibleLineIndex != currentFirstVisibleIndex

                //记住最后编辑列
                val editedColumnIndex = lastEditedColumnIndexState.intValue
//                println("lasteditcolumnindex:"+editedColumnIndex) //test2024081116726433
                val needUpdateLastEditedColumnIndex = oldLinePos?.columnIndex != editedColumnIndex

                if((needUpdateLastEditedLineIndex || needUpdateFirstVisibleLineIndex || needUpdateLastEditedColumnIndex)) {
//                    println("will save possssssssssssssssssssss")
//                    SettingsUtil.update {
                    val pos = oldLinePos
//                        if(pos==null) {
//                            pos = FileEditedPos()
//                        }
                    if(needUpdateLastEditedLineIndex) {
                        pos.lineIndex = lastEditedLineIndexState.intValue
                    }
                    if(needUpdateFirstVisibleLineIndex) {
                        pos.firstVisibleLineIndex = currentFirstVisibleIndex
                    }
                    if(needUpdateLastEditedColumnIndex) {
                        pos.columnIndex = editedColumnIndex
                    }
//                if(debugModeOn) {
//                    println("editorPos will save: "+pos)
//                }
//                        println(pos) //test2024081116726433
                    FileOpenHistoryMan.set(fileFullPath, pos)
//                    }
                }
            }catch (e:Exception) {
                MyLog.e(TAG, "#TextEditorOnDispose@ , err: "+e.stackTraceToString())
            }
        }
    }

    CompositionLocalProvider(
        LocalTextInputService provides (if(allowKeyboard.value && !readOnlyMode) LocalTextInputService.current else null),  //为null可阻止弹出键盘
        LocalTextSelectionColors provides (if(!needShowCursorHandle.value) customTextSelectionColors_hideCursorHandle else if(inDarkTheme) customTextSelectionColors_darkMode else customTextSelectionColors),
    ) {
        if(textEditorState.fields.isEmpty()) {
            Column(modifier = Modifier
                .fillMaxSize()
                .padding(contentPaddingValues)
                .verticalScroll(rememberScrollState())
                ,
            ) {
                // noop
            }
        }else{
            expectConflictStrDto.value.reset()

            LazyColumn(
                state = lazyColumnState,
                //fillMaxSize是为了点哪都能触发滚动，这样点哪都能隐藏顶栏
                modifier = modifier.fillMaxSize(),
                contentPadding = contentPaddingValues
            ) {
                //fields本身就是toList()出来的，无需再toList()
                textEditorState.fields.forEachIndexed{ index, textFieldState ->
                    val curLineText = textFieldState.value.text

                    val bgColor = if(mergeMode) {
                        UIHelper.getBackgroundColorForMergeConflictSplitText(
                            text = curLineText,
                            settings = settings,
                            expectConflictStrDto = expectConflictStrDto.value,
                            oursBgColor = conflictOursBlockBgColor,
                            theirsBgColor = conflictTheirsBlockBgColor,
                            startLineBgColor= conflictStartLineBgColor,
                            splitLineBgColor= conflictSplitLineBgColor,
                            endLineBgColor= conflictEndLineBgColor
                        )
                    } else {
                        Color.Unspecified
                    }


                    if(mergeMode && curLineText.startsWith(settings.editor.conflictStartStr)) {
                        item {
                            AcceptButtons(
                                lineIndex = index,
                                lineText = curLineText,
                                prepareAcceptBlock = prepareAcceptBlock,
                                conflictOursBlockBgColor = conflictOursBlockBgColor,
                                conflictTheirsBlockBgColor = conflictTheirsBlockBgColor,
                                conflictSplitLineBgColor = conflictSplitLineBgColor
                            )
                        }
                    }

                    item(key = textFieldState.id) {
                        val focusRequester by remember { mutableStateOf(FocusRequester()) }

                        DisposableEffect(Unit) {
                            focusRequesters[index] = focusRequester
                            onDispose LazyColumnItemOnDispose@{
                                try {
                                    focusRequesters.remove(index)

                                }catch (e:Exception) {
                                    MyLog.e(TAG, "#LazyColumnItemOnDispose@: `focusRequesters.remove(index)` err: "+e.stackTraceToString())
                                }
                            }
                        }

                        decorationBox(
                            index,
                            textFieldState.isSelected
                        ) { modifier ->
                            // FileEditor里的innerTextFiled()会执行这的代码
                            Box(
                                modifier = modifier
                                    .background(bgColor)
                                    .combinedClickable(
                                        //不显示点击效果（闪烁动画）
                                        interactionSource = remember { MutableInteractionSource() },
                                        indication = null,

                                        onLongClick = clickable@{
                                            if (!textEditorState.isMultipleSelectionMode) return@clickable

                                            //震动反馈，和长按选择文本的震动反馈冲突了，若开会振两下
//                                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)

                                            //执行区域选择
                                            editableController.selectFieldSpan(index)
                                        }
                                    ) clickable@{
                                        if (!textEditorState.isMultipleSelectionMode) return@clickable

                                        editableController.selectField(targetIndex = index)
                                    }
                            ) {
                                TextField(
                                    mergeMode=mergeMode,
                                    searchMode = searchMode.value,
                                    lastEditedColumnIndexState=lastEditedColumnIndexState,
                                    needShowCursorHandle = needShowCursorHandle,
                                    textFieldState = textFieldState,
                                    enabled = !textEditorState.isMultipleSelectionMode && !readOnlyMode,
                                    focusRequester = focusRequester,
                                    fontSize = fontSize,
//                                    bgColor = bgColor,
                                    bgColor = Color.Unspecified,
                                    onUpdateText = { newText ->
                                        try{
                                            //写入编辑缓存
                                            doJobThenOffLoading {
                                                EditCache.writeToFile(newText.text)
                                            }

                                            editableController.updateField(
                                                targetIndex = index,
                                                textFieldValue = newText
                                            )

                                            //改用onFocus定位最后编辑行了，这里不需要了，实际上现在的最后编辑行就是光标最后所在行
//                                            lastScrollEvent = ScrollEvent(index)

                                        }catch (e:Exception) {
                                            Msg.requireShowLongDuration("#onUpdateText err: "+e.localizedMessage)
                                            MyLog.e(TAG, "#onUpdateText err: "+e.stackTraceToString())
                                        }
                                    },
                                    onContainNewLine = { newText ->
                                        try {
                                            //写入编辑缓存
                                            doJobThenOffLoading {
                                                EditCache.writeToFile(newText.text)
                                            }

                                            if (lastScrollEvent != null && lastScrollEvent?.isConsumed != true) return@TextField
                                            editableController.splitNewLine(
                                                targetIndex = index,
                                                textFieldValue = newText
                                            )
                                            lastScrollEvent = ScrollEvent(index + 1)
                                        }catch (e:Exception) {
                                            Msg.requireShowLongDuration("#onContainNewLine err: "+e.localizedMessage)
                                            MyLog.e(TAG, "#onContainNewLine err: "+e.stackTraceToString())
                                        }

                                    },
                                    onAddNewLine = { newText ->
                                        try {
                                            //写入编辑缓存
                                            doJobThenOffLoading {
                                                EditCache.writeToFile(newText.text)
                                            }

                                            if (lastScrollEvent != null && lastScrollEvent?.isConsumed != true) return@TextField
                                            editableController.splitAtCursor(
                                                targetIndex = index,
                                                textFieldValue = newText
                                            )
                                            lastScrollEvent = ScrollEvent(index + 1)
                                        }catch (e:Exception) {
                                            Msg.requireShowLongDuration("#onAddNewLine err: "+e.localizedMessage)
                                            MyLog.e(TAG, "#onAddNewLine err: "+e.stackTraceToString())
                                        }

                                    },
                                    onDeleteNewLine = {
                                        try {
                                            if (lastScrollEvent != null && lastScrollEvent?.isConsumed != true) return@TextField
                                            editableController.deleteField(targetIndex = index)
                                            if (index != 0) lastScrollEvent = ScrollEvent(index - 1)
                                        }catch (e:Exception) {
                                            Msg.requireShowLongDuration("#onDeleteNewLine err: "+e.localizedMessage)
                                            MyLog.e(TAG, "#onDeleteNewLine err: "+e.stackTraceToString())
                                        }

                                    },
                                    onFocus = {
                                        try {
                                            editableController.selectField(index)

                                            //更新最后聚焦行(最后编辑行)
                                            lastScrollEvent = ScrollEvent(index)
                                        }catch (e:Exception) {
                                            Msg.requireShowLongDuration("#onFocus err: "+e.localizedMessage)
                                            MyLog.e(TAG, "#onFocus err: "+e.stackTraceToString())
                                        }
                                    },
                                    onUpFocus = {
                                        try {
                                            if (lastScrollEvent != null && lastScrollEvent?.isConsumed != true) return@TextField
                                            editableController.selectPreviousField()
                                            if (index != 0) lastScrollEvent = ScrollEvent(index - 1)
                                        }catch (e:Exception) {
                                            Msg.requireShowLongDuration("#onUpFocus err: "+e.localizedMessage)
                                            MyLog.e(TAG, "#onUpFocus err: "+e.stackTraceToString())
                                        }

                                    },
                                    onDownFocus = {
                                        try {
                                            if (lastScrollEvent != null && lastScrollEvent?.isConsumed != true) return@TextField
                                            editableController.selectNextField()
                                            if (index != textEditorState.fields.lastIndex) lastScrollEvent =
                                                ScrollEvent(index + 1)
                                        }catch (e:Exception) {
                                            Msg.requireShowLongDuration("#onDownFocus err: "+e.localizedMessage)
                                            MyLog.e(TAG, "#onDownFocus err: "+e.stackTraceToString())
                                        }

                                    },
                                )
                            }
                        }
                    }

                    if(mergeMode && curLineText.startsWith(settings.editor.conflictEndStr)) {
                        item {
                            AcceptButtons(
                                lineIndex = index,
                                lineText = curLineText,
                                prepareAcceptBlock = prepareAcceptBlock,
                                conflictOursBlockBgColor = conflictOursBlockBgColor,
                                conflictTheirsBlockBgColor = conflictTheirsBlockBgColor,
                                conflictSplitLineBgColor = conflictSplitLineBgColor
                            )
                        }
                    }

                }

                item {
                    Spacer(modifier = Modifier
                        .width(virtualWidth)
                        //设高度为屏幕高度-50dp，基本上能滚动到顶部，但会留出最后一行多一些的空间
                        .height(virtualHeight)
                        .clickable(
                            //隐藏点击效果（就是一点屏幕明暗变化一下那个效果）
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) {
                            //点击空白区域定位到最后一行最后一个字符后面
                            //第1个参数是行索引；第2个参数是当前行的哪个位置
                            editableController.selectField(
                                textEditorState.fields.lastIndex,
                                EditorController.SelectionOption.LAST_POSITION
                            )
                            keyboardController?.show()  //确保弹出键盘，不加的话“点击空白区域，关闭键盘，再点击空白区域”就不弹出键盘了
                        }
//                    .background(Color.Red)  //debug
                        ,
                    )
//                PaddingRow()
                }
            }
        }

    }
}




fun getNextKeyWordForConflict(curKeyWord:String, settings: AppSettings):String {
    if(curKeyWord == settings.editor.conflictStartStr) {
        return settings.editor.conflictSplitStr
    }else if(curKeyWord == settings.editor.conflictSplitStr) {
        return settings.editor.conflictEndStr
    }else { // curKeyWord == settings.editor.conflictEndStr
        return settings.editor.conflictStartStr
    }
}
fun getPreviousKeyWordForConflict(curKeyWord:String, settings: AppSettings):String {
    if(curKeyWord == settings.editor.conflictStartStr) {
        return settings.editor.conflictEndStr
    }else if(curKeyWord == settings.editor.conflictEndStr) {
        return settings.editor.conflictSplitStr
    }else { // curKeyWord == settings.editor.conflictSplitStr
        return settings.editor.conflictStartStr
    }
}

data class ScrollEvent(val index: Int = -1, val time: Long = Date().time, val forceGo:Boolean = false,
                       val goColumn:Boolean=false, val columnStartIndexInclusive:Int=0, val columnEndIndexExclusive:Int=columnStartIndexInclusive
) {
    var isConsumed: Boolean = false
        private set

    fun consume() {
        isConsumed = true
    }
}

data class SearchPos(var lineIndex:Int=-1, var columnIndex:Int=-1) {
    companion object{
        val NotFound = SearchPos(-1, -1)
    }
}

data class SearchPosResult(val foundPos: SearchPos = SearchPos.NotFound, val nextPos: SearchPos = SearchPos.NotFound) {
    companion object{
        val NotFound = SearchPosResult(foundPos = SearchPos.NotFound, nextPos = SearchPos.NotFound)
    }
}
