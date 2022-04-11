package com.mumayank.airqr.helpers

import android.Manifest
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.*
import androidx.camera.core.ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
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
        appCompatActivity: AppCompatActivity?,
        previewView: PreviewView?,
        onException: ((Exception) -> Unit)?
    ) {
        appCompatActivity?.let { activity ->
            previewView?.let { view ->
                cameraExecutorService = Executors.newSingleThreadExecutor()
                val cameraProviderFuture = ProcessCameraProvider.getInstance(activity)
                cameraProviderFuture.addListener({
                    val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()
                    try {
                        cameraProvider.unbindAll()
                        val camera = cameraProvider.bindToLifecycle(
                            activity,
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
                                            onNextProxyImageAvailableForAnalysis?.invoke(imageProxy)
                                        }
                                    }
                            }
                        )
                        cameraControl = camera.cameraControl
                        val cameraInfo = camera.cameraInfo
                        if (cameraInfo.hasFlashUnit()) {
                            isFlashHardwareDetected?.invoke(true)
                            cameraInfo.torchState.observe(activity) {
                                if (it == TorchState.ON) {
                                    onFlashStateChanged?.invoke(true)
                                } else {
                                    onFlashStateChanged?.invoke(false)
                                }
                            }
                        } else {
                            isFlashHardwareDetected?.invoke(false)
                        }
                    } catch (e: Exception) {
                        onPause()
                        onException?.invoke(e)
                    }

                }, ContextCompat.getMainExecutor(activity))
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

}