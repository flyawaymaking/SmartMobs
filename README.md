# SmartMobs - Intelligent Enhanced Mob System

SmartMobs is an innovative Minecraft plugin that transforms ordinary hostile mobs into challenging opponents while preserving the game's vanilla aesthetic. The plugin not only enhances mob stats but also adds unique abilities to some of them, making every encounter a unique challenge.

* Русский перевод конфига расположен [ЗДЕСЬ](/src/main/resources/ru_config.yml)

## 🧩 Version Compatibility

| **Plugin version** | **Supported Paper**  | **Java** |
|--------------------|----------------------|----------|
| `1.4.0+`           | `1.21.3` – `26.2`    | 25       |
| `1.3.1`            | `1.21.3` – `1.21.11` | 21       |

## 🌟 Key Features

### 🎯 Natural Mob Evolution
- Two development tiers: Hardened and Elite (chances configurable)
- Unique thematic names for each mob type
- Logical progression from simple mutations to advanced evolutionary forms
- Automatic difficulty scaling based on distance from world spawn

### ⚡ Smart Abilities
- **Skeletons**: increased firing speed, triple shot for elite variants
- **Creepers**: faster fuse time, charged form for elite variants
- **Spiders**: web traps, enhanced jumping, poisonous bites for elite variants
- **Endermen**: improved teleportation, water resistance
- And many more unique abilities for each mob type

### 🛠️ Full Customization
- Enable/disable individual mob types
- Configurable spawn probabilities
- Difficulty scaling settings
- Adjustable health, damage, and speed multipliers
- Custom names and colors for each mob
- Flexible special ability configuration

### 🎮 Preserving Vanilla Spirit
- Lore-friendly names that fit Minecraft's world
- Balanced difficulty rather than artificial difficulty spikes
- Seamless integration into the existing mob ecosystem
- Names displayed only when looked at

### 🚀 Performance Optimization
- Efficient mob processing on spawn
- Smart event filtering and early rejection
- Minimal impact on server performance

## 📋 Supported Mobs

The plugin covers all major hostile Minecraft mobs:

| Regular Mob             | Hardened Variant | Elite Variant     |
|-------------------------|------------------|-------------------|
| Zombie                  | Rotten Wanderer  | Corrupted Drifter |
| Skeleton                | Eternal Sentinel | Bone Echo         |
| Creeper                 | Scorching Guest  | Stormbreaker      |
| Spider                  | Silk Slayer      | Web Lord          |
| Enderman                | Abyssal Wanderer | Spatial Rift      |
| Witch                   | Swamp Brewmaster | Shadow Witch      |
| Phantom                 | Nightmare        | Ethereal Scream   |
| Blaze                   | Fire Spirit      | Infernal Core     |
| Ghast                   | Mist Ghost       | Heavenly Cry      |
| And 40+ more mob types! |                  |                   |

## 🚀 Installation

1. Download the **latest release** from the [Releases](../../releases) section
2. Place the `.jar` file into your server's `plugins/` folder
3. Restart the server
4. Configure settings in `plugins/SmartMobs/config.yml`
5. Use `/smartmobs reload` to apply changes

## ⚙️ Configuration

The plugin uses a flexible YAML configuration system. Main settings:

