@file:OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class, androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.vaultmind.feature.folders

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
fun FoldersScreen(onBack: () -> Unit, viewModel: FoldersViewModel = hiltViewModel()) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var dialog by remember { mutableStateOf(false) }
    Scaffold(topBar = { TopAppBar(title = { Text("Folders") }, navigationIcon = { IconButton(onBack) { Icon(Icons.Outlined.ArrowBack, null) } }, actions = { IconButton({ dialog = true }) { Icon(Icons.Outlined.CreateNewFolder, null) } }) }) { padding ->
        when {
            state.loading -> VaultLoading()
            state.error != null -> VaultError(state.error!!)
            else -> Row(Modifier.padding(padding).fillMaxSize()) {
                LazyColumn(Modifier.weight(1f), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (state.folders.isEmpty()) item { VaultEmpty("No folders", "Use folders for broad organization such as Classes, Research, Work, or Writing.", "Create folder") { dialog = true } }
                    items(state.folders, key = { it.id }) { folder -> ElevatedCard(onClick = { viewModel.select(folder.id) }) { ListItem(headlineContent = { Text(folder.name, fontWeight = FontWeight.SemiBold) }, supportingContent = { Text("${folder.cardCount} cards") }, leadingContent = { Icon(Icons.Outlined.Folder, null) }, trailingContent = { IconButton({ viewModel.delete(folder.id) }) { Icon(Icons.Outlined.Delete, null) } }) } }
                }
                if (state.selectedFolder != null) Column(Modifier.weight(1.2f).padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(state.selectedFolder!!.name, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    Text(state.selectedFolder!!.description ?: "No description")
                    Text("Folder statistics: ${state.selectedCards.size} cards")
                    state.selectedCards.take(6).forEach { KnowledgeCardRow(it, {}) }
                }
            }
        }
    }
    if (dialog) FolderDialog({ dialog = false }) { name, desc -> viewModel.save(null, name, desc); dialog = false }
}
@Composable private fun FolderDialog(onDismiss: () -> Unit, onSave: (String, String?) -> Unit) { var name by remember { mutableStateOf("") }; var desc by remember { mutableStateOf("") }; AlertDialog(onDismissRequest = onDismiss, title = { Text("Folder") }, text = { Column(verticalArrangement = Arrangement.spacedBy(8.dp)) { OutlinedTextField(name, { name = it }, label = { Text("Name") }); OutlinedTextField(desc, { desc = it }, label = { Text("Description") }) } }, confirmButton = { Button({ onSave(name, desc.ifBlank { null }) }) { Text("Save") } }, dismissButton = { TextButton(onDismiss) { Text("Cancel") } }) }
