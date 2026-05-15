@file:OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class, androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.vaultmind.feature.backup

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.vaultmind.core.common.toReadableDate
import com.vaultmind.core.designsystem.VaultLoading

@Composable
fun BackupScreen(onBack: () -> Unit, viewModel: BackupViewModel = hiltViewModel()) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    Scaffold(topBar = { TopAppBar(title = { Text("Backup & restore") }, navigationIcon = { IconButton(onBack) { Icon(Icons.Outlined.ArrowBack, null) } }) }) { padding ->
        if (state.loading) VaultLoading() else Column(Modifier.padding(padding).fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            ElevatedCard(Modifier.fillMaxWidth()) { Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Backup status", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Text(state.metadata?.statusMessage ?: "No backup yet")
                Text("Last export: ${state.metadata?.lastExportedAt?.toReadableDate() ?: "Never"}")
                Text("Last import: ${state.metadata?.lastImportedAt?.toReadableDate() ?: "Never"}")
            } }
            Button(viewModel::export, enabled = !state.busy, modifier = Modifier.fillMaxWidth()) { Icon(Icons.Outlined.FileUpload, null); Spacer(Modifier.width(8.dp)); Text("Export vault backup") }
            OutlinedButton(viewModel::importSample, enabled = !state.busy, modifier = Modifier.fillMaxWidth()) { Icon(Icons.Outlined.FileDownload, null); Spacer(Modifier.width(8.dp)); Text("Import placeholder backup") }
            ElevatedCard { ListItem(
                headlineContent = { Text("Production file integration") },
                supportingContent = { Text("Connect CreateDocument/OpenDocument ActivityResult APIs here, then serialize Room tables to JSON and restore inside a Room transaction.") },
                leadingContent = { Icon(Icons.Outlined.Info, null) }
            ) }
            if (state.busy) LinearProgressIndicator(Modifier.fillMaxWidth())
            state.message?.let { Text(it, color = MaterialTheme.colorScheme.primary) }
            state.error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
        }
    }
}
