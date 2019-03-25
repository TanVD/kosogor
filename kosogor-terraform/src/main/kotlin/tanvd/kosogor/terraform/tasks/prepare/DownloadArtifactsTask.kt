package tanvd.kosogor.terraform.tasks.prepare

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.*
import tanvd.kosogor.terraform.TerraformDsl
import tanvd.kosogor.terraform.terraformDsl
import tanvd.kosogor.terraform.utils.Downloads
import java.io.File
import java.net.URL

open class DownloadArtifactsTask : DefaultTask() {
    val toDownload: LinkedHashSet<TerraformDsl.Artifacts.Remote>
        get() = terraformDsl.artifacts.remotes

    @get:Input
    val urls: LinkedHashSet<URL>
        get() = LinkedHashSet(toDownload.map { it.url })

    @get:OutputDirectories
    val files: LinkedHashSet<File>
        get() = LinkedHashSet(toDownload.map { it.destDir })


    @TaskAction
    fun download() {
        for ((url, file, archiver, filter) in toDownload) {
            logger.lifecycle("Downloading remote artifact from URL ${url.toExternalForm()}")
            if (archiver == null) {
                Downloads.download(url, file)
            } else {
                Downloads.download(url, file, archiver, filter)
            }
            logger.lifecycle("Remote artifact for URL ${url.toExternalForm()} successfully downloaded")
        }
    }
}
