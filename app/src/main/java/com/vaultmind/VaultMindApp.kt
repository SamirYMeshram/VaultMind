package com.vaultmind

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.*
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

private object R {
    const val Dashboard = "dashboard"; const val Cards = "cards"; const val Search = "search"; const val Pinned = "pinned"; const val Settings = "settings"
    const val Detail = "card/{cardId}"; const val Edit = "edit?cardId={cardId}"
    const val Tags = "tags"; const val Folders = "folders"; const val Collections = "collections"; const val Backup = "backup"; const val Activity = "activity"
}

@Composable
fun VaultMindApp() {
    val nav = rememberNavController()
    val backStack by nav.currentBackStackEntryAsState()
    val current = backStack?.destination?.route
    val top = listOf(R.Dashboard, R.Cards, R.Search, R.Pinned, R.Settings)
    Scaffold(
        bottomBar = {
            NavigationBar {
                item(R.Dashboard, current, "Home", Icons.Outlined.SpaceDashboard) { nav.top(R.Dashboard) }
                item(R.Cards, current, "Cards", Icons.Outlined.Article) { nav.top(R.Cards) }
                item(R.Search, current, "Search", Icons.Outlined.Search) { nav.top(R.Search) }
                item(R.Pinned, current, "Pinned", Icons.Outlined.PushPin) { nav.top(R.Pinned) }
                item(R.Settings, current, "Settings", Icons.Outlined.Settings) { nav.top(R.Settings) }
            }
        },
        floatingActionButton = { if (current in top) FloatingActionButton(onClick = { nav.navigate("edit") }) { Icon(Icons.Outlined.Add, "Create") } }
    ) { padding ->
        NavHost(nav, startDestination = R.Dashboard, modifier = Modifier.padding(padding)) {
            composable(R.Dashboard) { DashboardScreen(
                onOpenCard = { nav.navigate("card/${it.id}") }, onCreateCard = { nav.navigate("edit") }, onSearch = { nav.navigate(R.Search) },
                onOpenTags = { nav.navigate(R.Tags) }, onOpenFolders = { nav.navigate(R.Folders) }, onOpenCollections = { nav.navigate(R.Collections) }, onOpenBackup = { nav.navigate(R.Backup) }, onOpenActivity = { nav.navigate(R.Activity) }
            ) }
            composable(R.Cards) { KnowledgeListScreen({ nav.navigate("card/${it.id}") }, { nav.navigate("edit") }) }
            composable(R.Search) { SearchScreen { nav.navigate("card/${it.id}") } }
            composable(R.Pinned) { PinnedScreen { nav.navigate("card/${it.id}") } }
            composable(R.Settings) { SettingsScreen { nav.navigate(R.Backup) } }
            composable(R.Tags) { TagsScreen { nav.popBackStack() } }
            composable(R.Folders) { FoldersScreen { nav.popBackStack() } }
            composable(R.Collections) { CollectionsScreen { nav.popBackStack() } }
            composable(R.Backup) { BackupScreen { nav.popBackStack() } }
            composable(R.Activity) { RecentActivityScreen { nav.popBackStack() } }
            composable(R.Detail, listOf(navArgument("cardId") { type = NavType.StringType })) {
                CardDetailScreen(onBack = { nav.popBackStack() }, onEdit = { nav.navigate("edit?cardId=$it") }, onOpenCard = { nav.navigate("card/${it.id}") })
            }
            composable(R.Edit, listOf(navArgument("cardId") { type = NavType.StringType; nullable = true; defaultValue = null })) {
                CardEditScreen(onBack = { nav.popBackStack() }, onSaved = { nav.navigate("card/$it") { popUpTo(R.Cards) } })
            }
        }
    }
}
private fun androidx.navigation.NavHostController.top(route: String) = navigate(route) { popUpTo(graph.findStartDestination().id) { saveState = true }; launchSingleTop = true; restoreState = true }
@Composable private fun NavigationBarScope.item(route: String, current: String?, label: String, icon: androidx.compose.ui.graphics.vector.ImageVector, onClick: () -> Unit) {
    NavigationBarItem(selected = current == route, onClick = onClick, icon = { Icon(icon, label) }, label = { Text(label) })
}
