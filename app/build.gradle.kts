import java.io.FileInputStream
import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    id("com.google.devtools.ksp")
    // aboutlibraries isn't in the shared catalog (single-repo use).
    id("com.mikepenz.aboutlibraries.plugin") version "10.6.1"
    id("com.google.dagger.hilt.android")
    alias(libs.plugins.kotlin.compose)
    id("com.batodev.releasetools")
    id("io.gitlab.arturbosch.detekt")
}

var localProperties = Properties()
localProperties.load(FileInputStream(rootProject.file("local.properties")))

val keystorePropertiesFile = rootProject.file("../keystore.properties")
val keystoreProperties = Properties()
if (keystorePropertiesFile.exists()) {
    keystoreProperties.load(FileInputStream(keystorePropertiesFile))
}

val versionProps = Properties()
file("version.properties").inputStream().use { versionProps.load(it) }

android {
    namespace = "com.batodev.sudoku"
    compileSdk = 37

    defaultConfig {
        applicationId = "com.batodev.sudoku"
        minSdk = 26
        targetSdk = 37
        versionCode = versionProps.getProperty("versionCode").toInt()
        versionName = versionProps.getProperty("versionName")

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        vectorDrawables {
            useSupportLibrary = true
        }

        ksp {
            arg("room.schemaLocation", "${projectDir}/schemas")
        }
        buildConfigField(
            "String",
            "AD_HELPER_AD_ID",
            "\"" + localProperties.getProperty("adhelper.ad.id") + "\""
        )

        manifestPlaceholders.put("MANIFEST_AD_ID", localProperties.getProperty("manifest.ad.id"))
    }

    signingConfigs {
        create("release") {
            if (keystorePropertiesFile.exists()) {
                storeFile = rootProject.file(keystoreProperties.getProperty("storeFile"))
                storePassword = keystoreProperties.getProperty("storePassword")
                keyAlias = keystoreProperties.getProperty("keyAlias")
                keyPassword = keystoreProperties.getProperty("keyPassword")
            }
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
            signingConfig = signingConfigs.getByName("release")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.1"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

aboutLibraries {
    // Remove the "generated" timestamp to allow for reproducible builds
    excludeFields = arrayOf("generated")
}

detekt {
    buildUponDefaultConfig = true
    config.setFrom(files("$projectDir/config/detekt/detekt.yml"))
}

dependencies {
    detektPlugins(libs.detekt.formatting)

    // Behind the shared catalog's versions - still sourced from it, strictly
    // pinned to this repo's own values rather than bumped as a side effect.
    implementation(libs.androidx.core.ktx) { version { strictly("1.16.0") } }
    implementation(libs.androidx.lifecycle.runtime.ktx) { version { strictly("2.8.7") } }
    // lifecycle-runtime-compose isn't in the shared catalog.
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.8.7")
    implementation(libs.androidx.activity.compose) { version { strictly("1.10.1") } }
    implementation(libs.androidx.compose.ui) { version { strictly("1.7.8") } }
    // ui-util isn't in the shared catalog.
    implementation("androidx.compose.ui:ui-util:1.7.8")
    implementation(libs.androidx.compose.ui.graphics) { version { strictly("1.7.8") } }
    implementation(libs.androidx.compose.ui.tooling.preview) { version { strictly("1.7.8") } }
    implementation(libs.androidx.compose.material3) { version { strictly("1.3.2") } }
    implementation(libs.androidx.compose.material.icons.extended)
    debugImplementation(libs.androidx.compose.ui.tooling) { version { strictly("1.7.8") } }
    debugImplementation(libs.androidx.compose.ui.test.manifest) { version { strictly("1.7.8") } }

    implementation(libs.androidx.navigation.compose) { version { strictly("2.8.9") } }

    // accompanist-systemuicontroller/-pager-indicators aren't in the shared catalog.
    implementation("com.google.accompanist:accompanist-systemuicontroller:0.28.0")
    implementation("com.google.accompanist:accompanist-pager-indicators:0.28.0")

    // Hilt/Room/ACRA/aboutlibraries aren't in the shared catalog (single-repo use).
    implementation("com.google.dagger:hilt-android:2.56.1")
    implementation("androidx.hilt:hilt-navigation-compose:1.2.0")
    ksp("com.google.dagger:hilt-compiler:2.56.1")

    implementation("androidx.room:room-runtime:2.7.0")
    implementation("androidx.room:room-ktx:2.7.0")
    ksp("androidx.room:room-compiler:2.7.0")

    implementation(libs.androidx.datastore.preferences) { version { strictly("1.1.4") } }

    implementation(libs.androidx.appcompat) { version { strictly("1.7.0") } }
    implementation("ch.acra:acra-dialog:5.9.7")
    implementation("ch.acra:acra-mail:5.9.7")

    implementation("com.mikepenz:aboutlibraries-compose:10.6.1")

    implementation (libs.bumptech.glide.compose)
    implementation (libs.play.services.ads) { version { strictly("24.2.0") } }
    implementation (libs.mennovogel.zoom.compose) { version { strictly("1.1") } }

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.test.ext.junit)
    androidTestImplementation(libs.androidx.test.espresso.core)
    androidTestImplementation(libs.androidx.test.espresso.intents)
    androidTestImplementation("androidx.compose.ui:ui-test-junit4:1.7.8")
    // ui-test-manifest (debugImplementation above) pulls old androidx.test:core/monitor/
    // concurrent-futures onto the debug classpath; AGP forces androidTest to resolve those
    // consistently with debug, so without bumping them here too, espresso/compose-test
    // can't satisfy their floor (same issue/fix as snake-game-android-main).
    debugImplementation(libs.androidx.test.core)
    debugImplementation(libs.androidx.test.monitor)
    debugImplementation(libs.androidx.concurrent.futures)
}
