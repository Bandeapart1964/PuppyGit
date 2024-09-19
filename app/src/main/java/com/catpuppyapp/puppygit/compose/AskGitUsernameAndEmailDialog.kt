package com.catpuppyapp.puppygit.compose

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.catpuppyapp.puppygit.play.pro.R
import com.catpuppyapp.puppygit.data.entity.RepoEntity
import com.catpuppyapp.puppygit.utils.AppModel
import com.catpuppyapp.puppygit.utils.state.CustomStateSaveable
import com.catpuppyapp.puppygit.utils.Libgit2Helper
import com.catpuppyapp.puppygit.utils.doJobThenOffLoading
import com.github.git24j.core.Repository


@Composable
fun AskGitUsernameAndEmailDialog(
    title: String,
    text: String,
    username: MutableState<String>,
    email: MutableState<String>,
    isForGlobal: Boolean,
    curRepo: CustomStateSaveable<RepoEntity>,
    onOk: () -> Unit,
    onCancel: () -> Unit,
    enableOk: () -> Boolean,
) {

    val appContext = AppModel.singleInstanceHolder.appContext

    AlertDialog(
        title = {
            Text(title)
        },
        text = {
            Column {
                Row(modifier = Modifier.padding(10.dp)) {
                    Text(text = text)
                }
                TextField(
                    modifier = Modifier.fillMaxWidth(),

                    value = username.value,
                    singleLine = true,
                    onValueChange = {
                        username.value = it
                    },
                    label = {
                        Text(stringResource(R.string.username))
                    },
                    placeholder = {
                        Text(stringResource(R.string.username))
                    }
                )
                Row(modifier = Modifier.padding(5.dp)) {

                }
                TextField(
                    modifier = Modifier.fillMaxWidth(),

                    value = email.value,
                    singleLine = true,
                    onValueChange = {
                        email.value = it
                    },
                    label = {
                        Text(stringResource(R.string.email))
                    },
                    placeholder = {
                        Text(stringResource(R.string.email))
                    }
                )
            }

        },
        onDismissRequest = {
            onCancel()
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onOk()
                },
                enabled = enableOk()
            ) {
                Text(stringResource(id = R.string.save))
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

    LaunchedEffect(Unit) {
        //从配置文件读取设置
        doJobThenOffLoading(
            loadingOn = {},
            loadingOff = {},
            loadingText=appContext.getString(R.string.loading)
        ) {
            //如果是全局就读取全局的email和username，否则读取仓库的
            if (isForGlobal) {
                val (u, e) = Libgit2Helper.getGitUsernameAndEmailFromGlobalConfig()
                username.value = u
                email.value = e

            } else if (curRepo.value.fullSavePath.isNotBlank()) {
                Repository.open(curRepo.value.fullSavePath).use { repo ->
                    val (u, e) = Libgit2Helper.getGitUserNameAndEmailFromRepo(repo)
                    username.value = u
                    email.value = e
                }
            }
        }
    }
}

