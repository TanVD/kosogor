package tanvd.kosogor.terraform.tasks.validate

import com.beust.klaxon.Klaxon
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction
import tanvd.kosogor.terraform.PackageInfo
import tanvd.kosogor.terraform.terraformDsl
import tanvd.kosogor.terraform.utils.*
import java.io.File
import java.nio.file.Files
import java.nio.file.StandardCopyOption.COPY_ATTRIBUTES

/**
 * Task validates correctness of terraform modules.
 *
 * It uses terraform itself to check correctness.
 *
 * Basically, inside each module will be added `infra.tf`
 * file with specified in DSL aws provider and `terraform validate`
 * will be executed for this module.
 *
 * Task guarantees syntax correctness of module, and it's
 * compliance to specified version of aws provider.
 */
open class ValidateModulesTask : DefaultTask() {
    init {
        outputs.upToDateWhen { false }
        project.afterEvaluate {
            dependsOn(GlobalTask.tfDownload)
        }
    }

    @TaskAction
    fun validateModules() {
        project.projectDir
                .walk()
                .filter { it.absolutePath.endsWith("/package.json") }
                .forEach { file ->
                    val packageInfo = Klaxon().parse<PackageInfo>(file.readText())!!

                    if (packageInfo.validation.skip) {
                        println("Skipped validation for module ${packageInfo.group}:${packageInfo.name}:${packageInfo.version}")
                        return@forEach
                    }

                    val workingDir = file.parentFile

                    val providerFile = if (packageInfo.validation.createProvider) {
                        addProvider(workingDir)
                    } else null

                    try {
                        init(workingDir)
                        validate(workingDir)
                    } finally {
                        File(file.parentFile, ".terraform").deleteRecursively()
                        providerFile?.delete()
                    }
                    println("Validated module ${packageInfo.group}:${packageInfo.name}:${packageInfo.version}")
                }
    }

    private fun copyPluginsDirWithAttrs(sourceDir: File, targetDir: File) {
        targetDir.mkdirs()
        File(sourceDir, "plugins").walk().forEach {
            val targetFile = File(targetDir, it.toRelativeString(sourceDir))
            Files.copy(it.toPath(), targetFile.toPath(), COPY_ATTRIBUTES)
        }
    }

    private fun init(workingDir: File) {
        val curInitDir = File(workingDir, ".terraform")
        if (curInitDir.exists()) curInitDir.deleteRecursively()
        if (terraformDsl.validater.cacheInitPlugins && GlobalFile.tfInitDir.exists()) {
            copyPluginsDirWithAttrs(GlobalFile.tfInitDir, curInitDir)
        }
        CommandLine.executeOrFail(GlobalFile.tfBin.absolutePath, listOf("init"), workingDir, false)

        if (terraformDsl.validater.cacheInitPlugins && !GlobalFile.tfInitDir.exists()) {
            copyPluginsDirWithAttrs(curInitDir, GlobalFile.tfInitDir)
        }
    }

    private fun validate(workingDir: File) {
        val args = when (terraformDsl.config.tfVersionInt < 1200) {
            true -> listOf("validate", "-check-variables=false")
            else -> listOf("validate")
        }
        CommandLine.executeOrFail(GlobalFile.tfBin.absolutePath, args,
                workingDir, true)
    }

    private fun addProvider(workingDir: File): File {
        val allFiles = workingDir.listFiles()!!

        val name = "provider"
        var index = 0
        while (allFiles.map { it.name }.any { it == "${name}_$index.tf" }) {
            index++
        }
        return File(workingDir, "${name}_$index.tf").apply {
            writeText(
                    """
                    | provider "aws" {
                    |   region = "${terraformDsl.config.awsRegion}"
                    |   profile = "${terraformDsl.config.awsProfile}"
                    |   version = "${terraformDsl.config.awsProvider}"
                    | }
                    """.trimMargin()
            )
        }
    }
}
