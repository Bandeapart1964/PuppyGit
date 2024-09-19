package com.catpuppyapp.puppygit.compose

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import com.catpuppyapp.puppygit.play.pro.R
import com.catpuppyapp.puppygit.utils.doJobThenOffLoading
import com.catpuppyapp.puppygit.utils.state.StateUtil


@Composable
fun CopyableDialog(
    title: String="",
    text: String="",
    requireShowTitleCompose:Boolean=false,
    titleCompose:@Composable ()->Unit={},
    requireShowTextCompose:Boolean=false,
    textCompose:@Composable ()->Unit={},
    cancelBtnText: String = stringResource(R.string.close),
    okBtnText: String = stringResource(R.string.copy),
    cancelTextColor: Color = Color.Unspecified,
    okTextColor: Color = Color.Unspecified,
    okBtnEnabled: Boolean=true,
    loadingOn:(loadingText:String)->Unit={},
    loadingOff:()->Unit={},
    loadingText: String= stringResource(R.string.loading),
    onCancel: () -> Unit,
    onOk: suspend () -> Unit,  //加suspend是为了避免拷贝的时候卡住或抛异常，不过外部不需要开协程，本组件内开就行了，调用者只写逻辑即可
) {
    //本来是想：如果执行完操作的某个阶段需要改loadingText，可使用此状态变量作为doJob协程的参数并在需要更新loadingText时修改此状态变量的值。但实际上：在onOk里更新loadingText就行了
//    val loadingTextState = StateUtil.getRememberSaveableState(initValue = loadingText)

    AlertDialog(
        title = {
            MySelectionContainer {
                if(requireShowTitleCompose) {
                    titleCompose()
                }else {
                    Text(title)
                }

            }
        },
        text = {
            MySelectionContainer {
                Column(modifier = Modifier
                    .verticalScroll(StateUtil.getRememberScrollState())
                ) {
                    if(requireShowTextCompose) {
                        textCompose()
                    }else {
                        Text(text)
                    }
                }
            }
        },
        //点击弹框外区域的时候触发此方法，一般设为和OnCancel一样的行为即可
        onDismissRequest = onCancel,
        dismissButton = {
            TextButton(
                onClick = onCancel
            ) {
                Text(
                    text = cancelBtnText,
                    color = cancelTextColor,
                )
            }
        },
        confirmButton = {
            TextButton(
                enabled = okBtnEnabled,
                onClick = {
                    //执行用户传入的callback
                    doJobThenOffLoading(loadingOn, loadingOff, loadingText) {
                        onOk()
                    }
                },
            ) {
                Text(
                    text = okBtnText,
                    color = okTextColor,
                )
            }
        },

        )


}
