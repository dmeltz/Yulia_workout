plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "com.yulia.workout"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.yulia.workout"
        minSdk = 26
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"
    }

    // One fixed key, so a newer APK installs over the old one and keeps the streak.
    signingConfigs {
        create("shared") {
            storeFile = file("../keystore/yulia.keystore")
            storePassword = "yuliaworkout"
            keyAlias = "yulia"
            keyPassword = "yuliaworkout"
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            signingConfig = signingConfigs.getByName("shared")
        }
        debug {
            signingConfig = signingConfigs.getByName("shared")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
}
