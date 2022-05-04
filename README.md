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
- Provides `source files` instead of packaging as a library - so that devs are in full control.

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
        airQr = AirQr()
        airQr?.onCreate(
                appCompatActivity = this@MainActivity,
                previewView = previewView,
                isFlashHardwareDetected = { isDetected ->
                    // do something
                },
                onFlashStateChanged = {
                    // do something
                },
                onDetection = { string ->
                    // do something, return true if this is the QR code you wanted, else return false to continue scanning
                },
                onError = { errorString ->
                    // can ignore as this does not stop the lib from analyzing the next frame
                },
                onPermissionsNotGranted = {
                    // do something
                },
                onException = {
                    // cannot proceed due to camera/ google ML issue. Do something
                }
            )
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
```
That's all :)

p.s. In case you face issues, or have suggestions, feel free to use the "issues" tab. Maybe the issue is already addressed

And of course, PRs are welcomed. Feel free to contribute and make the project better :)
