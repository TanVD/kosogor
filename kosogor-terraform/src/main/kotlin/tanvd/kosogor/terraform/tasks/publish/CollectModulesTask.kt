package tanvd.kosogor.terraform.tasks.publish


import com.beust.klaxon.Klaxon
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction
import org.gradle.kotlin.dsl.support.zipTo
import tanvd.kosogor.terraform.PackageInfo
import tanvd.kosogor.terraform.utils.GlobalFile
import java.io.File

open class CollectModulesTask : DefaultTask() {
    @TaskAction
    fun collectModules() {
        project.projectDir
                .walk()
                .filter { it.absolutePath.endsWith("/package.json") }
                .forEach {
                    val packageInfo = Klaxon().parse<PackageInfo>(it.readText())!!

                    val archivePath = File(
                            GlobalFile.modulesDir,
                            "${packageInfo.groupPath()}/${packageInfo.name}/${packageInfo.version}.zip"
                    )
                    archivePath.parentFile.mkdirs()
                    zipTo(archivePath, it.parentFile)

                }
    }
}
