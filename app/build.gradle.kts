import Config.Android
import Config.Libs
import org.gradle.kotlin.dsl.implementation

plugins {
    id("com.android.application")
    kotlin("android")
    kotlin("android.extensions")
    kotlin("kapt")
}

apply { from("experimentalExtensions.gradle") }

android {
    compileSdkVersion(Android.compileSdk)
    buildToolsVersion(Android.build_tools_version)

    defaultConfig {
        minSdkVersion(Android.minSdk)
        targetSdkVersion(Android.targetSdk)

        applicationId = Android.applicationId
        versionCode = Android.versionCode
        versionName = Android.versionName

        testInstrumentationRunner = "android.support.test.runner.AndroidJUnitRunner"
        vectorDrawables.useSupportLibrary = true
    }

    buildTypes {
        getByName("debug") {
            versionNameSuffix = "-dev"
            applicationIdSuffix = ".dev"
            isMinifyEnabled = false
            isZipAlignEnabled = false
        }

        getByName("release") {
            isMinifyEnabled = false
            proguardFiles("proguard-rules.pro")
        }
    }

    packagingOptions {
        exclude("META-INF/rxjava.properties")
    }

    lintOptions {
        isCheckReleaseBuilds = false
        isAbortOnError = false
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    sourceSets {
        getByName("debug").java.srcDirs("src/debug/java")
        getByName("release").java.srcDirs("src/release/java")
    }
}

dependencies {
    implementation(Libs.kotlin_stdlib)

    implementation(Libs.support_annotations)
    implementation(Libs.androidx_legacy)
    implementation(Libs.androidx_appcompat)
    implementation(Libs.androidx_core_ktx)
    implementation(Libs.androidx_constraintlayout)
    implementation(Libs.androidx_navigation_fragment)
    implementation(Libs.androidx_navigation_ui)

    implementation(Libs.androidx_room_runtime)
    implementation(Libs.androidx_room_rxjava)
    kapt(Libs.androidx_room_compiler)

    implementation(Libs.timber)

    implementation(Libs.rxjava)
    implementation(Libs.rxkotlin)
    implementation(Libs.rxrelay)
    implementation(Libs.rxbinding)
    implementation(Libs.rxbinding_viewpager)
    implementation(Libs.rxbinding_material)

    implementation(Libs.koinAndroid)
    implementation(Libs.koinAndroidViewModel)

    implementation(Libs.threetenabp)

    implementation(Libs.rxpm)

    implementation(Libs.randomColor)
    implementation(Libs.MPAndroidChart)

    testImplementation(Libs.junit)
    androidTestImplementation(Libs.room_test)
    androidTestImplementation(Libs.androidx_arch_core_test)
    androidTestImplementation(Libs.junit_runner)
    androidTestImplementation(Libs.espresso_core)
}