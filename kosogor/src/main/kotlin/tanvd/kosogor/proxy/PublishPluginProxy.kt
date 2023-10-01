package tanvd.kosogor.proxy

import org.gradle.api.Project
import tanvd.kosogor.utils.*

/** Configuration of `publishPlugin` facade */
data class PublishPluginConfig(
        /** ID of plugin to publish */
        var id: String? = null,
        /** Name that will be displayed on Gradle plugins portal */
        var displayName: String? = null,
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
 * Will apply `com.gradle.plugin-publish` if it is not already applied
 */
fun Project.publishPlugin(configure: PublishPluginConfig.() -> Unit) {
    val config = PublishPluginConfig().apply(configure)
    applyPluginSafely("com.gradle.plugin-publish")
    
    _gradlePlugin {
        website.set(config.info.website)
        vcsUrl.set(config.info.vcsUrl)
        plugins {
            it.create(config.id!!) { plugin ->
                plugin.id = config.id!!
                plugin.displayName = config.displayName!!
                plugin.description = config.info.description

                plugin.implementationClass = config.implementationClass!!
                plugin.tags.set(config.info.tags)
            }
        }
    }
}
