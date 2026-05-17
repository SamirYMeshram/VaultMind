@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.vaultmind.feature.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vaultmind.core.domain.ActivityRepository
import com.vaultmind.core.model.RecentActivity
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.History
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
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
import com.vaultmind.core.designsystem.VaultEmpty
import com.vaultmind.core.designsystem.VaultHeroCard
import com.vaultmind.core.designsystem.VaultScreen
import com.vaultmind.core.designsystem.VaultSectionHeader

@HiltViewModel
class RecentActivityViewModel @Inject constructor(repository: ActivityRepository) : ViewModel() {
    val activities: StateFlow<List<RecentActivity>> = repository.observeRecentActivity(100)
        .map { it }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
}

@Composable
fun RecentActivityScreen(
    onBack: () -> Unit,
    viewModel: RecentActivityViewModel = hiltViewModel()
) {
    val activities by viewModel.activities.collectAsStateWithLifecycle()
    val listState = rememberLazyListState()
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Recent activity") },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Outlined.ArrowBack, null) } }
            )
        }
    ) { padding ->
        if (activities.isEmpty()) {
            VaultEmpty("No activity", "Create, edit, view, search, import or export to build a local activity timeline.")
        } else {
            VaultScreen(modifier = Modifier.padding(padding)) {
                LazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(20.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    item(contentType = "hero") {
                        VaultHeroCard(
                            title = "Vault timeline",
                            subtitle = "A local privacy-friendly history of important actions inside VaultMind.",
                            icon = Icons.Outlined.History
                        )
                    }
                    item(contentType = "header") { VaultSectionHeader("Latest activity") }
                    items(activities, key = { it.id }, contentType = { "activity" }) { activity ->
                        ElevatedCard(colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh)) {
                            ListItem(
                                headlineContent = { Text(activity.title, fontWeight = FontWeight.ExtraBold) },
                                supportingContent = { Text("${activity.description} • ${activity.createdAt.toReadableDate()}") },
                                leadingContent = { Icon(Icons.Outlined.History, null) }
                            )
                        }
                    }
                }
            }
        }
    }
}
