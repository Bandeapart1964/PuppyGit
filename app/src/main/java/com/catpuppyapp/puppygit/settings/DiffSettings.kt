package com.catpuppyapp.puppygit.settings

import kotlinx.serialization.Serializable

@Serializable
data class DiffSettings (
    /**
    if ture, will show delLine and addLine closer, else, maybe will split
     * e.g.
     * if true, show:
     * -1 abc1
     * +1 abc2
     * -2 def3
     * +2 def4
     *
     * if false, show:
     * -1 abc1
     * -2 def3
     * +1 abc2
     * +2 def4
     */
    var groupDiffContentByLineNum:Boolean = true,

    var diffContentSizeMaxLimit:Long = 1000000L,  // default 1MB, unit Byte
)
