package com.mumayank.airqrandroidproject

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.mumayank.airqr.AirQr
import com.mumayank.airqr.helpers.BitmapHelper
import com.mumayank.airqrandroidproject.databinding.ActivityMainBinding

@androidx.camera.core.ExperimentalGetImage
class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private var airQr: AirQr? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        BitmapHelper.getBitmapFromGallery(
            this,
            onSuccess = {
                AirQr.analyzeBitmap(
                    it,
                    onDetection = { string ->
                        Toast.makeText(this, string, Toast.LENGTH_LONG).show()
                    },
                    onError = { error ->
                        Toast.makeText(this, error, Toast.LENGTH_LONG).show()
                    }
                )
            },
            onFailure = {
                Toast.makeText(this, it, Toast.LENGTH_LONG).show()
            }
        )

        BitmapHelper.getBitmapFromAsset(
            this,
            "kasper.png",
            onSuccess = {
                AirQr.analyzeBitmap(
                    it,
                    onDetection = { string ->
                        Toast.makeText(this, string, Toast.LENGTH_LONG).show()
                    },
                    onError = { error ->
                        Toast.makeText(this, error, Toast.LENGTH_LONG).show()
                    }
                )
            }, onFailure = {
                Toast.makeText(this, "Failed", Toast.LENGTH_LONG).show()
            }
        )

        with(binding) {
            airQr = AirQr()
            airQr?.let {
                it.onCreate(
                    appCompatActivity = this@MainActivity,
                    previewView = previewView,
                    isFlashHardwareDetected = { isDetected ->
                        if (isDetected) {
                            View.VISIBLE.let { visible ->
                                flashOn.visibility = visible
                                flashOff.visibility = visible
                            }
                            flashOn.setOnClickListener { _ ->
                                it.changeFlashState(false)

                            }
                            flashOff.setOnClickListener { _ ->
                                it.changeFlashState(true)
                            }
                        } else {
                            View.GONE.let { gone ->
                                flashOn.visibility = gone
                                flashOff.visibility = gone
                            }
                        }
                    },
                    onFlashStateChanged = {
                        with(binding) {
                            if (it) {
                                flashOn.visibility = View.VISIBLE
                                flashOff.visibility = View.GONE
                            } else {
                                flashOn.visibility = View.GONE
                                flashOff.visibility = View.VISIBLE
                            }
                        }
                    },
                    onDetection = { string ->
                        if (string == "https://www.kaspersky.com") {
                            startActivity(
                                Intent(
                                    this@MainActivity,
                                    QrScanResultActivity::class.java
                                ).putExtra(
                                    QrScanResultActivity.extra,
                                    string
                                )
                            )
                            true
                        } else {
                            false
                        }
                    },
                    onError = {
                        // todo ignore
                        // as this does not stop the lib from analyzing the next frame
                    },
                    onPermissionsNotGranted = {
                        Toast.makeText(
                            this@MainActivity,
                            "Permissions Denied",
                            Toast.LENGTH_SHORT
                        ).show()
                        finish()
                    },
                    onException = {
                        Toast.makeText(
                            this@MainActivity,
                            "Some exception occurred",
                            Toast.LENGTH_SHORT
                        ).show()
                        finish()
                    }
                )
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        airQr?.onRequestPermissionsResult(this, requestCode)
    }

    override fun onResume() {
        super.onResume()
        airQr?.onResume(this)
    }

    override fun onPause() {
        airQr?.onPause()
        super.onPause()
    }

}