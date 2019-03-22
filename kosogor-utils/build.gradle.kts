import tanvd.kosogor.proxy.publishJar

group = "tanvd.kosogor"
version = "1.0.0"

dependencies {
    compile(gradleKotlinDsl())
    compile(gradleApi())
}

publishJar {
    publication {
        artifactId = "kosogor-utils"
    }

    bintray {
        username = "tanvd"
        repository = "tanvd.kosogor"
        info {
            description = "Kosogor Gradle utils"
            githubRepo = "https://github.com/TanVD/kosogor"
            vcsUrl = "https://github.com/TanVD/kosogor"
            labels.addAll(listOf("gradle", "utils", "kotlin", "kotlin-dsl"))
        }
    }
}
