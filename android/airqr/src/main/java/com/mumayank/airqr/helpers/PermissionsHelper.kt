package com.mumayank.airqr.helpers

import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

class PermissionsHelper {
    companion object {

        private const val requestCode = 10
        private var permissions: Array<String>? = null
        private var onNotGranted: (() -> Unit)? = null
        private var onGranted: (() -> Unit)? = null

        fun onCreate(
            context: Context,
            permissions: Array<String>,
            onNotGranted: (() -> Unit)?,
            onGranted: (() -> Unit)?
        ) {
            Companion.onNotGranted = onNotGranted
            Companion.permissions = permissions
            Companion.onGranted = onGranted
            if (areAllPermissionsGranted(context)) {
                onGranted?.invoke()
            } else {
                ActivityCompat.requestPermissions(
                    context as Activity, permissions, requestCode
                )
            }
        }

        fun onRequestPermissionsResult(
            context: Context,
            requestCode: Int
        ) {
            if (requestCode == Companion.requestCode) {
                if (areAllPermissionsGranted(context)) {
                    onGranted?.invoke()
                } else {
                    onNotGranted?.invoke()
                }
            }
        }

        private fun areAllPermissionsGranted(
            context: Context
        ): Boolean {
            if (permissions == null) {
                return false
            }
            return (permissions as Array<String>).all {
                ContextCompat.checkSelfPermission(
                    context, it
                ) == PackageManager.PERMISSION_GRANTED
            }
        }

    }
}