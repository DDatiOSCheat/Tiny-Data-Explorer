plugins {
    id("com.android.application") version "8.5.0" apply false
    id("org.jetbrains.kotlin.android") version "2.0.0" apply false
}

android {
    namespace = "com.example.rootforgedataexplorer"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.example.rootforgedataexplorer"
        minSdk = 26
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
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
        compose = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.6.10"
    }
    packagingOptions {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {
    implementation("androidx.core:core-ktx:1.13.1")
    implementation("androidx.appcompat:appcompat:1.7.0")
    implementation("com.google.android.material:material:1.12.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("androidx.activity:activity-compose:1.9.0")
    implementation("androidx.compose.ui:ui:1.6.10")
    implementation("androidx.compose.ui:ui-graphics:1.6.10")
    implementation("androidx.compose.ui:ui-tooling-preview:1.6.10")
    implementation("androidx.compose.material3:material3:1.2.1")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.2")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.2")
    implementation("androidx.coordinatorlayout:coordinatorlayout:1.2.0")
    // libsu for root
    implementation("com.github.topjohnwu:libsu:1.0.9")
    // For file operations
    implementation("androidx.core:core-ktx:1.13.1")
    // For SQLite
    implementation("androidx.sqlite:sqlite:2.4.0")
    implementation("androidx.sqlite:sqlite-framework:2.4.0")
    // For ZIP
    implementation("java.util.zip:java.util.zip:1.1.1")
    // For JSON parsing
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.7.0")
    // For coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.8.0")
    // For Material 3 dynamic color
    implementation("androidx.core:core-splashscreen:1.2.0")
}