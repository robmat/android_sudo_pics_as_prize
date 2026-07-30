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
    id("com.github.triplet.play")
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

play {
    serviceAccountCredentials.set(rootProject.file("../play-console-api-465319-0f9c399097c5.json"))
    track.set("internal")
    defaultToAppBundles.set(true)
}

detekt {
    buildUponDefaultConfig = true
    config.setFrom(files("$projectDir/config/detekt/detekt.yml"))
}

dependencies {
    detektPlugins(libs.detekt.formatting)

    // core-ktx/lifecycle-runtime/activity-compose/compose/material3/navigation-compose/
    // accompanist/appcompat intentionally not on the shared catalog's values - this
    // repo is behind on all of them; bumping would be a real, untested-here change,
    // not a mechanical catalog migration.
    implementation("androidx.core:core-ktx:1.16.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.7")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.8.7")
    implementation("androidx.activity:activity-compose:1.10.1")
    implementation("androidx.compose.ui:ui:1.7.8")
    implementation("androidx.compose.ui:ui-util:1.7.8")
    implementation("androidx.compose.ui:ui-graphics:1.7.8")
    implementation("androidx.compose.ui:ui-tooling-preview:1.7.8")
    implementation("androidx.compose.material3:material3:1.3.2")
    implementation("androidx.compose.material:material-icons-extended")
    debugImplementation("androidx.compose.ui:ui-tooling:1.7.8")
    debugImplementation("androidx.compose.ui:ui-test-manifest:1.7.8")

    implementation("androidx.navigation:navigation-compose:2.8.9")

    implementation("com.google.accompanist:accompanist-systemuicontroller:0.28.0")
    implementation("com.google.accompanist:accompanist-pager-indicators:0.28.0")

    // Hilt/Room/ACRA/aboutlibraries/zoom-compose aren't in the shared catalog
    // (single-repo use).
    implementation("com.google.dagger:hilt-android:2.56.1")
    implementation("androidx.hilt:hilt-navigation-compose:1.2.0")
    ksp("com.google.dagger:hilt-compiler:2.56.1")

    implementation("androidx.room:room-runtime:2.7.0")
    implementation("androidx.room:room-ktx:2.7.0")
    ksp("androidx.room:room-compiler:2.7.0")

    // datastore-preferences intentionally not on the shared catalog's value
    // (1.1.7) - this repo is behind at 1.1.4.
    implementation("androidx.datastore:datastore-preferences:1.1.4")

    implementation("androidx.appcompat:appcompat:1.7.0")
    implementation("ch.acra:acra-dialog:5.9.7")
    implementation("ch.acra:acra-mail:5.9.7")

    implementation("com.mikepenz:aboutlibraries-compose:10.6.1")

    implementation (libs.bumptech.glide.compose)
    // play-services-ads intentionally not on the shared catalog's value
    // (25.4.0) - this repo is behind at 24.2.0.
    implementation ("com.google.android.gms:play-services-ads:24.2.0")
    implementation ("com.github.mennovogel:zoom-compose:1.1")

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
