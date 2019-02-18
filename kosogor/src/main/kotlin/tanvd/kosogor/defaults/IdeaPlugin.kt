package tanvd.kosogor.defaults

import org.gradle.api.Project
import org.gradle.plugins.ide.idea.IdeaPlugin
import tanvd.kosogor.utils.applyPluginSafely

class DefaultIdeaConfig(project: Project) {
    val excludeDirs = with(project) {
        files(".gradle", ".gradle-cache", "gradle", "gradlew", "gradlew.bat", "gradle.properties",
                ".idea",
                "out", "build", "tmp"
        ).toHashSet()
    }

    var inheritOutputDirs = true
    var isDownloadSources = true
    var isDownloadJavadoc = true
}

fun Project.configureIdea(configure: DefaultIdeaConfig.() -> Unit = {}) {
    applyPluginSafely("org.gradle.idea")

    val config = DefaultIdeaConfig(project).apply(configure)

    val plugin = plugins.getPlugin("org.gradle.idea") as IdeaPlugin
    plugin.model.apply {
        module.apply {
            excludeDirs = config.excludeDirs.toSet()
            inheritOutputDirs = config.inheritOutputDirs
            isDownloadSources = config.isDownloadSources
            isDownloadJavadoc = config.isDownloadJavadoc
        }
    }
}
