import tanvd.kosogor.proxy.publishJar
import tanvd.kosogor.proxy.publishPlugin

group = "tanvd.kosogor"
version = "1.0.11"

dependencies {
    compileOnly(gradleKotlinDsl())
    compileOnly(gradleApi())

    api("org.jfrog.buildinfo", "build-info-extractor-gradle", "4.15.1")
    api("com.jfrog.bintray.gradle", "gradle-bintray-plugin", "1.8.5")
    api("com.github.jengelman.gradle.plugins", "shadow", "5.2.0")
    api("com.gradle.publish", "plugin-publish-plugin", "0.12.0")
}

publishPlugin {
    id = "tanvd.kosogor"
    displayName = "kosogor"
    implementationClass = "tanvd.kosogor.KosogorPlugin"
    version = project.version.toString()

    info {
        website = "https://github.com/TanVD/kosogor"
        vcsUrl = "https://github.com/TanVD/kosogor"
        description = "Reasonable defaults and simplified Kotlin-DSL interfaces for everyday development"
        tags.addAll(listOf("default", "common", "kotlin", "jar", "shadowjar", "artifactory", "idea"))
    }
}

publishJar {
    publication {
        artifactId = "tanvd.kosogor.gradle.plugin"
    }

    bintray {
        username = "tanvd"
        repository = "tanvd.kosogor"
        info {
            description = "Kosogor plugin artifact"
            githubRepo = "https://github.com/TanVD/kosogor"
            vcsUrl = "https://github.com/TanVD/kosogor"
            labels.addAll(listOf("gradle", "kotlin", "kotlin-dsl", "plugin", "defaults", "common"))
        }
    }
}

