package tanvd.kosogor.zkm

import org.gradle.api.DefaultTask
import org.gradle.api.Project
import org.gradle.api.artifacts.Configuration
import org.gradle.api.tasks.*
import org.gradle.kotlin.dsl.task
import org.gradle.process.internal.ExecActionFactory
import java.io.File
import javax.inject.Inject

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
            val processedKmpScript = File(zkmScript.absolutePath + ".tmp")
            zkmScript.copyTo(processedKmpScript, overwrite = true)
            extendFileWithParameters(processedKmpScript)

            getExecActionFactory().newJavaExecAction().apply {
                args = listOf("-v", "-l", zkmLogFile.absolutePath, processedKmpScript.absolutePath)
                classpath = fileTree(zkmJar)
                mainClass.set("com.zelix.ZKM")
            }.execute()
            processedKmpScript.delete()
        }
    }

    private fun Collection<File>.joinFilesToString(prefix: String) : String =
        distinct().joinToString(prefix = prefix, postfix = ";", separator = "\n") {
            "\"${it.absolutePath}\""
        }

    private fun Project.extendFileWithParameters(fileToProcess: File) {
        var content = fileToProcess.readText()
        content = content.replace("__INPUT_JARS__", inputJars.joinFilesToString("open "))
        val classpath = zkmClasspath.flatMap { it.files } + fileTree(zkmJar).files
        content = content.replace("__CLASSPATH__", classpath.joinFilesToString("classpath "))
        content = content.replace("__OUTPUT_DIR__", "\"${outputDir.absolutePath}\"")
        content = content.replace("__CHANGELOG_FILE__", "\"${changeLogFile.absolutePath}\"")
        fileToProcess.writeText(content)
    }
}

fun Project.zkmJars(name: String = "zkmJars", configure: ZkmTask.() -> Unit): ZkmTask {
    return task(name, ZkmTask::class, configure)
}
