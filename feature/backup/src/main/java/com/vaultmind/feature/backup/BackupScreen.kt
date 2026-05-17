@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.vaultmind.feature.backup

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.FileDownload
import androidx.compose.material.icons.outlined.FileUpload
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material3.Button
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
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
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.vaultmind.core.common.toReadableDate
import com.vaultmind.core.designsystem.VaultHeroCard
import com.vaultmind.core.designsystem.VaultLoading
import com.vaultmind.core.designsystem.VaultMetricCard
import com.vaultmind.core.designsystem.VaultScreen
import com.vaultmind.core.designsystem.VaultSectionHeader

@Composable
fun BackupScreen(
    onBack: () -> Unit,
    viewModel: BackupViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Backup & restore") },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Outlined.ArrowBack, null) } }
            )
        }
    ) { padding ->
        if (state.loading) {
            VaultLoading("Checking backup status…")
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
                        title = "Protect your vault",
                        subtitle = "The current implementation contains the clean backup architecture, WorkManager reminder and file-picker integration points.",
                        icon = Icons.Outlined.FileUpload
                    )

                    VaultSectionHeader("Backup status")
                    ElevatedCard(
                        modifier = Modifier.fillMaxWidth(),
                        shape = androidx.compose.foundation.shape.RoundedCornerShape(28.dp),
                        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh)
                    ) {
                        Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            Text(state.metadata?.statusMessage ?: "No backup yet", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.ExtraBold)
                            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                VaultMetricCard("Last export", state.metadata?.lastExportedAt?.toReadableDate() ?: "Never", Icons.Outlined.FileUpload, Modifier.weight(1f))
                                VaultMetricCard("Last import", state.metadata?.lastImportedAt?.toReadableDate() ?: "Never", Icons.Outlined.FileDownload, Modifier.weight(1f))
                            }
                        }
                    }

                    Button(onClick = viewModel::export, enabled = !state.busy, modifier = Modifier.fillMaxWidth()) {
                        Icon(Icons.Outlined.FileUpload, null)
                        Spacer(Modifier.width(8.dp))
                        Text("Export vault backup")
                    }
                    OutlinedButton(onClick = viewModel::importSample, enabled = !state.busy, modifier = Modifier.fillMaxWidth()) {
                        Icon(Icons.Outlined.FileDownload, null)
                        Spacer(Modifier.width(8.dp))
                        Text("Import placeholder backup")
                    }

                    ElevatedCard(colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh)) {
                        ListItem(
                            headlineContent = { Text("Production file integration", fontWeight = FontWeight.Bold) },
                            supportingContent = { Text("Connect CreateDocument/OpenDocument ActivityResult APIs here, then serialize Room tables to JSON and restore inside one Room transaction.") },
                            leadingContent = { Icon(Icons.Outlined.Info, null) }
                        )
                    }
                    ElevatedCard(colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh)) {
                        ListItem(
                            headlineContent = { Text("Backup reminders", fontWeight = FontWeight.Bold) },
                            supportingContent = { Text("WorkManager can remind the user without blocking app startup or scrolling performance.") },
                            leadingContent = { Icon(Icons.Outlined.Schedule, null) }
                        )
                    }

                    if (state.busy) LinearProgressIndicator(Modifier.fillMaxWidth())
                    state.message?.let { Text(it, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.SemiBold) }
                    state.error?.let { Text(it, color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.SemiBold) }
                }
            }
        }
    }
}
