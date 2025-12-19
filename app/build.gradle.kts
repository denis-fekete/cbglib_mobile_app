plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "cv.demoapps.bangdemo"
    compileSdk = 36

    defaultConfig {
        applicationId = "cv.demoapps.bangdemo"
        minSdk = 29
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        viewBinding = true
    }
}

dependencies {
    // default
    implementation("androidx.core:core-ktx:1.17.0")
    implementation("androidx.appcompat:appcompat:1.7.1")
    implementation("com.google.android.material:material:1.13.0")
    implementation("androidx.activity:activity:1.11.0")
    implementation("androidx.constraintlayout:constraintlayout:2.2.1")
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:2.9.4")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.9.4")
    implementation("androidx.navigation:navigation-fragment-ktx:2.9.5")
    implementation("androidx.navigation:navigation-ui-ktx:2.9.5")
    implementation("androidx.legacy:legacy-support-v4:1.0.0")
    implementation("androidx.fragment:fragment-ktx:1.8.9")
    implementation("androidx.fragment:fragment:1.8.9")
    implementation("androidx.preference:preference:1.2.1")
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.3.0")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.7.0")

    // detection
    implementation("org.opencv:opencv:4.12.0")
    implementation("com.microsoft.onnxruntime:onnxruntime-android:latest.release")

    // camera
    implementation("androidx.camera:camera-core:1.5.1")
    implementation("androidx.camera:camera-camera2:1.5.1")
    implementation("androidx.camera:camera-view:1.5.1")
    implementation("androidx.camera:camera-lifecycle:1.5.1")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.9.4")
    implementation("androidx.concurrent:concurrent-futures-ktx:1.3.0")

    // coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.10.2")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.10.2")

    // ui navigation
    implementation("androidx.navigation:navigation-fragment-ktx:2.9.6")
    implementation("androidx.navigation:navigation-ui-ktx:2.9.6")
}