package com.catpuppyapp.puppygit.compose

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.catpuppyapp.puppygit.play.pro.R
import com.catpuppyapp.puppygit.ui.theme.Theme
import com.catpuppyapp.puppygit.utils.FsUtils
import com.catpuppyapp.puppygit.utils.Msg
import com.catpuppyapp.puppygit.utils.mime.MimeType
import com.catpuppyapp.puppygit.utils.mime.guessFromFileName
import com.catpuppyapp.puppygit.utils.mime.intentType
import com.catpuppyapp.puppygit.utils.state.StateUtil
import java.io.File


/**
 * fileName: 主要是用来检测mime类型的
 * filePath: 用来生成uri
 * showOpenInEditor: 是否显示在内部编辑器打开的选项
 * openInEditor: 执行在内部editor打开的函数
 * openSuccessCallback: 打开成功的回调，不管用内部editor打开还是用外部程序打开，只要打开成功就会调这个回调
 * close: 关闭弹窗
 */
@Composable
fun OpenAsDialog(fileName:String, filePath:String, showOpenInEditor:Boolean=false, openInEditor:(expectReadOnly:Boolean)->Unit={}, openSuccessCallback:()->Unit={}, close:()->Unit) {

    val appContext = LocalContext.current

    val mimeTypeList = FsUtils.FileMimeTypes.typeList.toMutableList()
    val mimeTextList = FsUtils.FileMimeTypes.textList.toMutableList()

    //添加一个根据文件名后缀打开的方式，不过可能不准
//    mimeTypeList.add(FsUtils.getMimeTypeForFilePath(appContext, filePath))
    mimeTypeList.add(MimeType.guessFromFileName(fileName).intentType)  //我的app里只允许对文件使用 open as，所以这里的filePath必然是文件（除非有bug），所以这里调用guessFromPath()无需判断路径是否是文件夹，也不需要写若是文件夹则在末尾追加分隔符的逻辑
    mimeTextList.add(stringResource(R.string.file_open_as_by_extension))


    val readOnly = rememberSaveable { mutableStateOf(false)}
    val inDarkTheme = Theme.inDarkTheme

    val color = if(inDarkTheme) Color.LightGray else Color.DarkGray

    val itemHeight = 50.dp

    Dialog(onDismissRequest = { close() }) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp),
        ) {
            Column(modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                ,
            ) {
                //文本
                //图像
                //音频
                //视频
                //其他(显示所有能打开的程序？)
                //根据后缀名检测
                if(showOpenInEditor) {
                    Spacer(modifier = Modifier.height(20.dp))

                    Row(modifier = Modifier
                        .height(itemHeight)
                        .fillMaxWidth()
                        .clickable {
                            val expectReadOnly = readOnly.value  //期望的readonly模式，若文件路径不属于app内置禁止编辑的目录，则使用此值作为readonly的初始值
                            openInEditor(expectReadOnly)

                            openSuccessCallback()
                            close()
                        }
                        ,
                    ) {
                        Column (modifier = Modifier.fillMaxSize()
                                ,
                               horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                        ){
                            Text(
                                text = stringResource(R.string.open_in_editor),
                                modifier = Modifier
                                    .wrapContentSize(Alignment.Center),
                                textAlign = TextAlign.Center,
                            )
                            //20240810：内置editor也支持read only了，所以不用显示此提示了，不过有例外：若是app内置禁止编辑的目录，则无视readonly值，强制启用readonly模式
//                            Text(
//                                text = "("+stringResource(R.string.will_ignore_read_only)+")",
//                                modifier = Modifier
//                                    .wrapContentSize(Alignment.Center),
//                                textAlign = TextAlign.Center,
//                                fontSize = 11.sp,
//                                color = color
//                            )

                        }
                    }

//                    Spacer(modifier = Modifier.height(10.dp))
                    //加这个分割线看着想标题，让人感觉不可点击，不好，所以去掉了
//                    HorizontalDivider(color = color)

                }
                Spacer(modifier = Modifier.height(10.dp))

                mimeTextList.forEachIndexed{ index, text ->
                    val mimeType = mimeTypeList[index]
                    Row(
                        modifier = Modifier
                            .height(itemHeight)
                            .fillMaxWidth()
                            .clickable {
                                val openSuccess = FsUtils.openFile(
                                    appContext,
                                    File(filePath),
                                    mimeType,
                                    readOnly.value
                                )

                                if (openSuccess) {
                                    openSuccessCallback()
                                } else {
                                    Msg.requireShow(appContext.getString(R.string.open_failed))
                                }

                                close()
                            },
                    ) {
                        Text(
                            text = text,
                            modifier = Modifier
                                .fillMaxSize()
                                .wrapContentSize(Alignment.Center),
                            textAlign = TextAlign.Center,
                        )
                    }
                }
                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    Modifier
                        .fillMaxWidth()
                        .height(itemHeight)
                        .toggleable(
                            enabled = true,
                            value = readOnly.value,
                            onValueChange = { readOnly.value = !readOnly.value },
                            role = Role.Checkbox
                        )
                        .padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = readOnly.value,
                        onCheckedChange = null // null recommended for accessibility with screenreaders
                    )

                    Text(
                        text = stringResource(R.string.read_only),
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.padding(start = 16.dp),
                    )

                }
                Spacer(modifier = Modifier.height(20.dp))

            }
        }
    }
}
