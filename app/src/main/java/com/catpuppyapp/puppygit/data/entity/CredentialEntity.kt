package com.catpuppyapp.puppygit.data.entity

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.catpuppyapp.puppygit.constants.Cons
import com.catpuppyapp.puppygit.data.entity.common.BaseFields
import com.catpuppyapp.puppygit.play.pro.R
import com.catpuppyapp.puppygit.utils.AppModel
import com.catpuppyapp.puppygit.utils.getShortUUID

@Entity(tableName = "credential")
data class CredentialEntity(
    @PrimaryKey
    var id: String = getShortUUID(),
    var name:String = "",  //字段需唯一
//    var testMigra:String="",

    // repo push or pull etc time
    var value: String = "",  // username or private key
    var pass: String = "",  // password or passphrase_for_ssh
    var type: Int= Cons.dbCredentialTypeHttp,  //"ssh" or "http"

    @Embedded
    var baseFields: BaseFields = BaseFields(),

){
    fun getTypeStr():String {
        val appContext = AppModel.singleInstanceHolder.appContext
        return appContext.getString(if(type == Cons.dbCredentialTypeHttp) R.string.http_https else R.string.ssh)
    }

}
