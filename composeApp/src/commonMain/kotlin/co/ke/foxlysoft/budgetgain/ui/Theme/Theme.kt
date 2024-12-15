package co.ke.foxlysoft.budgetgain.ui.Theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val DarkColorScheme = darkColorScheme(
    primary = Purple100,
    onPrimary = Black,
    secondary = Indigo500,
    onSecondary = LightBlue100,
    tertiary = LightBlue500,
    onTertiary = LightBlue50,
    surface = Black,
    onSurface = Purple100,
    background = Black,
    onBackground = Purple50,
)

private val LightColorScheme = lightColorScheme(
    primary = Purple700,
    onPrimary = Purple50,
    secondary = Indigo500,
    onSecondary = Indigo50,
    tertiary = LightBlue500,
    onTertiary = LightBlue50,
    surface = White,
    onSurface = Purple900,
    background = White,
    onBackground = Black,

    /* Other default colors to override
    background = Color(0xFFFFFBFE),
    surface = Color(0xFFFFFBFE),
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.White,
    onBackground = Color(0xFF1C1B1F),
    onSurface = Color(0xFF1C1B1F),
    */
)

@Composable
fun BudgetGainTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}