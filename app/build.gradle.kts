import Config.Android
import Config.Libs
import org.gradle.kotlin.dsl.implementation

plugins {
    id("com.android.application")
    id("io.fabric")
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

    signingConfigs {
        create("release") {
            if (System.getenv("CI") == "true") {
                storeFile = file("../ltimer.jks")
                storePassword = System.getenv("keystore_password")
                keyAlias = System.getenv("keystore_alias")
                keyPassword = System.getenv("keystore_alias_password")
            }
        }
    }

    buildTypes {
        getByName("debug") {
            isMinifyEnabled = false
            isZipAlignEnabled = false
            manifestPlaceholders = mapOf("enableCrashReporting" to "false")
        }

        getByName("release") {
            isMinifyEnabled = false
            proguardFiles("proguard-rules.pro")
            manifestPlaceholders = mapOf("enableCrashReporting" to "true")
            signingConfig = signingConfigs.getByName("release")
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

    implementation(Libs.firebase_core)
    implementation(Libs.firebase_crashlitics)

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

    implementation(Libs.koinAndroid)

    implementation(Libs.threetenabp)

    implementation(Libs.rxpm)

    implementation(Libs.randomColor)
    implementation(Libs.MPAndroidChart)

    testImplementation(Libs.junit)

    androidTestImplementation(Libs.koin_test)
    androidTestImplementation(Libs.room_test)
    androidTestImplementation(Libs.androidx_arch_core_test)
    androidTestImplementation(Libs.junit_runner)
    androidTestImplementation(Libs.espresso_core)
}

apply(plugin = "com.google.gms.google-services")