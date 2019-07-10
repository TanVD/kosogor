package tanvd.kosogor.terraform.tasks.publish

import com.github.kittinunf.fuel.Fuel
import com.github.kittinunf.fuel.core.FuelManager
import com.github.kittinunf.fuel.core.extensions.authentication
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction
import tanvd.kosogor.terraform.PackageInfo
import tanvd.kosogor.terraform.terraformDsl
import tanvd.kosogor.terraform.utils.GlobalFile

/**
 * Task publishes zipped modules to HTTP server
 * (e.g. artifactory generic repository) via PUT method.
 *
 * PUT method is executed upon `/$publisher.repository/$modulePath/$fileName`
 * path and uses basic authorization with an auth from
 * `username` and `secretKey`.
 *
 * If `ignoreExisting` set to true in a publisher config,
 * then task will not fail on `201` status.
 */
open class PublishModulesTask : DefaultTask() {
    init {
        outputs.upToDateWhen { false }
    }

    @TaskAction
    fun publish() {
        FuelManager.instance.basePath = terraformDsl.publisher.serverUrl

        var published = false

        GlobalFile.modulesDir
                .walk()
                .filter { it.absolutePath.endsWith(".zip") }
                .forEach {
                    val modulePath = it.relativeTo(GlobalFile.modulesDir).parent
                    val fullPath = "/${terraformDsl.publisher.repository}/$modulePath/${it.name}"

                    val parts = modulePath.split("/")
                    val info = PackageInfo(parts.dropLast(1).joinToString("."), parts.takeLast(1).single(), it.nameWithoutExtension)

                    val exists = checkIfExists(fullPath, info)

                    if (exists && !terraformDsl.publisher.ignoreExisting)  {
                        error("Artifact $info already exists.")
                    }

                    if (!exists || (terraformDsl.publisher.overwriteSnapshots && info.version.endsWith("SNAPSHOT"))) {

                        val response = Fuel.put("/${terraformDsl.publisher.repository}/$modulePath/${it.name}")
                                .authentication().basic(terraformDsl.publisher.username!!, terraformDsl.publisher.secretKey!!)
                                .body(it.readBytes())
                                .responseString()


                        if (response.second.statusCode != 201) {
                            error("Error occurred during upload of $info. Artifactory returned status code ${response.second.statusCode}")
                        }

                        published = true

                        println("Uploaded module $info")
                    }

                }

        if (!published) {
            println("No modules were published")
        }
    }

    private fun checkIfExists(path: String, info: PackageInfo): Boolean {
        val getStatusCode = Fuel.get(path).response().second.statusCode

        if (getStatusCode != 200 && getStatusCode != 404) {
            error("Error occurred during get of $info. Artifactory returned status code $getStatusCode")
        }

        return getStatusCode == 200
    }
}
