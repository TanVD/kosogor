@file:Suppress("FunctionName", "ObjectPropertyName", "SpellCheckingInspection", "UnstableApiUsage")

package tanvd.kosogor.utils

import com.gradle.publish.PluginBundleExtension
import com.jfrog.bintray.gradle.BintrayExtension
import org.gradle.api.Project
import org.gradle.api.internal.HasConvention
import org.gradle.api.plugins.ExtensionAware
import org.gradle.api.publish.PublishingExtension
import org.gradle.kotlin.dsl.getPluginByName
import org.gradle.plugin.devel.GradlePluginDevelopmentExtension
import org.jfrog.gradle.plugin.artifactory.dsl.ArtifactoryPluginConvention

//Generated accessors to use in plugin
val Project._artifactory: ArtifactoryPluginConvention
    get() = ((this as? Project)?.convention ?: (this as HasConvention).convention).getPluginByName("artifactory")

fun Project._artifactory(configure: ArtifactoryPluginConvention.() -> Unit) = configure(_artifactory)

fun Project._publishing(configure: PublishingExtension.() -> Unit) = extensions.configure("publishing", configure)

fun Project._bintray(configure: BintrayExtension.() -> Unit): Unit =
        (this as org.gradle.api.plugins.ExtensionAware).extensions.configure("bintray", configure)

fun Project._pluginBundle(configure: PluginBundleExtension.() -> Unit) = (this as ExtensionAware).extensions.configure("pluginBundle", configure)

fun Project._gradlePlugin(configure: GradlePluginDevelopmentExtension.() -> Unit): Unit = (this as ExtensionAware).extensions.configure("gradlePlugin", configure)

