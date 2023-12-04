package tanvd.kosogor.zkm

import org.gradle.api.DefaultTask
import org.gradle.api.Project
import org.gradle.api.file.FileCollection
import org.gradle.api.file.ProjectLayout
import org.gradle.api.tasks.*
import org.gradle.kotlin.dsl.task
import org.gradle.process.ExecOperations
import java.io.File
import javax.inject.Inject

/** Gradle wrapper task for ZKM process */
@CacheableTask
abstract class ZkmTask : DefaultTask() {
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
    lateinit var zkmClasspath: Set<FileCollection>

    /** Path zkm.jar file */
    @get:InputFile
    @get:PathSensitive(PathSensitivity.RELATIVE)
    lateinit var zkmJar: File

    /** Path to .zkm script file */
    @get:InputFile
    @get:PathSensitive(PathSensitivity.RELATIVE)
    lateinit var zkmScript: File

    @get:Inject
    abstract val execOperations: ExecOperations

    @get:Inject
    abstract val projectLayout: ProjectLayout

    @TaskAction
    fun obfuscate() {
        outputDir.mkdirs()
        val processedKmpScript = File(zkmScript.absolutePath + ".tmp")
        zkmScript.copyTo(processedKmpScript, overwrite = true)
        extendFileWithParameters(processedKmpScript)
        execOperations.javaexec {
            it.args = listOf("-v", "-l", zkmLogFile.absolutePath, processedKmpScript.absolutePath)
            it.classpath = projectLayout.files(zkmJar)
            it.mainClass.set("com.zelix.ZKM")
        }
        processedKmpScript.delete()
    }

    private fun Collection<File>.joinFilesToString(prefix: String) : String =
        distinct().joinToString(prefix = prefix, postfix = ";", separator = "\n") {
            "\"${it.absolutePath}\""
        }

    private fun extendFileWithParameters(fileToProcess: File) {
        var content = fileToProcess.readText()
        content = content.replace("__INPUT_JARS__", inputJars.joinFilesToString("open "))
        println(zkmClasspath)
        val classpath = zkmClasspath.flatMap { it.files } + zkmJar
        content = content.replace("__CLASSPATH__", classpath.joinFilesToString("classpath "))
        content = content.replace("__OUTPUT_DIR__", "\"${outputDir.absolutePath}\"")
        content = content.replace("__CHANGELOG_FILE__", "\"${changeLogFile.absolutePath}\"")
        fileToProcess.writeText(content)
    }
}

fun Project.zkmJars(name: String = "zkmJars", configure: ZkmTask.() -> Unit): ZkmTask {
    return task(name, ZkmTask::class, configure)
}
