# Keeps that only the instrumentation harness needs. Applied to the releaseTest build
# type alone, so the release APK users get stays fully shrunk and obfuscated.
#
# The androidTest APK is not minified and carries no Kotlin stdlib or androidx runtime of
# its own - it loads those out of the app APK. R8 legitimately strips whatever the app
# itself never calls, and the instrumentation then dies with ClassNotFoundException
# before a single test runs (seen: androidx.tracing.Trace, kotlin.LazyKt).

-keep class androidx.tracing.** { *; }
-keep class kotlin.** { *; }
-keep class kotlinx.** { *; }
-keep class androidx.test.** { *; }
-dontwarn androidx.test.**

# Espresso drives the app's own classes by name - Kotlin `object` singletons especially,
# whose INSTANCE field R8 renames and then optimises away entirely, so every test dies in
# setUp with "NoSuchFieldError: No field INSTANCE of type Lf10;". Keeping app code
# addressable here costs nothing in the shipped APK, and library code is still fully
# minified in this variant - so reflective-access regressions like the WorkManager one
# still surface exactly as they would in release.
-keep class com.batodev.sudoku.** { *; }
