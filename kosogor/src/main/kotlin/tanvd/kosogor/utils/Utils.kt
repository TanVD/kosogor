package tanvd.kosogor.utils

import org.gradle.api.Project
import org.gradle.kotlin.dsl.apply

val Project.fullName: String
    get() = ":$name"

fun Project.applyPluginSafely(id: String) {
    if (!plugins.hasPlugin(id)) {
        apply(plugin = id)
    }
}
