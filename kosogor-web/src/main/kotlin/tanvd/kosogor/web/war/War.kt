package tanvd.kosogor.web.war

import org.gradle.api.Project
import org.gradle.api.tasks.CacheableTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.bundling.War
import org.gradle.kotlin.dsl.task
import tanvd.kosogor.utils.FilesConfig
import tanvd.kosogor.utils.applyPluginSafely
import java.io.File

/**
 * Facade for war task.
 *
 * It offers simple Kotlin-DSL facade based on FilesConfig extensions
 */
@CacheableTask
open class WarProxy : War() {
    init {
        group = "war"
    }


    /** Should war name include project version */
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


    class ClasspathConfig(val include: LinkedHashSet<File> = LinkedHashSet(), val exclude: LinkedHashSet<File> = LinkedHashSet())

    /** Configure classpath of war */
    fun classpath(configure: ClasspathConfig.() -> Unit) {
        doFirst {
            with(ClasspathConfig().apply(configure)) {
                setClasspath(classpath!!.files.plus(include).subtract(exclude))
            }
        }
    }

    /** Configure files in META-INF war directory */
    fun metaRoot(configure: FilesConfig.() -> Unit) {
        metaInf {
            FilesConfig(project).apply(configure).apply(this)
        }
    }

    /** Configure files in WEB-INF war directory */
    fun webRoot(configure: FilesConfig.() -> Unit) {
        webInf {
            FilesConfig(project).apply(configure).apply(this)
        }
    }

    /** Configure files in root war directory */
    fun static(configure: FilesConfig.() -> Unit) {
        FilesConfig(project).apply(configure).apply(super.getRootSpec())
    }
}

/** Create WarProxy task with specified name (by default `createWar`) and specified configuration */
fun Project.createWar(name: String = "createWar", configure: WarProxy.() -> Unit): WarProxy {
    applyPluginSafely("war")
    return task(name, WarProxy::class) {
        configure()
    }
}
