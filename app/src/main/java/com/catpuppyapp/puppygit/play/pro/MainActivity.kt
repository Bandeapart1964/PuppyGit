package com.catpuppyapp.puppygit.play.pro

import android.content.Context
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.compositionContext
import androidx.compose.ui.platform.createLifecycleAwareWindowRecomposer
import androidx.core.view.WindowCompat
import com.catpuppyapp.puppygit.compose.LoadingText
import com.catpuppyapp.puppygit.screen.AppScreenNavigator
import com.catpuppyapp.puppygit.settings.SettingsUtil
import com.catpuppyapp.puppygit.ui.theme.PuppyGitAndroidTheme
import com.catpuppyapp.puppygit.user.UserUtil
import com.catpuppyapp.puppygit.utils.AppModel
import com.catpuppyapp.puppygit.utils.LanguageUtil
import com.catpuppyapp.puppygit.utils.MyLog
import com.catpuppyapp.puppygit.utils.doJobThenOffLoading
import com.catpuppyapp.puppygit.utils.showToast
import kotlinx.coroutines.CoroutineExceptionHandler
import java.util.Locale


private val TAG = "MainActivity"

// use `ActivityUtil.getCurrentActivity()` instead
//fun Context.findActivity(): Activity? = when (this) {
//    is Activity -> this
//    is ContextWrapper -> baseContext.findActivity()
//    else -> null
//}
////
//fun Context.setAppLocale(language: String): Context {
//    val locale = Locale(language)
//    Locale.setDefault(locale)
//    val config = resources.configuration
//    config.setLocale(locale)
//    config.setLayoutDirection(locale)
//    return createConfigurationContext(config)
//}

class MainActivity : ComponentActivity() {

    @OptIn(ExperimentalComposeUiApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        val funName = "onCreate"

        //20240519 上午: start: 尝试解决谷歌自动测试时的bug，什么gms err之类的
        //20240519 下午：更新：注释了这段代码，再上传，没报错。
        //20240519: 好像和这个无关，？参见未解决的问题文档，搜“play console测试莫名其妙报错 gms相关 原因不明 20240519”
//        val threadPolicy = StrictMode.ThreadPolicy.Builder()
//            .permitDiskReads()
//            .permitCustomSlowCalls()
////            .permitDiskWrites() // If you also want to ignore DiskWrites, Set this line too.
//            .build();
//        StrictMode.setThreadPolicy(threadPolicy);
        //20240519: end: 尝试解决谷歌自动测试时的bug，什么gms err之类的

        super.onCreate(savedInstanceState)

        //打印广告id，需要google play service
//        doJobThenOffLoading {
//            val adClient = AdvertisingIdClient.getAdvertisingIdInfo(applicationContext)
//            // AdLimit为true代表用户退出个性化广告，即限制广告跟踪
//            MyLog.d(TAG, "AdId:${adClient.id}, AdLimit:${adClient.isLimitAdTrackingEnabled}")
//        }

//        println("applicationContext.filesDir:"+applicationContext.filesDir)
//        println("applicationContext.dataDir:"+applicationContext.dataDir)

//        println("Environment.getExternalStorageDirectory()="+Environment.getExternalStorageDirectory())  // /storage/emulated/0
//        println("applicationContext.filesDir="+applicationContext.filesDir)  // /data/user/0/com.catpuppyapp.puppygit/files
//        println("applicationContext.getExternalFilesDir(null)="+applicationContext.getExternalFilesDir(null))  // /storage/emulated/0/Android/data/com.catpuppyapp.puppygit/files
//        println("getShortUuid():::"+getShortUuid())

        AppModel.init_1(applicationContext = applicationContext, exitApp = {finish()})

        //for make imePadding() work
        WindowCompat.setDecorFitsSystemWindows(window, false)

        // for catch exception, block start。( refer: https://stackoverflow.com/questions/76061623/how-to-restart-looper-when-exception-throwed-in-jetpack-compose
        val recomposer = window.decorView.createLifecycleAwareWindowRecomposer(
            CoroutineExceptionHandler { coroutineContext, throwable ->
                try {
    //                throwable.printStackTrace();
                    MyLog.e(TAG, "#$funName err: "+throwable.stackTraceToString())

                    //出错提示下用户就行，经我测试，画面会冻结，但数据不会丢，问题不大
                    showToast(applicationContext, getString(R.string.err_restart_app_may_resolve), Toast.LENGTH_LONG)  //测试了下，能显示Toast

                    //不重新创建Activity的话，页面会freeze，按什么都没响应，不过系统导航键还是可用的
                    //重新创建不一定成功，有可能会返回桌面
//                    ActivityUtil.restartActivityByIntent(this)

                    // 不重建Activity，直接退出
                    finish()

                    // 如果想显示错误弹窗，参见文档 “下一步-20240120.txt” 中的："compose错误处理 compose出错弹窗实现 20240505"

                }catch (e:Exception) {
                    e.printStackTrace()  //再出异常，管不了了，随便吧，打印下就行
                }
            }, lifecycle)

        // set window use our recomposer
        window.decorView.compositionContext = recomposer
        // for catch exception, block end

        val settings = SettingsUtil.getSettingsSnapshot()


        setContent {
            PuppyGitAndroidTheme(
                darkTheme = if(settings.theme == 0) isSystemInDarkTheme() else (settings.theme == 2)
            ) {
                MainCompose()
                //                Greeting(baseContext)
            }
        }

    }

