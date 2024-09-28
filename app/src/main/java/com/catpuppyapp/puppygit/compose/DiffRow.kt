package com.catpuppyapp.puppygit.compose

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import com.catpuppyapp.puppygit.constants.Cons
import com.catpuppyapp.puppygit.constants.LineNum
import com.catpuppyapp.puppygit.git.PuppyLine
import com.catpuppyapp.puppygit.style.MyStyleKt
import com.catpuppyapp.puppygit.ui.theme.Theme
import com.catpuppyapp.puppygit.utils.AppModel
import com.catpuppyapp.puppygit.utils.Libgit2Helper
import com.catpuppyapp.puppygit.utils.cache.Cache
import com.catpuppyapp.puppygit.utils.compare.result.IndexStringPart

/**
 * @param stringPartList 如果用不到，可传null或使用默认值（还是null）
 */
@Composable
fun DiffRow (line:PuppyLine, stringPartList:List<IndexStringPart>? = null, fileFullPath:String) {
    val useStringPartList = !stringPartList.isNullOrEmpty()

    val navController = AppModel.singleInstanceHolder.navController

    val inDarkTheme = Theme.inDarkTheme
    //libgit2会把连续行整合到一起，这里用getLines()获取拆分后的行
//                    puppyLineBase.getLines().forEach { line ->
    val color = Libgit2Helper.getDiffLineBgColor(line, inDarkTheme)
    val textColor = Libgit2Helper.getDiffLineTextColor(line, inDarkTheme)
//                        val lineTypeStr = getDiffLineTypeStr(line)
    val lineNumColor = if (inDarkTheme) MyStyleKt.TextColor.lineNum_forDiffInDarkTheme else MyStyleKt.TextColor.lineNum_forDiffInLightTheme

    val lineNum = if(line.lineNum== LineNum.EOF.LINE_NUM) LineNum.EOF.TEXT else line.lineNum.toString()
//    var prefix = ""
    val content = line.content
    //我发现明明新旧都没末尾行，但是originType却是添加了末尾行 '>'， 很奇怪，所以把行相关的背景颜色改了，文字颜色一律灰色，另外，因为patch输出会包含 no new line at end 之类的东西，所以不需要我再特意添加那句话了
    //只显示新增换行符、删除换行符、新旧文件都没换行符、新增行、删除行、上下文
//                    if (line.originType == Diff.Line.OriginType.DEL_EOFNL.toString() || line.originType == Diff.Line.OriginType.CONTEXT_EOFNL.toString()) { //新文件删除了换行符 和 新旧都没换行符
////                            prefix=line.originType+ ":"
////                            content = stringResource(R.string.no_new_line_at_end)  //不赋值的话，content什么都没有，用户看到的就是个带箭头(代表没新行的originType)的红色空行
//                    } else if (line.originType == Diff.Line.OriginType.ADD_EOFNL.toString()) {  //新增换行符
////                            prefix=line.originType+ ":"
//
//                    } else

//    prefix = line.originType + lineNum + ":"  // show add or del and line num, e.g. "+123:" or "-123:"
    val prefix = "$lineNum:"  // only show line num (can use color figure add or del), e.g. "123:"


    //因为下面用Row换行了，所以不需要内容以换行符结尾
//    prefix = prefix.removeSuffix("\n")
//    content = content.removeSuffix("\n")

    Row(
        modifier = Modifier
            .fillMaxWidth()
            //如果是经过compare的添加或删除行，背景半透明，然后真修改的内容用不透明，这样就能突出真修改的内容
            .background(if(useStringPartList) color.copy(alpha = 0.6f) else color)
//            .background(color)
//                            .clickable {
//
//                                //没卵用，这东西只会让人看了头疼，还不如加个点击弹出菜单可以让用户复制行之类的东西
//                                //显示新旧行号
////                                    showToast(
////                                        appContext,
////                                        "$lineTypeStr,$oldLineAt: ${line.oldLineNum},$newLineAt: ${line.newLineNum}",
////                                        Toast.LENGTH_SHORT
////                                    )
//                            },
    ) {
        //show add/del and line number, e.g. +123, or only show line num e.g. 123, it should make a settings item for it
        Text(
            text = prefix,
            color = lineNumColor,
            fontSize = MyStyleKt.TextSize.lineNumSize,
            modifier = Modifier.clickable {
                val filePathKey = Cache.setThenReturnKey(fileFullPath)
                //if jump line is EOF, should go to last line of file, but didn't know the line num, so set line num to a enough big number
                val goToLine = if(lineNum == LineNum.EOF.TEXT) LineNum.EOF.LINE_NUM else lineNum
                val initMergeMode = "0"  //能进diff页面说明没冲突，所以mergemode设为0
                val initReadOnly = "0"  //app内置目录下的文件不可能在diff页面显示，所以在这把readonly设为0即可
                navController.navigate(Cons.nav_SubPageEditor + "/$filePathKey"+"/$goToLine"+"/$initMergeMode"+"/$initReadOnly")
            }
        )

        if(useStringPartList) {
            //StringPart是比较过后的解析出哪些部分是真修改，哪些不是的一个数组，每个元素都包含完整字符串一部分，按序拼接即可得到原字符串
            val lastIndex = stringPartList!!.lastIndex  //用来判断，最后一个条目，需要移除末尾换行符

            //注意：这里不能改成用多个Text组件，不然若超过屏幕宽度软换行会失效
            Text(
                text = buildAnnotatedString {
                    stringPartList.forEachIndexed {idx, it ->
                        val text = content.substring(it.start, it.end)
                        //末尾会有个换行符，移除下，不然显示会多个行
                        val textNoLineSeparator = if(idx == lastIndex) text.removeSuffix("\n") else text

                        if(it.modified) {  //为修改的内容设置高亮颜色
                            withStyle(style = SpanStyle(background = color)) {
                                append(textNoLineSeparator)
                            }
                        }else {  //没修改的内容不用设置颜色
                            append(textNoLineSeparator)
                        }
                    }
                },

                color = textColor,
                overflow = TextOverflow.Visible,
                softWrap = true
            )
        }else {
            //文本内容
            Text(
                text = content.removeSuffix("\n"),
                color = textColor,
                overflow = TextOverflow.Visible,
                softWrap = true
            )

        }
    }

}
