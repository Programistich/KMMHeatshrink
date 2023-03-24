rootProject.name = "KMMHeatshrink"
include(":shared")

pluginManagement {
    repositories {
        google()
        gradlePluginPortal()
        mavenCentral()
    }
    plugins {
        kotlin("multiplatform").version("1.8.10")
        kotlin("native.cocoapods").version("1.8.10")
        id("com.android.library").version("7.4.2")
        id("com.goncalossilva.resources").version("0.3.0")
        id("com.chromaticnoise.multiplatform-swiftpackage").version("2.0.3")
    }
}

dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
    }
    versionCatalogs {
        create("libs") {
        }
    }
}
