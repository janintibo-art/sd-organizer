package art.janintibo.sdorganizer

import android.app.usage.StorageStatsManager
import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageInfo
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.os.Process
import android.os.storage.StorageManager
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.util.concurrent.ConcurrentHashMap

enum class Emplacement { INTERNE, CARTE_SD }

enum class Deplacable {
    OUI,          // le developpeur autorise le stockage externe
    NON,          // verrouille en memoire interne
    SYSTEME       // application systeme, jamais deplacable
}

data class AppEntry(
    val paquet: String,
    val nom: String,
    val octets: Long,
    val emplacement: Emplacement,
    val deplacable: Deplacable
)

object Apps {

    suspend fun lire(context: Context, avecSysteme: Boolean): List<AppEntry> =
        withContext(Dispatchers.IO) {
            val pm = context.packageManager
            val stats = if (Storage.accesStatistiques(context)) {
                try {
                    context.getSystemService(Context.STORAGE_STATS_SERVICE) as StorageStatsManager
                } catch (e: Exception) {
                    null
                }
            } else null
            val utilisateur = Process.myUserHandle()
            val resultat = ArrayList<AppEntry>()

            val paquets = try {
                pm.getInstalledPackages(0)
            } catch (e: Exception) {
                emptyList<PackageInfo>()
            }

            for (pi in paquets) {
                val ai = pi.applicationInfo ?: continue
                val systeme = (ai.flags and ApplicationInfo.FLAG_SYSTEM) != 0
                if (systeme && !avecSysteme) continue

                var octets = 0L
                if (stats != null) {
                    octets = try {
                        val s = stats.queryStatsForPackage(
                            StorageManager.UUID_DEFAULT,
                            pi.packageName,
                            utilisateur
                        )
                        s.appBytes + s.dataBytes + s.cacheBytes
                    } catch (e: Exception) {
                        0L
                    }
                }
                if (octets <= 0L) octets = tailleApk(ai)

                val surSd = (ai.flags and ApplicationInfo.FLAG_EXTERNAL_STORAGE) != 0
                val deplacable = when {
                    systeme -> Deplacable.SYSTEME
                    pi.installLocation == PackageInfo.INSTALL_LOCATION_AUTO -> Deplacable.OUI
                    pi.installLocation == PackageInfo.INSTALL_LOCATION_PREFER_EXTERNAL -> Deplacable.OUI
                    else -> Deplacable.NON
                }

                val nom = try {
                    ai.loadLabel(pm).toString()
                } catch (e: Exception) {
                    pi.packageName
                }

                resultat.add(
                    AppEntry(
                        paquet = pi.packageName,
                        nom = nom,
                        octets = octets,
                        emplacement = if (surSd) Emplacement.CARTE_SD else Emplacement.INTERNE,
                        deplacable = deplacable
                    )
                )
            }
            resultat.sortedByDescending { it.octets }
        }

    private fun tailleApk(ai: ApplicationInfo): Long {
        var total = 0L
        try {
            total += File(ai.sourceDir).length()
        } catch (e: Exception) {
        }
        val morceaux = ai.splitSourceDirs
        if (morceaux != null) {
            for (m in morceaux) {
                try {
                    total += File(m).length()
                } catch (e: Exception) {
                }
            }
        }
        return total
    }
}

object Icones {

    private const val COTE = 108
    private val cache = ConcurrentHashMap<String, ImageBitmap>()

    suspend fun charger(context: Context, paquet: String): ImageBitmap? =
        withContext(Dispatchers.IO) {
            cache[paquet] ?: try {
                val dessin = context.packageManager.getApplicationIcon(paquet)
                val image = versBitmap(dessin).asImageBitmap()
                cache[paquet] = image
                image
            } catch (e: Exception) {
                null
            }
        }

    private fun versBitmap(dessin: Drawable): Bitmap {
        if (dessin is BitmapDrawable) {
            val source = dessin.bitmap
            if (source != null) {
                return Bitmap.createScaledBitmap(source, COTE, COTE, true)
            }
        }
        val bitmap = Bitmap.createBitmap(COTE, COTE, Bitmap.Config.ARGB_8888)
        val toile = Canvas(bitmap)
        dessin.setBounds(0, 0, COTE, COTE)
        dessin.draw(toile)
        return bitmap
    }
}
