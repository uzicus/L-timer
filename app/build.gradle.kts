import Config.Android
import Config.Libs

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
    }

    buildTypes {
        getByName("debug") {
            isMinifyEnabled = false
            isZipAlignEnabled = false
        }

        getByName("release") {
            isMinifyEnabled = false
            proguardFiles("proguard-rules.pro")
        }
    }

    flavorDimensions("screenMode")

    productFlavors {
        create("dev") {
            setDimension("screenMode")
            versionNameSuffix = "-dev"
            applicationIdSuffix = ".dev"
        }

        create("prod") {
            setDimension("screenMode")
            versionNameSuffix = "-dev"
            applicationIdSuffix = ".dev"
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
}

dependencies {
    implementation(Libs.kotlin_stdlib)

    implementation(Libs.support_annotations)
    implementation(Libs.androidx_legacy)
    implementation(Libs.androidx_appcompat)
    implementation(Libs.androidx_core_ktx)
    implementation(Libs.androidx_constraintlayout)

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

    implementation(Libs.koin)

    implementation(Libs.conductor)
    implementation(Libs.conductor_support)

    implementation(Libs.rxpm)

    implementation(Libs.threetenabp)

    testImplementation(Config.Libs.junit)
    androidTestImplementation(Config.Libs.room_test)
    androidTestImplementation(Config.Libs.androidx_arch_core_test)
    androidTestImplementation(Config.Libs.junit_runner)
    androidTestImplementation(Config.Libs.espresso_core)
}