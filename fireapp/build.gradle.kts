// Top-level build file where you can add configuration options common to all sub-projects/modules.
// ✅ Root build.gradle.kts (modern Gradle setup)

plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    id("com.google.gms.google-services") version "4.4.2" apply false // ✅ add this line
}
