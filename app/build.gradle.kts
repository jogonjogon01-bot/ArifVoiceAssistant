plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "com.arif.voiceassistant"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.arif.voiceassistant"
        minSdk = 26
        targetSdk = 35
        versionCode = 2
        versionName = "1.1-wakeword-scaffold"
    }
}

dependencies {
    implementation("ai.picovoice:porcupine-android:4.0.2")
}
