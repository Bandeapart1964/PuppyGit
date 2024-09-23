package com.catpuppyapp.puppygit.screen.content.homescreen.scaffold.actions

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.FilterAlt
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.TextFieldValue
import androidx.navigation.NavHostController
import com.catpuppyapp.puppygit.compose.LongPressAbleIconBtn
import com.catpuppyapp.puppygit.constants.Cons
import com.catpuppyapp.puppygit.data.entity.RepoEntity
import com.catpuppyapp.puppygit.dev.importRepoTestPassed
import com.catpuppyapp.puppygit.dev.proFeatureEnabled
import com.catpuppyapp.puppygit.play.pro.R
import com.catpuppyapp.puppygit.style.MyStyleKt
import com.catpuppyapp.puppygit.utils.changeStateTriggerRefreshPage
import com.catpuppyapp.puppygit.utils.state.CustomStateSaveable
import com.catpuppyapp.puppygit.utils.state.StateUtil


@Composable
fun RepoPageActions(
    navController: NavHostController,
    curRepo: CustomStateSaveable<RepoEntity>,
    showGlobalUsernameAndEmailDialog: MutableState<Boolean>,
    needRefreshRepoPage: MutableState<String>,
    repoPageFilterModeOn:MutableState<Boolean>,
    repoPageFilterKeyWord:CustomStateSaveable<TextFieldValue>,
    showImportRepoDialog:MutableState<Boolean>
) {
    /*  TODO 添加个设置按钮
     * 跳转到仓库全局设置页面，至少两个开关：
     * Auto Fetch                default:Off
     * Auto check Status         default:Off
     */


    val dropDownMenuExpendState = StateUtil.getRememberSaveableState(initValue = false)

    val closeMenu = {dropDownMenuExpendState.value = false}

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
        tooltipText = stringResource(R.string.clone),
        icon = Icons.Filled.Add,
        iconContentDesc = stringResource(id = R.string.clone),
    ) {
//                                setShowCloneDialog(true)
        // if id is blank or null path to "CloneScreen/null", else path to "CloneScreen/repoId"
        navController.navigate(Cons.nav_CloneScreen+"/null")  //不传repoId，就是null，等于新建模式
    }

    LongPressAbleIconBtn(
        //这种需展开的菜单，禁用内部的选项即可
//        enabled = enableAction.value,

        tooltipText = stringResource(R.string.menu),
        icon = Icons.Filled.MoreVert,
        iconContentDesc = stringResource(R.string.menu),
        onClick = {
            //切换菜单展开状态
            dropDownMenuExpendState.value = !dropDownMenuExpendState.value
        }
    )

    //菜单列表
    Row(modifier = Modifier.padding(top = MyStyleKt.TopBar.dropDownMenuTopPaddingSize)) {
//        val enableMenuItem = enableAction.value && !changeListPageNoRepo.value  && !hasTmpStatus

        //菜单列表
        DropdownMenu(
            expanded = dropDownMenuExpendState.value,
            onDismissRequest = { closeMenu() }
        ) {
            if(proFeatureEnabled(importRepoTestPassed)) {
                DropdownMenuItem(
                    text = { Text(stringResource(R.string.import_repo)) },
                    onClick = {
                        closeMenu()
                        showImportRepoDialog.value = true
                    }
                )
            }

            DropdownMenuItem(
                text = { Text(stringResource(R.string.username_and_email)) },
                onClick = {
                    closeMenu()
                    showGlobalUsernameAndEmailDialog.value=true
                }
            )
            DropdownMenuItem(
                text = { Text(stringResource(R.string.credential_manager)) },
                onClick = {
                    closeMenu()
                    navController.navigate(Cons.nav_CredentialManagerScreen+"/${Cons.dbInvalidNonEmptyId}")
                }
            )
        }
    }
}
