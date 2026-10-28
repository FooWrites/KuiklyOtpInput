plugins {
    // 版本见 settings.ohos.gradle.kts
    kotlin("multiplatform") apply false
    id("com.google.devtools.ksp") apply false
    id("org.jetbrains.compose") apply false
    kotlin("plugin.compose") apply false
}

tasks.register("publishOhosToMavenLocal") {
    dependsOn(":otpinput:publishToMavenLocal")
    dependsOn(":otpinput-compose:publishToMavenLocal")
}
