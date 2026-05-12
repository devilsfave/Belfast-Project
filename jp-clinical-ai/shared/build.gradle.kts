plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.kotlinSerialization)
    alias(libs.plugins.androidLibrary)
}

kotlin {
    // Android target
    androidTarget {
        compilations.all {
            kotlinOptions { jvmTarget = "17" }
        }
    }

    // iOS targets — added now so migration is non-destructive
    iosX64()
    iosArm64()
    iosSimulatorArm64()

    sourceSets {
        // ── Shared business logic (NO Android imports allowed here) ──────────
        val commonMain by getting {
            dependencies {
                implementation(libs.kotlinx.coroutines.core)
                implementation(libs.kotlinx.serialization.json)
            }
        }
        val commonTest by getting {
            dependencies {
                implementation(libs.kotlin.test)
                implementation(libs.coroutines.test)
            }
        }

        // Android-specific shared-module code (only if truly needed)
        val androidMain by getting {
            dependencies {
                implementation(libs.kotlinx.coroutines.android)
            }
        }
    }
}

android {
    namespace = "com.belfasttrust.jpclinical.shared"
    compileSdk = 35
    defaultConfig { minSdk = 34 }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}
