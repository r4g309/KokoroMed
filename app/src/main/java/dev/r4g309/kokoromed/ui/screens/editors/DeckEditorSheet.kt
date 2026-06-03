package dev.r4g309.kokoromed.ui.screens.editors

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import dev.r4g309.kokoromed.R
import dev.r4g309.kokoromed.data.db.DeckEntity
import androidx.core.graphics.toColorInt

private val DECK_COLORS = listOf(
    // Teals / verdes
    "#0d9488", "#059669", "#16a34a", "#4ade80",
    // Azules
    "#2563eb", "#0891b2", "#6366f1",
    // Morados / rosas
    "#7c3aed", "#a21caf", "#db2777",
    // Rojos / naranjas
    "#e11d48", "#ea580c", "#d97706",
)

private val DECK_ICONS = listOf(
    R.drawable.favorite_24px,
    R.drawable.pill_24px,
    R.drawable.water_drop_24px,
    R.drawable.neurology_24px,
    R.drawable.stacks_24px,
    R.drawable.layers_24px,
    R.drawable.stethoscope_24px,
    R.drawable.trophy_24px,
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeckEditorSheet(
    deck: DeckEntity?,          // null = crear nuevo
    onDismiss: () -> Unit,
    onSave: (name: String, description: String, color: String, icon: Int) -> Unit,
) {
    var name by remember(deck) { mutableStateOf(deck?.name ?: "") }
    var desc by remember(deck) { mutableStateOf(deck?.description ?: "") }
    var color by remember(deck) { mutableStateOf(deck?.color ?: DECK_COLORS[0]) }
    var icon by remember(deck) { mutableIntStateOf(deck?.icon ?: R.drawable.stacks_24px) }

    val accent = remember(color) {
        runCatching { Color(color.toColorInt()) }
            .getOrDefault(Color(0xFF0D9488))
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        dragHandle = { BottomSheetDefaults.DragHandle() },
    ) {
        LazyColumn(
            contentPadding = PaddingValues(start = 20.dp, end = 20.dp, bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            item {
                Text(
                    stringResource(if (deck != null) R.string.deck_editor_title_edit else R.string.deck_editor_title_new),
                    style = MaterialTheme.typography.titleLarge
                )
            }

            item {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text(stringResource(R.string.deck_editor_name_label)) },
                    placeholder = { Text(stringResource(R.string.deck_editor_name_placeholder)) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = MaterialTheme.shapes.medium,
                )
            }

            item {
                OutlinedTextField(
                    value = desc,
                    onValueChange = { desc = it },
                    label = { Text(stringResource(R.string.deck_editor_desc_label)) },
                    placeholder = { Text(stringResource(R.string.deck_editor_desc_placeholder)) },
                    supportingText = { Text(stringResource(R.string.deck_editor_desc_supporting)) },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2, maxLines = 3,
                    shape = MaterialTheme.shapes.medium,
                )
            }

            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        stringResource(R.string.deck_editor_color_label), style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Row(
                        modifier = Modifier.horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        DECK_COLORS.forEach { c ->
                            val ac = remember(c) {
                                runCatching { Color(c.toColorInt()) }.getOrDefault(Color.Gray)
                            }
                            val selected = color == c
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(ac)
                                    .then(
                                        if (selected) Modifier.border(
                                            3.dp, MaterialTheme.colorScheme.onBackground, CircleShape
                                        ) else Modifier
                                    )
                                    .clickable { color = c },
                            ) {
                                if (selected) Icon(
                                    painter = painterResource(R.drawable.check_24px), null,
                                    tint = Color.White, modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }
            }

            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        stringResource(R.string.deck_editor_icon_label), style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Row(
                        modifier = Modifier.horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        DECK_ICONS.forEach { ic ->
                            val selected = icon == ic
                            Surface(
                                shape = MaterialTheme.shapes.medium,
                                color = if (selected) accent.copy(alpha = 0.15f)
                                else MaterialTheme.colorScheme.surfaceVariant,
                                border = if (selected)
                                    androidx.compose.foundation.BorderStroke(2.dp, accent)
                                else null,
                                modifier = Modifier.size(44.dp).clickable { icon = ic },
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        painter = painterResource(ic),
                                        contentDescription = null,
                                        tint = if (selected) accent
                                        else MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(22.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    OutlinedButton(onClick = onDismiss, modifier = Modifier.weight(1f)) {
                        Text(stringResource(R.string.action_cancel))
                    }
                    Button(
                        onClick = { onSave(name.trim(), desc.trim(), color, icon) },
                        enabled = name.isNotBlank(),
                        modifier = Modifier.weight(1f),
                    ) {
                        Text(stringResource(if (deck != null) R.string.deck_editor_save else R.string.deck_editor_create))
                    }
                }
            }
        }
    }
}
