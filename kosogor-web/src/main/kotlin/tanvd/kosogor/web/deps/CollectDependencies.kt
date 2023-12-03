package tanvd.kosogor.web.deps

import org.gradle.api.Action
import org.gradle.api.Project
import org.gradle.api.artifacts.ResolvableDependencies
import org.gradle.api.artifacts.result.ResolvedArtifactResult
import org.gradle.api.artifacts.result.ResolvedComponentResult
import org.gradle.api.file.FileCopyDetails
import org.gradle.api.tasks.*
import org.gradle.api.tasks.bundling.Zip
import org.gradle.kotlin.dsl.get
import org.gradle.kotlin.dsl.task
import tanvd.kosogor.web.utils.*
import java.io.File

/**
 * CollectDependencies task collects dependencies from specified configurations across all projects.
 *
 * Also, it supports exclusion of other CollectDependencies result sets from current.
 *
 * The main purpose of this task is to support preparing of libs for different classloaders
 * of web container.
 *
 * In case of Tomcat it can be used to collect in different archives dependencies for common
 * classloader and per-webapp classloader.
 *
 */
abstract class CollectDependencies : Zip() {
    init {
        group = "deps"
    }

    /** Dependency sets to exclude from this */
    fun exclude(vararg tasks: CollectDependencies) {
        tasks.forEach { deps ->
            dependsOn(deps)
            exclude {
                deps.wasProcessed(it.file)
            }
        }
    }

    @get:Input
    internal val includeConfigs = LinkedHashSet<String>()

    /**
     *
     * Note, configuration will be searched across all projects
     */
    fun includeConfigs(vararg configuration: String) {
        includeConfigs += configuration
    }

    @get:Internal
    internal val includeFiles = ArrayList<FilesConfig>()
    /** Include files into this dependencySet */
    fun include(configure: FilesConfig.() -> Unit) {
        includeFiles += FilesConfig(project).apply(configure)
    }

    @get:Internal
    internal val producedArtifacts = mutableListOf<ResolvableDependencies>()
    fun wasProcessed(file: File): Boolean {
        return producedArtifacts.any {
            file in it.files
        }
    }
}

fun Project.collectDependencies(name: String = "collectDependencies", configure: CollectDependencies.() -> Unit): CollectDependencies {
    return task(name, CollectDependencies::class) {
        configure()

        from(project.subprojects.flatMap { sub ->
            includeConfigs.map {
                sub.configurations[it].also { conf ->
                    producedArtifacts += conf.incoming
                }
            }
        })

        includeFiles.forEach { config ->
            config.apply(this)
        }
    }
}
