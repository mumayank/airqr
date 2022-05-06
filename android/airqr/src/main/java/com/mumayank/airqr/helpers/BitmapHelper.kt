package com.mumayank.airqr.helpers

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.provider.MediaStore
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import java.io.IOException
import java.io.InputStream


class BitmapHelper {
    companion object {

        fun getBitmapFromGallery(
            appCompatActivity: AppCompatActivity,
            onSuccess: ((Bitmap) -> Unit)?,
            onFailure: ((String) -> Unit)?
        ) {
            try {
                val getIntent = Intent(Intent.ACTION_GET_CONTENT)
                getIntent.type = "image/*"
                val pickIntent =
                    Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                pickIntent.type = "image/*"
                val chooserIntent =
                    Intent.createChooser(getIntent, "Select Image Containing QR Code")
                chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, arrayOf(pickIntent))
                appCompatActivity.registerForActivityResult(
                    ActivityResultContracts.StartActivityForResult()
                ) {
                    try {
                        val imageUri: Uri = it.data?.data ?: Uri.parse("")
                        val inputStream =
                            appCompatActivity.contentResolver.openInputStream(imageUri)
                        val bitmap = BitmapFactory.decodeStream(inputStream)
                        onSuccess?.invoke(bitmap)
                    } catch (e: Exception) {
                        onFailure?.invoke(e.message ?: "")
                    }
                }.launch(chooserIntent)
            } catch (e: Exception) {
                onFailure?.invoke(e.message ?: "")
            }
        }

        fun getBitmapFromAsset(
            context: Context?,
            filePath: String?,
            onSuccess: ((Bitmap) -> Unit)?,
            onFailure: ((String) -> Unit)?
        ) {
            if (
                (context == null)
                || (filePath == null)
            ) {
                onFailure?.invoke("context or filePath is null")
                return
            }
            val assetManager = context.assets
            val inputStream: InputStream
            try {
                inputStream = assetManager.open(filePath)
                val bitmap = BitmapFactory.decodeStream(inputStream)
                onSuccess?.invoke(bitmap)
            } catch (e: IOException) {
                onFailure?.invoke(e.message ?: "")
            }
        }

    }
}