package dev.r4g309.kokoromed.ui.screens.deck

import android.annotation.SuppressLint
import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import androidx.core.graphics.toColorInt
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.r4g309.kokoromed.R
import dev.r4g309.kokoromed.data.db.CardEntity
import dev.r4g309.kokoromed.data.repository.exportDeckJson
import dev.r4g309.kokoromed.domain.model.CardState
import dev.r4g309.kokoromed.domain.model.SrsData
import dev.r4g309.kokoromed.domain.srs.cardState
import dev.r4g309.kokoromed.ui.components.DifficultyBadge
import dev.r4g309.kokoromed.ui.theme.Danger
import dev.r4g309.kokoromed.ui.theme.DangerSoft
import dev.r4g309.kokoromed.ui.theme.Success
import dev.r4g309.kokoromed.ui.theme.SuccessSoft
import dev.r4g309.kokoromed.ui.theme.extended
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import java.io.File

@Composable
fun DeckDetailScreen(
    onBack: () -> Unit,
    onTitleChange: (String) -> Unit = {},
    onStudy: (id: String, name: String, progress: dev.r4g309.kokoromed.domain.srs.DeckProgress) -> Unit,
    onEditDeck: (dev.r4g309.kokoromed.data.db.DeckEntity) -> Unit = {},
    onAddCard: (deckId: String) -> Unit = {},
    onEditCard: (CardEntity) -> Unit = {},
    onDeleteCard: (cardId: String) -> Unit = {},
    onDeleteDeck: (deckId: String) -> Unit = {},
    vm: DeckDetailViewModel = hiltViewModel(),
) {
    val uiState by vm.uiState.collectAsStateWithLifecycle()
    val filter by vm.filter.collectAsStateWithLifecycle()
    var showDeleteConfirm by remember { mutableStateOf(false) }

    // Propaga el nombre del mazo al TopAppBar global
    LaunchedEffect(uiState?.deck?.name) {
        uiState?.deck?.name?.let { onTitleChange(it) }
    }

    val context = LocalContext.current
    val scope   = rememberCoroutineScope()

    // Guarda el JSON pendiente hasta que el launcher devuelva el URI
    var pendingJson by remember { mutableStateOf<String?>(null) }

    val saveFileLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("application/json")
    ) { uri ->
        uri ?: return@rememberLauncherForActivityResult
        scope.launch(Dispatchers.IO) {
            pendingJson?.let { json ->
                context.contentResolver.openOutputStream(uri)?.use { it.write(json.toByteArray()) }
                pendingJson = null
            }
        }
    }

    val dwp = uiState ?: return

    val accent = remember(dwp.deck.color) {
        runCatching { Color(dwp.deck.color.toColorInt()) }
            .getOrDefault(Color(0xFF0D9488))
    }
    val p = dwp.progress

    LazyColumn(
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 20.dp),
        verticalArrangement = Arrangement.spacedBy(0.dp),
    ) {
        // ── Hero ──────────────────────────────────────────────────────────────
        item {
            Column(verticalArrangement = Arrangement.spacedBy(14.dp),
                modifier = Modifier.padding(bottom = 20.dp)) {

                Row(verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp)) {

                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(54.dp)
                            .clip(MaterialTheme.shapes.large)
                            .background(accent),
                    ) {
                        Icon(painter = painterResource (dwp.deck.icon),
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(28.dp))
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Text(dwp.deck.name,
                            style = MaterialTheme.typography.headlineMedium,
                            maxLines = 2, overflow = TextOverflow.Ellipsis)
                        if (dwp.deck.description.isNotBlank()) {
                            Text(dwp.deck.description,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 2, overflow = TextOverflow.Ellipsis)
                        }
                    }
                }

                // Acciones hero
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()) {

                    OutlinedIconButton(onClick = { onEditDeck(dwp.deck) },
                        modifier = Modifier.size(42.dp)) {
                        Icon(painter = painterResource(R.drawable.edit_24px), contentDescription = stringResource(R.string.action_edit),
                            modifier = Modifier.size(18.dp))
                    }
                    // Compartir: genera el .json y abre el selector del sistema
                    // (WhatsApp, correo, Drive…) vía FileProvider + ACTION_SEND.
                    OutlinedIconButton(
                        onClick = {
                            val json = exportDeckJson(dwp.deck, dwp.cards)
                            val fileName = dwp.deck.name
                                .replace(Regex("[^\\w]"), "_") + ".json"
                            val file = File(context.cacheDir, fileName)
                            file.writeText(json)
                            val uri = FileProvider.getUriForFile(
                                context, "${context.packageName}.fileprovider", file)
                            val intent = Intent(Intent.ACTION_SEND).apply {
                                type = "application/json"
                                putExtra(Intent.EXTRA_STREAM, uri)
                                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                            }
                            context.startActivity(
                                Intent.createChooser(intent, context.getString(R.string.deck_detail_share_chooser)))
                        },
                        modifier = Modifier.size(42.dp),
                    ) {
                        Icon(painterResource(R.drawable.share_24px),
                            contentDescription = stringResource(R.string.deck_detail_export_share),
                            modifier = Modifier.size(18.dp))
                    }
                    // Guardar el .json como archivo en el dispositivo.
                    OutlinedIconButton(
                        onClick = {
                            pendingJson = exportDeckJson(dwp.deck, dwp.cards)
                            val fileName = dwp.deck.name
                                .replace(Regex("[^\\w]"), "_") + ".json"
                            saveFileLauncher.launch(fileName)
                        },
                        modifier = Modifier.size(42.dp),
                    ) {
                        Icon(painterResource(R.drawable.download_24px),
                            contentDescription = stringResource(R.string.deck_detail_export_save),
                            modifier = Modifier.size(18.dp))
                    }
                    OutlinedIconButton(onClick = { showDeleteConfirm = true },
                        modifier = Modifier.size(42.dp)) {
                        Icon(painter = painterResource(R.drawable.delete_24px), contentDescription = stringResource(R.string.action_delete),
                            modifier = Modifier.size(18.dp))
                    }
                    Button(
                        onClick = { onStudy(dwp.deck.id, dwp.deck.name, dwp.progress) },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = accent, contentColor = Color.White),
                    ) {
                        Icon(painter = painterResource(R.drawable.play_arrow_24px), contentDescription = null,
                            modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(6.dp))
                        Text(stringResource(R.string.action_study), style = MaterialTheme.typography.labelLarge)
                    }
                }
            }
        }

        // ── Stats 2×2 ─────────────────────────────────────────────────────────
        item {
            Row(modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)) {

                StatCard("${p.pct}%", stringResource(R.string.deck_detail_stat_completed), accent, Modifier.weight(1f))
                StatCard("${p.due}", stringResource(R.string.deck_detail_stat_due_today), MaterialTheme.colorScheme.onBackground,
                    Modifier.weight(1f))
            }
        }
        item {
            Row(modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)) {

                StatCard("${p.mastered}/${p.total}", stringResource(R.string.deck_detail_stat_mastered),
                    MaterialTheme.colorScheme.onBackground, Modifier.weight(1f))
                StatCard(if (p.accuracy != null) "${p.accuracy}%" else "—",
                    stringResource(R.string.deck_detail_stat_accuracy), MaterialTheme.colorScheme.onBackground, Modifier.weight(1f))
            }
        }

        // ── Cabecera de tarjetas + filtro ─────────────────────────────────────
        item {
            Column(modifier = Modifier.padding(bottom = 12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)) {

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(stringResource(R.string.deck_detail_cards_title), style = MaterialTheme.typography.titleLarge,
                        modifier = Modifier.weight(1f))
                    Surface(shape = CircleShape,
                        color = MaterialTheme.colorScheme.surfaceVariant) {
                        Text("${dwp.cards.size}",
                            style = MaterialTheme.typography.labelMedium,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp))
                    }
                }

                // Añadir tarjeta
                FilledTonalButton(onClick = { onAddCard(dwp.deck.id) },
                    modifier = Modifier.fillMaxWidth()) {
                    Icon(painter = painterResource(R.drawable.add_24px), null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text(stringResource(R.string.deck_detail_add_card))
                }

                // Segmented filter
                val filters = listOf(
                    CardFilter.all      to stringResource(R.string.deck_detail_filter_all),
                    CardFilter.new      to stringResource(R.string.deck_detail_filter_new),
                    CardFilter.learning to stringResource(R.string.deck_detail_filter_learning),
                    CardFilter.mastered to stringResource(R.string.deck_detail_filter_mastered),
                )
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(filters) { (f, label) ->
                        FilterChip(
                            selected = filter == f,
                            onClick  = { vm.filter.value = f },
                            label    = { Text(label,
                                style = MaterialTheme.typography.labelMedium) },
                        )
                    }
                }
            }
        }

        // ── Lista de tarjetas ─────────────────────────────────────────────────
        if (dwp.cards.isEmpty()) {
            item {
                Box(modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 32.dp),
                    contentAlignment = Alignment.Center) {
                    Text(stringResource(R.string.deck_detail_empty_category),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        } else {
            items(dwp.cards, key = { it.id }) { card ->
                CardRow(
                    card = card,
                    onEdit   = { onEditCard(card) },
                    onDelete = { onDeleteCard(card.id) },
                )
                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
            }
        }
    }

    // ── Confirmar borrado de mazo ─────────────────────────────────────────────
    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text(stringResource(R.string.deck_detail_delete_title)) },
            text  = { Text(stringResource(R.string.deck_detail_delete_body)) },
            confirmButton = {
                TextButton(onClick = {
                    showDeleteConfirm = false
                    onDeleteDeck(dwp.deck.id)
                }) { Text(stringResource(R.string.action_delete), color = Danger) }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) { Text(stringResource(R.string.action_cancel)) }
            },
        )
    }
}

