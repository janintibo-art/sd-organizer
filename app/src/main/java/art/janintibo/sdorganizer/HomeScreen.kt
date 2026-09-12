package art.janintibo.sdorganizer

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp

@Composable
fun EcranAccueil(
    interne: VolumeInfo?,
    sd: VolumeInfo?,
    accesFichiers: Boolean,
    onDemanderAcces: () -> Unit,
    onApplis: () -> Unit,
    onFichiers: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp)
            .padding(bottom = 28.dp)
    ) {

        if (!accesFichiers) {
            Spacer(Modifier.height(16.dp))
            Panneau {
                Column(Modifier.padding(18.dp)) {
                    Text(
                        text = "Autoriser l'accès aux fichiers",
                        style = MaterialTheme.typography.titleMedium,
                        color = Ardoise
                    )
                    Spacer(Modifier.height(6.dp))
                    Text(
                        text = "Sans cette autorisation, l'application ne peut ni mesurer " +
                            "la carte SD ni y déplacer quoi que ce soit.",
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

        Spacer(Modifier.height(16.dp))

        Panneau(rayon = 24) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 26.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.Top
            ) {
                if (interne != null) {
                    Anneau(
                        taux = interne.taux,
                        couleur = PetroleClair,
                        libre = taille(interne.libre),
                        legende = "Mémoire interne",
                        detail = taille(interne.occupe) + " sur " + taille(interne.total)
                    )
                }
                if (sd != null) {
                    Anneau(
                        taux = sd.taux,
                        couleur = Or,
                        libre = taille(sd.libre),
                        legende = "Carte SD",
                        detail = taille(sd.occupe) + " sur " + taille(sd.total)
                    )
                }
            }
        }

        if (sd == null) {
            Spacer(Modifier.height(12.dp))
            Panneau {
                Column(Modifier.padding(18.dp)) {
                    Text(
                        text = "Aucune carte SD détectée",
                        style = MaterialTheme.typography.titleMedium,
                        color = Ardoise
                    )
                    Spacer(Modifier.height(6.dp))
                    Text(
                        text = if (accesFichiers)
                            "Insérez une carte, puis revenez sur cet écran."
                        else
                            "Accordez d'abord l'accès aux fichiers : la carte reste invisible sans lui.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Brume
                    )
                }
            }
        }

        EnTete("Libérer de la place")

        Panneau {
            Column {
                Passerelle(
                    icone = R.drawable.ic_folder,
                    teinte = Or,
                    titre = "Déplacer des fichiers",
                    detail = "Photos, vidéos et téléchargements partent sur la carte. " +
                        "C'est ici qu'on récupère le plus d'espace.",
                    actif = sd != null && accesFichiers,
                    onClick = onFichiers
                )
                Filament()
                Passerelle(
                    icone = R.drawable.ic_apps,
                    teinte = PetroleClair,
                    titre = "Trier les applications",
                    detail = "Voir lesquelles pèsent le plus et lesquelles acceptent " +
                        "la carte SD, puis ouvrir le bon réglage.",
                    actif = true,
                    onClick = onApplis
                )
            }
        }

        EnTete("Bon à savoir")

        Panneau {
            Text(
                text = "Android ne laisse aucune application en déplacer une autre sur la carte SD : " +
                    "seul le menu Paramètres du téléphone en a le droit. SD Organizer repère " +
                    "les applications concernées et vous y emmène en un geste.",
                style = MaterialTheme.typography.bodyMedium,
                color = Brume,
                modifier = Modifier.padding(18.dp)
            )
        }
    }
}

@Composable
private fun Passerelle(
    icone: Int,
    teinte: Color,
    titre: String,
    detail: String,
    actif: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = actif, onClick = onClick)
            .padding(18.dp),
        verticalAlignment = Alignment.Top
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(11.dp))
                .background(teinte.copy(alpha = if (actif) 0.13f else 0.07f)),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(id = icone),
                contentDescription = null,
                colorFilter = ColorFilter.tint(if (actif) teinte else Filet),
                modifier = Modifier.size(21.dp)
            )
        }
        Spacer(Modifier.width(14.dp))
        Column(Modifier.weight(1f)) {
            Text(
                text = titre,
                style = MaterialTheme.typography.titleMedium,
                color = if (actif) Ardoise else Brume
            )
            Spacer(Modifier.height(3.dp))
            Text(
                text = detail,
                style = MaterialTheme.typography.bodySmall,
                color = Brume
            )
        }
    }
}
