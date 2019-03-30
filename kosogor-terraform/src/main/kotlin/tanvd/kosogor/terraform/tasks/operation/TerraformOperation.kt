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
        INIT("init"),
        PLAN("plan"),
        APPLY("apply", "-auto-approve"),
        DESTROY("destroy", "-auto-approve"),
        OUTPUT("output");

        constructor(vararg op: String) : this(op.toList())
    }

    lateinit var operation: Operation
    val targets: LinkedHashSet<String> = LinkedHashSet()
    lateinit var root: File

    @TaskAction
    fun execOperation() {
        CommandLine.execute(GlobalFile.tfBin.absolutePath, operation.op + targets.map { "-target=$it" }, root, redirectStdout = true, redirectErr = true)
    }
}
