# AirQr

[![](https://jitpack.io/v/mumayank/airqr.svg)](https://jitpack.io/#mumayank/airqr)

Wrapper that provides helper functions which help you include QR-code reading functionality in your android app using Jetpack's CameraX library and Google's Vision APIs

## Features
- Covers android `permissions` flow for camera
- Covers including a `viewfinder` for the users to see where is the camera pointing at
- Provides an easy callback method to get the `detected string` out of the QR code
- Provides an option to do additional checks on the detected QR code (to ensure it is the right QR code) and let's the dev control if detection should continue or stop
- Provides helper methods to check if `flash` hardware is present in the device, and to turn on or off the flash
- Provides additional optional callback methods so that devs can take care of the flow `on error`, `exception`, `permission denied`, etc. as per their app flow.
- Covers `releasing` camera resources properly `onPause()`, reclaiming them `onResume()`, closing `proxyImage` when detection is done. Takes care of backpressure.
- Uses `Jetpack's CameraX library` and `Google's ML Kit` internally - so you're in safe hands!
- Bonus: Provides a static helper method to detect QR code from Bitmap

## Demo
https://user-images.githubusercontent.com/8118918/162677144-e592fc47-a18c-4be8-a586-2cfd7d14d906.mp4

## Install

In project-level `build.gradle`
```gradle
allprojects {
  repositories {
    ...
    maven { url 'https://jitpack.io' }
  }
}
```

In app-level `build.gradle`
```gradle
android {
    .
    .
    compileOptions {
        sourceCompatibility JavaVersion.VERSION_1_8
        targetCompatibility JavaVersion.VERSION_1_8
    }
}
dependencies {
    def camerax_version = "1.1.0-beta03"
    implementation "androidx.camera:camera-core:${camerax_version}"
    implementation "androidx.camera:camera-view:${camerax_version}"
    implementation "com.github.mumayank:airqr:XXXXX"
}
```
where `XXXXX` is [![](https://jitpack.io/v/mumayank/airqr.svg)](https://jitpack.io/#mumayank/airqr)

To add a previewView in your layout

#### Option 1: Jetpack Compose

```kotlin
PreviewView(context).apply {
    setBackgroundColor(Color.GREEN)
    layoutParams = LinearLayout.LayoutParams(MATCH_PARENT, MATCH_PARENT)
    scaleType = PreviewView.ScaleType.FILL_START
    implementationMode = PreviewView.ImplementationMode.COMPATIBLE
    post {
        cameraProviderFuture.addListener(Runnable {
            val cameraProvider = cameraProviderFuture.get()
            bindPreview(
                cameraProvider,
                lifecycleOwner,
                this,
            )
        }, ContextCompat.getMainExecutor(context))
    }
}
```

#### Option 2: Add view in xml

In your layout file `xml`

```xml
<androidx.constraintlayout.widget.ConstraintLayout>
    .
    .
    <androidx.camera.view.PreviewView
        android:id="@+id/previewView"
        android:layout_width="match_parent"
        android:layout_height="match_parent"
        app:layout_constraintBottom_toBottomOf="parent"
        app:layout_constraintEnd_toEndOf="parent"
        app:layout_constraintStart_toStartOf="parent"
        app:layout_constraintTop_toTopOf="parent" />
    .
    .
</androidx.constraintlayout.widget.ConstraintLayout>
```

## Use

Call the helper methods in your activity:
```kotlin
@androidx.camera.core.ExperimentalGetImage
class MainActivity : AppCompatActivity() {

    private var airQr: AirQr? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        .
        .
        airQr = AirQr.Builder()
                .withAppCompatActivity(this)
                .withPreviewView(binding.previewView)
                .onQrCodeDetected { string ->
                    // qr code is successfully detected containing string
                    // analyse the string and return true if this is the correct qr
                    // else return false to continue scanning
                }
                .onIsFlashHardwareDetected { isDetected ->
                    // optional - informs if flash hardware is present in the device - can show/hide flash icons on the screen
                }
                .onFlashStateChanged {
                    // optional - flash state has changed from on to off, can do something like changing the icon
                }
                .onError {
                    // optional
                    // can ignore as this does not stop the lib from analyzing the next frame
                }
                .onPermissionsNotGranted {
                    // cannot proceed further due to permissions not provided. show some error to the user
                }
                .onException {
                    // some exception happened at google vision API level, cannot proceed further. show some error to the user
                }
                .build()
                .startScan() // you can also stop at build() to get airQr instance, and later use startScan() to begin scanning - depending on your app flow
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
```

## Bonus

The library offers a static helper function to analyze any bitmap for QR code:

```kotlin
AirQr.analyzeBitmap(
    bitmap,
    onDetection = { string ->
        // qr code is successfully detected containing string
    },
    onError = { errorString ->
        // show some error to the user
    }
)
```

Additionally, the library also offers another static helper function to get bitmap from image file in project's assets folder:

```kotlin
BitmapHelper.getBitmapFromAsset(
    appCompatActivity,
    "image.png",  // image file name with extension
    onSuccess = { bitmap ->
        // use the bitmap in the above function to analyze it
    }, onFailure = { errorString ->
        // show some error to the user
    }
)
```

The library also provides a static helper function to help choose images from user's device (using explorer app/ other photos app installed on user's device)

```kotlin
BitmapHelper.getBitmapFromGallery(
    this,
    onSuccess = { bitmap ->
        // use the bitmap in the above airqr function to analyze it
    },
    onFailure = { errorString ->
        // show some error to the user
    }
)
```

Please do checkout the [wiki](https://github.com/mumayank/airqr/wiki) too!

That's all! In case you face issues, or have suggestions, feel free to use the [issues](https://github.com/mumayank/airqr/issues) tab. Maybe the issue is already addressed. And of course, PRs are welcomed. Feel free to contribute and make the project better :)
