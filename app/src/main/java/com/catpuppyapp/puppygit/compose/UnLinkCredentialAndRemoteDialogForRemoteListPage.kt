package com.catpuppyapp.puppygit.compose

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import com.catpuppyapp.puppygit.play.pro.R
import com.catpuppyapp.puppygit.utils.AppModel
import com.catpuppyapp.puppygit.utils.doJobThenOffLoading
import com.catpuppyapp.puppygit.utils.state.StateUtil


@Composable
fun UnLinkCredentialAndRemoteDialogForRemoteListPage(
    remoteId:String,
    remoteName:String,
    onCancel: () -> Unit,
    onOkCallback:()->Unit,
) {
    val appContext = AppModel.singleInstanceHolder.appContext

    val fetchChecked = StateUtil.getRememberSaveableState(initValue = true)
    val pushChecked = StateUtil.getRememberSaveableState(initValue = true)

    AlertDialog(
        title = {
            Text(stringResource(R.string.unlink))
        },
        text = {
            Column {
                Row{
                    Text(text = stringResource(id = R.string.text_for)+": ")
                    Text(text = remoteName,
                        fontWeight = FontWeight.ExtraBold,
                        overflow = TextOverflow.Visible
                    )
                }
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

                    val remoteDb = AppModel.singleInstanceHolder.dbContainer.remoteRepository
                    val emptyId = ""

                    doJobThenOffLoading {
                        try {
                            if(fetchChecked.value && pushChecked.value) {
                                remoteDb.updateFetchAndPushCredentialIdByRemoteId(remoteId, emptyId, emptyId)
                            }else if(fetchChecked.value) {
                                remoteDb.updateCredentialIdByRemoteId(remoteId, emptyId)
                            }else {  //pushChecked.value is true
                                remoteDb.updatePushCredentialIdByRemoteId(remoteId, emptyId)
                            }

                        }finally {
                            onOkCallback()
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

