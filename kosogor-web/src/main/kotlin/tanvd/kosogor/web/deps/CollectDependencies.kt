package tanvd.kosogor.web.deps

import org.gradle.api.DefaultTask
import org.gradle.api.Project
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.DuplicatesStrategy
import org.gradle.api.file.RegularFile
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.*
import org.gradle.api.tasks.bundling.Zip
import org.gradle.kotlin.dsl.get
import org.gradle.kotlin.dsl.register
import org.gradle.kotlin.dsl.task
import org.gradle.work.DisableCachingByDefault
import tanvd.kosogor.web.utils.*
import java.io.File

/**
 * Resolves a single project's configuration and copies the resolved files into [outputDirectory],
 * recording a `target<TAB>source` line per file in [processedFiles].
 *
 * This task runs per subproject on purpose. With `org.gradle.parallel=true`, Gradle 9 rejects
 * resolving a project's configuration from a task owned by a different project (it fails with
 * "attempted without an exclusive lock"). Resolving here keeps each configuration under its own
 * project's lock, so [CollectDependencies] can stay a plain [Zip] that only packages the
 * already-collected files instead of resolving across projects itself.
 */
@DisableCachingByDefault(because = "Copies resolved dependencies for packaging")
abstract class ConfigurationDependencyCollector : DefaultTask() {
    @get:Classpath
    abstract val dependencyFiles: ConfigurableFileCollection

    @get:OutputDirectory
    abstract val outputDirectory: DirectoryProperty

    @get:OutputFile
    abstract val processedFiles: RegularFileProperty

    @TaskAction
    fun collect() {
        val outputDir = outputDirectory.get().asFile
        outputDir.deleteRecursively()
        outputDir.mkdirs()

        val processedPaths = linkedMapOf<String, String>()
        dependencyFiles.files.forEach { file ->
            val target = outputDir.resolve(file.name)
            if (file.isDirectory) {
                file.copyRecursively(target, overwrite = true)
                file.walkTopDown().forEach { source ->
                    processedPaths[target.resolve(source.relativeTo(file).path).canonicalPath] = source.canonicalPath
                }
            } else {
                target.parentFile.mkdirs()
                file.copyTo(target, overwrite = true)
                processedPaths[target.canonicalPath] = file.canonicalPath
            }
        }

        val manifest = processedFiles.get().asFile
        manifest.parentFile.mkdirs()
        manifest.writeText(
            processedPaths.entries.joinToString(
                separator = System.lineSeparator(),
                postfix = if (processedPaths.isEmpty()) "" else System.lineSeparator(),
            ) { (target, source) -> "$target\t$source" },
        )
    }
}

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
@DisableCachingByDefault(because = "Resolves and packages dependency state from configured projects")
abstract class CollectDependencies : Zip() {
    init {
        group = "deps"
        duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    }

    /**
     * Excludes from this archive every artifact already packaged by [tasks].
     *
     * Each task copies artifacts into its own directory, so the copies never share a path across
     * tasks. We therefore map our copy (`it.file`) back to its original resolved artifact via the
     * manifest ([originalFile]) before asking the other task whether it already packaged it.
     */
    fun exclude(vararg tasks: CollectDependencies) {
        tasks.forEach { deps ->
            dependsOn(deps)
            exclude {
                deps.wasProcessed(originalFile(it.file) ?: it.file)
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
    internal val processedFileLists = mutableListOf<Provider<RegularFile>>()

    private data class ProcessedFiles(
        val paths: Set<String>,
        val sourceByTarget: Map<String, String>,
    )

    @get:Internal
    private var processedFilesCache: ProcessedFiles? = null

    internal fun registerProcessedFileList(file: Provider<RegularFile>) {
        processedFileLists += file
        processedFilesCache = null
    }

    fun wasProcessed(file: File): Boolean {
        return file.canonicalPath in processedFiles().paths
    }

    private fun originalFile(file: File): File? {
        return processedFiles().sourceByTarget[file.canonicalPath]?.let(::File)
    }

    private fun processedFiles(): ProcessedFiles {
        processedFilesCache?.let { return it }
        val sourceByTarget = linkedMapOf<String, String>()
        val paths = linkedSetOf<String>()

        processedFileLists.forEach { provider ->
            provider.get().asFile.takeIf { it.exists() }?.forEachLine { line ->
                val parts = line.split("\t", limit = 2)
                val target = parts[0]
                val source = parts.getOrElse(1) { target }
                sourceByTarget[target] = source
                paths += target
                paths += source
            }
        }

        return ProcessedFiles(paths, sourceByTarget).also { processedFilesCache = it }
    }
}

fun Project.collectDependencies(name: String = "collectDependencies", configure: CollectDependencies.() -> Unit): CollectDependencies {
    return task(name, CollectDependencies::class) {
        configure()

        // Resolve each subproject's configuration inside that subproject (Gradle 9 forbids
        // cross-project resolution under parallel execution); this task then only zips the output.
        project.subprojects.forEach { sub ->
            includeConfigs.forEach { configurationName ->
                val configuration = sub.configurations[configurationName]
                val collector = sub.tasks.register<ConfigurationDependencyCollector>(
                    "${name}${sub.path.taskNamePart()}${configurationName.taskNamePart()}DependencyFiles",
                ) {
                    dependencyFiles.from(configuration)
                    outputDirectory.set(sub.layout.buildDirectory.dir("collected-dependencies/$name/$configurationName"))
                    processedFiles.set(sub.layout.buildDirectory.file("collected-dependencies/$name/$configurationName.files"))
                }

                dependsOn(collector)
                from(collector.flatMap { it.outputDirectory })
                registerProcessedFileList(collector.flatMap { it.processedFiles })
            }
        }

        includeFiles.forEach { config ->
            config.apply(this)
        }
    }
}

private fun String.taskNamePart(): String =
    replace(Regex("[^A-Za-z0-9]"), "_")
