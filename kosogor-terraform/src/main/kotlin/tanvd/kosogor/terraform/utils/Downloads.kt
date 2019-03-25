package tanvd.kosogor.terraform.utils

import org.codehaus.plexus.util.FileUtils
import java.io.File
import java.io.FileOutputStream
import java.net.URL
import java.nio.channels.Channels
import java.nio.file.Files

internal object Downloads {
    fun download(url: URL, toFile: File) {
        toFile.parentFile.mkdirs()
        FileOutputStream(toFile).channel.transferFrom(Channels.newChannel(url.openStream()), 0, java.lang.Long.MAX_VALUE)
    }

    fun download(url: URL, toFile: File, archive: Archive) {
        toFile.parentFile.mkdirs()
        val archiveFile = File(toFile.absolutePath + "." + archive.extension)

        download(url, archiveFile)

        archive.unarchive(archiveFile, toFile)

        archiveFile.delete()
    }

    fun download(url: URL, toFile: File, archive: Archive, filterToRoot: (File) -> Boolean) {
        val tmpDir = File(toFile, "tmp_${RandomCode.next()}")
        Downloads.download(url, tmpDir, archive)
        Files.walk(tmpDir.toPath())
                .filter { filterToRoot(it.toFile()) }
                .forEach {
                    FileUtils.copyFile(it.toFile(), File(toFile, it.toFile().name))
                }
        FileUtils.deleteDirectory(tmpDir)
    }
}
