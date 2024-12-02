plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.jetbrains.kotlin.android)
    alias(libs.plugins.google.gms.google.services)
}

android {
    namespace = "com.example.thisapp"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.thisapp"
        minSdk = 29
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

    kotlinOptions {
        jvmTarget = "1.8"
    }

    buildFeatures {
        viewBinding = true
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.navigation.fragment.ktx)
    implementation(libs.androidx.navigation.ui.ktx)
    implementation(libs.androidx.activity)
    implementation("androidx.viewpager2:viewpager2:1.0.0")

    // MongoDB Realm
    implementation("io.realm.kotlin:library-base:1.8.0")
    implementation(libs.firebase.firestore)

    implementation ("androidx.camera:camera-core:1.4.0")
    implementation ("androidx.camera:camera-camera2:1.4.0")
    implementation ("androidx.camera:camera-lifecycle:1.4.0")
    implementation ("androidx.camera:camera-view:1.4.0")
    // Gunakan ML Kit dari Play Services
    implementation(libs.play.services.mlkit.face.detection)
    implementation ("com.google.guava:guava:32.1.2-android")
    implementation("org.greenrobot:eventbus:3.3.1")

    //firebase
    implementation(platform("com.google.firebase:firebase-bom:33.5.1"))
    implementation("com.google.firebase:firebase-auth")
    implementation(libs.firebase.messaging)
    implementation(libs.firebase.database.ktx)
    implementation(libs.firebase.storage.ktx)
    implementation(libs.firebase.storage)

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}