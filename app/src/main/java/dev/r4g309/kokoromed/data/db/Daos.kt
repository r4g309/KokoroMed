package dev.r4g309.kokoromed.data.db

import androidx.room.*
import kotlinx.coroutines.flow.Flow

// ── Deck DAO ──────────────────────────────────────────────────────────────────

@Dao
interface DeckDao {
    @Query("SELECT * FROM decks ORDER BY createdAt ASC")
    fun observeAll(): Flow<List<DeckEntity>>

    @Query("SELECT * FROM decks WHERE id = :id")
    fun observeById(id: String): Flow<DeckEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(deck: DeckEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(decks: List<DeckEntity>)

    @Delete
    suspend fun delete(deck: DeckEntity)

    @Query("DELETE FROM decks WHERE id = :id")
    suspend fun deleteById(id: String)
}

// ── Card DAO ──────────────────────────────────────────────────────────────────

@Dao
interface CardDao {
    @Query("SELECT * FROM cards ORDER BY deckId, rowid ASC")
    fun observeAll(): Flow<List<CardEntity>>

    @Query("SELECT * FROM cards WHERE deckId = :deckId ORDER BY rowid ASC")
    fun observeByDeck(deckId: String): Flow<List<CardEntity>>

    @Query("SELECT * FROM cards WHERE id = :id")
    suspend fun getById(id: String): CardEntity?

    // Tarjetas vencidas hoy en todos los mazos (para badge "Hoy")
    @Query("SELECT * FROM cards WHERE due <= :today AND reps > 0")
    fun observeDueToday(today: String): Flow<List<CardEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(card: CardEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(cards: List<CardEntity>)

    @Delete
    suspend fun delete(card: CardEntity)

    @Query("DELETE FROM cards WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query("DELETE FROM cards WHERE deckId = :deckId")
    suspend fun deleteByDeck(deckId: String)
}

// ── Activity DAO ──────────────────────────────────────────────────────────────

@Dao
interface ActivityDao {
    @Query("SELECT * FROM activity ORDER BY date DESC")
    fun observeAll(): Flow<List<ActivityEntity>>

    @Query("SELECT * FROM activity WHERE date >= :from ORDER BY date ASC")
    suspend fun getFrom(from: String): List<ActivityEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entry: ActivityEntity)

    @Query("SELECT * FROM activity WHERE date >= :from ORDER BY date ASC")
    fun observeFrom(from: String): Flow<List<ActivityEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(entries: List<ActivityEntity>)
}
