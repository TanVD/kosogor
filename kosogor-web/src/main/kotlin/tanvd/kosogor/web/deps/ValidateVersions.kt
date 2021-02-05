package tanvd.kosogor.web.deps

import org.gradle.api.DefaultTask
import org.gradle.api.Project
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction
import org.gradle.kotlin.dsl.get
import org.gradle.kotlin.dsl.task
import tanvd.kosogor.web.utils.Console
import tanvd.kosogor.web.utils.fullName

/**
 * Task checks if there are multiple artifacts equal by name and group, but with different
 * versions in specified configurations.
 *
 * The main purpose of this task is to validate dependencies prepared for classloaders of
 * web container and be sure, that no dependencies are overridden
 */
open class ValidateVersions : DefaultTask() {
    init {
        group = "deps"
    }

    private data class ArtifactId(val name: String, val group: String) {
        override fun toString() = "$group:$name"
    }

    private data class ArtifactVersion(val version: String, val project: Project, val configuration: String)

    internal companion object : Console("Dependencies Versions Validation>>> ", Color.RED)

    /** If true, then task will fail build if validation failed. By default, true */
    @get:Input
    var failOnValidationError = true

    private val includeConfs = ArrayList<String>()
    fun include(vararg configuration: String) {
        includeConfs += configuration
    }

    private val excludeSubprojects = mutableSetOf<Project>()
    fun excludeSubProjects(vararg project: Project) {
        excludeSubprojects += project
    }

    @TaskAction
    fun validate() {
        with(project) {
            var hasErrors = false

            val dependencies = subprojects.filter { it !in excludeSubprojects }.flatMap { subproject ->
                includeConfs.flatMap { configuration ->
                    subproject.configurations[configuration].resolvedConfiguration.resolvedArtifacts
                            .map { it.moduleVersion.id }
                            .map { ArtifactId(it.name, it.group) to ArtifactVersion(it.version, subproject, configuration) }
                }
            }
            val groupedArtifacts = dependencies.groupBy { it.first }.mapValues { it.value.map { it.second }.toSet() }
            groupedArtifacts.filter { it.value.distinctBy { it.version }.size > 1 }.forEach { (artifact, versions) ->
                hasErrors = true
                ValidateVersions.println("For dependency $artifact found versions:")
                val byVersion = versions.groupBy { it.version }.mapValues { it.value.joinToString { "(project: ${it.project.fullName}, configuration: ${it.configuration})" } }
                for ((version, projects) in byVersion) {
                    ValidateVersions.println("\t version $version in: $projects")
                }
            }

            if (hasErrors && failOnValidationError) {
                error("Errors encountered in $name task during validation.")
            }
        }
    }
}

fun Project.validateVersions(name: String = "validateVersions", configure: ValidateVersions.() -> Unit): ValidateVersions {
    return task(name, ValidateVersions::class) { configure() }
}
