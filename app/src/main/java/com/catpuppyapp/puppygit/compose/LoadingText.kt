package com.catpuppyapp.puppygit.compose

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.catpuppyapp.puppygit.play.pro.R
import com.catpuppyapp.puppygit.utils.state.StateUtil

/**
 * 注：这个组件应该放到Scaffold里，不然背景色无法随系统主题变化(例如开了dark theme，这个背景色还是全白，会很刺眼)
 */
@Composable
fun LoadingText(text:String= stringResource(R.string.loading),
                contentPadding: PaddingValues,
                enableScroll:Boolean = true,
                scrollState:ScrollState = rememberScrollState()
                ) {
    Column(
        modifier = Modifier
            .padding(contentPadding)
            .fillMaxSize()

            //默认启用滚动，不然滚动 隐藏/显示 的顶栏无法触发 隐藏/显示
            .then(if(enableScroll) Modifier.verticalScroll(scrollState) else Modifier)
        ,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(text = text)
    }
}
