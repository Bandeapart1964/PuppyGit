package com.catpuppyapp.puppygit.screen

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.catpuppyapp.puppygit.compose.LoadingDialog
import com.catpuppyapp.puppygit.compose.LongPressAbleIconBtn
import com.catpuppyapp.puppygit.constants.Cons
import com.catpuppyapp.puppygit.data.entity.CredentialEntity
import com.catpuppyapp.puppygit.data.entity.RepoEntity
import com.catpuppyapp.puppygit.play.pro.R
import com.catpuppyapp.puppygit.style.MyStyleKt
import com.catpuppyapp.puppygit.utils.AppModel
import com.catpuppyapp.puppygit.utils.doJobThenOffLoading
import com.catpuppyapp.puppygit.utils.state.mutableCustomStateOf
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

private val stateKeyTag = "CredentialNewOrEdit"
private val TAG = "CredentialNewOrEdit"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CredentialNewOrEdit(
    credentialId: String?,  //编辑已存在条目的时候，用得着这个
    naviUp: () -> Unit,
) {

    val appContext = LocalContext.current

    val isEditMode = rememberSaveable { mutableStateOf(false)}
    val repoFromDb = mutableCustomStateOf(keyTag = stateKeyTag, keyName = "repoFromDb", initValue = RepoEntity(id = ""))
    //克隆完成后更新此变量，然后在重新渲染时直接返回。（注：因为无法在coroutine里调用naviUp()，所以才这样实现“存储完成返回上级页面”的功能）
//    val isTimeNaviUp = rememberSaveable { mutableStateOf(false) }
//
//    if(isTimeNaviUp.value) {
//        naviUp()
//    }

    val homeTopBarScrollBehavior = AppModel.singleInstanceHolder.homeTopBarScrollBehavior

//    val allRepoParentDir = AppModel.singleInstanceHolder.allRepoParentDir

//    val gitUrl = rememberSaveable { mutableStateOf("") }
//    val repoName = remember { mutableStateOf(TextFieldValue("")) }
//    val repoName = mutableCustomStateOf(value = TextFieldValue(""))
    val branch = rememberSaveable { mutableStateOf("")}
    val depth = rememberSaveable { mutableStateOf("")}  //默认depth 为空，克隆全部；不为空则尝试解析，大于0，则传给git；小于0则克隆全部
//    val credentialName = remember { mutableStateOf(TextFieldValue("")) }  //旋转手机，画面切换后值会被清，因为不是 rememberSaveable，不过rememberSaveable不适用于TextFieldValue，所以改用我写的自定义状态存储器了
    val credentialName = mutableCustomStateOf(keyTag = stateKeyTag, keyName = "credentialName", initValue = TextFieldValue(""))
    val credentialVal = rememberSaveable { mutableStateOf("")}
    val credentialPass = rememberSaveable { mutableStateOf("")}

//    val gitUrlType = rememberSaveable { mutableIntStateOf(Cons.gitUrlTypeHttp) }

    val credentialType = rememberSaveable{mutableIntStateOf(Cons.dbCredentialTypeHttp)}
    val credentialInThisPage = mutableCustomStateOf(keyTag = stateKeyTag, keyName = "credentialInThisPage", initValue = CredentialEntity(id = ""))
    val oldCredential = mutableCustomStateOf(keyTag = stateKeyTag, keyName = "oldCredential", initValue = CredentialEntity(id = ""))

    //获取输入焦点，弹出键盘
//    val focusRequesterGitUrl = remember{ FocusRequester() }  // 1
//    val focusRequesterRepoName = remember{ FocusRequester() }  // 2
    val focusRequesterCredentialName = remember { FocusRequester() }  // 3
    val focusToNone = 0
//    val focusToGitUrl = 1;
//    val focusToRepoName = 2;
    val focusToCredentialName = 3;
    val requireFocusTo = rememberSaveable{mutableIntStateOf(focusToNone)}  //初始值0谁都不聚焦，修改后的值： 1聚焦url；2聚焦仓库名；3聚焦凭据名

    val httpOrHttps = stringResource(R.string.http_https)
    val ssh = stringResource(R.string.ssh)
//    val selectCredential = stringResource(R.string.select_credential)

    val optNumHttp = 0
    val optNumSsh = 1

    // 20240414 废弃ssh支持，修改开始
//    val radioOptions = listOf(httpOrHttps, ssh)  // 编号: 文本  // ssh
    val radioOptions = listOf(httpOrHttps)  // nossh
    // 20240414 废弃ssh支持，修改结束

    val credentialSelectedOption = rememberSaveable{mutableIntStateOf(optNumHttp)}

    val isReadyForSave = rememberSaveable { mutableStateOf(false)}

    val passwordVisible = rememberSaveable { mutableStateOf(false)}

//    val dropDownMenuExpendState = rememberSaveable{ mutableStateOf(false) }

//    val showRepoNameAlreadyExistsErr = rememberSaveable { mutableStateOf(false) }
    val showCredentialNameAlreadyExistsErr = rememberSaveable { mutableStateOf(false)}
//    val showRepoNameHasBadCharsErr = rememberSaveable { mutableStateOf(false) }

    val updateCredentialName:(TextFieldValue)->Unit = {
        val newVal = it
        val oldVal = credentialName.value

        if(oldVal.text != newVal.text) {
            if (showCredentialNameAlreadyExistsErr.value) {
                showCredentialNameAlreadyExistsErr.value = false
            }
        }

        credentialName.value = newVal
    }
    val setCredentialNameExistAndFocus:()->Unit = {
        //设置错误state
        showCredentialNameAlreadyExistsErr.value=true

        //全选输入框字符
        val text = credentialName.value.text
        credentialName.value = credentialName.value.copy(
            //设置选择范围
            selection = TextRange(0, text.length)
        )

        //另一种写法，测试过，可行，上面的copy方法内部其实就是这么实现的，差不多
//        credentialName.value = TextFieldValue(text = credentialName.value.text,
//                                          selection = TextRange(0, credentialName.value.text.length)
//                                          )

        //聚焦输入框，弹出键盘（如果本身光标就在输入框，则不会弹出键盘）
        requireFocusTo.intValue = focusToCredentialName
    }

    val showLoadingDialog = rememberSaveable { mutableStateOf(false)}

    val doSave:()->Unit = {
        doJobThenOffLoading launch@{
            showLoadingDialog.value=true

            val credentialDb = AppModel.singleInstanceHolder.dbContainer.credentialRepository

            //如果选择的是新建Credential，则新建
            val credentialNameText = credentialName.value.text
            val oldCredentialName = oldCredential.value.name
            if(!isEditMode.value || credentialNameText != oldCredentialName) {  //只有当非编辑模式或者新旧名称不一样时才检查是否重名(新建模式必检查是否重名，编辑模式允许和当前条目旧名一样)
                //检查是否重名
                val isCredentialNameExist = credentialDb.isCredentialNameExist(credentialNameText)
                if(isCredentialNameExist) {  //如果名称存在则返回
                    setCredentialNameExistAndFocus()  //设置界面显示错误
                    showLoadingDialog.value=false
                    return@launch
                }

            }

            val credentialForSave = if(isEditMode.value) credentialInThisPage.value else CredentialEntity()

            credentialForSave.name = credentialNameText
            credentialForSave.value = credentialVal.value
            credentialForSave.pass = credentialPass.value
            credentialForSave.type = credentialType.intValue

            //编辑模式则更新，否则插入
            if(isEditMode.value) {
                val oldPass = oldCredential.value.pass
                val newPass = credentialForSave.pass
                if(oldPass != newPass) {  //新旧密码如果不一样，说明用户编辑了密码，这时候需要重新加密；如果一样，说明依然是编辑前的加密过的密码，则不需要加密；（不太可能发生的极特殊情况：用户输入的新密码原文和加密后的密码字符串完全一样！这样就会出问题，连接时会使用“解密”后的错误密码）
                    credentialDb.encryptPassIfNeed(credentialForSave)
                }
                credentialDb.update(credentialForSave)
            }else{  //新建的百分百加密
                credentialDb.insertWithEncrypt(credentialForSave)
            }

            showLoadingDialog.value=false

            //设置此变量，下次重新渲染就会直接返回上级页面了
//            isTimeNaviUp.value = true
            withContext(Dispatchers.Main) {
                naviUp()
            }
        }
    }
    val loadingText = rememberSaveable { mutableStateOf(appContext.getString(R.string.loading))}

    val spacerPadding = 2.dp
    Scaffold(
        modifier = Modifier.nestedScroll(homeTopBarScrollBehavior.nestedScrollConnection),
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.primary,
                ),
                title = {
                    if(isEditMode.value){
                        Text(stringResource(R.string.edit_credential))
                    }else{
                        Text(stringResource(R.string.new_credential))
                    }
                },
                navigationIcon = {
                    LongPressAbleIconBtn(
                        tooltipText = stringResource(R.string.back),
                        icon = Icons.AutoMirrored.Filled.ArrowBack,
                        iconContentDesc = stringResource(R.string.back),

                        ) {
                        naviUp()
                    }
                },
                actions = {
                    LongPressAbleIconBtn(
                        tooltipText = stringResource(R.string.save),
                        icon =  Icons.Filled.Check,
                        iconContentDesc = stringResource(id = R.string.save),
                        enabled = isReadyForSave.value,

                        ) {
                        doSave()
                    }
                },
                scrollBehavior = homeTopBarScrollBehavior,
            )
        },
    ){contentPadding->
        //遮罩loading，这里不用做if loading else show page 的判断，直接把loading遮盖在页面上即可
//        showLoadingDialog.value=true  //test

        if (showLoadingDialog.value) {
            LoadingDialog(loadingText.value)
        }

        Column (modifier = Modifier
            .padding(contentPadding)
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(bottom = MyStyleKt.Padding.PageBottom)  //这个padding是为了使密码框不在底部，类似vscode中文件的最后一行也可滑到屏幕中间一样的意义
        ){

                //显示新建credential的输入框
                TextField(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(10.dp)
                        .focusRequester(focusRequesterCredentialName)
                    ,
                    isError = showCredentialNameAlreadyExistsErr.value,
                    supportingText = {
                        if (showCredentialNameAlreadyExistsErr.value) {
                            Text(
                                modifier = Modifier.fillMaxWidth(),
                                text = stringResource(R.string.credential_name_exists_err),
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    },
                    trailingIcon = {
                        if (showCredentialNameAlreadyExistsErr.value)
                            Icon(imageVector=Icons.Filled.Error,
                                contentDescription= stringResource(R.string.credential_name_exists_err),
                                tint = MaterialTheme.colorScheme.error)
                    },
                    singleLine = true,

                    value = credentialName.value,
                    onValueChange = {
                        updateCredentialName(it)
                    },
                    label = {
                        Text(stringResource(R.string.credential_name))
                    },
                    placeholder = {
                        Text(stringResource(R.string.credential_name_placeholder))
                    }
                )
//            Row (modifier = Modifier.padding(10.dp)){
//                Text(text = stringResource(id = R.string.type)+":")
//            }
            for ((k, optext) in radioOptions.toList().withIndex()) {
                Row(
                    Modifier
                        .fillMaxWidth()
                        .heightIn(min = MyStyleKt.RadioOptions.minHeight)

                        .selectable(
                            selected = credentialSelectedOption.intValue == k,
                            onClick = {
                                //更新选择值
                                credentialSelectedOption.intValue = k
                                if (k == optNumHttp) {
                                    credentialType.intValue = Cons.dbCredentialTypeHttp
                                } else {
                                    credentialType.intValue = Cons.dbCredentialTypeSsh
                                }
                            },
                            role = Role.RadioButton
                        )
                        .padding(horizontal = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = credentialSelectedOption.intValue == k,
                        onClick = null // null recommended for accessibility with screenreaders
                    )
                    Text(
                        text = optext,
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.padding(start = 10.dp)
                    )
                }
            }

            TextField(
                    modifier =
                    //如果type是ssh，让private-key输入框高点
                    if(credentialType.intValue == Cons.dbCredentialTypeSsh) {
                        Modifier
                            .fillMaxWidth()
                            .heightIn(min = 300.dp, max = 300.dp)
                            .padding(10.dp)

                    }else{
                        Modifier
                            .fillMaxWidth()
                            .padding(10.dp)
                    }
                    ,
                    //如果是http的密码框，整成单行即可
                    singleLine = credentialType.intValue != Cons.dbCredentialTypeSsh,

                    value = credentialVal.value,
                    onValueChange = {
                        credentialVal.value=it
                    },
                    label = {
                        if(credentialType.intValue == Cons.dbCredentialTypeHttp) {
                            Text(stringResource(R.string.username))
                        }else{
                            Text(stringResource(R.string.private_key))
                        }
                    },
                    placeholder = {
                        if(credentialType.intValue == Cons.dbCredentialTypeHttp) {
                            Text(stringResource(R.string.username))
                        }else{
                            Text(stringResource(R.string.paste_your_private_key_here))
                        }
                    }
                )
                TextField(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(10.dp),
                    singleLine = true,
                    value = credentialPass.value,
                    onValueChange = {
                        credentialPass.value=it
                    },
                    label = {
                        if(credentialType.intValue == Cons.dbCredentialTypeHttp) {
                            Text(stringResource(R.string.password))
                        }else{
                            Text(stringResource(R.string.passphrase_if_have))
                        }
                    },
                    placeholder = {
                        if(credentialType.intValue == Cons.dbCredentialTypeHttp) {
                            Text(stringResource(R.string.password))
                        }else{
                            Text(stringResource(R.string.input_passphrase_if_have))
                        }
                    },
                    visualTransformation = if (passwordVisible.value) VisualTransformation.None else PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    trailingIcon = {
                        if(!isEditMode.value) {  //只有新建模式能查看密码，编辑模式不许看，避免被人偷看已保存的密码
                            val image = if (passwordVisible.value) Icons.Filled.Visibility
                            else Icons.Filled.VisibilityOff

                            // Please provide localized description for accessibility services
                            val description =
                                if (passwordVisible.value) stringResource(R.string.hide_password) else stringResource(
                                    R.string.show_password
                                )

                            IconButton(onClick = { passwordVisible.value = !passwordVisible.value }) {
                                // contentDescription is for accessibility
                                Icon(imageVector = image, contentDescription = description)
                            }
                        }
                    }

                )
        }
    }

    if(requireFocusTo.intValue==focusToCredentialName) {
        requireFocusTo.intValue=focusToNone
        focusRequesterCredentialName.requestFocus()
    }

    LaunchedEffect(Unit) {
//        MyLog.d(TAG, "#LaunchedEffect: credentialId=" + credentialId)
        //编辑已存在条目
        //      设置页面loading为true
        //      从数据库异步查询repo数据，更新页面state
        //      设置页面loading 为false
        doJobThenOffLoading(
            loadingOn = { showLoadingDialog.value = true },
            loadingOff = { showLoadingDialog.value = false }
        ) job@{
            if (credentialId != null && credentialId.isNotBlank() && credentialId != "null") {  //如果是编辑模式，查询仓库信息
                isEditMode.value = true
                val credentialDb = AppModel.singleInstanceHolder.dbContainer.credentialRepository

                //注意，查出的是加密的密码
                credentialInThisPage.value = credentialDb.getById(credentialId)?:return@job
                oldCredential.value = credentialInThisPage.value.copy()  //存个旧的信息，用来检查是否要跳过部分重名检测和是否需要加密密码
                credentialType.intValue=credentialInThisPage.value.type
                credentialSelectedOption.intValue = if(credentialType.intValue == Cons.dbCredentialTypeHttp) optNumHttp else optNumSsh  //设置凭据类型选中项为数据库中的类型
                credentialName.value = TextFieldValue(credentialInThisPage.value.name)
                credentialVal.value = credentialInThisPage.value.value;
                credentialPass.value = credentialInThisPage.value.pass
            } else {  //如果是新增模式，简单聚焦下第一个输入框，弹出键盘即可
                isEditMode.value = false
                //聚焦第一个输入框，算了不聚焦了，在协程里聚焦会报异常，虽然可以设置状态，然后在compose里判断，聚焦，再把状态关闭，但是，太麻烦了，而且感觉聚焦与否其实意义不大，甚至就连报错时的聚焦意义都不大，不过报错时的聚焦不需要在协程里执行也不会抛异常，所以暂且保留
                requireFocusTo.intValue = focusToCredentialName
            }

        }
    }


    //判定是否启用执行克隆的按钮，每次状态改变重新渲染页面都会执行这段代码更新此值
    //名称必填+类型必须是ssh或http+如果是ssh则必填privatekey字段+如果是http则必填密码字段
    isReadyForSave.value = (
            (!showCredentialNameAlreadyExistsErr.value)
           && (credentialName.value.text.isNotBlank())
           && ((credentialType.intValue==Cons.dbCredentialTypeSsh && credentialVal.value.isNotBlank()) || (credentialType.intValue==Cons.dbCredentialTypeHttp && credentialPass.value.isNotBlank())))

}

