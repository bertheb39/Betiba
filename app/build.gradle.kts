plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    id("kotlin-parcelize")
    alias(libs.plugins.google.gms.google.services)
}

android {
    namespace = "com.example.cantiquesdioula"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.example.cantiquesdioula"
        minSdk = 24
        targetSdk = 36
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
}

dependencies {

    // Importer la "Bill of Materials" (BOM) de Firebase
    // Cela gère automatiquement les versions de TOUTES les bibliothèques Firebase
    implementation(platform("com.google.firebase:firebase-bom:33.1.0"))

    // Pour lire et convertir facilement les données JSON
    implementation("com.google.code.gson:gson:2.10.1")

    // Pour les composants graphiques modernes de Material Design
    implementation("com.google.android.material:material:1.13.0")

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)

    // --- CORRECTION FINALE DE FIREBASE ---
    // 1. On supprime l'ancienne ligne 'implementation(libs.firebase.auth)'
    // 2. On ajoute les deux bibliothèques SANS version (la BOM s'en occupe)
    implementation("com.google.firebase:firebase-auth")
    implementation("com.google.firebase:firebase-auth-ktx")
    implementation("com.google.firebase:firebase-firestore-ktx")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.8.0")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.8.0")
    // --- FIN CORRECTION ---

    implementation(libs.androidx.credentials)
    implementation(libs.androidx.credentials.play.services.auth)
    implementation(libs.googleid)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}