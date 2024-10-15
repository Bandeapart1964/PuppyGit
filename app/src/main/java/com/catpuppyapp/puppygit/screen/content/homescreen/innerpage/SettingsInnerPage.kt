import android.app.Activity
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ToggleOff
import androidx.compose.material.icons.filled.ToggleOn
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.catpuppyapp.puppygit.compose.ConfirmDialog2
import com.catpuppyapp.puppygit.compose.MyCheckBox
import com.catpuppyapp.puppygit.compose.PaddingRow
import com.catpuppyapp.puppygit.compose.ScrollableColumn
import com.catpuppyapp.puppygit.compose.SingleSelectList
import com.catpuppyapp.puppygit.play.pro.R
import com.catpuppyapp.puppygit.settings.SettingsUtil
import com.catpuppyapp.puppygit.ui.theme.Theme
import com.catpuppyapp.puppygit.utils.AppModel
import com.catpuppyapp.puppygit.utils.ComposeHelper
import com.catpuppyapp.puppygit.utils.LanguageUtil
import com.catpuppyapp.puppygit.utils.Msg
import com.catpuppyapp.puppygit.utils.MyLog
import com.catpuppyapp.puppygit.utils.doJobThenOffLoading
import com.catpuppyapp.puppygit.utils.fileopenhistory.FileOpenHistoryMan
import com.catpuppyapp.puppygit.utils.getStoragePermission
import com.catpuppyapp.puppygit.utils.state.mutableCustomStateOf
import com.catpuppyapp.puppygit.utils.storagepaths.StoragePathsMan

private val stateKeyTag = "SettingsInnerPage"
private val TAG = "SettingsInnerPage"

