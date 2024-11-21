plugins {
    id("com.google.gms.google-services") // Aplicar el plugin de Google Services
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.example.funcion_loginregistro"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.funcion_loginregistro"
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
}

dependencies {
    // Firebase Storage
    implementation("com.google.firebase:firebase-storage:20.0.0") // Asegúrate de usar la versión más reciente de Firebase Storage

    // Dependencias comunes
    implementation("com.android.volley:volley:1.2.1")
    implementation("com.google.android.gms:play-services-auth:20.5.0") // Para la autenticación de Google
    implementation("com.google.firebase:firebase-analytics")

    // Firebase BOM para gestionar versiones de todas las dependencias de Firebase
    implementation(platform("com.google.firebase:firebase-bom:33.4.0"))

    // Otras dependencias
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    implementation(libs.firebase.firestore)
    implementation(libs.firebase.auth)

    // Dependencias para pruebas
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
}
