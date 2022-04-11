package com.mumayank.airqr.helpers

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
            imageProxy?.let {
                val mediaImage = it.image
                if (mediaImage != null) {
                    val image =
                        InputImage.fromMediaImage(mediaImage, it.imageInfo.rotationDegrees)
                    val scanner = BarcodeScanning
                        .getClient(
                            BarcodeScannerOptions.Builder()
                                .build()
                        )
                    val result = scanner
                        .process(image)
                        .addOnSuccessListener { barcodes ->
                            for (barcode in barcodes) {
                                barcode.rawValue?.let { string ->
                                    onDetection?.invoke(string)
                                }
                            }
                        }
                        .addOnFailureListener { e ->
                            onError?.invoke(e.toString())
                        }
                    result
                        .addOnCompleteListener { _ ->
                            it.close()
                        }
                }
            }
        }

    }

}