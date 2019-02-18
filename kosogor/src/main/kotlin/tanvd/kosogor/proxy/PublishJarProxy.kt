package tanvd.kosogor.proxy

import groovy.lang.GroovyObject
import org.gradle.api.Action
import org.gradle.api.Project
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.api.tasks.AbstractCopyTask
import org.gradle.api.tasks.bundling.Jar
import org.gradle.kotlin.dsl.delegateClosureOf
import org.gradle.kotlin.dsl.get
import org.gradle.kotlin.dsl.task
import org.jfrog.gradle.plugin.artifactory.dsl.PublisherConfig
import tanvd.kosogor._artifactory
import tanvd.kosogor._publishing
import tanvd.kosogor._sourceSets
import tanvd.kosogor.utils.applyPluginSafely

class PublishJarProxy {
    data class JarConfig(
            var publication: String = "jarPublication",
            var components: MavenPublication.(Project) -> Unit = { from(it.components.getByName("java")) }
    )

    internal var jarEnable: Boolean = true
    internal val jarConfig = JarConfig()
    fun jar(configure: JarConfig.() -> Unit) {
        jarEnable = true
        jarConfig.configure()
    }


    data class SourcesConfig(
            var publication: String = "sourcesPublication",
            var components: AbstractCopyTask.(Project) -> Unit = { from(it._sourceSets["main"]!!.allSource) }
    )

    internal var sourcesEnable: Boolean = true
    internal val sourcesConfig = SourcesConfig()
    fun sources(configure: SourcesConfig.() -> Unit) {
        sourcesEnable = true
        sourcesConfig.configure()
    }


    data class ArtifactoryConfig(
            var artifactoryUrl: String? = System.getenv("artifactory_url"),
            var artifactoryRepo: String? = System.getenv("artifactory_repo"),
            var artifactoryUser: String? = System.getenv("artifactory_user"),
            var artifactoryKey: String? = System.getenv("artifactory_key"),
            var publishPom: Boolean = true
    )

    internal var artifactoryEnable: Boolean = false
    internal val artifactoryConfig = ArtifactoryConfig()
    fun artifactory(configure: ArtifactoryConfig.() -> Unit) {
        artifactoryEnable = true
        artifactoryConfig.configure()
    }
}

fun Project.publishJar(configure: PublishJarProxy.() -> Unit): PublishJarProxy {
    val config = PublishJarProxy().apply { configure() }

    if (config.sourcesEnable) {
        task<Jar>(config.sourcesConfig.publication) {
            archiveClassifier.set("sources")
            config.sourcesConfig.components(this, project)
        }
    }

    if (config.jarEnable) {
        applyPluginSafely("maven-publish")
        _publishing {
            publications.create(
                    config.jarConfig.publication,
                    MavenPublication::class.java,
                    Action<MavenPublication> { t ->
                        config.jarConfig.components(t, project)
                        if (config.sourcesEnable) {
                            t.artifact(tasks[config.sourcesConfig.publication])
                        }
                    })
        }
    }

    if (config.artifactoryEnable) {
        applyPluginSafely("com.jfrog.artifactory")
        _artifactory {
            setContextUrl(config.artifactoryConfig.artifactoryUrl)

            publish(delegateClosureOf<PublisherConfig> {
                repository(delegateClosureOf<GroovyObject> {
                    setProperty("repoKey", config.artifactoryConfig.artifactoryRepo)
                    setProperty("username", config.artifactoryConfig.artifactoryUser)
                    setProperty("password", config.artifactoryConfig.artifactoryKey)
                    setProperty("maven", true)
                })
                defaults(delegateClosureOf<GroovyObject> {
                    setProperty("publishArtifacts", true)
                    setProperty("publishPom", config.artifactoryConfig.publishPom)
                    invokeMethod("publications", config.jarConfig.publication)
                })
            })
        }
    }
    return config
}
