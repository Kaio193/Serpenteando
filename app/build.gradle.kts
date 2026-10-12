plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.serpenteando.cobra3d"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.serpenteando.cobra3d"
        minSdk = 23
        targetSdk = 36
        versionCode = 1
        versionName = "0.1.0"
    }

    buildTypes {
        debug {
            isMinifyEnabled = false
        }
        release {
            isMinifyEnabled = true
            signingConfig = signingConfigs.getByName("debug")
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
}

dependencies {
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.constraintlayout)
    implementation(libs.recyclerview)
    implementation(libs.cardview)
    implementation(libs.activity.ktx)
    implementation(libs.navigation.fragment)
    implementation(libs.navigation.ui)
    implementation(libs.dynamicanimation)
    implementation(libs.viewpager2)

    // Filament — motor 3D (Serpenteando renderiza modelo anatômico da cobra)
    implementation(libs.filament)
    implementation(libs.filament.utils)
    implementation(libs.filamat)

    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
}