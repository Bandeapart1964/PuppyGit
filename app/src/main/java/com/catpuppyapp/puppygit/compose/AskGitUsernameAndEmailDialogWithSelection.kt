package com.catpuppyapp.puppygit.compose

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableIntState
import androidx.compose.runtime.MutableState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.catpuppyapp.puppygit.play.pro.R
import com.catpuppyapp.puppygit.style.MyStyleKt
import com.catpuppyapp.puppygit.utils.MyLog

private val TAG = "AskGitUsernameAndEmailDialogWithSelection"

@Composable
fun AskGitUsernameAndEmailDialogWithSelection(
    text: String,
    optionsList: List<String>,  //这个列表两个条目，一个代表为全局设置用户名和邮箱，另一个代表为当前仓库设置用户名和邮箱
    selectedOption: MutableIntState,  //代表选中了optionsList中的哪个条目
    username: MutableState<String>,
    email: MutableState<String>,
    onOk: () -> Unit,
    onCancel: () -> Unit,
    enableOk: () -> Boolean,
) {
    AlertDialog(
        title = {
            Text(stringResource(R.string.username_and_email))
        },
        text = {
            Column {
                Row(modifier = Modifier.padding(10.dp)) {
                    Text(text = text)
                }

                Column(
                    modifier = Modifier.selectableGroup(),

                ) {
                    //如果设置了有效gitUrl，显示新建和选择凭据，否则只显示无凭据
                    //key should like: "1"; value should like "1: balbalba"
                    for ((k, optext) in optionsList.toList().withIndex()) {
                        //k=1,v=text, optionNumAndText="1: text"
//                        val optionNumAndText = RadioOptionsUtil.formatOptionKeyAndText(k, v)
                        Row(
                            Modifier
                                .fillMaxWidth()
                                .heightIn(min=MyStyleKt.RadioOptions.minHeight)
                                .selectable(
                                    selected = selectedOption.intValue==k,
                                    onClick = {
                                        MyLog.d(TAG, "#onClick(), selected option, k="+k +", optext="+optext)
                                        //更新选择值
                                        selectedOption.intValue = k
                                    },
                                    role = Role.RadioButton
                                )
                                .padding(horizontal = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = selectedOption.intValue==k,
                                onClick = null // null recommended for accessibility with screenreaders
                            )
                            Text(
                                text = optext,
                                style = MaterialTheme.typography.bodyLarge,
                                modifier = Modifier.padding(start = 10.dp)
                            )
                        }
                    }
                }
                Row(modifier = Modifier.padding(5.dp)) {

                }
                TextField(
                    modifier = Modifier.fillMaxWidth(),

//                            modifier = Modifier.focusRequester(focusRequester),
                    value = username.value,
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

}

