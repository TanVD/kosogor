package tanvd.kosogor.defaults

import org.gradle.api.Project
import org.gradle.plugins.ide.idea.IdeaPlugin
import tanvd.kosogor.utils.applyPluginSafely
import java.io.File

/** Proxy configuration of `idea` plugin */
data class DefaultIdeaConfig(
        val project: Project,
        /** Dirs and files to add to excluded in IDEA */
        val exclude: Set<File> = with(project) {
            files(
                    ".gradle", ".gradle-cache", "gradle", "gradlew", "gradlew.bat",
                    ".idea",
                    "out", "build", "tmp"
            ).toHashSet()
        },

        /** Should IDEA module use global dir for build */
        var inheritOutputDirs: Boolean = true,

        /** Should IDEA download sources for dependencies */
        var isDownloadSources: Boolean = true,

        /** Should IDEA download javadocs for dependencies */
        var isDownloadJavadoc: Boolean = true
)

/**
 * Configure IDEA task of this project. Should be used for root project.
 *
 * By default excludes most of the build and tmp dirs, gradle utility files and so on.
 *
 * Used in default chain of Kosogor plugin.
 */
fun Project.configureIdea(configure: DefaultIdeaConfig.() -> Unit = {}) {
    applyPluginSafely("org.gradle.idea")

    val config = DefaultIdeaConfig(project).apply(configure)

    val plugin = plugins.getPlugin("org.gradle.idea") as IdeaPlugin
    plugin.model.apply {
        module.apply {
            excludeDirs = config.exclude.toSet()
            inheritOutputDirs = config.inheritOutputDirs
            isDownloadSources = config.isDownloadSources
            isDownloadJavadoc = config.isDownloadJavadoc
        }
    }
}
