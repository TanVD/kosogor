import com.gradle.publish.PublishTask
import org.jetbrains.kotlin.gradle.tasks.KotlinJvmCompile


group = "tanvd.kosogor"

plugins {
    id("tanvd.kosogor") version "1.0.16" apply true
    id("io.gitlab.arturbosch.detekt") version "1.20.0" apply true
    kotlin("jvm") version "1.7.20" apply true
}

repositories {
    mavenCentral()
}

subprojects {
    apply(plugin = "kotlin")
    apply(plugin = "tanvd.kosogor")
    apply(plugin = "io.gitlab.arturbosch.detekt")

    tasks.withType(JavaCompile::class) {
        targetCompatibility = "1.8"
    }

    tasks.withType(KotlinJvmCompile::class) {
        kotlinOptions {
            languageVersion = "1.7"
            apiVersion = "1.7"
            jvmTarget = "1.8"
        }
    }

    repositories {
        mavenCentral()
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
            tasks.withType(PublishTask::class) {
                enabled = false
            }
        }

        System.setProperty("gradle.publish.key", System.getenv("gradle_publish_key") ?: "")
        System.setProperty("gradle.publish.secret", System.getenv("gradle_publish_secret") ?: "")
    }
}

