package tanvd.kosogor.utils

import org.gradle.api.Project
import org.gradle.kotlin.dsl.apply

/** Apply plugin if it is not already applied. */
fun Project.applyPluginSafely(id: String) {
    if (!plugins.hasPlugin(id)) {
        apply(plugin = id)
    }
}

/** Execute if current project is root */
fun Project.ifRootProject(body: Project.() -> Unit) {
    if (this.parent == null) {
        body()
    }
}

/** Execute if current project is parent (not root) */
fun Project.ifParentProject(body: Project.() -> Unit) {
    if (this.parent != null) {
        body()
    }
}

val Project.fullName: String
    get() = ":$name"

