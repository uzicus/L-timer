
object Config {

    object Versions {
        val kotlin_version = "1.3.11"
        val android_plugin_version = "3.4.0-alpha07"

        val support_lib = "28.0.0"
        val androidx_appcompat = "1.0.2"
        val androidx_core = "1.0.1"
        val androidx_legacy = "1.0.0"
        val androidx_constraintlayout = "2.0.0-alpha2"
        val androidx_room = "2.1.0-alpha02"
        val androidx_arch = "2.0.0"
        val androidx_navigation = "1.0.0-alpha08"

        val google_material = "1.0.0"

        val threetenabp = "1.1.1"

        val timber = "4.7.1"

        val rxjava = "2.2.0"
        val rxkotlin = "2.3.0"
        val rxrelay = "2.1.0"
        val rxbinding = "3.0.0-alpha1"

        val koin = "1.0.2"

        val livedata_ktx = "2.0.1"

        val MPAndroidChart = "v3.1.0-alpha"

        val junit = "4.12"
        val junit_runner = "1.0.2"
        val espresso_core = "3.0.2"
    }

    object BuildPlugins {
        val android_plugin = "com.android.tools.build:gradle:${Versions.android_plugin_version}"
        val kotlin_plugin = "org.jetbrains.kotlin:kotlin-gradle-plugin:${Versions.kotlin_version}"
    }

    object Android {
        val applicationId = "com.tkachenkod.l_timer"
        val build_tools_version = "28.0.3"
        val compileSdk = 28
        val minSdk = 21
        val targetSdk = 28
        val versionName = "0.1-alpha1"
        val versionCode = System.getenv("TRAVIS_BUILD_NUMBER")?.toInt() ?: 1
    }

    object Libs {
        val kotlin_stdlib = "org.jetbrains.kotlin:kotlin-stdlib-jdk7:${Versions.kotlin_version}"

        val support_annotations = "com.android.support:support-annotations:${Versions.support_lib}"
        val androidx_appcompat = "androidx.appcompat:appcompat:${Versions.androidx_appcompat}"
        val androidx_core_ktx = "androidx.core:core-ktx:${Versions.androidx_core}"
        val androidx_legacy = "androidx.legacy:legacy-support-v4:${Versions.androidx_legacy}"
        val androidx_constraintlayout = "androidx.constraintlayout:constraintlayout:${Versions.androidx_constraintlayout}"
        val androidx_navigation_fragment = "android.arch.navigation:navigation-fragment-ktx:${Versions.androidx_navigation}"
        val androidx_navigation_ui = "android.arch.navigation:navigation-ui-ktx:${Versions.androidx_navigation}"
        val androidx_lifecycle_extensions = "androidx.lifecycle:lifecycle-extensions:${Versions.androidx_arch}"
        val androidx_lifecycle_compiler = "androidx.lifecycle:lifecycle-compiler:${Versions.androidx_arch}"
        val google_material = "com.google.android.material:material:${Versions.google_material}"

        val androidx_room_runtime = "androidx.room:room-runtime:${Versions.androidx_room}"
        val androidx_room_rxjava = "androidx.room:room-rxjava2:${Versions.androidx_room}"
        val androidx_room_compiler = "androidx.room:room-compiler:${Versions.androidx_room}"

        val timber = "com.jakewharton.timber:timber:${Versions.timber}"

        //RX
        val rxjava = "io.reactivex.rxjava2:rxjava:${Versions.rxjava}"
        val rxkotlin = "io.reactivex.rxjava2:rxkotlin:${Versions.rxkotlin}"
        val rxrelay = "com.jakewharton.rxrelay2:rxrelay:${Versions.rxrelay}"
        val rxbinding = "com.jakewharton.rxbinding3:rxbinding:${Versions.rxbinding}"
        val rxbinding_viewpager = "com.jakewharton.rxbinding3:rxbinding-viewpager:${Versions.rxbinding}"
        val rxbinding_material = "com.jakewharton.rxbinding3:rxbinding-material:${Versions.rxbinding}"

        //DI
        val koinAndroid = "org.koin:koin-android:${Versions.koin}"
        val koinAndroidViewModel = "org.koin:koin-android-viewmodel:${Versions.koin}"

        val threetenabp = "com.jakewharton.threetenabp:threetenabp:${Versions.threetenabp}"

        val livedata_ktx = "com.shopify:livedata-ktx:${Versions.livedata_ktx}"

        // Chart
        val MPAndroidChart = "com.github.PhilJay:MPAndroidChart:${Versions.MPAndroidChart}"

        // Test
        val koin_test = "org.koin:koin-test:${Versions.koin}"
        val room_test = "androidx.room:room-testing:${Versions.androidx_room}"
        val androidx_arch_core_test = "androidx.arch.core:core-testing:${Versions.androidx_arch}"
        val junit = "junit:junit:${Versions.junit}"
        val junit_runner = "com.android.support.test:runner:${Versions.junit_runner}"
        val espresso_core = "com.android.support.test.espresso:espresso-core:${Versions.espresso_core}"
    }
}