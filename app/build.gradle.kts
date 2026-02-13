import io.grpc.internal.SharedResourceHolder.release
import java.util.Properties
import java.io.FileInputStream

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    id("kotlin-kapt")
    id("com.google.dagger.hilt.android")
    id("com.google.gms.google-services")
    id("androidx.navigation.safeargs.kotlin") // 네비게이션 인수 전달
}

android {
    namespace = "com.example.momenty"
    compileSdk = 36

    signingConfigs {
        create("release") {
            storeFile = file("momenty_release_key.jks")
            storePassword = "momenty_key"
            keyAlias = "momenty_key"
            keyPassword = "momenty_key"
        }
    }

    defaultConfig {
        applicationId = "com.example.momenty"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        // local.properties
        val properties = Properties()
        val localPropertiesFile = rootProject.file("local.properties")
        if (localPropertiesFile.exists()) {
            properties.load(FileInputStream(localPropertiesFile))
        }

        val kakaoKey = properties.getProperty("KAKAO_NATIVE_APP_KEY") ?: ""
        val naverClientId = properties.getProperty("NAVER_CLIENT_ID") ?: ""
        val naverClientSecret = properties.getProperty("NAVER_CLIENT_SECRET") ?: ""

        buildConfigField("String", "KAKAO_NATIVE_APP_KEY", "\"$kakaoKey\"")
        buildConfigField("String", "NAVER_CLIENT_ID", "\"$naverClientId\"")
        buildConfigField("String", "NAVER_CLIENT_SECRET", "\"$naverClientSecret\"")
        manifestPlaceholders["KAKAO_NATIVE_APP_KEY"] = kakaoKey
        manifestPlaceholders["NAVER_CLIENT_ID"] = naverClientId
        manifestPlaceholders["NAVER_CLIENT_SECRET"] = naverClientSecret
    }

    buildTypes {
        debug {
            versionNameSuffix = "-dev"
            isDebuggable = true
            buildConfigField("Boolean", "USE_MOCK_API", "true")  // ← 추가
            buildConfigField("String", "API_BASE_URL", "\"https://dev.api.momenty.com/\"")  // ← 추가
        }
        release {
            isMinifyEnabled = true
            signingConfig = signingConfigs.getByName("release")
            buildConfigField("Boolean", "USE_MOCK_API", "false")
            buildConfigField("String", "API_BASE_URL", "\"https://api.momenty.com/\"")
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
        viewBinding = true
        buildConfig = true
        dataBinding = true
    }
}

dependencies {
    // Firebase
    implementation(platform("com.google.firebase:firebase-bom:33.7.0"))
    implementation("com.google.firebase:firebase-messaging-ktx")


    // 카카오 SDK
    implementation("com.kakao.sdk:v2-user:2.20.1")

    // Google
    implementation("com.google.android.gms:play-services-auth:21.2.0")

    // 네이버 SDK
    implementation("com.navercorp.nid:oauth:5.9.1")

    // Retrofit
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")

    // OkHttp
    implementation("com.squareup.okhttp3:okhttp:4.11.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.11.0")

    // Hilt
    implementation("com.google.dagger:hilt-android:2.52")
    implementation(libs.androidx.ui)
    kapt("com.google.dagger:hilt-compiler:2.52")


    // Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-play-services:1.7.3")

    // ViewModel & LiveData
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.7.0")
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:2.7.0")
    implementation("androidx.activity:activity-ktx:1.8.2")
    implementation("androidx.fragment:fragment-ktx:1.6.2")



    // Android 기본
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)

    // Glide
    implementation("com.github.bumptech.glide:glide:4.16.0")
    implementation("com.github.bumptech.glide:compiler:4.11.0")

    // Splash
    implementation("androidx.core:core-splashscreen:1.0.1")


    implementation("androidx.security:security-crypto:1.1.0-alpha06")

    // 테스트
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    // Navigation Component
    implementation("androidx.navigation:navigation-fragment-ktx:2.7.7")
    implementation("androidx.navigation:navigation-ui-ktx:2.7.7")

    // 캘린더 recyclerView, cardView
    implementation("androidx.recyclerview:recyclerview:1.3.2")
    implementation("androidx.cardview:cardview:1.0.0")


}