@Composable
fun SettingsInnerPage(
    contentPadding: PaddingValues,
    needRefreshPage:MutableState<String>,
//    appContext:Context,
    openDrawer:()->Unit,
    exitApp:()->Unit,
    listState:ScrollState
){

    val appContext = LocalContext.current

    val settingsState = mutableCustomStateOf(stateKeyTag, "settingsState", SettingsUtil.getSettingsSnapshot())

    val themeList = listOf(
        stringResource(R.string.auto),
        stringResource(R.string.light),
        stringResource(R.string.dark),
    )
    val selectedTheme = rememberSaveable { mutableIntStateOf(settingsState.value.theme) }

    val languageList = LanguageUtil.languageCodeList
    val selectedLanguage = rememberSaveable { mutableStateOf(LanguageUtil.get(appContext)) }

    val logLevelList = MyLog.logLevelList
    val selectedLogLevel = rememberSaveable { mutableStateOf(MyLog.getCurrentLogLevel()) }

    val enableEditCache = rememberSaveable { mutableStateOf(settingsState.value.editor.editCacheEnable) }
    val enableSnapshot_File = rememberSaveable { mutableStateOf(settingsState.value.editor.enableFileSnapshot) }
    val enableSnapshot_Content = rememberSaveable { mutableStateOf(settingsState.value.editor.enableContentSnapshot) }

    val groupContentByLineNum = rememberSaveable { mutableStateOf(settingsState.value.diff.groupDiffContentByLineNum) }

    val showCleanDialog = rememberSaveable { mutableStateOf(false) }
    val cleanCacheFolder = rememberSaveable { mutableStateOf(true) }
    val cleanEditCache = rememberSaveable { mutableStateOf(true) }
    val cleanSnapshot = rememberSaveable { mutableStateOf(true) }
    val cleanLog = rememberSaveable { mutableStateOf(true) }
//    val cleanContentSnapshot = rememberSaveable { mutableStateOf(true) }
    val cleanStoragePath = rememberSaveable { mutableStateOf(false) }
    val cleanFileOpenHistory = rememberSaveable { mutableStateOf(false) }


    if(showCleanDialog.value) {
        ConfirmDialog2(
            title = stringResource(R.string.clean),
            requireShowTextCompose = true,
            textCompose = {
                ScrollableColumn {
                    MyCheckBox(stringResource(R.string.log), cleanLog)
                    MyCheckBox(stringResource(R.string.cache), cleanCacheFolder)
                    MyCheckBox(stringResource(R.string.edit_cache), cleanEditCache)
                    MyCheckBox(stringResource(R.string.all_snapshots), cleanSnapshot)  // file snapshots and content snapshots all will be delete
                    MyCheckBox(stringResource(R.string.storage_paths), cleanStoragePath)
                    if(cleanStoragePath.value) {
                        Text(stringResource(R.string.the_storage_path_are_the_paths_you_chosen_and_added_when_cloning_repo), fontWeight = FontWeight.Light)
                    }
                    MyCheckBox(stringResource(R.string.file_opened_history), cleanFileOpenHistory)
                    if(cleanFileOpenHistory.value) {
                        Text(stringResource(R.string.this_include_editor_opened_files_history_and_their_last_edited_position), fontWeight = FontWeight.Light)
                    }
                }
            },
            onCancel = {showCleanDialog.value = false}
        ) {
            showCleanDialog.value=false

            doJobThenOffLoading {
                Msg.requireShow(appContext.getString(R.string.cleaning))

                if(cleanLog.value) {
                    try {
                        AppModel.singleInstanceHolder.getOrCreateLogDir().deleteRecursively()
                        AppModel.singleInstanceHolder.getOrCreateLogDir()
                    }catch (e:Exception) {
                        MyLog.e(TAG, "clean log err: ${e.localizedMessage}")
                    }
                }

                if(cleanCacheFolder.value) {
                    try {
                        AppModel.singleInstanceHolder.externalCacheDir.deleteRecursively()
                        AppModel.singleInstanceHolder.externalCacheDir.mkdirs()
                    }catch (e:Exception) {
                        MyLog.e(TAG, "clean cache folder err: ${e.localizedMessage}")
                    }
                }

                if(cleanEditCache.value) {
                    try {
                        AppModel.singleInstanceHolder.getOrCreateEditCacheDir().deleteRecursively()
                        AppModel.singleInstanceHolder.getOrCreateEditCacheDir()
                    }catch (e:Exception) {
                        MyLog.e(TAG, "clean edit cache err: ${e.localizedMessage}")
                    }
                }

                if(cleanSnapshot.value) {
                    try {
                        AppModel.singleInstanceHolder.getOrCreateFileSnapshotDir().deleteRecursively()
                        AppModel.singleInstanceHolder.getOrCreateFileSnapshotDir()
                    }catch (e:Exception) {
                        MyLog.e(TAG, "clean file and content snapshot err: ${e.localizedMessage}")
                    }
                }


                if(cleanStoragePath.value) {
                    try {
                        StoragePathsMan.reset()
                    }catch (e:Exception) {
                        MyLog.e(TAG, "clean storage paths err: ${e.localizedMessage}")
                    }
                }

                if(cleanFileOpenHistory.value) {
                    try {
                        FileOpenHistoryMan.reset()
                    }catch (e:Exception) {
                        MyLog.e(TAG, "clean file opened history err: ${e.localizedMessage}")
                    }
                }

                Msg.requireShow(appContext.getString(R.string.success))
            }
        }
    }


    //back handler block start
    val isBackHandlerEnable = rememberSaveable { mutableStateOf(true)}
    val backHandlerOnBack = ComposeHelper.getDoubleClickBackHandler(appContext = appContext, openDrawer = openDrawer, exitApp= exitApp)
    //注册BackHandler，拦截返回键，实现双击返回和返回上级目录
    BackHandler(enabled = isBackHandlerEnable.value, onBack = {backHandlerOnBack()})
    //back handler block end

    val itemFontSize = 20.sp
    val itemDescFontSize = 12.sp
    val switcherIconSize = 60.dp


    Column(
        modifier = Modifier
            .padding(contentPadding)
            .fillMaxSize()
            .verticalScroll(listState)
    ) {
        SettingsTitle(stringResource(R.string.general))

        SettingsContent {
            Column {
                Text(stringResource(R.string.theme), fontSize = itemFontSize)
                Text(stringResource(R.string.require_restart_app), fontSize = itemDescFontSize, fontWeight = FontWeight.Light, fontStyle = FontStyle.Italic)
            }

            Column(modifier = Modifier.width(100.dp)) {
                SingleSelectList(optionsList = themeList, selectedOptionIndex = selectedTheme,
                    menuItemOnClick = { index, value ->
                        selectedTheme.intValue = index

                        if(index != settingsState.value.theme) {
                            settingsState.value.theme = index

                            SettingsUtil.update {
                                it.theme = index
                            }
                        }
                    }
                )
            }
        }


        SettingsContent {
            Column {
                Text(stringResource(R.string.language), fontSize = itemFontSize)
                Text(stringResource(R.string.require_restart_app), fontSize = itemDescFontSize, fontWeight = FontWeight.Light, fontStyle = FontStyle.Italic)
            }

            Column(modifier = Modifier.width(120.dp)) {
                SingleSelectList(
                    optionsList = languageList,
                    selectedOptionIndex = null,
                    selectedOptionValue = selectedLanguage.value,
                    menuItemOnClick = { index, value ->
                        selectedLanguage.value = value

                        if(value != LanguageUtil.get(appContext)) {
                            LanguageUtil.set(appContext, value)
                        }
                    },
                    menuItemSelected = {index, value ->
                        value == selectedLanguage.value
                    },
                    menuItemFormatter = { index, value ->
                        LanguageUtil.getLanguageTextByCode(value?:"", appContext)
                    }
                )
            }
        }
        SettingsContent {
            Column {
                Text(stringResource(R.string.log_level), fontSize = itemFontSize)
//                Text(stringResource(R.string.require_restart_app), fontSize = itemDescFontSize, fontWeight = FontWeight.Light, fontStyle = FontStyle.Italic)
            }

            Column(modifier = Modifier.width(120.dp)) {
                SingleSelectList(
                    optionsList = logLevelList,
                    selectedOptionIndex = null,
                    selectedOptionValue = selectedLogLevel.value,
                    menuItemOnClick = { index, value ->
                        selectedLogLevel.value = value

                        if(value != MyLog.getCurrentLogLevel()) {
                            // set in memory
                            val newLevel = value.get(0)
                            MyLog.setLogLevel(newLevel)

                            // this update or not update, all fine
                            //这个更新与否其实没差，因为在这用不到，而从其他地方获取的settings也是重新读取的新实例
                            settingsState.value.logLevel=newLevel

                            // save to disk
                            SettingsUtil.update {
                                it.logLevel = newLevel
                            }
                        }
                    },
                    menuItemSelected = {index, value ->
                        value == selectedLogLevel.value
                    },
                    menuItemFormatter = { index, value ->
                        MyLog.getTextByLogLevel(value?:"", appContext)
                    }
                )
            }
        }



        SettingsTitle(stringResource(R.string.editor))

        SettingsContent(onClick = {
            val newValue = !enableEditCache.value

            //save
            enableEditCache.value = newValue
            SettingsUtil.update {
                it.editor.editCacheEnable = newValue
            }
        }) {
            Column {
                Text(stringResource(R.string.edit_cache), fontSize = itemFontSize)
                Text(stringResource(R.string.require_restart_app), fontSize = itemDescFontSize, fontWeight = FontWeight.Light, fontStyle = FontStyle.Italic)
            }

            Icon(
                modifier = Modifier.size(switcherIconSize),
                imageVector = if(enableEditCache.value) Icons.Filled.ToggleOn else Icons.Filled.ToggleOff,
                contentDescription = if(enableEditCache.value) stringResource(R.string.enable) else stringResource(R.string.disable),
                tint = if(enableEditCache.value) MaterialTheme.colorScheme.primary else LocalContentColor.current,
            )
        }

        SettingsContent(
            onClick = {
                val newValue = !enableSnapshot_File.value
                enableSnapshot_File.value = newValue
                SettingsUtil.update {
                    it.editor.enableFileSnapshot = newValue
                }
            }
        ) {
            Column {
                Text(stringResource(R.string.file_snapshot), fontSize = itemFontSize)
                Text(stringResource(R.string.before_opening_a_file_create_a_snapshot_first), fontSize = itemDescFontSize, fontWeight = FontWeight.Light)
                Text(stringResource(R.string.require_restart_app), fontSize = itemDescFontSize, fontWeight = FontWeight.Light, fontStyle = FontStyle.Italic)

            }

            Icon(
                modifier = Modifier.size(switcherIconSize),
                imageVector = if(enableSnapshot_File.value) Icons.Filled.ToggleOn else Icons.Filled.ToggleOff,
                contentDescription = if(enableSnapshot_File.value) stringResource(R.string.enable) else stringResource(R.string.disable),
                tint = if(enableSnapshot_File.value) MaterialTheme.colorScheme.primary else LocalContentColor.current,

            )
        }
        SettingsContent(
            onClick = {
                val newValue = !enableSnapshot_Content.value
                enableSnapshot_Content.value = newValue
                SettingsUtil.update {
                    it.editor.enableContentSnapshot = newValue
                }
            }
        ) {
            Column {
                Text(stringResource(R.string.content_snapshot), fontSize = itemFontSize)
                Text(stringResource(R.string.before_saving_a_file_create_a_snapshot_first), fontSize = itemDescFontSize, fontWeight = FontWeight.Light)
                Text(stringResource(R.string.require_restart_app), fontSize = itemDescFontSize, fontWeight = FontWeight.Light, fontStyle = FontStyle.Italic)

            }

            Icon(
                modifier = Modifier.size(switcherIconSize),
                imageVector = if(enableSnapshot_Content.value) Icons.Filled.ToggleOn else Icons.Filled.ToggleOff,
                contentDescription = if(enableSnapshot_Content.value) stringResource(R.string.enable) else stringResource(R.string.disable),
                tint = if(enableSnapshot_Content.value) MaterialTheme.colorScheme.primary else LocalContentColor.current,

            )
        }

        SettingsTitle(stringResource(R.string.diff))

        SettingsContent(onClick = {
            val newValue = !groupContentByLineNum.value

            groupContentByLineNum.value = newValue
            SettingsUtil.update {
                it.diff.groupDiffContentByLineNum = newValue
            }
        }) {
            Column {
                Text(stringResource(R.string.group_content_by_line_num), fontSize = itemFontSize)
//                Text(stringResource(R.string.before_saving_a_file_create_a_snapshot_first), fontSize = itemDescFontSize, fontWeight = FontWeight.Light)
//                Text(stringResource(R.string.require_restart_app), fontSize = itemDescFontSize, fontWeight = FontWeight.Light, fontStyle = FontStyle.Italic)

            }

            Icon(
                modifier = Modifier.size(switcherIconSize),
                imageVector = if(groupContentByLineNum.value) Icons.Filled.ToggleOn else Icons.Filled.ToggleOff,
                contentDescription = if(groupContentByLineNum.value) stringResource(R.string.enable) else stringResource(R.string.disable),
                tint = if(groupContentByLineNum.value) MaterialTheme.colorScheme.primary else LocalContentColor.current,

            )
        }

        SettingsTitle(stringResource(R.string.clean))
        SettingsContent(onClick = {
            showCleanDialog.value = true
        }) {
            Text(stringResource(R.string.clean), fontSize = itemFontSize)

        }

        SettingsTitle(stringResource(R.string.permissions))
        SettingsContent(onClick = {
            // grant permission for read/write external storage
            val activity = appContext as? Activity
            if (activity == null) {
                Msg.requireShowLongDuration(appContext.getString(R.string.please_go_to_settings_allow_manage_storage))
            }else {
                activity.getStoragePermission()
            }
        }) {
            Column {
                Text(stringResource(R.string.manage_storage), fontSize = itemFontSize)
                Text(stringResource(R.string.if_you_want_to_clone_repo_into_external_storage_this_permission_is_required), fontSize = itemDescFontSize, fontWeight = FontWeight.Light)
            }
        }

        PaddingRow()
    }


    LaunchedEffect(needRefreshPage) {
        settingsState.value = SettingsUtil.getSettingsSnapshot()

    }
}

@Composable
fun SettingsTitle(text:String){
    val inDarkTheme = Theme.inDarkTheme
    Row(modifier = Modifier
        .background(color = if (inDarkTheme) Color.DarkGray else Color.LightGray)
        .fillMaxWidth()
        .padding(start = 5.dp),
        horizontalArrangement = Arrangement.Start,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text)
    }
}

@Composable
fun SettingsContent(onClick:(()->Unit)?=null, content:@Composable ()->Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .defaultMinSize(minHeight = 60.dp)
            .then(if (onClick != null) Modifier.clickable { onClick() } else Modifier)
            .padding(10.dp)
        ,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        content()
    }
    HorizontalDivider()
}
