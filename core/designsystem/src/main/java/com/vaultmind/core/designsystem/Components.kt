@file:OptIn(
    androidx.compose.foundation.layout.ExperimentalLayoutApi::class,
    androidx.compose.material3.ExperimentalMaterial3Api::class
)

package com.vaultmind.core.designsystem

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Article
import androidx.compose.material.icons.outlined.AttachFile
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.outlined.Code
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material.icons.outlined.Favorite
import androidx.compose.material.icons.outlined.Folder
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material.icons.outlined.Inventory2
import androidx.compose.material.icons.outlined.Lightbulb
import androidx.compose.material.icons.outlined.Link
import androidx.compose.material.icons.outlined.MoreHoriz
import androidx.compose.material.icons.outlined.PictureAsPdf
import androidx.compose.material.icons.outlined.PushPin
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.vaultmind.core.common.toReadableDate
import com.vaultmind.core.model.Attachment
import com.vaultmind.core.model.AttachmentType
import com.vaultmind.core.model.CardType
import com.vaultmind.core.model.KnowledgeCard
import com.vaultmind.core.model.Tag

@Stable
object VaultSpacing {
    val screen = 20.dp
    val section = 18.dp
    val card = 16.dp
    val compact = 10.dp
}

@Composable
fun VaultScreen(
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(VaultSpacing.screen),
    content: @Composable (PaddingValues) -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.08f),
                        MaterialTheme.colorScheme.surface,
                        MaterialTheme.colorScheme.surface
                    )
                )
            )
    ) {
        content(contentPadding)
    }
}

