package tanvd.kosogor.terraform.utils

import org.gradle.api.Task

internal object GlobalTask {
    lateinit var tfDownload: Task
    lateinit var tfLintDownload: Task
    lateinit var prepareJars: Task
    lateinit var prepareRemotes: Task
}
