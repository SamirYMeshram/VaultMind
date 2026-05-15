@file:OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class, androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.vaultmind.core.designsystem

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.vaultmind.core.common.toReadableDate
import com.vaultmind.core.model.*

@Composable fun VaultLoading(message: String = "Loading vault…") {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(12.dp)) { CircularProgressIndicator(); Text(message) }
    }
}
@Composable fun VaultEmpty(title: String, message: String, actionLabel: String? = null, onAction: (() -> Unit)? = null) {
    Box(Modifier.fillMaxSize().padding(24.dp), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Icon(Icons.Outlined.Inventory2, null, modifier = Modifier.size(52.dp)); Text(title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold); Text(message)
            if (actionLabel != null && onAction != null) Button(onClick = onAction) { Text(actionLabel) }
        }
    }
}
@Composable fun VaultError(message: String, onRetry: (() -> Unit)? = null) {
    Column(Modifier.fillMaxSize().padding(24.dp), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(Icons.Outlined.ErrorOutline, null, tint = MaterialTheme.colorScheme.error); Text("Something went wrong", fontWeight = FontWeight.Bold); Text(message)
        if (onRetry != null) Button(onClick = onRetry) { Text("Retry") }
    }
}
@Composable fun TagChip(tag: Tag) = AssistChip(onClick = {}, label = { Text(tag.name) })
@Composable fun KnowledgeCardRow(card: KnowledgeCard, onClick: () -> Unit, modifier: Modifier = Modifier) {
    ElevatedCard(onClick = onClick, modifier = modifier.fillMaxWidth(), shape = RoundedCornerShape(20.dp)) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(card.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f))
                if (card.isPinned) Icon(Icons.Outlined.PushPin, null, modifier = Modifier.size(18.dp))
                if (card.isFavorite) Icon(Icons.Outlined.Favorite, null, modifier = Modifier.size(18.dp))
            }
            Text(card.body.ifBlank { card.summary ?: card.sourceLink ?: card.type.label }, maxLines = 2, overflow = TextOverflow.Ellipsis)
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                card.tags.take(4).forEach { TagChip(it) }
                AssistChip(onClick = {}, label = { Text(card.type.label) })
            }
            Text(card.updatedAt.toReadableDate(), style = MaterialTheme.typography.labelSmall)
        }
    }
}
@Composable fun KnowledgeCardList(cards: List<KnowledgeCard>, onCardClick: (KnowledgeCard) -> Unit, modifier: Modifier = Modifier) {
    LazyColumn(modifier.fillMaxSize(), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        items(cards, key = { it.id }) { KnowledgeCardRow(it, { onCardClick(it) }) }
    }
}
@Composable fun AttachmentPreview(attachment: Attachment) {
    ElevatedCard(Modifier.fillMaxWidth()) {
        Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            if (attachment.type == AttachmentType.IMAGE || attachment.type == AttachmentType.SCREENSHOT) AsyncImage(model = attachment.uri, contentDescription = attachment.displayName, modifier = Modifier.size(56.dp)) else Icon(Icons.Outlined.AttachFile, null)
            Column { Text(attachment.displayName, fontWeight = FontWeight.SemiBold); Text(attachment.mimeType, style = MaterialTheme.typography.labelMedium) }
        }
    }
}
