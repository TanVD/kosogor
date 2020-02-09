package tanvd.kosogor.terraform.tasks.operation

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.*
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

    @get:Input
    lateinit var operation: Operation

    @get:Input
    val targets: LinkedHashSet<String> = LinkedHashSet()

    @get:Input
    val parameters = ArrayList<String>()

    @get:InputDirectory
    lateinit var root: File

    @TaskAction
    fun execOperation() {
        CommandLine.executeOrFail(GlobalFile.tfBin.absolutePath, operation.op + targets.map { "-target=$it" } + parameters, root, redirectStdout = true, redirectErr = true)
    }
}
