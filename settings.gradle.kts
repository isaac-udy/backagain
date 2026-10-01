rootProject.name = providers.gradleProperty("backagain.projectName").get()

enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

pluginManagement {
    includeBuild("build-logic")
    repositories {
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositories {
        google {
            mavenContent {
                includeGroupAndSubgroups("androidx")
                includeGroupAndSubgroups("com.google")
            }
        }
        mavenCentral()
    }
}

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}

include(":app:client:common")
include(":app:client:web")
include(":app:server")

include(":feature:deck:api")
include(":feature:deck:client")

include(":feature:live:api")
include(":feature:live:client")
include(":feature:live:server")

include(":platform:client:design")
include(":platform:client:http")
include(":platform:common:architecture")
include(":platform:common:http")
include(":platform:server:development")
include(":platform:server:http")
include(":platform:server:postgres")
