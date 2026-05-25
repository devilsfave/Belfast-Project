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
    alias(libs.plugins.legacy.kapt)
}

android {
    namespace = "com.belfasttrust.jpclinical.android"
    compileSdk = 36
    compileSdkExtension = 19

    defaultConfig {
        applicationId = "com.belfasttrust.jpclinical.android"
        minSdk = 34
        targetSdk = 36
        versionCode = 1
        versionName = "0.1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        // Build config flags
        buildConfigField("boolean", "ENABLE_VISION_ENCODER", "false")
        buildConfigField("boolean", "SHOW_BELFAST_UI", "false")
        buildConfigField("boolean", "SHOW_DEV_TOOLS", "false")
    }

    buildTypes {
        debug {
            buildConfigField("boolean", "SHOW_BELFAST_UI", "true")
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

    packaging {
        resources {
            excludes.add("/META-INF/AL2.0")
            excludes.add("/META-INF/LGPL2.1")
            excludes.add("/META-INF/LICENSE.md")
            excludes.add("/META-INF/NOTICE.md")
            excludes.add("/META-INF/LICENSE.txt")
            excludes.add("/META-INF/NOTICE.txt")
            excludes.add("/META-INF/LICENSE")
            excludes.add("/META-INF/NOTICE")
        }
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
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.fragment.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)

    // ── Compose ──────────────────────────────────────────────────────────────
    implementation(platform(libs.compose.bom))
    implementation(libs.compose.ui)
    implementation(libs.compose.material3)
    implementation(libs.compose.preview)
    implementation(libs.compose.activity)
    implementation(libs.compose.viewmodel)
    implementation(libs.compose.navigation)
    implementation(libs.androidx.compose.material.icons.extended)

    // ── AndroidX AppCompat & Material UI ──────────────────────────────────────
    implementation(libs.androidx.appcompat)
    implementation(libs.material)

    // ── Splash Screen ────────────────────────────────────────────────────────
    implementation(libs.androidx.core.splashscreen)

    // ── DataStore Preferences ─────────────────────────────────────────────────
    implementation(libs.androidx.datastore.preferences)

    // ── Hilt DI ──────────────────────────────────────────────────────────────
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)
    implementation(libs.androidx.hilt.navigation.compose)
    implementation(libs.androidx.hilt.lifecycle.viewmodel.compose)

    // ── Markdown / RichText ──────────────────────────────────────────────────
    implementation(libs.richtext.commonmark)
    implementation(libs.richtext.ui.material3)

    // ── SoLoader & fbjni (Inference dependencies) ────────────────────────────
    implementation(libs.soloader.nativeloader)
    implementation(libs.soloader)
    implementation(libs.fbjni)

    // ── TensorFlow Lite (LiteRT) ─────────────────────────────────────────────
    implementation(libs.litert)

    // ── Jetpack PDF Viewer ───────────────────────────────────────────────────
    implementation(libs.androidx.pdf.viewer)

    // ── Coil (Image Loading) ─────────────────────────────────────────────────
    implementation(libs.coil.compose)

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
