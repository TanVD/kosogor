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

val sources = task<Jar>("sourcesJar") {
    archiveClassifier.set("sources")
    from(sourceSets["main"]!!.allSource)
}

publishing {
    publications.create("publishJar", MavenPublication::class) {
        artifactId = "tanvd.kosogor.gradle.plugin"
        from(components.getByName("java"))
        artifact(sources)
    }
}
