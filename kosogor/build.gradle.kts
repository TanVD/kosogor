group = "tanvd.kosogor"
version = "2019.1.0-SNAPSHOT"

dependencies {
    compile("org.jfrog.buildinfo", "build-info-extractor-gradle", "4.7.5")
    compile(gradleKotlinDsl())
    compile("com.github.jengelman.gradle.plugins", "shadow", "4.0.4")
    compile(gradleApi())
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