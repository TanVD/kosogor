import tanvd.kosogor.proxy.publishPlugin

group = "tanvd.kosogor"
version = "1.0.22-SNAPSHOT"

dependencies {
    compileOnly(gradleKotlinDsl())
    compileOnly(gradleApi())

    api("org.jfrog.buildinfo", "build-info-extractor-gradle", "5.1.11")
    api("com.github.johnrengelman", "shadow", "8.1.1")
    api("com.gradle.publish", "plugin-publish-plugin", "1.2.1")
}

publishPlugin {
    id = "tanvd.kosogor"
    displayName = "kosogor"
    implementationClass = "tanvd.kosogor.KosogorPlugin"

    info {
        website = "https://github.com/TanVD/kosogor"
        vcsUrl = "https://github.com/TanVD/kosogor"
        description = "Reasonable defaults and simplified Kotlin-DSL interfaces for everyday development"
        tags.addAll(listOf("default", "common", "kotlin", "jar", "shadowjar", "artifactory", "idea"))
    }
}
