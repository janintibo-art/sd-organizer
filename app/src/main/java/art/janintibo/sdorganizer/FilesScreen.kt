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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp

data class Progression(val fait: Int, val total: Int, val nom: String)

@Composable
fun EcranFichiers(
    dossiers: List<DossierEntry>,
    chargement: Boolean,
    sd: VolumeInfo?,
    accesFichiers: Boolean,
    progression: Progression?,
    bilan: Bilan?,
    onDemanderAcces: () -> Unit,
    onDeplacer: (List<DossierEntry>) -> Unit,
    onFermerBilan: () -> Unit
) {
    val choix = remember(dossiers) { mutableStateMapOf<String, Boolean>() }
    var confirmation by remember { mutableStateOf(false) }

    val selection = dossiers.filter { choix[it.dossier.absolutePath] == true }
    val poids = selection.sumOf { it.octets }

    if (!accesFichiers) {
        Column(Modifier.padding(16.dp)) {
            Panneau {
                Column(Modifier.padding(18.dp)) {
                    Text(
                        text = "Accès aux fichiers requis",
                        style = MaterialTheme.typography.titleMedium,
                        color = Ardoise
                    )
                    Spacer(Modifier.height(6.dp))
                    Text(
                        text = "Déplacer un fichier suppose de pouvoir le lire ici et l'écrire " +
                            "là-bas. Android demande une autorisation explicite pour cela.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Brume
                    )
                    Spacer(Modifier.height(14.dp))
                    Button(
                        onClick = onDemanderAcces,
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Petrole)
                    ) {
                        Text("Ouvrir l'autorisation")
                    }
                }
            }
        }
        return
    }

    Column(Modifier.fillMaxSize()) {

        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 16.dp)
        ) {

            item {
                Panneau {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(11.dp))
                                .background(Or.copy(alpha = 0.13f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.ic_sd_card),
                                contentDescription = null,
                                colorFilter = ColorFilter.tint(Or),
                                modifier = Modifier.size(21.dp)
                            )
                        }
                        Spacer(Modifier.width(14.dp))
                        Column(Modifier.weight(1f)) {
                            Text(
                                text = "Destination : carte SD",
                                style = MaterialTheme.typography.titleMedium,
                                color = Ardoise
                            )
                            Text(
                                text = if (sd != null)
                                    taille(sd.libre) + " de libre"
                                else
                                    "Aucune carte détectée",
                                style = MaterialTheme.typography.bodySmall,
                                color = Brume
                            )
                        }
                    }
                }
                EnTete("Dossiers de la mémoire interne")
            }

            if (chargement) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 50.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = PetroleClair)
                    }
                }
            } else if (dossiers.isEmpty()) {
                item {
                    LigneVide(
                        titre = "Rien de volumineux ici",
                        aide = "Les dossiers publics de la mémoire interne sont vides."
                    )
                }
            } else {
                item { Spacer(Modifier.height(2.dp)) }
                items(dossiers, key = { it.dossier.absolutePath }) { entree ->
                    val cle = entree.dossier.absolutePath
                    LigneDossier(
                        entree = entree,
                        choisi = choix[cle] == true,
                        onBascule = { choix[cle] = choix[cle] != true }
                    )
                    Filament()
                }
                item {
                    Text(
                        text = "Les photos et vidéos déplacées restent visibles dans la galerie. " +
                            "Une carte SD est plus lente que la mémoire interne : évitez d'y " +
                            "envoyer ce que vous ouvrez tous les jours.",
                        style = MaterialTheme.typography.bodySmall,
                        color = Brume,
                        modifier = Modifier.padding(top = 18.dp)
                    )
                }
            }
        }

        if (selection.isNotEmpty() && sd != null) {
            Panneau(rayon = 0) {
                Column(Modifier.padding(16.dp)) {
                    Button(
                        onClick = { confirmation = true },
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Or),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                    ) {
                        Text(
                            text = "Déplacer " + taille(poids) + " vers la carte SD",
                            style = MaterialTheme.typography.labelLarge
                        )
                    }
                }
            }
        }
    }

    if (confirmation) {
        AlertDialog(
            onDismissRequest = { confirmation = false },
            shape = RoundedCornerShape(18.dp),
            title = { Text("Déplacer maintenant ?") },
            text = {
                Text(
                    text = selection.joinToString(", ") { it.titre } + ". " +
                        "Les fichiers sont copiés sur la carte, vérifiés, puis effacés de la " +
                        "mémoire interne. Gardez le téléphone branché si le volume est important."
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    confirmation = false
                    onDeplacer(selection)
                }) {
                    Text("Déplacer", color = Petrole)
                }
            },
            dismissButton = {
                TextButton(onClick = { confirmation = false }) {
                    Text("Annuler", color = Brume)
                }
            }
        )
    }

    if (progression != null) {
        val part = if (progression.total <= 0) 0f
        else progression.fait.toFloat() / progression.total.toFloat()
        AlertDialog(
            onDismissRequest = { },
            shape = RoundedCornerShape(18.dp),
            title = { Text("Déplacement en cours") },
            text = {
                Column {
                    Text(
                        text = progression.nom,
                        style = MaterialTheme.typography.bodySmall,
                        color = Brume,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(Modifier.height(12.dp))
                    LinearProgressIndicator(
                        progress = { part },
                        color = Or,
                        trackColor = Or.copy(alpha = 0.15f),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = progression.fait.toString() + " / " + progression.total.toString(),
                        style = MaterialTheme.typography.bodySmall,
                        color = Brume
                    )
                }
            },
            confirmButton = { }
        )
    }

    if (bilan != null) {
        AlertDialog(
            onDismissRequest = onFermerBilan,
            shape = RoundedCornerShape(18.dp),
            title = { Text(if (bilan.reussite) "Terminé" else "Déplacement interrompu") },
            text = { Text(bilan.message) },
            confirmButton = {
                TextButton(onClick = onFermerBilan) {
                    Text("D'accord", color = Petrole)
                }
            }
        )
    }
}

@Composable
private fun LigneDossier(
    entree: DossierEntry,
    choisi: Boolean,
    onBascule: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onBascule)
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(
            checked = choisi,
            onCheckedChange = { onBascule() },
            colors = CheckboxDefaults.colors(
                checkedColor = Or,
                uncheckedColor = Filet,
                checkmarkColor = Blanc
            )
        )
        Spacer(Modifier.width(4.dp))
        Column(Modifier.weight(1f)) {
            Text(
                text = entree.titre,
                style = MaterialTheme.typography.titleMedium,
                color = Ardoise,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(Modifier.height(2.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = entree.fichiers.toString() + " fichiers",
                    style = MaterialTheme.typography.bodySmall,
                    color = Brume
                )
                Text(
                    text = entree.dossier.name,
                    style = MaterialTheme.typography.bodySmall,
                    color = Filet
                )
            }
        }
        Spacer(Modifier.width(10.dp))
        Text(
            text = taille(entree.octets),
            style = MaterialTheme.typography.titleSmall,
            color = Ardoise
        )
    }
}
