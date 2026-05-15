pluginManagement {
    repositories { google(); mavenCentral(); gradlePluginPortal() }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories { google(); mavenCentral() }
}
rootProject.name = "VaultMind"
include(":app")
include(":core:common")
include(":core:model")
include(":core:domain")
include(":core:database")
include(":core:datastore")
include(":core:network")
include(":core:designsystem")
include(":core:analytics")
include(":core:testing")
include(":feature:dashboard")
include(":feature:knowledge")
include(":feature:search")
include(":feature:tags")
include(":feature:folders")
include(":feature:collections")
include(":feature:pinned")
include(":feature:backup")
include(":feature:settings")
