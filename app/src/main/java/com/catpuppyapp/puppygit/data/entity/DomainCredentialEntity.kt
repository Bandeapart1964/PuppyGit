package com.catpuppyapp.puppygit.data.entity

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.catpuppyapp.puppygit.data.entity.common.BaseFields
import com.catpuppyapp.puppygit.utils.getShortUUID

@Entity(tableName = "domain_credential")
data class DomainCredentialEntity (
        @PrimaryKey
        var id: String= getShortUUID(),

        var domain:String="",
        var credentialId:String="",


        @Embedded
        var baseFields: BaseFields = BaseFields(),
)
