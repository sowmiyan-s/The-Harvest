package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = HarvestMintLight,
    onPrimary = HarvestEmeraldDark,
    primaryContainer = HarvestEmeraldDark,
    onPrimaryContainer = HarvestMintLight,
    secondary = HarvestGoldLight,
    onSecondary = Color.Black,
    secondaryContainer = Color(0xFF452200),
    onSecondaryContainer = HarvestGoldLight,
    tertiary = HarvestSkyLight,
    onTertiary = Color.Black,
    background = HarvestBgDark,
    onBackground = HarvestTextLight,
    surface = HarvestSurfaceDark,
    onSurface = HarvestTextLight,
    surfaceVariant = HarvestSurfaceVariantDark,
    onSurfaceVariant = Color(0xFFCBD5E1),
    outline = HarvestOutlineDark
)

private val LightColorScheme = lightColorScheme(
    primary = HarvestEmeraldPrimary,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFDCFCE7),
    onPrimaryContainer = HarvestEmeraldDark,
    secondary = HarvestGoldAccent,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFFEF3C7),
    onSecondaryContainer = Color(0xFF78350F),
    tertiary = HarvestSkyBlue,
    onTertiary = Color.White,
    background = HarvestBgLight,
    onBackground = HarvestTextDark,
    surface = HarvestSurfaceLight,
    onSurface = HarvestTextDark,
    surfaceVariant = HarvestSurfaceVariantLight,
    onSurfaceVariant = HarvestTextMuted,
    outline = HarvestOutlineLight
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Keep brand aesthetic consistent by default
    content: @Composable () -> Unit,
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
