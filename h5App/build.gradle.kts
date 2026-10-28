plugins {
    kotlin("multiplatform")
}

val kuiklyVersion = property("KUIKLY_VERSION") as String

kotlin {
    js(IR) {
        browser {
            webpackTask {
                outputFileName = "h5App.js"
            }
            commonWebpackConfig {
                output?.library = null
            }
        }
        binaries.executable()
    }
    sourceSets {
        val jsMain by getting {
            dependencies {
                implementation(project(":shared"))
                implementation("com.tencent.kuikly-open.core-render-web:base:$kuiklyVersion")
                implementation("com.tencent.kuikly-open.core-render-web:h5:$kuiklyVersion")
            }
        }
    }
}
