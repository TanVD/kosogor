package tanvd.kosogor.proxy

import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import org.gradle.api.Project
import org.gradle.api.publish.maven.MavenArtifact
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.kotlin.dsl.withType
import tanvd.kosogor.utils.applyPluginSafely
import java.io.File


class ShadowJarProxy(private val project: Project) {
    data class ShadowJarConfig(
            var archiveName: String? = null,
            var mainClass: String? = null,
            var path: File? = null
    )

    fun artifact(pub: MavenPublication): MavenArtifact = pub.artifact(project.tasks.withType<ShadowJar>().single())

    internal val jarConfig = ShadowJarConfig()
    fun jar(configure: ShadowJarConfig.() -> Unit) {
        jarConfig.configure()
    }
}

fun Project.shadowJar(configure: ShadowJarProxy.() -> Unit): ShadowJarProxy {
    val config = ShadowJarProxy(this).apply { configure() }

    applyPluginSafely("com.github.johnrengelman.shadow")

    val shadowJars = tasks.withType<ShadowJar> {
        config.jarConfig.archiveName?.let {
            archiveFileName.set(it)
        }
        config.jarConfig.mainClass?.let { cls ->
            manifest {
                it.attributes(mapOf("Main-Class" to cls))
            }
        }
        config.jarConfig.path?.let {
            destinationDirectory.set(it)
        } ?: destinationDirectory.set(File(project.rootProject.projectDir, "build/shadow"))
    }

    return config
}
