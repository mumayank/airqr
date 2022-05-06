package com.mumayank.airqr.helpers

import android.Manifest
import android.content.Context
import androidx.camera.core.*
import androidx.camera.core.ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

@androidx.camera.core.ExperimentalGetImage
class CameraXHelper {

    companion object {
        val permissions = listOf(
            Manifest.permission.CAMERA
        ).toTypedArray()
    }

    private var isFlashHardwareDetected: ((Boolean) -> Unit)? = null
    private var onFlashStateChanged: ((Boolean) -> Unit)? = null
    private var onNextProxyImageAvailableForAnalysis: ((ImageProxy) -> Unit)? = null

    private var cameraExecutorService: ExecutorService? = null
    private var cameraControl: CameraControl? = null
    private var cameraInfo: CameraInfo? = null

    fun onCreate(
        isFlashHardwareDetected: ((Boolean) -> Unit)?,
        onFlashStateChanged: ((Boolean) -> Unit)?,
        onNextProxyImageAvailableForAnalysis: ((ImageProxy) -> Unit)?
    ) {
        this.isFlashHardwareDetected = isFlashHardwareDetected
        this.onFlashStateChanged = onFlashStateChanged
        this.onNextProxyImageAvailableForAnalysis = onNextProxyImageAvailableForAnalysis
    }

    fun onResume(
        contextNullable: Context?,
        lifecycleOwnerNullable: LifecycleOwner?,
        previewView: PreviewView?,
        onException: ((Exception) -> Unit)?
    ) {
        contextNullable?.let { context ->
            lifecycleOwnerNullable?.let { lifecycleOwner ->
                previewView?.let { view ->
                    cameraExecutorService = Executors.newSingleThreadExecutor()
                    val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
                    cameraProviderFuture.addListener({
                        val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()
                        try {
                            cameraProvider.unbindAll()
                            val camera = cameraProvider.bindToLifecycle(
                                lifecycleOwner,
                                CameraSelector.DEFAULT_BACK_CAMERA,
                                Preview.Builder()
                                    .build()
                                    .also {
                                        it.setSurfaceProvider(view.surfaceProvider)
                                    },
                                cameraExecutorService?.let {
                                    ImageAnalysis.Builder()
                                        .setBackpressureStrategy(STRATEGY_KEEP_ONLY_LATEST)
                                        .build()
                                        .also {
                                            it.setAnalyzer(
                                                cameraExecutorService as ExecutorService
                                            ) { imageProxy ->
                                                onNextProxyImageAvailableForAnalysis?.invoke(
                                                    imageProxy
                                                )
                                            }
                                        }
                                }
                            )
                            cameraControl = camera.cameraControl
                            cameraInfo = camera.cameraInfo
                            cameraInfo?.let {
                                if (it.hasFlashUnit()) {
                                    isFlashHardwareDetected?.invoke(true)
                                    it.torchState.observe(lifecycleOwner) { torchState ->
                                        if (torchState == TorchState.ON) {
                                            onFlashStateChanged?.invoke(true)
                                        } else {
                                            onFlashStateChanged?.invoke(false)
                                        }
                                    }
                                } else {
                                    isFlashHardwareDetected?.invoke(false)
                                }
                            }
                        } catch (e: Exception) {
                            onPause()
                            onException?.invoke(e)
                        }

                    }, ContextCompat.getMainExecutor(context))
                }
            }
        }
    }

    fun onPause() {
        cameraExecutorService?.shutdown()
    }

    fun changeFlashState(
        turnOn: Boolean
    ) {
        cameraControl?.enableTorch(turnOn)
    }

    fun toggleFlashState() {
        cameraControl?.enableTorch(cameraInfo?.torchState?.value == TorchState.OFF)
    }

}