import tanvd.kosogor.proxy.publishJar
import tanvd.kosogor.proxy.publishPlugin

group = "tanvd.kosogor.terraform"
version = "1.0.11-SNAPSHOT"

dependencies {
    compileOnly(gradleKotlinDsl())
    compileOnly(gradleApi())

    compile("org.codehaus.plexus", "plexus-utils", "3.1.1")
    compile("org.codehaus.plexus", "plexus-archiver", "4.1.0")
    compile("org.codehaus.plexus", "plexus-container-default", "1.0-alpha-30")
    compile("com.github.kittinunf.fuel", "fuel", "2.0.1")

    compile("com.beust", "klaxon", "5.0.1")
    implementation("org.apache.maven:maven-artifact:3.8.1")
}

publishPlugin {
    id = "tanvd.kosogor.terraform"
    displayName = "kosogor-terraform"
    implementationClass = "tanvd.kosogor.terraform.KosogorTerraformPlugin"
    version = project.version.toString()

    info {
        website = "https://github.com/TanVD/kosogor"
        vcsUrl = "https://github.com/TanVD/kosogor"
        description = "Support of Terraform in Gradle — from modules publish to deployment"
        tags.addAll(listOf("terraform", "kotlin", "kotlin-dsl"))
    }
}

publishJar {
    publication {
        artifactId = "tanvd.kosogor.terraform.gradle.plugin"
    }

    bintray {
        username = "tanvd"
        repository = "tanvd.kosogor"
        info {
            description = "kosogor-terraform plugin artifact"
            githubRepo = "https://github.com/TanVD/kosogor"
            vcsUrl = "https://github.com/TanVD/kosogor"
            labels.addAll(listOf("gradle", "kotlin", "kotlin-dsl", "plugin", "terraform"))
        }
    }
}

