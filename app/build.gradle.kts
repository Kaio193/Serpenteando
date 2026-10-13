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

    // O modelo 3D (assets/Serpente_3D.glb, 66 MB) NÃO entra comprimido no APK.
    //
    // Sem isto o AAPT2 guarda o .glb com deflate, e toda abertura da aba 3D
    // precisava INFLAR os 66 MB antes do Filament ler o arquivo — era essa a
    // demora de ~3 s entre a tela de carregamento e o modelo (medido no APK:
    // 66.817.284 bytes guardados em 48.013.859 = Defl:N). Guardado sem
    // compressão (Stored), o AssetManager lê direto, sem descompactar.
    // Custo: o APK cresce ~19 MB, mas só na instalação — o que o usuário
    // ganha é a abertura do 3D quase imediata.
    androidResources {
        noCompress += "glb"
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