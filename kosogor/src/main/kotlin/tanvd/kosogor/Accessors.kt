@file:Suppress("FunctionName", "ObjectPropertyName", "SpellCheckingInspection", "UnstableApiUsage")

package tanvd.kosogor

import org.gradle.api.Project
import org.gradle.api.internal.HasConvention
import org.gradle.api.publish.PublishingExtension
import org.gradle.api.tasks.SourceSetContainer
import org.gradle.kotlin.dsl.getByName
import org.gradle.kotlin.dsl.getPluginByName
import org.jfrog.gradle.plugin.artifactory.dsl.ArtifactoryPluginConvention


private inline fun <reified T : Any> Project.extByName(name: String): T = extensions.getByName<T>(name)

val Project._artifactory: ArtifactoryPluginConvention
    get() = ((this as? Project)?.convention ?: (this as HasConvention).convention).getPluginByName("artifactory")

fun Project._artifactory(configure: ArtifactoryPluginConvention.() -> Unit) = configure(_artifactory)

val Project._sourceSets: SourceSetContainer
    get() = extByName("sourceSets")

fun Project._publishing(configure: PublishingExtension.() -> Unit) = extensions.configure("publishing", configure)

