package dev.r4g309.kokoromed.ui.screens.editors

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.r4g309.kokoromed.data.db.CardEntity
import dev.r4g309.kokoromed.data.db.DeckEntity
import dev.r4g309.kokoromed.data.repository.DeckRepository
import dev.r4g309.kokoromed.data.repository.importCardsJson
import dev.r4g309.kokoromed.data.repository.importDeckJson
import dev.r4g309.kokoromed.domain.model.todayKey
import kotlinx.coroutines.flow.first
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class EditorsViewModel @Inject constructor(
    private val repo: DeckRepository,
) : ViewModel() {

    // ── Mazo ──────────────────────────────────────────────────────────────────

    suspend fun saveDeck(
        existing: DeckEntity?,
        name: String, description: String, color: String, icon: Int,
    ): String {
        val id = existing?.id ?: uid()
        repo.upsertDeck(
            DeckEntity(id = id, name = name, description = description,
                color = color, icon = icon,
                createdAt = existing?.createdAt ?: System.currentTimeMillis())
        )
        return id
    }

    suspend fun deleteDeck(id: String) = repo.deleteDeck(id)

    // ── Tarjeta ───────────────────────────────────────────────────────────────

    suspend fun saveCard(
        deckId: String,
        existing: CardEntity?,
        question: String, options: List<String>, correct: Int,
        explanation: String, tags: List<String>, difficulty: String,
    ) {
        val today = todayKey()
        repo.upsertCard(
            CardEntity(
                id          = existing?.id ?: uid(),
                deckId      = deckId,
                question    = question,
                optionsJson = Json.encodeToString(options),
                correct     = correct,
                explanation = explanation,
                tagsJson    = Json.encodeToString(tags),
                difficulty  = difficulty,
                ease        = existing?.ease ?: 2.5f,
                interval    = existing?.interval ?: 0,
                reps        = existing?.reps ?: 0,
                lapses      = existing?.lapses ?: 0,
                due         = existing?.due ?: today,
                seen        = existing?.seen ?: 0,
                correct2    = existing?.correct2 ?: 0,
            )
        )
    }

    suspend fun deleteCard(id: String) = repo.deleteCard(id)

    // ── Import / Export ───────────────────────────────────────────────────────

    suspend fun importDeck(raw: String, fallbackName: String): Result<String> = runCatching {
        val deck  = importDeckJson(raw, fallbackName)
        val cards = importCardsJson(raw, deck.id)
        repo.upsertDeck(deck)
        cards.forEach { repo.upsertCard(it) }
        "${deck.name} (${cards.size} tarjetas)"
    }

    private fun uid() = UUID.randomUUID().toString().replace("-", "").take(8)
}
