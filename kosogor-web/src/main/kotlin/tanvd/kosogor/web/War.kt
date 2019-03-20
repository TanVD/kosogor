package tanvd.kosogor.web

import org.gradle.api.Project
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.bundling.War
import org.gradle.kotlin.dsl.task
import tanvd.kosogor.utils.FilesConfig
import tanvd.kosogor.utils.applyPluginSafely
import java.io.File

open class GenerateWarTask : War() {
    init {
        group = "war"
    }

    class ClasspathConfig(val project: Project,
                          val include: LinkedHashSet<File> = LinkedHashSet(),
                          val exclude: LinkedHashSet<File> = LinkedHashSet())

    @get:Input
    var addVersion: Boolean
        set(value) {
            archiveVersion.set(if (value) {
                project.version.toString()
            } else {
                ""
            })
        }
        get() = archiveVersion.get() != ""

    fun classpath(configure: ClasspathConfig.() -> Unit) {
        doFirst {
            val configClasspath = ClasspathConfig(project).apply(configure)
            setClasspath(classpath!!.files.plus(configClasspath.include).subtract(configClasspath.exclude))
        }
    }

    fun metaRoot(configure: FilesConfig.() -> Unit) {
        metaInf {
            FilesConfig(project).apply(configure).apply(this)
        }
    }

    fun webRoot(configure: FilesConfig.() -> Unit) {
        webInf {
            FilesConfig(project).apply(configure).apply(this)
        }
    }

    fun static(configure: FilesConfig.() -> Unit) {
        FilesConfig(project).apply(configure).apply(super.getRootSpec())
    }
}

fun Project.generateWar(name: String = "generateWar", configure: GenerateWarTask.() -> Unit): GenerateWarTask {
    applyPluginSafely("war")
    return task(name, GenerateWarTask::class) {
        configure()
    }
}
