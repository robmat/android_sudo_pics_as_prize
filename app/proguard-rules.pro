# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Uncomment this to preserve the line number information for
# debugging stack traces.
#-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile

-dontobfuscate

-dontwarn javax.annotation.processing.AbstractProcessor
-dontwarn javax.annotation.processing.SupportedOptions
-dontwarn android.content.res.**

# Room's own consumer rule (-keep class * extends androidx.room.RoomDatabase, shipped in
# room-runtime's proguard.txt) keeps the class but not its no-arg constructor. WorkManager's
# internal WorkDatabase is only ever instantiated reflectively (androidx.room.util.DBUtil.
# createWorkManager -> Class.getDeclaredConstructor().newInstance()), since it's a precompiled
# library class with no app-module-generated call site R8 can see - so without this, R8 strips
# the constructor as apparently unused and WorkManager crashes on startup with
# "NoSuchMethodException: androidx.work.impl.WorkDatabase_Impl.<init> []" (confirmed on
# sgtpuzzles/samegame, same play-services-ads + androidx.work.runtime override).
-keepclassmembers class * extends androidx.room.RoomDatabase {
    public <init>();
}