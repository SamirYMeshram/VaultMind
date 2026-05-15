@file:OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class, androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.vaultmind.feature.pinned

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.vaultmind.core.designsystem.*
import com.vaultmind.core.model.KnowledgeCard

@Composable
fun PinnedScreen(onOpenCard: (KnowledgeCard) -> Unit, viewModel: PinnedViewModel = hiltViewModel()) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    Scaffold(topBar = { TopAppBar(title = { Text("Pinned cards") }) }) { padding ->
        Box(Modifier.padding(padding)) {
            when {
                state.loading -> VaultLoading()
                state.error != null -> VaultError(state.error!!)
                state.cards.isEmpty() -> VaultEmpty("Nothing pinned", "Pin important cards from the detail screen to keep them one tap away.")
                else -> KnowledgeCardList(state.cards) { viewModel.cardOpened(); onOpenCard(it) }
            }
        }
    }
}
