import io.sentry.android.gradle.extensions.SentryPluginExtension

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.serialization")
    id("io.sentry.android.gradle")
}

configure<SentryPluginExtension> {
    // Auto-upload des ProGuard mappings au build
    autoUploadProguardMapping = true
    autoInstallation = false  // On initialise manuellement
    enableTracing = true      // Performance monitoring

    // Mapping files
    includeProguardMapping = true

    // Source maps Kotlin (pour les stack traces)
    includeSourceContext = true
}

import java.util.Properties
import java.io.FileInputStream

val keystorePropertiesFile = rootProject.file("keystore.properties")
val keystoreProperties = Properties()
if (keystorePropertiesFile.exists()) {
    keystoreProperties.load(FileInputStream(keystorePropertiesFile))
}

android {
    namespace = "dev.voile"
    compileSdk = 34

    defaultConfig {
        applicationId = "dev.voile.app"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0.0"

        buildConfigField("String", "SUPABASE_URL", "\"\"")
        buildConfigField("String", "SUPABASE_ANON_KEY", "\"\"")
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    packaging {
        resources.excludes += setOf(
            "META-INF/LICENSE.md",
            "META-INF/LICENSE-notice.md",
        )
    }

    signingConfigs {
        create("release") {
            if (keystorePropertiesFile.exists()) {
                keyAlias = keystoreProperties["keyAlias"] as String
                keyPassword = keystoreProperties["keyPassword"] as String
                storeFile = file(keystoreProperties["storeFile"] as String)
                storePassword = keystoreProperties["storePassword"] as String
            }
        }
    }

    buildTypes {
        debug {
            buildConfigField("String", "SENTRY_DSN", "\"\"")
            buildConfigField("String", "SENTRY_RELEASE", "\"\"")
            buildConfigField("String", "SENTRY_ENVIRONMENT", "\"development\"")
            isMinifyEnabled = false
        }
        release {
            buildConfigField(
                "String",
                "SENTRY_DSN",
                "\"\""
            )
            buildConfigField(
                "String",
                "SENTRY_RELEASE",
                "\"1.0.0\""
            )
            buildConfigField(
                "String",
                "SENTRY_ENVIRONMENT",
                "\"production\""
            )
            isMinifyEnabled = true
            isShrinkResources = true
            signingConfig = signingConfigs.getByName("release")
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.14"
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }
}

dependencies {
    implementation("io.sentry:sentry-android:7.16.0")
    implementation("io.sentry:sentry-compose-android:7.16.0")
    
    // WireGuard
    implementation("com.wireguard.android:tunnel:1.0.20230725")
    implementation("com.wireguard.android:backend:1.0.20230725")

    // BouncyCastle pour X25519 (Curve25519 natif, audité)
    implementation("org.bouncycastle:bcprov-jdk18on:1.78.1")

    // OkHttp pour les requêtes Cloudflare
    implementation("com.squareup.okhttp3:okhttp:4.12.0")

    // Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.8.1")
    // Jetpack Compose
    implementation("androidx.compose.ui:ui:1.6.8")
    implementation("androidx.compose.material3:material3:1.2.1")
    implementation("androidx.compose.ui:ui-tooling-preview:1.6.8")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.2")
    implementation("androidx.activity:activity-compose:1.9.0")
    
    // WorkManager
    implementation("androidx.work:work-runtime-ktx:2.9.0")

    // Ktor
    implementation("io.ktor:ktor-client-android:2.3.11")
    implementation("io.ktor:ktor-client-serialization:2.3.11")

    // Kotlinx Serialization
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.3")

    // Supabase
    implementation("io.github.jan-tennert.supabase:gotrue-kt:2.6.1")
    implementation("io.github.jan-tennert.supabase:postgrest-kt:2.6.1")
    
    // DataStore Preferences
    implementation("androidx.datastore:datastore-preferences:1.1.1")

    androidTestImplementation("androidx.test.ext:junit:1.2.1")
    androidTestImplementation("androidx.test:runner:1.6.1")
    androidTestImplementation("androidx.test:rules:1.6.1")
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")
    androidTestImplementation("androidx.compose.ui:ui-test-manifest")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.6.1")
    androidTestImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.8.1")
    debugImplementation("androidx.compose.ui:ui-test-manifest")
}
