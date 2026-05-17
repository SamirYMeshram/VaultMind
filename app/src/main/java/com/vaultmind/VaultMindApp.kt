package com.vaultmind

import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.automirrored.outlined.Article
import androidx.compose.material.icons.outlined.PushPin
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.SpaceDashboard
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.vaultmind.feature.backup.BackupScreen
import com.vaultmind.feature.collections.CollectionsScreen
import com.vaultmind.feature.dashboard.DashboardScreen
import com.vaultmind.feature.dashboard.RecentActivityScreen
import com.vaultmind.feature.folders.FoldersScreen
import com.vaultmind.feature.knowledge.CardDetailScreen
import com.vaultmind.feature.knowledge.CardEditScreen
import com.vaultmind.feature.knowledge.KnowledgeListScreen
import com.vaultmind.feature.pinned.PinnedScreen
import com.vaultmind.feature.search.SearchScreen
import com.vaultmind.feature.settings.SettingsScreen
import com.vaultmind.feature.tags.TagsScreen

private object Routes {
    const val Dashboard = "dashboard"
    const val Cards = "cards"
    const val Search = "search"
    const val Pinned = "pinned"
    const val Settings = "settings"
    const val Detail = "card/{cardId}"
    const val Edit = "edit?cardId={cardId}"
    const val Tags = "tags"
    const val Folders = "folders"
    const val Collections = "collections"
    const val Backup = "backup"
    const val Activity = "activity"
}

private data class TopDestination(
    val route: String,
    val label: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector
)

private val topDestinations = listOf(
    TopDestination(Routes.Dashboard, "Home", Icons.Outlined.SpaceDashboard),
    TopDestination(Routes.Cards, "Cards", Icons.AutoMirrored.Outlined.Article),
    TopDestination(Routes.Search, "Search", Icons.Outlined.Search),
    TopDestination(Routes.Pinned, "Pinned", Icons.Outlined.PushPin),
    TopDestination(Routes.Settings, "Settings", Icons.Outlined.Settings)
)

@Composable
fun VaultMindApp() {
    val nav = rememberNavController()
    val backStack by nav.currentBackStackEntryAsState()
    val currentRoute = backStack?.destination?.route
    val showTopNavigation = currentRoute in topDestinations.map { it.route }

    Scaffold(
        bottomBar = {
            if (showTopNavigation) {
                NavigationBar(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh) {
                    topDestinations.forEach { destination ->
                        NavigationBarItem(
                            selected = currentRoute == destination.route,
                            onClick = { nav.top(destination.route) },
                            icon = { Icon(destination.icon, destination.label) },
                            label = { Text(destination.label) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = MaterialTheme.colorScheme.onPrimaryContainer,
                                selectedTextColor = MaterialTheme.colorScheme.primary,
                                indicatorColor = MaterialTheme.colorScheme.primaryContainer
                            )
                        )
                    }
                }
            }
        },
        floatingActionButton = {
            if (showTopNavigation) {
                FloatingActionButton(onClick = { nav.navigate("edit") }) {
                    Icon(Icons.Outlined.Add, "Create")
                }
            }
        }
    ) { padding ->
        NavHost(
            navController = nav,
            startDestination = Routes.Dashboard,
            modifier = Modifier.padding(padding),
            enterTransition = { fadeIn(tween(180)) + slideInHorizontally(tween(220)) { it / 16 } },
            exitTransition = { fadeOut(tween(120)) + slideOutHorizontally(tween(180)) { -it / 24 } },
            popEnterTransition = { fadeIn(tween(180)) + slideInHorizontally(tween(220)) { -it / 16 } },
            popExitTransition = { fadeOut(tween(120)) + slideOutHorizontally(tween(180)) { it / 24 } }
        ) {
            composable(Routes.Dashboard) {
                DashboardScreen(
                    onOpenCard = { nav.navigate("card/${it.id}") },
                    onCreateCard = { nav.navigate("edit") },
                    onSearch = { nav.navigate(Routes.Search) },
                    onOpenTags = { nav.navigate(Routes.Tags) },
                    onOpenFolders = { nav.navigate(Routes.Folders) },
                    onOpenCollections = { nav.navigate(Routes.Collections) },
                    onOpenBackup = { nav.navigate(Routes.Backup) },
                    onOpenActivity = { nav.navigate(Routes.Activity) }
                )
            }
            composable(Routes.Cards) {
                KnowledgeListScreen(
                    onOpenCard = { nav.navigate("card/${it.id}") },
                    onCreateCard = { nav.navigate("edit") }
                )
            }
            composable(Routes.Search) {
                SearchScreen(onOpenCard = { nav.navigate("card/${it.id}") })
            }
            composable(Routes.Pinned) {
                PinnedScreen(onOpenCard = { nav.navigate("card/${it.id}") })
            }
            composable(Routes.Settings) {
                SettingsScreen(onOpenBackup = { nav.navigate(Routes.Backup) })
            }
            composable(Routes.Tags) {
                TagsScreen(onBack = { nav.popBackStack() })
            }
            composable(Routes.Folders) {
                FoldersScreen(onBack = { nav.popBackStack() })
            }
            composable(Routes.Collections) {
                CollectionsScreen(onBack = { nav.popBackStack() })
            }
            composable(Routes.Backup) {
                BackupScreen(onBack = { nav.popBackStack() })
            }
            composable(Routes.Activity) {
                RecentActivityScreen(onBack = { nav.popBackStack() })
            }
            composable(
                route = Routes.Detail,
                arguments = listOf(navArgument("cardId") { type = NavType.StringType })
            ) {
                CardDetailScreen(
                    onBack = { nav.popBackStack() },
                    onEdit = { nav.navigate("edit?cardId=$it") },
                    onOpenCard = { nav.navigate("card/${it.id}") }
                )
            }
            composable(
                route = Routes.Edit,
                arguments = listOf(navArgument("cardId") { type = NavType.StringType; nullable = true; defaultValue = null })
            ) {
                CardEditScreen(
                    onBack = { nav.popBackStack() },
                    onSaved = { nav.navigate("card/$it") { popUpTo(Routes.Cards) } }
                )
            }
        }
    }
}

private fun androidx.navigation.NavHostController.top(route: String) = navigate(route) {
    popUpTo(graph.findStartDestination().id) { saveState = true }
    launchSingleTop = true
    restoreState = true
}
