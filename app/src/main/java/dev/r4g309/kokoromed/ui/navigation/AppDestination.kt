package dev.r4g309.kokoromed.ui.navigation

import kotlinx.serialization.Serializable

@Serializable object Library
@Serializable object Today
@Serializable object Progress
@Serializable object Settings

@Serializable data class DeckDetail(val deckId: String)

// max = 0 → sin límite
@Serializable data class Study(
    val deckId: String,
    val mode: String,
    val max: Int = 0,
)
