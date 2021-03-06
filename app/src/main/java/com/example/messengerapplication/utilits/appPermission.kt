package com.example.messengerapplication.utilits

import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

const val READ_CONTACTS = android.Manifest.permission.READ_CONTACTS
const val PER_REQUEST = 200

fun checkPermission(permission: String) : Boolean{
    return if(Build.VERSION.SDK_INT >= 23
        && ContextCompat.checkSelfPermission(appActivity, permission)!= PackageManager.PERMISSION_GRANTED){
        ActivityCompat.requestPermissions(appActivity, arrayOf(permission), PER_REQUEST)
        false
    }else true
}