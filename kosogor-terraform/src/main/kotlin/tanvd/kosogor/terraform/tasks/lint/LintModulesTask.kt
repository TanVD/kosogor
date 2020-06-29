package tanvd.kosogor.terraform.tasks.lint

import com.beust.klaxon.Klaxon
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction
import tanvd.kosogor.terraform.PackageInfo
import tanvd.kosogor.terraform.terraformDsl
import tanvd.kosogor.terraform.utils.CommandLine
import tanvd.kosogor.terraform.utils.GlobalFile
import tanvd.kosogor.terraform.utils.GlobalTask
import java.io.File

open class LintModulesTask : DefaultTask() {
    init {
        outputs.upToDateWhen { false }
        project.afterEvaluate {
            dependsOn(GlobalTask.tfLintDownload)
        }
    }

    @TaskAction
    fun lintModules() {
        val defaultConfig = configureTFLint()
        try {
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

                        val configFile = File(workingDir, ".tflint.hcl")
                        val config = when {
                            configFile.exists() -> configFile.absolutePath
                            terraformDsl.config.tfLintConfigPath != null -> terraformDsl.config.tfLintConfigPath
                            else -> defaultConfig.absolutePath
                        }

                        println("TFLint module ${packageInfo.group}:${packageInfo.name}:${packageInfo.version}")
                        lint(workingDir, config!!)
                    }
        } finally {
            defaultConfig.delete()
        }
    }

    private fun lint(workingDir: File, config: String) {
        CommandLine.executeOrFail(GlobalFile.tfLintBin.absolutePath, listOf("-c=$config"),
                workingDir, true)
    }

    private fun configureTFLint(): File {
        return File(project.buildDir, "tflint.hcl").apply {
            writeText(
                    """
                        | config {
                        |   module = false
                        |   deep_check = false
                        |   force = false
                        | }
                        | 
                        | rule "terraform_unused_declarations" {
                        |   enabled = true
                        | }
                        | 
                        | rule "terraform_deprecated_index" {
                        |   enabled = true
                        | }
                        | 
                        | rule "terraform_naming_convention" {
                        |   enabled = true
                        |   custom = "^[a-z0-9]+([_-][a-z0-9]+)*${'$'}"
                        | }
                        """.trimMargin()
            )
        }
    }
}
