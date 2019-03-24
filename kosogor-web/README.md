# kosogor-web

`kosogor-web` is a plugin which provides Kotlin-DSL facades and tasks to simplify Web development.

It includes tasks for:
* JavaScript compilation (using closure-compiler)
* Web container libraries preparation 
    * Task to collect configuration dependencies into archives (used per classloader in Web container)
    * Task to validate that there are no dependencies with more than one version
    * Task to ensure that dependencies fixed for specific configuration are not used in others

Also, it provides simplified facades for:
* War plugin

## Setup

`kosogor-web` is released to `plugins.gradle.org`

To set up it just apply plugin: 

```
plugins {
    id("tanvd.kosogor.web") version "1.0.3" apply true
}
```
## JavaScript compilation

`kosogor-web` plugin includes task providing Kotlin-DSL interface to `closure-compiler`. 
Using it you can compile JS files as a part of Gradle build lifecycle.

Here is the simple definition of Js compiling task: 

```kotlin
compileJs {
    fromDir = file("/js-scripts")
    toDir = file("/compiler-js-scripts")
    configuration = configurations["closure"]
    excludeFiles.add(file("/js-scripts/requirejs-config.js"))
}
```

Note, that you need to define configuration with closure-compiler version you want to use. By default task will use 
`SIMPLE_OPTIMIZATIONS` mode and `ECMASCRIPT5` as target language, but all parameters can be overridden.

Compilation of JS performed per file and in parallel on all available cores.

### Web container libraries

`kosogor-web` plugin includes bunch of tasks to support web-container libraries preparation and validate
result. 

#### CollectDependencies

This task collects dependencies from specified configurations across all projects.

The main purpose of a task is to support prepartion of dependencies sets per web-container classloader. 

In case of Tomcat it can be used in a following manner &mdash; you define dependencies using `common`, `shared` and `webapp` configurations and during the build generate 3 archives &mdash; `common` (all in common configuration), `shared` (all in shared configuration, excluding those which already in `common`) and `webapp` (all in webapp configuration, excluding shared and common). This archives can be used as dependency sets for Tomcat classloaders now.

Here is the example of CollectDependencies task configuration: 
```kotlin
collectDependencies("sharedLibs") {
    include("shared")
    exclude(commonLibs)
   
    archiveFile = File(globalLibsDir, "shared-libs.zip")
}
```

#### ValidateVersions

This task checks if there are multiple artifacts equal by name and group, but with different versions across all specified configurations.

The main purpose of this task is to validate dependencies prepared by `CollectDependencies` task  to be sure, that no dependencies will be overridden, when this sets are used per web-container classloader.

Here is the example of ValidateVersions task configuration:

```kotlin
validateVersions {
    include("compile", "compileOnly", "deployment", "common", "shared")
}
```

This task will verify, that there no overrides across all specified configurations.

#### ValidateConfigurations

This task checks if there are dependencies which were fixed (using `fixFor`) for specific configuration, but now are used in some other 
configuration as well.

The main purpose of this task is to fix dependencies per configuration. In case of web containers it guarantees that library will 
always be in the fixed classloader. It may be important to fix library in some classloader, cause, for example, some libraries may
save state in static variables which will be shared across all webapps, if you load this library in `shared` classloader of Tomcat.

To fix dependency you need to use `fixFor`:

```kotlin
compile("tanvd", "library", "1.0.0") {
    fixFor(project, "compile")
}
```

Here is the example of ValidateConfigurations task configuration: 

```kotlin
validateConfigurations {
    include("compile", "compileOnly", "deployment", "common", "shared")
}
```

### War facade

`kosogor-web` plugin includes simplified Kotlin-DSL for War plugin. It uses `FilesConfig` interface to declare what files 
should and what should not be included in War.

The simple example of War facade usage to create Light War:

```kotlin
createWar {
    classpath {
        include += project.configurations["compile"].resolve()
        exclude += project.compileOnlyLibs().set
        exclude += project.deploymentLibs().set
        exclude += project.commonLibs().set
        exclude += project.sharedLibs().set
    }
    
    static {
        include += files("static")
    }
}
```
