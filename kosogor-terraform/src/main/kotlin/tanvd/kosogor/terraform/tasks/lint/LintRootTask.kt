package tanvd.kosogor.terraform.tasks.lint

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction
import tanvd.kosogor.terraform.TerraformDsl.Linter.LinterType
import tanvd.kosogor.terraform.terraformDsl
import tanvd.kosogor.terraform.utils.*
import java.io.File

/**
 * Task validates correctness of terraform root.
 *
 * It uses terraform itself or TfLint to check correctness.
 *
 * If terraform chosen in DSL as linter, then
 * `plan` operation will be performed and linting will be successful
 * only if `plan` returned 0 exit code.
 *
 * If `tflint` chosen in DSL as linter, then it will be
 * executed upon provided source root and linting will be successful
 * only if `tflint` returned 0 exit code.
 *
 * NOTE: right now it is better to use `terraform` linter,
 * cause author of `tflint` does not recommend using its latest
 * version.
 */
open class LintRootTask : DefaultTask() {
    init {
        outputs.upToDateWhen { false }
        project.afterEvaluate {
            dependsOn(GlobalTask.prepareJars, GlobalTask.prepareRemotes, GlobalTask.tfLintDownload, GlobalTask.tfDownload)
        }
    }

    @get:Input
    lateinit var root: File

    @TaskAction
    fun lintDir() {
        when (terraformDsl.linter.linter) {
            LinterType.Terraform -> {
                val retInit = CommandLine.execute(GlobalFile.tfBin.absolutePath, listOf("init"), root)
                if (retInit != 0) {
                    error("Terraform init failed (during linting)")
                }

                val retPlan = CommandLine.execute(GlobalFile.tfBin.absolutePath, listOf("plan"), root)
                if (retPlan != 0) {
                    error("Terraform plan failed (during linting)")
                }
            }
            LinterType.TfLint -> {
                val retLint = CommandLine.execute(GlobalFile.tfLintBin.absolutePath, emptyList(), root)
                if (retLint != 0) {
                    error("TfLint failed")
                }
            }
        }
    }
}
