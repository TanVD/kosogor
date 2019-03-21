package tanvd.kosogor.web.deps

import org.gradle.api.DefaultTask
import org.gradle.api.Project
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction
import org.gradle.kotlin.dsl.get
import org.gradle.kotlin.dsl.task
import tanvd.kosogor.utils.Console
import tanvd.kosogor.utils.fullName

open class ValidateVersionsTask : DefaultTask() {
    init {
        group = "libs"
    }

    data class ArtifactId(val name: String, val group: String) {
        override fun toString() = "$group:$name"
    }

    data class ArtifactVersion(val version: String, val project: Project, val configuration: String)

    companion object : Console("Libs Versions Validation>>> ", Color.RED)

    @get:Input
    var failOnValidationError = true

    private val includeConfs = ArrayList<String>()
    fun includeConf(vararg config: String) {
        includeConfs += config
    }

    @TaskAction
    fun validate() {
        with(project) {
            var hasErrors = false

            val libs = subprojects.flatMap { sub ->
                includeConfs.flatMap { conf ->
                    sub.configurations[conf].resolvedConfiguration.resolvedArtifacts.map { it.moduleVersion.id }.map { ArtifactId(it.name, it.group) to ArtifactVersion(it.version, sub, conf) }
                }
            }
            val grouped = libs.groupBy { it.first }.mapValues { it.value.map { it.second }.toSet() }
            grouped.filter { it.value.distinctBy { it.version }.size > 1 }.forEach { (lib, versions) ->
                hasErrors = true
                ValidateVersionsTask.println("For lib $lib found versions:")
                val byVersion = versions.groupBy { it.version }.mapValues { it.value.joinToString { "(project: ${it.project.fullName}, configuration: ${it.configuration})" } }
                for ((version, projects) in byVersion) {
                    ValidateVersionsTask.println("\t version $version in: $projects")
                }
            }

            if (hasErrors && failOnValidationError) {
                error("Errors encountered in $name task during validation.")
            }
        }
    }
}

fun Project.validateVersionsLibs(name: String = "validateVersions", configure: ValidateVersionsTask.() -> Unit): ValidateVersionsTask {
    return task(name, ValidateVersionsTask::class) { configure() }
}
