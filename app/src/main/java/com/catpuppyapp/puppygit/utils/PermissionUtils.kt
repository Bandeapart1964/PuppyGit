package com.catpuppyapp.puppygit.utils

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.core.app.ActivityCompat

/**
 * src: https://github.com/NeoApplications/Neo-Backup/blob/main/src/main/java/com/machiav3lli/backup/utils/PermissionUtils.kt
 */

// request code, will return when operation success? idk actually
const val READ_PERMISSION = 2
const val WRITE_PERMISSION = 3

fun Activity.getStoragePermission() {
    if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.R){
        val intent = Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION)
        intent.data = Uri.parse("package:$packageName")
        startActivity(intent)
    }

    else {
        requireWriteStoragePermission()
        requireReadStoragePermission()
    }
}

private fun Activity.requireReadStoragePermission() {
    if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) !=
        PackageManager.PERMISSION_GRANTED
    )
        ActivityCompat.requestPermissions(
            this,
            arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
            READ_PERMISSION
        )
}

private fun Activity.requireWriteStoragePermission() {
    if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) !=
        PackageManager.PERMISSION_GRANTED
    )
        ActivityCompat.requestPermissions(
            this,
            arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
            WRITE_PERMISSION
        )
}
