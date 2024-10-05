package com.catpuppyapp.puppygit.compose

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.catpuppyapp.puppygit.data.entity.RepoEntity
import com.catpuppyapp.puppygit.play.pro.R
import com.catpuppyapp.puppygit.utils.AppModel
import com.catpuppyapp.puppygit.utils.Libgit2Helper
import com.catpuppyapp.puppygit.utils.Msg
import com.catpuppyapp.puppygit.utils.doJobThenOffLoading
import com.catpuppyapp.puppygit.utils.state.CustomStateSaveable
import com.catpuppyapp.puppygit.utils.state.StateUtil
import com.github.git24j.core.Repository
import java.io.File


private val TAG = "ApplyPatchDialog"
private val stateKeyTag = "ApplyPatchDialog"


@Composable
fun ApplyPatchDialog(
    showDialog: MutableState<Boolean>,
    selectedRepo:CustomStateSaveable<RepoEntity>,
    patchFileFullPath:String,
    onCancel: () -> Unit,
    onErrCallback:suspend (err:Exception, selectedRepoId:String)->Unit,
    onFinallyCallback:()->Unit,
    onOkCallback:()->Unit,
) {

    val appContext = AppModel.singleInstanceHolder.appContext

    val loading = StateUtil.getRememberSaveableState(true)

    val repoList = StateUtil.getCustomSaveableStateList(keyTag = stateKeyTag, keyName = "repoList") {
        listOf<RepoEntity>()
    }


    if(!loading.value) {
        ConfirmDialog2(
            okBtnEnabled = repoList.value.isNotEmpty() && selectedRepo.value.id.isNotBlank(),
            title = stringResource(R.string.apply_patch),
            requireShowTextCompose = true,
            textCompose = {
                Column {
                    Text(text = stringResource(R.string.select_target_repo)+":")
                    Spacer(modifier = Modifier.height(10.dp))

                    SingleSelectList(optionsList = repoList.value,
                        menuItemSelected = {idx, value -> value.id == selectedRepo.value.id},
                        menuItemOnClick = {idx, value -> selectedRepo.value = value},
                        menuItemFormatter = {idx, value -> value?.repoName ?: ""},
                        selectedOptionIndex = null,
                        selectedOptionValue = selectedRepo.value
                    )
//
//                MyLazyColumn(
//                    modifier = Modifier.heightIn(max=150.dp),
//                    requireUseParamModifier = true,
//                    contentPadding = PaddingValues(0.dp),
//                    list = repoList.value,
//                    listState = StateUtil.getRememberLazyListState(),
//                    requireForEachWithIndex = true,
//                    requirePaddingAtBottom =false
//                ) {k, it ->
//                    Row(
//                        Modifier
//                            .fillMaxWidth()
//                            .heightIn(min = MyStyleKt.RadioOptions.minHeight)
//
//                            .selectable(
//                                selected = it.id == selectedRepo.value.id,
//                                onClick = {
//                                    //更新选择值
//                                    selectedRepo.value = it
//                                },
//                                role = Role.RadioButton
//                            )
//                            .padding(horizontal = 10.dp),
//                        verticalAlignment = Alignment.CenterVertically
//                    ) {
//                        RadioButton(
//                            selected = it.id == selectedRepo.value.id,
//                            onClick = null // null recommended for accessibility with screenreaders
//                        )
//                        Text(
//                            text = it.repoName,
//                            style = MaterialTheme.typography.bodyLarge,
//                            modifier = Modifier.padding(start = 10.dp)
//                        )
//                    }
//
//                }

                }
            },
            onCancel = { onCancel() }) {

            doJobThenOffLoading {
                try {
                    Repository.open(selectedRepo.value.fullSavePath).use { repo ->
                        /*
                         *(
                                inputFile:File,
                                repo:Repository,
                                applyOptions: Apply.Options?=null,
                                location:Apply.LocationT = Apply.LocationT.WORKDIR,  // default same as `git apply`
                                checkWorkdirCleanBeforeApply: Boolean = true,
                                checkIndexCleanBeforeApply: Boolean = false
                            )
                         */
                        val inputFile = File(patchFileFullPath)
                        val ret = Libgit2Helper.applyPatchFromFile(
                            inputFile,
                            repo,
                        )

                        if(ret.hasError()) {
                            if(ret.exception!=null) {
                                throw ret.exception!!
                            }else {
                                throw RuntimeException(ret.msg)
                            }
                        }
                    }


                    onOkCallback()
                }catch (e:Exception){
                    onErrCallback(e, selectedRepo.value.id)
                }finally {
                    onFinallyCallback()
                }
            }
        }
    }


    LaunchedEffect(Unit) {
        doJobThenOffLoading job@{
            loading.value = true

            val repoDb = AppModel.singleInstanceHolder.dbContainer.repoRepository
            val listFromDb = repoDb.getReadyRepoList()
            if(listFromDb.isEmpty()) {
                Msg.requireShowLongDuration(appContext.getString(R.string.repo_list_is_empty))
                //关闭弹窗
                onCancel()
                return@job
            }

            repoList.value.clear()
            repoList.value.addAll(listFromDb)

            // if selectedRepo not in list, select first
            if(listFromDb.indexOfFirst { selectedRepo.value.id == it.id } == -1) {
                selectedRepo.value = repoList.value[0]
            }

            loading.value = false
        }
    }
}
