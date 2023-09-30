import tanvd.kosogor.proxy.publishJar
import tanvd.kosogor.proxy.publishPlugin

group = "tanvd.kosogor"
version = "1.0.19"

dependencies {
    compileOnly(gradleKotlinDsl())
    compileOnly(gradleApi())

    api("org.jfrog.buildinfo", "build-info-extractor-gradle", "5.1.9")
    api("gradle.plugin.com.github.johnrengelman", "shadow", "7.1.2")
    api("com.gradle.publish", "plugin-publish-plugin", "1.2.1")
}

publishJar {
    publication {
        artifactId = "tanvd.kosogor.gradle.plugin"
    }
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
