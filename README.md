# SD Organizer

Application Android qui aide à libérer la mémoire interne d'un téléphone
équipé d'une carte SD.

## Ce qu'elle fait

- **Accueil** : deux anneaux, mémoire interne et carte SD, avec l'espace libre
  au centre. Le pétrole désigne l'interne, l'or désigne la carte ; ce code
  couleur est repris partout.
- **Applications** : toutes les applications triées par poids, avec la mention
  de celles qui acceptent la carte SD. Un appui ouvre la fiche Paramètres de
  l'application, seul endroit où Android autorise le déplacement.
- **Fichiers** : les dossiers publics de la mémoire interne (DCIM, Images,
  Vidéos, Téléchargements, Musique, Podcasts, Livres audio, Documents) avec
  leur taille. Sélection, puis déplacement réel vers la carte, fichier par
  fichier, avec vérification de la taille avant suppression de la source.

## Ce qu'elle ne fait pas, et pourquoi

Aucune application tierce ne peut déplacer une autre application vers la carte
SD. L'API n'existe pas : `pm move-package` est réservé au shell et au système.
SD Organizer repère donc les applications éligibles et conduit au bon écran
des Paramètres, sans prétendre faire le déplacement lui-même.

## Autorisations

- `MANAGE_EXTERNAL_STORAGE` : indispensable pour lire la mémoire interne et
  écrire sur la carte SD. Sans elle, la carte n'est même pas mesurable.
- `QUERY_ALL_PACKAGES` : lister les applications installées depuis Android 11.
- `PACKAGE_USAGE_STATS` : facultative. Sans elle, la taille affichée est celle
  des fichiers d'installation ; avec elle, données et cache sont comptés.

## Compilation

Kotlin, Jetpack Compose, Material 3. `minSdk` 30, `compileSdk` 34.
La compilation se fait sur GitHub Actions (`.github/workflows/build.yml`) :
Gradle 8.9, JDK 17, artefact `sd-organizer-debug`.
