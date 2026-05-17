@file:OptIn(
    androidx.compose.foundation.layout.ExperimentalLayoutApi::class,
    androidx.compose.material3.ExperimentalMaterial3Api::class
)

package com.vaultmind.feature.knowledge

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.Article
import androidx.compose.material.icons.outlined.AttachFile
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Favorite
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Folder
import androidx.compose.material.icons.outlined.Link
import androidx.compose.material.icons.outlined.PushPin
import androidx.compose.material.icons.outlined.Save
import androidx.compose.material3.Button
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.vaultmind.core.common.toReadableDate
import com.vaultmind.core.designsystem.AttachmentPreview
import com.vaultmind.core.designsystem.CardTypeIcon
import com.vaultmind.core.designsystem.KnowledgeCardList
import com.vaultmind.core.designsystem.KnowledgeCardRow
import com.vaultmind.core.designsystem.TagChip
import com.vaultmind.core.designsystem.VaultEmpty
import com.vaultmind.core.designsystem.VaultError
import com.vaultmind.core.designsystem.VaultHeroCard
import com.vaultmind.core.designsystem.VaultInput
import com.vaultmind.core.designsystem.VaultLoading
import com.vaultmind.core.designsystem.VaultScreen
import com.vaultmind.core.designsystem.VaultSearchPill
import com.vaultmind.core.designsystem.VaultSectionHeader
import com.vaultmind.core.model.CardType
import com.vaultmind.core.model.KnowledgeCard

