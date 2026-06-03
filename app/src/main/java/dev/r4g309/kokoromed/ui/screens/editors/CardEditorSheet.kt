package dev.r4g309.kokoromed.ui.screens.editors

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import dev.r4g309.kokoromed.data.db.CardEntity
import dev.r4g309.kokoromed.ui.theme.Success
import kotlinx.serialization.json.Json

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CardEditorSheet(
    card: CardEntity?,          // null = nueva tarjeta
    onDismiss: () -> Unit,
    onSave: (
        question: String, options: List<String>, correct: Int,
        explanation: String, tags: List<String>, difficulty: String
    ) -> Unit,
) {
    val initOptions = remember(card) {
        card?.let {
            runCatching { Json.decodeFromString<List<String>>(it.optionsJson) }
                .getOrDefault(List(4) { "" })
        }
            ?: List(4) { "" }
    }
    val initTags = remember(card) {
        card?.let {
            runCatching { Json.decodeFromString<List<String>>(it.tagsJson) }
                .getOrDefault(emptyList())
        }
            ?: emptyList()
    }

    var question by remember(card) { mutableStateOf(card?.question ?: "") }
    val options = remember(card) { mutableStateListOf(*initOptions.toTypedArray()) }
    var correct by remember(card) { mutableIntStateOf(card?.correct ?: 0) }
    var explanation by remember(card) { mutableStateOf(card?.explanation ?: "") }
    var tagsText by remember(card) { mutableStateOf(initTags.joinToString(", ")) }
    var difficulty by remember(card) { mutableStateOf(card?.difficulty ?: "medium") }

    val isValid = question.isNotBlank() && options.all { it.isNotBlank() }

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
                    stringResource(if (card != null) R.string.card_editor_title_edit else R.string.card_editor_title_new),
                    style = MaterialTheme.typography.titleLarge
                )
            }

            item {
                OutlinedTextField(
                    value = question,
                    onValueChange = { question = it },
                    label = { Text(stringResource(R.string.card_editor_question_label)) },
                    placeholder = { Text(stringResource(R.string.card_editor_question_placeholder)) },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2, maxLines = 4,
                    shape = MaterialTheme.shapes.medium,
                )
            }

            item {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    stringResource(R.string.card_editor_options_title),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    stringResource(R.string.card_editor_options_hint),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(4.dp))
                val letters = "ABCD"
                options.forEachIndexed { i, opt ->
                    val isCorrect = correct == i
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        // Radio button
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(28.dp)
                                .clip(CircleShape)
                                .background(
                                    if (isCorrect) Success
                                    else MaterialTheme.colorScheme.surfaceVariant
                                )
                                .border(
                                    1.5.dp,
                                    if (isCorrect) Success
                                    else MaterialTheme.colorScheme.outline,
                                    CircleShape
                                )
                                .clickable { correct = i },
                        ) {
                            if (isCorrect)
                                Icon(
                                    painter = painterResource(R.drawable.check_24px),
                                    null,
                                    tint = Color.White,
                                    modifier = Modifier.size(14.dp)
                                )
                        }

                        // Letra
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(28.dp)
                                .clip(RoundedCornerShape(6.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant),
                        ) {
                            Text(
                                letters[i].toString(),
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        OutlinedTextField(
                            value = opt,
                            onValueChange = { options[i] = it },
                            placeholder = { Text(stringResource(R.string.card_editor_option_placeholder, letters[i].toString())) },
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            shape = MaterialTheme.shapes.medium,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = if (isCorrect) Success
                                else MaterialTheme.colorScheme.primary,
                            ),
                        )
                    }
                }
            }
            } // cierre item opciones

            item {
                OutlinedTextField(
                    value = explanation,
                    onValueChange = { explanation = it },
                    label = { Text(stringResource(R.string.card_editor_explanation_label)) },
                    placeholder = { Text(stringResource(R.string.card_editor_explanation_placeholder)) },
                    supportingText = { Text(stringResource(R.string.card_editor_explanation_supporting)) },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2, maxLines = 5,
                    shape = MaterialTheme.shapes.medium,
                )
            }

            item {
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth(),
            ) {
                OutlinedTextField(
                    value = tagsText,
                    onValueChange = { tagsText = it },
                    label = { Text(stringResource(R.string.card_editor_tags_label)) },
                    placeholder = { Text(stringResource(R.string.card_editor_tags_placeholder)) },
                    supportingText = { Text(stringResource(R.string.card_editor_tags_supporting)) },
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    shape = MaterialTheme.shapes.medium,
                )

                // Dificultad
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    Text(
                        stringResource(R.string.card_editor_difficulty_label),
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    val easyLabel = stringResource(R.string.difficulty_easy)
                    val mediumLabel = stringResource(R.string.difficulty_medium)
                    val hardLabel = stringResource(R.string.difficulty_hard)
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        listOf(
                            "easy" to easyLabel, "medium" to mediumLabel,
                            "hard" to hardLabel
                        ).forEach { (v, l) ->
                            val sel = difficulty == v
                            val bg = if (sel) MaterialTheme.colorScheme.primaryContainer
                            else MaterialTheme.colorScheme.surfaceVariant
                            val fg = if (sel) MaterialTheme.colorScheme.onPrimaryContainer
                            else MaterialTheme.colorScheme.onSurfaceVariant
                            Surface(
                                shape = MaterialTheme.shapes.small,
                                color = bg,
                                modifier = Modifier.clickable { difficulty = v },
                            ) {
                                Text(
                                    l, style = MaterialTheme.typography.labelSmall,
                                    color = fg,
                                    modifier = Modifier.padding(
                                        horizontal = 8.dp, vertical = 6.dp
                                    )
                                )
                            }
                        }
                    }
                }
            }
            } // cierre item tags+dificultad

            item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                OutlinedButton(onClick = onDismiss, modifier = Modifier.weight(1f)) {
                    Text(stringResource(R.string.action_cancel))
                }
                Button(
                    onClick = {
                        val tags = tagsText.split(",")
                            .map { it.trim() }.filter { it.isNotBlank() }
                        onSave(
                            question.trim(), options.map { it.trim() },
                            correct, explanation.trim(), tags, difficulty
                        )
                    },
                    enabled = isValid,
                    modifier = Modifier.weight(1f),
                ) {
                    Text(stringResource(R.string.card_editor_save))
                }
            }
            } // cierre item botones
        }
    }
}
