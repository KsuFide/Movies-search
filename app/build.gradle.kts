plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    id("kotlin-kapt")
    id("com.google.dagger.hilt.android")
    id("kotlin-parcelize")
}

android {
    namespace = "com.example.moviesearch"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.moviesearch"
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
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }

    kotlin {
        jvmToolchain(17)
    }

    buildFeatures {
        viewBinding = true
        buildConfig = true
    }
    kapt {
        correctErrorTypes = true
        javacOptions {
            option("--add-exports=jdk.compiler/com.sun.tools.javac.main=ALL-UNNAMED")
            option("--add-exports=jdk.compiler/com.sun.tools.javac.util=ALL-UNNAMED")
            option("--add-exports=jdk.compiler/com.sun.tools.javac.tree=ALL-UNNAMED")
            option("--add-exports=jdk.compiler/com.sun.tools.javac.code=ALL-UNNAMED")
            option("--add-exports=jdk.compiler/com.sun.tools.javac.processing=ALL-UNNAMED")
            option("--add-exports=jdk.compiler/com.sun.tools.javac.api=ALL-UNNAMED")
            option("--add-exports=jdk.compiler/com.sun.tools.javac.comp=ALL-UNNAMED")
            option("--add-exports=jdk.compiler/com.sun.tools.javac.parser=ALL-UNNAMED")
            option("--add-exports=jdk.compiler/com.sun.tools.javac.model=ALL-UNNAMED")
        }
    }
}

dependencies {
    // Базовые Android зависимости
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)

    // Navigation
    implementation("androidx.navigation:navigation-fragment-ktx:2.7.2")
    implementation("androidx.navigation:navigation-ui-ktx:2.7.2")

    // Glide
    implementation("com.github.bumptech.glide:glide:4.15.1")
    kapt("com.github.bumptech.glide:compiler:4.15.1")

    // Retrofit
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.11.0")

    // Hilt
    implementation("com.google.dagger:hilt-android:2.48")
    kapt("com.google.dagger:hilt-compiler:2.48")

    // Splashscreen
    implementation("androidx.core:core-splashscreen:1.0.1")

    // Material Design (только одна версия!)
    implementation("com.google.android.material:material:1.9.0")

    // Тестирование
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    // SwipeRefreshLayout
    implementation("androidx.swiperefreshlayout:swiperefreshlayout:1.1.0")
}

kapt {
    correctErrorTypes = true
}