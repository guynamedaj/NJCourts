plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "edu.njit.njcourts"
    compileSdk = 36

    defaultConfig {
        applicationId = "edu.njit.njcourts"
        minSdk = 26
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
}

dependencies {
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)

    // Task 6: ViewModel
    implementation(libs.lifecycle.viewmodel)
    implementation(libs.lifecycle.livedata)

    // Task 7: Room
    implementation(libs.room.runtime)
    implementation(libs.object1.detection.common)
    implementation(libs.object1.detection)
    annotationProcessor(libs.room.compiler)

    // Task 10/20: ML Kit
    implementation(libs.mlkit.face.detection)
    //implementation(libs.mlkit.object.detection)


    // Task 14/31: Retrofit
    implementation(libs.retrofit)
    implementation(libs.retrofit.converter.gson)

    // Task 18: CameraX
    implementation(libs.camerax.core)
    implementation(libs.camerax.camera2)
    implementation(libs.camerax.lifecycle)
    implementation(libs.camerax.view)

    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)

}
