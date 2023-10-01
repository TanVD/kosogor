import tanvd.kosogor.proxy.publishJar
import tanvd.kosogor.proxy.publishPlugin

group = "tanvd.kosogor.zkm"
version = "1.0.10-SNAPSHOT"

dependencies {
    compileOnly(gradleKotlinDsl())
    compileOnly(gradleApi())
}

publishJar {
    publication {
        artifactId = "tanvd.kosogor.zkm.gradle.plugin"
    }
}

publishPlugin {
    id = "tanvd.kosogor.zkm"
    displayName = "kosogor-zkm"
    implementationClass = "tanvd.kosogor.zkm.KosogorZkmPlugin"

    info {
        website = "https://github.com/TanVD/kosogor"
        vcsUrl = "https://github.com/TanVD/kosogor"
        description = "ZKM wrapper task for Gradle"
        tags.addAll(listOf("zkm", "obfuscate", "kotlin"))
    }
}

