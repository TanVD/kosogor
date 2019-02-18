package tanvd.kosogor

import org.gradle.api.Plugin
import org.gradle.api.Project
import tanvd.kosogor.defaults.configureGlobalBuildDir
import tanvd.kosogor.defaults.configureIdea
import tanvd.kosogor.defaults.configureWrapper

/**
 * Implementation of Kosogor plugin.
 *
 * It configures by default: IDEA, global build dir for all subprojects and Wrapper
 */
class KosogorPlugin : Plugin<Project> {
    override fun apply(project: Project) {
        project.configureIdea()
        project.configureGlobalBuildDir()
        project.configureWrapper()
    }
}
