package art.janintibo.sdorganizer

import android.content.Context
import android.media.MediaScannerConnection
import android.os.Environment
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream

data class DossierEntry(
    val titre: String,
    val dossier: File,
    val octets: Long,
    val fichiers: Int
)

data class Bilan(
    val reussite: Boolean,
    val deplaces: Int,
    val echecs: Int,
    val octets: Long,
    val message: String
)

object Fichiers {

    private val CANDIDATS = listOf(
        Environment.DIRECTORY_DCIM to "Photos et vidéos de l'appareil",
        Environment.DIRECTORY_PICTURES to "Images",
        Environment.DIRECTORY_MOVIES to "Vidéos",
        Environment.DIRECTORY_DOWNLOADS to "Téléchargements",
        Environment.DIRECTORY_MUSIC to "Musique",
        Environment.DIRECTORY_PODCASTS to "Podcasts",
        Environment.DIRECTORY_AUDIOBOOKS to "Livres audio",
        Environment.DIRECTORY_DOCUMENTS to "Documents"
    )

    suspend fun scanner(): List<DossierEntry> = withContext(Dispatchers.IO) {
        val racine = Environment.getExternalStorageDirectory()
        val liste = ArrayList<DossierEntry>()
        for (candidat in CANDIDATS) {
            val dossier = File(racine, candidat.first)
            if (!dossier.isDirectory) continue
            val mesure = mesurer(dossier)
            if (mesure.second == 0) continue
            liste.add(DossierEntry(candidat.second, dossier, mesure.first, mesure.second))
        }
        liste.sortedByDescending { it.octets }
    }

    private fun mesurer(racine: File): Pair<Long, Int> {
        var octets = 0L
        var nombre = 0
        val pile = ArrayDeque<File>()
        pile.addLast(racine)
        while (pile.isNotEmpty()) {
            val courant = pile.removeLast()
            val enfants = courant.listFiles() ?: continue
            for (enfant in enfants) {
                if (enfant.isDirectory) {
                    pile.addLast(enfant)
                } else {
                    octets += enfant.length()
                    nombre++
                }
            }
        }
        return Pair(octets, nombre)
    }

    private fun lister(racine: File): List<File> {
        val trouves = ArrayList<File>()
        val pile = ArrayDeque<File>()
        pile.addLast(racine)
        while (pile.isNotEmpty()) {
            val courant = pile.removeLast()
            val enfants = courant.listFiles() ?: continue
            for (enfant in enfants) {
                if (enfant.isDirectory) pile.addLast(enfant) else trouves.add(enfant)
            }
        }
        return trouves
    }

    suspend fun deplacer(
        context: Context,
        dossiers: List<DossierEntry>,
        sd: VolumeInfo,
        progres: (Int, Int, String) -> Unit
    ): Bilan = withContext(Dispatchers.IO) {

        if (!sd.racine.isDirectory) {
            return@withContext Bilan(false, 0, 0, 0L, "La carte SD n'est plus accessible.")
        }
        if (!sd.racine.canWrite()) {
            return@withContext Bilan(
                false, 0, 0, 0L,
                "Écriture refusée sur la carte SD. Vérifiez l'autorisation « Accès à tous les fichiers »."
            )
        }

        val aDeplacer = dossiers.sumOf { it.octets }
        if (aDeplacer > sd.libre) {
            return@withContext Bilan(
                false, 0, 0, 0L,
                "Il manque de la place : " + taille(aDeplacer) + " à déplacer, " +
                    taille(sd.libre) + " de libre sur la carte."
            )
        }

        val travail = ArrayList<Pair<File, File>>()
        for (entree in dossiers) {
            val cible = File(sd.racine, entree.dossier.name)
            for (fichier in lister(entree.dossier)) {
                val relatif = fichier.relativeTo(entree.dossier).path
                travail.add(Pair(fichier, File(cible, relatif)))
            }
        }

        val total = travail.size
        var deplaces = 0
        var echecs = 0
        var octets = 0L
        val aScanner = ArrayList<String>()

        for ((index, couple) in travail.withIndex()) {
            val source = couple.first
            val destination = libre(couple.second)
            progres(index + 1, total, source.name)
            val poids = source.length()
            if (copier(source, destination)) {
                if (source.delete()) {
                    deplaces++
                    octets += poids
                    if (aScanner.size < 4000) {
                        aScanner.add(source.absolutePath)
                        aScanner.add(destination.absolutePath)
                    }
                } else {
                    echecs++
                }
            } else {
                echecs++
            }
        }

        for (entree in dossiers) {
            viderDossiersVides(entree.dossier)
        }

        if (aScanner.isNotEmpty()) {
            try {
                MediaScannerConnection.scanFile(context, aScanner.toTypedArray(), null, null)
            } catch (e: Exception) {
            }
        }

        val message = when {
            total == 0 -> "Aucun fichier à déplacer."
            echecs == 0 -> taille(octets) + " libérés sur la mémoire interne."
            deplaces == 0 -> "Aucun fichier n'a pu être déplacé."
            else -> taille(octets) + " libérés, " + echecs + " fichiers laissés sur place."
        }

        Bilan(deplaces > 0 || total == 0, deplaces, echecs, octets, message)
    }

    private fun libre(souhaite: File): File {
        if (!souhaite.exists()) return souhaite
        val nom = souhaite.nameWithoutExtension
        val extension = souhaite.extension
        var i = 1
        while (i < 1000) {
            val suffixe = if (extension.isEmpty()) "$nom($i)" else "$nom($i).$extension"
            val essai = File(souhaite.parentFile, suffixe)
            if (!essai.exists()) return essai
            i++
        }
        return souhaite
    }

    private fun copier(source: File, destination: File): Boolean {
        return try {
            destination.parentFile?.mkdirs()
            FileInputStream(source).use { entree ->
                FileOutputStream(destination).use { sortie ->
                    entree.copyTo(sortie, 1 shl 16)
                    sortie.flush()
                }
            }
            if (destination.length() != source.length()) {
                destination.delete()
                false
            } else {
                true
            }
        } catch (e: Exception) {
            try { destination.delete() } catch (e2: Exception) { }
            false
        }
    }

    private fun viderDossiersVides(racine: File) {
        val enfants = racine.listFiles() ?: return
        for (enfant in enfants) {
            if (enfant.isDirectory) {
                viderDossiersVides(enfant)
                val restants = enfant.listFiles()
                if (restants != null && restants.isEmpty()) {
                    try { enfant.delete() } catch (e: Exception) { }
                }
            }
        }
    }
}
