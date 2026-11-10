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
        // CI passes -PbuildNumber=<run number>; releases are tagged build-<same number>.
        val buildNumber = (project.findProperty("buildNumber") as String?)?.toIntOrNull() ?: 1
        versionCode = buildNumber
        versionName = "1.$buildNumber"
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
    buildFeatures {
        buildConfig = true
    }
}

dependencies {
    implementation("androidx.core:core:1.13.1")
}
