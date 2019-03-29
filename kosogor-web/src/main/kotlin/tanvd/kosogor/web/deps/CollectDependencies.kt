package tanvd.kosogor.web.deps

import org.gradle.api.DefaultTask
import org.gradle.api.Project
import org.gradle.api.internal.DocumentationRegistry
import org.gradle.api.internal.file.FileResolver
import org.gradle.api.internal.file.archive.ZipCopyAction
import org.gradle.api.internal.file.copy.*
import org.gradle.api.tasks.*
import org.gradle.internal.nativeplatform.filesystem.FileSystem
import org.gradle.internal.reflect.Instantiator
import org.gradle.kotlin.dsl.get
import org.gradle.kotlin.dsl.task
import tanvd.kosogor.web.utils.*
import java.io.File
import java.util.zip.ZipOutputStream
import javax.inject.Inject

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
open class CollectDependencies : DefaultTask() {
    init {
        group = "deps"
    }

    /** Archive with collected dependencies */
    @get:OutputFile
    lateinit var archiveFile: File

    private val setName: String
        get() = "${name}MemoizationSet"

    /** Collected dependencies across all projects (with exclusions applied) */
    @get:InputFiles
    @get:PathSensitive(PathSensitivity.RELATIVE)
    val dependencySet: Set<File>
        get() {
            initializeGlobalSet()
            return project.rootProject.ext(setName)
        }

    private val exclude = LinkedHashSet<CollectDependencies>()
    /** Dependency sets to exclude from this */
    fun exclude(vararg tasks: CollectDependencies) {
        exclude += tasks
        dependsOn(tasks)
    }

    private val includeConfs = LinkedHashSet<String>()
    /**
     * Include a configuration into this dependencySet
     *
     * Note, configuration will be searched across all projects
     */
    fun include(vararg configuration: String) {
        includeConfs += configuration
    }

    private val includeFiles = ArrayList<FilesConfig>()
    /** Include files into this dependencySet */
    fun include(configure: FilesConfig.() -> Unit) {
        includeFiles += FilesConfig(project).apply(configure)
    }

    @Inject
    open fun getInstantiator(): Instantiator {
        throw NotImplementedError()
    }

    @Inject
    open fun getFileSystem(): FileSystem {
        throw NotImplementedError()
    }

    @Inject
    open fun getFileResolver(): FileResolver {
        throw NotImplementedError()
    }

    private var initialized: Boolean = false
    private fun initializeGlobalSet() {
        if (initialized) return

        exclude.forEach { it.initializeGlobalSet() }

        with(project) {
            if (!rootProject.hasProperty(setName)) {
                rootProject._ext[setName] = HashSet<File>()
            }
            val set = rootProject._ext.get(setName) as HashSet<File>

            set.addAll(subprojects.flatMap { sub -> includeConfs.flatMap { sub.configurations[it].resolvedConfiguration.resolvedArtifacts.map { it.file } } })
            set.removeAll(exclude.flatMap { rootProject._ext[it.setName] as HashSet<File> })
        }

        initialized = true
    }

    @TaskAction
    fun archiveDependencies() {
        archiveFile.parentFile.mkdirs()
        initializeGlobalSet()

        val copyAction = ZipCopyAction(archiveFile, DefaultZipCompressor(false, ZipOutputStream.DEFLATED),
                services.get(DocumentationRegistry::class.java), "UTF-8", true)

        val rootSpec = getInstantiator().newInstance(DefaultCopySpec::class.java, getFileResolver(), getInstantiator()).apply {
            from(dependencySet)
            includeFiles.forEach { config ->
                config.apply(this)
            }
        }
        CopyActionExecuter(getInstantiator(), getFileSystem(), false).execute(rootSpec, copyAction)
    }
}

fun Project.collectDependencies(name: String = "collectDependencies", configure: CollectDependencies.() -> Unit): CollectDependencies {
    return task(name, CollectDependencies::class) { configure() }
}
