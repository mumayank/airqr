package com.mumayank.airqr.helpers

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import java.io.IOException
import java.io.InputStream


class BitmapHelper {
    companion object {

        fun getBitmapFromAsset(
            context: Context?,
            filePath: String?,
            onSuccess: ((Bitmap) -> Unit)?,
            onFailure: (() -> Unit)?
        ) {
            if (
                (context == null)
                || (filePath == null)
            ) {
                onFailure?.invoke()
                return
            }
            val assetManager = context.assets
            val inputStream: InputStream
            var bitmap: Bitmap? = null
            try {
                inputStream = assetManager.open(filePath)
                bitmap = BitmapFactory.decodeStream(inputStream)
                onSuccess?.invoke(bitmap)
            } catch (e: IOException) {
                onFailure?.invoke()
            }
        }

    }
}