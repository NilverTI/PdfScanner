package com.tantalean.scaneradd.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = Red,
    onPrimary = Color.White,

    secondary = RedSoft,
    onSecondary = Color.White,

    background = Black,
    onBackground = White,

    surface = SurfaceDark,
    onSurface = White,

    surfaceVariant = SurfaceDark2,
    onSurfaceVariant = WhiteSoft,

    outline = Color(0xFF2A2A36),
    error = Color(0xFFFF4D4D),
    onError = Color.White
)

@Composable
fun ScanerADDTheme(
    // Forzamos oscuro, aunque el sistema esté claro
    darkTheme: Boolean = true,
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        typography = Typography(),
        content = content
    )
}