package tanvd.kosogor.web.utils

import org.gradle.api.Project
import org.gradle.api.plugins.ExtraPropertiesExtension
import org.gradle.kotlin.dsl.getByName

//Generated accessors to use in a plugin
internal inline fun <reified T : Any> Project.extByName(name: String): T = extensions.getByName<T>(name)

internal inline fun <reified T : Any> Project.ext(name: String) = _ext[name] as T

internal val Project._ext: ExtraPropertiesExtension
    get() = extByName("ext")
