package dev.r4g309.kokoromed.ui.screens.study

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.annotation.StringRes
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.r4g309.kokoromed.R
import dev.r4g309.kokoromed.domain.model.Rating
import kotlinx.coroutines.delay
import dev.r4g309.kokoromed.ui.components.ProgressRing
import dev.r4g309.kokoromed.ui.theme.Danger
import dev.r4g309.kokoromed.ui.theme.Success
import dev.r4g309.kokoromed.ui.theme.extended
import kotlinx.serialization.json.Json

private val ratingOptions = listOf(
    Triple(Rating.again, R.string.study_rating_again, Danger),
    Triple(Rating.hard,  R.string.study_rating_hard,  Color(0xFFD97706)),
    Triple(Rating.good,  R.string.study_rating_good,  Color(0xFF0D9488)),
    Triple(Rating.easy,  R.string.study_rating_easy,  Success),
)

@Composable
fun StudySessionScreen(
    onExit: () -> Unit,
    timer: Boolean = false,
    vm: StudyViewModel = hiltViewModel(),
) {
    val s by vm.state.collectAsStateWithLifecycle()
    var showExitDialog by remember { mutableStateOf(false) }

    // Intercepta el botón atrás del sistema
    androidx.activity.compose.BackHandler { showExitDialog = true }

    if (s.loading) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    if (s.done) {
        SessionCompleteScreen(state = s, onExit = onExit)
        return
    }

    val card = s.currentCard
    if (card == null) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    stringResource(R.string.study_no_cards), style = MaterialTheme.typography.bodyLarge
                )
                Button(onClick = onExit) { Text(stringResource(R.string.study_action_back)) }
            }
        }
        return
    }

    val options = remember(card.optionsJson) {
        runCatching { Json.decodeFromString<List<String>>(card.optionsJson) }.getOrDefault(emptyList())
    }
    val accentColor = MaterialTheme.colorScheme.primary

    // ── Temporizador ──────────────────────────────────────────────────────────
    var secs by remember(s.pos) { mutableIntStateOf(0) }
    if (timer) {
        LaunchedEffect(s.pos) {
            while (true) {
                delay(1_000); secs++
            }
        }
    }

    // ── Animación de feedback (pulse verde / shake rojo) ──────────────────────
    var shakeOffset by remember { mutableFloatStateOf(0f) }
    val animatedOffset by animateFloatAsState(
        targetValue = shakeOffset,
        animationSpec = spring(stiffness = Spring.StiffnessHigh),
        finishedListener = { shakeOffset = 0f },
        label = "shake",
    )
    var pulseAlpha by remember { mutableFloatStateOf(0f) }
    val animatedPulse by animateFloatAsState(
        targetValue = pulseAlpha,
        animationSpec = tween(600),
        finishedListener = { pulseAlpha = 0f },
        label = "pulse",
    )

    // Dispara animación cuando se responde
    LaunchedEffect(s.answered) {
        if (!s.answered || s.isExam) return@LaunchedEffect
        val isCorrect = s.selected == s.currentCard?.correct
        if (isCorrect) {
            pulseAlpha = 0.25f
        } else {
            shakeOffset = 12f
            delay(60); shakeOffset = -10f
            delay(60); shakeOffset = 8f
            delay(60); shakeOffset = -6f
            delay(60); shakeOffset = 0f
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .navigationBarsPadding(),
    ) {
        // ── Barra superior con progreso ───────────────────────────────────────
        StudyTopBar(
            pos = s.pos,
            total = s.queue.size,
            baseLen = s.baseLen,
            progress = s.progress,
            accentColor = accentColor,
            timerSecs = if (timer) secs else null,
            onExit = { showExitDialog = true },
        )

        // ── Tarjeta de pregunta ───────────────────────────────────────────────
        Box(modifier = Modifier.weight(1f)) {
            AnimatedContent(
                targetState = s.pos,
                transitionSpec = {
                    (slideInHorizontally(tween(220)) { it / 2 } + fadeIn(tween(220))) togetherWith (slideOutHorizontally(
                        tween(180)
                    ) { -it / 2 } + fadeOut(tween(180)))
                },
                modifier = Modifier
                    .fillMaxSize()
                    .offset(x = animatedOffset.dp),
                label = "cardTransition",
            ) { _ ->
                val scrollState = rememberScrollState()
                val isWrongAnswer = s.answered && !s.isExam && s.selected != card?.correct

                LaunchedEffect(s.answered) {
                    if (isWrongAnswer) scrollState.animateScrollTo(scrollState.maxValue)
                }

                Column(
                    modifier = Modifier
                        .padding(horizontal = 16.dp, vertical = 20.dp)
                        .verticalScroll(scrollState),
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                ) {
                    // Chip de mazo + badge de dificultad
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = CircleShape, color = accentColor
                        ) {
                            Text(
                                text = card.difficulty.replaceFirstChar { it.uppercase() },
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.White,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                            )
                        }
                        if (s.isExam) {
                            Surface(
                                shape = CircleShape,
                                color = MaterialTheme.colorScheme.surfaceVariant
                            ) {
                                Text(
                                    stringResource(R.string.study_chip_exam),
                                    style = MaterialTheme.typography.labelSmall,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }

                    // Pregunta
                    Text(
                        text = card.question,
                        style = MaterialTheme.typography.titleLarge,
                    )

                    // Opciones
                    val letters = "ABCD"
                    options.forEachIndexed { i, option ->
                        val isSelected = s.selected == i
                        val isCorrect = i == card.correct
                        val isWrong = s.answered && !s.isExam && isSelected && !isCorrect

                        val borderColor = when {
                            s.answered && !s.isExam && isCorrect -> Success
                            isWrong -> Danger
                            isSelected && !s.answered -> accentColor
                            else -> MaterialTheme.colorScheme.outline
                        }
                        val bgColor = when {
                            s.answered && !s.isExam && isCorrect -> MaterialTheme.extended.successSoft
                            isWrong -> MaterialTheme.extended.dangerSoft
                            else -> MaterialTheme.colorScheme.surface
                        }
                        val textAlpha =
                            if (s.answered && !s.isExam && !isCorrect && !isSelected) 0.4f else 1f

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(MaterialTheme.shapes.medium)
                                .background(bgColor)
                                .border(1.5.dp, borderColor, MaterialTheme.shapes.medium)
                                .clickable(enabled = !s.answered) { vm.selectOption(i) }
                                .padding(horizontal = 16.dp, vertical = 15.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(13.dp),
                        ) {
                            // Key indicator (A/B/C/D)
                            val keyBg = if (isSelected && !s.answered) accentColor
                            else MaterialTheme.colorScheme.surfaceVariant
                            val keyFg = if (isSelected && !s.answered) Color.White
                            else MaterialTheme.colorScheme.onSurfaceVariant
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier
                                    .size(30.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(keyBg),
                            ) {
                                Text(
                                    letters[i].toString(),
                                    style = MaterialTheme.typography.labelMedium,
                                    color = keyFg
                                )
                            }

                            Text(
                                text = option,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = textAlpha),
                                modifier = Modifier.weight(1f),
                            )

                            // Icono check/x tras responder
                            if (s.answered && !s.isExam) {
                                when {
                                    isCorrect -> Icon(
                                        painter = painterResource(R.drawable.check_24px),
                                        null,
                                        modifier = Modifier.size(20.dp),
                                        tint = Success
                                    )

                                    isSelected -> Icon(
                                        painter = painterResource(R.drawable.close_24px),
                                        null,
                                        modifier = Modifier.size(20.dp),
                                        tint = Danger
                                    )
                                }
                            }
                        }
                    }

                    // Feedback + explicación (solo en modo estudio)
                    if (s.answered && !s.isExam) {
                        val isCorrect = s.selected == card.correct
                        FeedbackBox(
                            isCorrect = isCorrect,
                            explanation = card.explanation,
                            showExplanation = s.showExplanation,
                            onToggle = { vm.toggleExplanation() },
                        )
                    }
                }
            }
            // Overlay de pulse verde (respuesta correcta)
            if (animatedPulse > 0f) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Success.copy(alpha = animatedPulse))
                )
            }
        } // cierre Box

        // ── Acciones inferiores ───────────────────────────────────────────────
        Surface(
            shadowElevation = 8.dp,
            color = MaterialTheme.colorScheme.surface,
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 14.dp),
            ) {
                when {
                    !s.answered -> Button(
                        onClick = { vm.confirm() },
                        enabled = s.selected != null,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                    ) {
                        Text(
                            stringResource(R.string.study_action_confirm),
                            style = MaterialTheme.typography.labelLarge,
                        )
                    }

                    else -> RatingBar(onRate = { vm.rate(it) })
                }
            }
        }
    }

    // ── Diálogo de confirmación al salir ──────────────────────────────────────
    if (showExitDialog) {
        // En modo estudio cada calificación se guarda al instante; en examen
        // no se guarda nada hasta terminar.
        val answeredCount = if (s.isExam) s.examAnswers.size else s.total
        AlertDialog(
            onDismissRequest = { showExitDialog = false },
            title = { Text(stringResource(R.string.study_exit_title)) },
            text = {
                Text(
                    if (s.isExam) {
                        if (answeredCount > 0)
                            stringResource(R.string.study_exit_exam_with_answers, answeredCount)
                        else
                            stringResource(R.string.study_exit_exam_no_answers)
                    } else {
                        if (answeredCount > 0)
                            stringResource(R.string.study_exit_study_with_answers, answeredCount)
                        else
                            stringResource(R.string.study_exit_study_no_answers)
                    }
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    showExitDialog = false
                    onExit()
                }) { Text(stringResource(R.string.study_action_exit), color = Danger) }
            },
            dismissButton = {
                TextButton(onClick = { showExitDialog = false }) { Text(stringResource(R.string.study_action_keep_studying)) }
            },
        )
    }
}

