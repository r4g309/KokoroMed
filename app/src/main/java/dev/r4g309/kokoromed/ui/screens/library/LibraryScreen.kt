package dev.r4g309.kokoromed.ui.screens.library

import androidx.compose.animation.*
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.core.graphics.toColorInt
import dev.r4g309.kokoromed.R
import dev.r4g309.kokoromed.data.repository.DeckWithProgress
import dev.r4g309.kokoromed.domain.srs.DeckProgress
import dev.r4g309.kokoromed.ui.components.ProgressRing

@Composable
fun LibraryScreen(
    onOpenDeck: (String) -> Unit,
    onStudyDeck: (id: String, name: String, progress: DeckProgress) -> Unit,
    onNewDeck: () -> Unit,
    onImport: () -> Unit = {},
    vm: LibraryViewModel = hiltViewModel(),
) {
    val decks    by vm.decks.collectAsStateWithLifecycle()
    val allDecks by vm.allDecks.collectAsStateWithLifecycle()
    val query    by vm.query.collectAsStateWithLifecycle()

    val totals = remember(allDecks) {
        allDecks.fold(Triple(0, 0, 0)) { (cards, due, mastered), d ->
            Triple(cards + d.progress.total, due + d.progress.due, mastered + d.progress.mastered)
        }
    }

    var fabExpanded by remember { mutableStateOf(false) }
    val fabRotation by animateFloatAsState(
        targetValue   = if (fabExpanded) 45f else 0f,
        animationSpec = tween(200),
        label         = "fabRotation",
    )

    Box(modifier = Modifier.fillMaxSize()) {
        // ── Contenido ─────────────────────────────────────────────────────────
        LazyColumn(
            contentPadding = PaddingValues(
                start  = 16.dp,
                end    = 16.dp,
                top    = 20.dp,
                bottom = 96.dp,   // espacio para el FAB
            ),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        stringResource(R.string.library_stats, allDecks.size, totals.first),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Text(
                        stringResource(R.string.library_due_today, totals.second),
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.primary,
                    )
                }
            }

            item {
                OutlinedTextField(
                    value         = query,
                    onValueChange = { vm.query.value = it },
                    modifier      = Modifier.fillMaxWidth(),
                    placeholder   = { Text(stringResource(R.string.library_search_placeholder)) },
                    leadingIcon   = {
                        Icon(painterResource(R.drawable.search_24px), null,
                            modifier = Modifier.size(18.dp))
                    },
                    singleLine = true,
                    shape      = MaterialTheme.shapes.medium,
                    colors     = OutlinedTextFieldDefaults.colors(
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                        focusedBorderColor   = MaterialTheme.colorScheme.primary,
                    ),
                )
            }

            if (decks.isEmpty()) {
                item { EmptyLibrary(hasDecks = allDecks.isNotEmpty(), onNewDeck = onNewDeck) }
            } else {
                items(decks, key = { it.deck.id }) { dwp ->
                    DeckCard(
                        dwp     = dwp,
                        onOpen  = { onOpenDeck(dwp.deck.id) },
                        onStudy = { onStudyDeck(dwp.deck.id, dwp.deck.name, dwp.progress) },
                    )
                }
            }
        }

        // ── Scrim al expandir ─────────────────────────────────────────────────
        if (fabExpanded) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.4f))
                    .clickable(
                        interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() },
                        indication = null,
                    ) { fabExpanded = false }
            )
        }

        // ── FAB speed dial ────────────────────────────────────────────────────
        Column(
            modifier              = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 20.dp, bottom = 24.dp),
            horizontalAlignment   = Alignment.End,
            verticalArrangement   = Arrangement.spacedBy(12.dp),
        ) {
            AnimatedVisibility(
                visible = fabExpanded,
                enter   = fadeIn(tween(150)) + slideInVertically(tween(150)) { it / 2 },
                exit    = fadeOut(tween(100)) + slideOutVertically(tween(100)) { it / 2 },
            ) {
                Column(
                    horizontalAlignment = Alignment.End,
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    FabItem(
                        label   = stringResource(R.string.library_fab_new_deck),
                        icon    = R.drawable.add_24px,
                        onClick = { fabExpanded = false; onNewDeck() },
                    )
                    FabItem(
                        label   = stringResource(R.string.library_fab_import),
                        icon    = R.drawable.upload_24px,
                        onClick = { fabExpanded = false; onImport() },
                    )
                }
            }

            FloatingActionButton(
                onClick           = { fabExpanded = !fabExpanded },
                containerColor    = MaterialTheme.colorScheme.primary,
                contentColor      = MaterialTheme.colorScheme.onPrimary,
            ) {
                Icon(
                    painterResource(R.drawable.add_24px),
                    contentDescription = if (fabExpanded) stringResource(R.string.action_close) else stringResource(R.string.action_add),
                    modifier = Modifier.rotate(fabRotation),
                )
            }
        }
    }
}