@Composable
fun KnowledgeListScreen(
    onOpenCard: (KnowledgeCard) -> Unit,
    onCreateCard: () -> Unit,
    viewModel: KnowledgeListViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Knowledge cards") },
                actions = { IconButton(onClick = onCreateCard) { Icon(Icons.Outlined.Edit, null) } }
            )
        }
    ) { padding ->
        Box(Modifier.padding(padding)) {
            when {
                state.loading -> VaultLoading("Loading cards smoothly…")
                state.error != null -> VaultError(state.error!!)
                state.cards.isEmpty() -> VaultEmpty(
                    title = "No cards yet",
                    message = "Collect notes, PDFs, links, code snippets, screenshots and research in one offline vault.",
                    actionLabel = "Create card",
                    onAction = onCreateCard
                )
                else -> VaultScreen {
                    Column(Modifier.fillMaxSize()) {
                        Column(Modifier.padding(horizontal = 20.dp, vertical = 14.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            VaultSearchPill(
                                text = "${state.cards.size} cards ready offline",
                                onClick = {},
                                placeholder = "Cards ready offline"
                            )
                            VaultSectionHeader(
                                title = "All knowledge",
                                subtitle = "Stable lazy-list keys and compact rows keep scrolling smooth even as the vault grows."
                            )
                        }
                        KnowledgeCardList(
                            cards = state.cards,
                            onCardClick = onOpenCard,
                            contentPadding = PaddingValues(start = 20.dp, top = 4.dp, end = 20.dp, bottom = 24.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun CardDetailScreen(
    onBack: () -> Unit,
    onEdit: (String) -> Unit,
    onOpenCard: (KnowledgeCard) -> Unit,
    viewModel: CardDetailViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val card = state.card
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(card?.title ?: "Card detail") },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Outlined.ArrowBack, null) } },
                actions = {
                    if (card != null) {
                        IconButton(onClick = { viewModel.togglePin() }) { Icon(Icons.Outlined.PushPin, null) }
                        IconButton(onClick = { viewModel.toggleFavorite() }) {
                            Icon(if (card.isFavorite) Icons.Outlined.Favorite else Icons.Outlined.FavoriteBorder, null)
                        }
                        IconButton(onClick = { onEdit(card.id) }) { Icon(Icons.Outlined.Edit, null) }
                        IconButton(onClick = { viewModel.delete(onBack) }) { Icon(Icons.Outlined.Delete, null) }
                    }
                }
            )
        }
    ) { padding ->
        Box(Modifier.padding(padding)) {
            when {
                state.loading -> VaultLoading("Opening card…")
                state.error != null && card == null -> VaultError(state.error!!)
                card != null -> CardDetailContent(card = card, related = state.related, onOpenCard = onOpenCard)
            }
        }
    }
}

@Composable
private fun CardDetailContent(
    card: KnowledgeCard,
    related: List<KnowledgeCard>,
    onOpenCard: (KnowledgeCard) -> Unit
) {
    VaultScreen {
        Column(
            Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            ElevatedCard(
                colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh),
                shape = androidx.compose.foundation.shape.RoundedCornerShape(30.dp)
            ) {
                Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    Row(verticalAlignment = Alignment.Top, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                        CardTypeIcon(card.type)
                        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(card.title.ifBlank { "Untitled card" }, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.ExtraBold)
                            Text(card.type.label, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        card.tags.forEach { TagChip(it) }
                        card.folder?.let { androidx.compose.material3.AssistChip(onClick = {}, label = { Text(it.name) }, leadingIcon = { Icon(Icons.Outlined.Folder, null) }) }
                        card.collections.forEach { androidx.compose.material3.AssistChip(onClick = {}, label = { Text(it.name) }) }
                    }
                    Text(
                        "Created ${card.createdAt.toReadableDate()} • Updated ${card.updatedAt.toReadableDate()}",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            if (!card.summary.isNullOrBlank()) {
                VaultSectionHeader("Summary")
                ElevatedCard(colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)) {
                    Text(card.summary.orEmpty(), Modifier.padding(18.dp), style = MaterialTheme.typography.bodyLarge)
                }
            }

            if (card.body.isNotBlank()) {
                VaultSectionHeader("Content")
                ElevatedCard(colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh)) {
                    Text(card.body, Modifier.padding(18.dp), style = MaterialTheme.typography.bodyLarge)
                }
            }

            if (!card.sourceLink.isNullOrBlank()) {
                ElevatedCard(colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh)) {
                    ListItem(
                        headlineContent = { Text("Source link", fontWeight = FontWeight.SemiBold) },
                        supportingContent = { Text(card.sourceLink.orEmpty()) },
                        leadingContent = { Icon(Icons.Outlined.Link, null) }
                    )
                }
            }

            if (card.attachments.isNotEmpty()) {
                VaultSectionHeader("Attachments", subtitle = "Previews are rendered with Coil and stable metadata rows.")
                card.attachments.forEach { AttachmentPreview(it) }
            }

            VaultSectionHeader("Related cards", subtitle = "Local AI-like suggestions from tags, title words, body words, folder, collection and type.")
            if (related.isEmpty()) {
                ElevatedCard(colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh)) {
                    Text(
                        "No strong local suggestions yet. Add tags, collections, folder context, or richer content to improve suggestions.",
                        Modifier.padding(18.dp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                related.forEach { KnowledgeCardRow(it, { onOpenCard(it) }, compact = true) }
            }
        }
    }
}

@Composable
fun CardEditScreen(
    onBack: () -> Unit,
    onSaved: (String) -> Unit,
    viewModel: CardEditViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            if (event is CardEditEvent.Saved) onSaved(event.cardId)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (state.id == null) "Create card" else "Edit card") },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Outlined.Close, null) } }
            )
        }
    ) { padding ->
        if (state.loading) {
            VaultLoading("Preparing editor…")
        } else {
            VaultScreen(modifier = Modifier.padding(padding)) {
                Column(
                    Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    VaultHeroCard(
                        title = if (state.id == null) "Capture something useful" else "Refine your knowledge card",
                        subtitle = "Use clean metadata now so offline search and related-card suggestions work better later.",
                        icon = Icons.AutoMirrored.Outlined.Article
                    )

                    EditorSection("Main content") {
                        VaultInput(state.title, viewModel::title, "Title", singleLine = true)
                        VaultInput(
                            state.body,
                            viewModel::body,
                            "Body / content",
                            modifier = Modifier.heightIn(min = 170.dp),
                            minLines = 7
                        )
                        VaultInput(state.summary, viewModel::summary, "Short summary", minLines = 2)
                        VaultInput(state.sourceLink, viewModel::source, "Source link", singleLine = true, leadingIcon = Icons.Outlined.Link)
                    }

                    EditorSection("Card type") {
                        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            CardType.entries.forEach { type ->
                                FilterChip(
                                    selected = state.type == type,
                                    onClick = { viewModel.type(type) },
                                    label = { Text(type.label) },
                                    leadingIcon = { CardTypeIcon(type, modifier = Modifier.size(28.dp)) }
                                )
                            }
                        }
                    }

                    EditorSection("Tags") {
                        if (state.tags.isEmpty()) Text("No tags yet. Create tags from the Tags screen.", color = MaterialTheme.colorScheme.onSurfaceVariant) else {
                            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                state.tags.forEach { tag ->
                                    TagChip(tag = tag, selected = tag.id in state.selectedTagIds, onClick = { viewModel.toggleTag(tag.id) })
                                }
                            }
                        }
                    }

                    EditorSection("Folder") {
                        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            FilterChip(state.selectedFolderId == null, { viewModel.folder(null) }, label = { Text("No folder") })
                            state.folders.forEach { folder ->
                                FilterChip(state.selectedFolderId == folder.id, { viewModel.folder(folder.id) }, label = { Text(folder.name) })
                            }
                        }
                    }

                    EditorSection("Collections") {
                        if (state.collections.isEmpty()) Text("No collections yet. Create collections to reuse cards across contexts.", color = MaterialTheme.colorScheme.onSurfaceVariant) else {
                            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                state.collections.forEach { collection ->
                                    FilterChip(collection.id in state.selectedCollectionIds, { viewModel.toggleCollection(collection.id) }, label = { Text(collection.name) })
                                }
                            }
                        }
                    }

                    EditorSection("Priority") {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(state.isPinned, viewModel::pin)
                            Text("Pinned")
                            Spacer(Modifier.width(18.dp))
                            Checkbox(state.isFavorite, viewModel::favorite)
                            Text("Favorite")
                        }
                    }

                    EditorSection("Attachments") {
                        ListItem(
                            headlineContent = { Text("File picker integration point") },
                            supportingContent = { Text("Connect ActivityResultContracts.OpenDocument here, persist URI permissions, and save AttachmentEntity metadata.") },
                            leadingContent = { Icon(Icons.Outlined.AttachFile, null) }
                        )
                    }

                    if (state.error != null) Text(state.error!!, color = MaterialTheme.colorScheme.error)
                    if (state.saving) LinearProgressIndicator(Modifier.fillMaxWidth())
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                        OutlinedButton(onClick = onBack, modifier = Modifier.weight(1f)) { Text("Cancel") }
                        Button(onClick = viewModel::save, enabled = !state.saving, modifier = Modifier.weight(1f)) {
                            Icon(Icons.Outlined.Save, null)
                            Spacer(Modifier.width(8.dp))
                            Text("Save")
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun EditorSection(title: String, content: @Composable ColumnScope.() -> Unit) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = androidx.compose.foundation.shape.RoundedCornerShape(28.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh)
    ) {
        Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.ExtraBold)
            content()
        }
    }
}
