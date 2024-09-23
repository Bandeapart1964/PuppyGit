package com.catpuppyapp.puppygit.utils

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext


object ActivityUtil {

    @Composable
    fun getCurrentActivity(): Activity? {
        val context = LocalContext.current
        return context as? Activity
    }

    //貌似必须在主线程执行此方法
    //这个不一定能显示文件是否保存的toast
    //注：recreate适用于 Build.VERSION.SDK_INT >= 11
    fun restartActivityByRecreate(activity: Activity) {
        activity.recreate()
    }

    //这个重启几乎能百分百显示是否保存的Toast
    fun restartActivityByIntent(activity:Activity) {
        activity.apply {
            val intent = getIntent()
//            intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)  //禁用切换动画，不知道禁用有什么意义，所以注释了
            finish()
//            overridePendingTransition(0, 0)  //禁用切换动画
            startActivity(intent)
//            overridePendingTransition(0, 0)  //禁用切换动画
        }

    }

    //打开网址、mailto链接等url，但因为url和uri太像，所以参数名改成linkUrl了
    fun openUrl(context: Context, linkUrl:String) {
        val uri = Uri.parse(linkUrl)
        val intent = Intent(Intent.ACTION_VIEW, uri)
        context.startActivity(intent)
    }

}
