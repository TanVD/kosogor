package tanvd.kosogor.proxy

import groovy.lang.GroovyObject
import org.gradle.api.Action
import org.gradle.api.Project
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.api.tasks.AbstractCopyTask
import org.gradle.api.tasks.bundling.Jar
import org.gradle.kotlin.dsl.*
import org.jfrog.gradle.plugin.artifactory.dsl.PublisherConfig
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

    var enablePublication: Boolean = true
    internal val publicationConfig = PublicationConfig()
    fun publication(configure: PublicationConfig.() -> Unit) {
        enablePublication = true
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

/**
 * Provides a simple interface to jar, maven-publish and artifactory plugin through proxy
 *
 * Will apply `maven-publish` if it is not already applied and `jar { ... }` is used
 * Will apply `com.jfrog.artifactory` if it is not already applied and `artifactory { ... }` is used
 */
fun Project.publishJar(configure: PublishJarProxy.() -> Unit): PublishJarProxy {
    val config = PublishJarProxy().apply { configure() }

    if (config.enableSources) {
        task<Jar>(config.sourcesConfig.task) {
            archiveClassifier.set("sources")
            config.sourcesConfig.components(this, project)
        }
    }

    if (config.enablePublication) {
        applyPluginSafely("maven-publish")
        _publishing {
            publications.create(
                    config.publicationConfig.publicationName,
                    MavenPublication::class.java,
                    Action<MavenPublication> { t ->
                        t.artifactId = config.publicationConfig.artifactId ?: project.name

                        config.jarConfig.components(t, project)
                        if (config.enableSources) {
                            t.artifact(tasks[config.sourcesConfig.task])
                        }
                    })
        }
    }

    if (config.enableArtifactory) {
        applyPluginSafely("com.jfrog.artifactory")
        _artifactory {
            setContextUrl(config.artifactoryConfig.serverUrl)

            publish(delegateClosureOf<PublisherConfig> {
                repository(delegateClosureOf<GroovyObject> {
                    setProperty("repoKey", config.artifactoryConfig.repository)
                    setProperty("username", config.artifactoryConfig.username)
                    setProperty("password", config.artifactoryConfig.secretKey)
                    setProperty("maven", true)
                })
                defaults(delegateClosureOf<GroovyObject> {
                    setProperty("publishArtifacts", true)
                    setProperty("publishPom", config.artifactoryConfig.publishPom)
                    invokeMethod("publications", config.publicationConfig.publicationName)
                })
            })
        }
    }

    return config
}
