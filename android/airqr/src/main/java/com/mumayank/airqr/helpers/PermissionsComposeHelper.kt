package com.mumayank.airqr.helpers

import androidx.compose.runtime.Composable
import com.google.accompanist.permissions.*

@OptIn(ExperimentalPermissionsApi::class)
class PermissionsComposeHelper {
    companion object {

        private var permissionState: PermissionState? = null
        private var multiplePermissionsState: MultiplePermissionsState? = null

        @Composable
        fun OnComposeCreate(
            permissions: Array<String>,
            onNotGranted: (() -> Unit)?,
            onGranted: (() -> Unit)?
        ) {
            if (permissions.size > 1) {
                multiplePermissionsState = rememberMultiplePermissionsState(
                    permissions.asList()
                ) { permissionStateMap ->
                    if (!permissionStateMap.containsValue(false)) {
                        onGranted?.invoke()
                    } else {
                        onNotGranted?.invoke()
                    }
                }
            } else {
                permissionState = rememberPermissionState(
                    permissions.asList()[0]
                ) { isGranted ->
                    if (isGranted) {
                        onGranted?.invoke()
                    } else {
                        onNotGranted?.invoke()
                    }
                }
            }
        }

        fun onComposeRequestPermission() {
            when {
                permissionState != null -> permissionState?.launchPermissionRequest()
                multiplePermissionsState != null -> multiplePermissionsState?.launchMultiplePermissionRequest()
            }
        }

    }
}