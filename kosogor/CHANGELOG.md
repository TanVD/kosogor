# Changelog
The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/)

# 1.0.22 - 2023-12-03
* Upgrade to Kotlin 1.9.21
* Upgrade to gradle 8.5
* Upgrade gradle plugin shadow 8.1.1

# 1.0.20 - 2023-10-01
* Upgrade to Kotlin 1.9.10
* Upgrade to gradle 8.3
* Upgrade gradle plugin plugin-publish 1.2.1
* Upgrade gradle plugin build-info-extractor-gradle 5.1.9

# 1.0.18 - 2022-12-16
* Upgrade gradle plugin plugin-publish 1.1.0
* Upgrade gradle plugin build-info-extractor-gradle 4.30.0
* Upgrade gradle plugin shadow 7.1.2

# 1.0.17 - 2022-01-25
* Upgrade to Kotlin 1.7.20
* Upgrade to gradle 7.5.1
* Upgrade gradle plugin plugin-publish 1.0.0 
* Upgrade gradle plugin build-info-extractor-gradle 4.29.2
* Support multiple jars publications with `publishJars`

# 1.0.16 - 2022-09-28
* `ShadowJarProxy` supports `transformers`

# 1.0.15 - 2022-05-17
* Fix plugins jar artifactId

# 1.0.14 - 2022-05-14
* Upgrade to Kotlin 1.6.21
* Upgrade to gradle 7.4.2

# 1.0.13 - 2021-12-29
* Upgrade to Kotlin 1.6.10
* Upgrade to gradle 7.3.3

# 1.0.12 - 2021-06-08
* Upgrade to Kotlin 1.5.10

# 1.0.11 - 2021-02-05
* Upgrade to gradle 6.8.1

# 1.0.10 - 2020-07-10
* Upgrade to gradle 6.5.1

# 1.0.9 - 2020-05-22
* Upgrade to gradle 6.4.1

# 1.0.8 — 2020-03-08
* Upgrade to gradle 6.2.2

# 1.0.7 — 2019-06-26
* Add possibility to disable publish to Bintray

# 1.0.6 — 2019-06-09
* Update Gradle wrapper to 5.4.1
* Fix bug in a IdeaPlugin - make excludes mutable

# 1.0.5 — 2019-05-07
* Update Gradle wrapper to 5.3.1

# 1.0.4 — 2019-03-22
* Add few accessors
* Remove kosogor utils

# 1.0.3 — 2019-03-20
* Add the task getter to ShadowJarProxy
* Add possibility to override the main jar for PublishJarProxy

# 1.0.2 — 2019-03-13
* Fix BuildDir defaults broken in 1.0.2 (it was not applied to parent projects)
* Update wrapper defaults to 5.2.1
* Add `userOrg` field for bintray plugin

# 1.0.1 — 2019-03-7
### Added
* PublishPlugin facade with support of Gradle plugins portal
* Fix issue with unneeded delete of build dir

# 1.0.0 — 2019-02-19
### Added
* PublishJar facade with support of Artifactory and Bintray
* ShadowJar facade
* IDEA plugin defaults
* Build dir defaults
* Wrapper defaults
