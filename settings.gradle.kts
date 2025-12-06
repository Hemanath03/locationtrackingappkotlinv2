pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google() // This hosts the Places SDK
        mavenCentral()
    }
}

rootProject.name = "locationtrackingappv2"
include(":app")
