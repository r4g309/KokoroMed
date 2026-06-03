package dev.r4g309.kokoromed.data.repository

import dev.r4g309.kokoromed.data.db.ActivityDao
import dev.r4g309.kokoromed.data.db.ActivityEntity
import dev.r4g309.kokoromed.data.db.CardDao
import dev.r4g309.kokoromed.data.db.CardEntity
import dev.r4g309.kokoromed.data.db.DeckDao
import dev.r4g309.kokoromed.data.db.DeckEntity
import dev.r4g309.kokoromed.domain.model.todayKey
import dev.r4g309.kokoromed.domain.srs.DeckProgress
import dev.r4g309.kokoromed.domain.srs.deckProgress
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

data class DeckWithProgress(
    val deck: DeckEntity,
    val cards: List<CardEntity>,
    val progress: DeckProgress,
)

@Singleton
class DeckRepository @Inject constructor(
    private val deckDao: DeckDao,
    private val cardDao: CardDao,
    private val activityDao: ActivityDao,
) {
    // Combina todos los mazos con todas las tarjetas en un solo combine.
    fun observeDecks(): Flow<List<DeckWithProgress>> =
        combine(deckDao.observeAll(), cardDao.observeAll()) { decks, allCards ->
            val byDeck = allCards.groupBy { it.deckId }
            decks.map { deck ->
                val cards = byDeck[deck.id] ?: emptyList()
                DeckWithProgress(deck, cards, deckProgress(cards))
            }
        }

    fun observeDeck(id: String): Flow<DeckWithProgress?> =
        combine(deckDao.observeById(id), cardDao.observeByDeck(id)) { deck, cards ->
            deck?.let { DeckWithProgress(it, cards, deckProgress(cards)) }
        }

    // Suma total de tarjetas vencidas en todos los mazos (badge "Hoy").
    fun observeTotalDue(): Flow<Int> =
        cardDao.observeDueToday(todayKey()).map { it.size }

    suspend fun upsertDeck(deck: DeckEntity) = deckDao.upsert(deck)
    suspend fun deleteDeck(id: String) {
        deckDao.deleteById(id)
        cardDao.deleteByDeck(id)
    }

    suspend fun upsertCard(card: CardEntity) = cardDao.upsert(card)
    suspend fun deleteCard(id: String) = cardDao.deleteById(id)

    fun observeActivity(from: String): Flow<List<ActivityEntity>> =
        activityDao.observeFrom(from)

    suspend fun incrementActivity() {
        val today = todayKey()
        val existing = activityDao.getFrom(today).firstOrNull { it.date == today }
        activityDao.upsert(ActivityEntity(today, (existing?.count ?: 0) + 1))
    }
}
