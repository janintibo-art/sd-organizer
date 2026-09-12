package art.janintibo.sdorganizer

import android.app.AppOpsManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Environment
import android.os.Process
import android.os.storage.StorageManager
import android.provider.Settings
import java.io.File
import java.text.DecimalFormat

data class VolumeInfo(
    val nom: String,
    val racine: File,
    val total: Long,
    val libre: Long,
    val amovible: Boolean
) {
    val occupe: Long get() = (total - libre).coerceAtLeast(0L)
    val taux: Float get() = if (total <= 0L) 0f else (occupe.toFloat() / total.toFloat()).coerceIn(0f, 1f)
}

object Storage {

    fun volumes(context: Context): List<VolumeInfo> {
        val sm = context.getSystemService(Context.STORAGE_SERVICE) as StorageManager
        val liste = ArrayList<VolumeInfo>()
        for (v in sm.storageVolumes) {
            val dossier = v.directory ?: continue
            val total = try { dossier.totalSpace } catch (e: Exception) { 0L }
            if (total <= 0L) continue
            val libre = try { dossier.freeSpace } catch (e: Exception) { 0L }
            val nom = if (v.isRemovable) "Carte SD" else "Mémoire interne"
            liste.add(VolumeInfo(nom, dossier, total, libre, v.isRemovable))
        }
        return liste
    }

    fun interne(context: Context): VolumeInfo? =
        volumes(context).firstOrNull { !it.amovible }

    fun carteSd(context: Context): VolumeInfo? =
        volumes(context).firstOrNull { it.amovible }

    fun accesFichiers(): Boolean = Environment.isExternalStorageManager()

    fun demanderAccesFichiers(context: Context) {
        val cible = Intent(
            Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION,
            Uri.parse("package:" + context.packageName)
        )
        cible.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        try {
            context.startActivity(cible)
        } catch (e: Exception) {
            val secours = Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION)
            secours.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            try { context.startActivity(secours) } catch (e2: Exception) { }
        }
    }

    fun accesStatistiques(context: Context): Boolean {
        return try {
            val ops = context.getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
            val mode = ops.unsafeCheckOpNoThrow(
                AppOpsManager.OPSTR_GET_USAGE_STATS,
                Process.myUid(),
                context.packageName
            )
            mode == AppOpsManager.MODE_ALLOWED
        } catch (e: Exception) {
            false
        }
    }

    fun demanderAccesStatistiques(context: Context) {
        val cible = Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS)
        cible.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        try { context.startActivity(cible) } catch (e: Exception) { }
    }

    fun ouvrirOptionsDeveloppeur(context: Context) {
        val cible = Intent(Settings.ACTION_APPLICATION_DEVELOPMENT_SETTINGS)
        cible.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        try { context.startActivity(cible) } catch (e: Exception) { }
    }

    fun ouvrirFicheApplication(context: Context, paquet: String) {
        val cible = Intent(
            Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
            Uri.fromParts("package", paquet, null)
        )
        cible.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        try { context.startActivity(cible) } catch (e: Exception) { }
    }
}

fun taille(octets: Long): String {
    if (octets < 1024L) return "$octets o"
    val unites = arrayOf("Ko", "Mo", "Go", "To")
    var valeur = octets.toDouble() / 1024.0
    var i = 0
    while (valeur >= 1024.0 && i < unites.size - 1) {
        valeur /= 1024.0
        i++
    }
    val format = if (valeur >= 100.0) DecimalFormat("#") else DecimalFormat("#.#")
    return format.format(valeur) + " " + unites[i]
}
