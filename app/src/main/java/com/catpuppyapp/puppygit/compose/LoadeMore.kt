package com.catpuppyapp.puppygit.compose

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.catpuppyapp.puppygit.play.pro.R
import com.catpuppyapp.puppygit.style.MyStyleKt
import com.catpuppyapp.puppygit.ui.theme.Theme
import com.catpuppyapp.puppygit.utils.UIHelper

@Composable
fun LoadMore(
    modifier: Modifier = Modifier,
    paddingValues: PaddingValues = PaddingValues(30.dp),
    text:String= stringResource(R.string.load_more),
    loadToEndText:String= stringResource(R.string.load_to_end),
    enableLoadMore:Boolean=true,
    enableAndShowLoadToEnd:Boolean=true,
    loadToEndOnClick:()->Unit={},
    onClick:()->Unit
) {
    val inDarkTheme = Theme.inDarkTheme

    val cardColor = UIHelper.defaultCardColor()

    Column(modifier= Modifier
        .fillMaxWidth()
        .padding(paddingValues)
        .then(modifier)
    ) {
        Card(
            //0.9f 占父元素宽度的百分之90
            modifier = Modifier
                .clickable(enabled = enableLoadMore) {  //如果有更多，则启用点击加载更多，否则禁用
                    onClick()
                }
            ,
            colors = CardDefaults.cardColors(
                containerColor = cardColor,
            ),
            elevation = CardDefaults.cardElevation(
                defaultElevation = 6.dp
            )

        ) {
            Row(
                modifier = Modifier
                    .padding(start = 30.dp, end = 30.dp)
                    .height(50.dp)
                    .fillMaxWidth()
                ,

                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = text,
                    color = if(enableLoadMore) MyStyleKt.TextColor.enable else if(inDarkTheme) MyStyleKt.TextColor.disable_DarkTheme else MyStyleKt.TextColor.disable
                )
            }

        }

        Spacer(modifier = Modifier.height(20.dp))
        if(enableAndShowLoadToEnd) {
            Card(
                //0.9f 占父元素宽度的百分之90
                modifier = Modifier
                    .clickable{  //如果有更多，则启用点击加载更多，否则禁用
                        loadToEndOnClick()
                    }
                ,
                colors = CardDefaults.cardColors(
                    containerColor = cardColor,
                ),
                elevation = CardDefaults.cardElevation(
                    defaultElevation = 6.dp
                )

            ) {
                Row(
                    modifier = Modifier
                        .padding(start = 30.dp, end = 30.dp)
                        .height(50.dp)
                        .fillMaxWidth()
                    ,
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = loadToEndText,
                        color = MyStyleKt.TextColor.enable
                    )
                }

            }
        }

    }


}
