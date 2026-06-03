package dev.r4g309.kokoromed.data.db

import androidx.annotation.DrawableRes
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "decks")
data class DeckEntity(
    @PrimaryKey val id: String,
    val name: String,
    val description: String,
    val color: String,    // "#RRGGBB"
    @DrawableRes val icon: Int,     // nombre del icono: "heart", "pill", etc.
    val createdAt: Long = System.currentTimeMillis(),
)

@Entity(
    tableName = "cards",
    foreignKeys = [ForeignKey(
        entity        = DeckEntity::class,
        parentColumns = ["id"],
        childColumns  = ["deckId"],
        onDelete      = ForeignKey.CASCADE,
    )],
    indices = [Index("deckId")],
)
data class CardEntity(
    @PrimaryKey val id: String,
    val deckId: String,
    val question: String,
    // Opciones serializadas como JSON array: ["a","b","c","d"]
    val optionsJson: String,
    val correct: Int,      // índice 0-based de la opción correcta
    val explanation: String,
    // Tags serializados como JSON array: ["tag1","tag2"]
    val tagsJson: String,
    val difficulty: String, // "easy" | "medium" | "hard"
    // SRS
    val ease: Float  = 2.5f,
    val interval: Int = 0,
    val reps: Int     = 0,
    val lapses: Int   = 0,
    val due: String   = "",  // "YYYY-MM-DD"
    // Stats
    val seen: Int    = 0,
    val correct2: Int = 0,   // correctCount (distinto del campo "correct" de arriba)
)

// Actividad de estudio: fecha → número de tarjetas estudiadas
@Entity(tableName = "activity")
data class ActivityEntity(
    @PrimaryKey val date: String,  // "YYYY-MM-DD"
    val count: Int,
)
