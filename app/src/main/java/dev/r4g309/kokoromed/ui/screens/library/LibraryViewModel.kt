package dev.r4g309.kokoromed.ui.screens.library

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.r4g309.kokoromed.data.repository.DeckRepository
import dev.r4g309.kokoromed.data.repository.DeckWithProgress
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class LibraryViewModel @Inject constructor(
    private val repo: DeckRepository,
) : ViewModel() {

    val query = MutableStateFlow("")

    val decks = combine(repo.observeDecks(), query) { decks, q ->
        if (q.isBlank()) decks
        else decks.filter {
            it.deck.name.contains(q, ignoreCase = true) ||
            it.deck.description.contains(q, ignoreCase = true)
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val allDecks = repo.observeDecks()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
}
