package com.catpuppyapp.puppygit.compose

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.catpuppyapp.puppygit.play.pro.R
import com.catpuppyapp.puppygit.data.AppContainer
import com.catpuppyapp.puppygit.git.DiffItemSaver
import com.catpuppyapp.puppygit.utils.AppModel
import com.catpuppyapp.puppygit.utils.doJobThenOffLoading
import com.catpuppyapp.puppygit.utils.showToast
import com.github.git24j.core.Diff
import com.github.git24j.core.Repository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File

@Deprecated("DiffContent一统江湖了")
@Composable
fun DiffDeletedContent(
    repoId: String,
    relativePathUnderRepoDecoded: String,
    fromTo: String,
    changeType: String,  //modification, new, del，之类的只有modification需要diff
    naviUp: () -> Boolean,
    loading: MutableState<Boolean>,
    dbContainer: AppContainer,
    contentPadding: PaddingValues
) {
    val diffItem = remember { mutableStateOf(DiffItemSaver()) }
    val lines: SnapshotStateList<String> = remember { mutableStateListOf() }
    val errOpenFileFailed = stringResource(R.string.open_file_failed)


    val hasError = rememberSaveable { mutableStateOf(false) }
    val errMsg = rememberSaveable {mutableStateOf("") }
    if(hasError.value) {
        showToast(AppModel.singleInstanceHolder.appContext, errOpenFileFailed+":"+errMsg.value)
        return
    }

    Column(
        modifier = Modifier
            .verticalScroll(rememberScrollState())
            .padding(contentPadding)
            .fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ){
        if(diffItem.value.flags.contains(Diff.FlagT.BINARY)) {
            Text(stringResource(R.string.doesnt_support_view_binary_file))
        }else {

        }

    }

    LaunchedEffect(Unit) {
        if (repoId.isNotBlank() && relativePathUnderRepoDecoded.isNotBlank()) {
            // TODO 设置页面loading为true
            //      从数据库异步查询repo数据，调用diff方法获得diff内容，然后使用diff内容更新页面state
            //      最后设置页面loading 为false
            doJobThenOffLoading launch@{
                try{
                    if(!loading.value){
                        loading.value=true
                    }
                    //从数据库查询repo，记得用会自动调用close()的use代码块
                    val repoDb = dbContainer.repoRepository
                    val repoFromDb = repoDb.getById(repoId)

                    repoFromDb?:return@launch

                    Repository.open(repoFromDb.fullSavePath).use { repo->
                        val file = File(repo.workdir().toString() + File.separator + relativePathUnderRepoDecoded)
                        val bufferedReader = file.bufferedReader()
                        lines.clear()
                        bufferedReader.lines().forEach { line ->
                            lines.add(line)
                        }
                    }
                }catch (e:Exception) {
                    hasError.value=true
                    errMsg.value = e.localizedMessage?:""
                }

            }
        }


    }
}