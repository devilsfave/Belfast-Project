// Root build file — configuration applies to all subprojects.
plugins {
    // KMP
    alias(libs.plugins.kotlinMultiplatform) apply false
    // Android application
    alias(libs.plugins.androidApplication) apply false
    // Android Kotlin
    alias(libs.plugins.kotlinAndroid) apply false
    // Android library
    alias(libs.plugins.androidLibrary) apply false
    // Android Kotlin Multiplatform Library (AGP 9)
    alias(libs.plugins.androidKotlinMultiplatformLibrary) apply false
    // Hilt
    alias(libs.plugins.hilt) apply false
    // KSP
    alias(libs.plugins.ksp) apply false
    // Kotlin Compose compiler plugin
    alias(libs.plugins.kotlin.compose) apply false
    // Compose Multiplatform plugin
    alias(libs.plugins.composeMultiplatform) apply false
    // SQLDelight plugin
    alias(libs.plugins.sqldelight) apply false
}
