// Top-level build file where you can add configuration options common to all sub-projects/modules.

buildscript {
    repositories {
        google()
        jcenter()
    }
    dependencies {
        classpath (Config.BuildPlugins.android_plugin)
        classpath (Config.BuildPlugins.kotlin_plugin)
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
