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

/**
 * JS compile task wrapping closure-compiler calls
 *
 * It will compile JS files in `fromDir` to `toDir` via closure-compiler.
 *
 * Compilation is performed in parallel on all available cores.
 */
@CacheableTask
open class CompileJS : DefaultTask() {
    init {
        group = "js"
    }

    companion object {
        private val threadPool = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors() + 1)
    }

    /** Directory from which JS files should be taken by compiler */
    @get:InputDirectory
    @get:PathSensitive(PathSensitivity.RELATIVE)
    lateinit var fromDir: File

    /** Directory to which compiler should output files */
    @get:OutputDirectory
    lateinit var toDir: File

    /** Configuration which should include closure-compiler jar */
    @get:Classpath
    lateinit var configuration: Configuration

    /**
     * Extensions with which files will be compiled
     * By default it is `js`
     */
    @get:Input
    val includeExtensions = hashSetOf(".js")

    /** Files that should be excluded from compilation */
    @get:InputFiles
    @get:PathSensitive(PathSensitivity.RELATIVE)
    val excludeFiles = HashSet<File>()

    /**
     * Extensions with which files will be ignored
     * By default it is `.min.js`
     */
    @get:Input
    val excludeExtensions = hashSetOf(".min.js")

    /**
     * Compilation level of closure compiler.
     *
     * By default it is `SIMPLE_OPTIMIZATIONS`
     */
    @get:Input
    var compilationLevel: String = "SIMPLE_OPTIMIZATIONS"

    /**
     * Language level of input code.
     *
     * By default it is `ECMASCRIPT6`
     */
    @get:Input
    var fromLang: String = "ECMASCRIPT6"

    /**
     * Language level of input code.
     *
     * By default it is `ECMASCRIPT5`
     */
    @get:Input
    var toLang: String = "ECMASCRIPT5"

    /** Warning level that should be used by `closure-compiler` during compilation */
    @get:Input
    var warningLevel: String = "QUIET"

    /** Main class to pass to JVM during call of closure-compiler jar */
    @get:Input
    var compilerMainClass: String = "com.google.javascript.jscomp.CommandLineRunner"

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
                            main = compilerMainClass
                        }.execute()
                    }
                }
            }
            futures.forEach { it.get() }
        }
    }
}

/** Create CompileJS task with specified name (or `compileJS` by default) and configuration */
fun Project.compileJs(name: String = "compileJS", configure: CompileJS.() -> Unit): CompileJS {
    return task(name, CompileJS::class) { apply(configure) }
}
