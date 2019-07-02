package tanvd.kosogor.terraform.tasks.publish


import com.beust.klaxon.Klaxon
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction
import org.gradle.kotlin.dsl.support.zipTo
import tanvd.kosogor.terraform.PackageInfo
import tanvd.kosogor.terraform.terraformDsl
import tanvd.kosogor.terraform.utils.GlobalFile
import java.io.File

/**
 * Task creates zip archives per package for following publishing.
 *
 * It will find packages automatically in project sources — just will
 * find all directories with `package.json`
 */
open class CollectModulesTask : DefaultTask() {
    @TaskAction
    fun collectModules() {
        val workDir = terraformDsl.collector.directory?.let {
            File(it)
        } ?: project.projectDir

        workDir
                .walk()
                .filter { it.absolutePath.endsWith("/package.json") }
                .forEach {
                    val packageInfo = Klaxon().parse<PackageInfo>(it.readText())!!
                    val currentDir = it.parentFile

                    val archivePath = File(GlobalFile.modulesDir, "${packageInfo.groupPath()}/${packageInfo.name}/${packageInfo.version}.zip")

                    val files = currentDir.walk()
                            .filter { !it.relativeTo(currentDir).startsWith(".terraform") }
                            .filter { it.isFile }
                            .map { Pair(it.relativeTo(currentDir).toString(), it.readBytes()) }

                    archivePath.parentFile.mkdirs()
                    zipTo(archivePath, files)
                }
    }
}
