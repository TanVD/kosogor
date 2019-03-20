package tanvd.kosogor.web.libs

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
import tanvd.kosogor.utils._ext
import java.io.File
import java.util.zip.ZipOutputStream
import javax.inject.Inject

open class CollectLibsTask : DefaultTask() {
    init {
        group = "libs"
    }

    @get:OutputDirectory
    lateinit var destinationDir: File

    @get:Input
    lateinit var archiveName: String

    private val archivePath: File
        get() = File(destinationDir, archiveName)

    private val setName: String
        get() = "${name}MemoizationSet"

    @get:InputFiles
    @get:PathSensitive(PathSensitivity.RELATIVE)
    val set: HashSet<File>
        get() {
            initializeGlobalSet()
            return project.rootProject._ext.get(setName) as HashSet<File>
        }

    private val exclude = LinkedHashSet<CollectLibsTask>()
    fun exclude(vararg tasks: CollectLibsTask) {
        exclude += tasks
        dependsOn(tasks)
    }

    private val includeConfs = LinkedHashSet<String>()
    fun includeConf(vararg config: String) {
        includeConfs += config
    }

    private val includeFiles = LinkedHashSet<Pair<File, String?>>()
    fun includeFile(vararg file: Pair<File, String?>) {
        includeFiles += file
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
    fun collectLibs() {
        destinationDir.mkdirs()

        initializeGlobalSet()

        val copyAction = ZipCopyAction(archivePath, DefaultZipCompressor(false, ZipOutputStream.DEFLATED), services.get(DocumentationRegistry::class.java), "UTF-8", true)
        val copyActionExecuter = CopyActionExecuter(getInstantiator(), getFileSystem(), false)
        val rootSpec = getInstantiator().newInstance(DefaultCopySpec::class.java, getFileResolver(), getInstantiator()).apply {
            from(set)
            includeFiles.forEach { (file, toFile) ->
                from(file) {
                    toFile?.let { into(it) }
                }
            }
        }
        copyActionExecuter.execute(rootSpec, copyAction)
    }
}

fun Project.collectLibs(name: String = "collectLibs", configure: CollectLibsTask.() -> Unit): CollectLibsTask {
    return task(name, CollectLibsTask::class) { configure() }
}
