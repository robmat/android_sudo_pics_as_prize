import java.io.FileInputStream
import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    id("com.google.devtools.ksp")
    // aboutlibraries isn't in the shared catalog (single-repo use). 10.6.1's
    // AboutLibrariesCollectorTask calls Task.project at execution time, which
    // the configuration cache rejects outright - fixed in v13's plugin split
    // (core manual-task plugin + this .android plugin for the automatic
    // Android build hook this app relies on to generate the runtime resource
    // LibrariesContainer reads).
    id("com.mikepenz.aboutlibraries.plugin") version "13.2.1"
    id("com.mikepenz.aboutlibraries.plugin.android") version "13.2.1"
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
    export {
        // Remove the "generated" timestamp to allow for reproducible builds
        excludeFields.addAll("generated")
    }
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
    // Bumped from 2.8.7 - the Compose 1.10.0 bump below now transitively
    // requires lifecycle-runtime-ktx 2.9.4 from several dependency paths.
    implementation(libs.androidx.lifecycle.runtime.ktx) { version { strictly("2.9.4") } }
    implementation(libs.sudoku.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.activity.compose) { version { strictly("1.10.1") } }
    // Bumped from 1.7.8 - com.mikepenz:aboutlibraries-compose:13.2.1 (below)
    // transitively requires androidx.compose.ui ~1.10.0, incompatible with the
    // old strict pin.
    implementation(libs.androidx.compose.ui) { version { strictly("1.10.0") } }
    implementation(libs.sudoku.androidx.compose.ui.util)
    implementation(libs.androidx.compose.ui.graphics) { version { strictly("1.10.0") } }
    implementation(libs.androidx.compose.ui.tooling.preview) { version { strictly("1.10.0") } }
    implementation(libs.androidx.compose.material3) { version { strictly("1.3.2") } }
    implementation(libs.androidx.compose.material.icons.extended)
    debugImplementation(libs.androidx.compose.ui.tooling) { version { strictly("1.10.0") } }
    debugImplementation(libs.androidx.compose.ui.test.manifest) { version { strictly("1.10.0") } }

    implementation(libs.androidx.navigation.compose) { version { strictly("2.8.9") } }

    implementation(libs.google.accompanist.systemuicontroller)
    implementation(libs.google.accompanist.pager.indicators)

    implementation(libs.hilt.android)
    implementation(libs.androidx.hilt.navigation.compose)
    ksp(libs.hilt.compiler)

    // Behind the shared catalog's value (2.8.4) - still sourced from it,
    // strictly pinned to this repo's own value.
    implementation(libs.androidx.room.runtime) { version { strictly("2.7.0") } }
    implementation(libs.androidx.room.ktx) { version { strictly("2.7.0") } }
    ksp(libs.androidx.room.compiler) { version { strictly("2.7.0") } }

    implementation(libs.androidx.datastore.preferences) { version { strictly("1.1.4") } }

    implementation(libs.androidx.appcompat) { version { strictly("1.7.0") } }
    implementation(libs.acra.dialog)
    implementation(libs.acra.mail)

    // -m3 (not the base -compose artifact): 13.x moved LibrariesContainer out
    // of the base module into the Material3-specific one - this app's UI is
    // all Material3. Kept in lockstep with the plugin version pinned above
    // rather than the shared catalog's 10.6.1 (which other repos still
    // depend on unchanged).
    implementation(libs.mikepenz.aboutlibraries.compose.m3) { version { strictly("13.2.1") } }

    implementation (libs.bumptech.glide.compose)
    implementation (libs.play.services.ads) { version { strictly("24.2.0") } }
    implementation (libs.mennovogel.zoom.compose) { version { strictly("1.1") } }

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.test.ext.junit)
    androidTestImplementation(libs.androidx.test.espresso.core)
    androidTestImplementation(libs.androidx.test.espresso.intents)
    androidTestImplementation(libs.sudoku.androidx.compose.ui.test.junit4)
    // ui-test-manifest (debugImplementation above) pulls old androidx.test:core/monitor/
    // concurrent-futures onto the debug classpath; AGP forces androidTest to resolve those
    // consistently with debug, so without bumping them here too, espresso/compose-test
    // can't satisfy their floor (same issue/fix as snake-game-android-main).
    debugImplementation(libs.androidx.test.core)
    debugImplementation(libs.androidx.test.monitor)
    debugImplementation(libs.androidx.concurrent.futures)
}
