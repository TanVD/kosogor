import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

group = "tanvd.kosogor"

plugins {
    idea apply true
    kotlin("jvm") version "1.3.21" apply true
    id("com.jfrog.artifactory") version "4.7.5"
}
repositories {
    jcenter()
}

idea {
    module {
        inheritOutputDirs = true
        isDownloadSources = true
        isDownloadJavadoc = true
        excludeDirs = files(".gradle", ".gradle-cache", "gradle", "gradlew", "gradlew.bat", "gradle.properties",
                ".idea",
                "out", "build", "tmp"
        ).toHashSet()
    }
}

allprojects {
    buildDir = File(rootProject.projectDir, "build/${project.name}")
}

subprojects {
    apply(plugin = "kotlin")
    apply(plugin = "idea")
    apply(plugin = "java-gradle-plugin")
    apply(plugin = "maven-publish")
    apply(plugin = "com.jfrog.artifactory")

    (tasks.getByName("compileKotlin") as KotlinCompile).let {
        it.kotlinOptions {
            languageVersion = "1.3"
            apiVersion = "1.3"
        }
    }

    repositories {
        jcenter()
    }
}

tasks.withType(Wrapper::class) {
    gradleVersion = "5.1.1"
}
