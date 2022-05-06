package com.mumayank.airqr

import android.content.Context
import android.graphics.Bitmap
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.view.PreviewView
import com.mumayank.airqr.helpers.BarcodeScannerHelper
import com.mumayank.airqr.helpers.CameraXHelper
import com.mumayank.airqr.helpers.PermissionsHelper

@androidx.camera.core.ExperimentalGetImage
class AirQr private constructor(
    private var appCompatActivity: AppCompatActivity?,
    private var previewView: PreviewView?,
    private var onIsFlashHardwareDetected: ((Boolean) -> Unit)?,
    private var onFlashStateChanged: ((Boolean) -> Unit)?,
    private var onQrCodeDetected: ((String) -> Boolean)?,
    private var onError: ((String) -> Unit)?,
    private var onPermissionsNotGranted: (() -> Unit)?,
    private var onException: ((Exception) -> Unit)?
) {

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

    data class Builder(
        private var appCompatActivity: AppCompatActivity? = null,
        private var previewView: PreviewView? = null,
        private var onIsFlashHardwareDetected: ((Boolean) -> Unit)? = null,
        private var onFlashStateChanged: ((Boolean) -> Unit)? = null,
        private var onQrCodeDetected: ((String) -> Boolean)? = null,
        private var onError: ((String) -> Unit)? = null,
        private var onPermissionsNotGranted: (() -> Unit)? = null,
        private var onException: ((Exception) -> Unit)? = null
    ) {
        fun withAppCompatActivity(appCompatActivity: AppCompatActivity) =
            apply { this.appCompatActivity = appCompatActivity }

        fun withPreviewView(previewView: PreviewView) = apply { this.previewView = previewView }
        fun onIsFlashHardwareDetected(onIsFlashHardwareDetected: ((Boolean) -> Unit)?) =
            apply { this.onIsFlashHardwareDetected = onIsFlashHardwareDetected }

        fun onFlashStateChanged(onFlashStateChanged: ((Boolean) -> Unit)?) =
            apply { this.onFlashStateChanged = onFlashStateChanged }

        fun onQrCodeDetected(onQrCodeDetected: ((String) -> Boolean)?) =
            apply { this.onQrCodeDetected = onQrCodeDetected }

        fun onError(onError: ((String) -> Unit)?) = apply { this.onError = onError }
        fun onPermissionsNotGranted(onPermissionsNotGranted: (() -> Unit)?) =
            apply { this.onPermissionsNotGranted = onPermissionsNotGranted }

        fun onException(onException: ((Exception) -> Unit)?) =
            apply { this.onException = onException }

        fun build(): AirQr = AirQr(
            appCompatActivity,
            previewView,
            onIsFlashHardwareDetected,
            onFlashStateChanged,
            onQrCodeDetected,
            onError,
            onPermissionsNotGranted,
            onException
        )
    }

    fun startScan(): AirQr {
        appCompatActivity?.let {
            cameraXHelper = CameraXHelper()
            cameraXHelper?.let { cxHelper ->
                cxHelper.onCreate(
                    onIsFlashHardwareDetected,
                    onFlashStateChanged,
                    onNextProxyImageAvailableForAnalysis = { imageProxy ->
                        BarcodeScannerHelper.analyze(
                            imageProxy,
                            onError = { error ->
                                imageProxy.close()
                                onError?.invoke(error)
                            },
                            onDetection = { string ->
                                imageProxy.close()
                                if (onQrCodeDetected?.invoke(string) == true) {
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
        return this
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