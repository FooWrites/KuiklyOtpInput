import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    kotlin("multiplatform")
    id("com.android.library")
    `maven-publish`
}

val kuiklyVersion = property("KUIKLY_VERSION") as String

kotlin {
    androidTarget {
        publishLibraryVariants("release")
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_1_8)
        }
    }
    js(IR) {
        browser()
    }
    iosArm64()
    iosSimulatorArm64()
    iosX64()
    sourceSets {
        commonMain.dependencies {
            implementation("com.tencent.kuikly-open:core:$kuiklyVersion")
        }
        commonTest.dependencies {
            implementation(kotlin("test"))
        }
    }
}

android {
    namespace = "com.kuikly.otp"
    compileSdk = 34
    defaultConfig {
        minSdk = 21
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    lint {
        abortOnError = false
        checkReleaseBuilds = false
    }
}

group = "com.github.FooWrites.KuiklyOtpInput"
// JitPack 以 tag 作为 VERSION，tag 需与 {MAVEN_VERSION}-{KOTLIN_VERSION} 一致
version = System.getenv("VERSION")?.takeIf { it.isNotBlank() }
    ?: "${property("MAVEN_VERSION")}-${property("KOTLIN_VERSION")}"

publishing {
    publications.withType<MavenPublication>().configureEach {
        pom {
            name.set("otpinput")
            description.set("Kuikly OTP input — Kuikly DSL.")
            url.set("https://github.com/FooWrites/KuiklyOtpInput")
            licenses {
                license {
                    name.set("MIT License")
                    url.set("https://opensource.org/licenses/MIT")
                }
            }
            developers {
                developer {
                    id.set("FooWrites")
                    name.set("FooWrites")
                }
            }
            scm {
                url.set("https://github.com/FooWrites/KuiklyOtpInput")
                connection.set("scm:git:https://github.com/FooWrites/KuiklyOtpInput.git")
                developerConnection.set("scm:git:ssh://git@github.com/FooWrites/KuiklyOtpInput.git")
            }
        }
    }
}
