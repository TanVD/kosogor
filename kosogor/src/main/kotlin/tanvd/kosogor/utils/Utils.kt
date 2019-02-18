package tanvd.kosogor.utils

import org.gradle.api.Project
import org.gradle.kotlin.dsl.apply

fun Project.applyPluginSafely(id: String) {
    if (!plugins.hasPlugin(id)) {
        apply(plugin = id)
    }
}
