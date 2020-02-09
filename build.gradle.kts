import com.gradle.publish.PublishTask
import com.jfrog.bintray.gradle.tasks.BintrayUploadTask
import org.jetbrains.kotlin.gradle.dsl.KotlinJvmCompile

group = "tanvd.kosogor"

plugins {
    id("tanvd.kosogor") version "1.0.5" apply true
    id("io.gitlab.arturbosch.detekt").version("1.5.1") apply true
    kotlin("jvm") version "1.3.31" apply true
}

repositories {
    jcenter()
}

subprojects {
    apply(plugin = "kotlin")
    apply(plugin = "tanvd.kosogor")
    apply(plugin = "io.gitlab.arturbosch.detekt")

    tasks.withType(KotlinJvmCompile::class) {
        kotlinOptions {
            languageVersion = "1.3"
            apiVersion = "1.3"
            jvmTarget = "1.8"
        }
    }

    repositories {
        jcenter()
        gradlePluginPortal()
    }

    detekt {
        parallel = true
        failFast = false
        config = files(File(project.rootProject.projectDir, "buildScripts/detekt/detekt.yml"))
        reports {
            xml {
                enabled = false
            }
            html {
                enabled = false
            }
        }
    }

    afterEvaluate {
        if (version.toString().contains("SNAPSHOT")) {
            tasks.withType(BintrayUploadTask::class) {
                enabled = false
            }
            tasks.withType(PublishTask::class) {
                enabled = false
            }
        }

        System.setProperty("gradle.publish.key", System.getenv("gradle_publish_key") ?: "")
        System.setProperty("gradle.publish.secret", System.getenv("gradle_publish_secret") ?: "")
    }
}

