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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.catpuppyapp.puppygit.play.pro.R
import com.catpuppyapp.puppygit.settings.SettingsUtil
import com.catpuppyapp.puppygit.style.MyStyleKt
import com.catpuppyapp.puppygit.ui.theme.Theme
import com.catpuppyapp.puppygit.utils.AppModel
import com.catpuppyapp.puppygit.utils.Msg
import com.catpuppyapp.puppygit.utils.MyLog
import com.catpuppyapp.puppygit.utils.UIHelper


private val TAG = "LoadMore"

@Composable
fun LoadMore(
    modifier: Modifier = Modifier,
    paddingValues: PaddingValues = PaddingValues(30.dp),
    text:String= stringResource(R.string.load_more),
    loadToEndText:String= stringResource(R.string.load_to_end),
    enableLoadMore:Boolean=true,
    enableAndShowLoadToEnd:Boolean=true,
    loadToEndOnClick:()->Unit={},
    pageSize:MutableState<Int>,
    rememberPageSize:MutableState<Boolean>,
    onClick:()->Unit
) {
    val inDarkTheme = Theme.inDarkTheme

    val appContext = AppModel.singleInstanceHolder.appContext

    val cardColor = UIHelper.defaultCardColor()

    val invalidPageSize = -1
    val minPageSize = 1  // make sure it bigger than `invalidPageSize`

    val isInvalidPageSize = { ps:Int ->
        ps < minPageSize
    }

    val showSetPageSizeDialog = rememberSaveable { mutableStateOf(false) }
    val pageSizeForDialog = rememberSaveable { mutableStateOf(""+pageSize.value) }
    if(showSetPageSizeDialog.value) {
        ConfirmDialog2(
            title = stringResource(R.string.page_size),
            requireShowTextCompose = true,
            textCompose = {
                ScrollableColumn {
                    TextField(
                        modifier = Modifier.fillMaxWidth(),

                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),

                        value = pageSizeForDialog.value,
                        singleLine = true,
                        onValueChange = {
                            pageSizeForDialog.value = it
                        },
                        label = {
                            Text(stringResource(R.string.page_size))
                        },
                    )

                    Spacer(Modifier.height(10.dp))

                    MyCheckBox(text= stringResource(R.string.remember), rememberPageSize)
                }
            },
            onCancel = {showSetPageSizeDialog.value=false}
        ) {
            showSetPageSizeDialog.value=false

            try {
                val newPageSize = try {
                    pageSizeForDialog.value.toInt()
                }catch (_:Exception) {
                    Msg.requireShow(appContext.getString(R.string.invalid_number))
                    invalidPageSize
                }

                if(!isInvalidPageSize(newPageSize)) {
                    pageSize.value = newPageSize

                    if(rememberPageSize.value) {
                        SettingsUtil.update {
                            it.commitHistoryPageSize = newPageSize
                        }
                    }
                }

            }catch (e:Exception) {
                MyLog.e(TAG, "#SetPageSizeDialog err: ${e.localizedMessage}")
            }
        }
    }


    Column(modifier= Modifier
        .fillMaxWidth()
        .padding(paddingValues)
        .then(modifier)
    ) {
        Row (
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ){
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
                        // at here, padding must before height and width, else, style bad, no space to screen border
                        .padding(start = 30.dp, end = 30.dp)
                        .height(50.dp)
                        .fillMaxWidth(.85f)
                    ,

                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = text,
                        color = if(enableLoadMore) MyStyleKt.TextColor.enable else if(inDarkTheme) MyStyleKt.TextColor.disable_DarkTheme else MyStyleKt.TextColor.disable
                    )
                }

            }


            LongPressAbleIconBtn(
                tooltipText = stringResource(R.string.set_page_size),
                icon =  Icons.Filled.Settings,
                iconContentDesc = stringResource(R.string.set_page_size),
//                iconModifier = Modifier.fillMaxWidth(.2f),
//                enabled = currentPage.intValue>1,
            ) {
                pageSizeForDialog.value = ""+pageSize.value
                showSetPageSizeDialog.value = true
            }

        }

        if(enableAndShowLoadToEnd) {
            Spacer(modifier = Modifier.height(20.dp))

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

        Spacer(modifier = Modifier.height(95.dp))

    }

}
