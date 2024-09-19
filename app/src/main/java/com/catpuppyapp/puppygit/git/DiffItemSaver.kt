package com.catpuppyapp.puppygit.git

import com.catpuppyapp.puppygit.constants.Cons
import com.github.git24j.core.Diff
import java.util.EnumSet
import java.util.TreeMap

class DiffItemSaver {
    var from:String= Cons.gitDiffFromIndexToWorktree
//    var fileHeader:String="";  // file好像没有header
    var oldFileOid:String=""
    var newFileOid:String=""
    var newFileSize=0L
    var oldFileSize=0L
    //diff 所有 line.content总和有无超过限制大小
    var isContentSizeOverLimit=false
//    var efficientFileSize=0L
    var flags: EnumSet<Diff.FlagT> = EnumSet.of(Diff.FlagT.NOT_BINARY);
    var hunks:MutableList<PuppyHunkAndLines> = mutableListOf()  //key是hunk的指针地址

    //指示文件是否修改过，因为有时候会错误的diff没修改过的文件，所以需要判断下
    var isFileModified:Boolean=false


    //获取实际生效的文件大小
    //ps:如果想判断文件大小有无超过限制，用此方法返回值作为 isFileSizeOverLimit() 的入参做判断即可
    fun getEfficientFileSize():Long {
        return if(newFileSize>0) newFileSize else oldFileSize
    }
}

class PuppyHunkAndLines {
    var hunk:PuppyHunk=PuppyHunk();
    var lines:MutableList<PuppyLine> = mutableListOf()


    //根据行号分组
    //{lineNum: {originType:line}}, 其中 originType预期有3种类型：context/del/add
    var groupedLines:TreeMap<Int, HashMap<String, PuppyLine>> = TreeMap()

    fun addLineToGroup(puppyLine: PuppyLine) {
        val lineNum = puppyLine.lineNum
        val line = groupedLines.get(lineNum)
        if(line==null) {
            val map = HashMap<String, PuppyLine>()
            map.put(puppyLine.originType, puppyLine)
            groupedLines.put(lineNum, map)
        }else {
            line.put(puppyLine.originType, puppyLine)
        }
    }
}

class PuppyHunk {
    var header:String=""
}

data class PuppyLine (
    var originType:String="",  //这个当初实现的时候考虑不周，既然原始类型是char我为什么要用String存呢？
    var oldLineNum:Int=-1,
    var newLineNum:Int=-1,
    var contentLen:Int=0,
    var content:String="",  //根据字节数分割后的内容
//    var rawContent:String="";  //原始内容
    var lineNum:Int=1,  // getLineNum(PuppyLine) 的返回值，实际的行号
    var howManyLines:Int=0  // content里有多少行
//    private val lines:MutableList<PuppyLine> = mutableListOf();  //针对content处理，把content的每个行都存成了一个PuppyLine（content默认只有多个连续行的第一个行有行号
//
//    /**
//     * 由于libgit2会把多个连续行合并成一个，只有第一个显示行号，所以需要处理下
//     * 注意：这个方法的正确性有待验证。
//     */
//    fun getLines():MutableList<PuppyLine> {
//        if (lines.isNotEmpty()) {
//            return lines
//        }
//        //TODO 把这个diff整清楚
//        if(debugModeOn) {
//            println("startLine=${lineNum}，多少行："+howManyLines)
//            println("content分割出多少行:"+content.lines().size)
//        }
//        lines.add(this)
////
////        //只处理 上下文（白色）和 新增（绿色）和 删除（红色）三种类型，其他的比如添加删除文件末尾行之类的不做处理
////        if(originType != Diff.Line.OriginType.CONTEXT.toString()
////            && originType != Diff.Line.OriginType.ADDITION.toString()
////            && originType!=Diff.Line.OriginType.DELETION.toString()
////        ) {
////            lines.add(this)
////        }else {
////            var cnt = lineNum
//////        println(content.lines().size-1)
////            val cls = content.lines()
////            for((idx, it) in cls.withIndex()) {
////                //多余的行。判断条件为“最后一行且为空”，可能是 lines()函数返回的空行，比如 abc\n，lines() 会返回两行，但有时候这和函数又不会返回多余的行，我凌乱了，目前先这样吧
////                if(idx==cls.size-1 && it.isEmpty()){
////                    break
////                }
////                val p = PuppyLine()
////                p.originType = originType
////                p.lineNum=cnt++
////                p.content = it
////                lines.add(p)
////            }
////        }
//
//        return lines
//    }

)
