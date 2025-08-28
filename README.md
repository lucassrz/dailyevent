# DailyEvent Plugin

A Minecraft plugin that automatically changes server seasons with unique effects.

## 🌟 New Features

- **🎯 Title Bar Messages** : Display season changes directly in the interface
- **🌍 Localization System** : Fully configurable messages and season names
- **⚙️ Advanced Configuration** : Complete text customization

## Available Seasons

### 🌙 **BLOOD** (Blood Season)
- **Mobs spawn in mass** with +30% health and damage buff
- **Increased mob loots** (x1.5) - 50% chance to double drops
- **All neutral mobs become aggressive** :
  - Endermen : Permanent strength effect
  - Zombie Pigmen : Angry
  - Wolves : Angry

### 🍽️ **FAMINE** (Famine Season)
- **Food nourishes 50% less** than normal
- **Saturation decreases faster** (70% of normal)
- **Farming bonus** : 30% chance that ores give 2 drops

### ⛈️ **STORM** (Storm Season)
- **Permanent storm and rain** on all worlds
- **More charged creepers** (30% chance)
- **More skeletons on horses** (20% chance)
- **Boosted fish and marine loots** (more tridents)

### 🌑 **TENEBRE** (Darkness Season)
- **Ultra dark night** with light blindness effect
- **Torches light less** (visual effect)
- **Spawner spawn x2** (50% chance of double spawn)

### 🎭 **ILLUSION** (Illusion Season)
- **Fake ores appear in caves** when a player is nearby (they drop nothing)
- **Random pushing sensation** at random moments
- **Bonus** : Real ores double their drops

### 😱 **PARANOIA** (Paranoia Season)
- **Mining ores shows 64 items** in inventory for a second before being corrected
- **Fake players (Herobrine NPCs)** appear randomly near players for 0.25 seconds with stressful in-game sounds
- **Bonus** : All players have double jump

## Configuration

The plugin configures automatically via `config.yml` :

```yaml
# DailyEvent Plugin Configuration

# Localization settings
messages:
  season_change: "§6New season: §e{season}"
  season_current: "§6Current season: §e{season}"

# Season names (customizable)
seasons:
  names:
    BLOOD: "Blood Season"
    FAMINE: "Famine Season"
    STORM: "Storm Season"
    TENEBRE: "Darkness Season"
    ILLUSION: "Illusion Season"
    PARANOIA: "Paranoia Season"
  enabled:
    - BLOOD
    - FAMINE
    - STORM
    - TENEBRE
    - ILLUSION
    - PARANOIA

# Season change time (in-game ticks)
# 0 = 6:00 AM, 6000 = 12:00 PM, 12000 = 6:00 PM, 18000 = 12:00 AM (midnight)
inGameChangeTime: 18000
```

## Commands

- **`/season`** - Shows current season
- **`/season set <season>`** - Manually changes season
- **`/season reload`** - Reloads localization configuration

## Advanced Features

### 🎯 Title Bar Messages
- **Automatic change** : Display of new season name in title bar
- **Configurable duration** : 10 seconds display with fade in/out
- **Secure fallback** : Fallback to chat if title bar fails

### 🌍 Configurable Localization System
- **Customizable messages** : Format notifications as you want
- **Season names** : Customize season names
- **Placeholders** : `{season}` variables in messages
- **Dynamic reloading** : `/season reload` to apply changes

### ⚙️ Flexible Configuration
- **Custom language** : Configure your own messages and names
- **Free formatting** : Use Minecraft color codes and formatting
- **Hot reloading** : Modify config without restarting server

## Language Customization

To customize the language, simply modify the `config.yml` file.

After modification, use `/season reload` to apply changes.

## Support

For any questions or issues, contact me.
