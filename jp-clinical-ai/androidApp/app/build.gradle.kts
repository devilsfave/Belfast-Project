plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.legacy.kapt)
    alias(libs.plugins.hilt.android)
}

android {
    namespace = "com.example.medgem"
    compileSdk = 36
    compileSdkExtension = 19

    defaultConfig {
        applicationId = "com.example.medgem"
        minSdk = 34
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                    getDefaultProguardFile("proguard-android-optimize.txt"),
                    "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    buildFeatures { compose = true }
}

dependencies {
    implementation(files("libs/executorch.aar"))
    implementation(files("libs/sherpa_onnx.aar"))
    implementation(libs.androidx.core.splashscreen)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.fragment.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material.icons.extended)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.hilt.navigation.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.hilt.lifecycle.viewmodel.compose)
    implementation(libs.richtext.commonmark)
    implementation(libs.richtext.ui.material3)
    implementation(libs.soloader.nativeloader)
    implementation(libs.soloader)
    implementation(libs.fbjni)
    // TensorFlow Lite
    implementation(libs.litert)
    // ObjectBox Database
    implementation(libs.objectbox.kotlin)
    // Jetpack PDF Viewer
    implementation(libs.androidx.pdf.viewer)

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
    implementation(libs.androidx.datastore.preferences)

    implementation(libs.hilt.android)
    kapt(libs.hilt.compiler)

    // Kotlin serialization
    implementation(libs.kotlinx.serialization.json)

    // Coil for image loading
    implementation(libs.coil.compose)
}

// Apply ObjectBox plugin after dependencies block so it does not overwrite manually added dependencies
apply(plugin = "io.objectbox")
