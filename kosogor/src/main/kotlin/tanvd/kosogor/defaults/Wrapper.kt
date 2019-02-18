package tanvd.kosogor.defaults

import org.gradle.api.Project
import org.gradle.api.tasks.wrapper.Wrapper

/** Create wrapper task for this project */
data class WrapperConfig(var version: String = "5.1.1", var distributionUrl: String? = null)

fun Project.configureWrapper(body: WrapperConfig.() -> Unit = {}) {
    val config = WrapperConfig().apply(body)
    (tasks.findByName("wrapper") as Wrapper?)?.apply {
        gradleVersion = config.version
        config.distributionUrl?.let { distributionUrl = it }
    }
}
