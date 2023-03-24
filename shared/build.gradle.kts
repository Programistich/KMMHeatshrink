plugins {
    kotlin("multiplatform")
    kotlin("native.cocoapods")
    id("com.android.library")
    id("com.goncalossilva.resources")
    id("com.chromaticnoise.multiplatform-swiftpackage")
    `maven-publish`
}

val okioVersion = "3.3.0"
val libraryVersion: String = System.getProperty("version", "debug")
val libraryPackage: String = System.getProperty("group", "com.flipperdevices.kmm-heatshrink")

/* required for maven publication */
group = "com.flipperdevices.heatshrink"
version = libraryVersion

kotlin {
    cocoapods {
        version = libraryVersion
        summary = "Kotlin Multiplatform Heatshrink library"
        homepage = ""

        framework {
            baseName = "KMMHeatshrink"
            isStatic = true
        }
    }


    android()
    iosX64()
    iosArm64()
    iosSimulatorArm64()

    sourceSets {
        /* Main source sets */
        val commonMain by getting {
            dependencies {
                implementation("com.squareup.okio:okio:$okioVersion")
            }
        }
        val androidMain by getting
        val iosMain by creating
        val iosX64Main by getting 
        val iosArm64Main by getting
        val iosSimulatorArm64Main by getting

        /* Main hierarchy */
        androidMain.dependsOn(commonMain)
        iosMain.dependsOn(commonMain)
        iosX64Main.dependsOn(iosMain)
        iosArm64Main.dependsOn(iosMain)
        iosSimulatorArm64Main.dependsOn(iosMain)

        /* Test source sets */
        val commonTest by getting {
            dependencies {
                implementation(kotlin("test"))
                implementation("com.goncalossilva:resources:0.3.0")
            }
        }
        val androidUnitTest by getting
        val iosTest by creating
        val iosX64Test by getting 
        val iosArm64Test by getting
        val iosSimulatorArm64Test by getting

        /* Test hierarchy */
        androidUnitTest.dependsOn(commonTest)
        iosTest.dependsOn(commonTest)
        iosX64Test.dependsOn(iosTest)
        iosArm64Test.dependsOn(iosTest)
        iosSimulatorArm64Test.dependsOn(iosTest)
    }
}

android {
    namespace = "com.flipperdevices.heatshrink"
    compileSdk = 31
    defaultConfig {
        minSdk = 21
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

multiplatformSwiftPackage {
    packageName("KMMHeatshrink")
    swiftToolsVersion("5.3")
    targetPlatforms {
        iOS { v("13") }
    }
}

configure<PublishingExtension> {
    publications {
        create<MavenPublication>("maven") {
            groupId = libraryPackage
            version = libraryVersion

            from(components["kotlin"])
        }
    }
}