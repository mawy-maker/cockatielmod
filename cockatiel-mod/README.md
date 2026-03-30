# 🦜 Cockatiel Mod — NeoForge 1.21.1

Un mod Minecraft qui ajoute les calopsittes (cockatiels) avec plein de features !

## Features
- **3 variantes** : Grise, Lutino (jaune), Albino (blanche)
- **Apprivoisement** au millet
- **Chant matinal** à l'aube
- **Alerte Creeper** — l'oiseau crie si un Creeper est proche
- **Vol de cultures** si l'oiseau n'est pas nourri
- **Sifflet** pour rappeler l'oiseau jusqu'à 200 blocs
- **Cage décorative** et **Perchoir**
- **Élixir de Calopsitte** (Saut II + Vitesse + Chute lente + Régén)

## Build local (sans GitHub)

### Prérequis
- **Java 21** (JDK, pas JRE) — [Adoptium Temurin](https://adoptium.net/)
- **Git** (optionnel, pour cloner)

### Étapes

```bash
# 1. Cloner ou décompresser le projet
git clone https://github.com/TON_PSEUDO/cockatiel-mod.git
cd cockatiel-mod

# 2. (Linux/Mac) Rendre gradlew exécutable
chmod +x gradlew

# 3. Build
./gradlew build          # Linux / Mac
gradlew.bat build        # Windows

# 4. Récupérer le JAR
# Le fichier se trouve dans :  build/libs/cockatiel-1.0.0.jar
```

> ⏱ Le premier build télécharge NeoForge (~500 Mo) — prévoir 5-10 min.

### Installer le JAR dans Minecraft
1. Ouvre ton launcher Minecraft avec **NeoForge 1.21.1** installé
2. Copie `build/libs/cockatiel-1.0.0.jar` dans ton dossier `mods/`
3. Lance le jeu !

---

## Build automatique via GitHub Actions

À chaque `push` sur `main`, GitHub compile automatiquement le mod.

### Récupérer le JAR depuis GitHub
1. Va sur ton repo → onglet **Actions**
2. Clique sur le dernier workflow réussi ✅
3. En bas de la page → **Artifacts** → télécharge `cockatiel-mod-jar`

### Publier une release avec le JAR
```bash
git tag v1.0.0
git push origin v1.0.0
```
→ GitHub Actions crée automatiquement une Release avec le JAR en pièce jointe.

---

## Structure du projet

```
cockatiel-mod/
├── build.gradle
├── gradle.properties          ← version du mod ici
├── settings.gradle
├── .github/workflows/build.yml
└── src/main/
    ├── java/com/cockatielmod/cockatiel/
    │   ├── CockatielMod.java
    │   ├── CockatielConfig.java
    │   ├── entity/CockatielEntity.java
    │   ├── entity/ai/             ← comportements IA
    │   ├── block/                 ← Perchoir, Cage
    │   ├── blockentity/
    │   ├── item/                  ← Sifflet, Potion, Millet...
    │   ├── client/                ← Modèle 3D + Renderer
    │   └── registry/              ← Enregistrements NeoForge
    └── resources/
        ├── META-INF/neoforge.mods.toml
        └── assets/cockatiel/
            ├── lang/              ← fr_fr.json + en_us.json
            ├── models/
            ├── textures/          ← PNG à remplacer par de l'art final
            └── sounds.json        ← Sons à ajouter dans ce dossier
```

## Configuration (`config/cockatiel-common.toml`)
| Paramètre | Défaut | Description |
|-----------|--------|-------------|
| `hungerThresholdTicks` | 24000 | Ticks avant vol de cultures (1 jour = 24000) |
| `creeperAlertRange` | 16 | Portée de détection Creeper (blocs) |
| `whistleRange` | 200 | Portée max du sifflet (blocs) |
| `cropStealChance` | 0.05 | Probabilité de vol de culture par tick |
| `songDurationTicks` | 100 | Durée du chant matinal |

## Sons
Placer les fichiers `.ogg` dans :
`src/main/resources/assets/cockatiel/sounds/`

| Fichier | Événement |
|---------|-----------|
| `cockatiel_ambient1.ogg` | Gazouillis aléatoire |
| `cockatiel_ambient2.ogg` | Gazouillis aléatoire (variante) |
| `cockatiel_hurt.ogg` | Blessure |
| `cockatiel_death.ogg` | Mort |
| `cockatiel_song.ogg` | Chant du matin |
| `cockatiel_alert.ogg` | Alerte Creeper |
| `cockatiel_eat.ogg` | Manger |
| `cockatiel_happy.ogg` | Contente |
| `whistle_blow.ogg` | Coup de sifflet |

## Crafts

| Résultat | Ingrédients |
|----------|-------------|
| Graines de Millet ×4 | Wheat Seeds + Sugar |
| Grappe de Millet | 3 Graines de Millet (colonne) |
| Sifflet | Lingot d'or (haut) + Bambou (bas) |
| Perchoir | Planches + Bâtons (forme T) |
| Cage | 8 × Iron Bars (carré vide) |
| Friandise ×4 | Millet + Pomme + Honey Bottle |
