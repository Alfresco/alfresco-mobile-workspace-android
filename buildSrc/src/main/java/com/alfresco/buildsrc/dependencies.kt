package com.alfresco.buildsrc

@Suppress("unused")
object Libs {

    const val androidGradlePlugin = "com.android.tools.build:gradle:4.0.1"

    const val junit = "junit:junit:4.13"

    const val spotless = "com.diffplug.spotless:spotless-plugin-gradle:5.1.1"

    const val gradleVersions = "com.github.ben-manes:gradle-versions-plugin:0.29.0"

    object Alfresco {
        const val auth = "com.alfresco.android:auth:0.6-SNAPSHOT"
        const val content = "com.alfresco.android:content:0.2-SNAPSHOT"
    }

    object Kotlin {
        private const val version = "1.4.0"
        const val stdlib = "org.jetbrains.kotlin:kotlin-stdlib-jdk8:$version"
        const val reflect = "org.jetbrains.kotlin:kotlin-reflect:$version"
        const val gradlePlugin = "org.jetbrains.kotlin:kotlin-gradle-plugin:$version"
        const val extensions = "org.jetbrains.kotlin:kotlin-android-extensions:$version"
    }

    object Coroutines {
        private const val version = "1.3.9"
        const val core = "org.jetbrains.kotlinx:kotlinx-coroutines-core:$version"
        const val android = "org.jetbrains.kotlinx:kotlinx-coroutines-android:$version"
        const val test = "org.jetbrains.kotlinx:kotlinx-coroutines-test:$version"
    }

    object AndroidX {
        const val appcompat = "androidx.appcompat:appcompat:1.2.0"
        const val coreKtx = "androidx.core:core-ktx:1.3.1"

        object Activity {
            private const val version = "1.1.0"
            const val activity = "androidx.activity:activity:$version"
            const val activityKtx = "androidx.activity:activity-ktx:$version"
        }

        object Fragment {
            private const val version = "1.2.5"
            const val fragment = "androidx.fragment:fragment:$version"
            const val fragmentKtx = "androidx.fragment:fragment-ktx:$version"
        }

        object Lifecycle {
            private const val version = "2.2.0"
            const val viewmodelKtx = "androidx.lifecycle:lifecycle-viewmodel-ktx:$version"
            const val runtime = "androidx.lifecycle:lifecycle-runtime-ktx:$version"
        }

        const val recyclerview = "androidx.recyclerview:recyclerview:1.1.0"

        const val constraintlayout = "androidx.constraintlayout:constraintlayout:1.1.3"

        const val coordinatorlayout = "androidx.coordinatorlayout:coordinatorlayout:1.1.0"

        const val swiperefreshlayout = "androidx.swiperefreshlayout:swiperefreshlayout:1.1.0"

        const val preference = "androidx.preference:preference:1.1.1"

        object Navigation {
            private const val version = "2.3.0"
            const val fragment = "androidx.navigation:navigation-fragment-ktx:$version"
            const val ui = "androidx.navigation:navigation-ui-ktx:$version"
        }

        object Test {
            private const val version = "1.2.0"
            const val core = "androidx.test:core:$version"
            const val runner = "androidx.test:runner:$version"
            const val rules = "androidx.test:rules:$version"

            const val espressoCore = "androidx.test.espresso:espresso-core:3.2.0"
        }
    }

    object Google {
        const val material = "com.google.android.material:material:1.2.0"

        const val servicesPlugin = "com.google.gms:google-services:4.3.3"

        object Firebase {
            const val analytics = "com.google.firebase:firebase-analytics-ktx:17.5.0"
            const val crashlytics = "com.google.firebase:firebase-crashlytics-ktx:17.2.1"
            const val crashlyticsPlugin = "com.google.firebase:firebase-crashlytics-gradle:2.2.1"
        }
    }

    object OkHttp {
        private const val version = "4.8.1"
        const val okhttp = "com.squareup.okhttp3:okhttp:$version"
        const val loggingInterceptor = "com.squareup.okhttp3:logging-interceptor:$version"
    }

    const val mvRx = "com.airbnb.android:mvrx:1.5.1"

    object Epoxy {
        private const val version = "3.11.0"
        const val epoxy = "com.airbnb.android:epoxy:$version"
        const val processor = "com.airbnb.android:epoxy-processor:$version"
    }

    object Coil {
        private const val version = "0.13.0"
        const val core = "io.coil-kt:coil:$version"
        const val gifExt = "io.coil-kt:coil-gif:$version"
        const val svgExt = "io.coil-kt:coil-svg:$version"
    }

    object ExoPlayer {
        private const val version = "2.12.0"
        const val core = "com.google.android.exoplayer:exoplayer-core:$version"
        const val ui = "com.google.android.exoplayer:exoplayer-ui:$version"
    }
}
