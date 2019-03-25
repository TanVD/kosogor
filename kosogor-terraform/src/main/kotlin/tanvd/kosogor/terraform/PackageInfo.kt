package tanvd.kosogor.terraform

internal data class PackageInfo(val group: String, val name: String, val version: String) {
    fun groupPath() = group.replace(".", "/")
    override fun toString() = "$group:$name:$version"
}
