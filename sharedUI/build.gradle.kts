import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.gradle.kotlin.dsl.withType
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.compose.multiplatform)
    alias(libs.plugins.android.kmp.library)
    alias(libs.plugins.kotlinx.serialization)
}

kotlin {
    android {
        namespace = "com.jian.forkune"
        compileSdk = 36
        minSdk = 23
        androidResources.enable = true
        compilerOptions { jvmTarget = JvmTarget.JVM_17 }

        // Without this there is no `androidHostTest` source set, so everything in
        // commonTest runs on the iOS simulator target ONLY. Shared code deserves to be
        // asserted on both targets.
        withHostTest { }
    }

    iosArm64()
    iosSimulatorArm64()

    sourceSets {
        commonMain.dependencies {
            // `api` only for what a consumer of this module compiles against: androidApp's
            // AppActivity touches compose-runtime and compose-ui directly. Everything else is
            // an implementation detail of the shared UI and stays off the consumer classpath.
            api(libs.compose.runtime)
            api(libs.compose.ui)

            implementation(project.dependencies.platform(libs.koin.bom))
            implementation(libs.koin.core)
            implementation(libs.koin.compose)
            implementation(libs.koin.core.viewmodel)
            implementation(libs.koin.compose.viewmodel)
            implementation(libs.compass.geolocation)
            implementation(libs.compass.geolocation.mobile)
            implementation(libs.compass.geocoder)
            implementation(libs.compass.geocoder.mobile)
            implementation(libs.compass.permissions)
            implementation(libs.compass.permissions.mobile)
            implementation(libs.compose.foundation)
            implementation(libs.compose.resources)
            implementation(libs.compose.ui.tooling.preview)
            implementation(libs.compose.material3)
            implementation(libs.kotlinx.coroutines.core)
            implementation(libs.ktor.client.core)
            implementation(libs.ktor.client.content.negotiation)
            implementation(libs.ktor.client.serialization)
            implementation(libs.ktor.serialization.json)
            implementation(libs.ktor.client.logging)
            implementation(libs.androidx.lifecycle.viewmodel)
            implementation(libs.androidx.lifecycle.runtime)
            implementation(libs.androidx.lifecycle.viewmodel.navigation3)
            implementation(libs.compose.nav3)
            implementation(libs.androidx.savedstate)
            implementation(libs.kotlinx.serialization.json)
            implementation(libs.coil)
            implementation(libs.coil.network.ktor)
            implementation(libs.kotlinx.datetime)
        }

        commonTest.dependencies {
            implementation(kotlin("test"))
            implementation(libs.compose.ui.test)
            implementation(libs.kotlinx.coroutines.test)
        }

        androidMain.dependencies {
            implementation(libs.androidx.core.ktx)
            implementation(libs.kotlinx.coroutines.android)
            implementation(libs.ktor.client.okhttp)
        }

        iosMain.dependencies {
            implementation(libs.ktor.client.darwin)
        }

    }

    targets
        .withType<KotlinNativeTarget>()
        .matching { it.konanTarget.family.isAppleFamily }
        .configureEach {
            binaries {
                framework {
                    baseName = "SharedUI"
                    isStatic = true
                }
            }
        }
}

dependencies {
    androidRuntimeClasspath(libs.compose.ui.tooling)
}
