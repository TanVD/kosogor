package tanvd.kosogor.defaults

import org.gradle.api.Project
import org.gradle.api.tasks.Delete
import java.io.File

fun Project.configureGlobalBuildDir() {
    val globalBuildDir = File(project.rootProject.projectDir, "build")

    project.allprojects {
        buildDir = File(globalBuildDir, project.name)
        cleanTask(buildDir)
    }

    project.cleanTask(globalBuildDir)
}

internal fun Project.cleanTask(projectBuildDir: File) {
    afterEvaluate {
        tasks.findByName("clean")?.let {
            delete(projectBuildDir)
        } ?: tasks.create("clean", Delete::class.java) {
            delete(projectBuildDir)
        }
    }
}


fun Project.alsoClean(vararg dirs: String) = alsoClean(*dirs.map { File(project.projectDir, it) }.toTypedArray())

fun Project.alsoClean(vararg dirs: File) {
    afterEvaluate {
        dirs.forEach { cleanTask(it) }
    }
}
