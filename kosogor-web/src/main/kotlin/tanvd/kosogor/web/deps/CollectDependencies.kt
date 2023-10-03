package tanvd.kosogor.web.deps

import org.gradle.api.Project
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

    /** Collected dependencies across all projects (with exclusions applied) */
    fun allDependencies(): Set<File> {
        project.subprojects.flatMap { sub ->
            includeConfigs.map { sub.configurations[it] }
        }
        val allExcluded = exclude.flatMapTo(hashSetOf()) {
            project.configurations[it].files
        }
        val currentSet = project.subprojects.flatMapTo(hashSetOf()) { sub ->
            includeConfigs.flatMap { sub.configurations[it].files }
        }
        return currentSet - allExcluded
    }

    @get:Input
    internal val exclude = mutableSetOf<String>()
    /** Dependency sets to exclude from this */
    fun exclude(vararg tasks: CollectDependencies) {
        exclude += tasks.flatMap { it.includeConfigs }
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
}

fun Project.collectDependencies(name: String = "collectDependencies", configure: CollectDependencies.() -> Unit): CollectDependencies {
    return task(name, CollectDependencies::class) {
        configure()
        from(project.subprojects.flatMap { sub ->
            includeConfigs.map { sub.configurations[it] }
        })
        var excludedFiles: Set<File>? = null
        exclude {
            if (excludedFiles == null) {
                excludedFiles = exclude.flatMapTo(hashSetOf()) { configurations[it].files }
            }
            it.file in excludedFiles!!
        }
//        from(dependencySet) // init lazy
        includeFiles.forEach { config ->
            config.apply(this)
        }
    }
}
