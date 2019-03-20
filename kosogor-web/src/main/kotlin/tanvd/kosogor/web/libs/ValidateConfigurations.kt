package tanvd.kosogor.web.libs

import org.gradle.api.DefaultTask
import org.gradle.api.Project
import org.gradle.api.artifacts.ExternalModuleDependency
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction
import org.gradle.kotlin.dsl.get
import org.gradle.kotlin.dsl.task
import tanvd.kosogor.utils.*

open class ValidateConfigurationsTask : DefaultTask() {
    init {
        group = "libs"
    }

    data class Artifact(val group: String, val name: String, val version: String?, val project: Project) {
        override fun toString() = "$group:$name:$version"
    }

    companion object {
        private const val mapName = "fixed_config"
        fun getMap(project: Project): HashMap<String, Artifact> {
            if (!project.rootProject._ext.has(mapName)) {
                project.rootProject._ext[mapName] = HashMap<String, Artifact>()
            }
            return project.rootProject._ext[mapName] as HashMap<String, Artifact>
        }

        val console = object : Console("Libs Configuration Validation>>> ", Color.RED) {}
    }

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
    ValidateConfigurationsTask.getMap(project)[configuration] = ValidateConfigurationsTask.Artifact(group!!, name, version, project)
}

fun Project.validateConfigurationsLibs(name: String = "validateConfigurations",
                                       configure: ValidateConfigurationsTask.() -> Unit): ValidateConfigurationsTask {
    return task(name, ValidateConfigurationsTask::class) { configure() }
}
