package com.catpuppyapp.puppygit.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.catpuppyapp.puppygit.utils.encrypt.PassEncryptHelper

@Entity(tableName = "passEncrypt")
data class PassEncryptEntity (
    @PrimaryKey
    var id: Int= 1,

    var ver: Int= PassEncryptHelper.passEncryptCurrentVer,
    var reserve1:String="",  //保留字段1
    var reserve2:String="",  //保留字段2
    )
