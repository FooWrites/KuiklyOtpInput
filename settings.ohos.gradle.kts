pluginManagement {
    val OHOS_KOTLIN_VERSION: String by settings
    repositories {
        maven { url = uri("https://mirrors.tencent.com/nexus/repository/maven-tencent/") }
        maven { url = uri("https://mirrors.tencent.com/nexus/repository/maven-public/") }
        google()
        gradlePluginPortal()
        mavenCentral()
        mavenLocal()
    }
    plugins {
        kotlin("multiplatform") version OHOS_KOTLIN_VERSION
        kotlin("plugin.compose") version OHOS_KOTLIN_VERSION
        id("com.google.devtools.ksp") version "2.0.21-1.0.27"
        id("org.jetbrains.compose") version "1.7.3"
    }
}

dependencyResolutionManagement {
    repositories {
        maven { url = uri("https://mirrors.tencent.com/nexus/repository/maven-tencent/") }
        maven { url = uri("https://mirrors.tencent.com/nexus/repository/maven-public/") }
        google()
        mavenCentral()
        mavenLocal()
    }
}

val ohosBuildfFileName = "build.ohos.gradle.kts"
rootProject.buildFileName = ohosBuildfFileName
rootProject.name = "KuiklyOtpInputShell"

include(":shared")
include(":otpinput")
include(":otpinput-compose")
project(":shared").buildFileName = ohosBuildfFileName
project(":otpinput").buildFileName = ohosBuildfFileName
project(":otpinput-compose").buildFileName = ohosBuildfFileName
