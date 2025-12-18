plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}

rootProject.name = "asmplus"

include("asmplus-lib")

includeBuild(providers.gradleProperty("dependencies.jlib.path")) {
    dependencySubstitution {
        substitute(module("me.exeos:jlib")).using(project(":jlib-lib"))
    }
}