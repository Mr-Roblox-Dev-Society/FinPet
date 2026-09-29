package ru.finny.pet.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

val FinnySky = Color(0xFF4EC5F1)
val FinnySun = Color(0xFFFFC857)
val FinnyGrass = Color(0xFF7BD389)
val FinnyCoral = Color(0xFFFF6B6B)
val FinnyInk = Color(0xFF1F2A37)
val FinnyCloud = Color(0xFFFFF8E7)
val FinnyDeep = Color(0xFF0E7490)

private val LightColors = lightColorScheme(
    primary = FinnyDeep,
    onPrimary = Color.White,
    secondary = FinnySun,
    onSecondary = FinnyInk,
    tertiary = FinnyCoral,
    background = FinnyCloud,
    onBackground = FinnyInk,
    surface = Color.White,
    onSurface = FinnyInk,
    error = FinnyCoral
)

val FinnyTypography = Typography(
    displayLarge = TextStyle(
        fontFamily = FontFamily.Serif,
        fontWeight = FontWeight.Bold,
        fontSize = 36.sp,
        lineHeight = 42.sp
    ),
    headlineMedium = TextStyle(
        fontFamily = FontFamily.Serif,
        fontWeight = FontWeight.Bold,
        fontSize = 24.sp,
        lineHeight = 30.sp
    ),
    titleLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.SemiBold,
        fontSize = 20.sp
    ),
    bodyLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 22.sp
    ),
    bodyMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontSize = 16.sp
    ),
    labelLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.SemiBold,
        fontSize = 16.sp
    )
)

@Composable
fun FinnyTheme(content: @Composable () -> Unit) {

    MaterialTheme(
        colorScheme = LightColors,
        typography = FinnyTypography,
        content = content
    )
}
