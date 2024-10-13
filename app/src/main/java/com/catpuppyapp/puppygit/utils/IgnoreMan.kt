package com.catpuppyapp.puppygit.utils

import java.io.File
import java.io.FileWriter

/**
 * app specified ignore files manager, it under every repo's ".git/PuppyGit/ignores.txt"
 *
 * usage:
 *   1 call `getAllValidPattern()` get a rules list
 *   2 for each file path call `matchedPatternList(input, rules)`, if return true, means should be ignore
 */
object IgnoreMan {
    private const val commentBegin = "//"
    private const val newFileContent = "$commentBegin a line start with \"$commentBegin\" will treat as comment\n$commentBegin each line one relative path under repo, support simple wildcard like *.log match all files has .log suffix\n\n"
    private const val fileName = "ignore_v2.txt"

    private fun getFile(repoDotGitDir: String): File {
        val f = File(AppModel.PuppyGitUnderGitDirManager.getDir(repoDotGitDir).canonicalPath, fileName)
        if(!f.exists()){
            f.createNewFile()
            f.bufferedWriter().use {
                it.write(newFileContent)
            }
        }

        return f
    }

    fun getAllValidPattern(repoDotGitDir: String):List<String> {
        val f = getFile(repoDotGitDir)
        val retList = mutableListOf<String>()
        val br = f.bufferedReader()
        var rline = br.readLine()
        while (rline!=null){
            if(isValidLine(rline)){
                retList.add(rline)
            }

            rline = br.readLine()
        }

        return retList
    }

    fun matchedPatternList(input:String, patternList:List<String>):Boolean {
        if(patternList.isEmpty()) {
            return false
        }

        for(i in patternList.indices) {
            if(RegexUtil.matchForIgnoreFile(input, patternList[i])) {
                return true
            }
        }

        return false
    }

    private fun isComment(str:String):Boolean {
        return str.trimStart().startsWith(commentBegin)
    }

    private fun isValidLine(line:String):Boolean {
        return line.isNotEmpty() && !isComment(line)
    }

    fun getFileFullPath(repoDotGitDir: String):String {
        return getFile(repoDotGitDir).canonicalPath
    }

    fun appendLinesToIgnoreFile(repoDotGitDir: String, lines:List<String>) {
        if(lines.isEmpty()) {
            return
        }
        val curTime = getNowInSecFormatted()
        val ignoreFile = getFile(repoDotGitDir)
        val append = true
        val filerWriter = FileWriter(ignoreFile, append)
        filerWriter.buffered().use { writer ->
            // if no this head line and file was not ends with new line, content will concat as unexpected
            writer.write("\n$commentBegin $curTime\n")  // newLine + timestamp
            lines.forEach { ln ->
                writer.write(ln+"\n")
            }
        }
    }

}
