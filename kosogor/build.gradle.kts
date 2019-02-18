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

afterEvaluate {
    System.setProperty("gradle.publish.key", System.getenv("gradle_publish_key") ?: "")
    System.setProperty("gradle.publish.secret", System.getenv("gradle_publish_secret") ?: "")
}

pluginBundle {
    website = "https://github.com/TanVD/kosogor"
    vcsUrl = "https://github.com/TanVD/kosogor"

    (plugins) {
        "kosogor-plugin" {
            displayName = "kosogor"
            description = "Reasonable defaults and simplified Kotlin-DSL interfaces for everyday development"
            tags = listOf("default", "common", "kotlin", "jar", "shadowjar", "artifactory", "idea")
            version = project.version.toString()
        }
    }
}

