# kosogor-terraform

`kosogor-terraform` is a plugin, which provides support of Terraform for 
Gradle — from creation and publishing of reusable terraform modules to
deployment.

## Setup

`kosogor-terraform` is released to `plugins.gradle.org`

To set up it just apply plugin: 

```
plugins {
    id("tanvd.kosogor.terraform") version "1.0.0" apply true
}
```

## What's inside

Plugin includes support of:
* Terraform modules creation, validation and publishing
* Terraform code linting with TfLint or Terraform itself
* Deployment of infrastructure, including:
    * Getting jars and remote artifacts before deploy
    * Execution of terraform operation — init, plan, apply 

### Support of modules

One of the key ideas of `kosogor-terraform` is to give you 
possibility to easily create and distribute reusable terraform
modules. 

It takes the best from the Java world and offers simple solution —
modules are distributed almost as jars: validated, packed with Gradle
and uploaded to Artifactory. When you use them, you just download
them from Artifactory (using `terraform get`) and run deployment.

There are few tasks in `kosogor-terraform` providing support of modules:
* `ValidateModulesTask` — validation of modules with terraform. 
  It guarantees syntax correctness of module, and it's compliance
  to specified version of aws provider.  
* `CollectModulesTask` — archive modules into zips before uploading
* `PublishModulesTask` — publish modules to HTTP server

Setting up modules publishing is very simple:
```kotlin
terraform {
    config {
        awsRegion = "eu-west-1"
        awsProfile = "default"

        tfVersion = "0.11.11"
        awsProvider = "1.60.0"
    }

    publish {
        artifactoryUrl = "https://artifactory.example.com"
        artifactoryRepo = "example-repo"
        artifactoryUser = "username"
        artifactoryKey = System.getenv("artifactory_api_key")
        ignoreExisting = true
    }
}
```

### Deployment support

`kosogor-terraform` supports deployment of infrastructure via
few tasks executing `init`, `plan` and `apply` operations
upon registered terraform roots. 

It is as simple as it sounds, you just specify terraform 
version, sources root and `kosogor-terraform` will create 
all the needed tasks for you:

```kotlin
terraform {
    config {
        tfVersion = "0.11.11"
        awsProvider = "1.60.0"
        awsRegion = "eu-west-1"
        awsProfile = "default"
    }
    
    root("example", File(projectDir, "terraform"))
}
```

In this example, `kosogor-terraform` will create tasks
`example.init`, `example.plan` and `example.apply` for
specified root.

Note: operations will be executed with terraform binary
of appropriate version downloaded by `kosogor-terraform`
itself into the build directory.

You can specify targets in root and enable creation
of `destroy` operation (disabled by default) also from DSL.

### Additional artifacts support

In some cases, your deployment may depend on external 
artifacts. For example, you may need to download JARs
from artifactory or from remote HTTP server. Such
cases also supported in `kosogor-terraform`.

Just use extension of DSL syntax  (called `artifacts`)
to define external artifacts:

```kotlin
val lambdas = configurations["lambdas"]

terraform {
    config {
        tfVersion = "0.11.11"
        awsProvider = "1.60.0"
        awsRegion = "eu-west-1"
        awsProfile = "default"
    }

    artifacts {
        // Add external http artifact
        remote(URL("https://example.com/archive.tar.gz"), toFile, Archiver.TARGZ)

        // Add the configuration with a pack of jars
        lambdas.collectTo(File(projectDir, "lambdas"))
    }
}
```

### Linting support

`kosogor-terraform` supports linting of terraform source roots 
out of the box.

It uses terraform itself or TfLint to check correctness.

If `terraform `chosen in DSL as linter, then `plan` operation will 
be performed and linting will be successful only if `plan` 
returned 0 exit code.

If `tflint` chosen in DSL as linter, then it will be executed 
upon provided source root and linting will be successful only 
if `tflint` returned 0 exit code.

Linting is executed upon terraform sources root (for modules 
`validate` task acts as linter).

Setting up sources roots linting is very simple:
```kotlin

terraform {
    config {
        tfVersion = "0.11.11"
        awsProvider = "1.60.0"
        awsRegion = "eu-west-1"
        awsProfile = "default"
    }
    
    lint {
        linter = LinterType.Terraform
    }

    root("example", File(projectDir, "terraform"))
}
```

For such configuration task `default.lint` will be created.

Note, by default `LinterType.Terraform` will be used

## Authors

* @sndl — author of idea, implementation of modules support
* @tanvd — maintainer, implementation of deployments support
  and validation
