@file:OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class, androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.vaultmind.feature.search

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.vaultmind.core.designsystem.*
import com.vaultmind.core.model.*

@Composable
fun SearchScreen(onOpenCard: (KnowledgeCard) -> Unit, viewModel: SearchViewModel = hiltViewModel()) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    Scaffold(topBar = { TopAppBar(title = { Text("Offline search") }) }) { padding ->
        Column(Modifier.padding(padding).fillMaxSize()) {
            OutlinedTextField(
                value = state.query,
                onValueChange = viewModel::query,
                label = { Text("Search title, content, tags, folders, collections, attachments") },
                leadingIcon = { Icon(Icons.Outlined.Search, null) },
                singleLine = true,
                modifier = Modifier.padding(16.dp).fillMaxWidth(),
                keyboardActions = androidx.compose.foundation.text.KeyboardActions(onSearch = { viewModel.submitSearch() })
            )
            FlowRow(Modifier.padding(horizontal = 16.dp), horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(state.pinnedOnly, { viewModel.pinnedOnly(!state.pinnedOnly) }, label = { Text("Pinned") })
                FilterChip(state.favoritesOnly, { viewModel.favoritesOnly(!state.favoritesOnly) }, label = { Text("Favorites") })
                CardType.entries.take(6).forEach { FilterChip(it in state.selectedTypes, { viewModel.toggleType(it) }, label = { Text(it.label) }) }
            }
            if (state.query.isBlank() && state.recentSearches.isNotEmpty()) {
                Text("Recent searches", Modifier.padding(16.dp), style = MaterialTheme.typography.titleMedium)
                FlowRow(Modifier.padding(horizontal = 16.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) { state.recentSearches.take(10).forEach { AssistChip({ viewModel.applyHistory(it.query) }, { Text(it.query) }) } }
            }
            when {
                state.loading -> LinearProgressIndicator(Modifier.fillMaxWidth())
                state.error != null -> VaultError(state.error!!)
                state.results.isEmpty() -> VaultEmpty("No matching cards", "Try another keyword, remove filters, or create a new card from your research.")
                else -> KnowledgeCardList(state.results, onOpenCard)
            }
        }
    }
}