// ── Mini FAB con etiqueta ─────────────────────────────────────────────────────

@Composable
private fun FabItem(label: String, icon: Int, onClick: () -> Unit) {
    Row(
        verticalAlignment     = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Surface(
            shape  = MaterialTheme.shapes.small,
            color  = MaterialTheme.colorScheme.surface,
            shadowElevation = 4.dp,
        ) {
            Text(
                label,
                style    = MaterialTheme.typography.labelLarge,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            )
        }
        SmallFloatingActionButton(
            onClick        = onClick,
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor   = MaterialTheme.colorScheme.primary,
        ) {
            Icon(painterResource(icon), contentDescription = label,
                modifier = Modifier.size(20.dp))
        }
    }
}

// ── DeckCard ──────────────────────────────────────────────────────────────────

@Composable
fun DeckCard(dwp: DeckWithProgress, onOpen: () -> Unit, onStudy: () -> Unit) {
    val accent = remember(dwp.deck.color) {
        runCatching { Color(dwp.deck.color.toColorInt()) }.getOrDefault(Color(0xFF0D9488))
    }
    val p = dwp.progress

    Surface(
        shape           = MaterialTheme.shapes.large,
        color           = MaterialTheme.colorScheme.surface,
        border          = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        shadowElevation = 2.dp,
        modifier        = Modifier.fillMaxWidth().clickable(onClick = onOpen),
    ) {
        Column(modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)) {

            Row(modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.size(44.dp).clip(MaterialTheme.shapes.medium)
                        .background(accent),
                ) {
                    Icon(painterResource(dwp.deck.icon), null,
                        tint = Color.White, modifier = Modifier.size(22.dp))
                }
                ProgressRing(value = p.pct, color = accent, size = 50.dp, strokeWidth = 5.dp)
            }

            Text(dwp.deck.name, style = MaterialTheme.typography.titleMedium,
                maxLines = 1, overflow = TextOverflow.Ellipsis)
            if (dwp.deck.description.isNotBlank()) {
                Text(dwp.deck.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2, overflow = TextOverflow.Ellipsis)
            }

            val totalStr = stringResource(R.string.deck_stat_total, p.total)
            val dueStr = if (p.due > 0) stringResource(R.string.deck_stat_due_today, p.due) else null
            val accuracyStr = p.accuracy?.let { stringResource(R.string.deck_stat_accuracy, it) }
            Text(buildString {
                append(totalStr)
                if (dueStr != null) append("  ·  $dueStr")
                if (accuracyStr != null) append("  ·  $accuracyStr")
            }, style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant)

            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                MiniChip(stringResource(R.string.chip_new_count, p.new),
                    MaterialTheme.colorScheme.surfaceVariant,
                    MaterialTheme.colorScheme.onSurfaceVariant)
                MiniChip(stringResource(R.string.chip_learning_count, p.learning),
                    Color(0xFFFEF3C7), Color(0xFFB45309))
                MiniChip(stringResource(R.string.chip_mastered_count, p.mastered),
                    Color(0xFFDCFCE7), Color(0xFF16A34A))
            }

            Button(onClick = onStudy, modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = accent, contentColor = Color.White)) {
                Icon(painterResource(R.drawable.play_arrow_24px), null,
                    modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(6.dp))
                Text(stringResource(R.string.action_study), style = MaterialTheme.typography.labelLarge)
            }
        }
    }
}

@Composable
private fun MiniChip(label: String, bg: Color, fg: Color) {
    Surface(shape = CircleShape, color = bg) {
        Text(label, style = MaterialTheme.typography.labelSmall, color = fg,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp))
    }
}

@Composable
private fun EmptyLibrary(hasDecks: Boolean, onNewDeck: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(vertical = 48.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Surface(shape = CircleShape, color = MaterialTheme.colorScheme.surfaceVariant,
            modifier = Modifier.size(72.dp)) {
            Box(contentAlignment = Alignment.Center) {
                Icon(painterResource(R.drawable.layers_24px), null,
                    modifier = Modifier.size(34.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        Text(
            stringResource(if (hasDecks) R.string.library_empty_title_no_results else R.string.library_empty_title_no_decks),
            style = MaterialTheme.typography.titleMedium)
        Text(
            stringResource(if (hasDecks) R.string.library_empty_body_no_results else R.string.library_empty_body_no_decks),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}
