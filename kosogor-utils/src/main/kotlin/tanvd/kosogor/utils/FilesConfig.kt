package tanvd.kosogor.utils

import org.gradle.api.Project
import org.gradle.api.file.CopySpec
import java.io.File

/** Include includes files to root, directories to root/${nameOfDirectory} */
class FilesConfig(val project: Project,
                  val include: LinkedHashSet<File> = LinkedHashSet(),
                  val exclude: LinkedHashSet<File> = LinkedHashSet(),
                  var custom: ArrayList<CopySpec.() -> Unit> = ArrayList()) {
    fun apply(spec: CopySpec) {
        include.filter { it.isFile }.forEach { spec.from(it) }
        include.filter { it.isDirectory }.forEach { file ->
            spec.from(file) {
                it.into(file.name)
            }
        }
        spec.exclude(exclude.map { it.absolutePath })
        custom.forEach {
            spec.it()
        }
    }
}
