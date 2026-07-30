pluginManagement {
    includeBuild("../release-tools")
    repositories {
        gradlePluginPortal()
        google()
        mavenCentral()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        maven("https://jitpack.io")
    }
    versionCatalogs {
        create("sharedLibs") {
            from(files("../release-tools/gradle/libs.versions.toml"))
        }
    }
}
rootProject.name = "Sudoku"
include(":app")