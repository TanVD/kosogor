import tanvd.kosogor.proxy.publishPlugin

group = "tanvd.kosogor.terraform"
version = "1.0.12"

dependencies {
    compileOnly(gradleKotlinDsl())
    compileOnly(gradleApi())

    implementation("org.codehaus.plexus", "plexus-utils", "3.1.1")
    implementation("org.codehaus.plexus", "plexus-archiver", "4.1.0")
    implementation("org.codehaus.plexus", "plexus-container-default", "1.0-alpha-30")
    implementation("com.github.kittinunf.fuel", "fuel", "2.0.1")

    implementation("com.beust", "klaxon", "5.0.1")
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
