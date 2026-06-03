package dev.r4g309.kokoromed.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import dev.r4g309.kokoromed.R
import dev.r4g309.kokoromed.ui.theme.AmberSoft
import dev.r4g309.kokoromed.ui.theme.AmberText
import dev.r4g309.kokoromed.ui.theme.Danger
import dev.r4g309.kokoromed.ui.theme.DangerSoft
import dev.r4g309.kokoromed.ui.theme.Success
import dev.r4g309.kokoromed.ui.theme.SuccessSoft

// ── Anillo de progreso ────────────────────────────────────────────────────────

@Composable
fun ProgressRing(
    value: Int,
    modifier: Modifier = Modifier,
    size: Dp = 50.dp,
    strokeWidth: Dp = 5.dp,
    color: Color = MaterialTheme.colorScheme.primary,
) {
    val trackColor = MaterialTheme.colorScheme.outline
    val pct = value.coerceIn(0, 100) / 100f
    Canvas(modifier = modifier.size(size)) {
        val sw = strokeWidth.toPx()
        val style = Stroke(width = sw, cap = StrokeCap.Round)
        drawArc(color = trackColor, startAngle = 0f, sweepAngle = 360f,
            useCenter = false, style = style)
        if (pct > 0f) {
            drawArc(color = color, startAngle = -90f, sweepAngle = 360f * pct,
                useCenter = false, style = style)
        }
    }
}

// ── Badge de dificultad ───────────────────────────────────────────────────────

@Composable
fun DifficultyBadge(difficulty: String) {
    val (label, bg, fg) = when (difficulty.lowercase()) {
        "easy"  -> Triple(stringResource(R.string.difficulty_easy),   SuccessSoft, Success)
        "hard"  -> Triple(stringResource(R.string.difficulty_hard),   DangerSoft,  Danger)
        else    -> Triple(stringResource(R.string.difficulty_medium),  AmberSoft,   AmberText)
    }
    Surface(
        shape = MaterialTheme.shapes.extraSmall,
        color = bg,
    ) {
        Text(
            text  = label,
            style = MaterialTheme.typography.labelSmall,
            color = fg,
            modifier = Modifier.padding(horizontal = 9.dp, vertical = 3.dp),
        )
    }
}

// ── Chip de estado de tarjeta ─────────────────────────────────────────────────

@Composable
fun StateChip(label: String, bg: Color, fg: Color) {
    Surface(shape = MaterialTheme.shapes.extraSmall, color = bg) {
        Text(
            text  = label,
            style = MaterialTheme.typography.labelSmall,
            color = fg,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
        )
    }
}
