plugins {
    kotlin("multiplatform") version "2.1.21" apply false
    kotlin("android") version "2.1.21" apply false
    kotlin("plugin.compose") version "2.1.21" apply false
    id("com.android.library") version "8.5.2" apply false
    id("com.android.application") version "8.5.2" apply false
    id("com.google.devtools.ksp") version "2.1.21-2.0.1" apply false
}

subprojects {
    group = "com.github.FooWrites.KuiklyOtpInput"
    val tagged = System.getenv("VERSION")?.takeIf { it.isNotBlank() }
        ?: (findProperty("version") as String?)?.takeIf { it.isNotBlank() && it != "unspecified" }
    version = tagged ?: "v0.1.0"
}
