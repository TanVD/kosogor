import org.gradle.jvm.tasks.Jar
import com.gradle.publish.PublishTask
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.dsl.KotlinVersion


group = "tanvd.kosogor"

plugins {
    id("tanvd.kosogor") version "1.0.22" apply false
    kotlin("jvm") version "2.1.0" apply true
}

repositories {
    mavenCentral()
}

// kotlin plugin exposes Jar tasks but root project does not have any sources
tasks.withType<Jar>().configureEach {
    enabled = false
}

subprojects {
    apply(plugin = "kotlin")
    apply(plugin = "tanvd.kosogor")
    apply(plugin = "com.gradle.plugin-publish")
    
    java {
        toolchain {
            languageVersion.set(JavaLanguageVersion.of("17"))
        }
    }

    kotlin {
        jvmToolchain(17)
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_17)
            apiVersion.set(KotlinVersion.KOTLIN_2_1)
            languageVersion.set(KotlinVersion.KOTLIN_2_1)
            // https://jakewharton.com/kotlins-jdk-release-compatibility-flag/
            // https://youtrack.jetbrains.com/issue/KT-49746/Support-Xjdk-release-in-gradle-toolchain#focus=Comments-27-8935065.0-0
            freeCompilerArgs.addAll("-Xjdk-release=17")
        }
    }

    repositories {
        mavenCentral()
        gradlePluginPortal()
    }

    afterEvaluate {
        if (version.toString().contains("SNAPSHOT")) {
            tasks.withType(PublishTask::class) {
                enabled = false
            }
        }

        System.setProperty("gradle.publish.key", System.getenv("gradle_publish_key") ?: "")
        System.setProperty("gradle.publish.secret", System.getenv("gradle_publish_secret") ?: "")
    }
}

