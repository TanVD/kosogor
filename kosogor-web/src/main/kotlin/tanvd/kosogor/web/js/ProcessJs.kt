package tanvd.kosogor.web.js

import org.gradle.api.DefaultTask
import org.gradle.api.Project
import org.gradle.api.tasks.*
import org.gradle.kotlin.dsl.task
import java.io.File

@CacheableTask
open class ProcessJsTask : DefaultTask() {
    init {
        group = "js"
    }

    @get:Input
    val replaceDictionary = HashMap<String, String>()

    @get:InputFiles
    @get:PathSensitive(PathSensitivity.RELATIVE)
    val from: List<File>
        get() = includeFiles.map { it.first }

    @get:OutputFiles
    @get:PathSensitive(PathSensitivity.RELATIVE)
    val to: List<File>
        get() = includeFiles.map { it.second }

    private val includeFiles = HashSet<Pair<File, File>>()
    fun include(vararg file: Pair<File, File>) {
        includeFiles += file
    }

    @TaskAction
    fun compileJs() {
        for ((from, to) in includeFiles) {
            var res = from.readText()
            replaceDictionary.forEach { (initial, replaced) ->
                res = res.replace(initial, replaced)
            }
            to.parentFile.mkdirs()
            to.writeText(res)
        }
    }
}

fun Project.processJs(configure: ProcessJsTask.() -> Unit): ProcessJsTask {
    return task("processJs", ProcessJsTask::class) {}.apply(configure)
}
