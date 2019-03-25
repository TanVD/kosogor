package tanvd.kosogor.terraform.tasks.download

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.*
import tanvd.kosogor.terraform.terraformDsl
import tanvd.kosogor.terraform.utils.*
import tanvd.kosogor.terraform.utils.CommandLine.os
import java.io.File
import java.net.URL

open class TerraformDownloadTask : DefaultTask() {
    @get:Input
    val version: String
        get() = terraformDsl.config.tfVersion

    @get:OutputFile
    val file: File
        get() = GlobalFile.tfBin

    @TaskAction
    fun download() {
        logger.lifecycle("Downloading terraform version $version for OS $os")

        Downloads.download(URL("https://releases.hashicorp.com/terraform/$version/terraform_${version}_$os.zip"), file.parentFile, Archiver.ZIP)

        CommandLine.execute("chmod", listOf("+x", file.absolutePath), file.parentFile, false)

        logger.lifecycle("Terraform version $version for OS $os successfully downloaded")
    }
}
