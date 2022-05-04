package com.mumayank.airqr

import android.content.Context
import android.graphics.Bitmap
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.view.PreviewView
import com.mumayank.airqr.helpers.BarcodeScannerHelper
import com.mumayank.airqr.helpers.CameraXHelper
import com.mumayank.airqr.helpers.PermissionsHelper

@androidx.camera.core.ExperimentalGetImage
class AirQr {

    companion object {
        fun analyzeBitmap(
            bitmap: Bitmap?,
            onDetection: ((String) -> Unit)? = null,
            onError: ((String) -> Unit)? = null
        ) {
            try {
                BarcodeScannerHelper.analyze(
                    bitmap,
                    onError = { error ->
                        onError?.invoke(error)
                    },
                    onDetection = { string ->
                        onDetection?.invoke(string)
                    }
                )
            } catch (e: Exception) {
                onError?.invoke(e.message ?: "")
            }
        }
    }

    private var cameraXHelper: CameraXHelper? = null
    private var previewView: PreviewView? = null
    private var onException: ((Exception) -> Unit)? = null
    private var onPermissionsNotGranted: (() -> Unit)? = null

    /*
    Note:
    if a QR code is detected successfully,
    but you'd like to apply additional checks
    like if you're interested in only QR codes that have a specific format/ information, etc.
    then simply return true in onDetection() if the QR code you're interested in is found
    else return false if you'd like to continue the scanning. QR codes
     */
    fun onCreate(
        appCompatActivity: AppCompatActivity?,
        previewView: PreviewView?,
        isFlashHardwareDetected: ((Boolean) -> Unit)? = null,
        onFlashStateChanged: ((Boolean) -> Unit)? = null,
        onDetection: ((String) -> Boolean)? = null,
        onError: ((String) -> Unit)? = null,
        onPermissionsNotGranted: (() -> Unit)? = null,
        onException: ((Exception) -> Unit)?
    ) {
        appCompatActivity?.let {
            this.previewView = previewView
            this.onException = onException
            this.onPermissionsNotGranted = onPermissionsNotGranted
            cameraXHelper = CameraXHelper()
            cameraXHelper?.let { cxHelper ->
                cxHelper.onCreate(
                    isFlashHardwareDetected,
                    onFlashStateChanged,
                    onNextProxyImageAvailableForAnalysis = { imageProxy ->
                        BarcodeScannerHelper.analyze(
                            imageProxy,
                            onError = { error ->
                                onError?.invoke(error)
                            },
                            onDetection = { string ->
                                if (onDetection?.invoke(string) == true) {
                                    onPause()
                                }
                            }
                        )
                    }
                )
                PermissionsHelper.onCreate(it, CameraXHelper.permissions, onPermissionsNotGranted) {
                    onCameraXHelperResume(it)
                }
            }
        }
    }

    fun changeFlashState(turnOn: Boolean) {
        cameraXHelper?.changeFlashState(turnOn)
    }

    fun onRequestPermissionsResult(
        context: Context?,
        requestCode: Int?
    ) {
        context?.let { c ->
            requestCode?.let {
                PermissionsHelper.onRequestPermissionsResult(
                    c,
                    requestCode
                )
            }
        }
    }

    fun onResume(
        appCompatActivity: AppCompatActivity?
    ) {
        appCompatActivity?.let { onCameraXHelperResume(it) }
    }

    private fun onCameraXHelperResume(
        appCompatActivity: AppCompatActivity?
    ) {
        previewView?.let {
            cameraXHelper?.onResume(
                appCompatActivity,
                previewView,
                onException
            )
        }
    }

    fun onPause() {
        cameraXHelper?.onPause()
    }

}