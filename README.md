# AirQr

Wrapper that provides helper functions which help you include QR-code reading functionality in your android app using Jetpack's CameraX library and Google's Vision APIs

## Features
- Covers android `permissions` flow for camera
- Covers including a `viewfinder` for the users to see where is the camera pointing at
- Provides an easy callback method to get the `detected string` out of the QR code
- Provides helper methods to check if `flash` hardware is present in the device, and to turn on or off the flash
- Provides additional optional callback methods so that devs can take care of the flow `on error`, `exception`, `permission denied`, etc. as per their app flow.
- Covers `releasing` camera resources properly `onPause()`, reclaiming them `onResume()`, closing `proxyImage` when detection is done. Takes care of backpressure.
- Uses `Jetpack's CameraX library` and `Google's ML Kit` internally - so you're in safe hands!
- Provides `source files` instead of packaging as a library - so that devs are in full control.

## Demo
https://user-images.githubusercontent.com/8118918/162677144-e592fc47-a18c-4be8-a586-2cfd7d14d906.mp4

## Usage

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
    // jetpack cameraX
    def camerax_version = "1.1.0-beta03"
    implementation "androidx.camera:camera-core:${camerax_version}"
    implementation "androidx.camera:camera-lifecycle:${camerax_version}"
    implementation "androidx.camera:camera-view:${camerax_version}"
    implementation "androidx.camera:camera-extensions:${camerax_version}"
    // google ml kit barcode scanning
    implementation 'com.google.mlkit:barcode-scanning:17.0.2'
}
```
In `AndroidManifest.xml`
```xml
<manifest>
    <uses-feature android:name="android.hardware.camera.any" /> 
    <uses-permission android:name="android.permission.CAMERA" />

    <application>
       .
       .
    </application>

</manifest>
```
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
Now copy this folder into your project at any suitable location
https://github.com/mumayank/airqr/tree/main/android/app/src/main/java/com/mumayank/airqr/air_qr

Now you can call the helper methods in your activity:
```kotlin
@androidx.camera.core.ExperimentalGetImage
class MainActivity : AppCompatActivity() {

    private val airQr = AirQr()

    override fun onCreate(savedInstanceState: Bundle?) {
        .
        .
        with(airQr) {
                onCreate(
                    appCompatActivity = this@MainActivity,
                    previewView = previewView,
                    onDetection = { string ->
                        // library stops, do something with the string
                    },
                    isFlashHardwareDetected = { isDetected ->
                        // optional
                        // change UI based on flash is detected or not
                    },
                    onFlashStateChanged = { isFlashOn ->
                        // optional
                        // change UI based on flash state changed
                    },
                    onError = { errorString ->
                        // optional
                        // either do something
                        // or can ignore as this error is only for the current frame
                    },
                    onPermissionsNotGranted = {
                        // user denied permissions, do something
                    },
                    onException = {
                        // some exception occurred in the camera process, do something
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
        airQr.onRequestPermissionsResult(this, requestCode, permissions, grantResults)
    }

    override fun onResume() {
        super.onResume()
        airQr.onResume(this)
    }

    override fun onPause() {
        airQr.onPause()
        super.onPause()
    }

}
```
That's all :)

p.s. In case you face issues, or have suggestions, feel free to use the "issues" tab. Maybe the issue is already addressed

And of course, PRs are welcomed. Feel free to contribute and make the project better :)
