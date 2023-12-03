package tanvd.kosogor.web.deps

import org.gradle.api.DefaultTask
import org.gradle.api.Project
import org.gradle.api.artifacts.component.ModuleComponentIdentifier
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction
import org.gradle.kotlin.dsl.get
import org.gradle.kotlin.dsl.task
import tanvd.kosogor.web.utils.Console
import tanvd.kosogor.web.utils.fullName
import tanvd.kosogor.web.utils.resolvableConfiguration

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

    private data class ArtifactVersion(val version: String, val projectName: String, val configuration: String)

    internal companion object : Console("Dependencies Versions Validation>>> ", Color.RED)

    /** If true, then task will fail build if validation failed. By default, true */
    @get:Input
    var failOnValidationError = true

    private val includeConfs = ArrayList<String>()
    fun include(vararg configuration: String) {
        includeConfs += configuration
    }

    private val excludeSubprojectNames = mutableSetOf<String>()
    fun excludeSubProjects(vararg project: Project) {
        excludeSubprojectNames += project.map { it.fullName }
    }

    @get:Input
    internal lateinit var subprojectNames: Set<String>

    private lateinit var dependencies: List<Provider<List<Pair<ArtifactId, ArtifactVersion>>>>

    internal fun initVersions(subprojects: Set<Project>) {
        subprojectNames = subprojects.map { it.fullName }.toSet()
        dependencies = subprojects.filter { it.fullName !in excludeSubprojectNames }.flatMap { subproject ->
            val subprojectName = subproject.fullName
            includeConfs.map { configurationName ->
                val configuration = subproject.resolvableConfiguration(configurationName)
                val artifactProvider = configuration.incoming.artifacts.resolvedArtifacts
                artifactProvider.map { artifacts ->
                    artifacts.mapNotNull { a ->
                        (a.variant.owner as? ModuleComponentIdentifier)?.let {
                            ArtifactId(it.module, it.group) to ArtifactVersion(it.version, subprojectName, configurationName)
                        }
                    }
                }
            }
        }
    }

    @TaskAction
    fun validate() {
        var hasErrors = false

        val groupedArtifacts = dependencies.flatMap { it.get() }.groupBy { it.first }.mapValues { it.value.map { it.second }.toSet() }
        groupedArtifacts.filter { it.value.distinctBy { it.version }.size > 1 }.forEach { (artifact, versions) ->
            hasErrors = true
            ValidateVersions.println("For dependency $artifact found versions:")
            val byVersion = versions.groupBy { it.version }.mapValues { it.value.joinToString { "(project: ${it.projectName}, configuration: ${it.configuration})" } }
            for ((version, projects) in byVersion) {
                ValidateVersions.println("\t version $version in: $projects")
            }
        }

        if (hasErrors && failOnValidationError) {
            error("Errors encountered in $name task during validation.")
        }
    }
}

fun Project.validateVersions(name: String = "validateVersions", configure: ValidateVersions.() -> Unit): ValidateVersions {
    return task(name, ValidateVersions::class) {
        configure()
        initVersions(subprojects)
    }
}
