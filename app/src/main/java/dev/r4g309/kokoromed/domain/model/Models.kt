package dev.r4g309.kokoromed.domain.model

import kotlinx.serialization.Serializable

// ── SM-2 SRS state ────────────────────────────────────────────────────────────

@Serializable
data class SrsData(
    val ease: Float  = 2.5f,
    val interval: Int = 0,
    val reps: Int     = 0,
    val lapses: Int   = 0,
    val due: String   = "",   // "YYYY-MM-DD"
)

// ── Per-card study stats ──────────────────────────────────────────────────────

@Serializable
data class CardStats(
    val seen: Int    = 0,
    val correct: Int = 0,
)

// ── Difficulty ────────────────────────────────────────────────────────────────

enum class Difficulty { easy, medium, hard }

// ── Card state ────────────────────────────────────────────────────────────────

enum class CardState { new, learning, mastered }

// ── Study rating ─────────────────────────────────────────────────────────────

enum class Rating { again, hard, good, easy }
