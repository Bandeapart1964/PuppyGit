package com.catpuppyapp.puppygit.compose

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectable
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.catpuppyapp.puppygit.play.pro.R
import com.catpuppyapp.puppygit.constants.Cons
import com.catpuppyapp.puppygit.data.entity.RepoEntity
import com.catpuppyapp.puppygit.style.MyStyleKt
import com.catpuppyapp.puppygit.utils.Libgit2Helper
import com.catpuppyapp.puppygit.utils.Msg
import com.catpuppyapp.puppygit.utils.MyLog
import com.catpuppyapp.puppygit.utils.createAndInsertError
import com.catpuppyapp.puppygit.utils.doJobThenOffLoading
import com.catpuppyapp.puppygit.utils.state.StateUtil
import com.github.git24j.core.Repository

private val TAG = "SetBranchForRemoteDialog"
private val stateKeyTag = "SetBranchForRemoteDialog"

@Composable
fun SetBranchForRemoteDialog(
    curRepo: RepoEntity,
    remoteName: String,
    isAllInitValue: Boolean,
    onCancel: () -> Unit,
    onOk: (remoteName:String, isAll: Boolean, branchCsvStr: String) -> Unit
) {
    val appContext = LocalContext.current
    val isAll = StateUtil.getRememberSaveableState(initValue = isAllInitValue)
//    val branchList = StateUtil.getCustomSaveableStateList(keyTag = stateKeyTag, keyName = "branchList") {
//        listOf<String>()
//    }
    val branchCsvStr = StateUtil.getRememberSaveableState(initValue = "")
    val strListSeparator = Cons.stringListSeparator

    AlertDialog(
        title = {
            Text(text = appContext.getString(R.string.set_branch_mode))
        },
        text = {
            Column {
                Row {
                    Text(text = stringResource(R.string.branch_mode_note))
                }
                Spacer(modifier = Modifier.height(20.dp))


                Row {
                    Text(text = appContext.getString(R.string.text_for) + ": ")
                    Text(text = remoteName,
                        fontWeight = FontWeight.ExtraBold
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    Modifier
                        .fillMaxWidth()
                        .heightIn(min = MyStyleKt.RadioOptions.minHeight)

                        .selectable(
                            selected = isAll.value,
                            onClick = {
                                isAll.value = true
                            },
                            role = Role.RadioButton
                        )
                        .padding(horizontal = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = isAll.value,
                        onClick = null // null recommended for accessibility with screenreaders
                    )
                    Text(
                        text = appContext.getString(R.string.all),
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.padding(start = 10.dp)
                    )
                }
                Row(
                    Modifier
                        .fillMaxWidth()
                        .heightIn(min = MyStyleKt.RadioOptions.minHeight)

                        .selectable(
                            selected = !isAll.value,
                            onClick = {
                                isAll.value = false
                            },
                            role = Role.RadioButton
                        )
                        .padding(horizontal = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = !isAll.value,
                        onClick = null // null recommended for accessibility with screenreaders
                    )
                    Text(
                        text = appContext.getString(R.string.custom),
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.padding(start = 10.dp)
                    )
                }
                if(!isAll.value) {
                    TextField(
                        modifier = Modifier.fillMaxWidth(),

                        value = branchCsvStr.value,
                        onValueChange = {
                            branchCsvStr.value = it
                        },
                        label = {
                            Text(stringResource(R.string.branches_split_by)+" '${strListSeparator}'")
                        },
                        placeholder = {
                            Text(stringResource(R.string.branches_placeholder))
                        }
                    )
                }
            }
        },
        onDismissRequest = { onCancel() },
        dismissButton = {
            TextButton(onClick = { onCancel() }) {
                Text(text = appContext.getString(R.string.cancel))
            }
        },
        confirmButton = {
            TextButton(
                enabled = isAll.value || branchCsvStr.value.isNotBlank(),
                onClick = { onOk(remoteName, isAll.value, branchCsvStr.value) }) {
                Text(text = appContext.getString(R.string.save))
            }
        }
    )


    LaunchedEffect(Unit) {
        doJobThenOffLoading {
            try {
                Repository.open(curRepo.fullSavePath).use { repo ->
                    val remote = Libgit2Helper.resolveRemote(repo, remoteName)
                    if (remote == null) {
                        Msg.requireShowLongDuration(appContext.getString(R.string.err_resolve_remote_failed))
                        return@doJobThenOffLoading
                    }

                    val (isAllRealValue, branchNameList) = Libgit2Helper.getRemoteFetchBranchList(remote)

                    //更新状态变量
                    isAll.value = isAllRealValue

                    if (isAll.value) {
                        branchCsvStr.value = ""
                    } else {
                        val sb = StringBuilder()
                        branchNameList.forEach {
                            sb.append(it).append(strListSeparator)
                        }
                        branchCsvStr.value = sb.removeSuffix(strListSeparator).toString()
                    }

//                    branchList.value.clear()
//                    branchList.value.addAll(refspecList)
                }

            } catch (e: Exception) {
                Msg.requireShowLongDuration("err:" + e.localizedMessage)
                createAndInsertError(curRepo.id, "err:${e.localizedMessage}")
                MyLog.e(TAG, "#LaunchedEffect: err: ${e.stackTraceToString()}")
            }
        }
    }
}
