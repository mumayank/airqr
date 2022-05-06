package com.mumayank.airqrandroidproject

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.viewinterop.AndroidView
import com.mumayank.airqr.AirQr
import com.mumayank.airqrandroidproject.ui.theme.AirQrAndroidProjectTheme

var previewView: PreviewView? = null

@androidx.camera.core.ExperimentalGetImage
class ComposeExampleActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        previewView = PreviewView(this)
        setContent {
            Content()
            AirQr.Builder()
                .withContext(LocalContext.current)
                .withLifecycleOwner(LocalLifecycleOwner.current)
                .withPreviewView(previewView!!)
                .onQrCodeDetected { string, shouldStopScanning ->
                    if (string.isNotEmpty()) {
                        shouldStopScanning?.invoke()
                        startActivity(
                            Intent(
                                this,
                                QrScanResultActivity::class.java
                            ).putExtra(
                                QrScanResultActivity.extra,
                                string
                            )
                        )
                    }
                }
                .onError {
                    Log.e("AirQr", it)
                }
                .onPermissionsNotGranted {
                    Log.e("AirQr", "")
                }
                .onException {
                    Log.e("AirQr", it.message ?: "")
                }
                .build()
                .startScan()
        }
    }
}

@Preview
@Composable
fun Content() {
    AirQrAndroidProjectTheme {
        Surface(
            modifier = Modifier
                .fillMaxSize(),
            color = MaterialTheme.colors.background
        ) {
            AndroidView(factory = { previewView!! }, modifier = Modifier.fillMaxSize())
        }
    }
}