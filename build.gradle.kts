// Top-level build file where you can add configuration options common to all sub-projects/modules.

buildscript {
    repositories {
        google()
        jcenter()

        maven("https://maven.fabric.io/public")
    }
    dependencies {
        classpath(Config.BuildPlugins.android_plugin)
        classpath(Config.BuildPlugins.kotlin_plugin)
        classpath(Config.BuildPlugins.google_services_plugin)
        classpath(Config.BuildPlugins.fabric_plugin)
    }
}

allprojects {
    repositories {
        google()
        jcenter()

        maven("https://oss.sonatype.org/content/repositories/snapshots/")
        maven("https://jitpack.io")
    }
}

task("clean") {
    delete(rootProject.buildDir)
}

task("getVersion") {
    doLast {
        println(Config.Android.versionName)
    }
}
