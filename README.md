# AirQr

[![](https://jitpack.io/v/mumayank/airqr.svg)](https://jitpack.io/#mumayank/airqr)

Helps in detecting QR codes using Jetpack's CameraX and Google Vision.

## Features
- Covers `permissions`, `viewfinder`
- Provides `detected string` from QR code
- Option to signal the library to stop detection
- Informs if `flash` hardware present, Option to toggle it on or off
- Provides `on error`, `exception`, `permission denied` callbacks
- Releases resources on pause, reclaims on resume, closes proxyImage when done, handles backpressure
- Uses `Jetpack's CameraX library` and `Google's ML Kit` internally - so you're in safe hands!
- Bonus: 
	- Option to detect QR code from Bitmap
	- Option to get bitmap from project assets or device gallery on the fly
	- Supports Jetpack Compose

## Demo
https://user-images.githubusercontent.com/8118918/162677144-e592fc47-a18c-4be8-a586-2cfd7d14d906.mp4

## Install

In project-level `build.gradle`
```gradle
allprojects {
  repositories {
    maven { url 'https://jitpack.io' }
  }
}
```

In app-level `build.gradle`
```gradle
android {
    compileOptions {
        sourceCompatibility JavaVersion.VERSION_1_8
        targetCompatibility JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = '1.8'
    }
    // if using compose
    buildFeatures {
        compose true
    }
    composeOptions {
        kotlinCompilerExtensionVersion compose_version
    }
}
dependencies {
    // airqr
    implementation "com.github.mumayank:airqr:AIR_QR_VERSION"
    // jetpack cameraX
    implementation "androidx.camera:camera-core:+"
    implementation "androidx.camera:camera-view:+"
    // jetpack compose
    implementation "androidx.compose.ui:ui:+"
    implementation "androidx.compose.ui:ui-tooling-preview:+"
    implementation "androidx.compose.material:material:+"
    implementation 'androidx.activity:activity-compose:+'
}
```

where `AIR_QR_VERSION` is [![](https://jitpack.io/v/mumayank/airqr.svg)](https://jitpack.io/#mumayank/airqr)

## The Jetpack Compose Way

in `onCreate` define `previewView`:
```kotlin
val previewView = PreviewView(this)
```

in `onCreate`'s `setContent`:

```kotlin
AndroidView(
	factory = { previewView }, 
	modifier = Modifier.fillMaxSize()
)
AirQr.Builder()
	.withContext(LocalContext.current)
	.withLifecycleOwner(LocalLifecycleOwner.current)
	.withPreviewView(previewView)
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
	    if (string.isNotEmpty()) { 		// your logic to confirm this is the QR code you are interested in
		shouldStopScanning?.invoke() 	// call this method to inform the library to stop
						// your regular flow continues from here
	    }
	}
	.build()
	.startScan() // you could choose to stop at .build() and call airQr?.startScan() later too
```

## The View Binding Way

Add `PreviewView` in your layout `xml`:
```xml
<androidx.camera.view.PreviewView
	android:id="@+id/previewView"
	android:layout_width="match_parent"
	android:layout_height="match_parent"
	app:layout_constraintBottom_toBottomOf="parent"
	app:layout_constraintEnd_toEndOf="parent"
	app:layout_constraintStart_toStartOf="parent"
	app:layout_constraintTop_toTopOf="parent" />
```

In your activity:

Declare `airQr` at activity-level:
```kotlin
private var airQr: AirQr? = null
```

Define `airQr` inside `onCreate`:
```kotlin
airQr = AirQr.Builder()
	.withContext(this@BindingExampleActivity)
	.withLifecycleOwner(this@BindingExampleActivity)
	.withPreviewView(binding.previewView)
	// (rest of the builder functions same as above jetpack-way)
```

In activity, override:
```kotlin
override fun onRequestPermissionsResult(
	requestCode: Int,
	permissions: Array<String>,
	grantResults: IntArray
) {
	super.onRequestPermissionsResult(requestCode, permissions, grantResults)
	airQr?.onRequestPermissionsResult(this, requestCode)
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

Checkout: [Issues](https://github.com/mumayank/airqr/issues) and [Wiki](https://github.com/mumayank/airqr/wiki) too!

PRs are welcomed!
