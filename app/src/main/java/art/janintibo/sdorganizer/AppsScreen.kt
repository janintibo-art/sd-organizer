package art.janintibo.sdorganizer

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import java.util.Locale

private enum class Filtre { INSTALLEES, DEPLACABLES, SYSTEME }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EcranApplications(
    applis: List<AppEntry>,
    chargement: Boolean,
    accesStatistiques: Boolean,
    onAccesStatistiques: () -> Unit,
    onOptionsDeveloppeur: () -> Unit,
    onOuvrirFiche: (String) -> Unit
) {
    var recherche by remember { mutableStateOf("") }
    var filtre by remember { mutableStateOf(Filtre.INSTALLEES) }

    val visibles = remember(applis, recherche, filtre) {
        val terme = recherche.trim().lowercase(Locale.getDefault())
        applis.filter { app ->
            val passeFiltre = when (filtre) {
                Filtre.INSTALLEES -> app.deplacable != Deplacable.SYSTEME
                Filtre.DEPLACABLES -> app.deplacable == Deplacable.OUI
                Filtre.SYSTEME -> app.deplacable == Deplacable.SYSTEME
            }
            passeFiltre && (terme.isEmpty() ||
                app.nom.lowercase(Locale.getDefault()).contains(terme) ||
                app.paquet.lowercase(Locale.getDefault()).contains(terme))
        }
    }

    Column(Modifier.fillMaxWidth()) {

        Column(Modifier.padding(horizontal = 16.dp)) {
            Spacer(Modifier.height(12.dp))
            OutlinedTextField(
                value = recherche,
                onValueChange = { recherche = it },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                placeholder = { Text("Chercher une application") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(10.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Puce("Installées", filtre == Filtre.INSTALLEES) { filtre = Filtre.INSTALLEES }
                Puce("Déplaçables", filtre == Filtre.DEPLACABLES) { filtre = Filtre.DEPLACABLES }
                Puce("Système", filtre == Filtre.SYSTEME) { filtre = Filtre.SYSTEME }
            }
        }

        if (chargement) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 60.dp),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = PetroleClair)
            }
            return@Column
        }

        LazyColumn(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 14.dp, bottom = 28.dp)
        ) {

            if (!accesStatistiques) {
                item {
                    Panneau {
                        Column(Modifier.padding(16.dp)) {
                            Text(
                                text = "Les tailles affichées sont celles des fichiers d'installation",
                                style = MaterialTheme.typography.titleSmall,
                                color = Ardoise
                            )
                            Spacer(Modifier.height(4.dp))
                            Text(
                                text = "Autorisez l'accès aux données d'utilisation pour voir le poids " +
                                    "réel, données et cache compris.",
                                style = MaterialTheme.typography.bodySmall,
                                color = Brume
                            )
                            TextButton(onClick = onAccesStatistiques) {
                                Text("Ouvrir le réglage", color = Petrole)
                            }
                        }
                    }
                    Spacer(Modifier.height(14.dp))
                }
            }

            if (filtre == Filtre.DEPLACABLES) {
                item {
                    Panneau {
                        Column(Modifier.padding(16.dp)) {
                            Text(
                                text = "La liste est courte ?",
                                style = MaterialTheme.typography.titleSmall,
                                color = Ardoise
                            )
                            Spacer(Modifier.height(4.dp))
                            Text(
                                text = "Dans les options développeur, l'interrupteur « Forcer " +
                                    "l'autorisation d'applis sur stockage externe » rend beaucoup " +
                                    "plus d'applications éligibles après redémarrage.",
                                style = MaterialTheme.typography.bodySmall,
                                color = Brume
                            )
                            TextButton(onClick = onOptionsDeveloppeur) {
                                Text("Ouvrir les options développeur", color = Petrole)
                            }
                        }
                    }
                    Spacer(Modifier.height(14.dp))
                }
            }

            if (visibles.isEmpty()) {
                item {
                    LigneVide(
                        titre = "Rien à afficher",
                        aide = "Aucune application ne correspond à ce filtre."
                    )
                }
            }

            items(visibles, key = { it.paquet }) { app ->
                LigneApplication(app = app, onClick = { onOuvrirFiche(app.paquet) })
                Filament()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun Puce(texte: String, choisi: Boolean, onClick: () -> Unit) {
    FilterChip(
        selected = choisi,
        onClick = onClick,
        shape = RoundedCornerShape(9.dp),
        label = { Text(texte, style = MaterialTheme.typography.labelLarge) },
        colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = Petrole,
            selectedLabelColor = Blanc,
            containerColor = Blanc,
            labelColor = Brume
        )
    )
}

@Composable
private fun LigneApplication(app: AppEntry, onClick: () -> Unit) {
    val context = LocalContext.current
    val icone by produceState<ImageBitmap?>(initialValue = null, key1 = app.paquet) {
        value = Icones.charger(context, app.paquet)
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 13.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(42.dp)
                .clip(RoundedCornerShape(11.dp))
                .background(Papier),
            contentAlignment = Alignment.Center
        ) {
            val image = icone
            if (image != null) {
                Image(
                    bitmap = image,
                    contentDescription = null,
                    modifier = Modifier.size(34.dp)
                )
            } else {
                Image(
                    painter = painterResource(id = R.drawable.ic_apps),
                    contentDescription = null,
                    colorFilter = ColorFilter.tint(Filet),
                    modifier = Modifier.size(18.dp)
                )
            }
        }

        Spacer(Modifier.width(13.dp))

        Column(Modifier.weight(1f)) {
            Text(
                text = app.nom,
                style = MaterialTheme.typography.titleMedium,
                color = Ardoise,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(Modifier.height(4.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                if (app.emplacement == Emplacement.CARTE_SD) {
                    Pastille("Sur la carte SD", Or)
                } else {
                    when (app.deplacable) {
                        Deplacable.OUI -> Pastille("Peut aller sur la carte", Or)
                        Deplacable.NON -> Pastille("Reste en interne", Brume)
                        Deplacable.SYSTEME -> Pastille("Système", Brume)
                    }
                }
            }
        }

        Spacer(Modifier.width(10.dp))

        Text(
            text = taille(app.octets),
            style = MaterialTheme.typography.titleSmall,
            color = Ardoise
        )
    }
}
