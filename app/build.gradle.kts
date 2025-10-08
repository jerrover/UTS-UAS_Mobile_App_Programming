plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    // Versi sudah diatur di settings.gradle.kts, jadi tidak perlu di sini
    id("org.jetbrains.kotlin.plugin.compose")
}

android {
    namespace = "com.example.uts_uasmobileappprogramming"
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
    buildFeatures {
        compose = true
    }
    // Blok ini tidak lagi diperlukan untuk Kotlin 2.0+
    // composeOptions {
    //     kotlinCompilerExtensionVersion = "1.5.11"
    // }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    // implementation(libs.androidx.appcompat) // DIHAPUS: Tidak diperlukan untuk Compose
    implementation(libs.material)
    implementation(libs.androidx.activity)
    // implementation(libs.androidx.constraintlayout) // DIHAPUS: Tidak diperlukan untuk Compose

    // Jetpack Compose Dependencies
    implementation(platform("androidx.compose:compose-bom:2024.06.00"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    // Pastikan Anda menggunakan activity-compose, ini sangat penting
    implementation("androidx.activity:activity-compose:1.9.0")
    implementation("androidx.navigation:navigation-compose:2.7.7")

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    // Menambahkan BOM untuk Android Test agar library test ditemukan
    androidTestImplementation(platform("androidx.compose:compose-bom:2024.06.00"))
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")

    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")
}