package art.janintibo.sdorganizer

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import kotlinx.coroutines.launch

private enum class Onglet { ACCUEIL, APPLIS, FICHIERS }

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
private fun Application() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var onglet by remember { mutableStateOf(Onglet.ACCUEIL) }
    var cycle by remember { mutableIntStateOf(0) }

    var interne by remember { mutableStateOf<VolumeInfo?>(null) }
    var sd by remember { mutableStateOf<VolumeInfo?>(null) }
    var accesFichiers by remember { mutableStateOf(false) }
    var accesStatistiques by remember { mutableStateOf(false) }

    var applis by remember { mutableStateOf<List<AppEntry>>(emptyList()) }
    var chargementApplis by remember { mutableStateOf(true) }

    var dossiers by remember { mutableStateOf<List<DossierEntry>>(emptyList()) }
    var chargementDossiers by remember { mutableStateOf(true) }

    var progression by remember { mutableStateOf<Progression?>(null) }
    var bilan by remember { mutableStateOf<Bilan?>(null) }

    val proprietaire = LocalLifecycleOwner.current
    DisposableEffect(proprietaire) {
        val observateur = LifecycleEventObserver { _, evenement ->
            if (evenement == Lifecycle.Event.ON_RESUME) cycle++
        }
        proprietaire.lifecycle.addObserver(observateur)
        onDispose { proprietaire.lifecycle.removeObserver(observateur) }
    }

    LaunchedEffect(cycle) {
        accesFichiers = Storage.accesFichiers()
        accesStatistiques = Storage.accesStatistiques(context)
        interne = Storage.interne(context)
        sd = Storage.carteSd(context)

        chargementApplis = true
        applis = Apps.lire(context, avecSysteme = true)
        chargementApplis = false

        if (accesFichiers) {
            chargementDossiers = true
            dossiers = Fichiers.scanner()
            chargementDossiers = false
        } else {
            dossiers = emptyList()
            chargementDossiers = false
        }
    }

    val titre = when (onglet) {
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
                    choisi = onglet == Onglet.ACCUEIL
                ) { onglet = Onglet.ACCUEIL }
                Onglets(
                    icone = R.drawable.ic_apps,
                    texte = "Applications",
                    choisi = onglet == Onglet.APPLIS
                ) { onglet = Onglet.APPLIS }
                Onglets(
                    icone = R.drawable.ic_folder,
                    texte = "Fichiers",
                    choisi = onglet == Onglet.FICHIERS
                ) { onglet = Onglet.FICHIERS }
            }
        }
    ) { marges ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(marges)
        ) {
            when (onglet) {
                Onglet.ACCUEIL -> EcranAccueil(
                    interne = interne,
                    sd = sd,
                    accesFichiers = accesFichiers,
                    onDemanderAcces = { Storage.demanderAccesFichiers(context) },
                    onApplis = { onglet = Onglet.APPLIS },
                    onFichiers = { onglet = Onglet.FICHIERS }
                )

                Onglet.APPLIS -> EcranApplications(
                    applis = applis,
                    chargement = chargementApplis,
                    accesStatistiques = accesStatistiques,
                    onAccesStatistiques = { Storage.demanderAccesStatistiques(context) },
                    onOptionsDeveloppeur = { Storage.ouvrirOptionsDeveloppeur(context) },
                    onOuvrirFiche = { paquet -> Storage.ouvrirFicheApplication(context, paquet) }
                )

                Onglet.FICHIERS -> EcranFichiers(
                    dossiers = dossiers,
                    chargement = chargementDossiers,
                    sd = sd,
                    accesFichiers = accesFichiers,
                    progression = progression,
                    bilan = bilan,
                    onDemanderAcces = { Storage.demanderAccesFichiers(context) },
                    onFermerBilan = { bilan = null },
                    onDeplacer = { choisis ->
                        val carte = sd
                        if (carte != null) {
                            scope.launch {
                                progression = Progression(0, 1, "Préparation")
                                val resultat = Fichiers.deplacer(
                                    context = context,
                                    dossiers = choisis,
                                    sd = carte
                                ) { fait, total, nom ->
                                    if (fait == total || total < 40 || fait % 5 == 0) {
                                        progression = Progression(fait, total, nom)
                                    }
                                }
                                progression = null
                                bilan = resultat
                                cycle++
                            }
                        }
                    }
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
