// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    // KSP isn't in the shared catalog (only a handful of repos use it, each
    // pinned to a KSP release matching its own Kotlin version).
    id("com.google.devtools.ksp") version "2.3.2" apply false
    // Hilt isn't in the shared catalog (single-repo use).
    id("com.google.dagger.hilt.android") version "2.56.1" apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.triplet.play) apply false
    alias(libs.plugins.detekt) apply false
}
true // Needed to make the Suppress annotation work for the plugins block

//plugins {
//    id 'com.android.application' version '8.0.0-rc01' apply false
//    id 'com.android.library' version '8.0.0-rc01' apply false
//    id 'org.jetbrains.kotlin.android' version '1.8.10' apply false
//    id 'com.google.dagger.hilt.android' version '2.45' apply false
//    id 'com.mikepenz.aboutlibraries.plugin' version "$latestAboutLibsRelease" apply false
//}
