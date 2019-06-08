# kosogor

`kosogor` is a plugin, which provides reasonable defaults and simplified Kotlin-DSL interface to Gradle plugins and API functions used in everyday development.

It includes simplified Kotlin-DSL interfaces for:
* IDEA plugin
* Publishing: 
    * Artifactory
    * Bintray
    * Gradle Plugin Portal
* ShadowJar

Also, it provides reasonable defaults (which, nevertheless, can be overridden) for:
* IDEA plugin
* Wrapper
* Build dir + Clean task


## Setup

`kosogor` is released to `plugins.gradle.org`

To set up it just apply plugin: 

```
plugins {
    id("tanvd.kosogor") version "1.0.4" apply true
}
```
## What's inside

The only thing you need to do to get all the defaults is to apply Kosogor plugin. 

### Default configuration

The default configuration of Kosogor plugin on apply will:
* Apply `idea` plugin if not applied. The default configuration will add to `excluded` most of the build and tmp dirs, 
gradle utility files and so on. Also, will be enabled a download of javadocs, sources for dependencies and inheritance
of output dirs 
* Setup `wrapper` version to `5.4.1` (the latest stable on the date of release).
* Setup global build dir for projects (each project will be built below own project dir inside of global). This behavior 
is similar to IntelliJ IDEA behavior with a flag `inheritOutputDirs`. Also, `clean` tasks in projects will be updated (or 
created if not existed) to remove build dirs created.

### Jar publishing

Kosogor includes a facade to a collection of publishing tasks implemented by `Jar`, `maven-publish`, `com.jfrog.artifactory`,
`com.jfrog.bintray` APIs.

The facade is quite simple. You just use `publishJar` lambda and passes to it in a declarative Kotlin-DSL format configuration you need:

```kotlin
publishJar {
    publication {
        artifactId = "myPreciousLibrary"
    }
    
    artifactory {
        serverUrl = "https://artifactory.server.com"
        repository = "artifactory-repo"
        username = "artifactory-user"
        secretKey = System.getenv("artifactory_key")
    }
}

```

This simple code will create Jar publication with artifact id `myPreciousLibrary` (which will be used as artifact name for
publishing) and task generating Jar with sources named `sourcesJar`. Then it will apply `com.jfrog.artifactory` plugin 
(if not already applied) and prepare package including jar, sources and pom to publish to artifactory. 

Note, that by default jar and sources also created, in example we just override some parameters. Of course, it is possible 
to disable generation of some parts of package, and it will be also removed from package published to artifactory.

You can use also `bintray` to set up bintray publishing. It does not intersect with `artifactory` and both can 
be used simultaneously.

### Gradle plugin publishing

Kosogor includes a simplified facade to `java-gradle-plugin` and `com.gradle.publish-plugin`

Here is the definition of Kosogor plugin publisher:
```kotlin
publishPlugin {
    id = "tanvd.kosogor"
    displayName = "kosogor"
    implementationClass = "tanvd.kosogor.KosogorPlugin"
    version = project.version.toString()

    info {
        website = "https://github.com/TanVD/kosogor"
        vcsUrl = "https://github.com/TanVD/kosogor"
        description = "Reasonable defaults and simplified Kotlin-DSL interfaces for everyday development"
        tags.addAll(listOf("default", "common", "kotlin", "jar", "shadowjar", "artifactory", "idea"))
    }
}
```

Such a quite concise piece of code applies `java-gradle-plugin` if not applied and sets up plugin bundle
definition with specified id and implementation class. After it applies `com.gradle.plugin-publish`
if not applied and sets up all the needed publishing configuration to publish a plugin to `plugins.gradle.org`.

### Shadow jar 

Kosogor includes a simplified facade to `com.github.johnrengelman.shadow`.

```kotlin
shadowJar {
    jar {
        archiveName = "archive.jar"
        mainClass = "tanvd.example.MainKt"
    }
}
```

This code will apply `com.github.johnrengelman.shadow` if it is not already applied. Then it will override some defaults
of shadowJar plugin with values passed with DSL. In example, we override the name of resulting archive and mainClass (which is pushed
to manifest). By default, destination dir for shadowJar is `$global_build_dir/shadow`, but you can also override it.
