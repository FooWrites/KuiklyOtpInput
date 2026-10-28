plugins {
    // 统一声明各子模块使用的插件，版本见 settings.gradle.kts
    id("com.android.application") apply false
    id("com.android.library") apply false
    kotlin("android") apply false
    kotlin("multiplatform") apply false
    id("com.google.devtools.ksp") apply false
    id("org.jetbrains.compose") apply false
    kotlin("plugin.compose") apply false
}

buildscript {
    repositories {
        maven { url = uri("https://mirrors.tencent.com/nexus/repository/maven-public/") }
        mavenCentral()
        gradlePluginPortal()
    }
    dependencies {
        classpath("com.tencent.kuikly-open:core-gradle-plugin:${property("KUIKLY_VERSION")}")
    }
}
