package com.catpuppyapp.puppygit.compose

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.ContentCut
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.catpuppyapp.puppygit.play.pro.R
import com.catpuppyapp.puppygit.style.MyStyleKt

@Deprecated("改用BottomBar了")
@Composable
fun FilesBottomBar(
    filesPageSelectionBarHeight: Dp = MyStyleKt.BottomBar.height,
    filesPageSelectionBarBackgroundColor: Color = MaterialTheme.colorScheme.primaryContainer,
    filesPageQuitSelectionMode: () -> Unit,
    isPasteMode: MutableState<Boolean>,
    opType: MutableState<String>,
    opCodeMove: String,
    isFileSelectionMode: MutableState<Boolean>,
    opCodeCopy: String,
    opCodeDelete: String,
    getSelectedFilesCount:()->Int
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Bottom,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(filesPageSelectionBarHeight)
                .background(filesPageSelectionBarBackgroundColor),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
            ) {
                LongPressAbleIconBtn(
                    modifier = Modifier
                        .padding(10.dp)
                        .size(40.dp),
                    tooltipText = stringResource(R.string.close),
                    icon = Icons.Filled.Close,
                    iconContentDesc = stringResource(R.string.quit_selection_files_mode),
                    onClick = {
                        //退出选择模式
                        filesPageQuitSelectionMode()
                        //退出粘贴模式
                        isPasteMode.value = false
                    },
                )
                //选择的条目数
                Text(text = ""+getSelectedFilesCount())
            }
            Row(
                horizontalArrangement = Arrangement.End
            ) {
                if (isPasteMode.value) {
                    LongPressAbleIconBtn(
                        tooltipText = stringResource(R.string.paste),
                        icon = Icons.Filled.ContentPaste,
                        iconContentDesc = stringResource(R.string.paste),
                        onClick = {
                            /*TODO paste */
                            //把选择的文件拷贝到当前目录即可

                        }
                    )
                } else {
                    LongPressAbleIconBtn(
                        tooltipText = stringResource(R.string.move),
                        icon = Icons.Filled.ContentCut,
                        iconContentDesc = stringResource(R.string.move),
                        onClick = {
                            /*TODO cut/move */
                            opType.value = opCodeMove
                            isPasteMode.value = true
                            isFileSelectionMode.value = false
                        }
                    )
                    LongPressAbleIconBtn(
                        tooltipText = stringResource(R.string.copy),
                        icon = Icons.Filled.ContentCopy,
                        iconContentDesc = stringResource(R.string.copy),
                        onClick = { /*TODO copy*/
                            opType.value = opCodeCopy
                            isPasteMode.value = true
                            isFileSelectionMode.value = false

                        }
                    )
                    LongPressAbleIconBtn(
                        tooltipText = stringResource(R.string.delete),
                        icon = Icons.Filled.DeleteForever,
                        iconContentDesc = stringResource(R.string.delete),
                        onClick = { /*TODO delete*/
                            opType.value = opCodeDelete
                            //TODO 显示删除确认弹窗，如果确认，遮罩阻塞式删除，删除后退出文件选择模式
                            val delFinishedCallBack = {  //删除完成后执行此callback，如果取消删除操作，则只关闭弹窗即可
                                isPasteMode.value = false
                                isFileSelectionMode.value = false
                            }
                            val delCancelCallBack = { //把opType设为非delete就会关闭删除弹窗了
                                opType.value = ""
                            }

                        }
                    )

                }
            }
        }
    }
}