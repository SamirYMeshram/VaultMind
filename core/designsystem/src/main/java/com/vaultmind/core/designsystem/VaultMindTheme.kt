package com.vaultmind.core.designsystem

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.vaultmind.core.model.ThemeMode

private val VaultLightColors = lightColorScheme(
    primary = Color(0xFF4257FF),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFE1E5FF),
    onPrimaryContainer = Color(0xFF07124D),
    secondary = Color(0xFF7257FF),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFE9DDFF),
    onSecondaryContainer = Color(0xFF241143),
    tertiary = Color(0xFF006D77),
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFB7EEF4),
    onTertiaryContainer = Color(0xFF001F23),
    error = Color(0xFFBA1A1A),
    errorContainer = Color(0xFFFFDAD6),
    background = Color(0xFFFBF9FF),
    onBackground = Color(0xFF191A24),
    surface = Color(0xFFFBF9FF),
    onSurface = Color(0xFF191A24),
    surfaceVariant = Color(0xFFE4E1EC),
    onSurfaceVariant = Color(0xFF464552),
    outline = Color(0xFF777584)
)

private val VaultDarkColors = darkColorScheme(
    primary = Color(0xFFBAC3FF),
    onPrimary = Color(0xFF071B9A),
    primaryContainer = Color(0xFF2237D9),
    onPrimaryContainer = Color(0xFFE1E5FF),
    secondary = Color(0xFFD2BCFF),
    onSecondary = Color(0xFF3A2171),
    secondaryContainer = Color(0xFF573E9B),
    onSecondaryContainer = Color(0xFFE9DDFF),
    tertiary = Color(0xFF8ED3DA),
    onTertiary = Color(0xFF00363B),
    tertiaryContainer = Color(0xFF005158),
    onTertiaryContainer = Color(0xFFB7EEF4),
    error = Color(0xFFFFB4AB),
    errorContainer = Color(0xFF93000A),
    background = Color(0xFF10111A),
    onBackground = Color(0xFFE5E1EC),
    surface = Color(0xFF10111A),
    onSurface = Color(0xFFE5E1EC),
    surfaceVariant = Color(0xFF464552),
    onSurfaceVariant = Color(0xFFC8C5D0),
    outline = Color(0xFF918F9A)
)

private val VaultTypography = Typography(
    displaySmall = TextStyle(fontFamily = FontFamily.Default, fontWeight = FontWeight.ExtraBold, fontSize = 36.sp, lineHeight = 42.sp),
    headlineLarge = TextStyle(fontFamily = FontFamily.Default, fontWeight = FontWeight.ExtraBold, fontSize = 32.sp, lineHeight = 38.sp),
    headlineMedium = TextStyle(fontFamily = FontFamily.Default, fontWeight = FontWeight.ExtraBold, fontSize = 28.sp, lineHeight = 34.sp),
    headlineSmall = TextStyle(fontFamily = FontFamily.Default, fontWeight = FontWeight.Bold, fontSize = 24.sp, lineHeight = 30.sp),
    titleLarge = TextStyle(fontFamily = FontFamily.Default, fontWeight = FontWeight.Bold, fontSize = 20.sp, lineHeight = 26.sp),
    titleMedium = TextStyle(fontFamily = FontFamily.Default, fontWeight = FontWeight.SemiBold, fontSize = 16.sp, lineHeight = 22.sp),
    bodyLarge = TextStyle(fontFamily = FontFamily.Default, fontWeight = FontWeight.Normal, fontSize = 16.sp, lineHeight = 24.sp),
    bodyMedium = TextStyle(fontFamily = FontFamily.Default, fontWeight = FontWeight.Normal, fontSize = 14.sp, lineHeight = 21.sp),
    labelLarge = TextStyle(fontFamily = FontFamily.Default, fontWeight = FontWeight.SemiBold, fontSize = 14.sp, lineHeight = 18.sp),
    labelMedium = TextStyle(fontFamily = FontFamily.Default, fontWeight = FontWeight.Medium, fontSize = 12.sp, lineHeight = 16.sp),
    labelSmall = TextStyle(fontFamily = FontFamily.Default, fontWeight = FontWeight.Medium, fontSize = 11.sp, lineHeight = 14.sp)
)

private val VaultShapes = Shapes()

@Composable
fun VaultMindTheme(
    themeMode: ThemeMode,
    dynamicColor: Boolean,
    content: @Composable () -> Unit
) {
    val dark = when (themeMode) {
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
    }

    val colors: ColorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            if (dark) dynamicDarkColorScheme(LocalContext.current) else dynamicLightColorScheme(LocalContext.current)
        }
        dark -> VaultDarkColors
        else -> VaultLightColors
    }

    MaterialTheme(
        colorScheme = colors,
        typography = VaultTypography,
        shapes = VaultShapes,
        content = content
    )
}
