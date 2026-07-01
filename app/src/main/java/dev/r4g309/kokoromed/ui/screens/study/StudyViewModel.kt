package dev.r4g309.kokoromed.ui.screens.study

import androidx.lifecycle.SavedStateHandle
import androidx.navigation.toRoute
import dev.r4g309.kokoromed.ui.navigation.Study
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.r4g309.kokoromed.data.db.CardDao
import dev.r4g309.kokoromed.data.db.CardEntity
import dev.r4g309.kokoromed.data.repository.DeckRepository
import dev.r4g309.kokoromed.domain.model.Rating
import dev.r4g309.kokoromed.domain.model.SrsData
import dev.r4g309.kokoromed.domain.model.todayKey
import dev.r4g309.kokoromed.domain.srs.StudyMode
import dev.r4g309.kokoromed.domain.srs.applySrs
import dev.r4g309.kokoromed.domain.srs.buildQueue
import dev.r4g309.kokoromed.domain.srs.scheduleSM2
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class StudyUiState(
    val loading: Boolean = true,
    val cards: List<CardEntity> = emptyList(),
    val queue: List<String> = emptyList(),   // card IDs in order
    val baseLen: Int = 0,
    val pos: Int = 0,
    val selected: Int? = null,
    val answered: Boolean = false,
    val showExplanation: Boolean = true,
    val isExam: Boolean = false,
    val examAnswers: Map<String, Int> = emptyMap(),
    val correct: Int = 0,
    val total: Int = 0,
    val done: Boolean = false,
) {
    val currentCard: CardEntity? get() = cards.firstOrNull { it.id == queue.getOrNull(pos) }
    val progress: Float get() = if (queue.isEmpty()) 0f else pos.toFloat() / queue.size
    val requeueCount: Int get() = queue.size - baseLen
}

@HiltViewModel
class StudyViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val repo: DeckRepository,
    private val cardDao: CardDao,
) : ViewModel() {

    private val route = savedStateHandle.toRoute<Study>()
    val deckId: String = route.deckId
    val mode: StudyMode = StudyMode.valueOf(route.mode)
    private val maxQuestions: Int? = route.max.takeIf { it > 0 }

    private val _state = MutableStateFlow(StudyUiState())
    val state: StateFlow<StudyUiState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            val dwp = repo.observeDeck(deckId).first() ?: return@launch
            val cards = dwp.cards
            // buildQueue ya devuelve la cola barajada en todos los modos.
            var queue = buildQueue(cards, mode).map { it.id }
            // Aplica el límite si se especificó
            if (maxQuestions != null && queue.size > maxQuestions) {
                queue = queue.take(maxQuestions)
            }
            _state.update {
                it.copy(
                    loading  = false,
                    cards    = cards,
                    queue    = queue,
                    baseLen  = queue.size,
                    isExam   = mode == StudyMode.exam,
                )
            }
        }
    }

    fun selectOption(index: Int) {
        if (_state.value.answered) return
        _state.update { it.copy(selected = index) }
    }

    fun confirm() {
        val s = _state.value
        val card = s.currentCard
        if (s.selected == null || s.answered || card == null) return
        val wasCorrect = s.selected == card.correct
        val selectedIdx = s.selected

        if (s.isExam) {
            // Persistimos las estadísticas de esta respuesta de inmediato, para que
            // un cierre forzado del proceso no borre el progreso del examen.
            viewModelScope.launch {
                val updated = card.copy(
                    seen     = card.seen + 1,
                    correct2 = card.correct2 + if (wasCorrect) 1 else 0,
                )
                cardDao.upsert(updated)
                repo.incrementActivity()
            }
            _state.update {
                it.copy(
                    answered    = true,
                    examAnswers = it.examAnswers + (card.id to selectedIdx),
                )
            }
            advanceExam()
            return
        }

        _state.update {
            it.copy(
                answered = true,
                total    = it.total + 1,
                correct  = it.correct + if (wasCorrect) 1 else 0,
            )
        }
    }

    fun rate(rating: Rating) {
        val s = _state.value
        val card = s.currentCard ?: return
        val wasCorrect = s.selected == card.correct

        viewModelScope.launch {
            val fresh = cardDao.getById(card.id) ?: card
            val srs = SrsData(fresh.ease, fresh.interval, fresh.reps, fresh.lapses, fresh.due)
            val newSrs = scheduleSM2(srs, rating)
            cardDao.upsert(fresh.applySrs(newSrs, wasCorrect))
            repo.incrementActivity()
        }

        val requeue = rating == Rating.again
        _state.update { st ->
            val newQueue = if (requeue) st.queue + card.id else st.queue
            val nextPos = st.pos + 1
            if (nextPos >= newQueue.size) {
                st.copy(queue = newQueue, done = true)
            } else {
                st.copy(
                    queue    = newQueue,
                    pos      = nextPos,
                    selected = null,
                    answered = false,
                    showExplanation = true,
                )
            }
        }
    }

    private fun advanceExam() {
        val s = _state.value
        val nextPos = s.pos + 1
        if (nextPos >= s.queue.size) {
            finishExam()
        } else {
            _state.update {
                it.copy(pos = nextPos, selected = null, answered = false)
            }
        }
    }

    private fun finishExam() {
        val s = _state.value
        val today = todayKey()
        val total = s.queue.size
        val corr = s.queue.count { id ->
            val card = s.cards.firstOrNull { it.id == id } ?: return@count false
            s.examAnswers[id] == card.correct
        }

        // Aplica SM-2 a cada respuesta del examen:
        // incorrecta → Rating.again (lapses++, aparece en Falladas)
        // correcta   → Rating.good  (intervalo avanza)
        // Leemos el card fresco de la BD para no pisar seen/correct2 guardados en confirm().
        viewModelScope.launch {
            s.queue.forEach { id ->
                val fresh = cardDao.getById(id) ?: return@forEach
                val wasCorrect = s.examAnswers[id] == fresh.correct
                val rating = if (wasCorrect) Rating.good else Rating.again
                val srs = SrsData(fresh.ease, fresh.interval, fresh.reps, fresh.lapses, fresh.due)
                val newSrs = scheduleSM2(srs, rating, today)
                cardDao.upsert(fresh.copy(
                    ease     = newSrs.ease,
                    interval = newSrs.interval,
                    reps     = newSrs.reps,
                    lapses   = newSrs.lapses,
                    due      = newSrs.due,
                ))
            }
        }

        _state.update { it.copy(correct = corr, total = total, done = true) }
    }

    fun toggleExplanation() {
        _state.update { it.copy(showExplanation = !it.showExplanation) }
    }
}
