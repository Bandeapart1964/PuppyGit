package com.catpuppyapp.puppygit.compose

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.catpuppyapp.puppygit.constants.SpecialCredential
import com.catpuppyapp.puppygit.data.entity.CredentialEntity
import com.catpuppyapp.puppygit.play.pro.R
import com.catpuppyapp.puppygit.utils.AppModel
import com.catpuppyapp.puppygit.utils.state.CustomStateSaveable


@OptIn(ExperimentalFoundationApi::class)
@Composable
fun CredentialItem(
    showBottomSheet: MutableState<Boolean>,
    curCredentialState: CustomStateSaveable<CredentialEntity>,
    idx:Int,
    thisItem:CredentialEntity,
    onClick:(CredentialEntity)->Unit
) {
    val haptic = AppModel.singleInstanceHolder.haptic

    val isMatchByDoMain = SpecialCredential.MatchByDomain.equals_to(thisItem)

//    println("IDX::::::::::"+idx)
    Column(
        //0.9f 占父元素宽度的百分之90
        modifier = Modifier
            .fillMaxWidth()
//            .defaultMinSize(minHeight = 100.dp)
            .combinedClickable(
                enabled = true,
                onClick = {
                    onClick(thisItem)
                },
                onLongClick = {
                    //震动反馈
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)

                    curCredentialState.value = CredentialEntity()

                    //设置当前条目，供bottomsheet使用
                    curCredentialState.value = thisItem

                    //显示底部菜单
                    showBottomSheet.value = true
                },
            )
            //padding要放到 combinedClickable后面，不然点按区域也会padding
//            .background(if (idx % 2 == 0) Color.Transparent else CommitListSwitchColor)
            .padding(10.dp)



    ) {
        Row (
            verticalAlignment = Alignment.CenterVertically,

        ){

            Text(text = stringResource(R.string.name) +":")
            Text(text = thisItem.name,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                fontWeight = FontWeight.Light

            )
        }
        Row (
            verticalAlignment = Alignment.CenterVertically,

        ){

            Text(text = stringResource(R.string.type) +":")
            Text(text = thisItem.getTypeStr(),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                fontWeight = FontWeight.Light

            )
        }

        if(isMatchByDoMain) {
            Row (
                verticalAlignment = Alignment.CenterVertically,

            ){
                Text(text = stringResource(R.string.desc) +":")
                Text(text = stringResource(R.string.credential_match_by_domain_note_short),
                    overflow = TextOverflow.Ellipsis,
                    fontWeight = FontWeight.Light

                )
            }
        }

    }
}
