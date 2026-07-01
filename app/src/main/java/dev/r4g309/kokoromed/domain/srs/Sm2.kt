package dev.r4g309.kokoromed.domain.srs

import dev.r4g309.kokoromed.data.db.CardEntity
import dev.r4g309.kokoromed.domain.model.CardState
import dev.r4g309.kokoromed.domain.model.Rating
import dev.r4g309.kokoromed.domain.model.SrsData
import dev.r4g309.kokoromed.domain.model.todayKey
import java.util.Calendar
import kotlin.math.max
import kotlin.math.roundToInt

const val MASTERED_REPS = 3
const val MASTERED_EASE = 2.3f

// ── Core SM-2 ─────────────────────────────────────────────────────────────────

fun freshSrs(today: String = todayKey()): SrsData =
    SrsData(ease = 2.5f, interval = 0, reps = 0, lapses = 0, due = today)

fun scheduleSM2(srs: SrsData, rating: Rating, today: String = todayKey()): SrsData {
    val q = when (rating) {
        Rating.again -> 0
        Rating.hard  -> 3
        Rating.good  -> 4
        Rating.easy  -> 5
    }

    var ease     = srs.ease
    var interval = srs.interval
    var reps     = srs.reps
    var lapses   = srs.lapses

    if (q < 3) {
        reps     = 0
        interval = 0
        lapses  += 1
    } else {
        interval = when (reps) {
            0    -> 1
            1    -> 6
            else -> (interval * ease).roundToInt()
        }
        if (rating == Rating.hard) interval = max(1, (interval * 0.7f).roundToInt())
        if (rating == Rating.easy) interval = (interval * 1.3f).roundToInt()
        reps += 1
    }

    ease = max(1.3f, ease + (0.1f - (5 - q) * (0.08f + (5 - q) * 0.02f)))

    val cal = Calendar.getInstance()
    cal.add(Calendar.DAY_OF_YEAR, interval)
    val due = todayKey(cal.time)

    return SrsData(
        ease     = (ease * 100).roundToInt() / 100f,
        interval = interval,
        reps     = reps,
        lapses   = lapses,
        due      = due,
    )
}

// ── Card state ────────────────────────────────────────────────────────────────

fun cardState(srs: SrsData): CardState = when {
    srs.reps == 0                                    -> CardState.new
    srs.reps >= MASTERED_REPS && srs.ease >= MASTERED_EASE -> CardState.mastered
    else                                             -> CardState.learning
}

fun isDue(srs: SrsData, ref: String = todayKey()): Boolean = srs.due <= ref

// ── Queue builder ─────────────────────────────────────────────────────────────

enum class StudyMode { due, new, failed, exam }

fun buildQueue(cards: List<CardEntity>, mode: StudyMode, today: String = todayKey()): List<CardEntity> {
    val filtered = when (mode) {
        StudyMode.due    -> cards.filter { it.reps > 0 && it.due <= today }
        StudyMode.new    -> cards.filter { it.reps == 0 }
        StudyMode.failed -> cards.filter { it.lapses > 0 && it.due <= today && cardState(it.toSrsData()) != CardState.mastered }
        StudyMode.exam   -> cards
    }
    return filtered.shuffled()
}

// ── Deck progress ─────────────────────────────────────────────────────────────

data class DeckProgress(
    val total: Int,
    val mastered: Int,
    val learning: Int,
    val new: Int,
    val due: Int,
    val failed: Int,        // lapses > 0 y no dominadas (coincide exactamente con buildQueue failed)
    val pct: Int,           // % dominadas
    val accuracy: Int?,     // null si nunca se ha estudiado
)

fun deckProgress(cards: List<CardEntity>, today: String = todayKey()): DeckProgress {
    val total   = cards.size.coerceAtLeast(1)
    var mastered = 0; var learning = 0; var new = 0; var due = 0; var failed = 0
    var seen = 0; var correct = 0
    cards.forEach { c ->
        val srs = c.toSrsData()
        val state = cardState(srs)
        when (state) {
            CardState.mastered -> mastered++
            CardState.learning -> learning++
            CardState.new      -> new++
        }
        if (srs.reps > 0 && isDue(srs, today)) due++
        if (c.lapses > 0 && c.due <= today && state != CardState.mastered) failed++
        seen    += c.seen
        correct += c.correct2
    }
    return DeckProgress(
        total    = cards.size,
        mastered = mastered,
        learning = learning,
        new      = new,
        due      = due,
        failed   = failed,
        pct      = (mastered * 100) / total,
        accuracy = if (seen > 0) (correct * 100) / seen else null,
    )
}

// ── Streak ────────────────────────────────────────────────────────────────────

fun computeStreak(activity: Map<String, Int>, today: String = todayKey()): Int {
    val cal = Calendar.getInstance()
    // si hoy no se estudió, la racha puede continuar desde ayer
    if (!activity.containsKey(today)) cal.add(Calendar.DAY_OF_YEAR, -1)
    var streak = 0
    while (activity.containsKey(todayKey(cal.time))) {
        streak++
        cal.add(Calendar.DAY_OF_YEAR, -1)
    }
    return streak
}

// ── CardEntity ↔ SrsData helpers ──────────────────────────────────────────────

fun CardEntity.toSrsData() = SrsData(
    ease     = ease,
    interval = interval,
    reps     = reps,
    lapses   = lapses,
    due      = due,
)

fun CardEntity.applySrs(srs: SrsData, wasCorrect: Boolean) = copy(
    ease     = srs.ease,
    interval = srs.interval,
    reps     = srs.reps,
    lapses   = srs.lapses,
    due      = srs.due,
    seen     = seen + 1,
    correct2 = correct2 + if (wasCorrect) 1 else 0,
)
