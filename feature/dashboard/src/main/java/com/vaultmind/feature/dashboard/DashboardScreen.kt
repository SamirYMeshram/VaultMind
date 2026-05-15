@file:OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class, androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.vaultmind.feature.dashboard

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.vaultmind.core.common.toReadableDate
import com.vaultmind.core.designsystem.*
import com.vaultmind.core.model.KnowledgeCard

@Composable
fun DashboardScreen(
    onOpenCard: (KnowledgeCard) -> Unit,
    onCreateCard: () -> Unit,
    onSearch: () -> Unit,
    onOpenTags: () -> Unit,
    onOpenFolders: () -> Unit,
    onOpenCollections: () -> Unit,
    onOpenBackup: () -> Unit,
    onOpenActivity: () -> Unit,
    viewModel: DashboardViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    when (val s = state) {
        DashboardUiState.Loading -> VaultLoading()
        is DashboardUiState.Error -> VaultError(s.message)
        is DashboardUiState.Success -> LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text("VaultMind", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.ExtraBold)
                        Text("Your private offline knowledge vault")
                    }
                    FilledTonalButton(onClick = onSearch) { Icon(Icons.Outlined.Search, null); Spacer(Modifier.width(8.dp)); Text("Search") }
                }
            }
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                    StatCard("Cards", s.data.stats.totalCards.toString(), Icons.Outlined.Article, Modifier.weight(1f))
                    StatCard("Pinned", s.data.stats.pinnedCards.toString(), Icons.Outlined.PushPin, Modifier.weight(1f))
                    StatCard("Tags", s.data.stats.totalTags.toString(), Icons.Outlined.LocalOffer, Modifier.weight(1f))
                }
            }
            item { QuickActions(onCreateCard, onOpenTags, onOpenFolders, onOpenCollections, onOpenBackup, onOpenActivity) }
            item { SectionHeader("Continue where you left off") }
            if (s.data.recentCards.isEmpty()) item { VaultEmpty("No cards yet", "Create your first knowledge card.", "Create card", onCreateCard) }
            else items(s.data.recentCards, key = { it.id }) { KnowledgeCardRow(it, { onOpenCard(it) }) }
            item { SectionHeader("Pinned knowledge") }
            if (s.data.pinnedCards.isEmpty()) item { Text("Pin important cards to make them appear here.") }
            else items(s.data.pinnedCards, key = { "pinned-${it.id}" }) { KnowledgeCardRow(it, { onOpenCard(it) }) }
            item { SectionHeader("Top tags") }
            item { FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) { s.data.topTags.forEach { TagChip(it) } } }
            item { SectionHeader("Recent activity") }
            items(s.data.recentActivity, key = { it.id }) { activity ->
                ListItem(
                    headlineContent = { Text(activity.title) },
                    supportingContent = { Text("${activity.description} • ${activity.createdAt.toReadableDate()}") },
                    leadingContent = { Icon(Icons.Outlined.History, null) }
                )
            }
        }
    }
}

@Composable private fun StatCard(title: String, value: String, icon: androidx.compose.ui.graphics.vector.ImageVector, modifier: Modifier) {
    ElevatedCard(modifier) { Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) { Icon(icon, null); Text(value, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold); Text(title) } }
}
@Composable private fun QuickActions(create: () -> Unit, tags: () -> Unit, folders: () -> Unit, collections: () -> Unit, backup: () -> Unit, activity: () -> Unit) {
    ElevatedCard { FlowRow(Modifier.padding(16.dp), horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Button(create) { Text("Quick create") }
        AssistChip(tags, { Text("Tags") }, leadingIcon = { Icon(Icons.Outlined.LocalOffer, null) })
        AssistChip(folders, { Text("Folders") }, leadingIcon = { Icon(Icons.Outlined.Folder, null) })
        AssistChip(collections, { Text("Collections") }, leadingIcon = { Icon(Icons.Outlined.CollectionsBookmark, null) })
        AssistChip(backup, { Text("Backup") }, leadingIcon = { Icon(Icons.Outlined.Backup, null) })
        AssistChip(activity, { Text("Activity") }, leadingIcon = { Icon(Icons.Outlined.History, null) })
    } }
}
@Composable private fun SectionHeader(title: String) { Text(title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold) }
