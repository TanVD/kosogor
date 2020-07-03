package tanvd.kosogor.terraform.utils

import tanvd.kosogor.terraform.terraformDsl
import java.io.File

internal object TFLint {
    private var defaultConfig: File? = null

    fun lint(workingDir: File) {
        val customConfig = File(workingDir, ".tflint.hcl")
        val config = customConfig.takeIf { it.exists() }?.absolutePath
                ?: terraformDsl.config.tfLintConfigPath
                ?: configureTFLint(workingDir).absolutePath

        try {
            CommandLine.executeOrFail(GlobalFile.tfLintBin.absolutePath, listOf("-c=$config"),
                    workingDir, true)
        } finally {
            defaultConfig?.delete()
        }
    }

    private fun configureTFLint(workingDir: File): File {
        val commonConfig = TFLint::class.java.classLoader.getResource("tflint.hcl")!!
        defaultConfig = File(workingDir, "tflint.hcl").apply {
            writeText(commonConfig.readText())
        }

        return defaultConfig!!
    }
}