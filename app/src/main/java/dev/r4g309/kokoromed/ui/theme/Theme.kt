package dev.r4g309.kokoromed.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf

// ── Color schemes ─────────────────────────────────────────────────────────────

private val LightColorScheme = lightColorScheme(
    primary              = Teal500,
    onPrimary            = NeutralSurface,
    primaryContainer     = TealSoft,
    onPrimaryContainer   = Teal700,
    secondary            = Teal600,
    onSecondary          = NeutralSurface,
    secondaryContainer   = TealTint,
    onSecondaryContainer = Teal700,
    background           = NeutralBg,
    onBackground         = NeutralText,
    surface              = NeutralSurface,
    onSurface            = NeutralText,
    surfaceVariant       = NeutralSurface2,
    onSurfaceVariant     = NeutralText2,
    outline              = NeutralBorder,
    outlineVariant       = NeutralBorderStrong,
    error                = Danger,
    onError              = NeutralSurface,
    errorContainer       = DangerSoft,
    onErrorContainer     = Danger,
)

private val DarkColorScheme = darkColorScheme(
    primary              = TealDark,
    onPrimary            = DarkBg,
    primaryContainer     = TealDarkSoft,
    onPrimaryContainer   = TealDark,
    secondary            = Teal600,
    onSecondary          = DarkBg,
    secondaryContainer   = TealDarkTint,
    onSecondaryContainer = TealDark,
    background           = DarkBg,
    onBackground         = DarkText,
    surface              = DarkSurface,
    onSurface            = DarkText,
    surfaceVariant       = DarkSurface2,
    onSurfaceVariant     = DarkText2,
    outline              = DarkBorder,
    outlineVariant       = DarkBorderStrong,
    error                = Danger,
    onError              = NeutralSurface,
    errorContainer       = DangerSoftDark,
    onErrorContainer     = Danger,
)

// ── Extended colors (semánticos + surface-3) ──────────────────────────────────

@Immutable
data class ExtendedColors(
    val surface3: androidx.compose.ui.graphics.Color,
    val textSubtle: androidx.compose.ui.graphics.Color,  // text-3
    val success: androidx.compose.ui.graphics.Color,
    val successSoft: androidx.compose.ui.graphics.Color,
    val danger: androidx.compose.ui.graphics.Color,
    val dangerSoft: androidx.compose.ui.graphics.Color,
    val amber: androidx.compose.ui.graphics.Color,
    val amberText: androidx.compose.ui.graphics.Color,
    val amberSoft: androidx.compose.ui.graphics.Color,
)

val LocalExtendedColors = staticCompositionLocalOf {
    ExtendedColors(
        surface3     = NeutralSurface3,
        textSubtle   = NeutralText3,
        success      = Success,
        successSoft  = SuccessSoft,
        danger       = Danger,
        dangerSoft   = DangerSoft,
        amber        = Amber,
        amberText    = AmberText,
        amberSoft    = AmberSoft,
    )
}

val LightExtended = ExtendedColors(
    surface3     = NeutralSurface3,
    textSubtle   = NeutralText3,
    success      = Success,
    successSoft  = SuccessSoft,
    danger       = Danger,
    dangerSoft   = DangerSoft,
    amber        = Amber,
    amberText    = AmberText,
    amberSoft    = AmberSoft,
)

val DarkExtended = ExtendedColors(
    surface3     = DarkSurface3,
    textSubtle   = DarkText3,
    success      = Success,
    successSoft  = SuccessSoftDark,
    danger       = Danger,
    dangerSoft   = DangerSoftDark,
    amber        = Amber,
    amberText    = Amber,
    amberSoft    = AmberSoftDark,
)

// ── Theme entry point ─────────────────────────────────────────────────────────

@Composable
fun KokoroMedTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val colorScheme  = if (darkTheme) DarkColorScheme  else LightColorScheme
    val extendedColors = if (darkTheme) DarkExtended   else LightExtended

    CompositionLocalProvider(LocalExtendedColors provides extendedColors) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography  = Typography,
            shapes      = KokoroShapes,
            content     = content,
        )
    }
}

// ── Convenience accessor ──────────────────────────────────────────────────────
val MaterialTheme.extended: ExtendedColors
    @Composable get() = LocalExtendedColors.current
