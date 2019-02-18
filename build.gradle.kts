group = "tanvd.kosogor"

plugins {
    idea apply true
    kotlin("jvm") version "1.3.21" apply true
    `maven-publish` apply true
    `java-gradle-plugin` apply true
    id("com.jfrog.artifactory") version "4.7.5" apply true
}
repositories {
    jcenter()
}

idea {
    module {
        inheritOutputDirs = true
        excludeDirs = setOf(
                file(".gradle"), file(".gradle-cache"), file("gradle"), file("gradlew"), file("gradlew.bat"),
                file("gradle.properties"), file(".idea"), file("out"), file("build"), file("tmp")
        )
    }
}

allprojects {
    buildDir = File(rootProject.projectDir, "out/${project.name}")
}

subprojects {
    apply(plugin = "kotlin")
    apply(plugin = "idea")
    apply(plugin = "java-gradle-plugin")
    apply(plugin = "maven-publish")
    apply(plugin = "com.jfrog.artifactory")

//    (tasks.getByName("compileKotlin") as KotlinCompile).let {
//        it.kotlinOptions {
//            languageVersion = "1.3"
//            apiVersion = "1.3"
//        }
//    }

    repositories {
        jcenter()
    }
}

tasks.withType(Wrapper::class) {
    gradleVersion = "5.1.1"
}
