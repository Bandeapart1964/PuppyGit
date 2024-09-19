package com.catpuppyapp.puppygit.compose

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Checkbox
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.catpuppyapp.puppygit.play.pro.R
import com.catpuppyapp.puppygit.style.MyStyleKt
import com.catpuppyapp.puppygit.utils.state.StateUtil


@Composable
fun SetUpstreamDialog(
    remoteList:List<String>,  //remote列表
    curBranch:String,  //供显示的，让用户知道在为哪个分支设置上游
    selectedOption: MutableIntState,  //选中的remote在列表中的索引
    branch: MutableState<String>,
    branchSameWithLocal: MutableState<Boolean>,
    onOkText:String="",
    onOk: () -> Unit,
    onCancel: () -> Unit,
) {

    AlertDialog(
        title = {
            Text(stringResource(R.string.set_upstream_title))
        },
        text = {
            Column {
                Row(modifier = Modifier.padding(10.dp)) {
                    Text(text = stringResource(R.string.set_upstream_for_branch)+":")
                }
                Row(modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(text = curBranch,
                        fontWeight = FontWeight.ExtraBold)
                }
                Spacer(modifier = Modifier.height(15.dp))
                //这个文案感觉没太大必要，不显示了，省点空间
//                Row(modifier = Modifier.padding(10.dp)) {
//                    Text(text = stringResource(R.string.set_upstream_text))
//                }
                Row(modifier = Modifier.padding(10.dp)) {
                    Text(text = stringResource(R.string.select_a_remote)+":")
                }
                //下拉列表，弹窗显示这个会崩溃，日后不崩溃了再启用
            //SingleSelectList(optionsList = optionsList, selectedOption = selectedOption)

                if(remoteList.isEmpty()){  //remotelist为空，显示提示，同时应禁用ok按钮
                    Text(text = stringResource(R.string.err_remote_list_is_empty),
                        color = MyStyleKt.TextColor.error)
                }else{
                    MyLazyColumn(
                        modifier = Modifier.heightIn(max=150.dp),
                        requireUseParamModifier = true,
                        contentPadding = PaddingValues(0.dp),
                        list = remoteList,
                        listState = StateUtil.getRememberLazyListState(),
                        requireForEachWithIndex = true,
                        requirePaddingAtBottom =false
                    ) {k, optext ->
                        Row(
                            Modifier
                                .fillMaxWidth()
                                .heightIn(min = MyStyleKt.RadioOptions.minHeight)

                                .selectable(
                                    selected = selectedOption.intValue == k,
                                    onClick = {
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

                    enabled = !branchSameWithLocal.value,
                    value = branch.value,
                    singleLine = true,
                    onValueChange = {
                        branch.value = it
                    },
                    label = {
                        Text(stringResource(R.string.set_upstream_branch_name))
                    },
                    placeholder = {
                        Text(stringResource(R.string.upstream_branch_name))
                    }
                )
                Row(
                    Modifier
                        .fillMaxWidth()
                        .height(MyStyleKt.CheckoutBox.height)
                        .toggleable(
                            enabled = true,
                            value = branchSameWithLocal.value,
                            onValueChange = {
                                branchSameWithLocal.value = !branchSameWithLocal.value
                            },
                            role = Role.Checkbox
                        )
                        .padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = branchSameWithLocal.value,
                        onCheckedChange = null // null recommended for accessibility with screenreaders
                    )
                    Text(
                        text = stringResource(R.string.same_with_local),
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.padding(start = 16.dp),
                    )
                }

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
                //如果没勾选上游使用和本地同名分支且上游分支引用为空（没填或删了默认的，就会空），返回假，没设计成留空自动生成，所以，必须要填个分支
                enabled = (!(!branchSameWithLocal.value && branch.value.isBlank())) && remoteList.isNotEmpty(),
                ) {
                if(onOkText.isBlank()) {
                    Text(stringResource(R.string.save))
                }else {
                    Text(text = onOkText)
                }
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

