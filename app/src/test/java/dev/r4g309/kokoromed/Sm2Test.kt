package dev.r4g309.kokoromed

import dev.r4g309.kokoromed.domain.model.CardState
import dev.r4g309.kokoromed.domain.model.Rating
import dev.r4g309.kokoromed.domain.model.SrsData
import dev.r4g309.kokoromed.domain.srs.MASTERED_EASE
import dev.r4g309.kokoromed.domain.srs.MASTERED_REPS
import dev.r4g309.kokoromed.domain.srs.buildQueue
import dev.r4g309.kokoromed.domain.srs.cardState
import dev.r4g309.kokoromed.domain.srs.computeStreak
import dev.r4g309.kokoromed.domain.srs.freshSrs
import dev.r4g309.kokoromed.domain.srs.isDue
import dev.r4g309.kokoromed.domain.srs.scheduleSM2
import dev.r4g309.kokoromed.domain.srs.StudyMode
import dev.r4g309.kokoromed.data.db.CardEntity
import org.junit.Assert.*
import org.junit.Test

private const val TODAY = "2026-06-02"
private const val YESTERDAY = "2026-06-01"
private const val TOMORROW = "2026-06-03"

class Sm2Test {

    // ── freshSrs ──────────────────────────────────────────────────────────────

    @Test fun `fresh card has ease 2_5 and zero reps`() {
        val s = freshSrs(TODAY)
        assertEquals(2.5f, s.ease)
        assertEquals(0, s.reps)
        assertEquals(0, s.interval)
        assertEquals(0, s.lapses)
        assertEquals(TODAY, s.due)
    }

    // ── scheduleSM2 — lapse (again) ───────────────────────────────────────────

    @Test fun `again resets reps to 0 and increments lapses`() {
        val s = scheduleSM2(SrsData(ease = 2.5f, interval = 10, reps = 3, lapses = 0, due = TODAY), Rating.again, TODAY)
        assertEquals(0, s.reps)
        assertEquals(1, s.interval)
        assertEquals(1, s.lapses)
    }

    @Test fun `again lowers ease`() {
        val before = freshSrs(TODAY)
        val after  = scheduleSM2(before, Rating.again, TODAY)
        assertTrue("ease should decrease after again", after.ease < before.ease)
    }

    // ── scheduleSM2 — first reps ──────────────────────────────────────────────

    @Test fun `first good gives interval 1`() {
        val s = scheduleSM2(freshSrs(TODAY), Rating.good, TODAY)
        assertEquals(1, s.interval)
        assertEquals(1, s.reps)
    }

    @Test fun `second good gives interval 6`() {
        val s1 = scheduleSM2(freshSrs(TODAY), Rating.good, TODAY)
        val s2 = scheduleSM2(s1, Rating.good, TODAY)
        assertEquals(6, s2.interval)
        assertEquals(2, s2.reps)
    }

    @Test fun `third good multiplies by ease`() {
        val s1 = scheduleSM2(freshSrs(TODAY), Rating.good, TODAY)
        val s2 = scheduleSM2(s1, Rating.good, TODAY)
        val s3 = scheduleSM2(s2, Rating.good, TODAY)
        // interval = round(6 * 2.5) = 15
        assertEquals(15, s3.interval)
        assertEquals(3, s3.reps)
    }

    // ── scheduleSM2 — hard / easy modifiers ───────────────────────────────────

    @Test fun `hard applies 0_7 multiplier`() {
        val s1 = scheduleSM2(freshSrs(TODAY), Rating.good, TODAY)
        val s2 = scheduleSM2(s1, Rating.good, TODAY)  // interval = 6
        val s3 = scheduleSM2(s2, Rating.hard, TODAY)
        // round(round(6 * ease) * 0.7) — ease ≈ 2.42 after two goods
        // interval before hard = round(6 * 2.42) = 15; * 0.7 = 10 (rounded)
        assertTrue("hard interval should be less than good", s3.interval < 15)
    }

    @Test fun `easy applies 1_3 multiplier`() {
        val s1 = scheduleSM2(freshSrs(TODAY), Rating.good, TODAY)
        val s2 = scheduleSM2(s1, Rating.good, TODAY)  // interval = 6
        val s3good = scheduleSM2(s2, Rating.good, TODAY)
        val s3easy = scheduleSM2(s2, Rating.easy, TODAY)
        assertTrue("easy interval should be >= good interval", s3easy.interval >= s3good.interval)
    }

    // ── ease bounds ───────────────────────────────────────────────────────────

    @Test fun `ease never drops below 1_3`() {
        var s = freshSrs(TODAY)
        repeat(20) { s = scheduleSM2(s, Rating.again, TODAY) }
        assertTrue("ease must be >= 1.3", s.ease >= 1.3f)
    }

    @Test fun `ease increases on easy`() {
        val s1 = freshSrs(TODAY)
        val s2 = scheduleSM2(s1, Rating.easy, TODAY)
        assertTrue(s2.ease > s1.ease)
    }

    // ── cardState ─────────────────────────────────────────────────────────────

    @Test fun `fresh card is new`() {
        assertEquals(CardState.new, cardState(freshSrs(TODAY)))
    }

