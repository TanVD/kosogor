package tanvd.kosogor.utils

import org.gradle.api.Project
import org.gradle.kotlin.dsl.apply

/** Apply plugin if it is not already applied. */
internal fun Project.applyPluginSafely(id: String) {
    if (!plugins.hasPlugin(id)) {
        apply(plugin = id)
    }
}

/** Execute if current project is root */
internal fun Project.ifRootProject(body: Project.() -> Unit) {
    if (this.parent == null) {
        body()
    }
}