@Composable
fun VaultLoading(message: String = "Preparing your vault…") {
    VaultScreen { padding ->
        Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
            ElevatedCard(
                colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh),
                shape = RoundedCornerShape(28.dp)
            ) {
                Column(
                    Modifier.padding(28.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    CircularProgressIndicator(strokeWidth = 3.dp)
                    Text(message, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                    Text(
                        "Optimized for fast offline access",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
fun VaultEmpty(
    title: String,
    message: String,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null
) {
    Box(Modifier.fillMaxSize().padding(24.dp), contentAlignment = Alignment.Center) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(14.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Surface(
                color = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                shape = CircleShape,
                modifier = Modifier.size(72.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(Icons.Outlined.Inventory2, null, modifier = Modifier.size(34.dp))
                }
            }
            Text(title, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.ExtraBold)
            Text(
                message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.fillMaxWidth(0.82f)
            )
            if (actionLabel != null && onAction != null) {
                Button(onClick = onAction, shape = RoundedCornerShape(18.dp)) { Text(actionLabel) }
            }
        }
    }
}

@Composable
fun VaultError(message: String, onRetry: (() -> Unit)? = null) {
    Box(Modifier.fillMaxSize().padding(24.dp), contentAlignment = Alignment.Center) {
        OutlinedCard(
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.outlinedCardColors(containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.24f))
        ) {
            Column(
                Modifier.padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(Icons.Outlined.ErrorOutline, null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(36.dp))
                Text("Something went wrong", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Text(message, color = MaterialTheme.colorScheme.onSurfaceVariant)
                if (onRetry != null) TextButton(onClick = onRetry) { Text("Try again") }
            }
        }
    }
}

@Composable
fun VaultLinearLoading(modifier: Modifier = Modifier) {
    LinearProgressIndicator(modifier.fillMaxWidth())
}

@Composable
fun VaultSectionHeader(
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null
) {
    Row(modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.ExtraBold)
            if (subtitle != null) {
                Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        if (actionLabel != null && onAction != null) TextButton(onClick = onAction) { Text(actionLabel) }
    }
}

@Composable
fun VaultHeroCard(
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier,
    icon: ImageVector = Icons.Outlined.AutoAwesome,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null
) {
    ElevatedCard(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(32.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
    ) {
        Box(
            Modifier
                .fillMaxWidth()
                .background(
                    Brush.linearGradient(
                        listOf(
                            MaterialTheme.colorScheme.primaryContainer,
                            MaterialTheme.colorScheme.secondaryContainer,
                            MaterialTheme.colorScheme.tertiaryContainer
                        )
                    )
                )
                .padding(22.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(14.dp), modifier = Modifier.fillMaxWidth()) {
                Surface(
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.42f),
                    shape = CircleShape,
                    modifier = Modifier.size(46.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) { Icon(icon, null, modifier = Modifier.size(24.dp)) }
                }
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(title, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.ExtraBold)
                    Text(subtitle, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                if (actionLabel != null && onAction != null) {
                    Button(onClick = onAction, shape = RoundedCornerShape(18.dp)) { Text(actionLabel) }
                }
            }
        }
    }
}

@Composable
fun VaultMetricCard(
    title: String,
    value: String,
    icon: ImageVector,
    modifier: Modifier = Modifier,
    supportingText: String? = null
) {
    ElevatedCard(
        modifier = modifier,
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh)
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                    contentColor = MaterialTheme.colorScheme.primary,
                    shape = CircleShape,
                    modifier = Modifier.size(38.dp)
                ) { Box(contentAlignment = Alignment.Center) { Icon(icon, null, modifier = Modifier.size(20.dp)) } }
                Spacer(Modifier.weight(1f))
                Icon(Icons.Outlined.MoreHoriz, null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Text(value, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.ExtraBold)
            Text(title, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
            if (supportingText != null) {
                Text(supportingText, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
fun VaultActionChip(
    text: String,
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    AssistChip(
        onClick = onClick,
        label = { Text(text, maxLines = 1, overflow = TextOverflow.Ellipsis) },
        leadingIcon = { Icon(icon, null, modifier = Modifier.size(18.dp)) },
        modifier = modifier
    )
}

@Composable
fun VaultSearchPill(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "Search your offline vault"
) {
    Surface(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
        tonalElevation = 2.dp
    ) {
        Row(
            Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(Icons.Outlined.Search, null, tint = MaterialTheme.colorScheme.primary)
            Text(
                text.ifBlank { placeholder },
                style = MaterialTheme.typography.bodyLarge,
                color = if (text.isBlank()) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
fun VaultInput(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    singleLine: Boolean = false,
    minLines: Int = 1,
    leadingIcon: ImageVector? = null
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        modifier = modifier.fillMaxWidth(),
        singleLine = singleLine,
        minLines = minLines,
        leadingIcon = leadingIcon?.let { icon -> { Icon(icon, null) } },
        shape = RoundedCornerShape(20.dp)
    )
}

@Composable
fun TagChip(tag: Tag, selected: Boolean = false, onClick: (() -> Unit)? = null) {
    FilterChip(
        selected = selected,
        onClick = onClick ?: {},
        label = { Text(tag.name) },
        leadingIcon = if (selected) ({ Icon(Icons.Outlined.BookmarkBorder, null, modifier = Modifier.size(16.dp)) }) else null
    )
}

@Composable
fun KnowledgeCardRow(
    card: KnowledgeCard,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    compact: Boolean = false
) {
    ElevatedCard(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .animateContentSize(),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 1.dp, pressedElevation = 4.dp)
    ) {
        Column(Modifier.padding(if (compact) 14.dp else 18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(verticalAlignment = Alignment.Top, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                CardTypeIcon(card.type)
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            card.title.ifBlank { "Untitled card" },
                            style = if (compact) MaterialTheme.typography.titleSmall else MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.ExtraBold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f)
                        )
                        if (card.isPinned) Icon(Icons.Outlined.PushPin, null, modifier = Modifier.size(18.dp), tint = MaterialTheme.colorScheme.primary)
                        if (card.isFavorite) Icon(Icons.Outlined.Favorite, null, modifier = Modifier.size(18.dp), tint = MaterialTheme.colorScheme.tertiary)
                    }
                    Text(
                        cardPreviewText(card),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = if (compact) 1 else 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                AssistChip(onClick = {}, label = { Text(card.type.label) })
                card.folder?.let { AssistChip(onClick = {}, label = { Text(it.name) }, leadingIcon = { Icon(Icons.Outlined.Folder, null, Modifier.size(16.dp)) }) }
                card.tags.take(if (compact) 2 else 4).forEach { TagChip(it) }
                if (card.attachments.isNotEmpty()) {
                    AssistChip(
                        onClick = {},
                        label = { Text("${card.attachments.size} files") },
                        leadingIcon = { Icon(Icons.Outlined.AttachFile, null, Modifier.size(16.dp)) }
                    )
                }
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    "Updated ${card.updatedAt.toReadableDate()}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.weight(1f)
                )
                if (card.relatedScore > 0) {
                    Text(
                        "Related ${card.relatedScore}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

@Composable
fun KnowledgeCardList(
    cards: List<KnowledgeCard>,
    onCardClick: (KnowledgeCard) -> Unit,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(16.dp),
    compact: Boolean = false
) {
    val listState = rememberLazyListState()
    LazyColumn(
        state = listState,
        modifier = modifier.fillMaxSize(),
        contentPadding = contentPadding,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(
            items = cards,
            key = { it.id },
            contentType = { "knowledge-card-${it.type.name}" }
        ) { card ->
            KnowledgeCardRow(card = card, onClick = { onCardClick(card) }, compact = compact)
        }
    }
}

@Composable
fun AttachmentPreview(attachment: Attachment) {
    ElevatedCard(
        Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh)
    ) {
        Row(
            Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            if (attachment.type == AttachmentType.IMAGE || attachment.type == AttachmentType.SCREENSHOT) {
                AsyncImage(
                    model = attachment.uri,
                    contentDescription = attachment.displayName,
                    modifier = Modifier
                        .size(64.dp)
                        .clip(RoundedCornerShape(18.dp)),
                    contentScale = ContentScale.Crop
                )
            } else {
                Surface(
                    color = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    shape = RoundedCornerShape(18.dp),
                    modifier = Modifier.size(64.dp)
                ) { Box(contentAlignment = Alignment.Center) { Icon(attachment.type.icon(), null) } }
            }
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(attachment.displayName, fontWeight = FontWeight.ExtraBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(attachment.mimeType, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(formatBytes(attachment.sizeBytes), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
fun CardTypeIcon(type: CardType, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier.size(46.dp),
        shape = RoundedCornerShape(16.dp),
        color = type.iconContainerColor(),
        contentColor = type.iconContentColor()
    ) {
        Box(contentAlignment = Alignment.Center) { Icon(type.icon(), null, modifier = Modifier.size(23.dp)) }
    }
}

fun cardPreviewText(card: KnowledgeCard): String = when {
    !card.summary.isNullOrBlank() -> card.summary.orEmpty()
    card.body.isNotBlank() -> card.body
    !card.sourceLink.isNullOrBlank() -> card.sourceLink.orEmpty()
    card.attachments.isNotEmpty() -> "${card.attachments.size} attachment${if (card.attachments.size == 1) "" else "s"} saved offline"
    else -> card.type.label
}

fun CardType.icon(): ImageVector = when (this) {
    CardType.TEXT_NOTE -> Icons.AutoMirrored.Outlined.Article
    CardType.PDF -> Icons.Outlined.PictureAsPdf
    CardType.IMAGE -> Icons.Outlined.Image
    CardType.SCREENSHOT -> Icons.Outlined.Image
    CardType.LINK -> Icons.Outlined.Link
    CardType.FILE -> Icons.Outlined.AttachFile
    CardType.IDEA -> Icons.Outlined.Lightbulb
    CardType.CODE_SNIPPET -> Icons.Outlined.Code
    CardType.RESEARCH_NOTE -> Icons.Outlined.Description
    CardType.MIXED -> Icons.Outlined.AutoAwesome
}

fun AttachmentType.icon(): ImageVector = when (this) {
    AttachmentType.IMAGE -> Icons.Outlined.Image
    AttachmentType.SCREENSHOT -> Icons.Outlined.Image
    AttachmentType.PDF -> Icons.Outlined.PictureAsPdf
    AttachmentType.FILE -> Icons.Outlined.AttachFile
}

@Composable
private fun CardType.iconContainerColor(): Color = when (this) {
    CardType.TEXT_NOTE -> MaterialTheme.colorScheme.primaryContainer
    CardType.PDF -> MaterialTheme.colorScheme.errorContainer
    CardType.IMAGE, CardType.SCREENSHOT -> MaterialTheme.colorScheme.tertiaryContainer
    CardType.LINK -> MaterialTheme.colorScheme.secondaryContainer
    CardType.FILE -> MaterialTheme.colorScheme.surfaceVariant
    CardType.IDEA -> MaterialTheme.colorScheme.tertiaryContainer
    CardType.CODE_SNIPPET -> MaterialTheme.colorScheme.inversePrimary
    CardType.RESEARCH_NOTE -> MaterialTheme.colorScheme.primaryContainer
    CardType.MIXED -> MaterialTheme.colorScheme.secondaryContainer
}

@Composable
private fun CardType.iconContentColor(): Color = when (this) {
    CardType.PDF -> MaterialTheme.colorScheme.onErrorContainer
    CardType.IMAGE, CardType.SCREENSHOT, CardType.IDEA -> MaterialTheme.colorScheme.onTertiaryContainer
    CardType.LINK, CardType.MIXED -> MaterialTheme.colorScheme.onSecondaryContainer
    CardType.FILE -> MaterialTheme.colorScheme.onSurfaceVariant
    else -> MaterialTheme.colorScheme.onPrimaryContainer
}

private fun formatBytes(bytes: Long): String {
    if (bytes <= 0L) return "0 B"
    val kb = bytes / 1024.0
    if (kb < 1024) return "%.1f KB".format(kb)
    val mb = kb / 1024.0
    if (mb < 1024) return "%.1f MB".format(mb)
    return "%.1f GB".format(mb / 1024.0)
}
