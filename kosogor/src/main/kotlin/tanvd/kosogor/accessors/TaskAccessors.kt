package tanvd.kosogor.accessors

import org.gradle.api.Project
import org.gradle.jvm.tasks.Jar

fun Project.jar(name: String, body: Jar.() -> Unit): Jar {
    return (tasks.findByName(name) as? Jar)?.apply(body)
            ?: tasks.create(name, Jar::class.java).apply(body)
}
