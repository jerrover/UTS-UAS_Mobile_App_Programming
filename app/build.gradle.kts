plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.google.services)

    id("kotlin-parcelize")
    id("kotlin-kapt")
    id("androidx.navigation.safeargs.kotlin")
}

android {
    namespace = "com.example.dermamindapp"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.example.uts_uasmobileappprogramming"
        minSdk = 24
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

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }

    buildFeatures {
        viewBinding = true
    }
}

dependencies {
    // Dependensi Asli Anda
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.navigation.fragment.ktx)
    implementation(libs.androidx.navigation.ui.ktx)

    // ▼▼▼ PERBAIKAN 1: PickVisualMedia ▼▼▼
    // HAPUS ATAU KOMENTARI BARIS INI:
    // implementation(libs.androidx.activity)
    // GANTI DENGAN INI (Versi 1.7.0+ diperlukan):
    implementation("androidx.activity:activity-ktx:1.9.0")

    // TFLite (Sudah Benar)
    implementation("org.tensorflow:tensorflow-lite:2.15.0")
    implementation("org.tensorflow:tensorflow-lite-select-tf-ops:2.15.0")
    implementation("org.tensorflow:tensorflow-lite-support:0.4.4")
    implementation("org.tensorflow:tensorflow-lite-gpu:2.15.0")

    // CameraX (Sudah Benar)
    val camerax_version = "1.3.1"
    implementation("androidx.camera:camera-core:${camerax_version}")
    implementation("androidx.camera:camera-camera2:${camerax_version}")
    implementation("androidx.camera:camera-lifecycle:${camerax_version}")
    implementation("androidx.camera:camera-view:${camerax_version}")

    // Testing (Sudah Benar)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    // Navigation Component (Sudah Benar)
    implementation("androidx.navigation:navigation-fragment-ktx:2.8.0")
    implementation("androidx.navigation:navigation-ui-ktx:2.8.0")

    // Cloudinary Android SDK
    implementation("com.cloudinary:cloudinary-android:2.5.0")

    // Glide
    implementation("com.github.bumptech.glide:glide:4.16.0") {
        exclude(group = "com.google.inject", module = "guice")
    }

    // ▼▼▼ PERBAIKAN 2: Error Dexing 'guice' ▼▼▼
    kapt("com.github.bumptech.glide:compiler:4.16.0") {
        // Kecualikan 'guice' agar tidak menyebabkan error dexing di API < 26
        exclude(group = "com.google.inject", module = "guice")
    }
    // Tambahkan juga ini untuk memperbaiki error 'mergeExtDexDebugAndroidTest'
    kaptAndroidTest("com.github.bumptech.glide:compiler:4.16.0") {
        exclude(group = "com.google.inject", module = "guice")
    }

    configurations.all {
        exclude(group = "com.google.inject", module = "guice")
    }

    // Firebase BOM (Bill of Materials)
    implementation(platform(libs.firebase.bom))

    // Library Firebase yang kita butuhkan
    implementation(libs.firebase.firestore)
    implementation(libs.firebase.analytics)

    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.6.2")
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:2.6.2")

    // Tambahkan Retrofit agar anotasi @GET, @POST, dll dikenali
    implementation("com.squareup.retrofit2:retrofit:2.9.0")

    // Tambahkan Converter (biasanya Gson) untuk parsing JSON
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")

    // Opsional: Logging Interceptor untuk melihat log API di Logcat
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")

    // Tambahkan ini untuk Login & Register
    implementation("com.google.firebase:firebase-auth-ktx:22.3.1")

    // Pastikan Firestore juga ada (kalau belum)
    implementation("com.google.firebase:firebase-firestore-ktx:24.10.2")

    // GSON (Untuk membaca file JSON)
    implementation("com.google.code.gson:gson:2.10.1")

}