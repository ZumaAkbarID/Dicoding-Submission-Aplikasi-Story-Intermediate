plugins {
    id("com.android.application")
    id("kotlin-android")
    id("kotlin-parcelize")
    id("com.google.android.libraries.mapsplatform.secrets-gradle-plugin")
    id("com.google.devtools.ksp")
}

android {
    namespace = "com.rwa.tellme"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.rwa.tellme"
        minSdk = 21
        targetSdk = 33
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        buildConfigField("String", "BASE_URL", "\"https://story-api.dicoding.dev/v1/\"")
    }

    buildTypes {
        named("release") {
            isMinifyEnabled = false
            setProguardFiles(listOf(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro"))
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
        buildConfig = true
    }
}

dependencies {

    implementation("androidx.core:core-ktx:1.15.0")
    implementation("androidx.appcompat:appcompat:1.7.0")
    implementation("com.google.android.material:material:1.12.0")
    implementation("androidx.constraintlayout:constraintlayout:2.2.0")
    implementation("androidx.activity:activity-ktx:1.9.3")
    implementation("androidx.activity:activity:1.9.3")
    implementation("com.google.android.gms:play-services-maps:18.1.0")
    testImplementation("junit:junit:4.13.2")
    testImplementation("io.kotlintest:kotlintest-runner-junit5:3.1.7")
    testImplementation("androidx.test.ext:junit-ktx:1.2.1")
    androidTestImplementation("androidx.test.ext:junit:1.2.1")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.6.1")
    testImplementation("org.robolectric:robolectric:4.4")
    implementation("androidx.test:core:1.6.1")
    testImplementation("io.mockk:mockk:1.9.3")

    implementation("androidx.datastore:datastore-preferences:1.1.1")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.8.7")
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:2.8.7")

    implementation("com.github.bumptech.glide:glide:4.16.0")
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.11.0")

    implementation("com.github.bumptech.glide:glide:4.16.0")

    implementation("com.github.yalantis:ucrop:2.2.8")

    implementation("androidx.paging:paging-runtime-ktx:3.1.0")
    implementation("androidx.room:room-paging:2.4.0-rc01")

    androidTestImplementation("androidx.arch.core:core-testing:2.2.0")
    androidTestImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.7.3")

    testImplementation("androidx.arch.core:core-testing:2.2.0")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.7.3")
    testImplementation("org.mockito:mockito-core:5.5.0")
    testImplementation("org.mockito:mockito-inline:4.4.0")
    testImplementation("org.mockito.kotlin:mockito-kotlin:5.0.0")
    androidTestImplementation("org.mockito:mockito-android:4.11.0")
    androidTestImplementation("androidx.arch.core:core-testing:2.2.0")

    implementation("androidx.room:room-runtime:2.5.2")
    ksp("androidx.room:room-compiler:2.5.2")
    implementation("androidx.room:room-ktx:2.5.2")

    implementation("com.google.android.gms:play-services-location:21.3.0")

    implementation("androidx.test.espresso:espresso-intents:3.6.1")
    implementation("androidx.test.espresso:espresso-idling-resource:3.6.1")
    implementation("com.android.support.test.espresso:espresso-contrib:3.0.2")
}