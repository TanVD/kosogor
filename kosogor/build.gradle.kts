group = "tanvd.kosogor"
version = "2019.1.0-SNAPSHOT"

dependencies {
    compile(gradleKotlinDsl())
    compile(gradleApi())
    compile("org.jfrog.buildinfo", "build-info-extractor-gradle", "4.7.5")
    compile("com.github.jengelman.gradle.plugins", "shadow", "4.0.4")
}

gradlePlugin {
    plugins {
        create("kosogor-plugin") {
            id = "tanvd.kosogor"
            implementationClass = "tanvd.kosogor.KosogorPlugin"
            version = project.version
        }
    }
}

pluginBundle {
    website = "https://github.com/TanVD/kosogor"
    vcsUrl = "https://github.com/TanVD/kosogor"

    (plugins) {
        "kosogor-plugin" {
            description = "Reasonable defaults and simplified Kotlin-DSL interfaces for everyday development"
            tags = listOf("default", "common", "kotlin", "jar", "shadowjar", "artifactory", "idea")
            version = project.version.toString()
        }
    }
}

