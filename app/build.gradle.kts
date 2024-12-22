plugins {
    alias(libs.plugins.kotlin.compose)
    id("com.android.application")
    id("kotlin-android")
    id("kotlin-kapt")
    id("com.google.gms.google-services")
}



android {
    namespace = "com.example.food_rescue"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.food_rescue"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
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
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    kotlinOptions {
        jvmTarget = "11"
    }

    buildFeatures {
        compose = true
    }
}
dependencies {
    // AndroidX dependencies
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)

    // Compose dependencies
    implementation(platform(libs.androidx.compose.bom))  // BOM for Compose version control
    implementation(libs.androidx.ui)                    // For Compose UI
    implementation(libs.androidx.ui.graphics)           // For graphics
    implementation(libs.androidx.ui.tooling.preview)    // For previewing Compose UIs
    implementation(libs.androidx.material3)             // For Material 3 UI components
    implementation("androidx.compose.ui:ui:1.7.5")      // Latest Compose version
    implementation("androidx.compose.foundation:foundation:1.7.5")  // For Foundation (TextField etc.)
    implementation("androidx.compose.material:material:1.7.5")      // For Material components (Button, TextField, etc.)
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.6.0")
    implementation("androidx.activity:activity-compose:1.6.1")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-play-services:1.6.4")


    // Firebase dependencies (using BOM for versions control)
    implementation(platform("com.google.firebase:firebase-bom:33.5.1"))  // BOM for Firebase
    implementation(libs.firebase.auth)
    implementation(libs.firebase.firestore)
    implementation(libs.firebase.storage)

    // Testing dependencies
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)

    // Debugging tools for Compose
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
    implementation("androidx.compose.material3:material3:1.0.1")  // Ensure you have the latest version
    implementation("androidx.compose.material:material:1.7.5")  // If not already included
    implementation("androidx.compose.material:material-icons-extended:1.7.5")
    implementation("io.coil-kt:coil-compose:2.2.2")

    implementation("com.google.firebase:firebase-auth:23.1.0")
    implementation("com.google.firebase:firebase-firestore:24.5.0")
    implementation("com.google.firebase:firebase-storage:20.1.0")
    implementation("com.google.firebase:firebase-core:21.1.0")
    implementation("com.google.android.gms:play-services-location:18.0.0")
    implementation("com.google.firebase:firebase-auth-ktx")
    implementation("com.google.firebase:firebase-firestore-ktx")
    implementation("com.google.firebase:firebase-storage-ktx")
    implementation("androidx.navigation:navigation-compose:2.5.0") // or the latest version

    implementation("androidx.compose.material3:material3:1.3.1")
    implementation("androidx.compose.ui:ui:1.7.5")
    implementation("com.google.maps.android:maps-compose:2.13.0")
    implementation("androidx.compose.ui:ui-tooling:1.7.5")
    implementation("com.google.android.gms:play-services-maps:19.0.0")
    implementation("com.google.firebase:firebase-firestore-ktx:24.5.0") // Make sure to use the latest version
    implementation("com.google.firebase:firebase-auth-ktx:21.0.1")
    implementation("com.google.firebase:firebase-firestore-ktx:24.0.1")
    implementation("com.google.firebase:firebase-storage-ktx:20.0.1")
    implementation("androidx.compose.runtime:runtime-livedata:1.7.5")// Add this for Livedata and observeAsState
    implementation("androidx.compose.runtime:runtime:1.4.0") // This is for the basic Compose runtime
}


