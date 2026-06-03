package dev.r4g309.kokoromed.data.repository

import dev.r4g309.kokoromed.R
import dev.r4g309.kokoromed.data.db.CardEntity
import dev.r4g309.kokoromed.data.db.DeckEntity
import dev.r4g309.kokoromed.domain.model.todayKey
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.util.UUID

// ── Mapeo nombre ↔ drawable ───────────────────────────────────────────────────
// El JSON siempre viaja con nombres legibles ("heart", "pill"…).
// La DB almacena el @DrawableRes Int.

private val nameToRes = mapOf(
    "heart"       to R.drawable.favorite_24px,
    "pill"        to R.drawable.pill_24px,
    "droplet"     to R.drawable.water_drop_24px,
    "brain"       to R.drawable.neurology_24px,
    "stack"       to R.drawable.stacks_24px,
    "layers"      to R.drawable.layers_24px,
    "stethoscope" to R.drawable.stethoscope_24px,
    "trophy"      to R.drawable.trophy_24px,
)
private val resToName = nameToRes.entries.associate { (k, v) -> v to k }

fun iconNameToRes(name: String): Int =
    nameToRes[name.lowercase()] ?: R.drawable.stacks_24px

fun iconResToName(res: Int): String =
    resToName[res] ?: "stack"

// ── Formato JSON ──────────────────────────────────────────────────────────────

@Serializable
data class DeckJson(
    val deck: DeckMetaJson,
    val cards: List<CardJson>,
)

@Serializable
data class DeckMetaJson(
    val name: String,
    val description: String = "",
    val color: String = "#0d9488",
    val icon: String = "stack",       // nombre legible, no resource ID
)

@Serializable
data class CardJson(
    val question: String,
    val options: List<String>,
    val correct: Int,
    val explanation: String = "",
    val tags: List<String> = emptyList(),
    val difficulty: String = "medium",
)

private val parser = Json { ignoreUnknownKeys = true; coerceInputValues = true }
private fun uid() = UUID.randomUUID().toString().replace("-", "").take(8)

// ── Import ────────────────────────────────────────────────────────────────────

fun importDeckJson(raw: String, fallbackName: String = "Mazo importado"): DeckEntity {
    val parsed = parser.decodeFromString<DeckJson>(raw)
    return DeckEntity(
        id          = uid(),
        name        = parsed.deck.name.ifBlank { fallbackName },
        description = parsed.deck.description,
        color       = parsed.deck.color,
        icon        = iconNameToRes(parsed.deck.icon),
    )
}

fun importCardsJson(raw: String, deckId: String): List<CardEntity> {
    val parsed = parser.decodeFromString<DeckJson>(raw)
    val today  = todayKey()
    return parsed.cards.map { c ->
        CardEntity(
            id          = uid(),
            deckId      = deckId,
            question    = c.question,
            optionsJson = Json.encodeToString(c.options),
            correct     = c.correct,
            explanation = c.explanation,
            tagsJson    = Json.encodeToString(c.tags),
            difficulty  = c.difficulty,
            due         = today,
        )
    }
}

// ── Export ────────────────────────────────────────────────────────────────────

fun exportDeckJson(deck: DeckEntity, cards: List<CardEntity>): String {
    val obj = DeckJson(
        deck  = DeckMetaJson(
            name        = deck.name,
            description = deck.description,
            color       = deck.color,
            icon        = iconResToName(deck.icon),   // Int → nombre legible
        ),
        cards = cards.map { c ->
            CardJson(
                question    = c.question,
                options     = runCatching {
                    Json.decodeFromString<List<String>>(c.optionsJson)
                }.getOrDefault(emptyList()),
                correct     = c.correct,
                explanation = c.explanation,
                tags        = runCatching {
                    Json.decodeFromString<List<String>>(c.tagsJson)
                }.getOrDefault(emptyList()),
                difficulty  = c.difficulty,
            )
        },
    )
    return Json { prettyPrint = true }.encodeToString(obj)
}
