package com.vaultmind.feature.knowledge

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vaultmind.core.analytics.AnalyticsEvent
import com.vaultmind.core.analytics.AnalyticsTracker
import com.vaultmind.core.domain.*
import com.vaultmind.core.model.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class KnowledgeListUiState(val loading: Boolean = true, val cards: List<KnowledgeCard> = emptyList(), val error: String? = null)
@HiltViewModel
class KnowledgeListViewModel @Inject constructor(repository: KnowledgeRepository) : ViewModel() {
    val uiState = repository.observeAllCards()
        .map { KnowledgeListUiState(loading = false, cards = it) }
        .catch { emit(KnowledgeListUiState(false, error = it.message)) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), KnowledgeListUiState())
}

data class CardDetailUiState(val loading: Boolean = true, val card: KnowledgeCard? = null, val related: List<KnowledgeCard> = emptyList(), val error: String? = null)
@HiltViewModel
class CardDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val repository: KnowledgeRepository,
    private val relatedUseCase: GetRelatedKnowledgeCardsUseCase,
    private val deleteUseCase: DeleteKnowledgeCardUseCase,
    private val togglePinned: TogglePinnedUseCase,
    private val toggleFavorite: ToggleFavoriteUseCase,
    private val analytics: AnalyticsTracker
) : ViewModel() {
    private val cardId: String = checkNotNull(savedStateHandle["cardId"])
    private val related = MutableStateFlow<List<KnowledgeCard>>(emptyList())
    val uiState = combine(repository.observeCard(cardId), related) { card, suggestions -> CardDetailUiState(false, card, suggestions, if (card == null) "Card not found" else null) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), CardDetailUiState())
    init { viewModelScope.launch { repository.markViewed(cardId); related.value = relatedUseCase(cardId) } }
    fun togglePin() = viewModelScope.launch { uiState.value.card?.let { togglePinned(it) } }
    fun toggleFavorite() = viewModelScope.launch { uiState.value.card?.let { toggleFavorite(it) } }
    fun delete(onDone: () -> Unit) = viewModelScope.launch { uiState.value.card?.let { deleteUseCase(it); analytics.track(AnalyticsEvent.cardDeleted()); onDone() } }
}

data class CardEditUiState(
    val loading: Boolean = true,
    val id: String? = null,
    val title: String = "",
    val body: String = "",
    val type: CardType = CardType.TEXT_NOTE,
    val tags: List<Tag> = emptyList(),
    val selectedTagIds: Set<String> = emptySet(),
    val folders: List<Folder> = emptyList(),
    val selectedFolderId: String? = null,
    val collections: List<com.vaultmind.core.model.Collection> = emptyList(),
    val selectedCollectionIds: Set<String> = emptySet(),
    val isPinned: Boolean = false,
    val isFavorite: Boolean = false,
    val sourceLink: String = "",
    val summary: String = "",
    val saving: Boolean = false,
    val error: String? = null
)
sealed interface CardEditEvent { data class Saved(val cardId: String) : CardEditEvent }

@HiltViewModel
class CardEditViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    knowledgeRepository: KnowledgeRepository,
    organizationRepository: OrganizationRepository,
    private val saveUseCase: SaveKnowledgeCardUseCase,
    private val analytics: AnalyticsTracker
) : ViewModel() {
    private val cardId: String? = savedStateHandle["cardId"]
    private val mutable = MutableStateFlow(CardEditUiState(loading = true, id = cardId))
    val uiState: StateFlow<CardEditUiState> = mutable.asStateFlow()
    private val eventsChannel = Channel<CardEditEvent>(Channel.BUFFERED)
    val events = eventsChannel.receiveAsFlow()
    init {
        viewModelScope.launch {
            combine(knowledgeRepository.observeCard(cardId ?: ""), organizationRepository.observeTags(), organizationRepository.observeFolders(), organizationRepository.observeCollections()) { card, tags, folders, collections ->
                val base = mutable.value
                if (card != null && base.title.isBlank() && base.body.isBlank()) {
                    base.copy(
                        loading = false, id = card.id, title = card.title, body = card.body, type = card.type,
                        selectedTagIds = card.tags.map { it.id }.toSet(), selectedFolderId = card.folder?.id,
                        selectedCollectionIds = card.collections.map { it.id }.toSet(), isPinned = card.isPinned,
                        isFavorite = card.isFavorite, sourceLink = card.sourceLink.orEmpty(), summary = card.summary.orEmpty(),
                        tags = tags, folders = folders, collections = collections
                    )
                } else base.copy(loading = false, tags = tags, folders = folders, collections = collections)
            }.collect { mutable.value = it }
        }
    }
    fun title(v: String) { mutable.update { it.copy(title = v, error = null) } }
    fun body(v: String) { mutable.update { it.copy(body = v, error = null) } }
    fun type(v: CardType) { mutable.update { it.copy(type = v) } }
    fun source(v: String) { mutable.update { it.copy(sourceLink = v) } }
    fun summary(v: String) { mutable.update { it.copy(summary = v) } }
    fun pin(v: Boolean) { mutable.update { it.copy(isPinned = v) } }
    fun favorite(v: Boolean) { mutable.update { it.copy(isFavorite = v) } }
    fun toggleTag(id: String) { mutable.update { it.copy(selectedTagIds = if (id in it.selectedTagIds) it.selectedTagIds - id else it.selectedTagIds + id) } }
    fun folder(id: String?) { mutable.update { it.copy(selectedFolderId = id) } }
    fun toggleCollection(id: String) { mutable.update { it.copy(selectedCollectionIds = if (id in it.selectedCollectionIds) it.selectedCollectionIds - id else it.selectedCollectionIds + id) } }
    fun save() = viewModelScope.launch {
        val s = mutable.value
        mutable.update { it.copy(saving = true, error = null) }
        val result = saveUseCase(CardDraft(s.id, s.title, s.body, s.type, s.selectedTagIds.toList(), s.selectedFolderId, s.selectedCollectionIds.toList(), s.isPinned, s.isFavorite, s.sourceLink.ifBlank { null }, s.summary.ifBlank { null }))
        result.onSuccess { id ->
            analytics.track(if (s.id == null) AnalyticsEvent.cardCreated(s.type.name) else AnalyticsEvent.cardUpdated(s.type.name))
            eventsChannel.send(CardEditEvent.Saved(id))
        }.onFailure { error -> mutable.update { it.copy(saving = false, error = error.message) } }
    }
}
