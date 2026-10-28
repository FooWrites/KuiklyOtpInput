plugins {
    kotlin("multiplatform")
    id("com.google.devtools.ksp")
    id("org.jetbrains.compose")
    kotlin("plugin.compose")
}

val kuiklyOhosVersion = property("KUIKLY_OHOS_VERSION") as String

kotlin {
    ohosArm64 {
        binaries.sharedLib("shared") {
            freeCompilerArgs += "-Xadd-light-debug=enable"
            if (buildType == org.jetbrains.kotlin.gradle.plugin.mpp.NativeBuildType.RELEASE) {
                val clangOpt = "-Os -mllvm -enable-machine-outliner=always -ffunction-sections"
                val clangFlags = "clangOptFlags.ohos_arm64=$clangOpt;clangDebugFlags.ohos_arm64=$clangOpt"
                freeCompilerArgs += "-Xoverride-konan-properties=$clangFlags"
                linkerOpts += "--pack-dyn-relocs=relr"
                linkerOpts += "--gc-sections"
            }
        }
    }

    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation("com.tencent.kuikly-open:core:$kuiklyOhosVersion")
                implementation("com.tencent.kuikly-open:core-annotations:$kuiklyOhosVersion")
                implementation("com.tencent.kuikly-open:compose:$kuiklyOhosVersion")
                implementation(project(":otpinput"))
                implementation(project(":otpinput-compose"))
            }
        }
        val commonTest by getting {
            dependencies {
                implementation(kotlin("test"))
            }
        }
    }
}

dependencies {
    add("kspOhosArm64", "com.tencent.kuikly-open:core-ksp:$kuiklyOhosVersion")
}
