package dev.r4g309.kokoromed.ui.screens.deck

import androidx.lifecycle.SavedStateHandle
import androidx.navigation.toRoute
import dev.r4g309.kokoromed.ui.navigation.DeckDetail
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.r4g309.kokoromed.data.repository.DeckRepository
import dev.r4g309.kokoromed.data.repository.DeckWithProgress
import dev.r4g309.kokoromed.domain.model.CardState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

enum class CardFilter { all, new, learning, mastered }

@HiltViewModel
class DeckDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val repo: DeckRepository,
) : ViewModel() {

    val deckId: String = savedStateHandle.toRoute<DeckDetail>().deckId

    private val _deck = repo.observeDeck(deckId)

    val filter = MutableStateFlow(CardFilter.all)

    val uiState = combine(_deck, filter) { deck, f ->
        if (deck == null) return@combine null
        val filtered = when (f) {
            CardFilter.all      -> deck.cards
            CardFilter.new      -> deck.cards.filter { dev.r4g309.kokoromed.domain.srs.cardState(it.toSrsData()) == CardState.new }
            CardFilter.learning -> deck.cards.filter { dev.r4g309.kokoromed.domain.srs.cardState(it.toSrsData()) == CardState.learning }
            CardFilter.mastered -> deck.cards.filter { dev.r4g309.kokoromed.domain.srs.cardState(it.toSrsData()) == CardState.mastered }
        }
        deck.copy(cards = filtered)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    suspend fun deleteDeck() = repo.deleteDeck(deckId)
    suspend fun deleteCard(id: String) = repo.deleteCard(id)
}

private fun dev.r4g309.kokoromed.data.db.CardEntity.toSrsData() =
    dev.r4g309.kokoromed.domain.model.SrsData(ease, interval, reps, lapses, due)
