package dev.r4g309.kokoromed.ui.screens.today

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.r4g309.kokoromed.R
import dev.r4g309.kokoromed.data.repository.DeckWithProgress
import dev.r4g309.kokoromed.domain.model.todayKey
import dev.r4g309.kokoromed.domain.srs.StudyMode
import dev.r4g309.kokoromed.domain.srs.isDue
import dev.r4g309.kokoromed.domain.srs.toSrsData
import dev.r4g309.kokoromed.ui.screens.dashboard.DashboardViewModel
import dev.r4g309.kokoromed.ui.theme.Success
import dev.r4g309.kokoromed.ui.theme.SuccessSoft
import dev.r4g309.kokoromed.ui.theme.TealSoft
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import androidx.core.graphics.toColorInt

@Composable
fun TodayScreen(
    onStudyDeck: (id: String, name: String, progress: dev.r4g309.kokoromed.domain.srs.DeckProgress) -> Unit,
    vm: DashboardViewModel = hiltViewModel(),
) {
    val state by vm.state.collectAsStateWithLifecycle()
    val today = remember { todayKey() }

    // Mazos con tarjetas vencidas o nuevas
    val dueByDeck = remember(state.decks, today) {
        state.decks.mapNotNull { dwp ->
            val due = dwp.cards.count { it.reps > 0 && isDue(it.toSrsData(), today) }
            val new = dwp.cards.count { it.reps == 0 }
            if (due > 0 || new > 0) Triple(dwp, due, new) else null
        }
    }

    // Próximos 14 días
    val upcoming = remember(state.decks) {
        (0 until 14).map { i ->
            val cal = Calendar.getInstance()
            cal.add(Calendar.DAY_OF_YEAR, i)
            cal.set(Calendar.HOUR_OF_DAY, 0); cal.set(Calendar.MINUTE, 0)
            cal.set(Calendar.SECOND, 0); cal.set(Calendar.MILLISECOND, 0)
            val key = todayKey(cal.time)
            val count = state.decks.sumOf { dwp ->
                dwp.cards.count { c -> c.reps > 0 && c.due == key }
            }
            Triple(cal.time, key, count)
        }
    }
    val maxUp = remember(upcoming, dueByDeck) {
        maxOf(1, upcoming.maxOfOrNull { it.third } ?: 0, dueByDeck.sumOf { it.second })
    }

    val totalDue = dueByDeck.sumOf { it.second }

    LazyColumn(
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            Text(
                if (totalDue > 0) stringResource(R.string.today_cards_due, totalDue)
                else stringResource(R.string.today_all_done),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        // ── Estado vacío ──────────────────────────────────────────────────────
        if (dueByDeck.isEmpty()) {
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    Surface(
                        shape = CircleShape, color = SuccessSoft, modifier = Modifier.size(64.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                painter = painterResource(R.drawable.check_24px),
                                null,
                                modifier = Modifier.size(28.dp),
                                tint = Success
                            )
                        }
                    }
                    Text(stringResource(R.string.today_nothing_pending), style = MaterialTheme.typography.titleMedium)
                    Text(
                        stringResource(R.string.today_come_back),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            items(dueByDeck, key = { it.first.deck.id }) { (dwp, due, new) ->
                TodayDeckCard(
                    dwp = dwp,
                    due = due,
                    new = new,
                    onStudy = { onStudyDeck(dwp.deck.id, dwp.deck.name, dwp.progress) })
            }
        }

        // ── Próximos repasos ──────────────────────────────────────────────────
        item {
            Text(
                stringResource(R.string.today_upcoming),
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(top = 8.dp)
            )
        }

        item {
            Surface(
                shape = MaterialTheme.shapes.large,
                color = MaterialTheme.colorScheme.surface,
                border = androidx.compose.foundation.BorderStroke(
                    1.dp, MaterialTheme.colorScheme.outline
                ),
            ) {
                val fmt = remember { SimpleDateFormat("EEE d MMM", Locale("es")) }
                Column(modifier = Modifier.padding(vertical = 8.dp)) {
                    upcoming.forEachIndexed { i, (date, _, count) ->
                        val isToday = i == 0
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                        ) {
                            Text(
                                if (isToday) stringResource(R.string.today_label) else fmt.format(date),
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontWeight = if (isToday) FontWeight.Bold else FontWeight.Normal
                                ),
                                color = if (isToday) MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.width(96.dp),
                            )
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(7.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.surfaceVariant)
                            ) {
                                if (count > 0) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxHeight()
                                            .fillMaxWidth(count.toFloat() / maxUp)
                                            .clip(CircleShape)
                                            .background(
                                                if (isToday) MaterialTheme.colorScheme.primary
                                                else TealSoft
                                            )
                                    )
                                }
                            }
                            Text(
                                if (count > 0) "$count" else "—",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.width(24.dp),
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TodayDeckCard(
    dwp: DeckWithProgress,
    due: Int,
    new: Int,
    onStudy: () -> Unit,
) {
    val accent = remember(dwp.deck.color) {
        runCatching { Color(dwp.deck.color.toColorInt()) }.getOrDefault(
            Color(
                0xFF0D9488
            )
        )
    }

    Surface(
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.surface,
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(13.dp),
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(44.dp)
                    .clip(MaterialTheme.shapes.medium)
                    .background(accent),
            ) {
                Icon(
                    painter = painterResource(dwp.deck.icon),
                    null,
                    tint = Color.White,
                    modifier = Modifier.size(22.dp)
                )
            }
            Column(
                modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)
            ) {
                Text(dwp.deck.name, style = MaterialTheme.typography.titleSmall)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (due > 0) Text(
                        stringResource(R.string.today_due_count, due),
                        style = MaterialTheme.typography.bodySmall,
                        color = accent,
                        fontWeight = FontWeight.SemiBold
                    )
                    if (new > 0) Text(
                        stringResource(R.string.today_new_count, new),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            FilledTonalButton(
                onClick = onStudy,
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
            ) {
                Icon(
                    painter = painterResource(R.drawable.play_arrow_24px),
                    null,
                    modifier = Modifier.size(15.dp)
                )
                Spacer(Modifier.width(5.dp))
                Text(stringResource(R.string.action_study), style = MaterialTheme.typography.labelSmall)
            }
        }
    }
}