// ── Barra de progreso superior ────────────────────────────────────────────────

@Composable
private fun StudyTopBar(
    pos: Int, total: Int, baseLen: Int,
    progress: Float, accentColor: Color,
    timerSecs: Int? = null,
    onExit: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        IconButton(onClick = onExit, modifier = Modifier.size(40.dp)) {
            Icon(
                painter = painterResource(R.drawable.close_24px),
                contentDescription = stringResource(R.string.study_action_exit),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // Barra de progreso
        Box(
            modifier = Modifier
                .weight(1f)
                .height(6.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surfaceVariant),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(progress)
                    .clip(CircleShape)
                    .background(accentColor),
            )
        }

        // Contador
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(3.dp)
        ) {
            Text(
                "${pos + 1}/$total",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            if (total > baseLen) {
                Surface(
                    shape = CircleShape, color = MaterialTheme.colorScheme.primaryContainer
                ) {
                    Text(
                        "+${total - baseLen}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }
        }
        // Temporizador
        if (timerSecs != null) {
            val m = timerSecs / 60
            val sec = timerSecs % 60
            Text(
                "%02d:%02d".format(m, sec),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

// ── Caja de feedback con explicación ─────────────────────────────────────────

@Composable
private fun FeedbackBox(
    isCorrect: Boolean,
    explanation: String,
    showExplanation: Boolean,
    onToggle: () -> Unit,
) {
    val bg = if (isCorrect) MaterialTheme.extended.successSoft else MaterialTheme.extended.dangerSoft
    val fg = if (isCorrect) Success else Danger

    Surface(
        shape = MaterialTheme.shapes.medium,
        color = bg,
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    painter = painterResource(
                        if (isCorrect) R.drawable.check_24px else R.drawable.close_24px
                    ), contentDescription = null, tint = fg, modifier = Modifier.size(18.dp)
                )
                Text(
                    if (isCorrect) stringResource(R.string.study_feedback_correct) else stringResource(R.string.study_feedback_incorrect),
                    style = MaterialTheme.typography.titleSmall,
                    color = fg,
                    modifier = Modifier.weight(1f),
                )
                TextButton(
                    onClick = onToggle,
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp),
                ) {
                    Text(
                        if (showExplanation) stringResource(R.string.study_explanation_hide) else stringResource(R.string.study_explanation_show),
                        style = MaterialTheme.typography.labelSmall,
                        color = fg,
                    )
                }
            }
            if (showExplanation && explanation.isNotBlank()) {
                Text(
                    explanation,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}

// ── Barra de calificación ─────────────────────────────────────────────────────

@Composable
private fun RatingBar(onRate: (Rating) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            stringResource(R.string.study_rating_prompt),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            ratingOptions.forEach { (rating, labelRes, color) ->
                OutlinedButton(
                    onClick = { onRate(rating) },
                    modifier = Modifier
                        .weight(1f)
                        .height(52.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = color,
                    ),
                    border = ButtonDefaults.outlinedButtonBorder(enabled = true).let {
                        androidx.compose.foundation.BorderStroke(1.dp, color.copy(alpha = 0.4f))
                    },
                ) {
                    Text(
                        stringResource(labelRes),
                        style = MaterialTheme.typography.labelSmall,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}
