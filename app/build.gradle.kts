plugins {
    id("com.google.gms.google-services")

    alias(libs.plugins.android.application)
}

android {
    namespace = "com.example.consultapp"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.consultapp"
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
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    buildFeatures {
        viewBinding = true
    }
}

dependencies {
    implementation("com.android.volley:volley:1.2.1")
    implementation("com.google.android.gms:play-services-auth:20.5.0") // Para la autenticación de Google
    implementation("com.google.firebase:firebase-analytics")
    implementation(platform("com.google.firebase:firebase-bom:33.4.0"))
    implementation("com.google.firebase:firebase-auth:21.0.5")  // Por ejemplo, si usas Firebase Auth
    implementation("com.google.firebase:firebase-firestore:24.0.0")  // Si usas Firestore
    implementation("com.google.android.gms:play-services-auth:20.0.0")  // Para Google Play Services
    implementation("com.google.firebase:firebase-auth:version") // Usa la última versión estable
    implementation ("com.google.firebase:firebase-firestore:version")
    implementation ("com.google.android.gms:play-services-auth:version")
    implementation ("com.google.firebase:firebase-storage:20.2.1")
    implementation ("com.google.android.material:material:1.9.0") // O la versión más reciente
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    implementation(libs.google.firebase.auth)
    implementation(libs.google.firebase.firestore)
    implementation(libs.lifecycle.livedata.ktx)
    implementation(libs.lifecycle.viewmodel.ktx)
    implementation(libs.navigation.fragment)
    implementation(libs.navigation.ui)
    implementation(libs.annotations)
    implementation(libs.annotations)
    implementation(libs.recyclerview)
    implementation(libs.firebase.storage)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
}