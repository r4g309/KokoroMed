package dev.r4g309.kokoromed.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import dev.r4g309.kokoromed.R

// Coloca los .ttf/.otf de Hanken Grotesk en res/font/ con estos nombres:
//   hanken_grotesk_regular.ttf   (400)
//   hanken_grotesk_medium.ttf    (500)
//   hanken_grotesk_semibold.ttf  (600)
//   hanken_grotesk_bold.ttf      (700)
//   hanken_grotesk_extrabold.ttf (800)
val HankenGrotesk = FontFamily(
    Font(R.font.hanken_grotesk_regular,   FontWeight.Normal),
    Font(R.font.hanken_grotesk_medium,    FontWeight.Medium),
    Font(R.font.hanken_grotesk_semibold,  FontWeight.SemiBold),
    Font(R.font.hanken_grotesk_bold,      FontWeight.Bold),
    Font(R.font.hanken_grotesk_extrabold, FontWeight.ExtraBold),
)

val Typography = Typography(
    displayLarge = TextStyle(
        fontFamily   = HankenGrotesk,
        fontWeight   = FontWeight.ExtraBold,
        fontSize     = 36.sp,
        lineHeight   = 40.sp,
        letterSpacing = (-0.72).sp,
    ),
    headlineLarge = TextStyle(
        fontFamily   = HankenGrotesk,
        fontWeight   = FontWeight.ExtraBold,
        fontSize     = 28.sp,
        lineHeight   = 34.sp,
        letterSpacing = (-0.56).sp,
    ),
    headlineMedium = TextStyle(
        fontFamily   = HankenGrotesk,
        fontWeight   = FontWeight.ExtraBold,
        fontSize     = 22.sp,
        lineHeight   = 28.sp,
        letterSpacing = (-0.44).sp,
    ),
    titleLarge = TextStyle(
        fontFamily   = HankenGrotesk,
        fontWeight   = FontWeight.Bold,
        fontSize     = 20.sp,
        lineHeight   = 26.sp,
        letterSpacing = (-0.4).sp,
    ),
    titleMedium = TextStyle(
        fontFamily   = HankenGrotesk,
        fontWeight   = FontWeight.Bold,
        fontSize     = 17.sp,
        lineHeight   = 22.sp,
        letterSpacing = (-0.34).sp,
    ),
    titleSmall = TextStyle(
        fontFamily   = HankenGrotesk,
        fontWeight   = FontWeight.SemiBold,
        fontSize     = 15.sp,
        lineHeight   = 20.sp,
        letterSpacing = (-0.3).sp,
    ),
    bodyLarge = TextStyle(
        fontFamily   = HankenGrotesk,
        fontWeight   = FontWeight.Normal,
        fontSize     = 16.sp,
        lineHeight   = 24.sp,
        letterSpacing = 0.sp,
    ),
    bodyMedium = TextStyle(
        fontFamily   = HankenGrotesk,
        fontWeight   = FontWeight.Normal,
        fontSize     = 14.sp,
        lineHeight   = 20.sp,
        letterSpacing = 0.sp,
    ),
    bodySmall = TextStyle(
        fontFamily   = HankenGrotesk,
        fontWeight   = FontWeight.Normal,
        fontSize     = 13.sp,
        lineHeight   = 18.sp,
        letterSpacing = 0.sp,
    ),
    labelLarge = TextStyle(
        fontFamily   = HankenGrotesk,
        fontWeight   = FontWeight.Bold,
        fontSize     = 15.sp,
        lineHeight   = 20.sp,
        letterSpacing = 0.sp,
    ),
    labelMedium = TextStyle(
        fontFamily   = HankenGrotesk,
        fontWeight   = FontWeight.Bold,
        fontSize     = 13.sp,
        lineHeight   = 16.sp,
        letterSpacing = 0.sp,
    ),
    labelSmall = TextStyle(
        fontFamily   = HankenGrotesk,
        fontWeight   = FontWeight.SemiBold,
        fontSize     = 12.sp,
        lineHeight   = 16.sp,
        letterSpacing = 0.sp,
    ),
)
