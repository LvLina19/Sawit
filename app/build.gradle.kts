plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.google.gms.google.services)
    id("org.jetbrains.kotlin.plugin.serialization") version "1.9.22"
    id("kotlin-parcelize")
}

android {
    namespace = "com.example.sawit"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.example.sawit"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"
        buildConfigField("String", "GEMINI_API_KEY", "\"${project.findProperty("GEMINI_API_KEY")}\"")

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
    buildFeatures{
        viewBinding = true
        buildConfig = true

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
    // ==================== CORE ANDROID ====================
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("androidx.activity:activity-ktx:1.8.2")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("androidx.cardview:cardview:1.0.0")
    implementation("androidx.recyclerview:recyclerview:1.3.2")

    // ==================== MATERIAL DESIGN ====================
    implementation(libs.material)
    implementation("com.google.android.material:material:1.11.0")

    // ==================== LIFECYCLE & COROUTINES ====================
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.7.0")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")

    // ==================== GOOGLE AI (GEMINI) ====================
    implementation("com.google.ai.client.generativeai:generativeai:0.9.0")

    // Ktor Client - WAJIB untuk Google AI SDK
    implementation("io.ktor:ktor-client-android:2.3.7")
    implementation("io.ktor:ktor-client-core:2.3.7")
    implementation("io.ktor:ktor-client-okhttp:2.3.7")
    implementation("io.ktor:ktor-client-content-negotiation:2.3.7")
    implementation("io.ktor:ktor-serialization-kotlinx-json:2.3.7")

    // Kotlinx Serialization - WAJIB untuk Ktor
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.2")

    // ==================== FIREBASE ====================
    implementation(platform("com.google.firebase:firebase-bom:34.6.0"))
    implementation("com.google.firebase:firebase-analytics")
    implementation("com.google.firebase:firebase-auth:23.0.0")
    implementation("com.google.firebase:firebase-database")
    implementation("com.google.firebase:firebase-firestore")
    implementation(libs.firebase.auth)
    implementation(libs.firebase.ai)
    implementation(libs.firebase.database)
    implementation(libs.firebase.firestore)

    // ==================== GOOGLE PLAY SERVICES ====================
    implementation("com.google.android.gms:play-services-auth:21.2.0")
    implementation(libs.androidx.credentials)
    implementation(libs.androidx.credentials.play.services.auth)
    implementation(libs.googleid)

    // ==================== RETROFIT & NETWORKING ====================
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")

    // ==================== IMAGE LOADING ====================
    implementation("com.github.bumptech.glide:glide:4.16.0")
    implementation(libs.firebase.storage)
    annotationProcessor("com.github.bumptech.glide:compiler:4.16.0")

    // ==================== CHARTS ====================
    implementation("com.github.PhilJay:MPAndroidChart:v3.1.0")

    // ==================== MACHINE LEARNING ====================
    // ONNX Runtime
    implementation("com.microsoft.onnxruntime:onnxruntime-android:1.23.2")

    // OpenCV
    implementation("org.opencv:opencv:4.9.0")

    // ==================== TESTING ====================
    testImplementation(libs.junit)
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
    // Glide untuk load image dari URL

    implementation ("com.github.bumptech.glide:glide:4.16.0")
    annotationProcessor ("com.github.bumptech.glide:compiler:4.16.0")
    // Activity Result API
    implementation("androidx.fragment:fragment-ktx:1.6.2")

}