package tanvd.kosogor.web.js

import org.gradle.api.DefaultTask
import org.gradle.api.Project
import org.gradle.api.artifacts.Configuration
import org.gradle.api.tasks.*
import org.gradle.kotlin.dsl.task
import org.gradle.process.internal.ExecActionFactory
import java.io.File
import java.util.concurrent.Executors
import java.util.concurrent.Future
import javax.inject.Inject


@CacheableTask
open class CompileJsTask : DefaultTask() {
    init {
        group = "js"
    }

    companion object {
        private val threadPool = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors() + 1)
    }

    @get:InputDirectory
    @get:PathSensitive(PathSensitivity.RELATIVE)
    lateinit var fromDir: File

    @get:OutputDirectory
    lateinit var toDir: File

    @get:Classpath
    lateinit var configuration: Configuration

    @get:Input
    val includeExtensions = hashSetOf(".js")

    @get:InputFiles
    @get:PathSensitive(PathSensitivity.RELATIVE)
    val excludeFiles = HashSet<File>()

    @get:Input
    val excludeExtensions = hashSetOf(".min.js")

    @get:Input
    var compilationLevel: String = "SIMPLE_OPTIMIZATIONS"

    @get:Input
    var fromLang: String = "ECMASCRIPT6"

    @get:Input
    var toLang: String = "ECMASCRIPT5"

    @get:Input
    var warningLevel: String = "QUIET"

    @Inject
    open fun getExecActionFactory(): ExecActionFactory {
        throw NotImplementedError()
    }

    @TaskAction
    fun compileJs() {
        with(project) {
            val initialArgs = listOf("--compilation_level=$compilationLevel", "--warning_level=$warningLevel",
                    "--language_in=$fromLang", "--language_out=$toLang")

            val futures = ArrayList<Future<*>>()

            for (file in fileTree(fromDir).files) {
                if (includeExtensions.any { file.absolutePath.endsWith(it) } &&
                        !(excludeExtensions.any { file.absolutePath.endsWith(it) } || excludeFiles.any { file.absolutePath.startsWith(it.absolutePath) })) {
                    futures += threadPool.submit {
                        val allArgs = ArrayList(initialArgs)
                        allArgs += "--js_output_file=${toDir.absolutePath}/${file.toRelativeString(fromDir)}"
                        allArgs += file.absolutePath

                        getExecActionFactory().newJavaExecAction().apply {
                            args = allArgs
                            classpath = configuration
                            main = "com.google.javascript.jscomp.CommandLineRunner"
                        }.execute()
                    }
                }
            }
            futures.forEach { it.get() }
        }
    }
}


fun Project.compileJs(configure: CompileJsTask.() -> Unit): CompileJsTask {
    return task("compileJs", CompileJsTask::class) {}.apply(configure)
}
