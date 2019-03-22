package tanvd.kosogor.web.deps

import org.gradle.api.DefaultTask
import org.gradle.api.Project
import org.gradle.api.artifacts.ExternalModuleDependency
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction
import org.gradle.kotlin.dsl.get
import org.gradle.kotlin.dsl.task
import tanvd.kosogor.web.utils.*

/**
 * Task checks if there are dependencies which were fixed for specific configuration,
 * but now are used in some other configuration.
 *
 * The main purpose of this task is to fix dependencies per configuration (in case
 * of web containers it guarantees that library will always be in the same classloader).
 *
 * Use fixFor to fix dependency in some configuration and ValidateConfigurations task
 * will make sure that this dependency presenter only in this configuration.
 */
open class ValidateConfigurations : DefaultTask() {
    init {
        group = "deps"
    }

    internal data class Artifact(val group: String, val name: String, val version: String?, val project: Project) {
        override fun toString() = "$group:$name:$version"
    }

    companion object : Console("Dependencies Configurations Validation>>> ", Color.RED) {
        private const val mapName = "validate_configurations_fixed"
        internal fun getFixedMap(project: Project): HashMap<String, Artifact> {
            if (!project.rootProject._ext.has(mapName)) {
                project.rootProject._ext[mapName] = HashMap<String, Artifact>()
            }
            return project.rootProject.ext(mapName)
        }
    }

    /** If true, than task will fail build if validation failed. By default -- true */
    @get:Input
    var failOnValidationError = true

    private val includeConfs = ArrayList<String>()
    fun include(vararg configuration: String) {
        includeConfs += configuration
    }

    @TaskAction
    fun validate() {
        with(project) {
            var hasErrors = false

            val configurations = HashMap<String, ArrayList<Artifact>>()
            subprojects.forEach { sub ->
                includeConfs.forEach { conf ->
                    configurations.getOrPut(conf) { ArrayList() }.addAll(sub.configurations[conf].resolvedConfiguration.resolvedArtifacts.map {
                        with(it.moduleVersion.id) {
                            Artifact(group, name, version, sub)
                        }
                    })
                }
            }

            getFixedMap(project).forEach { (conf, artifact) ->
                configurations.forEach {
                    if (it.key != conf && it.value.any { it.toString() == artifact.toString() }) {
                        hasErrors = true
                        val other = it.value.filter { it.toString() == artifact.toString() }
                        ValidateConfigurations.println("Dependency $artifact fixed (conf: $conf, project: ${artifact.project.fullName}), but used " +
                                " (conf: ${it.key}, projects: [${other.joinToString { it.project.fullName }}])")
                    }
                }
            }

            if (hasErrors && failOnValidationError) {
                error("Errors encountered in $name task during validation.")
            }
        }
    }
}

fun <T : ExternalModuleDependency> T.fixFor(project: Project, configuration: String) {
    ValidateConfigurations.getFixedMap(project)[configuration] = ValidateConfigurations.Artifact(group!!, name, version, project)
}

fun Project.validateConfigurations(name: String = "validateConfigurations",
                                   configure: ValidateConfigurations.() -> Unit): ValidateConfigurations {
    return task(name, ValidateConfigurations::class) { configure() }
}
