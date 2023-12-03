package tanvd.kosogor.web.war

import org.gradle.api.Project
import org.gradle.api.file.DuplicatesStrategy
import org.gradle.api.tasks.CacheableTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.bundling.War
import org.gradle.kotlin.dsl.task
import tanvd.kosogor.web.utils.FilesConfig
import tanvd.kosogor.web.utils.applyPluginSafely
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

    class ClasspathConfig(val include: LinkedHashSet<File> = LinkedHashSet(), val exclude: LinkedHashSet<File> = LinkedHashSet())

    /** Configure classpath of war */
    fun classpath(configure: ClasspathConfig.() -> Unit) {
        doFirst {
            with(ClasspathConfig().apply(configure)) {
                setClasspath(classpath!!.plus(include).subtract(exclude))
            }
        }
    }

    /** Configure files in META-INF war directory */
    fun metaRoot(configure: FilesConfig.() -> Unit) {
        metaInf {
            it.duplicatesStrategy = DuplicatesStrategy.EXCLUDE
            FilesConfig(project).apply(configure).apply(it)
        }
    }

    /** Configure files in WEB-INF war directory */
    fun webRoot(configure: FilesConfig.() -> Unit) {
        webInf {
            it.duplicatesStrategy = DuplicatesStrategy.EXCLUDE
            FilesConfig(project).apply(configure).apply(it)
        }
    }

    /** Configure files in root war directory */
    fun static(configure: FilesConfig.() -> Unit) {
        duplicatesStrategy = DuplicatesStrategy.EXCLUDE
        FilesConfig(project).apply(configure).apply(rootSpec)
    }
}

/** Create WarProxy task with specified name (by default `createWar`) and specified configuration */
fun Project.createWar(name: String = "createWar", configure: WarProxy.() -> Unit): WarProxy {
    if (!plugins.hasPlugin("war")) {
        applyPluginSafely("war")
        afterEvaluate {
            tasks.getByPath("war").enabled = false
        }
    }
    return task(name, WarProxy::class) {
        configure()
        duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    }
}
