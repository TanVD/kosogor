import tanvd.kosogor.proxy.publishJar
import tanvd.kosogor.proxy.publishPlugin

group = "tanvd.kosogor"
version = "1.0.5-SNAPSHOT"

dependencies {
    implementation(gradleKotlinDsl())
    implementation(gradleApi())
    api("org.jfrog.buildinfo", "build-info-extractor-gradle", "4.7.5")
    api("com.jfrog.bintray.gradle", "gradle-bintray-plugin", "1.8.4")
    api("com.github.jengelman.gradle.plugins", "shadow", "4.0.4")
    api("com.gradle.publish", "plugin-publish-plugin", "0.10.1")
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

