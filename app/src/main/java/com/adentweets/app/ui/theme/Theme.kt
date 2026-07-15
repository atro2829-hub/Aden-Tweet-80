package com.adentweets.app.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme(
    primary = XBlue,
    onPrimary = Color.White,
    primaryContainer = XBlueDark,
    onPrimaryContainer = Color.White,
    secondary = XGray,
    onSecondary = Color.White,
    secondaryContainer = XDarkElevated,
    onSecondaryContainer = Color.White,
    tertiary = XGreen,
    onTertiary = Color.White,
    background = XDark,
    onBackground = Color.White,
    surface = XDarkSurface,
    onSurface = Color.White,
    surfaceVariant = XDarkElevated,
    onSurfaceVariant = XLightGray,
    outline = XGray,
    outlineVariant = XDarkHover,
    error = XRed,
    onError = Color.White,
    errorContainer = Color(0xFF93000A),
    onErrorContainer = Color(0xFFFFDAD6),
    inverseSurface = XExtraLightGray,
    inverseOnSurface = XDark,
    inversePrimary = XBlueDark,
    surfaceTint = Color.Transparent,
    scrim = Color.Black,
)

private val LightColorScheme = lightColorScheme(
    primary = XBlue,
    onPrimary = Color.White,
    primaryContainer = XBlueLight,
    onPrimaryContainer = XBlueDark,
    secondary = XGray,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFE8DEF8),
    onSecondaryContainer = XDark,
    tertiary = XGreen,
    onTertiary = Color.White,
    background = Color.White,
    onBackground = XDark,
    surface = Color.White,
    onSurface = XDark,
    surfaceVariant = XExtraLightGray,
    onSurfaceVariant = XGray,
    outline = Color(0xFF79747E),
    outlineVariant = Color(0xFFCAC4D0),
    error = XRed,
    onError = Color.White,
    errorContainer = Color(0xFFFFDAD6),
    onErrorContainer = Color(0xFF410E0B),
    inverseSurface = XDarkSurface,
    inverseOnSurface = Color.White,
    inversePrimary = XBlue,
    surfaceTint = Color.Transparent,
)

object XBlue { val value = Color(0xFF1D9BF0) }
object XBlueDark { val value = Color(0xFF1A8CD8) }
object XBlueLight { val value = Color(0xFFE8F5FD) }
object XDark { val value = Color(0xFF000000) }
object XDarkSurface { val value = Color(0xFF16181C) }
object XDarkElevated { val value = Color(0xFF1D1F23) }
object XDarkHover { val value = Color(0xFF2F3336) }
object XGray { val value = Color(0xFF536471) }
object XLightGray { val value = Color(0xFF71767B) }
object XExtraLightGray { val value = Color(0xFFEFF3F4) }
object XRed { val value = Color(0xFFF4212E) }
object XGreen { val value = Color(0xFF00BA7C) }
object XYellow { val value = Color(0xFFFFD400) }
object XPurple { val value = Color(0xFF7C3AED) }

@Composable
fun AdenTweetTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.background.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
            window.navigationBarColor = colorScheme.background.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightNavigationBars = false
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = TypographyX,
        content = content
    )
}