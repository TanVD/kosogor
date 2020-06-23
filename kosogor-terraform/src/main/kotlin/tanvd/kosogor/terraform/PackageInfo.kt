package tanvd.kosogor.terraform

import com.beust.klaxon.Json

internal data class PackageInfo(val group: String,
                                val name: String,
                                val version: String,
                                @Json(name = "validation-rules") val validationRules: ValidationRules = ValidationRules()) {
    fun groupPath() = group.replace(".", "/")
    override fun toString() = "$group:$name:$version"

    data class ValidationRules(
            @Json(name = "skip-validation") val skipValidation: Boolean = false,
            @Json(name = "create-provider") val forceProvider: Boolean = true
    )
}
