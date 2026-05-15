@file:OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class, androidx.compose.material3.ExperimentalMaterial3Api::class)

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

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.History
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.vaultmind.core.common.toReadableDate
import com.vaultmind.core.designsystem.VaultEmpty

@HiltViewModel
class RecentActivityViewModel @Inject constructor(repository: ActivityRepository) : ViewModel() {
    val activities: StateFlow<List<RecentActivity>> = repository.observeRecentActivity(100)
        .map { it }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
}

@Composable
fun RecentActivityScreen(onBack: () -> Unit, viewModel: RecentActivityViewModel = hiltViewModel()) {
    val activities by viewModel.activities.collectAsStateWithLifecycle()
    Scaffold(topBar = { TopAppBar(title = { Text("Recent activity") }, navigationIcon = { IconButton(onBack) { Icon(Icons.Outlined.ArrowBack, null) } }) }) { padding ->
        Box(Modifier.padding(padding)) {
            if (activities.isEmpty()) VaultEmpty("No activity", "Create, edit, view, search, import, or export to build a local activity timeline.")
            else LazyColumn { items(activities, key = { it.id }) { activity -> ListItem(headlineContent = { Text(activity.title) }, supportingContent = { Text("${activity.description} • ${activity.createdAt.toReadableDate()}") }, leadingContent = { Icon(Icons.Outlined.History, null) }) } }
        }
    }
}
