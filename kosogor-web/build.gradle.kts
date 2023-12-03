import tanvd.kosogor.proxy.publishJar
import tanvd.kosogor.proxy.publishPlugin

group = "tanvd.kosogor.web"
version = "1.0.16"

dependencies {
    compileOnly(gradleKotlinDsl())
    compileOnly(gradleApi())
}

publishJar {
    enableSources = false
    publication {
        artifactId = "tanvd.kosogor.web.gradle.plugin"
    }
}

publishPlugin {
    id = "tanvd.kosogor.web"
    displayName = "kosogor-web"
    implementationClass = "tanvd.kosogor.web.KosogorWebPlugin"

    info {
        website = "https://github.com/TanVD/kosogor"
        vcsUrl = "https://github.com/TanVD/kosogor"
        description = "Tasks and facades simplifying Web development -- from War preparation to Js minification"
        tags.addAll(listOf("web", "js", "tomcat", "server", "dependencies", "kotlin-dsl", "kotlin"))
    }
}
