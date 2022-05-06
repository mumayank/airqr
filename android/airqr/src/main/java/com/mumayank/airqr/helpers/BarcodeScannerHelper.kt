package com.mumayank.airqr.helpers

import android.graphics.Bitmap
import androidx.camera.core.ImageProxy
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.common.InputImage

class BarcodeScannerHelper {

    companion object {

        @androidx.camera.core.ExperimentalGetImage
        fun analyze(
            imageProxy: ImageProxy?,
            onError: ((String) -> Unit)?,
            onDetection: ((String) -> Unit)?
        ) {
            try {
                if (imageProxy == null) {
                    onError?.invoke("ImageProxy is null")
                    return
                }
                val mediaImage = imageProxy.image
                if (mediaImage == null) {
                    onError?.invoke("ImageProxy.image is null")
                    return
                }
                val inputImage =
                    InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)
                analyze(
                    inputImage,
                    onError = fun(string: String) {
                        onError?.invoke(string)
                    },
                    onDetection = fun(string: String) {
                        onDetection?.invoke(string)
                    }
                )
            } catch (e: Exception) {
                onError?.invoke(e.message ?: "")
            }
        }

        fun analyze(
            bitmap: Bitmap?,
            onError: ((String) -> Unit)?,
            onDetection: ((String) -> Unit)?
        ) {
            if (bitmap == null) {
                onError?.invoke("Bitmap is null")
                return
            }
            try {
                val inputImage = InputImage.fromBitmap(bitmap, 0)
                analyze(
                    inputImage,
                    onError = fun(string: String) {
                        onError?.invoke(string)
                    },
                    onDetection = fun(string: String) {
                        onDetection?.invoke(string)
                    }
                )
            } catch (e: Exception) {
                onError?.invoke(e.message ?: "")
            }
        }

        fun analyze(
            inputImage: InputImage,
            onError: ((String) -> Unit)?,
            onDetection: ((String) -> Unit)?
        ) {
            try {
                BarcodeScanning
                    .getClient(BarcodeScannerOptions.Builder().build())
                    .process(inputImage)
                    .addOnSuccessListener { barcodes ->
                        if (barcodes.size == 0) {
                            onError?.invoke("No barcode detected")
                            return@addOnSuccessListener
                        }
                        for (barcode in barcodes) {
                            barcode.rawValue?.let { string ->
                                onDetection?.invoke(string)
                            }
                        }
                    }
                    .addOnFailureListener { e ->
                        onError?.invoke(e.toString())
                    }
            } catch (e: Exception) {
                onError?.invoke(e.message ?: "")
            }
        }

    }

}