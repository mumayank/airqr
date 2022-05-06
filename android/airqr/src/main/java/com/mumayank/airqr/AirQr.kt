package com.mumayank.airqr

import android.content.Context
import android.graphics.Bitmap
import androidx.camera.view.PreviewView
import androidx.compose.runtime.Composable
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import com.mumayank.airqr.helpers.BarcodeScannerHelper
import com.mumayank.airqr.helpers.CameraXHelper
import com.mumayank.airqr.helpers.PermissionsComposeHelper
import com.mumayank.airqr.helpers.PermissionsHelper

@androidx.camera.core.ExperimentalGetImage
class AirQr private constructor(
    private var context: Context?,
    private var lifecycleOwner: LifecycleOwner?,
    private var previewView: PreviewView?,
    private var onIsFlashHardwareDetected: ((Boolean) -> Unit)?,
    private var onFlashStateChanged: ((Boolean) -> Unit)?,
    private var onQrCodeDetected: ((String, shouldStopScanning: (() -> Unit)?) -> Unit)?,
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
    private var isStopRequested: Boolean = false

    data class Builder(
        private var context: Context? = null,
        private var lifecycleOwner: LifecycleOwner? = null,
        private var previewView: PreviewView? = null,
        private var onIsFlashHardwareDetected: ((Boolean) -> Unit)? = null,
        private var onFlashStateChanged: ((Boolean) -> Unit)? = null,
        private var onQrCodeDetected: ((String, shouldStopScanning: (() -> Unit)?) -> Unit)? = null,
        private var onError: ((String) -> Unit)? = null,
        private var onPermissionsNotGranted: (() -> Unit)? = null,
        private var onException: ((Exception) -> Unit)? = null
    ) {
        fun withContext(context: Context) = apply { this.context = context }
        fun withLifecycleOwner(lifecycleOwner: LifecycleOwner) =
            apply { this.lifecycleOwner = lifecycleOwner }

        fun withPreviewView(previewView: PreviewView) = apply { this.previewView = previewView }
        fun onIsFlashHardwareDetected(onIsFlashHardwareDetected: ((Boolean) -> Unit)?) =
            apply { this.onIsFlashHardwareDetected = onIsFlashHardwareDetected }

        fun onFlashStateChanged(onFlashStateChanged: ((Boolean) -> Unit)?) =
            apply { this.onFlashStateChanged = onFlashStateChanged }

        fun onQrCodeDetected(
            onQrCodeDetected: ((String, shouldStopScanning: (() -> Unit)?) -> Unit)?,
        ) =
            apply { this.onQrCodeDetected = onQrCodeDetected }

        fun onError(onError: ((String) -> Unit)?) = apply { this.onError = onError }
        fun onPermissionsNotGranted(onPermissionsNotGranted: (() -> Unit)?) =
            apply { this.onPermissionsNotGranted = onPermissionsNotGranted }

        fun onException(onException: ((Exception) -> Unit)?) =
            apply { this.onException = onException }

        fun build(): AirQr = AirQr(
            context,
            lifecycleOwner,
            previewView,
            onIsFlashHardwareDetected,
            onFlashStateChanged,
            onQrCodeDetected,
            onError,
            onPermissionsNotGranted,
            onException
        )
    }

    init {
        lifecycleOwner?.lifecycle?.addObserver(object : LifecycleEventObserver {
            override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
                when (event) {
                    Lifecycle.Event.ON_RESUME -> {
                        onResume()
                    }
                    Lifecycle.Event.ON_PAUSE -> {
                        onPause()
                    }
                    else -> {
                        // do nothing
                    }
                }
            }
        })
    }

    private fun scanSetup() {
        lifecycleOwner?.let {
            cameraXHelper = CameraXHelper()
            cameraXHelper?.let { cxHelper ->
                cxHelper.onCreate(
                    onIsFlashHardwareDetected,
                    onFlashStateChanged,
                    onNextProxyImageAvailableForAnalysis = { imageProxy ->
                        if (isStopRequested.not()) {
                            BarcodeScannerHelper.analyze(
                                imageProxy,
                                onError = { error ->
                                    imageProxy.close()
                                    if (isStopRequested.not()) {
                                        onError?.invoke(error)
                                    }
                                },
                                onDetection = { string ->
                                    imageProxy.close()
                                    if (isStopRequested.not()) {
                                        onQrCodeDetected?.invoke(string) {
                                            onPause()
                                            isStopRequested = true
                                        }
                                    }
                                }
                            )
                        }
                    }
                )
            }
        }
    }

    fun startScan(): AirQr {
        scanSetup()
        context?.let {
            PermissionsHelper.onCreate(
                it,
                CameraXHelper.permissions,
                onPermissionsNotGranted
            ) {
                onCameraXHelperResume()
            }
        }
        return this
    }

    @Composable
    fun startScanCompose(): AirQr {
        scanSetup()
        PermissionsComposeHelper.OnComposeCreate(
            CameraXHelper.permissions,
            onNotGranted = {
                onPermissionsNotGranted?.invoke()
            },
            onGranted = {
                onCameraXHelperResume()
            }
        )
        PermissionsComposeHelper.onComposeRequestPermission()
        return this
    }

    fun changeFlashState(turnOn: Boolean) {
        cameraXHelper?.changeFlashState(turnOn)
    }

    fun toggleFlashState() {
        cameraXHelper?.toggleFlashState()
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

    private fun onResume() {
        isStopRequested = false
        onCameraXHelperResume()
    }

    private fun onCameraXHelperResume() {
        previewView?.let {
            cameraXHelper?.onResume(
                context,
                lifecycleOwner,
                previewView,
                onException
            )
        }
    }

    private fun onPause() {
        cameraXHelper?.onPause()
    }

}