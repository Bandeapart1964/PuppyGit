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

    var diffContentSizeMaxLimit:Long = 0L,  // 0=no limit, unit is Byte, e.g. 1MB should set to 1000000L,
)
