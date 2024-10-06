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
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.catpuppyapp.puppygit.play.pro.R
import com.catpuppyapp.puppygit.style.MyStyleKt
import com.catpuppyapp.puppygit.utils.AppModel
import com.catpuppyapp.puppygit.utils.Libgit2Helper
import com.catpuppyapp.puppygit.utils.Msg
import com.catpuppyapp.puppygit.utils.createAndInsertError
import com.catpuppyapp.puppygit.utils.doJobThenOffLoading
import com.catpuppyapp.puppygit.utils.state.StateUtil
import com.github.git24j.core.Repository
import com.github.git24j.core.Reset

@Composable
fun ResetDialog(
    fullOidOrBranchOrTag: MutableState<String>?,  // if null, will not show text field for input hash
    repoFullPath:String,
    repoId:String,
    closeDialog: () -> Unit,
    onOk:((resetType:Reset.ResetT)->Unit)? =null,  // here only passing reset type, the target hash can get from `fullOidOrBranchOrTag`, it's passed by caller, so caller can get it, no need passing at here
    refreshPage: (oldHeadCommitOid:String, isDetached:Boolean)->Unit,  //入参为Hard Reset之前HEAD指向的commit id和仓库是否detached。这个detached在这只是顺手判断，commitList页面要用到这个参数，但那个页面的curRepo若是从分支页面进入，则很少更新，所以，在这顺便更新下detached状态以尽量确保那个对象能持有准确的状态
) {

    val appContext = AppModel.singleInstanceHolder.appContext

    val optSoft = 0  // only HEAD
    val optMixed = 1  // HEAD+Index
    val optHard = 2  // HEAD+Index+Worktree
    val optDefault = optSoft  //默认选中创建分支，detach head如果没reflog，有可能丢数据
    val optList = listOf(
        appContext.getString(R.string.soft),
        appContext.getString(R.string.mixed),
        appContext.getString(R.string.hard),

    )

    val selectedOpt = rememberSaveable{mutableIntStateOf(optDefault)}

    AlertDialog(
        title = {
            Text(text = stringResource(id = R.string.reset))
        },
        text = {
            Column {
                if(fullOidOrBranchOrTag!=null) {
                    TextField(
                        modifier = Modifier.fillMaxWidth(),

                        value = fullOidOrBranchOrTag.value,
                        singleLine = true,
                        onValueChange = {
                            fullOidOrBranchOrTag.value = it
                        },
                        label = {
                            Text(stringResource(R.string.hash_branch_tag))
                        },
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                }

                for ((k, optext) in optList.withIndex()) {
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .heightIn(min = MyStyleKt.RadioOptions.minHeight)

                            .selectable(
                                selected = selectedOpt.intValue == k,
                                onClick = {
                                    //更新选择值
                                    selectedOpt.intValue = k
                                },
                                role = Role.RadioButton
                            )
                            .padding(horizontal = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = selectedOpt.intValue == k,
                            onClick = null // null recommended for accessibility with screenreaders
                        )
                        Text(
                            text = optext,
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier.padding(start = 10.dp)
                        )
                    }

                    val text = if(k== optSoft) {
                        stringResource(R.string.reset_head)
                    }else if(k==optMixed) {
                        stringResource(R.string.reset_head_index)
                    }else if(k==optHard){
                        stringResource(R.string.reset_head_index_worktree)
                    } else {
                        ""
                    }

                    Text(text,
                        fontWeight = FontWeight.Light,
                        modifier = Modifier.padding(horizontal = MyStyleKt.CheckoutBox.horizontalPadding)
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                }

            }
        },
        //点击弹框外区域的时候触发此方法，一般设为和OnCancel一样的行为即可
        onDismissRequest = { closeDialog() },
        dismissButton = {
            TextButton(
                onClick = { closeDialog() }
            ) {
                Text(
                    text = stringResource(id = R.string.cancel),
                )
            }
        },
        confirmButton = {
            TextButton(
                enabled = fullOidOrBranchOrTag==null || fullOidOrBranchOrTag.value.isNotBlank(),
                onClick = {
                    if(onOk==null) {

                        //关闭弹窗
                        closeDialog()
                        //执行 reset
                        doJobThenOffLoading job@{
                            Repository.open(repoFullPath).use { repo ->
                                val oldHead = Libgit2Helper.resolveHEAD(repo)
                                val type = if(selectedOpt.intValue == optSoft) Reset.ResetT.SOFT else if(selectedOpt.intValue == optMixed) Reset.ResetT.MIXED else Reset.ResetT.HARD
                                val commitRet = Libgit2Helper.resolveCommitByHashOrRef(repo, fullOidOrBranchOrTag?.value ?: "")

                                if(commitRet.hasError()) {
                                    Msg.requireShowLongDuration(commitRet.msg)
                                    createAndInsertError(repoId, "Reset $type err, resolve commit failed:"+commitRet.msg)
                                    return@job
                                }

                                val commit = commitRet.data!!

                                val ret = Libgit2Helper.resetToRevspec(repo, commit.id().toString(), type)
                                if (ret.hasError()) {
                                    Msg.requireShowLongDuration(ret.msg)
                                    createAndInsertError(repoId, "Reset $type err:"+ret.msg)
                                } else {
                                    val oldHeadCommitOid = oldHead?.id()?.toString() ?: ""
                                    //如果操作成功，刷新页面
                                    refreshPage(oldHeadCommitOid, repo.headDetached())

                                    Msg.requireShow(appContext.getString(R.string.reset_success))
                                }
                            }
                        }
                    }else {
                        //call custom onOk
                        onOk(
                            if(selectedOpt.intValue == optSoft) {
                                Reset.ResetT.SOFT
                            }else if(selectedOpt.intValue == optMixed) {
                                Reset.ResetT.MIXED
                            }else {
                                Reset.ResetT.HARD
                            }
                        )
                    }

                },
            ) {
                Text(
                    text = stringResource(id = R.string.reset),
                )
            }
        },

        )
}
