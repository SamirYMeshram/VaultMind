@file:OptIn(
    androidx.compose.foundation.layout.ExperimentalLayoutApi::class,
    androidx.compose.material3.ExperimentalMaterial3Api::class
)

package com.vaultmind.feature.dashboard

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.automirrored.outlined.Article
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.Backup
import androidx.compose.material.icons.outlined.CollectionsBookmark
import androidx.compose.material.icons.outlined.Favorite
import androidx.compose.material.icons.outlined.Folder
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.LocalOffer
import androidx.compose.material.icons.outlined.PushPin
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.AssistChip
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.vaultmind.core.common.toReadableDate
import com.vaultmind.core.designsystem.KnowledgeCardRow
import com.vaultmind.core.designsystem.TagChip
import com.vaultmind.core.designsystem.VaultActionChip
import com.vaultmind.core.designsystem.VaultEmpty
import com.vaultmind.core.designsystem.VaultError
import com.vaultmind.core.designsystem.VaultHeroCard
import com.vaultmind.core.designsystem.VaultLoading
import com.vaultmind.core.designsystem.VaultMetricCard
import com.vaultmind.core.designsystem.VaultScreen
import com.vaultmind.core.designsystem.VaultSearchPill
import com.vaultmind.core.designsystem.VaultSectionHeader
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
    when (val current = state) {
        DashboardUiState.Loading -> VaultLoading("Opening your knowledge vault…")
        is DashboardUiState.Error -> VaultError(current.message)
        is DashboardUiState.Success -> DashboardContent(
            state = current,
            onOpenCard = onOpenCard,
            onCreateCard = onCreateCard,
            onSearch = onSearch,
            onOpenTags = onOpenTags,
            onOpenFolders = onOpenFolders,
            onOpenCollections = onOpenCollections,
            onOpenBackup = onOpenBackup,
            onOpenActivity = onOpenActivity
        )
    }
}

