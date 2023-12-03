package tanvd.kosogor.web.utils

import org.gradle.api.Project
import org.gradle.api.artifacts.Configuration
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.get

/** Apply a plugin if it is not already applied. */
internal fun Project.applyPluginSafely(id: String) {
    if (!plugins.hasPlugin(id)) {
        apply(plugin = id)
    }
}

internal val Project.fullName: String
    get() = ":$name"

fun Project.resolvableConfiguration(configurationName: String): Configuration {
    val newConfigurationName = "version_${name}_$configurationName"
    val initialConfiguration = configurations[configurationName]
    return initialConfiguration.takeIf { it.isCanBeResolved }
        ?: configurations.findByName(newConfigurationName)
        ?: run {
            project.configurations.create(newConfigurationName).apply {
                extendsFrom(initialConfiguration)
                isCanBeResolved = true
            }
        }
}