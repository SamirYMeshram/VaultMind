pluginManagement {
    repositories { google(); mavenCentral(); gradlePluginPortal() }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories { google(); mavenCentral() }
}

// Room uses SQLite JDBC during annotation processing on the host machine.
// Keep SQLite native extraction away from C:\Windows on Windows machines.
// Do not set java.io.tmpdir here; Gradle needs that value before settings.gradle.kts is evaluated.
val vaultMindGradleTmpDir = settingsDir.resolve(".gradle/tmp")
vaultMindGradleTmpDir.mkdirs()
System.setProperty("org.sqlite.tmpdir", vaultMindGradleTmpDir.absolutePath)

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