@Composable
private fun DashboardContent(
    state: DashboardUiState.Success,
    onOpenCard: (KnowledgeCard) -> Unit,
    onCreateCard: () -> Unit,
    onSearch: () -> Unit,
    onOpenTags: () -> Unit,
    onOpenFolders: () -> Unit,
    onOpenCollections: () -> Unit,
    onOpenBackup: () -> Unit,
    onOpenActivity: () -> Unit
) {
    val listState = rememberLazyListState()
    val data = state.data
    VaultScreen {
        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(20.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            item(contentType = "hero") {
                VaultHeroCard(
                    title = "VaultMind",
                    subtitle = "A private offline knowledge command center for notes, files, links, screenshots, research and code.",
                    icon = Icons.Outlined.AutoAwesome,
                    actionLabel = "Create knowledge",
                    onAction = onCreateCard
                )
            }

            item(contentType = "search") {
                VaultSearchPill(text = "", onClick = onSearch, placeholder = "Instant offline search across your vault")
            }

            item(contentType = "stats") {
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    maxItemsInEachRow = 2
                ) {
                    VaultMetricCard("Cards", data.stats.totalCards.toString(), Icons.AutoMirrored.Outlined.Article, Modifier.weight(1f), "All saved knowledge")
                    VaultMetricCard("Pinned", data.stats.pinnedCards.toString(), Icons.Outlined.PushPin, Modifier.weight(1f), "Priority items")
                    VaultMetricCard("Favorites", data.stats.favoriteCards.toString(), Icons.Outlined.Favorite, Modifier.weight(1f), "Reusable references")
                    VaultMetricCard("Tags", data.stats.totalTags.toString(), Icons.Outlined.LocalOffer, Modifier.weight(1f), "Connected topics")
                }
            }

            item(contentType = "quick-actions") {
                QuickActions(
                    create = onCreateCard,
                    search = onSearch,
                    tags = onOpenTags,
                    folders = onOpenFolders,
                    collections = onOpenCollections,
                    backup = onOpenBackup,
                    activity = onOpenActivity
                )
            }

            item(contentType = "recent-header") {
                VaultSectionHeader(
                    title = "Continue where you left off",
                    subtitle = "Recently updated cards are cached and shown with stable lazy-list keys."
                )
            }
            if (data.recentCards.isEmpty()) {
                item(contentType = "empty-recent") {
                    Box(Modifier.fillParentMaxHeight(0.55f)) {
                        VaultEmpty("No cards yet", "Create your first knowledge card and VaultMind will start building your local memory.", "Create card", onCreateCard)
                    }
                }
            } else {
                items(
                    items = data.recentCards,
                    key = { "recent-${it.id}" },
                    contentType = { "recent-${it.type.name}" }
                ) { card ->
                    KnowledgeCardRow(card = card, onClick = { onOpenCard(card) })
                }
            }

            item(contentType = "pinned-header") {
                VaultSectionHeader(
                    title = "Pinned knowledge",
                    subtitle = "Important cards stay one tap away."
                )
            }
            item(contentType = "pinned-row") {
                if (data.pinnedCards.isEmpty()) {
                    EmptyDashboardCard("Pin important cards from the detail screen to make this area useful.")
                } else {
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        items(data.pinnedCards, key = { "pinned-row-${it.id}" }, contentType = { "pinned-card" }) { card ->
                            KnowledgeCardRow(
                                card = card,
                                onClick = { onOpenCard(card) },
                                modifier = Modifier.width(320.dp),
                                compact = true
                            )
                        }
                    }
                }
            }

            item(contentType = "tags-header") {
                VaultSectionHeader(title = "Top tags", subtitle = "Fast topic jumping without searching manually.")
            }
            item(contentType = "tag-row") {
                if (data.topTags.isEmpty()) EmptyDashboardCard("Tags will appear here after you create or assign them.") else {
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        data.topTags.forEach { TagChip(it) }
                    }
                }
            }

            item(contentType = "activity-header") {
                VaultSectionHeader(
                    title = "Recent activity",
                    subtitle = "Every create, edit, view, search and backup event is tracked locally.",
                    actionLabel = "View all",
                    onAction = onOpenActivity
                )
            }
            if (data.recentActivity.isEmpty()) {
                item(contentType = "empty-activity") { EmptyDashboardCard("Activity will build automatically as you use the vault.") }
            } else {
                items(data.recentActivity.take(6), key = { "activity-${it.id}" }, contentType = { "activity" }) { activity ->
                    ElevatedCard(
                        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh)
                    ) {
                        ListItem(
                            headlineContent = { Text(activity.title, fontWeight = FontWeight.SemiBold) },
                            supportingContent = { Text("${activity.description} • ${activity.createdAt.toReadableDate()}") },
                            leadingContent = { Icon(Icons.Outlined.History, null) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun QuickActions(
    create: () -> Unit,
    search: () -> Unit,
    tags: () -> Unit,
    folders: () -> Unit,
    collections: () -> Unit,
    backup: () -> Unit,
    activity: () -> Unit
) {
    ElevatedCard(
        shape = androidx.compose.foundation.shape.RoundedCornerShape(28.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh)
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text("Quick actions", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.ExtraBold)
                    Text("Create, connect and maintain the vault quickly.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                FilledTonalButton(onClick = create) {
                    Icon(Icons.Outlined.Add, null)
                    Text("New")
                }
            }
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                VaultActionChip("Search", Icons.Outlined.Search, search)
                VaultActionChip("Tags", Icons.Outlined.LocalOffer, tags)
                VaultActionChip("Folders", Icons.Outlined.Folder, folders)
                VaultActionChip("Collections", Icons.Outlined.CollectionsBookmark, collections)
                VaultActionChip("Backup", Icons.Outlined.Backup, backup)
                VaultActionChip("Activity", Icons.Outlined.History, activity)
            }
        }
    }
}

@Composable
private fun EmptyDashboardCard(message: String) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh)
    ) {
        Text(message, modifier = Modifier.padding(18.dp), color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
