// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    val kotlinVersion="2.0.20"
    val androidVersion = "8.7.0"

    id("com.android.application") version androidVersion apply false
    id("com.android.library") version androidVersion apply false
    id("org.jetbrains.kotlin.android") version kotlinVersion apply false
    id("org.jetbrains.kotlin.plugin.compose") version kotlinVersion apply false
    id("org.jetbrains.kotlin.plugin.serialization") version kotlinVersion apply false
    id("com.google.devtools.ksp") version "2.0.20-1.0.25" apply false
    id("androidx.room") version "2.6.1" apply false
}
