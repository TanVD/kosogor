package tanvd.kosogor.terraform.utils

import org.codehaus.plexus.util.FileUtils
import tanvd.kosogor.terraform.utils.Archiver
import java.io.File
import java.io.FileOutputStream
import java.net.URL
import java.nio.channels.Channels
import java.nio.file.Files

object Downloads {
    fun download(url: URL, toFile: File) {
        toFile.parentFile.mkdirs()
        FileOutputStream(toFile).channel.transferFrom(Channels.newChannel(url.openStream()), 0, java.lang.Long.MAX_VALUE)
    }

    fun download(url: URL, toFile: File, archiver: Archiver) {
        toFile.parentFile.mkdirs()
        val archive = File(toFile.absolutePath + "." + archiver.extension)

        download(url, archive)

        archiver.unarchive(archive, toFile)

        archive.delete()
    }

    fun download(url: URL, toFile: File, archiver: Archiver, filterToRoot: (File) -> Boolean) {
        val tmpDir = File(toFile, "tmp_${RandomCode.next()}")
        Downloads.download(url, tmpDir, archiver)
        Files.walk(tmpDir.toPath())
                .filter { filterToRoot(it.toFile()) }
                .forEach {
                    FileUtils.copyFile(it.toFile(), File(toFile, it.toFile().name))
                }
        FileUtils.deleteDirectory(tmpDir)
    }
}
