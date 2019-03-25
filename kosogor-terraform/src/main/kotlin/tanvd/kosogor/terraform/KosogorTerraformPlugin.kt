package tanvd.kosogor.terraform

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.Copy
import org.gradle.kotlin.dsl.create
import tanvd.kosogor.terraform.tasks.download.TerraformDownloadTask
import tanvd.kosogor.terraform.tasks.download.TfLintDownloadTask
import tanvd.kosogor.terraform.tasks.prepare.DownloadArtifactsTask
import tanvd.kosogor.terraform.tasks.publish.CollectModulesTask
import tanvd.kosogor.terraform.tasks.publish.PublishModulesTask
import tanvd.kosogor.terraform.tasks.validate.ValidateModulesTask
import tanvd.kosogor.terraform.utils.GlobalFile
import tanvd.kosogor.terraform.utils.GlobalTask
import java.io.File

class KosogorTerraformPlugin : Plugin<Project> {
    override fun apply(project: Project) {
        if (project.configurations.findByName("lambdas") == null) {
            project.configurations.create("lambdas")
        }

        GlobalFile.modulesDir = File(project.buildDir, "modules")

        GlobalFile.tfBin = File(project.buildDir, "bin/terraform")
        GlobalFile.tfInitDir = File(project.buildDir, "bin/tf_init")
        GlobalTask.tfDownload = project.tasks.create<TerraformDownloadTask>("tf_download") {
            group = "build setup"
            description = "Download Terraform"
        }

        //publisher
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
                project.tasks.create<PublishModulesTask>("publish") {
                    dependsOn(collect)

                    group = "terraform_modules"
                    description = "Publisher terraform modules"
                }
            }
        }


        //linter
        GlobalFile.tfLintBin = File(project.buildDir, "bin/tflint")

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


