package tanvd.kosogor.defaults

import org.gradle.api.Project
import org.gradle.api.tasks.wrapper.Wrapper
import org.gradle.api.tasks.wrapper.Wrapper.DistributionType

/** The proxy configuration of `wrapper` */
data class WrapperConfig(var version: String = "5.3.1", var distributionType: DistributionType = DistributionType.ALL,
                         var distributionUrl: String? = null)

/**
 * Configure wrapper task of this project. Should be used only on root project.
 *
 * By default, uses version 5.3.1
 *
 * Used in default chain of Kosogor plugin.
 */
fun Project.configureWrapper(body: WrapperConfig.() -> Unit = {}) {
    val config = WrapperConfig().apply(body)
    (tasks.findByName("wrapper") as Wrapper?)?.apply {
        gradleVersion = config.version
        config.distributionUrl?.let { distributionUrl = it }
        distributionType = config.distributionType
    }
}
