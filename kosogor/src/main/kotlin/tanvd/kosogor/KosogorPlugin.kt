package tanvd.kosogor

import org.gradle.api.Plugin
import org.gradle.api.Project
import tanvd.kosogor.defaults.*
import tanvd.kosogor.utils.ifRootProject

/**
 * Implementation of Kosogor plugin.
 *
 * It configures by default: IDEA, global build dir for all subprojects and Wrapper
 */
class KosogorPlugin : Plugin<Project> {
    override fun apply(project: Project) {
        with(project) {
            ifRootProject {
                configureIdea()
                configureWrapper()
            }
            configureGlobalBuildDir()
        }
    }
}
