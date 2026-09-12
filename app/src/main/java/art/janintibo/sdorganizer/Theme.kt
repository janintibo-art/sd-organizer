package art.janintibo.sdorganizer

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

/**
 * Deux couleurs portent toute l'information de l'application :
 * le petrole designe la memoire interne, l'or designe la carte SD.
 * Elles sont utilisees partout de la meme facon : anneaux, pastilles, fleches.
 */
val Petrole = Color(0xFF12313F)
val PetroleClair = Color(0xFF2E6F8E)
val Or = Color(0xFFB4832A)
val OrClair = Color(0xFFF2C777)

val Ardoise = Color(0xFF0F2430)
val Brume = Color(0xFF5B6C77)
val Papier = Color(0xFFEDF0F3)
val Blanc = Color(0xFFFFFFFF)
val Filet = Color(0xFFD3DAE0)
val Alerte = Color(0xFFA8431F)

private val Palette = lightColorScheme(
    primary = Petrole,
    onPrimary = Blanc,
    primaryContainer = Color(0xFFDCE6EC),
    onPrimaryContainer = Petrole,
    secondary = Or,
    onSecondary = Blanc,
    secondaryContainer = Color(0xFFFAECD2),
    onSecondaryContainer = Color(0xFF5C4310),
    tertiary = PetroleClair,
    onTertiary = Blanc,
    background = Papier,
    onBackground = Ardoise,
    surface = Blanc,
    onSurface = Ardoise,
    surfaceVariant = Color(0xFFE3E8EC),
    onSurfaceVariant = Brume,
    outline = Filet,
    outlineVariant = Color(0xFFE6EBEF),
    error = Alerte,
    onError = Blanc,
    errorContainer = Color(0xFFF8E2D9),
    onErrorContainer = Color(0xFF5A2310)
)

private val Lettres = Typography().let { base ->
    base.copy(
        displayMedium = TextStyle(
            fontWeight = FontWeight.Light,
            fontSize = 44.sp,
            letterSpacing = (-1).sp
        ),
        headlineSmall = base.headlineSmall.copy(
            fontWeight = FontWeight.Normal,
            letterSpacing = (-0.4).sp
        ),
        titleMedium = base.titleMedium.copy(fontWeight = FontWeight.Medium),
        labelLarge = base.labelLarge.copy(fontWeight = FontWeight.Medium),
        labelSmall = base.labelSmall.copy(letterSpacing = 0.2.sp)
    )
}

@Composable
fun SdOrganizerTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = Palette,
        typography = Lettres,
        content = content
    )
}
