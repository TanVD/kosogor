group = "tanvd.kosogor"
version = "1.0.1-SNAPSHOT"

dependencies {
    compile(gradleKotlinDsl())
    compile(gradleApi())
    compile("org.jfrog.buildinfo", "build-info-extractor-gradle", "4.7.5")
    compile("com.jfrog.bintray.gradle", "gradle-bintray-plugin", "1.8.4")
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

val sources = task<Jar>("sourcesJar") {
    classifier = "sources"
    from(sourceSets["main"]!!.allSource)
}

publishing {
    publications.create("publishJar", MavenPublication::class) {
        artifactId = "tanvd.kosogor.gradle.plugin"
        from(components.getByName("java"))
        artifact(sources)
    }
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

