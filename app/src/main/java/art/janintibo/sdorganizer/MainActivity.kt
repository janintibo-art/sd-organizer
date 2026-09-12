package art.janintibo.sdorganizer

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.viewmodel.compose.viewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SdOrganizerTheme {
                Application()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun Application(etat: EtatViewModel = viewModel()) {
    val context = LocalContext.current

    val proprietaire = LocalLifecycleOwner.current
    DisposableEffect(proprietaire) {
        val observateur = LifecycleEventObserver { _, evenement ->
            if (evenement == Lifecycle.Event.ON_RESUME) etat.reprendre()
        }
        proprietaire.lifecycle.addObserver(observateur)
        onDispose { proprietaire.lifecycle.removeObserver(observateur) }
    }

    val titre = when (etat.onglet) {
        Onglet.ACCUEIL -> "SD Organizer"
        Onglet.APPLIS -> "Applications"
        Onglet.FICHIERS -> "Fichiers"
    }

    Scaffold(
        containerColor = Papier,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = titre,
                        style = MaterialTheme.typography.titleLarge
                    )
                },
                actions = {
                    if (etat.occupe) {
                        Box(
                            modifier = Modifier.size(48.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(
                                color = Blanc,
                                strokeWidth = 2.dp,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    } else {
                        IconButton(onClick = { etat.rafraichir() }) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_refresh),
                                contentDescription = "Actualiser",
                                tint = Blanc,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Petrole,
                    titleContentColor = Blanc
                )
            )
        },
        bottomBar = {
            NavigationBar(containerColor = Blanc, tonalElevation = 0.dp) {
                Onglets(
                    icone = R.drawable.ic_home,
                    texte = "Accueil",
                    choisi = etat.onglet == Onglet.ACCUEIL
                ) { etat.onglet = Onglet.ACCUEIL }
                Onglets(
                    icone = R.drawable.ic_apps,
                    texte = "Applications",
                    choisi = etat.onglet == Onglet.APPLIS
                ) { etat.onglet = Onglet.APPLIS }
                Onglets(
                    icone = R.drawable.ic_folder,
                    texte = "Fichiers",
                    choisi = etat.onglet == Onglet.FICHIERS
                ) { etat.onglet = Onglet.FICHIERS }
            }
        }
    ) { marges ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(marges)
        ) {
            when (etat.onglet) {
                Onglet.ACCUEIL -> EcranAccueil(
                    interne = etat.interne,
                    sd = etat.sd,
                    accesFichiers = etat.accesFichiers,
                    onDemanderAcces = { Storage.demanderAccesFichiers(context) },
                    onApplis = { etat.onglet = Onglet.APPLIS },
                    onFichiers = { etat.onglet = Onglet.FICHIERS }
                )

                Onglet.APPLIS -> EcranApplications(
                    applis = etat.applis,
                    chargement = etat.chargementApplis,
                    accesStatistiques = etat.accesStatistiques,
                    onAccesStatistiques = { Storage.demanderAccesStatistiques(context) },
                    onOptionsDeveloppeur = { Storage.ouvrirOptionsDeveloppeur(context) },
                    onOuvrirFiche = { paquet -> Storage.ouvrirFicheApplication(context, paquet) }
                )

                Onglet.FICHIERS -> EcranFichiers(
                    dossiers = etat.dossiers,
                    chargement = etat.chargementDossiers,
                    sd = etat.sd,
                    accesFichiers = etat.accesFichiers,
                    progression = etat.progression,
                    bilan = etat.bilan,
                    onDemanderAcces = { Storage.demanderAccesFichiers(context) },
                    onFermerBilan = { etat.fermerBilan() },
                    onDeplacer = { choisis -> etat.deplacer(choisis) }
                )
            }
        }
    }
}

@Composable
private fun RowScope.Onglets(
    icone: Int,
    texte: String,
    choisi: Boolean,
    onClick: () -> Unit
) {
    NavigationBarItem(
        selected = choisi,
        onClick = onClick,
        icon = {
            Icon(
                painter = painterResource(id = icone),
                contentDescription = texte,
                modifier = Modifier.size(21.dp)
            )
        },
        label = { Text(texte, style = MaterialTheme.typography.labelSmall) },
        colors = NavigationBarItemDefaults.colors(
            selectedIconColor = Petrole,
            selectedTextColor = Petrole,
            unselectedIconColor = Brume,
            unselectedTextColor = Brume,
            indicatorColor = Petrole.copy(alpha = 0.10f)
        )
    )
}
