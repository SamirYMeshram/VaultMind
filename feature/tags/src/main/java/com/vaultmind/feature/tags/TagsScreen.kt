@file:OptIn(
    androidx.compose.foundation.layout.ExperimentalLayoutApi::class,
    androidx.compose.material3.ExperimentalMaterial3Api::class
)

package com.vaultmind.feature.tags

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.LocalOffer
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
import com.vaultmind.core.designsystem.TagChip
import com.vaultmind.core.designsystem.VaultEmpty
import com.vaultmind.core.designsystem.VaultError
import com.vaultmind.core.designsystem.VaultHeroCard
import com.vaultmind.core.designsystem.VaultLoading
import com.vaultmind.core.designsystem.VaultScreen
import com.vaultmind.core.designsystem.VaultSectionHeader

@Composable
fun TagsScreen(
    onBack: () -> Unit,
    viewModel: TagsViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var editingName by remember { mutableStateOf<String?>(null) }
    val listState = rememberLazyListState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Tags") },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Outlined.ArrowBack, null) } },
                actions = { IconButton(onClick = { editingName = "" }) { Icon(Icons.Outlined.Add, null) } }
            )
        }
    ) { padding ->
        when {
            state.loading -> VaultLoading("Loading tags…")
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
                            title = "Connected topics",
                            subtitle = "Tags are the fastest way to make offline related-card suggestions smarter.",
                            icon = Icons.Outlined.LocalOffer,
                            actionLabel = "Create tag",
                            onAction = { editingName = "" }
                        )
                    }
                    item(contentType = "tag-flow") {
                        VaultSectionHeader("All tags", subtitle = "Tap a tag to preview its cards.")
                        if (state.tags.isEmpty()) {
                            VaultEmpty("No tags", "Create tags to connect cards across folders and collections.", "Create tag") { editingName = "" }
                        } else {
                            FlowRow(
                                modifier = Modifier.padding(top = 12.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                state.tags.forEach { tag ->
                                    TagChip(tag = tag, selected = state.selectedTagId == tag.id, onClick = { viewModel.select(tag.id) })
                                }
                            }
                        }
                    }
                    items(state.tags, key = { "tag-row-${it.id}" }, contentType = { "tag-row" }) { tag ->
                        ElevatedCard(
                            onClick = { viewModel.select(tag.id) },
                            modifier = Modifier.fillMaxWidth(),
                            shape = androidx.compose.foundation.shape.RoundedCornerShape(24.dp),
                            colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh)
                        ) {
                            ListItem(
                                headlineContent = { Text(tag.name, fontWeight = FontWeight.ExtraBold) },
                                supportingContent = { Text("${tag.useCount} card${if (tag.useCount == 1) "" else "s"} connected") },
                                leadingContent = { Icon(Icons.Outlined.LocalOffer, null) },
                                trailingContent = { IconButton(onClick = { viewModel.delete(tag.id) }) { Icon(Icons.Outlined.Delete, null) } }
                            )
                        }
                    }
                    if (state.selectedTag != null) {
                        item(contentType = "selected-title") {
                            VaultSectionHeader("Cards tagged ${state.selectedTag!!.name}")
                        }
                        if (state.selectedCards.isEmpty()) {
                            item(contentType = "selected-empty") {
                                ElevatedCard(colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh)) {
                                    Text("No cards assigned yet.", Modifier.padding(18.dp), color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                        } else {
                            items(state.selectedCards, key = { "selected-tag-card-${it.id}" }, contentType = { "selected-tag-card" }) { card ->
                                KnowledgeCardRow(card, onClick = {}, compact = true)
                            }
                        }
                    }
                }
            }
        }
    }

    if (editingName != null) {
        NameDialog(
            title = "Tag name",
            initial = editingName.orEmpty(),
            onDismiss = { editingName = null },
            onSave = { name ->
                viewModel.save(null, name)
                editingName = null
            }
        )
    }
}

@Composable
private fun NameDialog(
    title: String,
    initial: String,
    onDismiss: () -> Unit,
    onSave: (String) -> Unit
) {
    var text by remember(initial) { mutableStateOf(initial) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = { OutlinedTextField(text, { text = it }, singleLine = true, label = { Text("Name") }) },
        confirmButton = { Button(onClick = { onSave(text) }) { Text("Save") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}
