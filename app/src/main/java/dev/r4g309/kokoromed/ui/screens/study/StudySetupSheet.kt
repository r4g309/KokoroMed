package dev.r4g309.kokoromed.ui.screens.study

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import dev.r4g309.kokoromed.R
import dev.r4g309.kokoromed.domain.srs.DeckProgress
import dev.r4g309.kokoromed.domain.srs.StudyMode
import dev.r4g309.kokoromed.ui.theme.Danger
import dev.r4g309.kokoromed.ui.theme.DangerSoft
import dev.r4g309.kokoromed.ui.theme.Success
import dev.r4g309.kokoromed.ui.theme.SuccessSoft

private data class ModeOption(
    val mode: StudyMode,
    @StringRes val labelRes: Int,
    @StringRes val descriptionRes: Int,
    @DrawableRes val icon: Int,
    val bg: Color,
    val fg: Color,
    val count: (DeckProgress) -> Int,
)

private val modeOptions = listOf(
    ModeOption(
        StudyMode.due, R.string.setup_mode_due_label, R.string.setup_mode_due_desc,
        R.drawable.calendar_today_24px, Color(0xFFFEF3C7), Color(0xFFB45309)
    ) { it.due },
    ModeOption(
        StudyMode.new, R.string.setup_mode_new_label, R.string.setup_mode_new_desc,
        R.drawable.star_shine_24px, Color(0xFFEFF6FF), Color(0xFF1D4ED8)
    ) { it.new },
    ModeOption(
        StudyMode.failed, R.string.setup_mode_failed_label, R.string.setup_mode_failed_desc,
        R.drawable.local_fire_department_24px, DangerSoft, Danger
    ) { it.failed },
    ModeOption(
        StudyMode.exam, R.string.setup_mode_exam_label, R.string.setup_mode_exam_desc,
        R.drawable.layers_24px, SuccessSoft, Success
    ) { it.total },
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudySetupSheet(
    deckName: String,
    progress: DeckProgress,
    onDismiss: () -> Unit,
    onStart: (StudyMode, maxQuestions: Int?) -> Unit,
) {
    var unlimited by remember { mutableStateOf(true) }
    var inputText by remember { mutableStateOf("") }
    val maxQuestions: Int? = if (unlimited) null else inputText.toIntOrNull()?.takeIf { it > 0 }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        dragHandle = { BottomSheetDefaults.DragHandle() },
    ) {
        LazyColumn(
            contentPadding = PaddingValues(start = 20.dp, end = 20.dp, bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item {
                Text(
                    stringResource(R.string.setup_title, deckName),
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(bottom = 4.dp),
                )
            }

            // ── Límite de preguntas ───────────────────────────────────────────
            item {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Column {
                            Text(
                                stringResource(R.string.setup_max_questions_title),
                                style = MaterialTheme.typography.labelLarge,
                            )
                            Text(
                                if (unlimited) stringResource(R.string.setup_max_questions_unlimited)
                                else stringResource(R.string.setup_max_questions_limited),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                        Switch(
                            checked         = !unlimited,
                            onCheckedChange = { unlimited = !it },
                        )
                    }
                    if (!unlimited) {
                        OutlinedTextField(
                            value         = inputText,
                            onValueChange = { v ->
                                // Solo dígitos, máximo 4 caracteres
                                if (v.all { it.isDigit() } && v.length <= 4) inputText = v
                            },
                            modifier      = Modifier.fillMaxWidth(),
                            label         = { Text(stringResource(R.string.setup_questions_label)) },
                            placeholder   = { Text(stringResource(R.string.setup_questions_placeholder)) },
                            singleLine    = true,
                            shape         = MaterialTheme.shapes.medium,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            isError       = inputText.isNotEmpty() && maxQuestions == null,
                            supportingText = {
                                if (inputText.isNotEmpty() && maxQuestions == null)
                                    Text(stringResource(R.string.setup_questions_error))
                            },
                        )
                    }
                }
            }

            // ── Modos de estudio ──────────────────────────────────────────────
            val inputValid = unlimited || maxQuestions != null
            items(modeOptions) { opt ->
                StudyModeCard(
                    labelRes       = opt.labelRes,
                    descriptionRes = opt.descriptionRes,
                    icon           = opt.icon,
                    bg          = opt.bg,
                    fg          = opt.fg,
                    count       = opt.count(progress),
                    enabled     = inputValid,
                    onClick     = { onStart(opt.mode, maxQuestions) },
                )
            }
        }
    }
}

@Composable
private fun StudyModeCard(
    @StringRes labelRes: Int,
    @StringRes descriptionRes: Int,
    @DrawableRes icon: Int,
    bg: Color,
    fg: Color,
    count: Int,
    enabled: Boolean = true,
    onClick: () -> Unit,
) {
    Surface(
        onClick  = onClick,
        enabled  = enabled,
        shape    = MaterialTheme.shapes.large,
        color   = MaterialTheme.colorScheme.surface,
        border  = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Surface(shape = CircleShape, color = bg, modifier = Modifier.size(44.dp)) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(painterResource(icon), null, tint = fg, modifier = Modifier.size(22.dp))
                }
            }
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(stringResource(labelRes), style = MaterialTheme.typography.titleSmall)
                Text(stringResource(descriptionRes),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Surface(shape = CircleShape, color = bg) {
                Text("$count",
                    style = MaterialTheme.typography.labelMedium,
                    color = fg,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                    textAlign = TextAlign.Center)
            }
        }
    }
}
