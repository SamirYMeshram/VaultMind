@file:OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class, androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.vaultmind.feature.tags

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.vaultmind.core.designsystem.*

@Composable
fun TagsScreen(onBack: () -> Unit, viewModel: TagsViewModel = hiltViewModel()) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var editingName by remember { mutableStateOf<String?>(null) }
    Scaffold(topBar = { TopAppBar(title = { Text("Tags") }, navigationIcon = { IconButton(onBack) { Icon(Icons.Outlined.ArrowBack, null) } }, actions = { IconButton({ editingName = "" }) { Icon(Icons.Outlined.Add, null) } }) }) { padding ->
        when {
            state.loading -> VaultLoading()
            state.error != null -> VaultError(state.error!!)
            else -> Row(Modifier.padding(padding).fillMaxSize()) {
                LazyColumn(Modifier.weight(1f), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (state.tags.isEmpty()) item { VaultEmpty("No tags", "Create tags to connect cards across folders.", "Create tag") { editingName = "" } }
                    items(state.tags, key = { it.id }) { tag ->
                        ElevatedCard(onClick = { viewModel.select(tag.id) }, Modifier.fillMaxWidth()) { ListItem(headlineContent = { Text(tag.name, fontWeight = FontWeight.SemiBold) }, supportingContent = { Text("${tag.useCount} cards") }, trailingContent = { IconButton({ viewModel.delete(tag.id) }) { Icon(Icons.Outlined.Delete, null) } }) }
                    }
                }
                if (state.selectedTag != null) Column(Modifier.weight(1.2f).padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Tag detail", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    TagChip(state.selectedTag!!)
                    Text("Cards inside this tag")
                    if (state.selectedCards.isEmpty()) Text("No cards assigned yet.") else state.selectedCards.take(6).forEach { KnowledgeCardRow(it, {}) }
                }
            }
        }
    }
    if (editingName != null) NameDialog("Tag name", editingName.orEmpty(), { editingName = null }) { name -> viewModel.save(null, name); editingName = null }
}
@Composable private fun NameDialog(title: String, initial: String, onDismiss: () -> Unit, onSave: (String) -> Unit) { var text by remember(initial) { mutableStateOf(initial) }; AlertDialog(onDismissRequest = onDismiss, title = { Text(title) }, text = { OutlinedTextField(text, { text = it }, singleLine = true) }, confirmButton = { Button({ onSave(text) }) { Text("Save") } }, dismissButton = { TextButton(onDismiss) { Text("Cancel") } }) }
