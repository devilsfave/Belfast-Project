// androidApp/build.gradle.kts
// This module is the Android application layer.
// It contains: UI (Jetpack Compose), inference wrappers, DI (Hilt), ObjectBox init.
// Business logic lives in :shared (commonMain) — no cross-contamination.

plugins {
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.kotlinSerialization)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.ksp)
    alias(libs.plugins.hilt)
    alias(libs.plugins.objectbox)
}

android {
    namespace = "com.belfasttrust.jpclinical.android"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.belfasttrust.jpclinical.android"
        minSdk = 34
        targetSdk = 35
        versionCode = 1
        versionName = "0.1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        // Build config flags
        buildConfigField("boolean", "ENABLE_VISION_ENCODER", "false")
        buildConfigField("boolean", "SHOW_DEV_TOOLS", "false")
    }

    buildTypes {
        debug {
            buildConfigField("boolean", "SHOW_DEV_TOOLS", "true")
        }
        release {
            isMinifyEnabled = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
            buildConfigField("boolean", "SHOW_DEV_TOOLS", "false")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }
}

dependencies {
    // ── Shared business logic module ─────────────────────────────────────────
    implementation(project(":shared"))

    // ── MedGEM inference AARs (DO NOT REMOVE OR MODIFY) ─────────────────────
    implementation(files("libs/executorch.aar"))
    implementation(files("libs/sherpa_onnx.aar"))

    // ── Android core ─────────────────────────────────────────────────────────
    implementation(libs.kotlin.stdlib)
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.kotlinx.serialization.json)

    // ── Compose ──────────────────────────────────────────────────────────────
    implementation(platform(libs.compose.bom))
    implementation(libs.compose.ui)
    implementation(libs.compose.material3)
    implementation(libs.compose.preview)
    implementation(libs.compose.activity)
    implementation(libs.compose.viewmodel)
    implementation(libs.compose.navigation)

    // ── AndroidX AppCompat (MainActivity extends AppCompatActivity) ──────────
    implementation("androidx.appcompat:appcompat:1.7.0")

    // ── DataStore Preferences ─────────────────────────────────────────────────
    implementation(libs.androidx.datastore.preferences)

    // ── Hilt DI ──────────────────────────────────────────────────────────────
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)

    // ── ObjectBox ────────────────────────────────────────────────────────────
    implementation(libs.objectbox.android)
    implementation(libs.objectbox.kotlin)

    // ── HAPI FHIR R4 ─────────────────────────────────────────────────────────
    implementation(libs.hapi.fhir.base)
    implementation(libs.hapi.fhir.structures.r4)

    // ── Testing ──────────────────────────────────────────────────────────────
    testImplementation(libs.junit)
    testImplementation(libs.mockk)
}

kotlin {
    compilerOptions {
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
    }
}
