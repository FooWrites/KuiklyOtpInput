pluginManagement {
    val KOTLIN_VERSION: String by settings
    repositories {
        google()
        gradlePluginPortal()
        mavenCentral()
        maven { url = uri("https://mirrors.tencent.com/nexus/repository/maven-public/") }
        mavenLocal()
        maven { url = uri("https://mirrors.tencent.com/nexus/repository/maven-tencent/") }
    }
    plugins {
        id("com.android.application") version "8.2.2"
        id("com.android.library") version "8.2.2"
        kotlin("android") version KOTLIN_VERSION
        kotlin("multiplatform") version KOTLIN_VERSION
        kotlin("plugin.compose") version KOTLIN_VERSION
        id("com.google.devtools.ksp") version "2.1.21-2.0.2"
        id("org.jetbrains.compose") version "1.7.3"
    }
}

dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
        maven { url = uri("https://mirrors.tencent.com/nexus/repository/maven-public/") }
        gradlePluginPortal()
        mavenLocal()
        maven { url = uri("https://mirrors.tencent.com/nexus/repository/maven-tencent/") }
    }
}

rootProject.name = "KuiklyOtpInput"
include(":androidApp")
include(":shared")
include(":otpinput")
include(":otpinput-compose")
include(":h5App")
