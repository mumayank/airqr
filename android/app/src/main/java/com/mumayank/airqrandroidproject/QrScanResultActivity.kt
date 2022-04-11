package com.mumayank.airqrandroidproject

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.mumayank.airqrandroidproject.databinding.ActivityQrScanResultBinding

class QrScanResultActivity : AppCompatActivity() {

    companion object {
        const val extra = "qr.scan.result.extra"
    }

    private lateinit var binding: ActivityQrScanResultBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityQrScanResultBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val result = intent.getStringExtra(extra)
        binding.textView.text = result
    }
}