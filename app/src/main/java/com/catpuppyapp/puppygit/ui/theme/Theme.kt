package com.catpuppyapp.puppygit.ui.theme

import android.app.Activity
import android.content.Context
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import com.catpuppyapp.puppygit.play.pro.R

object Theme {
    val Orange = Color(0xFFFF5722)
    val darkLightBlue = Color(0xDF406060)



    const val auto = 0
    const val light = 1
    const val dark = 2

//    val invalidThemeValue: Int = -1
    const val defaultThemeValue: Int = auto // default is auto

    // this value will update when theme ready
    var inDarkTheme = false

    val themeList = listOf(
        auto,  // auto
        light,  // light
        dark,  // dark
    )

    fun getThemeTextByCode(themeCode:Int?, appContext: Context):String {
        if(themeCode== auto) {
            return appContext.getString(R.string.auto)
        }else if(themeCode == light) {
            return appContext.getString(R.string.light)
        }else if(themeCode == dark) {
            return appContext.getString(R.string.dark)
        }else {
            return appContext.getString(R.string.unknown)
        }

    }
}

private val DarkColorScheme = darkColorScheme(
    primary = Purple80,
    secondary = PurpleGrey80,
    tertiary = Pink80
)

private val LightColorScheme = lightColorScheme(
    primary = Purple40,
    secondary = PurpleGrey40,
    tertiary = Pink40

    /* Other default colors to override
    background = Color(0xFFFFFBFE),
    surface = Color(0xFFFFFBFE),
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.White,
    onBackground = Color(0xFF1C1B1F),
    onSurface = Color(0xFF1C1B1F),
    */
)

@Composable
fun PuppyGitAndroidTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    //TODO 实现手动切换是否暗黑主题，改下这里即可
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) {
                Theme.inDarkTheme = true;
                dynamicDarkColorScheme(context)
            }else {
                Theme.inDarkTheme=false
                dynamicLightColorScheme(context)
            }
        }

        darkTheme -> {
            Theme.inDarkTheme=true;
            DarkColorScheme
        }

        else -> {
            Theme.inDarkTheme = false;
            LightColorScheme
        }
    }
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.primary.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = darkTheme
        }
    }

    //test
//    Theme.inDarkTheme=true
    //test

    MaterialTheme(
        //test
//        colorScheme = DarkColorScheme,
        //test
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
