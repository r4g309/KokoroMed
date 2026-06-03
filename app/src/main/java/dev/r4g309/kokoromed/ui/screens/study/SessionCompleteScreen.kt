package dev.r4g309.kokoromed.ui.screens.study

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import dev.r4g309.kokoromed.R
import dev.r4g309.kokoromed.ui.components.ProgressRing
import dev.r4g309.kokoromed.ui.theme.Danger
import dev.r4g309.kokoromed.ui.theme.Success
import dev.r4g309.kokoromed.ui.theme.extended
import kotlinx.serialization.json.Json

@Composable
fun SessionCompleteScreen(
    state: StudyUiState,
    onExit: () -> Unit,
) {
    val pct = if (state.total > 0) (state.correct * 100) / state.total else 0
    val grade = when {
        pct >= 90 -> stringResource(R.string.session_grade_excellent)
        pct >= 70 -> stringResource(R.string.session_grade_good)
        pct >= 50 -> stringResource(R.string.session_grade_improving)
        else      -> stringResource(R.string.session_grade_keep_trying)
    }
    val ringColor = when {
        pct >= 70 -> Success
        pct >= 50 -> MaterialTheme.colorScheme.primary
        else -> Danger
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        item {
            // Icono superior
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primaryContainer,
                modifier = Modifier.size(72.dp),
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        painter = painterResource(R.drawable.star_shine_24px),
                        contentDescription = null,
                        modifier = Modifier.size(34.dp),
                        tint = MaterialTheme.colorScheme.primary,
                    )
                }
            }
        }

        item {
            Text(
                grade, style = MaterialTheme.typography.headlineLarge, textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(4.dp))
            Text(
                if (state.isExam) stringResource(R.string.session_exam_complete) else stringResource(R.string.session_study_complete),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
        }

        // Anillo de progreso + score
        item {
            Box(
                contentAlignment = Alignment.Center, modifier = Modifier.padding(vertical = 8.dp)
            ) {
                ProgressRing(
                    value = pct, color = ringColor, size = 140.dp, strokeWidth = 12.dp
                )
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        "$pct%", style = MaterialTheme.typography.displayLarge, color = ringColor
                    )
                    Text(
                        "${state.correct}/${state.total}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        stringResource(R.string.session_correct_label),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // Repaso de examen (solo en modo examen)
        if (state.isExam && state.queue.isNotEmpty()) {
            item {
                Text(
                    stringResource(R.string.session_review_hint),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }

            itemsIndexed(state.queue) { index, cardId ->
                val card = state.cards.firstOrNull { it.id == cardId } ?: return@itemsIndexed
                val userAnswer = state.examAnswers[cardId]
                val isOk = userAnswer == card.correct
                val options = remember(card.optionsJson) {
                    runCatching { Json.decodeFromString<List<String>>(card.optionsJson) }.getOrDefault(
                        emptyList()
                    )
                }
                ExamReviewRow(
                    index = index + 1,
                    question = card.question,
                    isOk = isOk,
                    userAnswer = options.getOrNull(userAnswer ?: -1),
                    correctAnswer = options.getOrNull(card.correct) ?: "",
                    explanation = card.explanation,
                )
            }
        }

        item {
            Button(
                onClick = onExit,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
            ) {
                Text(
                    stringResource(R.string.session_back_to_deck), style = MaterialTheme.typography.labelLarge
                )
            }
        }
    }
}

@Composable
private fun ExamReviewRow(
    index: Int,
    question: String,
    isOk: Boolean,
    userAnswer: String?,
    correctAnswer: String,
    explanation: String,
) {
    var expanded by remember { mutableStateOf(false) }
    val borderColor = if (isOk) Success else Danger
    val bgColor = if (isOk) MaterialTheme.extended.successSoft else MaterialTheme.extended.dangerSoft

    Surface(
        shape = MaterialTheme.shapes.medium,
        border = androidx.compose.foundation.BorderStroke(1.dp, borderColor.copy(alpha = 0.4f)),
        color = bgColor,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column {
            // Cabecera plegable
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                // Número
                Text(
                    "$index",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.widthIn(min = 20.dp)
                )

                Text(
                    question,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.weight(1f),
                    maxLines = if (expanded) Int.MAX_VALUE else 2
                )

                Icon(
                    painter = painterResource(
                        if (expanded) R.drawable.keyboard_arrow_down_24px else R.drawable.keyboard_arrow_right_24px
                    ),
                    contentDescription = null,
                    modifier = Modifier.size(15.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Icon(
                    painter = painterResource(
                        if (isOk) R.drawable.check_24px else R.drawable.close_24px
                    ),
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = if (isOk) Success else Danger,
                )
            }

            // Detalle expandido
            if (expanded) {
                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(0.3f))
                Column(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    if (!isOk && userAnswer != null) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.close_24px),
                                null,
                                modifier = Modifier
                                    .size(13.dp)
                                    .padding(top = 2.dp),
                                tint = Danger
                            )
                            Text(
                                stringResource(R.string.session_you_answered, userAnswer),
                                style = MaterialTheme.typography.bodySmall,
                                color = Danger
                            )
                        }
                    }
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.check_24px),
                            null,
                            modifier = Modifier
                                .size(13.dp)
                                .padding(top = 2.dp),
                            tint = Success
                        )
                        Text(
                            correctAnswer,
                            style = MaterialTheme.typography.bodySmall,
                            color = Success
                        )
                    }
                    if (explanation.isNotBlank()) {
                        Text(
                            explanation,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Toggle expandir
            TextButton(
                onClick = { expanded = !expanded },
                modifier = Modifier.align(Alignment.End),
                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 4.dp),
            ) {
                Text(
                    if (expanded) stringResource(R.string.session_action_close) else stringResource(R.string.session_action_detail),
                    style = MaterialTheme.typography.labelSmall
                )
            }
        }
    }
}
