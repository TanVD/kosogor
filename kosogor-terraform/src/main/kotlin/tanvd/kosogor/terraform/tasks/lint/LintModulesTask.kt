package tanvd.kosogor.terraform.tasks.lint

import com.beust.klaxon.Klaxon
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction
import tanvd.kosogor.terraform.PackageInfo
import tanvd.kosogor.terraform.terraformDsl
import tanvd.kosogor.terraform.utils.CommandLine
import tanvd.kosogor.terraform.utils.GlobalFile
import tanvd.kosogor.terraform.utils.GlobalTask
import tanvd.kosogor.terraform.utils.TFLint
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

                    try {
                        TFLint.lint(workingDir)
                    } catch (e: Throwable) {
                        println("Linting failed for ${packageInfo.group}:${packageInfo.name}:${packageInfo.version}")
                        throw e
                    }
                    println("Linting successfully completed for ${packageInfo.group}:${packageInfo.name}:${packageInfo.version}")
                }
    }
}
