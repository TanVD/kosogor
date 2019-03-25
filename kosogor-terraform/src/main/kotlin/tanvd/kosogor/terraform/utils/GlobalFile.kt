package tanvd.kosogor.terraform.utils

import java.io.File

internal object GlobalFile {
    lateinit var tfBin: File
    lateinit var tfLintBin: File
    lateinit var tfInitDir: File
    lateinit var modulesDir: File
}
