package art.janintibo.sdorganizer

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.border
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

/**
 * Anneau d'occupation d'un volume. Le centre annonce l'espace libre,
 * c'est la seule information qui compte quand on cherche de la place.
 */
@Composable
fun Anneau(
    taux: Float,
    couleur: Color,
    libre: String,
    legende: String,
    detail: String,
    modifier: Modifier = Modifier
) {
    val anime by animateFloatAsState(
        targetValue = taux,
        animationSpec = tween(durationMillis = 700),
        label = "anneau"
    )
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier.size(124.dp),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val epaisseur = 13.dp.toPx()
                val marge = epaisseur / 2f
                val zone = Size(size.width - epaisseur, size.height - epaisseur)
                drawArc(
                    color = couleur.copy(alpha = 0.14f),
                    startAngle = -90f,
                    sweepAngle = 360f,
                    useCenter = false,
                    topLeft = Offset(marge, marge),
                    size = zone,
                    style = Stroke(width = epaisseur, cap = StrokeCap.Round)
                )
                if (anime > 0.001f) {
                    drawArc(
                        color = couleur,
                        startAngle = -90f,
                        sweepAngle = 360f * anime,
                        useCenter = false,
                        topLeft = Offset(marge, marge),
                        size = zone,
                        style = Stroke(width = epaisseur, cap = StrokeCap.Round)
                    )
                }
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = libre,
                    style = MaterialTheme.typography.headlineSmall,
                    color = Ardoise
                )
                Text(
                    text = "libre",
                    style = MaterialTheme.typography.bodySmall,
                    color = Brume
                )
            }
        }
        Text(
            text = legende,
            style = MaterialTheme.typography.titleSmall,
            color = Ardoise,
            modifier = Modifier.padding(top = 12.dp)
        )
        Text(
            text = detail,
            style = MaterialTheme.typography.bodySmall,
            color = Brume,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun Panneau(
    modifier: Modifier = Modifier,
    rayon: Int = 20,
    contenu: @Composable () -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(rayon.dp))
            .background(Blanc)
            .border(1.dp, Filet, RoundedCornerShape(rayon.dp))
    ) {
        contenu()
    }
}

@Composable
fun Pastille(texte: String, couleur: Color) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(couleur.copy(alpha = 0.12f))
            .padding(horizontal = 7.dp, vertical = 2.dp)
    ) {
        Text(
            text = texte,
            style = MaterialTheme.typography.labelSmall,
            color = couleur
        )
    }
}

@Composable
fun Filament() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(1.dp)
            .background(Filet)
    )
}

@Composable
fun LigneVide(titre: String, aide: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 40.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Text(
            text = titre,
            style = MaterialTheme.typography.titleMedium,
            color = Ardoise,
            textAlign = TextAlign.Center
        )
        Text(
            text = aide,
            style = MaterialTheme.typography.bodyMedium,
            color = Brume,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun EnTete(texte: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 4.dp, end = 4.dp, top = 22.dp, bottom = 10.dp)
    ) {
        Text(
            text = texte,
            style = MaterialTheme.typography.titleSmall,
            color = Brume
        )
    }
}
