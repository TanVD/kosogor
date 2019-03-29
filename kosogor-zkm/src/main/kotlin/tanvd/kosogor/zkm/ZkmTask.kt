package tanvd.kosogor.zkm

import org.gradle.api.DefaultTask
import org.gradle.api.Project
import org.gradle.api.artifacts.Configuration
import org.gradle.api.tasks.*
import org.gradle.kotlin.dsl.task
import org.gradle.process.internal.ExecActionFactory
import java.io.File
import javax.inject.Inject
import kotlin.collections.set

/** Gradle wrapper task for ZKM process */
@CacheableTask
open class ZkmTask : DefaultTask() {
    init {
        group = "obfuscate"
    }

    /**
     * Jars to pass to ZKM for obfuscation
     *
     * Mapped into `INPUT_JARS` variable in .zkm script
     */
    @get:InputFiles
    @get:PathSensitive(PathSensitivity.RELATIVE)
    lateinit var inputJars: Set<File>

    /**
     * Directory to output jars into
     *
     * Mapped into `OUTPUT_DIR` variable in .zkm script
     */
    @get:OutputDirectory
    lateinit var outputDir: File

    /**
     * Changelog of ZKM
     *
     * Mapped into `CHANGELOG_FILE` variable in .zkm script
     */
    @get:Internal
    lateinit var changeLogFile: File

    /** Main log of ZKM */
    @get:Internal
    lateinit var zkmLogFile: File

    /** Classpath, which should be used by ZKM during obfuscation */
    @get:Classpath
    lateinit var zkmClasspath: Set<Configuration>

    /** Path zkm.jar file */
    @get:InputFile
    @get:PathSensitive(PathSensitivity.RELATIVE)
    lateinit var zkmJar: File

    /** Path to .zkm script file */
    @get:InputFile
    @get:PathSensitive(PathSensitivity.RELATIVE)
    lateinit var zkmScript: File

    @Inject
    open fun getExecActionFactory(): ExecActionFactory = throw NotImplementedError()

    @TaskAction
    fun obfuscate() {
        with(project) {
            outputDir.mkdirs()
            getExecActionFactory().newJavaExecAction().apply {
                args = listOf("-v", "-l", zkmLogFile.absolutePath, zkmScript.absolutePath)
                classpath = files(zkmClasspath.flatMap { it.files } + fileTree(zkmJar))

                systemProperties["INPUT_JARS"] = inputJars.joinToString(prefix = "open ", separator = "\n") {
                    if (File.separatorChar == '\\')
                        "\\\"${it.absolutePath}\\\""
                    else
                        "\"${it.absolutePath}\""
                }
                systemProperties["OUTPUT_DIR"] = outputDir.absolutePath
                systemProperties["CHANGELOG_FILE"] = changeLogFile.absolutePath
                main = "com.zelix.ZKM"
            }.execute()
        }
    }
}

fun Project.zkmJars(name: String = "zkmJars", configure: ZkmTask.() -> Unit): ZkmTask {
    return task(name, ZkmTask::class, configure)
}
