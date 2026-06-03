package dev.r4g309.kokoromed.ui.screens.progress

import androidx.annotation.DrawableRes
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.r4g309.kokoromed.R
import dev.r4g309.kokoromed.domain.model.todayKey
import dev.r4g309.kokoromed.ui.screens.dashboard.DashboardViewModel
import dev.r4g309.kokoromed.ui.theme.Danger
import dev.r4g309.kokoromed.ui.theme.Success
import dev.r4g309.kokoromed.ui.theme.TealSoft
import java.util.Calendar
import androidx.core.graphics.toColorInt

@Composable
fun ProgressScreen(
    onOpenDeck: (String) -> Unit,
    vm: DashboardViewModel = hiltViewModel(),
) {
    val state by vm.state.collectAsStateWithLifecycle()

    val totals = remember(state.decks) {
        state.decks.fold(intArrayOf(0, 0, 0, 0, 0)) { acc, dwp ->
            val p = dwp.progress
            acc[0] += p.mastered; acc[1] += p.learning
            acc[2] += p.new; acc[3] += p.due; acc[4] += p.total
            acc
        }
    }
    val masteryPct = if (totals[4] > 0) (totals[0] * 100) / totals[4] else 0

    LazyColumn(
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item {
            Text(
                stringResource(R.string.progress_subtitle),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // ── 4 stat cards ──────────────────────────────────────────────────────
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                StatCard(
                    R.drawable.local_fire_department_24px,
                    "${state.streak}",
                    stringResource(if (state.streak == 1) R.string.progress_stat_streak_singular else R.string.progress_stat_streak_plural),
                    Color(0xFFEA580C),
                    Modifier.weight(1f)
                )
                StatCard(
                    R.drawable.layers_24px,
                    "${state.todayCount}",
                    stringResource(R.string.progress_stat_today),
                    MaterialTheme.colorScheme.primary,
                    Modifier.weight(1f)
                )
            }
        }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                StatCard(
                    R.drawable.bar_chart_24px,
                    "${state.weekCount}",
                    stringResource(R.string.progress_stat_week),
                    Color(0xFF2563EB),
                    Modifier.weight(1f)
                )
                StatCard(
                    R.drawable.check_24px,
                    "$masteryPct%",
                    stringResource(R.string.progress_stat_mastery),
                    Success,
                    Modifier.weight(1f)
                )
            }
        }

        // ── Heatmap ───────────────────────────────────────────────────────────
        item {
            Surface(
                shape = MaterialTheme.shapes.large,
                color = MaterialTheme.colorScheme.surface,
                border = androidx.compose.foundation.BorderStroke(
                    1.dp, MaterialTheme.colorScheme.outline
                ),
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            stringResource(R.string.progress_activity_title), style = MaterialTheme.typography.titleSmall
                        )
                        Text(
                            stringResource(R.string.progress_activity_stats, state.totalReviews, state.totalDays),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    ActivityHeatmap(
                        activity = state.activity, color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }

        // ── Barra de dominio de tarjetas ──────────────────────────────────────
        item {
            Surface(
                shape = MaterialTheme.shapes.large,
                color = MaterialTheme.colorScheme.surface,
                border = androidx.compose.foundation.BorderStroke(
                    1.dp, MaterialTheme.colorScheme.outline
                ),
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(stringResource(R.string.progress_mastery_title), style = MaterialTheme.typography.titleSmall)

                    // Barra segmentada
                    if (totals[4] > 0) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(10.dp)
                                .clip(CircleShape),
                        ) {
                            val total = totals[4].toFloat()
                            if (totals[0] > 0) Box(
                                Modifier
                                    .weight(totals[0] / total)
                                    .fillMaxHeight()
                                    .background(Success)
                            )
                            if (totals[1] > 0) Box(
                                Modifier
                                    .weight(totals[1] / total)
                                    .fillMaxHeight()
                                    .background(MaterialTheme.colorScheme.primary)
                            )
                            if (totals[2] > 0) Box(
                                Modifier
                                    .weight(totals[2] / total)
                                    .fillMaxHeight()
                                    .background(MaterialTheme.colorScheme.surfaceVariant)
                            )
                        }
                    }

                    // Leyenda
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        MasteryLegendRow(stringResource(R.string.progress_mastery_mastered), Success, totals[0])
                        MasteryLegendRow(
                            stringResource(R.string.progress_mastery_learning), MaterialTheme.colorScheme.primary, totals[1]
                        )
                        MasteryLegendRow(
                            stringResource(R.string.progress_mastery_new), MaterialTheme.colorScheme.onSurfaceVariant, totals[2]
                        )
                    }

                    // Due callout
                    Surface(
                        shape = MaterialTheme.shapes.medium,
                        color = MaterialTheme.colorScheme.primaryContainer
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                        ) {
                            Column {
                                Text(
                                    "${totals[3]}",
                                    style = MaterialTheme.typography.headlineMedium,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    stringResource(R.string.progress_due_today),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Icon(
                                painter = painterResource(R.drawable.calendar_today_24px),
                                null,
                                modifier = Modifier.size(26.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }
        }

        // ── Progreso por mazo ─────────────────────────────────────────────────
        item {
            Text(
                stringResource(R.string.progress_by_deck_title),
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(top = 4.dp)
            )
        }

        items(state.decks, key = { it.deck.id }) { dwp ->
            val accent = remember(dwp.deck.color) {
                runCatching { Color(dwp.deck.color.toColorInt()) }.getOrDefault(
                    Color(0xFF0D9488)
                )
            }
            Surface(
                shape = MaterialTheme.shapes.large,
                color = MaterialTheme.colorScheme.surface,
                border = androidx.compose.foundation.BorderStroke(
                    1.dp, MaterialTheme.colorScheme.outline
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onOpenDeck(dwp.deck.id) },
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(36.dp)
                            .clip(MaterialTheme.shapes.small)
                            .background(accent)
                    ) {
                        Icon(
                            painter = painterResource(dwp.deck.icon),
                            null,
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Text(
                        dwp.deck.name,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.width(110.dp),
                        maxLines = 1,
                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                    )
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(7.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxHeight()
                                .fillMaxWidth(dwp.progress.pct / 100f)
                                .clip(CircleShape)
                                .background(accent)
                        )
                    }
                    Text(
                        "${dwp.progress.pct}%",
                        style = MaterialTheme.typography.labelMedium,
                        color = accent,
                        modifier = Modifier.width(36.dp)
                    )
                    if (dwp.progress.due > 0) {
                        Text(
                            stringResource(R.string.progress_due_today_short, dwp.progress.due),
                            style = MaterialTheme.typography.labelSmall,
                            color = accent
                        )
                    }
                }
            }
        }
    }
}

// ── Componentes ───────────────────────────────────────────────────────────────

@Composable
private fun StatCard(
    @DrawableRes icon: Int,
    value: String,
    label: String,
    accent: Color,
    modifier: Modifier = Modifier,
) {
    Surface(
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.surface,
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        modifier = modifier,
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Surface(
                shape = CircleShape,
                color = accent.copy(alpha = 0.14f),
                modifier = Modifier.size(42.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        painter = painterResource(icon),
                        null,
                        tint = accent,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }
            Column {
                Text(
                    value,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.ExtraBold
                )
                Text(
                    label,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun MasteryLegendRow(label: String, color: Color, count: Int) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Box(
            Modifier
                .size(10.dp)
                .clip(CircleShape)
                .background(color)
        )
        Text(
            label, style = MaterialTheme.typography.bodySmall, modifier = Modifier.weight(1f)
        )
        Text(
            "$count", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold
        )
    }
}

// ── Heatmap de actividad ──────────────────────────────────────────────────────

@Composable
private fun ActivityHeatmap(
    activity: Map<String, Int>,
    color: Color,
) {
    val weeks = 26
    val today = remember {
        Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
        }
    }
    val todayStr = remember { todayKey(today.time) }

    // Construir grid: 27 cols × 7 rows
    val grid = remember(activity) {
        val start = today.clone() as Calendar
        start.add(Calendar.WEEK_OF_YEAR, -weeks)
        // Alinear al lunes
        val dow = (start.get(Calendar.DAY_OF_WEEK) + 5) % 7
        start.add(Calendar.DAY_OF_YEAR, -dow)

        val maxVal = activity.values.maxOrNull()?.takeIf { it > 0 } ?: 1
        val cur = start.clone() as Calendar
        (0..weeks).map {
            (0 until 7).map {
                val key = todayKey(cur.time)
                val v = activity[key] ?: 0
                val future = key > todayStr
                val level = when {
                    future || v == 0 -> 0
                    v >= maxVal * 0.75 -> 4
                    v >= maxVal * 0.5 -> 3
                    v >= maxVal * 0.25 -> 2
                    else -> 1
                }
                cur.add(Calendar.DAY_OF_YEAR, 1)
                Pair(future, level)
            }
        }
    }

    Row(
        horizontalArrangement = Arrangement.spacedBy(2.dp),
        modifier = Modifier.horizontalScroll(rememberScrollState()),
    ) {
        grid.forEach { col ->
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                col.forEach { (future, level) ->
                    val bg = when {
                        future -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                        level == 0 -> MaterialTheme.colorScheme.surfaceVariant
                        else -> color.copy(alpha = 0.2f + level * 0.2f)
                    }
                    Box(
                        Modifier
                            .size(10.dp)
                            .clip(androidx.compose.foundation.shape.RoundedCornerShape(2.dp))
                            .background(bg)
                    )
                }
            }
        }
    }

    // Leyenda
    Row(
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Text(
            stringResource(R.string.heatmap_less),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        (0..4).forEach { level ->
            val bg = if (level == 0) MaterialTheme.colorScheme.surfaceVariant
            else color.copy(alpha = 0.2f + level * 0.2f)
            Box(
                Modifier
                    .size(10.dp)
                    .clip(androidx.compose.foundation.shape.RoundedCornerShape(2.dp))
                    .background(bg)
            )
        }
        Text(
            stringResource(R.string.heatmap_more),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
