package com.catpuppyapp.puppygit.compose

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.catpuppyapp.puppygit.utils.state.StateUtil

//lazyColumn老出问题，不是并发修改异常就是索引越界，统一弄到这里方便修改和debug
@Composable
fun <T> MyLazyColumn(
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues,
    list: List<T>,
    listState: LazyListState,
    requireForEachWithIndex: Boolean,
    requirePaddingAtBottom: Boolean,
    requireUseParamModifier:Boolean=false,  //如果为true，将使用参数中的modifier，否则使用默认的。合并modifier(使用.then())，有时候样式会出问题，所以要么用默认，要么完全调用者自己调，这样比较好
    requireCustomBottom:Boolean=false,
    requireUseCustomLazyListScope:Boolean=false,
    customLazyListScope: LazyListScope.(T) -> Unit={},
    customLazyListScopeWithIndex: LazyListScope.(Int, T) -> Unit={ idx, v->},
    customBottom: @Composable ()->Unit={},
    forEachCb: @Composable (T) -> Unit={},
    forEachIndexedCb: @Composable (Int, T) -> Unit
) {
    if(list.isEmpty()) {  // 20240503:尝试解决索引越界异常
        Column(modifier =if(requireUseParamModifier) {
                    modifier
                }else {
                    Modifier
                        .fillMaxSize()
                        .padding(contentPadding)
                        .verticalScroll(rememberScrollState())
                }
            ,
        ) {
                // noop
        }
    }else {
        val listCopy = list.toList()
        LazyColumn(modifier = if(requireUseParamModifier) {
                        modifier
                    }else {
                        Modifier
                            .fillMaxSize()
                    }
            ,
            contentPadding = contentPadding,
            state = listState
        ){
            if(requireForEachWithIndex) {
                // toList似乎会拷贝元素可在一定程度避免并发修改异常
                listCopy.forEachIndexed { idx,it->
                    if(requireUseCustomLazyListScope) {
                        customLazyListScopeWithIndex(idx, it)
                    }else {
                        item {
                            forEachIndexedCb(idx, it)
                        }
                    }
                }

            }else {
                listCopy.forEach {
                    if(requireUseCustomLazyListScope) {
                        customLazyListScope(it)
                    }else {
                        item {
                            forEachCb(it)
                        }
                    }

                }
            }

            if(requireCustomBottom) {
                item {
                    customBottom()
                }
            }

            if(requirePaddingAtBottom) {
                item { PaddingRow() }
            }
        }
    }
}
