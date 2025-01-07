import tanvd.kosogor.proxy.publishPlugin

group = "tanvd.kosogor.terraform"
version = "1.0.18"

dependencies {
    compileOnly(gradleKotlinDsl())
    compileOnly(gradleApi())

    implementation("org.codehaus.plexus", "plexus-utils", "4.0.2")
    implementation("org.codehaus.plexus", "plexus-archiver", "4.10.0")
    implementation("org.codehaus.plexus", "plexus-container-default", "2.1.1")
    implementation("com.github.kittinunf.fuel", "fuel", "2.3.1")

    implementation("com.beust", "klaxon", "5.6")
    implementation("org.apache.maven", "maven-artifact", "3.9.9")
}

publishPlugin {
    id = "tanvd.kosogor.terraform"
    displayName = "kosogor-terraform"
    implementationClass = "tanvd.kosogor.terraform.KosogorTerraformPlugin"

    info {
        website = "https://github.com/TanVD/kosogor"
        vcsUrl = "https://github.com/TanVD/kosogor"
        description = "Support of Terraform in Gradle — from modules publish to deployment"
        tags.addAll(listOf("terraform", "kotlin", "kotlin-dsl"))
    }
}
