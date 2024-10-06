package com.catpuppyapp.puppygit.compose

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.catpuppyapp.puppygit.data.entity.RepoEntity
import com.catpuppyapp.puppygit.git.RemoteAndCredentials
import com.catpuppyapp.puppygit.git.TagDto
import com.catpuppyapp.puppygit.play.pro.R
import com.catpuppyapp.puppygit.style.MyStyleKt
import com.catpuppyapp.puppygit.utils.AppModel
import com.catpuppyapp.puppygit.utils.Libgit2Helper
import com.catpuppyapp.puppygit.utils.Msg
import com.catpuppyapp.puppygit.utils.doJobThenOffLoading
import com.catpuppyapp.puppygit.utils.state.StateUtil
import com.github.git24j.core.Repository

@Composable
fun TagFetchPushDialog(
    title:String,
    remoteList:List<String>,
    selectedRemoteList:MutableList<String>,
    remoteCheckedList:MutableList<Boolean>,
    enableOk:Boolean,
    showForce:Boolean,
    requireDel:Boolean,
    requireDelRemoteChecked:MutableState<Boolean>,
    trueFetchFalsePush:Boolean,
    showTagFetchPushDialog:MutableState<Boolean>,
    loadingOn:(String)->Unit,
    loadingOff:()->Unit,
    loadingTextForFetchPushDialog:MutableState<String>,
    curRepo:RepoEntity,
    selectedTagsList:List<TagDto>,
    allTagsList:List<TagDto>,
    onCancel:()->Unit,
    onSuccess:()->Unit,  //try代码块末尾
    onErr:suspend (e:Exception)->Unit, //catch代码块末尾
    onFinally:()->Unit, //在 try...catch...finally，finally代码块里的代码
) {

    val appContext = AppModel.singleInstanceHolder.appContext

    val force = rememberSaveable { mutableStateOf(false) }

    val repoId = curRepo.id

    val onOk = { force:Boolean ->
        //关闭弹窗
        showTagFetchPushDialog.value = false

        //执行操作
        doJobThenOffLoading(loadingOn, loadingOff, loadingTextForFetchPushDialog.value) {
            try {
                Repository.open(curRepo.fullSavePath).use { repo ->
                    //删除本地tags
                    if(requireDel) {
                        Libgit2Helper.delTags(repo, selectedTagsList.map { it.shortName })

                        //如果删除模式且没勾选删除远程，删完本地tags就可返回了
                        if(!requireDelRemoteChecked.value) {
                            Msg.requireShowLongDuration(appContext.getString(R.string.success))
                            return@doJobThenOffLoading
                        }
                    }


                    // fetch/push(and del remote tags)
                    val remoteAndCredentials = mutableListOf<RemoteAndCredentials>()
                    val remoteDb = AppModel.singleInstanceHolder.dbContainer.remoteRepository
                    val credentialDb = AppModel.singleInstanceHolder.dbContainer.credentialRepository

                    selectedRemoteList.forEach {
                        val remote = remoteDb.getByRepoIdAndRemoteName(repoId, it)  //无条目不跳过，只是不查凭据了

                        //其实这里fetch时不用查push，push时不用查fetch，但我为了简化逻辑，直接两个都查了
                        val fetchCredential = if(remote==null || remote.credentialId.isBlank()) null else credentialDb.getByIdWithDecryptAndMatchByDomain(
                            id = remote.credentialId,
                            url = remote.remoteUrl
                        )
                        val pushCredential = if(remote==null || remote.pushCredentialId.isBlank()) null else credentialDb.getByIdWithDecryptAndMatchByDomain(
                            id = remote.pushCredentialId,
                            url = remote.pushUrl
                        )

                        remoteAndCredentials.add(
                            RemoteAndCredentials(
                                remoteName = it,
                                fetchCredential = fetchCredential,
                                pushCredential = pushCredential
                            )

                        )
                    }

                    if(trueFetchFalsePush) {  // fetch
                        Libgit2Helper.fetchAllTags(repo, curRepo, remoteAndCredentials, force)
                    } else {  // push
                        val delRemote = requireDel && requireDelRemoteChecked.value && selectedRemoteList.isNotEmpty()

                        //修改下loading text
                        if(delRemote) {
                            loadingTextForFetchPushDialog.value = appContext.getString(R.string.deleting_remote_tags)
                        }

                        val force = if(delRemote) false else force  // del mode 没必要使用force，所以设为假
//                        val selectedAllTags = selectedTagsList.size == allTagsList.size
//                        if(selectedAllTags) {
//                            Libgit2Helper.pushAllTags(repo, remoteAndCredentials, force, delRemote)
//                        }else {
                        val pushRefSpecs = mutableListOf<String>()
                        selectedTagsList.forEach {
                            if(delRemote) {
                                pushRefSpecs.add(":${it.name}")
                            }else {
                                pushRefSpecs.add(
                                    if(force){
                                        "+"+it.name+":"+it.name
                                    }else{
                                        it.name+":"+it.name
                                    }
                                )

                            }
                        }

                        Libgit2Helper.pushTags(repo, remoteAndCredentials, pushRefSpecs)
//                        }
                    }
                }

                onSuccess()
            }catch (e:Exception) {
                onErr(e)
            }finally {
                onFinally()
            }
        }
    }

    AlertDialog(
        title = {
            Text(title)
        },
        text = {
            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {

                if(requireDel) {
                    Row(modifier = Modifier.padding(10.dp)) {
                        Text(text = stringResource(R.string.are_you_sure))
                    }

                    MyCheckBox(
                        text = stringResource(R.string.del_tags_on_remotes),
                        value = requireDelRemoteChecked
                    )
                }else {  // fetch/push，显示默认不勾选force的覆盖提示
                    Row(modifier = Modifier.padding(10.dp)) {
                        Text(text = if(trueFetchFalsePush) stringResource(R.string.note_as_default_remote_will_not_override_local_tags)
                                    else stringResource(R.string.note_as_default_local_will_override_remote_tags)
                            ,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                }

                if(!requireDel || requireDelRemoteChecked.value) {
                    Row(modifier = Modifier.padding(10.dp)) {
                        Text(text = stringResource(R.string.select_remotes) +":")
                    }

                    RemoteCheckBoxList(itemList = remoteList, selectedList = selectedRemoteList, remoteCheckedList)

                    if(showForce) {
                        Row(modifier = Modifier.padding(10.dp)) {
                            Text(text = stringResource(R.string.options) +":")
                        }
                        MyCheckBox(text = stringResource(id = R.string.force), value = force)
                        if(force.value) {  //显示勾选force后的覆盖提示
                            Row {
                                Text(text = if(trueFetchFalsePush) stringResource(R.string.warn_remote_tag_override_local_tag)
                                            else stringResource(R.string.warn_local_tag_override_remote_tag)
                                    ,
                                    color = MyStyleKt.TextColor.danger
                                )
                            }
                        }
                    }
                }

            }

        },
        onDismissRequest = {
            onCancel()
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onOk(force.value)
                },
                enabled = enableOk
            ) {
                Text(text = stringResource(id = R.string.ok))

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
