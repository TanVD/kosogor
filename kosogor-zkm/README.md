# kosogor-zkm

`kosogor-zkm` is a plugin, which provides Kotlin-DSL facade to ZKM obfuscation.

It wraps call of `ZKM` into Gradle task and passes to its script few predefined variables from task definition.

## Setup

`kosogor-zkm` is released to `plugins.gradle.org`

To set up it just apply plugin: 

```
plugins {
    id("tanvd.kosogor.zkm") version "1.0.4" apply true
}
```
## ZKM task

ZKM task provides a simple interface to ZKM from Gradle.

It passes following variables from task definition to ZKM script file as placeholders. All paths placeholders should be unquoted in script:
* inputJars &mdash; it will replace `__INPUT_JARS__` placeholder, which should contain the list of obfuscated jars. Evaluates into `open absolutePath.jar;`.
* outputDir &mdash; it will be `__OUTPUT_DIR__` placeholder
* changeLogFile &mdash; it will be `__CHANGELOG_FILE__` placeholder (can be used for `changeLogFileOut`)
* zkmClasspath &mdash; it will be `__CLASSPATH__` placeholder classpath to ZKM should use during obfuscation. Evaluates into `classpath absolutePath.jar;`.* 

Also, there are few variables used by ZKM itself:
* zkmJar &mdash; path to your ZKM jar
* zkmScript &mdash; path to your ZKM script template with placeholders described above
* zkmLogFile &mdash; used in call of zkm.jar as `-l zkmLogFile.absolutePath`

So, to sum up, here is the simple definition of task:

```kotlin
zkmJars {
    dependsOn(jar)

    zkmClasspath = configurations["compile"]
    
    inputJars = setOf(jar)
    outputDir = File(projectDir, "obfuscated-jar")

    changeLogFile = File(projectDir, "obfuscated-log/ChangeLog.txt")
    zkmLogFile = File(projectDir, "obfuscated-log/ZkmLog.txt")

    zkmJar = File(projectDir, "zkm/zkm.jar")
    zkmScript = File(projectDir, "zkm/script.zkm")
}
```
