package com.mumayank.airqrandroidproject

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.mumayank.airqr.helpers.CameraXHelper
import com.mumayank.airqr.helpers.PermissionsHelper
import com.mumayank.airqrandroidproject.databinding.ActivityMainBinding

@androidx.camera.core.ExperimentalGetImage
class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        with(binding) {
            bindingButton.setOnClickListener {
                startActivity(
                    Intent(
                        this@MainActivity,
                        BindingExampleActivity::class.java
                    )
                )
            }
            composeButton.setOnClickListener {
                startActivity(
                    Intent(
                        this@MainActivity,
                        ComposeExampleActivity::class.java
                    )
                )
            }
        }
        PermissionsHelper.onCreate(
            this,
            CameraXHelper.permissions,
            onNotGranted = {
                this.finish()
            },
            onGranted = {
                // do nothing
            }
        )
    }

}