    @Test fun `card with reps but low ease is learning`() {
        val s = SrsData(ease = 1.5f, interval = 3, reps = 5, lapses = 2, due = TOMORROW)
        assertEquals(CardState.learning, cardState(s))
    }

    @Test fun `card with enough reps and good ease is mastered`() {
        val s = SrsData(ease = MASTERED_EASE, interval = 10, reps = MASTERED_REPS, lapses = 0, due = TOMORROW)
        assertEquals(CardState.mastered, cardState(s))
    }

    @Test fun `mastered requires both reps and ease thresholds`() {
        val lowEase = SrsData(ease = 1.8f, interval = 10, reps = MASTERED_REPS, lapses = 0, due = TOMORROW)
        val lowReps = SrsData(ease = 2.5f, interval = 10, reps = MASTERED_REPS - 1, lapses = 0, due = TOMORROW)
        assertEquals(CardState.learning, cardState(lowEase))
        assertEquals(CardState.learning, cardState(lowReps))
    }

    // ── isDue ─────────────────────────────────────────────────────────────────

    @Test fun `card due today is due`() {
        val s = SrsData(ease = 2.5f, interval = 1, reps = 1, lapses = 0, due = TODAY)
        assertTrue(isDue(s, TODAY))
    }

    @Test fun `card due yesterday is due today`() {
        val s = SrsData(ease = 2.5f, interval = 1, reps = 1, lapses = 0, due = YESTERDAY)
        assertTrue(isDue(s, TODAY))
    }

    @Test fun `card due tomorrow is not due today`() {
        val s = SrsData(ease = 2.5f, interval = 1, reps = 1, lapses = 0, due = TOMORROW)
        assertFalse(isDue(s, TODAY))
    }

    // ── buildQueue ────────────────────────────────────────────────────────────

    private fun makeCard(id: String, reps: Int, lapses: Int, due: String) = CardEntity(
        id = id, deckId = "d1", question = "q", optionsJson = "[]",
        correct = 0, explanation = "", tagsJson = "[]", difficulty = "medium",
        reps = reps, lapses = lapses, due = due,
    )

    @Test fun `due mode returns only overdue reviewed cards`() {
        val cards = listOf(
            makeCard("new",  reps = 0, lapses = 0, due = TODAY),
            makeCard("due",  reps = 1, lapses = 0, due = TODAY),
            makeCard("future", reps = 1, lapses = 0, due = TOMORROW),
        )
        val queue = buildQueue(cards, StudyMode.due, TODAY)
        assertEquals(1, queue.size)
        assertEquals("due", queue[0].id)
    }

    @Test fun `new mode returns only new cards`() {
        val cards = listOf(
            makeCard("new1", reps = 0, lapses = 0, due = TODAY),
            makeCard("new2", reps = 0, lapses = 0, due = TODAY),
            makeCard("old",  reps = 2, lapses = 0, due = TOMORROW),
        )
        val queue = buildQueue(cards, StudyMode.new, TODAY)
        assertEquals(2, queue.size)
        assertTrue(queue.all { it.reps == 0 })
    }

    @Test fun `failed mode returns only cards with lapses`() {
        val cards = listOf(
            makeCard("ok",     reps = 3, lapses = 0, due = TOMORROW),
            makeCard("failed", reps = 1, lapses = 2, due = TOMORROW),
        )
        val queue = buildQueue(cards, StudyMode.failed, TODAY)
        assertEquals(1, queue.size)
        assertEquals("failed", queue[0].id)
    }

    @Test fun `exam mode returns all cards`() {
        val cards = listOf(
            makeCard("a", reps = 0, lapses = 0, due = TODAY),
            makeCard("b", reps = 1, lapses = 0, due = TOMORROW),
            makeCard("c", reps = 2, lapses = 1, due = TODAY),
        )
        val queue = buildQueue(cards, StudyMode.exam, TODAY)
        assertEquals(3, queue.size)
    }

    // ── computeStreak ─────────────────────────────────────────────────────────

    @Test fun `streak of 0 when no activity`() {
        assertEquals(0, computeStreak(emptyMap(), TODAY))
    }

    @Test fun `streak counts consecutive days ending today`() {
        val activity = mapOf(TODAY to 5, YESTERDAY to 3, "2026-05-31" to 4)
        assertEquals(3, computeStreak(activity, TODAY))
    }

    @Test fun `streak counts from yesterday if today missing`() {
        val activity = mapOf(YESTERDAY to 5, "2026-05-31" to 3)
        assertEquals(2, computeStreak(activity, TODAY))
    }

    @Test fun `streak breaks on gap`() {
        // gap: 2026-05-31 missing
        val activity = mapOf(TODAY to 5, YESTERDAY to 3, "2026-05-30" to 4)
        assertEquals(2, computeStreak(activity, TODAY))
    }

    // ── Regression: due date advances correctly ───────────────────────────────

    @Test fun `due date is in the future after scheduling`() {
        val s = scheduleSM2(freshSrs(TODAY), Rating.good, TODAY)
        assertTrue("due should be after today", s.due > TODAY)
    }

    @Test fun `interval 1 advances due by 1 day`() {
        val s = scheduleSM2(freshSrs(TODAY), Rating.good, TODAY)
        assertEquals(1, s.interval)
        assertEquals(TOMORROW, s.due)
    }
}
