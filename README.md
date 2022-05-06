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
- Bonus: 
	- Provides a static helper method to detect QR code from Bitmap
	- Supports Jetpack Compose

## Demo
https://user-images.githubusercontent.com/8118918/162677144-e592fc47-a18c-4be8-a586-2cfd7d14d906.mp4

## Install

In project-level `build.gradle`
```gradle
allprojects {
  repositories {
    ...
    maven { url 'https://jitpack.io' } // ADD THIS
  }
}
```

In app-level `build.gradle`
```gradle
android {
    .
    .
    // ADD THESE
    compileOptions {
        sourceCompatibility JavaVersion.VERSION_1_8
        targetCompatibility JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = '1.8'
    }
    packagingOptions {
        resources {
            excludes += '/META-INF/{AL2.0,LGPL2.1}'
        }
    }
    // OPTIONAL - IF USING JETPACK COMPOSE
    buildFeatures {
        compose true
    }
    composeOptions {
        kotlinCompilerExtensionVersion compose_version
    }
}
dependencies {
    // ADD THESE
    // airqr
    implementation "com.github.mumayank:airqr:AIR_QR_VERSION"
    // jetpack cameraX
    implementation "androidx.camera:camera-core:+"
    implementation "androidx.camera:camera-view:+"
    implementation "androidx.camera:camera-camera2:+"
    implementation "androidx.camera:camera-lifecycle:+"
    // jetpack compose
    implementation "androidx.compose.ui:ui:+"
    implementation "androidx.compose.ui:ui-tooling-preview:+"
    implementation "androidx.compose.material:material:+"
    debugImplementation "androidx.compose.ui:ui-tooling:+"
    implementation 'androidx.activity:activity-compose:+'
    implementation "com.google.android.material:compose-theme-adapter:+"
}
```

where `AIR_QR_VERSION` is [![](https://jitpack.io/v/mumayank/airqr.svg)](https://jitpack.io/#mumayank/airqr)

and latest version of other dependencies should be suggested automatically by Android Studio, or can also be found at their respective official websites.

## The Jetpack Compose Way

```kotlin
var previewView: PreviewView? = null // ADD THIS

@androidx.camera.core.ExperimentalGetImage // ADD THIS
class ComposeExampleActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        previewView = PreviewView(this) // ADD THIS
        setContent {
            Content()
            // ADD THIS
            AirQr.Builder()
                .withContext(LocalContext.current)
                .withLifecycleOwner(LocalLifecycleOwner.current)
                .withPreviewView(previewView!!)
                .onError {
                    // can ignore as this means error only in processing the current frame
                }
                .onPermissionsNotGranted {
                    // cannot proceed, show some error
                }
                .onException {
                    // cannot proceed, show some error
                }
               .onQrCodeDetected { string, shouldStopScanning ->
                    if (string.isNotEmpty()) { // your logic to confirm this is the QR code you are interested in
                        shouldStopScanning?.invoke() // call this method to inform the library to stop
                        // your regular flow continues from here
                    }
                }
                .build()
                .startScan() // you could choose to stop at .build() and call airQr?.startScan() a little later too
        }
    }
}

// ADD THIS
@Preview
@Composable
fun Content() {
    AirQrAndroidProjectTheme {
        Surface(
            modifier = Modifier
                .fillMaxSize(),
            color = MaterialTheme.colors.background
        ) {
            AndroidView(factory = { previewView!! }, modifier = Modifier.fillMaxSize()) // this creates a cameraX previewView
        }
    }
}

```

## The View Binding Way

```xml
<androidx.constraintlayout.widget.ConstraintLayout>
    .
    .
    <!-- ADD BELOW -->
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

```kotlin
@androidx.camera.core.ExperimentalGetImage // ADD THIS
class BindingExampleActivity : AppCompatActivity() {
    private lateinit var binding: ActivityBindingExampleBinding
    private var airQr: AirQr? = null // ADD THIS

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBindingExampleBinding.inflate(layoutInflater)
        setContentView(binding.root)

        with(binding) {
            // ADD THIS
            airQr = AirQr.Builder()
                .withContext(this@BindingExampleActivity)
                .withLifecycleOwner(this@BindingExampleActivity)
                .withPreviewView(binding.previewView)
                .onIsFlashHardwareDetected { isDetected ->
                    // can do something like changing visibility of flash icon
                }
                .onFlashStateChanged {
                    // can do something like changing icon of flash imageview to show current state
                }
                .onError {
                    // can ignore as this means error only in processing the current frame
                }
                .onPermissionsNotGranted {
                    // cannot proceed, show some error
                }
                .onException {
                    // cannot proceed, show some error
                }
                .onQrCodeDetected { string, shouldStopScanning ->
                    if (string.isNotEmpty()) { // your logic to confirm this is the QR code you are interested in
                        shouldStopScanning?.invoke() // call this method to inform the library to stop
                       // your regular flow continues from here
                    }
                }
                .build()
                .startScan()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        airQr?.onRequestPermissionsResult(this, requestCode) // ADD THIS
    }

}
```

## Bonus

Analyze any bitmap for QR code:

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

Get bitmap from image file in project's assets folder:

```kotlin
BitmapHelper.getBitmapFromAsset(
    appCompatActivity,
    "image.png",  // image file name with extension
    onSuccess = { bitmap ->
        // use the bitmap in the above function to analyze it
    }, 
    onFailure = { errorString ->
        // show some error to the user
    }
)
```

Choose image from user's device (using explorer app/ other photos app installed on user's device)

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

That's all! 

In case you face issues, or have suggestions, feel free to use the [issues](https://github.com/mumayank/airqr/issues) tab. (Maybe the issue is already addressed)

And of course, PRs are welcomed. Feel free to contribute and make the project better :)

Please do checkout the [wiki](https://github.com/mumayank/airqr/wiki) too!
