import android.content.Context
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.catpuppyapp.puppygit.compose.SingleSelectList
import com.catpuppyapp.puppygit.play.pro.R
import com.catpuppyapp.puppygit.settings.SettingsUtil
import com.catpuppyapp.puppygit.ui.theme.Theme
import com.catpuppyapp.puppygit.utils.ComposeHelper
import com.catpuppyapp.puppygit.utils.LanguageUtil
import com.catpuppyapp.puppygit.utils.state.mutableCustomStateOf

private val stateKeyTag = "SettingsInnerPage"
private val TAG = "SettingsInnerPage"

@Composable
fun SettingsInnerPage(
    contentPadding: PaddingValues,
    needRefreshPage:MutableState<String>,
    appContext:Context,
    openDrawer:()->Unit,
    exitApp:()->Unit,
    listState:ScrollState
){

    val settingsState = mutableCustomStateOf(stateKeyTag, "settingsState", SettingsUtil.getSettingsSnapshot())

    val themeList = listOf(
        stringResource(R.string.auto),
        stringResource(R.string.light),
        stringResource(R.string.dark),
    )
    val selectedTheme = rememberSaveable { mutableIntStateOf(settingsState.value.theme) }

    val languageList = LanguageUtil.languageCodeList

    val selectedLanguage = rememberSaveable { mutableStateOf(LanguageUtil.get(appContext)) }

    //back handler block start
    val isBackHandlerEnable = rememberSaveable { mutableStateOf(true)}
    val backHandlerOnBack = ComposeHelper.getDoubleClickBackHandler(appContext = appContext, openDrawer = openDrawer, exitApp= exitApp)
    //注册BackHandler，拦截返回键，实现双击返回和返回上级目录
    BackHandler(enabled = isBackHandlerEnable.value, onBack = {backHandlerOnBack()})
    //back handler block end

    Column(
        modifier = Modifier
            .padding(contentPadding)
            .fillMaxSize()
            .verticalScroll(listState)
    ) {
        SettingsTitle(stringResource(R.string.general))

        SettingsContent {
            Column {
                Text(stringResource(R.string.theme), fontSize = 20.sp)
                Text(stringResource(R.string.require_restart_app), fontSize = 12.sp, fontWeight = FontWeight.Light)
            }

            Column(modifier = Modifier.width(100.dp)) {
                SingleSelectList(optionsList = themeList, selectedOptionIndex = selectedTheme,
                    menuItemOnClick = { index, value ->
                        selectedTheme.intValue = index

                        settingsState.value.theme = index

                        SettingsUtil.update {
                            it.theme = index
                        }
                    }
                )
            }
        }


        SettingsContent {
            Column {
                Text(stringResource(R.string.language), fontSize = 20.sp)
                Text(stringResource(R.string.require_restart_app), fontSize = 12.sp, fontWeight = FontWeight.Light)
            }

            Column(modifier = Modifier.width(120.dp)) {
                SingleSelectList(
                    optionsList = languageList,
                    selectedOptionIndex = null,
                    selectedOptionValue = selectedLanguage.value,
                    menuItemOnClick = { index, value ->
                        selectedLanguage.value = value

                        LanguageUtil.set(appContext, value)
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

    }


    LaunchedEffect(needRefreshPage) {
        settingsState.value = SettingsUtil.getSettingsSnapshot()

    }
}

@Composable
fun SettingsTitle(text:String){
    val inDarkTheme = Theme.inDarkTheme
    Row(modifier = Modifier.background(color = if(inDarkTheme) Color.DarkGray else Color.LightGray).fillMaxWidth().padding(start = 5.dp),
        horizontalArrangement = Arrangement.Start,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text)
    }
}

@Composable
fun SettingsContent(content:@Composable ()->Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().defaultMinSize(minHeight = 60.dp).padding(10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        content()
    }
    HorizontalDivider()
}
