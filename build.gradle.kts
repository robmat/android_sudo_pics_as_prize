// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(sharedLibs.plugins.android.application) apply false
    alias(sharedLibs.plugins.kotlin.android) apply false
    alias(libs.plugins.ksp) apply false
    alias(libs.plugins.hilt) apply false
    alias(sharedLibs.plugins.android.library) apply false
    alias(sharedLibs.plugins.kotlin.compose) apply false
    alias(sharedLibs.plugins.triplet.play) apply false
    alias(sharedLibs.plugins.detekt) apply false
}
true // Needed to make the Suppress annotation work for the plugins block

//plugins {
//    id 'com.android.application' version '8.0.0-rc01' apply false
//    id 'com.android.library' version '8.0.0-rc01' apply false
//    id 'org.jetbrains.kotlin.android' version '1.8.10' apply false
//    id 'com.google.dagger.hilt.android' version '2.45' apply false
//    id 'com.mikepenz.aboutlibraries.plugin' version "$latestAboutLibsRelease" apply false
//}
