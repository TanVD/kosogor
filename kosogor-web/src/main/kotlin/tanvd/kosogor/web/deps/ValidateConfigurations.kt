package tanvd.kosogor.web.deps

import org.gradle.api.DefaultTask
import org.gradle.api.Project
import org.gradle.api.artifacts.ExternalModuleDependency
import org.gradle.api.artifacts.result.ResolvedComponentResult
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.TaskAction
import org.gradle.kotlin.dsl.get
import org.gradle.kotlin.dsl.task
import tanvd.kosogor.web.utils.*

/**
 * Task checks if there are dependencies, which were fixed for a specific configuration,
 * but now are used in some other configuration.
 *
 * The main purpose of this task is to fix dependencies per configuration (in case
 * of web containers it guarantees that library will always be in the same classloader).
 *
 * Use fixFor to fix dependency in some configuration and ValidateConfigurations task
 * will make sure this dependency presenter only in this configuration.
 */
open class ValidateConfigurations : DefaultTask() {
    init {
        group = "deps"
    }

    internal data class Artifact(val group: String, val name: String, val version: String?, val projectName: String) {
        override fun toString() = "$group:$name:$version"
    }

    internal companion object : Console("Dependencies Configurations Validation>>> ", Color.RED) {
        private const val mapName = "validate_configurations_fixed"
    }

    @get:Internal
    internal val fixedMap: HashMap<String, Artifact> = run {
        if (!project.rootProject._ext.has(mapName)) {
            project.rootProject._ext[mapName] = HashMap<String, Artifact>()
        }
        project.rootProject.ext(mapName)
    }

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
    
    private val configurationsMap = HashMap<String, MutableList<Pair<Provider<ResolvedComponentResult>, String>>>()

    internal fun initConfigurations(subprojects: Set<Project>) {
        configurationsMap.clear()
        subprojectNames = subprojects.map { it.fullName }.toSet()
        subprojects.filter { it.fullName !in excludeSubprojectNames }.forEach { sub ->
            includeConfs.forEach { conf ->
                val resolvedArtifacts = sub.configurations[conf].incoming.resolutionResult.rootComponent
                configurationsMap.getOrPut(conf) { arrayListOf() }.add(resolvedArtifacts to sub.fullName)
            }
        }
    }

    @TaskAction
    fun validate() {
        var hasErrors = false

        val configurations = HashMap<String, ArrayList<Artifact>>()
        configurationsMap.forEach { (confName, configurationsAndProject) ->
            configurations[confName] = arrayListOf()
            configurationsAndProject.forEach { (configuration, projectName) ->
                configurations[confName]!!.addAll(configuration.get().dependencies.map { dep ->
                    with(dep.from.moduleVersion!!) {
                        Artifact(group, name, version, projectName)
                    }
                })
            }
        }

        fixedMap.forEach { (conf, artifact) ->
            configurations.forEach { (confName, artifacts) ->
                val other = artifacts.filter { it.toString() == artifact.toString() }
                if (confName != conf && other.isNotEmpty()) {
                    hasErrors = true
                    ValidateConfigurations.println("Dependency $artifact fixed (conf: $conf, project: ${artifact.projectName}), but used " +
                            " (conf: ${confName}, projects: [${other.joinToString { it.projectName }}])")
                }
            }
        }

        if (hasErrors && failOnValidationError) {
            error("Errors encountered in $name task during validation.")
        }
    }
}

fun <T : ExternalModuleDependency> T.fixFor(project: Project, configuration: String) {
    project.tasks.filterIsInstance<ValidateConfigurations>().forEach {
        it.fixedMap[configuration] = ValidateConfigurations.Artifact(group!!, name, version, project.fullName)
    }
}

fun Project.validateConfigurations(name: String = "validateConfigurations",
                                   configure: ValidateConfigurations.() -> Unit): ValidateConfigurations {
    return task(name, ValidateConfigurations::class) {
        configure()
        initConfigurations(subprojects)
    }
}
