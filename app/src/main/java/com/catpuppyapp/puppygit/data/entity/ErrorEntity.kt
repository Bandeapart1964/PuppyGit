package com.catpuppyapp.puppygit.data.entity

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.catpuppyapp.puppygit.constants.Cons
import com.catpuppyapp.puppygit.data.entity.common.BaseFields
import com.catpuppyapp.puppygit.utils.getShortUUID

@Entity(tableName = "error")
data class ErrorEntity (
        @PrimaryKey
        var id: String= getShortUUID(),
        var date: String="",
        var msg: String="",
        var repoId:String="",
        var isChecked:Int= Cons.dbCommonFalse,

        @Embedded
        var baseFields: BaseFields = BaseFields(),

        )