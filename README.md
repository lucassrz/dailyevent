# DailyEvent Plugin

Un plugin Minecraft qui change automatiquement les saisons du serveur avec des effets uniques.

## Saisons Disponibles

### 🌙 **BLOOD** (Saison du Sang)
- **Mobs spawnent en masse** avec un buff de +30% vie et dégâts
- **Loots des mobs augmentés** (x1.5) - 50% de chance de doubler les drops
- **Tous les mobs neutres sont agressifs** :
  - Endermen : Effet de force permanent
  - Pigmen Zombie : En colère
  - Loups : En colère

### 🌃 **NOCTURNE** (Saison de la Nuit)
- **Nuits plus longues** (le temps reste bloqué la nuit)
- **Blood Moon possible** (15% de chance) :
  - Mobs avec effet de force et vitesse
  - Ambiance plus hostile

### 🍽️ **FAMINE** (Saison de la Famine)
- **Nourriture nourrit 50% moins** qu'en temps normal
- **Saturation baisse plus vite** (70% de la normale)
- **Bonus farming** : 30% de chance que les minerais donnent 2 drops

### ⛈️ **STORM** (Saison des Tempêtes)
- **Orage et pluie permanents** sur tous les mondes
- **Creepers chargés plus fréquents** (30% de chance)
- **Squelettes sur chevaux** plus fréquents (20% de chance)
- **Poissons et loots marins boostés** (plus de tridents)

### 🌑 **TENEBRE** (Saison des Ténèbres)
- **Nuit ultra sombre** avec effet de cécité léger
- **Torches éclairent moins** (effet visuel)
- **Spawn dans les spawners x2** (50% de chance de double spawn)

## Configuration

Le plugin se configure automatiquement via `config.yml` :

```yaml
# Saison actuelle
currentSeason: BLOOD

# Saisons activées
seasons:
  enabled:
    - BLOOD
    - NOCTURNE
    - FAMINE
    - STORM
    - TENEBRE

# Rotation des saisons
rotation:
  mode: MINUTES
  minutes: 1440  # 24 heures
```

## Installation

1. Placez le fichier `dailyevent-1.0.0.jar` dans le dossier `plugins/`
2. Redémarrez votre serveur
3. Le plugin se configurera automatiquement

## Commandes

- `/season` - Affiche la saison actuelle
- `/season <saison>` - Change manuellement la saison

## Compatibilité

- **Minecraft** : 1.17+
- **Spigot/Paper** : Oui
- **Bukkit** : Oui

## Support

Pour toute question ou problème, contactez le développeur.
