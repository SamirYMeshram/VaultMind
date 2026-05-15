@file:OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class, androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.vaultmind.feature.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.vaultmind.core.model.*

@Composable
fun SettingsScreen(onOpenBackup: () -> Unit, viewModel: SettingsViewModel = hiltViewModel()) {
    val settings by viewModel.settings.collectAsStateWithLifecycle()
    Scaffold(topBar = { TopAppBar(title = { Text("Settings") }) }) { padding ->
        Column(Modifier.padding(padding).fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            ElevatedCard { Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("Appearance", style = MaterialTheme.typography.titleLarge)
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) { ThemeMode.entries.forEach { FilterChip(settings.themeMode == it, { viewModel.theme(it) }, label = { Text(it.name.lowercase().replaceFirstChar(Char::uppercase)) }) } }
                SettingSwitch("Dynamic color", settings.dynamicColorEnabled, viewModel::dynamic)
            } }
            ElevatedCard { Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("Privacy & behavior", style = MaterialTheme.typography.titleLarge)
                SettingSwitch("Backup reminders", settings.backupReminderEnabled, viewModel::backup)
                SettingSwitch("Search history", settings.searchHistoryEnabled, viewModel::history)
                SettingSwitch("Private previews", settings.privatePreviewsEnabled, viewModel::previews)
            } }
            ElevatedCard { Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("Defaults", style = MaterialTheme.typography.titleLarge)
                Text("Card view mode")
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) { CardViewMode.entries.forEach { FilterChip(settings.defaultCardViewMode == it, { viewModel.viewMode(it) }, label = { Text(it.name) }) } }
                Text("Sort mode")
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) { SortMode.entries.forEach { FilterChip(settings.defaultSortMode == it, { viewModel.sortMode(it) }, label = { Text(it.name) }) } }
            } }
            OutlinedButton(onOpenBackup, Modifier.fillMaxWidth()) { Icon(Icons.Outlined.Backup, null); Spacer(Modifier.width(8.dp)); Text("Backup & restore") }
            ListItem(headlineContent = { Text("Ready for future upgrades") }, supportingContent = { Text("Architecture leaves clean seams for AI embeddings, OCR, encryption, cloud sync, and collaboration later.") }, leadingContent = { Icon(Icons.Outlined.Security, null) })
        }
    }
}
@Composable private fun SettingSwitch(title: String, checked: Boolean, onChange: (Boolean) -> Unit) { Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) { Text(title); Switch(checked, onChange) } }
