# Kosogor

[![CircleCI](https://circleci.com/gh/TanVD/kosogor.svg?style=svg)](https://circleci.com/gh/TanVD/kosogor)

The idea behind Kosogor project is to reduce the size  of your build.gradle.kts and remove from it all the boilerplate: 
setting plugins configurations, which should be defaults or using verbose plugin interfaces when you don't need such flexibility.

It includes a few different plugins:
* `kosogor` &mdash; simplified Kotlin-DSL facades and defaults for everyday development. See it's [README](https://github.com/TanVD/kosogor/blob/master/kosogor)
* `kosogor-terraform` &mdash; support of Terraform in Gradle — from modules publish to deployment. See it's [README](https://github.com/TanVD/kosogor/blob/master/kosogor-terraform)
* `kosogor-web` &mdash; tasks and facades simplifying Web development (from War preparation to Js minification). See it's [README](https://github.com/TanVD/kosogor/blob/master/kosogor-web)
* `kosogor-zkm` &mdash; Gradle wrapper for ZKM-based obfuscation, clean and simple.  See it's [README](https://github.com/TanVD/kosogor/blob/master/kosogor-zkm)

## More?

The priority of Kosogor project is to make Gradle simple and concise for everyday use. Feel free to add issues on extending
Kosogor with facades for the plugins/systems you use. Probably such issues will be implemented in Kosogor itself
or derivating plugins will occur (like `kosogor-zkm` already exists).

## Examples

Examples of Kosogor usage you can find in [JetBrains/Exposed](https://github.com/Jetbrains/Exposed), [TanVD/AORM](https://github.com/TanVD/AORM) or [TanVD/JetAudit](https://github.com/TanVD/JetAudit) libraries
