@file:OptIn(
    androidx.compose.foundation.layout.ExperimentalLayoutApi::class,
    androidx.compose.material3.ExperimentalMaterial3Api::class
)

package com.vaultmind.feature.search

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Favorite
import androidx.compose.material.icons.outlined.FilterList
import androidx.compose.material.icons.outlined.PushPin
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.AssistChip
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.vaultmind.core.designsystem.KnowledgeCardList
import com.vaultmind.core.designsystem.VaultEmpty
import com.vaultmind.core.designsystem.VaultError
import com.vaultmind.core.designsystem.VaultHeroCard
import com.vaultmind.core.designsystem.VaultLinearLoading
import com.vaultmind.core.designsystem.VaultScreen
import com.vaultmind.core.designsystem.VaultSectionHeader
import com.vaultmind.core.model.CardType
import com.vaultmind.core.model.KnowledgeCard

@Composable
fun SearchScreen(
    onOpenCard: (KnowledgeCard) -> Unit,
    viewModel: SearchViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    Scaffold(topBar = { TopAppBar(title = { Text("Offline search") }) }) { padding ->
        VaultScreen(modifier = Modifier.padding(padding)) {
            Column(Modifier.fillMaxSize()) {
                Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    VaultHeroCard(
                        title = "Find anything fast",
                        subtitle = "Local ranking prioritizes title, tags, collections, folders, body content, pinned cards and recent updates.",
                        icon = Icons.Outlined.Search
                    )
                    ElevatedCard(
                        shape = androidx.compose.foundation.shape.RoundedCornerShape(28.dp),
                        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh)
                    ) {
                        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            OutlinedTextField(
                                value = state.query,
                                onValueChange = viewModel::query,
                                label = { Text("Search title, content, tags, folders, collections, attachments") },
                                leadingIcon = { Icon(Icons.Outlined.Search, null) },
                                trailingIcon = {
                                    if (state.query.isNotBlank()) {
                                        IconButton(onClick = { viewModel.query("") }) { Icon(Icons.Outlined.Close, null) }
                                    }
                                },
                                singleLine = true,
                                shape = androidx.compose.foundation.shape.RoundedCornerShape(22.dp),
                                modifier = Modifier.fillMaxWidth(),
                                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                                keyboardActions = KeyboardActions(onSearch = { viewModel.submitSearch() })
                            )
                            FilterSection(state = state, viewModel = viewModel)
                        }
                    }

                    if (state.query.isBlank() && state.recentSearches.isNotEmpty()) {
                        VaultSectionHeader("Recent searches", subtitle = "Tap a previous query to run it again.")
                        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            state.recentSearches.take(10).forEach { history ->
                                AssistChip(onClick = { viewModel.applyHistory(history.query) }, label = { Text(history.query) })
                            }
                        }
                    }
                }

                when {
                    state.loading -> VaultLinearLoading()
                    state.error != null -> VaultError(state.error!!)
                    state.query.isBlank() -> VaultEmpty(
                        title = if (state.recentSearches.isEmpty()) "Search your vault" else "Ready when you are",
                        message = if (state.recentSearches.isEmpty()) {
                            "Try a title, topic, tag, collection, folder, source link, summary or attachment name."
                        } else {
                            "Pick a recent search above or type a new keyword to rank matching cards offline."
                        }
                    )
                    state.results.isEmpty() -> VaultEmpty(
                        title = "No matching cards",
                        message = "Try another keyword, remove filters, or create a new card from your research."
                    )
                    else -> {
                        Row(Modifier.padding(horizontal = 20.dp, vertical = 4.dp)) {
                            Text(
                                "${state.results.size} ranked result${if (state.results.size == 1) "" else "s"}",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        KnowledgeCardList(
                            cards = state.results,
                            onCardClick = onOpenCard,
                            contentPadding = PaddingValues(start = 20.dp, top = 8.dp, end = 20.dp, bottom = 28.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun FilterSection(state: SearchUiState, viewModel: SearchViewModel) {
    VaultSectionHeader("Smart filters", subtitle = "Combine filters without leaving offline mode.")
    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        FilterChip(
            selected = state.pinnedOnly,
            onClick = { viewModel.pinnedOnly(!state.pinnedOnly) },
            label = { Text("Pinned") },
            leadingIcon = { Icon(Icons.Outlined.PushPin, null) }
        )
        FilterChip(
            selected = state.favoritesOnly,
            onClick = { viewModel.favoritesOnly(!state.favoritesOnly) },
            label = { Text("Favorites") },
            leadingIcon = { Icon(Icons.Outlined.Favorite, null) }
        )
        AssistChip(onClick = {}, label = { Text("Title > tags > collections > folders > body") }, leadingIcon = { Icon(Icons.Outlined.FilterList, null) })
    }
    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        CardType.entries.forEach { type ->
            FilterChip(
                selected = type in state.selectedTypes,
                onClick = { viewModel.toggleType(type) },
                label = { Text(type.label) }
            )
        }
    }
}
