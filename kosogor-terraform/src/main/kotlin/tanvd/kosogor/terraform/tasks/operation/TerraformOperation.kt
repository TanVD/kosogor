package tanvd.kosogor.terraform.tasks.operation

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction
import tanvd.kosogor.terraform.utils.*
import java.io.File


/**
 * TerraformOperation task executes specified operation on terraform code
 *
 * Note: Apply and destroy will not require approve in a console,
 * terraform plugin passes to it `-auto-approve`
 */
open class TerraformOperation : DefaultTask() {
    init {
        outputs.upToDateWhen { false }
        project.afterEvaluate {
            dependsOn(GlobalTask.tfDownload, GlobalTask.prepareJars, GlobalTask.prepareRemotes)
        }
    }

    enum class Operation(val op: List<String>) {
        INIT(listOf("init")),
        PLAN(listOf("plan")),
        APPLY(listOf("apply", "-auto-approve")),
        DESTROY(listOf("destroy", "-auto-approve")),
        OUTPUT(listOf("output"));
    }

    lateinit var operation: Operation
    lateinit var targets: LinkedHashSet<String>
    lateinit var root: File

    @TaskAction
    fun execOperation() {
        targets = LinkedHashSet(targets.map { "-target=$it" })

        val args = operation.op + targets

        CommandLine.execute(GlobalFile.tfBin.absolutePath, args, root, redirectStdout = true, redirectErr = true)
    }
}
