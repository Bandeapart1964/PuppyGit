package com.catpuppyapp.puppygit.compose

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Error
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.catpuppyapp.puppygit.constants.Cons
import com.catpuppyapp.puppygit.play.pro.R
import com.catpuppyapp.puppygit.style.MyStyleKt
import com.catpuppyapp.puppygit.utils.state.StateUtil


@Composable
fun CreateFileOrFolderDialog(
    cancelBtnText: String = stringResource(R.string.cancel),
    okBtnText: String = stringResource(R.string.ok),
    cancelTextColor: Color = Color.Unspecified,
    okTextColor: Color = Color.Unspecified,
    errMsg: MutableState<String>,
    onCancel: () -> Unit,
    onOk: (String, Int) -> Boolean,
) {
    val appContext = LocalContext.current
    val fileName = rememberSaveable { mutableStateOf("")}
    val fileTypeOptions =
        listOf(stringResource(R.string.file), stringResource(R.string.folder))  // idx: 0 1
    val (selectedFileTypeOption, onFileTypeOptionSelected) = rememberSaveable{mutableIntStateOf(0)}

    val doCreate = doCreate@{
        val fileType = if (selectedFileTypeOption == 0) Cons.fileTypeFile else Cons.fileTypeFolder
        //执行用户传入的callback
        val createSuccess = onOk(fileName.value, fileType)
        if(createSuccess) {
            //关闭对话框
            onCancel()
        }

    }

    val hasErr = {
        errMsg.value.isNotEmpty()
    }

    AlertDialog(
        title = {
            Text(stringResource(R.string.create))
        },
        text = {
            Column {
                TextField(
                    modifier = Modifier.fillMaxWidth(),

                    value = fileName.value,
                    singleLine = true,
                    onValueChange = {
                        //一修改就清空错误信息，然后点创建的时候会再检测，若有错误会再设置上
                        errMsg.value = ""

                        fileName.value = it
                    },
                    isError = hasErr(),
                    supportingText = {
                        if (hasErr()) {
                            Text(
                                modifier = Modifier.fillMaxWidth(),
                                text = errMsg.value,
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    },
                    trailingIcon = {
                        if (hasErr()) {
                            Icon(imageVector= Icons.Filled.Error,
                                contentDescription=errMsg.value,
                                tint = MaterialTheme.colorScheme.error)
                        }
                    },
                    label = {
                        Text(stringResource(R.string.file_or_folder_name))
                    },
                    placeholder = {
//                    Text(stringResource(R.string.file_name_placeholder))
                    },
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(onDone = {
                        doCreate()
                    })
                )

                Spacer(modifier = Modifier.padding(15.dp))
                for ((idx, optext) in fileTypeOptions.toList().withIndex()) {
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .heightIn(min = MyStyleKt.RadioOptions.minHeight)

                            .selectable(
                                selected = (selectedFileTypeOption == idx),
                                onClick = {
                                    //更新选择值
                                    onFileTypeOptionSelected(idx)
                                },
                                role = Role.RadioButton
                            )
                            .padding(horizontal = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = (selectedFileTypeOption == idx),
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
        },
        //点击弹框外区域的时候触发此方法，一般设为和OnCancel一样的行为即可
        onDismissRequest = onCancel,
        dismissButton = {
            TextButton(
                onClick = onCancel
            ) {
                Text(
                    text = cancelBtnText,
                    color = cancelTextColor,
                )
            }
        },
        confirmButton = {
            TextButton(
                enabled = fileName.value.isNotEmpty() && !hasErr(),
                onClick = {
                    doCreate()
                },
            ) {
                Text(
                    text = okBtnText,
                    color = okTextColor,
                )
            }
        },

        )

}
