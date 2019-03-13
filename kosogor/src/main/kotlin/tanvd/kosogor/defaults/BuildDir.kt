package tanvd.kosogor.defaults

import org.gradle.api.Project
import org.gradle.api.tasks.Delete
import tanvd.kosogor.utils.ifRootProject
import java.io.File

/**
 * Configure build dir.
 *
 * By default overrides projects to use dir with name ${project.name} inside global build dir.
 *
 * Also adds build dir to clean task or create it, if it does not exist.
 *
 * Used in default chain of Kosogor plugin.
 */
fun Project.configureGlobalBuildDir() {
    val globalBuildDir = File(project.rootProject.projectDir, "build")

    buildDir = File(globalBuildDir, project.name)
    cleanTask(buildDir)

    ifRootProject {
        project.cleanTask(globalBuildDir)
    }
}

internal fun Project.cleanTask(projectBuildDir: File) {
    afterEvaluate {
        tasks.findByName("clean")?.let {
            (it as? Delete)?.delete(projectBuildDir)
        } ?: tasks.create("clean", Delete::class.java) {
            it.delete(projectBuildDir)
        }
    }
}

/** Add dirs to clean task of this project */
fun Project.alsoClean(vararg dirs: String) = alsoClean(*dirs.map { File(project.projectDir, it) }.toTypedArray())

/** Add dirs to clean task of this project */
fun Project.alsoClean(vararg dirs: File) {
    afterEvaluate {
        dirs.forEach { cleanTask(it) }
    }
}
