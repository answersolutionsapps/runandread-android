package com.answersolutions.runandread.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.answersolutions.runandread.R

val title = Font(R.font.shanti_regular, FontWeight.W600)

val Splash = TextStyle(
    fontFamily = FontFamily(fonts = listOf(title)),
    fontWeight = FontWeight.W300,
    fontSize = 38.sp
)

private val light = Font(R.font.raleway_light, FontWeight.W300)
private val regular = Font(R.font.raleway_regular, FontWeight.W400)
private val medium = Font(R.font.raleway_medium, FontWeight.W500)
private val semibold = Font(R.font.raleway_semibold, FontWeight.W600)

private val craneFontFamily = FontFamily(fonts = listOf(light, regular, medium, semibold))


val scTypography = Typography(
    displayLarge = TextStyle(//h1
        fontFamily = craneFontFamily,
        fontWeight = FontWeight.W300,
        fontSize = 57.sp
    ),
    displayMedium = TextStyle(
        fontFamily = craneFontFamily,
        fontWeight = FontWeight.W300,
        fontSize = 45.sp
    ),
    displaySmall = TextStyle(
        fontFamily = craneFontFamily,
        fontWeight = FontWeight.W300,
        fontSize = 36.sp
    ),
    headlineLarge = TextStyle(
        fontFamily = craneFontFamily,
        fontWeight = FontWeight.W400,
        fontSize = 32.sp
    ),
    headlineMedium = TextStyle(//h5
        fontFamily = craneFontFamily,
        fontWeight = FontWeight.W400,
        fontSize = 28.sp
    ),
    headlineSmall = TextStyle(
        fontFamily = craneFontFamily,
        fontWeight = FontWeight.W400,
        fontSize = 24.sp
    ),
    titleLarge = TextStyle(
        fontFamily = craneFontFamily,
        fontWeight = FontWeight.W500,
        fontSize = 22.sp
    ),
    titleMedium = TextStyle(
        fontFamily = craneFontFamily,
        fontWeight = FontWeight.W500,
        fontSize = 16.sp
    ),
    titleSmall = TextStyle(
        fontFamily = craneFontFamily,
        fontWeight = FontWeight.W500,
        fontSize = 14.sp
    ),
    bodyLarge = TextStyle(
        fontFamily = craneFontFamily,
        fontWeight = FontWeight.W400,
        fontSize = 16.sp
    ),
    bodyMedium = TextStyle(//body1
        fontFamily = craneFontFamily,
        fontWeight = FontWeight.W400,
        fontSize = 14.sp
    ),
    bodySmall = TextStyle(//body2
        fontFamily = craneFontFamily,
        fontWeight = FontWeight.W400,
        fontSize = 12.sp
    ),
    labelLarge = TextStyle(//button
        fontFamily = craneFontFamily,
        fontWeight = FontWeight.W500,
        fontSize = 14.sp
    ),
    labelMedium = TextStyle(//caption
        fontFamily = craneFontFamily,
        fontWeight = FontWeight.W500,
        fontSize = 12.sp
    ),
    labelSmall = TextStyle(//overline
        fontFamily = craneFontFamily,
        fontWeight = FontWeight.W500,
        fontSize = 11.sp
    )
)

val xSmallSpace = 3.dp
val noSpace = 0.dp
val cardSpace = 4.dp
val smallSpace = 8.dp
val normalSpace = 16.dp
val largeSpace = 22.dp
val doubleLargeSpace = 44.dp
val noRadius = 0.dp
val normalRadius = 8.dp
val cornerRadiusBig = 20.dp
val normalElevation = 8.dp
val doubledElevation = 16.dp
val noElevation = 0.dp

val footerHeight = 74.dp

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFFD6D6D6),
    secondary = Color(0xFF2E2E2E),
    tertiary = Color(0xFFB0B0B0),
    surface = Color(0xFF252525)
)

private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF2E2E2E),
    secondary = Color(0xFFD6D6D6),
    tertiary = Color(0xFF4A4A4A),
    surface = Color(0xFFF5F5F5)
)

@Composable
fun RunAndReadTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val colors = if (darkTheme) {
        DarkColorScheme
    } else {
        LightColorScheme
    }

    MaterialTheme(
        colorScheme = colors,
        typography = scTypography,
        content = content
    )
}