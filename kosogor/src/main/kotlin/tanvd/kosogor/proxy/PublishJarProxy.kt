package tanvd.kosogor.proxy

import org.gradle.api.Project
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.api.tasks.AbstractCopyTask
import org.gradle.api.tasks.bundling.Jar
import org.gradle.kotlin.dsl.*
import tanvd.kosogor.utils.*

/** Configuration of `publishJar` facade */
class PublishJarProxy {
    data class JarConfig(
        /** Components to add to main jar */
        var components: MavenPublication.(Project) -> Unit = { from(it.components.getByName("java")) }
    )

    internal val jarConfig = JarConfig()
    fun jar(configure: JarConfig.() -> Unit) {
        jarConfig.apply(configure)
    }

    data class SourcesConfig(
        /** Name of jar task for sources to create */
        var task: String = "sourcesJar",
        /** Components to add to sources jar */
        var components: AbstractCopyTask.(Project) -> Unit = { from(it._sourceSets["main"]!!.allSource) }
    )

    var enableSources: Boolean = true
    internal val sourcesConfig = SourcesConfig()
    fun sources(configure: SourcesConfig.() -> Unit) {
        enableSources = true
        sourcesConfig.configure()
    }

    data class PublicationConfig(
        /** Name of artifact to publish */
        var artifactId: String? = null,
        /** Name of maven publication to create */
        var publicationName: String = "jarPublication"
    )

    private var _enablePublication: Boolean? = null

    var enablePublication: Boolean
        get() = _enablePublication ?: true
        set(value) { _enablePublication = value }

    internal val publicationConfig = PublicationConfig()
    fun publication(configure: PublicationConfig.() -> Unit) {
        if (_enablePublication == null)
            _enablePublication = true
        publicationConfig.configure()
    }


    data class ArtifactoryConfig(
        /**
         * URL of artifactory server
         * If not set, will be taken from System environment param `artifactory_url`
         */
        var serverUrl: String? = System.getenv("artifactory_url"),
        /**
         * Maven repo on artifactory server
         * If not set, will be taken from System environment param `artifactory_repo`
         */
        var repository: String? = System.getenv("artifactory_repo"),
        /**
         * Artifactory user name to use
         * If not set, will be taken from System environment param `artifactory_user`
         */
        var username: String? = System.getenv("artifactory_user"),
        /**
         * Artifactory secret key to use
         * If not set, will be taken from System environment param `artifactory_key`
         */
        var secretKey: String? = System.getenv("artifactory_key"),
        /** Should the published artifact include pom.xml */
        var publishPom: Boolean = true
    )

    var enableArtifactory: Boolean = false
    internal val artifactoryConfig = ArtifactoryConfig()
    fun artifactory(configure: ArtifactoryConfig.() -> Unit) {
        enableArtifactory = true
        artifactoryConfig.configure()
    }
}

class PublishMultipleJarsConfig {
    internal val configs = mutableListOf<PublishJarProxy>()
    internal var enableArtifactory: Boolean = false
    internal var artifactoryConfig = PublishJarProxy.ArtifactoryConfig()

    fun artifactory(configure: PublishJarProxy.ArtifactoryConfig.() -> Unit) {
        enableArtifactory = true
        artifactoryConfig.configure()
    }

    fun publishJar(configure: PublishJarProxy.() -> Unit) {
        configs += PublishJarProxy().apply { configure() }.also {
            check(!it.enableArtifactory) {
                "Artifactory should be configured on publishJars level"
            }
        }
    }
}

fun Project.publishJars(configure: PublishMultipleJarsConfig.() -> Unit) {
    val multiConfig = PublishMultipleJarsConfig().apply { configure() }
    multiConfig.configs.forEach { config ->
        if (config.enableSources) {
            task<Jar>(config.sourcesConfig.task) {
                archiveClassifier.set("sources")
                config.sourcesConfig.components(this, project)
            }
        }

        if (config.enablePublication) {
            applyPluginSafely("maven-publish")
            _publishing {
                publications.create(config.publicationConfig.publicationName, MavenPublication::class.java) { t ->
                    t.artifactId = config.publicationConfig.artifactId ?: project.name
                    config.jarConfig.components(t, project)
                    if (config.enableSources) {
                        t.artifact(tasks[config.sourcesConfig.task])
                    }
                }
            }
        }
    }

    val allPublications = multiConfig.configs.filter {
        it.enablePublication
    }
    if (multiConfig.enableArtifactory && allPublications.isNotEmpty()) {
        applyPluginSafely("maven-publish")
        applyPluginSafely("com.jfrog.artifactory")
        _artifactory {
            val artifactoryConfig = multiConfig.artifactoryConfig

            publish {
                it.contextUrl = artifactoryConfig.serverUrl
                it.repository { r ->
                    r.repoKey = artifactoryConfig.repository
                    r.username = artifactoryConfig.username
                    r.password = artifactoryConfig.secretKey
                }
                it.defaults { at ->
                    at.setPublishArtifacts(true)
                    at.setPublishPom(artifactoryConfig.publishPom)

                    val publicationNames = allPublications.map { it.publicationConfig.publicationName  }.toTypedArray()
                    at.publications(*publicationNames)
                }
            }
        }
    }
}

/**
 * Provides a simple interface to jar, maven-publish and artifactory plugin through proxy
 *
 * Will apply `maven-publish` if it is not already applied and `jar { ... }` is used
 * Will apply `com.jfrog.artifactory` if it is not already applied and `artifactory { ... }` is used
 */
fun Project.publishJar(configure: PublishJarProxy.() -> Unit): PublishJarProxy {
    val config = PublishJarProxy().apply { configure() }

    publishJars {
        configs.add(config)
        enableArtifactory = config.enableArtifactory
        artifactoryConfig = config.artifactoryConfig
    }

    return config
}
