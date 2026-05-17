@file:OptIn(
    androidx.compose.foundation.layout.ExperimentalLayoutApi::class,
    androidx.compose.material3.ExperimentalMaterial3Api::class
)

package com.vaultmind.feature.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Backup
import androidx.compose.material.icons.outlined.Palette
import androidx.compose.material.icons.outlined.Security
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.vaultmind.core.designsystem.VaultHeroCard
import com.vaultmind.core.designsystem.VaultScreen
import com.vaultmind.core.model.CardViewMode
import com.vaultmind.core.model.SortMode
import com.vaultmind.core.model.ThemeMode

@Composable
fun SettingsScreen(
    onOpenBackup: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val settings by viewModel.settings.collectAsStateWithLifecycle()
    Scaffold(topBar = { TopAppBar(title = { Text("Settings") }) }) { padding ->
        VaultScreen(modifier = Modifier.padding(padding)) {
            Column(
                Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                VaultHeroCard(
                    title = "Make VaultMind yours",
                    subtitle = "Theme, privacy, backup reminders and defaults are saved locally with DataStore.",
                    icon = Icons.Outlined.Tune
                )

                SettingsCard(title = "Appearance", icon = Icons.Outlined.Palette) {
                    Text("Theme mode", style = MaterialTheme.typography.labelLarge)
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        ThemeMode.entries.forEach { mode ->
                            FilterChip(
                                selected = settings.themeMode == mode,
                                onClick = { viewModel.theme(mode) },
                                label = { Text(mode.name.lowercase().replaceFirstChar(Char::uppercase)) }
                            )
                        }
                    }
                    SettingSwitch("Dynamic color", "Use Android wallpaper colors on supported devices.", settings.dynamicColorEnabled, viewModel::dynamic)
                }

                SettingsCard(title = "Privacy & behavior", icon = Icons.Outlined.Security) {
                    SettingSwitch("Backup reminders", "Schedule WorkManager reminders for safer local data habits.", settings.backupReminderEnabled, viewModel::backup)
                    SettingSwitch("Search history", "Keep recent searches locally for faster repeated research.", settings.searchHistoryEnabled, viewModel::history)
                    SettingSwitch("Private previews", "Reduce sensitive preview exposure in future lock-screen surfaces.", settings.privatePreviewsEnabled, viewModel::previews)
                }

                SettingsCard(title = "Defaults", icon = Icons.Outlined.Tune) {
                    Text("Card view mode", style = MaterialTheme.typography.labelLarge)
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        CardViewMode.entries.forEach { mode ->
                            FilterChip(settings.defaultCardViewMode == mode, { viewModel.viewMode(mode) }, label = { Text(mode.name.lowercase().replaceFirstChar(Char::uppercase)) })
                        }
                    }
                    Text("Sort mode", style = MaterialTheme.typography.labelLarge)
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        SortMode.entries.forEach { mode ->
                            FilterChip(settings.defaultSortMode == mode, { viewModel.sortMode(mode) }, label = { Text(mode.name.lowercase().replace('_', ' ').replaceFirstChar(Char::uppercase)) })
                        }
                    }
                }

                OutlinedButton(onClick = onOpenBackup, modifier = Modifier.fillMaxWidth()) {
                    Icon(Icons.Outlined.Backup, null)
                    Spacer(Modifier.width(8.dp))
                    Text("Backup & restore")
                }

                ElevatedCard(colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh)) {
                    ListItem(
                        headlineContent = { Text("Ready for future upgrades", fontWeight = FontWeight.Bold) },
                        supportingContent = { Text("The architecture leaves clean seams for AI embeddings, OCR, encryption, cloud sync and collaboration later.") },
                        leadingContent = { Icon(Icons.Outlined.Security, null) }
                    )
                }
            }
        }
    }
}

@Composable
private fun SettingsCard(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    content: @Composable ColumnScope.() -> Unit
) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = androidx.compose.foundation.shape.RoundedCornerShape(28.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh)
    ) {
        Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Icon(icon, null, tint = MaterialTheme.colorScheme.primary)
                Text(title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.ExtraBold)
            }
            content()
        }
    }
}

@Composable
private fun SettingSwitch(
    title: String,
    subtitle: String,
    checked: Boolean,
    onChange: (Boolean) -> Unit
) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Column(Modifier.weight(1f)) {
            Text(title, fontWeight = FontWeight.SemiBold)
            Text(subtitle, color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
        }
        Switch(checked = checked, onCheckedChange = onChange)
    }
}
