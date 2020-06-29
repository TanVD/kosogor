package tanvd.kosogor.terraform

import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.artifacts.Configuration
import tanvd.kosogor.terraform.tasks.lint.LintRootTask
import tanvd.kosogor.terraform.tasks.operation.TerraformOperation
import tanvd.kosogor.terraform.utils.Archive
import java.io.File
import java.net.URL


@DslMarker
annotation class TerraformDSLTag

class TerraformDsl(var project: Project? = null) {
    data class Config(
            var tfVersion: String = "0.11.11",
            var tfLintVersion: String = "0.17.0",
            var tfLintConfigPath: String? = null,
            var awsProvider: String = "1.60.0",
            var awsRegion: String? = null,
            var awsProfile: String? = null
    ) {
        internal val tfVersionInt: Int
            get() = tfVersion.replace(".", "").toInt()
    }

    internal val config = Config()

    @TerraformDSLTag
    fun config(configure: Config.() -> Unit) {
        config.configure()
    }

    data class Parameters(
            var init: Iterable<String>? = null,
            var plan: Iterable<String>? = null,
            var apply: Iterable<String>? = null,
            var destroy: Iterable<String>? = null,
            var output: Iterable<String>? = null,
            var lint: Iterable<String>? = null
    )

    internal val parameters = Parameters()

    @TerraformDSLTag
    fun parameters(configure: Parameters.() -> Unit) {
        parameters.configure()
    }

    data class ModulesCollector(
            var directory: String? = null
    )

    internal var collector = ModulesCollector()

    /** Configuration of modules collecting */
    @TerraformDSLTag
    fun collect(configure: ModulesCollector.() -> Unit) {
        collector.configure()
    }

    data class Publisher(
            var serverUrl: String? = System.getenv("server_url"),
            var repository: String? = System.getenv("repository"),
            var username: String? = System.getenv("username"),
            var secretKey: String? = System.getenv("secret_key"),
            var ignoreErrorIfArtifactExists: Boolean = true,
            var overwriteSnapshots: Boolean = true
    )

    var enablePublisher = false

    internal val publisher = Publisher()

    @TerraformDSLTag
    fun publish(configure: Publisher.() -> Unit) {
        enablePublisher = true
        publisher.configure()
    }

    data class Linter(var linter: LinterType = LinterType.Terraform) {
        enum class LinterType {
            Terraform,
            TfLint
        }
    }

    internal val linter = Linter()

    @TerraformDSLTag
    fun lint(configure: Linter.() -> Unit) {
        linter.configure()
    }

    data class Validater(val cacheInitPlugins: Boolean = true)

    internal val validater = Validater()

    @TerraformDSLTag
    fun validate(configure: Validater.() -> Unit) {
        validater.configure()
    }

    data class Artifacts(val jars: LinkedHashSet<Jars> = LinkedHashSet(), val remotes: LinkedHashSet<Remote> = LinkedHashSet()) {
        data class Jars(val configuration: Configuration, val destDir: File)

        @TerraformDSLTag
        fun jars(configuration: Configuration, destDir: File) {
            this.jars += Jars(configuration, destDir)
        }

        data class Remote(val url: URL, val destDir: File, val archive: Archive? = null, val filterToRoot: (File) -> Boolean)

        @TerraformDSLTag
        fun remote(url: URL, destDir: File, archive: Archive? = null, filterToRoot: (File) -> Boolean = { true }) {
            this.remotes += Remote(url, destDir, archive, filterToRoot)
        }
    }

    internal val artifacts = Artifacts()

    @TerraformDSLTag
    fun artifacts(configure: Artifacts.() -> Unit) {
        artifacts.configure()
    }


    @TerraformDSLTag
    fun root(name: String, dir: File, enableDestroy: Boolean = false, targets: LinkedHashSet<String> = LinkedHashSet(), workspace: String? = null) {
        val lint = project!!.tasks.create("$name.lint", LintRootTask::class.java) { task ->
            task.group = "terraform.$name"
            task.description = "Lint root $name"

            task.root = dir
        }

        fun terraformOperation(operation: TerraformOperation.Operation, parameters: Iterable<String>?, vararg depends: Task): Task {
            var taskName = "$name.${operation.name.toLowerCase()}"
            if (operation == TerraformOperation.Operation.WORKSPACE) {
                taskName += "." + parameters!!.first()
            }
            return project!!.tasks.create(taskName, TerraformOperation::class.java) { task ->
                task.dependsOn(depends)
                task.group = "terraform.$name"
                task.description = "${operation.name.toLowerCase().capitalize()} root $name"

                task.operation = operation
                if (task.operation != TerraformOperation.Operation.WORKSPACE) {
                    task.targets.addAll(targets)
                }
                task.parameters.addAll(parameters ?: emptyList())
                task.root = dir
            }
        }

        val init = terraformOperation(TerraformOperation.Operation.INIT, parameters.init)

        val setup = if (workspace == null) {
            init
        } else {
            val workspaceSelect = terraformOperation(TerraformOperation.Operation.WORKSPACE, listOf("select", workspace), init)
            val workspaceNew = terraformOperation(TerraformOperation.Operation.WORKSPACE, listOf("new", workspace), init)
            val workspaceDelete = terraformOperation(TerraformOperation.Operation.WORKSPACE, listOf("delete", workspace), init)

            workspaceSelect
        }

        val plan = terraformOperation(TerraformOperation.Operation.PLAN, parameters.plan, setup)
        val apply = terraformOperation(TerraformOperation.Operation.APPLY, parameters.apply, setup)
        if (enableDestroy) {
            val destroy = terraformOperation(TerraformOperation.Operation.DESTROY, parameters.destroy, setup)
        }
        val output = terraformOperation(TerraformOperation.Operation.OUTPUT, parameters.output, setup)
    }
}

internal val terraformDsl = TerraformDsl()

@TerraformDSLTag
fun Project.terraform(configure: TerraformDsl.() -> Unit) {
    terraformDsl.project = this
    terraformDsl.configure()
}