```yml
# SmartMobs Configuration
# Plugin messages
messages:
  # General messages
  no-permission: "<red>❌ You don't have permission to use this command."
  only-players: "<red>❌ Only players can spawn mobs."
  unknown-subcommand: "<red>❌ Unknown subcommand."

  # Spawn command
  spawn-usage: "<yellow>Usage: /smartmobs spawn <mob> <hardened|elite>"
  spawn-no-permission: "<red>❌ You don't have permission to spawn mobs."
  spawn-mob-disabled: "<red>❌ Mob <white>{mob}</white> is disabled in the config."
  spawn-unknown-mob: "<red>❌ Unknown mob type: <white>{mob}"
  spawn-not-living: "<red>❌ This type is not a living entity."
  spawn-unknown-variant: "<red>❌ Unknown variant: <white>{variant}"
  spawn-success: "<green>✔ Spawned <yellow>{variant}</yellow> <gray>{mob}</gray>."

  # Reload command
  reload-no-permission: "<red>❌ You don't have permission to reload the plugin."
  reload-success: "<green>✔ SmartMobs config reloaded."

# Spawn chances for different mob types
chances:
  hardened: 0.05 # Base chance for hardened mobs to spawn
  elite: 0.01 # Base chance for elite mobs to spawn
  radius-complication: true # Whether mob difficulty should scale with distance from spawn
  world-radius: 10000 # World radius up to which difficulty scaling applies
  radius-levels:
    - from: 0.0 # zone start (0 = spawn)
      to: 0.33 # zone end (0.33 = one-third of world-radius)
      hardened: 0.5 # hardened chance at zone end
      elite: 0.05 # elite chance at zone end
    - from: 0.33
      to: 0.66
      hardened: 0.5
      elite: 0.5
    - from: 0.66
      to: 1.0
      hardened: 0.2
      elite: 0.8

# Enable/disable modifications for each mob
enabled-mobs:
  # OVERWORLD - Neutral & Passive
  bee: true
  iron_golem: true
  rabbit: true
  snow_golem: true
  wolf: true
  # ... and so on for all mobs

# Base stats for hardened mobs
hardened:
  hp-multiplier: 1.25
  damage-multiplier: 1.25
  knockback-resistance: 0.5
  name-visible: true

  # Custom display names per mob type
  display-names:
    # Overworld - neutral
    bee: "<yellow>Buzzing Slayer"
    iron_golem: "<gray>Steel Guardian"
    rabbit: "<gold>Killer Rabbit"
    snow_golem: "<white>Frost Golem"
    wolf: "<gray>Lunar Predator"

# Base stats for elite mobs
elite:
  hp-multiplier: 1.5
  damage-multiplier: 1.5
  speed-multiplier: 1.4
  knockback-resistance: 0.8
  name-visible: true
  strength:
    enabled: true
    level: 0

  # Custom display names per mob type
  display-names:
    # Overworld - neutral
    bee: "<red>Venomous Hive Threat"
    iron_golem: "<dark_gray>Iron Colossus"
    rabbit: "<red>Elite Killer Rabbit"
    snow_golem: "<white>Ice Colossus"
    wolf: "<dark_gray>Shadow Wolf"

# Special abilities for specific mobs
special-abilities:
  rabbit:
    hardened:
      killer: true
    elite:
      killer: true
      jump-strength: 1.5

  skeleton:
    hardened:
      arrow-speed-multiplier: 1.4
      attack-speed: 1.4
    elite:
      arrow-speed-multiplier: 1.8
      attack-speed: 1.8
      triple-shot: true
    # ... and so on for other mobs
```

## 🎮 Commands

- `/smartmobs` - show plugin help
- `/smartmobs reload` - reload configuration (requires `smartmobs.reload` permission)
- `/smartmobs spawn <mob> <hardened|elite>` - manually spawn an enhanced mob (requires `smartmobs.spawn` permission)

## 🔧 Development

The plugin is written in Java using the Bukkit/Paper API. Main classes:

- **SmartMobs** - main plugin class
- **ConfigManager** - configuration management
- **MobManager** - mob enhancement logic
- **MobSpawnListener** - mob spawn handler
- **SmartMobsCommand** - command handler

## 📝 Implementation Details

- **Automatic config generation** - full config with all settings is created on first run
- **Optimized processing** - checks for enabled mobs are performed before any enhancement
- **Dynamic difficulty scaling** - mob difficulty increases with distance from spawn

## 🤝 Contributing

We welcome contributions to the plugin's development! If you'd like to suggest improvements or fix bugs:

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/AmazingFeature`)
3. Commit your changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to the branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

## 📄 License

This project is distributed under the MIT License. See the LICENSE file for details.

## 🐛 Bug Reports

If you find a bug, please create an issue in the project repository with a detailed description:
- Minecraft version
- Paper version
- Plugin version
- Steps to reproduce the bug
- Error logs (if available)
