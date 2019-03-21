package tanvd.kosogor.web.deps

import org.gradle.api.DefaultTask
import org.gradle.api.Project
import org.gradle.api.artifacts.ExternalModuleDependency
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction
import org.gradle.kotlin.dsl.get
import org.gradle.kotlin.dsl.task
import tanvd.kosogor.utils.*

/**
 * Task checks if there are dependencies which were fixed for specific configuration,
 * but now are used in some other configuration.
 *
 * The main purpose of this task is to fix dependencies per configuration (classloader).
 * Use fixFor to fix dependency in some configuration and ValidateConfigurations task
 * will make sure that this dependency is added only to this classloader.
 */
open class ValidateConfigurations : DefaultTask() {
    init {
        group = "deps"
    }

    internal data class Artifact(val group: String, val name: String, val version: String?, val project: Project) {
        override fun toString() = "$group:$name:$version"
    }

    companion object {
        private const val mapName = "validate_configurations_fixed"
        internal fun getMap(project: Project): HashMap<String, Artifact> {
            if (!project.rootProject._ext.has(mapName)) {
                project.rootProject._ext[mapName] = HashMap<String, Artifact>()
            }
            return project.rootProject._ext[mapName] as HashMap<String, Artifact>
        }

        val console = object : Console("Dependencies Configuration Validation>>> ", Color.RED) {}
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

            getMap(project).forEach { (conf, artifact) ->
                configurations.forEach {
                    if (it.key != conf && it.value.any { it.toString() == artifact.toString() }) {
                        hasErrors = true
                        val other = it.value.filter { it.toString() == artifact.toString() }
                        console.println("Artifact $artifact fixed (conf: $conf, project: ${artifact.project.fullName}), but used " +
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
    ValidateConfigurations.getMap(project)[configuration] = ValidateConfigurations.Artifact(group!!, name, version, project)
}

fun Project.validateConfigurations(name: String = "validateConfigurations",
                                   configure: ValidateConfigurations.() -> Unit): ValidateConfigurations {
    return task(name, ValidateConfigurations::class) { configure() }
}
