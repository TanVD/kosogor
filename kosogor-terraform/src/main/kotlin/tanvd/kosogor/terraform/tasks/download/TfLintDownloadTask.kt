package tanvd.kosogor.terraform.tasks.download

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.*
import tanvd.kosogor.terraform.terraformDsl
import tanvd.kosogor.terraform.utils.*
import tanvd.kosogor.terraform.utils.CommandLine.os
import java.io.File
import java.net.URL

@CacheableTask
open class TfLintDownloadTask : DefaultTask() {
    @get:Input
    val version: String
        get() = terraformDsl.config.tfLintVersion

    @get:OutputFile
    val file: File
        get() = GlobalFile.tfLintBin

    @TaskAction
    fun download() {
        logger.lifecycle("Downloading TfLint version $version for OS $os")

        Downloads.download(URL("https://github.com/wata727/tflint/releases/download/v$version/tflint_$os.zip"), file.parentFile, Archiver.ZIP)

        CommandLine.execute("chmod", listOf("+x", file.absolutePath), file.parentFile, false)

        logger.lifecycle("TfLint version $version for OS $os successfully downloaded")
    }
}
