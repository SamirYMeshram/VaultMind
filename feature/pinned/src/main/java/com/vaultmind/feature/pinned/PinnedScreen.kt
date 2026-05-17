@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.vaultmind.feature.pinned

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.PushPin
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.vaultmind.core.designsystem.KnowledgeCardList
import com.vaultmind.core.designsystem.VaultEmpty
import com.vaultmind.core.designsystem.VaultError
import com.vaultmind.core.designsystem.VaultHeroCard
import com.vaultmind.core.designsystem.VaultLoading
import com.vaultmind.core.designsystem.VaultScreen
import com.vaultmind.core.designsystem.VaultSectionHeader
import com.vaultmind.core.model.KnowledgeCard

@Composable
fun PinnedScreen(
    onOpenCard: (KnowledgeCard) -> Unit,
    viewModel: PinnedViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    Scaffold(topBar = { TopAppBar(title = { Text("Pinned cards") }) }) { padding ->
        when {
            state.loading -> VaultLoading("Loading pinned cards…")
            state.error != null -> VaultError(state.error!!)
            state.cards.isEmpty() -> VaultEmpty("Nothing pinned", "Pin important cards from the detail screen to keep them one tap away.")
            else -> VaultScreen(modifier = Modifier.padding(padding)) {
                Column(Modifier.fillMaxSize()) {
                    Column(Modifier.padding(20.dp)) {
                        VaultHeroCard(
                            title = "Pinned knowledge",
                            subtitle = "Your most important cards are separated from the full vault for quicker access.",
                            icon = Icons.Outlined.PushPin
                        )
                        VaultSectionHeader(
                            title = "Priority cards",
                            subtitle = "Pinned cards are also boosted in offline search ranking.",
                            modifier = Modifier.padding(top = 18.dp)
                        )
                    }
                    KnowledgeCardList(
                        cards = state.cards,
                        onCardClick = {
                            viewModel.cardOpened()
                            onOpenCard(it)
                        },
                        contentPadding = PaddingValues(start = 20.dp, top = 4.dp, end = 20.dp, bottom = 28.dp)
                    )
                }
            }
        }
    }
}
