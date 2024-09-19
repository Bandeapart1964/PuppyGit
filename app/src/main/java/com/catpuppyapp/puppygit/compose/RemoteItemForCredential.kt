package com.catpuppyapp.puppygit.compose

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.catpuppyapp.puppygit.dto.RemoteDtoForCredential
import com.catpuppyapp.puppygit.play.pro.R
import com.catpuppyapp.puppygit.style.MyStyleKt
import com.catpuppyapp.puppygit.utils.AppModel


@Composable
fun RemoteItemForCredential(
    isShowLink:Boolean,
    idx:Int,
    thisItem:RemoteDtoForCredential,
    actText:String,
    actAction:()->Unit,
) {
    val haptic = AppModel.singleInstanceHolder.haptic
//    println("IDX::::::::::"+idx)
    Row(
        //0.9f 占父元素宽度的百分之90
        modifier = Modifier
            .fillMaxWidth()
//            .defaultMinSize(minHeight = 100.dp)
            //padding要放到 combinedClickable后面，不然点按区域也会padding
//            .background(if (idx % 2 == 0) Color.Transparent else CommitListSwitchColor)
            .padding(10.dp)
        ,
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Row (
                verticalAlignment = Alignment.CenterVertically,
            ){

                Text(text = stringResource(R.string.repo) +":")
                Text(text = thisItem.repoName,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    fontWeight = FontWeight.Light
                )
            }

            Row (
                verticalAlignment = Alignment.CenterVertically,
            ){
                Text(text = stringResource(R.string.remote) +":")
                Text(text = thisItem.remoteName,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    fontWeight = FontWeight.Light

                )
            }

            Row (
                verticalAlignment = Alignment.CenterVertically,
            ){
                Text(text = stringResource(R.string.fetch_linked) +":")
                Text(text = thisItem.getCredentialNameOrNone(),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    fontWeight = FontWeight.Light
                )
            }

            Row (
                verticalAlignment = Alignment.CenterVertically,
            ){
                Text(text = stringResource(R.string.push_linked) +":")
                Text(text = thisItem.getPushCredentialNameOrNone(),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    fontWeight = FontWeight.Light
                )
            }

        }
        Column {
            Text(
                text = actText,
                style = MyStyleKt.ClickableText.style,
                color = MyStyleKt.ClickableText.color,
                modifier = MyStyleKt.ClickableText.modifier.clickable(onClick = {
                    actAction()
                }),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                fontWeight = FontWeight.Light
            )
        }
    }
}