    override fun attachBaseContext(newBase: Context) {
        val languageCode = LanguageUtil.get(newBase)
        if(LanguageUtil.isSupportLanguage(languageCode)) {
            // split language codes, e.g. split "zh-rCN" to "zh" and "CN"
            val (language, country) = LanguageUtil.splitLanguageCode(languageCode)
            val locale = if(country.isBlank()) Locale(language) else Locale(language, country)
            Locale.setDefault(locale)
            val config = newBase.resources.configuration
            config.setLocale(locale)
            config.setLayoutDirection(locale)
            val context = newBase.createConfigurationContext(config)
//            super.attachBaseContext(ContextWrapper(context))   // chatgpt say no need ContextWrapper in this usage case
            super.attachBaseContext(context)
        }else {  // auto detected or unsupported language
            super.attachBaseContext(newBase)
        }

    }

//
//    fun changeLanguage(language: String) {
//        // auto detect
//        if(language.isBlank()) {
//            return
//        }
//
//        // specified language
//        val locale = Locale(language)
//        Locale.setDefault(locale)
//        val resources: Resources = resources
//        val config: Configuration = resources.configuration
//        val dm = resources.displayMetrics
//        config.setLocale(locale)
//        config.setLayoutDirection(locale)
//        resources.updateConfiguration(config, dm)
//    }



}


@Composable
fun MainCompose() {
    val stateKeyTag = "MainCompose"

    val funName = "MainCompose"
    val appContext = LocalContext.current
    val loadingText = rememberSaveable { mutableStateOf(appContext.getString(R.string.launching))}

    val isInitDone = rememberSaveable { mutableStateOf(false) };


    //start: init user and billing state, 这代码位置别随便挪，必须早于调用Billing.init()，不然相关的集合无法得到更新
    val isProState = rememberSaveable { mutableStateOf(false) }
    UserUtil.updateUserStateToRememberXXXForPage(newIsProState = isProState)

    //end: init user and billing state


    //初始化完成显示app界面，否则显示loading界面
    if(isInitDone.value) {
        AppScreenNavigator()
    }else {
        //这个东西太阴间了，除非是真的需要确保阻止用户操作，例如保存文件，否则尽量别用这个
//        LoadingDialog(loadingText.value)
//        LoadingDialog()

        //TODO 把文字loading替换成有App Logo 的精美画面
        //这里用Scaffold是因为其会根据系统是否暗黑模式调整背景色，就不需要我自己判断了
        Scaffold { contentPadding ->
            LoadingText(contentPadding = contentPadding, text = loadingText.value)
        }
    }

    //compose创建时的副作用
    LaunchedEffect(Unit) {
//        println("LaunchedEffect传Unit只会执行一次，由于maincompose是app其他compose的根基，不会被反复创建销毁，所以maincompose里的launchedEffect只会执行一次，可以用来执行读取配置文件之类的初始化操作")
        try {
//        读取配置文件，初始化状态之类的操作，初始化时显示一个loading页面，完成后更新状态变量，接着加载app页面
            //初始化完成之后，设置变量，显示应用界面
            doJobThenOffLoading {
                isInitDone.value = false

                //test passed
//                assert(!MyLog.isInited)
                //test

                AppModel.init_2()

                //test passed
//                assert(MyLog.isInited)
                //test

                isInitDone.value = true
            }

        } catch (e: Exception) {
            MyLog.e(TAG, "#$funName err: "+e.stackTraceToString())
        }

        //test passed
//        delay(30*1000)
//        AppModel.singleInstanceHolder.exitApp()  //测试exitApp()，Editor未保存的数据是否会保存，结论：会
//        appContext.findActivity()?.recreate()  // 测试重建是否会保存数据，结论：会
//        throw RuntimeException("throw exception test")
        //test
    }
    //compose被销毁时执行的副作用
//    DisposableEffect(Unit) {
////        ("DisposableEffect: entered main")
//        onDispose {
////            ("DisposableEffect: exited main")
//        }
//    }

}

//@Preview(showBackground = true)
//@Composable
//fun GreetingPreview() {
//    PuppyGitAndroidTheme {
//        Greeting("Android")
//    }
//}

//一个演示方法
//@Composable
//fun MainScreen(navController: NavController) {
//    LaunchedEffect(Unit) {
//        println("LaunchedEffect: entered main")
//        var i = 0
//        // Just an example of coroutines usage
//        // don't use this way to track screen disappearance
//        // DisposableEffect is better for this
//        try {
//            while (true) {
//                delay(1000)
//                println("LaunchedEffect: ${i++} sec passed")
//            }
//        } catch (cancel: Exception) {
//            println("LaunchedEffect: job cancelled")
//        }
//    }
//    DisposableEffect(Unit) {
//        println("DisposableEffect: entered main")
//        onDispose {
//            println("DisposableEffect: exited main")
//        }
//    }
//}
