package tanvd.kosogor.terraform

import com.beust.klaxon.Json

internal data class PackageInfo(val group: String,
                                val name: String,
                                val version: String,
                                @Json(name = "skip-validation") val skipValidation: Boolean = false) {
    fun groupPath() = group.replace(".", "/")
    override fun toString() = "$group:$name:$version"
}
