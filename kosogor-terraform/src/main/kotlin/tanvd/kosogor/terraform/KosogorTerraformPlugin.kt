package tanvd.kosogor.terraform

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.Copy
import org.gradle.kotlin.dsl.create
import tanvd.kosogor.terraform.tasks.download.TerraformDownloadTask
import tanvd.kosogor.terraform.tasks.download.TfLintDownloadTask
import tanvd.kosogor.terraform.tasks.lint.LintModulesTask
import tanvd.kosogor.terraform.tasks.prepare.DownloadArtifactsTask
import tanvd.kosogor.terraform.tasks.publish.CollectModulesTask
import tanvd.kosogor.terraform.tasks.publish.PublishModulesTask
import tanvd.kosogor.terraform.tasks.validate.ValidateModulesTask
import tanvd.kosogor.terraform.utils.GlobalFile
import tanvd.kosogor.terraform.utils.GlobalTask
import java.io.File

/**
 * Implementation of Kosogor Terraform plugin.
 *
 *
 * It adds few types of tasks:
 * * Terraform deployment tasks -- terraform operations on registered
 *   sources roots
 * * Terraform publishing tasks -- validation and uploading of
 *   terraform modules to remote repository.
 *
 * Tasks for publishing will be created automatically, if any
 * `package.json` found in project sources.
 *
 * Also, it creates tasks for build preparation:
 * * Download of needed binaries: Terraform, TfLint
 * * Download of remote artifacts: jars, archives
 */
class KosogorTerraformPlugin : Plugin<Project> {
    override fun apply(project: Project) {
        if (project.configurations.findByName("lambdas") == null) {
            project.configurations.create("lambdas")
        }

        GlobalFile.modulesDir = project.layout.buildDirectory.file("modules").get().asFile

        GlobalFile.tfBin = project.layout.buildDirectory.file("bin/terraform").get().asFile
        GlobalFile.tfInitDir = project.layout.buildDirectory.file("bin/tf_init").get().asFile
        GlobalTask.tfDownload = project.tasks.create<TerraformDownloadTask>("tf_download") {
            group = "build setup"
            description = "Download Terraform"
        }

        project.afterEvaluate {
            if (terraformDsl.enablePublisher) {
                val validate = project.tasks.create<ValidateModulesTask>("validate") {
                    dependsOn(GlobalTask.tfDownload)

                    group = "terraform_modules"
                    description = "Validate terraform modules"
                }
                val collect = project.tasks.create<CollectModulesTask>("collect") {
                    dependsOn(validate)

                    group = "terraform_modules"
                    description = "Package terraform modules"
                }
                project.tasks.create<LintModulesTask>("tflint") {
                    group = "terraform_modules"
                    description = "Validate modules with tflint"
                }
                project.tasks.create<PublishModulesTask>("publish") {
                    dependsOn(collect)

                    group = "terraform_modules"
                    description = "Publisher terraform modules"
                }
            }
        }


        //linter
        GlobalFile.tfLintBin = project.layout.buildDirectory.file("bin/tflint").get().asFile

        GlobalTask.tfLintDownload = project.tasks.create<TfLintDownloadTask>("tflint_download") {
            group = "build setup"
            description = "Download TfLint"
        }

        //artifacts
        GlobalTask.prepareJars = project.tasks.create<Copy>("prepare_jars") {
            group = "build setup"
            description = "Prepare jars"

            project.afterEvaluate {
                terraformDsl.artifacts.jars.forEach { (conf, file) ->
                    conf.resolve()
                    from(conf) {
                        into(file)
                    }
                }
            }
        }

        GlobalTask.prepareRemotes = project.tasks.create<DownloadArtifactsTask>("prepare_remotes") {
            group = "build setup"
            description = "Prepare remotes"
        }
    }
}


