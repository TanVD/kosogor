@file:Suppress("FunctionName", "ObjectPropertyName", "SpellCheckingInspection", "UnstableApiUsage")

package tanvd.kosogor.utils

import org.gradle.api.Project
import org.gradle.api.plugins.ExtraPropertiesExtension
import org.gradle.api.publish.PublishingExtension
import org.gradle.api.tasks.SourceSetContainer
import org.gradle.kotlin.dsl.getByName
import org.gradle.plugin.devel.GradlePluginDevelopmentExtension
import org.jfrog.gradle.plugin.artifactory.dsl.ArtifactoryPluginConvention
import org.jfrog.gradle.plugin.artifactory.utils.ExtensionsUtils

//Generated accessors to use in a plugin
internal val Project._artifactory: ArtifactoryPluginConvention
    get() = ExtensionsUtils.getOrCreateArtifactoryExtension(project);

internal fun Project._artifactory(configure: ArtifactoryPluginConvention.() -> Unit) {
    configure(_artifactory)
}

internal fun Project._publishing(configure: PublishingExtension.() -> Unit) {
    extensions.configure("publishing", configure)
}

internal fun Project._gradlePlugin(configure: GradlePluginDevelopmentExtension.() -> Unit) {
    extensions.configure("gradlePlugin", configure)
}

internal inline fun <reified T : Any> Project.extByName(name: String): T = extensions.getByName<T>(name)

internal inline fun <reified T : Any> Project.ext(name: String) = _ext[name] as T

internal val Project._ext: ExtraPropertiesExtension
    get() = extByName("ext")

internal val Project._sourceSets: SourceSetContainer
    get() = extByName("sourceSets")
