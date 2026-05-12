// Root build file — configuration applies to all subprojects.
plugins {
    // KMP
    alias(libs.plugins.kotlinMultiplatform) apply false
    // Android application
    alias(libs.plugins.androidApplication) apply false
    // Android library
    alias(libs.plugins.androidLibrary) apply false
    // Hilt
    alias(libs.plugins.hilt) apply false
    // KSP (for Hilt + ObjectBox code generation)
    alias(libs.plugins.ksp) apply false
}
