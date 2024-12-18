plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    id("kotlin-kapt") // Asegúrate de incluir esto para Room
}

android {
    namespace = "com.example.cartquina"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.cartquina"
        minSdk = 30
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
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
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
    buildFeatures {
        compose = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.1"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {
    // Core
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)

    // Room
    implementation(libs.androidx.room.runtime.v251)
    implementation(libs.androidx.compose.material3) // Reemplaza con la última versión estable
    kapt("androidx.room:room-compiler:2.6.1") // Procesador de anotaciones para Room
    implementation(libs.androidx.room.ktx.v251) // Extensiones de Kotlin para Room
    implementation( libs.androidx.room.runtime)


    // Firebase Crashlytics
    implementation(libs.firebase.crashlytics.buildtools)

    // Testing
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)

    // Debugging
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)

    // Navigation
    implementation(libs.androidx.navigation.compose)

    // Gson
    implementation(libs.gson)

    // Hilt
    implementation(libs.hilt.android)
    //kapt(libs.hilt.compiler) // Procesador de anotaciones para Hilt
    implementation(libs.androidx.hilt.navigation.compose)

    //implementation ("com.github.GrenderG:Toasty:1.5.2")


    // Lifecycle y ViewModel
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.lifecycle.viewmodel.savedstate)

    // Serialización
    implementation(kotlin("stdlib"))
    implementation(libs.kotlinx.serialization.json)
}
