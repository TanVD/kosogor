package tanvd.kosogor.terraform

import com.beust.klaxon.Json

internal data class PackageInfo(val group: String,
                                val name: String,
                                val version: String,
                                @Json(name = "validation") val validation: Validation = Validation()) {
    fun groupPath() = group.replace(".", "/")
    override fun toString() = "$group:$name:$version"

    data class Validation(
            @Json(name = "skip") val skip: Boolean = false,
            @Json(name = "create-provider") val createProvider: Boolean = true
    )
}
