# Micronaut Uber Build

This project creates a synthetic composite builds of multiple Micronaut projects, allowing for example to test the changes of Micronaut Core with another project without having to rely on publishing snapshots and triggering downstream builds manually.

## How-to

First, edit the `settings.gradle` file and find the following lines:

```gradle
uberBuild {
    includes {
        micronaut('core') {
            branch '4.0.x'
        }
        micronaut 'security'
        micronaut 'validation'
        micronaut('serde') {
            slug 'micronaut-serialization'
        }
    }
}
```

You can put as many projects as you want, but only some of them will work (typically adding `micronaut-test` will fail because of a circular dependency between `core` and `test`).

Some combinations of project will not work (e.g using `serde` and `test-resources` in the same build).

Then invoke the build using `./gradlew -I disable-broken.gradle test --continue`.

It is recommended to run with `--continue` to get the maximum feedback for all projects.

Note that if you forget about the init script, builds are likely to fail with:

```
1: Task failed with an exception.
-----------
* What went wrong:
Execution failed for task ':micronaut-groovy:runtime-groovy:generatePomFileForMavenPublication'.
> Cannot invoke "org.gradle.api.internal.project.ProjectInternal.getOwner()" because "project" is null

* Try:
> Run with --stacktrace option to get the stack trace.
> Run with --info or --debug option to get more log output.
==============================================================================

```

## Using local versions of projects

By default, the project will clone projects from GitHub.
It is however possible to use local versions of projects instead, in which case the `branch` which is specified in the configuration will be ignored.

To do this, you must set the `auto.include.git.dirs` Gradle property to the list of directories to look for.

For example, if all your micronaut projects are cloned in a single parent directory, say `/home/elena/micronaut-projects`, then set the `auto.include.git.dirs` in your `~/.gradle/gradle.properties` to:

```
auto.include.git.dirs=/home/elena/micronaut-projects
```
