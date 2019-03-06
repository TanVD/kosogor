import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

group = "tanvd.kosogor"

plugins {
    id("tanvd.kosogor") version "1.0.1" apply true
    kotlin("jvm") version "1.3.21" apply true
}

repositories {
    jcenter()
}

subprojects {
    apply(plugin = "kotlin")
    apply(plugin = "tanvd.kosogor")

    (tasks.getByName("compileKotlin") as KotlinCompile).let {
        it.kotlinOptions {
            languageVersion = "1.3"
            apiVersion = "1.3"
        }
    }

    repositories {
        jcenter()
        gradlePluginPortal()
    }
}

