# Changelog
The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/)

# 1.0.7 - 2021-02-05
* Upgrade to gradle 6.8.1
* Support subproject exclusion for ValidationVersions and ValidateConfiguration tasks

# 1.0.6 - 2020-07-11
* Fix bug in CollectDependencies task doesn't work with gradle 6.5.1
* GradleMetadata should allow using Java 8 

# 1.0.5 - 2020-07-10
* Upgrade to gradle 6.5.1

# 1.0.4 — 2020-03-08
* Upgrade to gradle 6.2.2

# 1.0.3 — 2019-03-23
* Fix bug in war proxy -- copy used wrong copyspec as argument

# 1.0.2 — 2019-03-22
* Add few accessors
* Remove kosogor utils

# 1.0.1 — 2019-03-22
* Add few accessors
* Remove kosogor utils

# 1.0.0 — 2019-03-21
### Added
* Add tasks for Web containers support
    * CollectDependencies task
    * ValidateConfigurations task
    * ValidateVersions task
* Add War facade
* Add CompileJs task
