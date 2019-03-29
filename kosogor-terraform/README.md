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
modules are distributed almost as jars: validate, pack with Gradle
and upload to Artifactory. When you use them, you just download
them from Artifactory locally and run deployment.

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

### Linting support

`kosogor-terraform` supports linting of terraform source roots 
out of the box.

It uses terraform itself or TfLint to check correctness.

If terraform chosen in DSL as linter, then `plan` operation will 
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