// ── StatCard ──────────────────────────────────────────────────────────────────

@Composable
private fun StatCard(
    value: String,
    label: String,
    valueColor: Color,
    modifier: Modifier = Modifier,
) {
    Surface(
        shape  = MaterialTheme.shapes.medium,
        color  = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        modifier = modifier,
    ) {
        Column(modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(value,
                style = MaterialTheme.typography.headlineMedium,
                color = valueColor,
                fontWeight = FontWeight.ExtraBold)
            Text(label,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

// ── CardRow ───────────────────────────────────────────────────────────────────

@Composable
private fun CardRow(
    card: CardEntity,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
) {
    val srs = SrsData(card.ease, card.interval, card.reps, card.lapses, card.due)
    val state = cardState(srs)
    val dotColor = when (state) {
        CardState.new      -> MaterialTheme.colorScheme.onSurfaceVariant
        CardState.learning -> Color(0xFFF59E0B)
        CardState.mastered -> Success
    }

    val options = remember(card.optionsJson) {
        runCatching { Json.decodeFromString<List<String>>(card.optionsJson) }
            .getOrDefault(emptyList())
    }
    val accuracy = if (card.seen > 0)
        (card.correct2 * 100) / card.seen else null

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(11.dp),
        verticalAlignment = Alignment.Top,
    ) {
        // Dot indicador de estado
        Surface(
            shape = CircleShape,
            color = dotColor,
            modifier = Modifier
                .padding(top = 7.dp)
                .size(8.dp),
        ) {}

        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                card.question,
                style = MaterialTheme.typography.bodyMedium,
                overflow = TextOverflow.Ellipsis,
            )

            // Chips + acciones en la misma fila
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                // Chips con scroll horizontal
                Row(
                    modifier = Modifier
                        .weight(1f)
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    if (options.isNotEmpty()) {
                        Icon(
                            painter = painterResource(R.drawable.check_24px),
                            contentDescription = null,
                            modifier = Modifier.size(12.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Text(
                            options.getOrNull(card.correct) ?: "",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                        )
                    }
                    DifficultyBadge(card.difficulty)
                    if (accuracy != null) {
                        Text(
                            "$accuracy% · ${card.seen}×",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }

                // Botones fijos a la derecha
                Row(horizontalArrangement = Arrangement.spacedBy((-8).dp)) {
                    IconButton(onClick = onEdit, modifier = Modifier.size(36.dp)) {
                        Icon(
                            painter = painterResource(R.drawable.edit_24px),
                            contentDescription = stringResource(R.string.action_edit),
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    IconButton(onClick = onDelete, modifier = Modifier.size(36.dp)) {
                        Icon(
                            painter = painterResource(R.drawable.delete_24px),
                            contentDescription = stringResource(R.string.action_delete),
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
        }
    }
}
