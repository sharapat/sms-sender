package com.example.smssender

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.core.content.ContextCompat.checkSelfPermission

object PermissionUtils {

    val READ_EXTERNAL_STORAGE = 234
    val WRITE_EXTERNAL_STORAGE = 18236
    val CAMERA = 235
    val SEND_SMS = 240
    val READ_PHONE_STATE = 241
    val LOCATION = 236
    val RECORD_AUDIO = 237
    var READ_CONTACTS = 238
    var RECEIVE_SMS = 239
    val permissions: HashMap<Int, String> = hashMapOf(
        READ_EXTERNAL_STORAGE to Manifest.permission.READ_EXTERNAL_STORAGE,
        WRITE_EXTERNAL_STORAGE to Manifest.permission.WRITE_EXTERNAL_STORAGE,
        READ_CONTACTS to Manifest.permission.READ_CONTACTS,
        CAMERA to Manifest.permission.CAMERA,
        RECEIVE_SMS to Manifest.permission.RECEIVE_SMS,
        SEND_SMS to Manifest.permission.SEND_SMS,
        READ_PHONE_STATE to Manifest.permission.READ_PHONE_STATE
    )

    fun askPermissions(activity: Activity, requestCode: Int): Boolean {
        permissions[requestCode]?.let { permissionString ->
            if (checkSelfPermission(activity, permissionString) != PackageManager.PERMISSION_GRANTED ||
                    checkSelfPermission(activity, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
                activity.requestPermissions(arrayOf(permissionString, Manifest.permission.READ_PHONE_STATE), requestCode)
            } else {
                return true
            }
        }
        return false
    }



    fun onRequestPermissionsResult(requestCode: Int, grantResults: IntArray, currentPermission: Int): Boolean {
        if (requestCode == currentPermission) {
            return (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)
        }
        return false
    }

    fun openPermissionSettings(activity: Activity) {
        val intent = Intent()
        intent.action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
        val uri = Uri.fromParts("package", activity.packageName, null)
        intent.data = uri
        activity.startActivity(intent)
    }
}