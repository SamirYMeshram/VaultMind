@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.vaultmind.feature.collections

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.CollectionsBookmark
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.vaultmind.core.designsystem.KnowledgeCardRow
import com.vaultmind.core.designsystem.VaultEmpty
import com.vaultmind.core.designsystem.VaultError
import com.vaultmind.core.designsystem.VaultHeroCard
import com.vaultmind.core.designsystem.VaultLoading
import com.vaultmind.core.designsystem.VaultScreen
import com.vaultmind.core.designsystem.VaultSectionHeader

@Composable
fun CollectionsScreen(
    onBack: () -> Unit,
    viewModel: CollectionsViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var dialog by remember { mutableStateOf(false) }
    val listState = rememberLazyListState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Collections") },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Outlined.ArrowBack, null) } },
                actions = { IconButton(onClick = { dialog = true }) { Icon(Icons.Outlined.Add, null) } }
            )
        }
    ) { padding ->
        when {
            state.loading -> VaultLoading("Loading collections…")
            state.error != null -> VaultError(state.error!!)
            else -> VaultScreen(modifier = Modifier.padding(padding)) {
                LazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    item(contentType = "hero") {
                        VaultHeroCard(
                            title = "Flexible collections",
                            subtitle = "A card can live in multiple contexts such as Exam Prep, Book Draft, Startup Ideas or Research Themes.",
                            icon = Icons.Outlined.CollectionsBookmark,
                            actionLabel = "Create collection",
                            onAction = { dialog = true }
                        )
                    }
                    item(contentType = "header") { VaultSectionHeader("All collections", subtitle = "Select a collection to preview its cards.") }
                    if (state.collections.isEmpty()) {
                        item(contentType = "empty") { VaultEmpty("No collections", "Collections let one card live in many contexts.", "Create collection") { dialog = true } }
                    } else {
                        items(state.collections, key = { "collection-${it.id}" }, contentType = { "collection" }) { collection ->
                            ElevatedCard(
                                onClick = { viewModel.select(collection.id) },
                                modifier = Modifier.fillMaxWidth(),
                                shape = androidx.compose.foundation.shape.RoundedCornerShape(24.dp),
                                colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh)
                            ) {
                                ListItem(
                                    headlineContent = { Text(collection.name, fontWeight = FontWeight.ExtraBold) },
                                    supportingContent = { Text("${collection.cardCount} card${if (collection.cardCount == 1) "" else "s"} • ${collection.description ?: "No description"}") },
                                    leadingContent = { Icon(Icons.Outlined.CollectionsBookmark, null) },
                                    trailingContent = { IconButton(onClick = { viewModel.delete(collection.id) }) { Icon(Icons.Outlined.Delete, null) } }
                                )
                            }
                        }
                    }
                    if (state.selectedCollection != null) {
                        item(contentType = "selected-collection-header") {
                            VaultSectionHeader(
                                title = state.selectedCollection!!.name,
                                subtitle = "${state.selectedCards.size} card${if (state.selectedCards.size == 1) "" else "s"} in this collection."
                            )
                        }
                        if (state.selectedCards.isEmpty()) {
                            item(contentType = "selected-empty") {
                                ElevatedCard(colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh)) {
                                    Text("No cards assigned to this collection yet.", Modifier.padding(18.dp), color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                        } else {
                            items(state.selectedCards, key = { "collection-card-${it.id}" }, contentType = { "collection-card" }) { card ->
                                KnowledgeCardRow(card, onClick = {}, compact = true)
                            }
                        }
                    }
                }
            }
        }
    }

    if (dialog) {
        CollectionDialog(
            onDismiss = { dialog = false },
            onSave = { name, desc ->
                viewModel.save(null, name, desc)
                dialog = false
            }
        )
    }
}

@Composable
private fun CollectionDialog(onDismiss: () -> Unit, onSave: (String, String?) -> Unit) {
    var name by remember { mutableStateOf("") }
    var desc by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Collection") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(name, { name = it }, label = { Text("Name") }, singleLine = true)
                OutlinedTextField(desc, { desc = it }, label = { Text("Description") })
            }
        },
        confirmButton = { Button(onClick = { onSave(name, desc.ifBlank { null }) }) { Text("Save") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}
