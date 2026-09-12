package art.janintibo.sdorganizer

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

enum class Onglet { ACCUEIL, APPLIS, FICHIERS }

/**
 * Tout l'etat vit ici, pas dans l'ecran : revenir des Parametres ne relance
 * plus aucun chargement visible. Les volumes et les autorisations sont relus
 * a chaque retour parce que c'est instantane ; les applications et les dossiers
 * ne sont explores qu'au premier lancement, apres un deplacement, ou quand on
 * appuie sur le bouton de rafraichissement.
 */
class EtatViewModel(application: Application) : AndroidViewModel(application) {

    var onglet by mutableStateOf(Onglet.ACCUEIL)

    var interne by mutableStateOf<VolumeInfo?>(null)
        private set
    var sd by mutableStateOf<VolumeInfo?>(null)
        private set
    var accesFichiers by mutableStateOf(false)
        private set
    var accesStatistiques by mutableStateOf(false)
        private set

    var applis by mutableStateOf<List<AppEntry>>(emptyList())
        private set
    var chargementApplis by mutableStateOf(true)
        private set

    var dossiers by mutableStateOf<List<DossierEntry>>(emptyList())
        private set
    var chargementDossiers by mutableStateOf(true)
        private set

    var progression by mutableStateOf<Progression?>(null)
        private set
    var bilan by mutableStateOf<Bilan?>(null)
        private set

    var occupe by mutableStateOf(false)
        private set

    private var premierPassage = true
    private var dossiersAJour = false

    /** Retour au premier plan : on ne relit que ce qui est instantane. */
    fun reprendre() {
        val contexte = getApplication<Application>()
        accesFichiers = Storage.accesFichiers()
        accesStatistiques = Storage.accesStatistiques(contexte)
        interne = Storage.interne(contexte)
        sd = Storage.carteSd(contexte)

        if (premierPassage) {
            premierPassage = false
            relire()
        }
    }

    /** Bouton de rafraichissement : on refait tout, y compris les dossiers. */
    fun rafraichir() {
        val contexte = getApplication<Application>()
        accesFichiers = Storage.accesFichiers()
        accesStatistiques = Storage.accesStatistiques(contexte)
        interne = Storage.interne(contexte)
        sd = Storage.carteSd(contexte)
        dossiersAJour = false
        relire()
    }

    private fun relire() {
        if (occupe) return
        occupe = true
        val contexte = getApplication<Application>()

        viewModelScope.launch {
            applis = Apps.lire(contexte, avecSysteme = true)
            chargementApplis = false

            if (!accesFichiers) {
                dossiers = emptyList()
                dossiersAJour = false
            } else if (!dossiersAJour) {
                dossiers = Fichiers.scanner()
                dossiersAJour = true
            }
            chargementDossiers = false
            occupe = false
        }
    }

    fun deplacer(choisis: List<DossierEntry>) {
        val carte = sd ?: return
        if (progression != null) return

        viewModelScope.launch {
            progression = Progression(0, 1, "Préparation")
            val resultat = Fichiers.deplacer(
                context = getApplication<Application>(),
                dossiers = choisis,
                sd = carte
            ) { fait, total, nom ->
                if (fait == total || total < 40 || fait % 5 == 0) {
                    progression = Progression(fait, total, nom)
                }
            }
            progression = null
            bilan = resultat
            dossiersAJour = false
            rafraichir()
        }
    }

    fun fermerBilan() {
        bilan = null
    }
}
