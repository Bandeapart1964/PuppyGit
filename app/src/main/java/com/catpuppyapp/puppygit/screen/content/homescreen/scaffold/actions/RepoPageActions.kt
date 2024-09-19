package com.catpuppyapp.puppygit.screen.content.homescreen.scaffold.actions

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.FilterAlt
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.TextFieldValue
import androidx.navigation.NavHostController
import com.catpuppyapp.puppygit.play.pro.R
import com.catpuppyapp.puppygit.compose.LongPressAbleIconBtn
import com.catpuppyapp.puppygit.constants.Cons
import com.catpuppyapp.puppygit.data.entity.RepoEntity
import com.catpuppyapp.puppygit.utils.changeStateTriggerRefreshPage
import com.catpuppyapp.puppygit.utils.state.CustomStateSaveable


@Composable
fun RepoPageActions(
    navController: NavHostController,
    curRepo: CustomStateSaveable<RepoEntity>,
    showGlobalUsernameAndEmailDialog: MutableState<Boolean>,
    needRefreshRepoPage: MutableState<String>,
    repoPageFilterModeOn:MutableState<Boolean>,
    repoPageFilterKeyWord:CustomStateSaveable<TextFieldValue>
) {
    /*  TODO 添加个设置按钮
     * 跳转到仓库全局设置页面，至少两个开关：
     * Auto Fetch                default:Off
     * Auto check Status         default:Off
     */
    LongPressAbleIconBtn(
        tooltipText = stringResource(R.string.filter),
        icon =  Icons.Filled.FilterAlt,
        iconContentDesc = stringResource(id = R.string.filter),

    ) {
        repoPageFilterKeyWord.value = TextFieldValue("")
        repoPageFilterModeOn.value = true
    }

    LongPressAbleIconBtn(
        tooltipText = stringResource(R.string.refresh),
        icon = Icons.Filled.Refresh,
        iconContentDesc = stringResource(id = R.string.refresh),
    ) {
        changeStateTriggerRefreshPage(needRefreshRepoPage)
    }
    LongPressAbleIconBtn(
        tooltipText = stringResource(R.string.username_email),
        icon = Icons.Filled.Person,
        iconContentDesc = stringResource(id = R.string.global_username_email),
    ) {
        /*弹出输入用户名邮箱的对话框*/
        showGlobalUsernameAndEmailDialog.value=true
    }
    LongPressAbleIconBtn(
        tooltipText = stringResource(R.string.credential_manager),
//        icon = ImageVector.vectorResource(id = R.drawable.key_chain_variant),
        icon = Icons.Filled.Key,
        iconContentDesc = stringResource(id = R.string.credentials),
    ) {
        navController.navigate(Cons.nav_CredentialManagerScreen+"/${Cons.dbInvalidNonEmptyId}")
    }

    LongPressAbleIconBtn(
        tooltipText = stringResource(R.string.clone),
        icon = Icons.Filled.Add,
        iconContentDesc = stringResource(id = R.string.clone),
    ) {
//                                setShowCloneDialog(true)
        // if id is blank or null path to "CloneScreen/null", else path to "CloneScreen/repoId"
        navController.navigate(Cons.nav_CloneScreen+"/null")  //不传repoId，就是null，等于新建模式
    }
}
