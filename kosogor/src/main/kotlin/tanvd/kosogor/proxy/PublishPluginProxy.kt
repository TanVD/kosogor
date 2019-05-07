package tanvd.kosogor.proxy

import org.gradle.api.Project
import org.gradle.kotlin.dsl.invoke
import tanvd.kosogor.utils.*

/** Configuration of `publishPlugin` facade */
data class PublishPluginConfig(
        /** ID of plugin to publish */
        var id: String? = null,
        /** Name that will be displayed on Gradle plugins portal */
        var displayName: String? = null,
        /** Version of plugin. By default, equals to version of project. */
        var version: String? = null,
        /** Class implementing plugin interface */
        var implementationClass: String? = null,
        /** Additional information on package */
        val info: Information = Information()) {
    data class Information(var website: String? = null, var vcsUrl: String? = null,
                           var tags: ArrayList<String> = ArrayList(), var description: String? = null)

    fun info(configure: Information.() -> Unit) {
        info.configure()
    }
}

/**
 * Provides a simple interface to Gradle plugin publishing
 *
 * Will apply `java-gradle-plugin` and `com.gradle.plugin-publish` if it is not already applied
 */
fun Project.publishPlugin(configure: PublishPluginConfig.() -> Unit) {
    val config = PublishPluginConfig().apply(configure)
    applyPluginSafely("java-gradle-plugin")

    _gradlePlugin {
        plugins.create(config.id!!).apply {
            id = config.id!!
            implementationClass = config.implementationClass!!
        }
    }

    applyPluginSafely("com.gradle.plugin-publish")
    _pluginBundle {
        website = config.info.website
        vcsUrl = config.info.vcsUrl
        (plugins) {
            config.id!! {
                displayName = config.displayName!!
                description = config.info.description
                tags = config.info.tags
                version = config.version ?: project.version.toString()
            }
        }
    }
}
