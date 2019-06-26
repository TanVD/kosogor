# Changelog
The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/)

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
