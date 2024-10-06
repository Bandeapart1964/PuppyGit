package com.catpuppyapp.puppygit.style

import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.selection.TextSelectionColors
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

class MyStyleKt{

    object ClickableText{
        val style: TextStyle = TextStyle(textDecoration = TextDecoration.Underline)
        val color = Color(0xFF0096FF)
        val errColor = Color(0xFFFF5733)
        val minClickableSize = 25.dp
//        val modifier = Modifier.padding(start = 1.dp,top=15.dp, bottom = 0.dp, end=1.dp)
        //defaultMinSize为了确保基本的可点击范围，避免分支名字很小点不到的情况发生
        val modifier = Modifier.padding(start = 3.dp,top=0.dp, bottom = 0.dp, end=1.dp).defaultMinSize(minClickableSize)
        val modifierNoPadding = Modifier.defaultMinSize(minClickableSize)
//        val fontSize = 15.sp
        val textAlign = TextAlign.Center
    }
    object NormalText{
//        val modifier = Modifier.padding(start = 1.dp,top=15.dp, bottom = 0.dp, end=1.dp)
        //defaultMinSize为了确保基本的可点击范围，避免分支名字很小点不到的情况发生
        val modifier = Modifier.padding(start = 3.dp,top=0.dp, bottom = 0.dp, end=1.dp).defaultMinSize(25.dp)
//        val fontSize = 15.sp
    }

    object ChangeListItemColor {
        val added = Color(0xFF78ab78)
        val added_darkTheme = Color(0xFF1D4E1D)
        val modified = Color(0xFF426c95)
        val modified_darkTheme = Color(0xFF183653)
        val deleted = Color(0xFFbc6767)
        val deleted_darkTheme = Color(0xFF6D1818)
        val conflict = Color(0xFF913FA8)
        val conflict_darkTheme = Color(0xFF621E75)

    }

    object IconColor {
        val enable = Color(0xFF0F479B)
        val disable = Color.LightGray
        val disable_DarkTheme = Color(0xFF505050)
        val normal = Color(0xFF5F5F5F)
    }

    object TextColor {
        val enable = Color.Unspecified
        val disable = Color.LightGray
        val disable_DarkTheme = Color(0xFF505050)
        val highlighting_green =Color(0xFF4CAF50)

        //Editor font color
        val lineNum_forEditorInLightTheme = Color.DarkGray
        val lineNum_forEditorInDarkTheme = Color(0xFF535353)

        //DiffContent font color
        val lineNum_forDiffInLightTheme = Color.DarkGray
        val lineNum_forDiffInDarkTheme = Color(0xFF757575)

        val fontColor = Color.Unspecified
        val secondaryFontColor = Color.Gray
        val darkThemeFontColor = Color.Gray
        val darkThemeSecondaryFontColor = Color.DarkGray

        val error = Color.Red

        val danger = Color.Red
    }

    object TextSize {
        val default = 16.sp
        val lineNumSize = 10.sp
    }

    object Editor {
        val fontSize = 16.sp
        val bottomBarHeight = 80.dp
    }

    //只能在compose下获取这个颜色
//    bgColor = MaterialTheme.colorScheme.primaryContainer,  //标题栏背景色
//    titleColor = MaterialTheme.colorScheme.primary,  //标题栏文字颜色

    object BottomBar{
        val height=60.dp

        //使用BottomBar的那个页面，需要padding出这个高度，否则列表内容会被BottomBar盖住
        val outsideContentPadding = height+10.dp
    }


    object Fab {
        fun getFabModifierForEditor(isMultipleSelectionMode:Boolean):Modifier {
            return Modifier.imePadding().then(if(isMultipleSelectionMode) Modifier.padding(bottom = MyStyleKt.Editor.bottomBarHeight+20.dp) else Modifier)
        }

        fun getFabModifier():Modifier {
            //貌似Fab自带一点Padding，所以这里直接用BottomBar的高度即可，不需要再手动加padding
            return Modifier.padding(end = 20.dp, bottom = BottomBar.height)
        }
    }


    object RadioOptions{
        val minHeight=30.dp
    }

    object Title {
        val lineHeight = 20.dp

        //标题大字下面那行小字的字体大小
        val secondLineFontSize = 12.sp

        //可点击title的最小尺寸
        val clickableTitleMinWidth = 40.dp
    }

    object Padding {
        val PageBottom=500.dp
    }

    object TextSelectionColor {
        val customTextSelectionColors = TextSelectionColors(
            handleColor = Color(0xff4b6cc6),  //光标拖手颜色
            backgroundColor = Color(0xFF6A86D1),  //选中文本背景颜色
        )
        val customTextSelectionColors_darkMode = TextSelectionColors(
            handleColor = Color(0xFF1F3368),  //光标拖手颜色
            backgroundColor = Color(0xFF23376F),  //选中文本背景颜色
        )

        //隐藏光标拖手（拖柄）。注意：只是透明了，但实际仍存在且可点击和拖动
        val customTextSelectionColors_hideCursorHandle = TextSelectionColors(
            handleColor = Color.Transparent,  //光标拖手颜色
            backgroundColor = Color.Transparent,  //选中文本背景颜色
        )
    }

    object Icon {
        val size = 25.dp
        val modifier = Modifier.size(size)
    }

    object CheckoutBox {
//        val height = 56.dp
        val height = 40.dp

        val horizontalPadding = 16.dp
    }

    object TopBar {
        val dropDownMenuTopPaddingSize = 70.dp
    }

    object BottomSheet{
        val skipPartiallyExpanded = true
    }
}
