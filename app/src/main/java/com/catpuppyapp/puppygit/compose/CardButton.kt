package com.catpuppyapp.puppygit.compose

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.catpuppyapp.puppygit.style.MyStyleKt
import com.catpuppyapp.puppygit.ui.theme.Theme
import com.catpuppyapp.puppygit.utils.UIHelper


private val TAG = "CardButton"

@Composable
fun CardButton(
    modifier: Modifier = Modifier,
//    paddingValues: PaddingValues = PaddingValues(30.dp),
    text: String,
    enabled: Boolean,
    onClick: () -> Unit
) {
    val inDarkTheme = Theme.inDarkTheme

//    val appContext = AppModel.singleInstanceHolder.appContext

    val cardColor = UIHelper.defaultCardColor()

    val buttonHeight = 50



    Column(
        modifier = Modifier
            .fillMaxWidth()
//            .padding(paddingValues)
//            .padding(start = 10.dp, end = 10.dp)
            .then(modifier),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Card(
            //0.9f 占父元素宽度的百分之90
            modifier = Modifier
                .clickable(enabled = enabled) {
                    onClick()
                },
            colors = CardDefaults.cardColors(
                containerColor = cardColor,
            ),
            elevation = CardDefaults.cardElevation(
                defaultElevation = 6.dp
            )

        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth(.85f)
                    .height(buttonHeight.dp),

                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = text,
                    color = if (enabled) MyStyleKt.TextColor.enable else if (inDarkTheme) MyStyleKt.TextColor.disable_DarkTheme else MyStyleKt.TextColor.disable
                )
            }

        }

//        Spacer(modifier = Modifier.height(95.dp))

    }

}
