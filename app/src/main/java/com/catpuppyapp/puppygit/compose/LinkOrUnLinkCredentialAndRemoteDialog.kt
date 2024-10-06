package com.catpuppyapp.puppygit.compose

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.res.stringResource
import com.catpuppyapp.puppygit.data.entity.CredentialEntity
import com.catpuppyapp.puppygit.dto.RemoteDtoForCredential
import com.catpuppyapp.puppygit.play.pro.R
import com.catpuppyapp.puppygit.utils.AppModel
import com.catpuppyapp.puppygit.utils.doJobThenOffLoading
import com.catpuppyapp.puppygit.utils.state.CustomStateSaveable
import com.catpuppyapp.puppygit.utils.state.StateUtil


@Composable
fun LinkOrUnLinkCredentialAndRemoteDialog(
    curItemInPage:CustomStateSaveable<CredentialEntity>,
    requireDoLink:Boolean, // true require do link, else require do unlink
    targetAll:Boolean,
    title:String,
    thisItem: RemoteDtoForCredential,
    onCancel: () -> Unit,
    onErrCallback:suspend (err:Exception)->Unit,
    onFinallyCallback:()->Unit,
    onOkCallback:()->Unit,
) {
    val appContext = AppModel.singleInstanceHolder.appContext

    val fetchChecked = rememberSaveable { mutableStateOf(if(targetAll) true else if(requireDoLink) thisItem.credentialId!=curItemInPage.value.id else thisItem.credentialId==curItemInPage.value.id)}
    val pushChecked = rememberSaveable { mutableStateOf(if(targetAll) true else if(requireDoLink) thisItem.pushCredentialId!=curItemInPage.value.id else thisItem.pushCredentialId==curItemInPage.value.id)}


    AlertDialog(
        title = {
            Text(if(title.isEmpty()) { if(requireDoLink) stringResource(R.string.link) else stringResource(R.string.unlink)} else {title})
        },
        text = {
            Column {
                MyCheckBox(text = stringResource(R.string.fetch), value = fetchChecked)
                MyCheckBox(text = stringResource(R.string.push), value = pushChecked)
            }

        },
        onDismissRequest = {
            onCancel()
        },
        confirmButton = {
            TextButton(
                enabled = fetchChecked.value || pushChecked.value,  //at least checked 1, else dont enable

                onClick = onOk@{
                    if(!fetchChecked.value && !pushChecked.value) {
                        return@onOk
                    }

                    val remoteId = thisItem.remoteId
                    val curCredentialId = curItemInPage.value.id
                    val remoteDb = AppModel.singleInstanceHolder.dbContainer.remoteRepository
                    doJobThenOffLoading {
                        try {
                            if(requireDoLink) {  //link
                                //TODO 目前只实现了link single item，但实际上也可实现link all，sql写成类似`update remote set credentialId = :curCrendentialId, pushCredentialId = :curCredentialId` 就行了，日后可考虑实现
                                if(fetchChecked.value && pushChecked.value) {
                                    remoteDb.updateFetchAndPushCredentialIdByRemoteId(remoteId, curCredentialId, curCredentialId)
                                }else if(fetchChecked.value) {
                                    remoteDb.updateCredentialIdByRemoteId(remoteId, curCredentialId)
                                }else {  //pushChecked.value is true
                                    remoteDb.updatePushCredentialIdByRemoteId(remoteId, curCredentialId)
                                }
                            }else {  // unlink
                                val emptyId = ""
                                if(targetAll) {  // unlink all by current credentialId
                                    if(fetchChecked.value && pushChecked.value) {
                                        remoteDb.updateFetchAndPushCredentialIdByCredentialId(curCredentialId, curCredentialId, emptyId, emptyId)
                                    }else if(fetchChecked.value) {
                                        remoteDb.updateCredentialIdByCredentialId(curCredentialId, emptyId)
                                    }else {  //pushChecked.value is true
                                        remoteDb.updatePushCredentialIdByCredentialId(curCredentialId, emptyId)
                                    }
                                }else {  //unlink single item
                                    if(fetchChecked.value && pushChecked.value) {
                                        remoteDb.updateFetchAndPushCredentialIdByRemoteId(remoteId, emptyId, emptyId)
                                    }else if(fetchChecked.value) {
                                        remoteDb.updateCredentialIdByRemoteId(remoteId, emptyId)
                                    }else {  //pushChecked.value is true
                                        remoteDb.updatePushCredentialIdByRemoteId(remoteId, emptyId)
                                    }
                                }
                            }

                            onOkCallback()
                        }catch (e:Exception){
                            onErrCallback(e)
                        }finally {
                            onFinallyCallback()
                        }
                    }
                }
            ) {
                Text(stringResource(id = R.string.ok))
            }
        },
        dismissButton = {
            TextButton(
                onClick = {
                    onCancel()
                }
            ) {
                Text(stringResource(id = R.string.cancel))
            }
        }
    )

}

