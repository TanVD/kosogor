import tanvd.kosogor.proxy.publishJar
import tanvd.kosogor.proxy.publishPlugin

group = "tanvd.kosogor.zkm"
version = "1.0.11"

dependencies {
    compileOnly(gradleKotlinDsl())
    compileOnly(gradleApi())
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

