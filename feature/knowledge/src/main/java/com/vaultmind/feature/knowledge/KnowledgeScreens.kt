@file:OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class, androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.vaultmind.feature.knowledge

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.vaultmind.core.common.toReadableDate
import com.vaultmind.core.designsystem.*
import com.vaultmind.core.model.*

@Composable
fun KnowledgeListScreen(onOpenCard: (KnowledgeCard) -> Unit, onCreateCard: () -> Unit, viewModel: KnowledgeListViewModel = hiltViewModel()) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    Scaffold(topBar = { TopAppBar(title = { Text("Knowledge cards") }, actions = { IconButton(onCreateCard) { Icon(Icons.Outlined.Add, null) } }) }) { padding ->
        Box(Modifier.padding(padding)) {
            when {
                state.loading -> VaultLoading()
                state.error != null -> VaultError(state.error!!)
                state.cards.isEmpty() -> VaultEmpty("No cards", "Collect notes, PDFs, links, code snippets, screenshots, and research.", "Create card", onCreateCard)
                else -> KnowledgeCardList(state.cards, onOpenCard)
            }
        }
    }
}

@Composable
fun CardDetailScreen(onBack: () -> Unit, onEdit: (String) -> Unit, onOpenCard: (KnowledgeCard) -> Unit, viewModel: CardDetailViewModel = hiltViewModel()) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val card = state.card
    Scaffold(topBar = {
        TopAppBar(title = { Text(card?.title ?: "Card") }, navigationIcon = { IconButton(onBack) { Icon(Icons.Outlined.ArrowBack, null) } }, actions = {
            if (card != null) {
                IconButton({ viewModel.togglePin() }) { Icon(if (card.isPinned) Icons.Outlined.PushPin else Icons.Outlined.PushPin, null) }
                IconButton({ viewModel.toggleFavorite() }) { Icon(if (card.isFavorite) Icons.Outlined.Favorite else Icons.Outlined.FavoriteBorder, null) }
                IconButton({ onEdit(card.id) }) { Icon(Icons.Outlined.Edit, null) }
                IconButton({ viewModel.delete(onBack) }) { Icon(Icons.Outlined.Delete, null) }
            }
        })
    }) { padding ->
        Box(Modifier.padding(padding)) {
            when {
                state.loading -> VaultLoading()
                state.error != null && card == null -> VaultError(state.error!!)
                card != null -> Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Text(card.title, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.ExtraBold)
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) { card.tags.forEach { TagChip(it) }; AssistChip({}, { Text(card.type.label) }) }
                    if (card.summary?.isNotBlank() == true) ElevatedCard { Text(card.summary!!, Modifier.padding(16.dp)) }
                    if (card.body.isNotBlank()) Text(card.body, style = MaterialTheme.typography.bodyLarge)
                    if (!card.sourceLink.isNullOrBlank()) ListItem(headlineContent = { Text("Source") }, supportingContent = { Text(card.sourceLink!!) }, leadingContent = { Icon(Icons.Outlined.Link, null) })
                    Text("Created ${card.createdAt.toReadableDate()} • Updated ${card.updatedAt.toReadableDate()}", style = MaterialTheme.typography.labelMedium)
                    if (card.attachments.isNotEmpty()) { Text("Attachments", style = MaterialTheme.typography.titleLarge); card.attachments.forEach { AttachmentPreview(it) } }
                    Text("Related cards", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    if (state.related.isEmpty()) Text("No strong local suggestions yet. Shared tags, folders, collections, and similar text will appear here.")
                    state.related.forEach { KnowledgeCardRow(it, { onOpenCard(it) }) }
                }
            }
        }
    }
}

@Composable
fun CardEditScreen(onBack: () -> Unit, onSaved: (String) -> Unit, viewModel: CardEditViewModel = hiltViewModel()) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    LaunchedEffect(Unit) { viewModel.events.collect { if (it is CardEditEvent.Saved) onSaved(it.cardId) } }
    Scaffold(topBar = { TopAppBar(title = { Text(if (state.id == null) "Create card" else "Edit card") }, navigationIcon = { IconButton(onBack) { Icon(Icons.Outlined.Close, null) } }) }) { padding ->
        if (state.loading) VaultLoading() else Column(Modifier.padding(padding).fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            OutlinedTextField(state.title, viewModel::title, label = { Text("Title") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
            OutlinedTextField(state.body, viewModel::body, label = { Text("Body / content") }, modifier = Modifier.fillMaxWidth().heightIn(min = 160.dp), minLines = 6)
            Text("Card type", style = MaterialTheme.typography.titleMedium)
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) { CardType.entries.forEach { FilterChip(state.type == it, { viewModel.type(it) }, label = { Text(it.label) }) } }
            OutlinedTextField(state.sourceLink, viewModel::source, label = { Text("Source link") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(state.summary, viewModel::summary, label = { Text("Summary") }, modifier = Modifier.fillMaxWidth(), minLines = 2)
            Text("Tags", style = MaterialTheme.typography.titleMedium)
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) { state.tags.forEach { FilterChip(it.id in state.selectedTagIds, { viewModel.toggleTag(it.id) }, label = { Text(it.name) }) } }
            Text("Folder", style = MaterialTheme.typography.titleMedium)
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) { FilterChip(state.selectedFolderId == null, { viewModel.folder(null) }, label = { Text("No folder") }); state.folders.forEach { FilterChip(state.selectedFolderId == it.id, { viewModel.folder(it.id) }, label = { Text(it.name) }) } }
            Text("Collections", style = MaterialTheme.typography.titleMedium)
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) { state.collections.forEach { FilterChip(it.id in state.selectedCollectionIds, { viewModel.toggleCollection(it.id) }, label = { Text(it.name) }) } }
            Row { Checkbox(state.isPinned, viewModel::pin); Text("Pinned", Modifier.padding(top = 12.dp)); Spacer(Modifier.width(16.dp)); Checkbox(state.isFavorite, viewModel::favorite); Text("Favorite", Modifier.padding(top = 12.dp)) }
            ElevatedCard { ListItem(headlineContent = { Text("Attachments") }, supportingContent = { Text("File picker integration point: connect ActivityResultContracts.OpenDocument here and persist URI permissions.") }, leadingContent = { Icon(Icons.Outlined.AttachFile, null) }) }
            if (state.error != null) Text(state.error!!, color = MaterialTheme.colorScheme.error)
            Button(onClick = viewModel::save, enabled = !state.saving, modifier = Modifier.fillMaxWidth()) { if (state.saving) CircularProgressIndicator(Modifier.size(18.dp)) else Text("Save card") }
        }
    }
}
