package tanvd.kosogor.terraform

import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.artifacts.Configuration
import tanvd.kosogor.terraform.tasks.lint.LintRootTask
import tanvd.kosogor.terraform.tasks.operation.TerraformOperation
import tanvd.kosogor.terraform.utils.Archive
import java.io.File
import java.net.URL


class TerraformDsl(var project: Project? = null) {
    data class Config(
            var tfVersion: String = "0.11.11",
            var tfLintVersion: String = "0.7.3",
            var awsProvider: String = "1.60.0",
            var awsRegion: String? = null,
            var awsProfile: String? = null
    )

    internal val config = Config()
    fun config(configure: Config.() -> Unit) {
        config.configure()
    }

    data class ModulesCollector(
            var directory: String? = null
    )

    internal var collector = ModulesCollector()
    /** Configuration of modules collecting */
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
    fun lint(configure: Linter.() -> Unit) {
        linter.configure()
    }

    data class Validater(val cacheInitPlugins: Boolean = true)

    internal val validater = Validater()
    fun validate(configure: Validater.() -> Unit) {
        validater.configure()
    }

    data class Artifacts(val jars: LinkedHashSet<Jars> = LinkedHashSet(), val remotes: LinkedHashSet<Remote> = LinkedHashSet()) {
        data class Jars(val configuration: Configuration, val destDir: File)

        fun jars(configuration: Configuration, destDir: File) {
            this.jars += Jars(configuration, destDir)
        }

        data class Remote(val url: URL, val destDir: File, val archive: Archive? = null, val filterToRoot: (File) -> Boolean)

        fun remote(url: URL, destDir: File, archive: Archive? = null, filterToRoot: (File) -> Boolean = { true }) {
            this.remotes += Remote(url, destDir, archive, filterToRoot)
        }
    }

    internal val artifacts = Artifacts()
    fun artifacts(configure: Artifacts.() -> Unit) {
        artifacts.configure()
    }


    fun root(name: String, dir: File, enableDestroy: Boolean = false, targets: LinkedHashSet<String> = LinkedHashSet(),
             variables: LinkedHashMap<String, Any> = LinkedHashMap()) {
        val lint = project!!.tasks.create("$name.lint", LintRootTask::class.java) { task ->
            task.group = "terraform.$name"
            task.description = "Lint root $name"

            task.root = dir
        }

        fun terraformOperation(operation: TerraformOperation.Operation, vararg depends: Task): Task {
            return project!!.tasks.create(
                    "$name.${operation.name.toLowerCase()}",
                    TerraformOperation::class.java
            ) { task ->
                task.dependsOn(depends)
                task.group = "terraform.$name"
                task.description = "${operation.name.toLowerCase().capitalize()} root $name"

                task.operation = operation
                task.targets.addAll(targets)
                task.variables.putAll(variables)
                task.root = dir
            }
        }

        val init = terraformOperation(TerraformOperation.Operation.INIT)
        val plan = terraformOperation(TerraformOperation.Operation.PLAN, init)
        val apply = terraformOperation(TerraformOperation.Operation.APPLY, init)
        if (enableDestroy) {
            val destroy = terraformOperation(TerraformOperation.Operation.DESTROY, init)
        }
        val output = terraformOperation(TerraformOperation.Operation.OUTPUT, init)
    }
}

internal val terraformDsl = TerraformDsl()

fun Project.terraform(configure: TerraformDsl.() -> Unit) {
    terraformDsl.project = this
    terraformDsl.configure()
}
