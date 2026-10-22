plugins {
    id("com.android.application")
    kotlin("android")
}

android {
    namespace = "com.foowrites.kuikly.otp.host"
    compileSdk = 34
    defaultConfig {
        applicationId = "com.foowrites.kuikly.otp.host"
        minSdk = 21
        targetSdk = 34
        versionCode = 1
        versionName = "0.1.0"
    }
    buildTypes {
        release {
            isMinifyEnabled = false
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
    lint {
        abortOnError = false
        checkReleaseBuilds = false
    }
}

dependencies {
    val kuiklyVersion: String by project
    implementation(project(":demo"))
    implementation(project(":otpinput"))
    implementation("com.tencent.kuikly-open:core:$kuiklyVersion")
    implementation("com.tencent.kuikly-open:core-render-android:$kuiklyVersion")
    implementation("com.tencent.kuikly-open:compose:$kuiklyVersion")
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.appcompat:appcompat:1.6.1")
}
