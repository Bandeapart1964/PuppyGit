package com.catpuppyapp.puppygit.data.entity.common

import com.catpuppyapp.puppygit.constants.Cons
import com.catpuppyapp.puppygit.utils.getSecFromTime

data class BaseFields (
    // base 相关的字段是表的基本字段
    var baseStatus:Int=Cons.dbCommonBaseStatusOk,  //直接用字符来表示状态
    // data row create and update time
    var baseCreateTime:Long=getSecFromTime(),
    var baseUpdateTime:Long=getSecFromTime(),
    var baseIsDel:Int=Cons.dbCommonFalse,
)
