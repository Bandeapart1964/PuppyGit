package com.catpuppyapp.puppygit.compose

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.catpuppyapp.puppygit.constants.Cons
import com.catpuppyapp.puppygit.data.entity.RepoEntity
import com.catpuppyapp.puppygit.play.pro.R
import com.catpuppyapp.puppygit.utils.AppModel
import com.catpuppyapp.puppygit.utils.Libgit2Helper
import com.catpuppyapp.puppygit.utils.Msg
import com.catpuppyapp.puppygit.utils.cache.Cache

private val TAG = "DiffCommitsDialog"
private val stateKeyTag = "DiffCommitsDialog"


@Composable
fun DiffCommitsDialog(
    showDialog:MutableState<Boolean>,
    commit1:MutableState<String>,
    commit2:MutableState<String>,
    curRepo:RepoEntity,
) {
    val repoId = curRepo.id

    val appContext = AppModel.singleInstanceHolder.appContext
    val navController = AppModel.singleInstanceHolder.navController

    ConfirmDialog(title = appContext.getString(R.string.diff_commits),
        requireShowTextCompose = true,
        textCompose = {
            //只能有一个节点，因为这个东西会在lambda后返回，而lambda只能有一个返回值，弄两个布局就乱了，和react组件只能有一个root div一个道理 。
            Column {
                Text(text = stringResource(R.string.note_leave_commit_hash_empty_to_compare_with_local_worktree),
//                    fontWeight = FontWeight.ExtraBold
                )
                Spacer(modifier = Modifier.height(10.dp))


                Text(text = stringResource(R.string.will_show_differences_of_left_to_right)
                )

                Spacer(modifier = Modifier.height(10.dp))

                TextField(
                    modifier = Modifier.fillMaxWidth(),

                    value = commit1.value,
                    singleLine = true,
                    onValueChange = {
                        commit1.value = it
                    },
                    label = {
                        Text(stringResource(R.string.left))
                    },
                    placeholder = {
                        Text(stringResource(R.string.hash_branch_tag))
                    },
                )
                
                Spacer(modifier = Modifier.height(10.dp))

                TextField(
                    modifier = Modifier.fillMaxWidth(),
                    value = commit2.value,
                    singleLine = true,
                    onValueChange = {
                        commit2.value = it
                    },
                    label = {
                        Text(stringResource(R.string.right))
                    },
                    placeholder = {
                        Text(stringResource(R.string.hash_branch_tag))
                    },
                )
            }
        },
        okBtnText = stringResource(id = R.string.ok),
        cancelBtnText = stringResource(id = R.string.cancel),
//            okBtnEnabled = (!(checkoutSelectedOption.intValue==checkoutOptionCreateBranch && checkoutRemoteCreateBranchName.value.isBlank()) && !(requireUserInputCommitHash.value && checkoutUserInputCommitHash.value.isBlank())),
        onCancel = {
            showDialog.value = false
        }
    ) ok@{  //onOk
        //关弹窗
        //如果为空，替换为 local (ps:界面有提示，输入框留空等于和worktree对比）
        val commit1 = commit1.value.ifBlank { Cons.gitLocalWorktreeCommitHash }
        val commit2 = commit2.value.ifBlank { Cons.gitLocalWorktreeCommitHash }

        if(Libgit2Helper.CommitUtil.isSameCommitHash(commit1, commit2)) {
            Msg.requireShow(appContext.getString(R.string.num2_commits_same))
            return@ok
        }

        showDialog.value = false

        //当前比较的描述信息的key，用来在界面显示这是在比较啥，值例如“和父提交比较”或者“比较两个提交”之类的
        val descKey = Cache.setThenReturnKey(appContext.getString(R.string.diff_commits))
        //不用parent，传无效的zero oid即可
        val commitForQueryParents = Cons.allZeroOidStr
        // url 参数： 页面导航id/repoId/treeoid1/treeoid2/desckey
        navController.navigate(
            //注意是 parentTreeOid to thisObj.treeOid，也就是 旧提交to新提交，相当于 git diff abc...def，比较的是旧版到新版，新增或删除或修改了什么，反过来的话，新增删除之类的也就反了
            "${Cons.nav_TreeToTreeChangeListScreen}/$repoId/$commit1/$commit2/$descKey/$commitForQueryParents"
        )


    }
}
