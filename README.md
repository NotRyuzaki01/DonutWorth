# PriceLoreInjector

**PriceLoreInjector** is a Minecraft Paper/Spigot plugin that dynamically injects price information into item lores without affecting stacking.  
It supports mined items, mob drops, and chest loot — making it ideal for economy-based servers.

## Dependency
Uses `ProtocolLib 5.3.0`
To run on Minecraft version 1.21.4+ `ProtocolLib 5.4.0` is required.

## Features
- **Dynamic price injection** into item lore (display price on hover).
- **No impact on stacking** — items still stack naturally.
- **Supports**:
  - Mined ores
  - Mob drops
  - Chest loot
- **Configurable prices** in `config.yml`.
- **Performance optimized** — does not modify persistent NBT unless necessary.

## Gallery
![image](https://github.com/user-attachments/assets/d6ef5de6-98da-4dbf-972b-4d6f7057a41f)
![image](https://github.com/user-attachments/assets/9a69117c-cb2a-4fb5-9e66-a1f974ca6e12)


## Installation
1. Download the latest PriceLoreInjector `.jar` file.
2. Place the `.jar` into your server's `plugins` folder.
3. Restart or reload your server.
4. Configure the prices inside the generated `config.yml`.

## Configuration
After first launch, a `config.yml` will be created where you can define item prices.  
Example:

```yaml
prices:
  DIAMOND: 500.0
  IRON_INGOT: 100.0
  GOLD_INGOT: 200.0
  EMERALD: 800.0
```
Use official Minecraft Material names (uppercase with underscores).

## How It Works
When a player mines, picks up, or receives an item, the plugin temporarily injects the price lore.
When items stack or move in inventory, stacking remains unaffected.
Prices are shown on hover but do not modify core item data permanently.

## Requirements
  - Minecraft Paper or Spigot server 1.18+ (recommended 1.20+)
  - Java 17+

## Permissions
No permissions required.
(Operators can reload the plugin manually if needed.)

## Advantages
Keeps inventories clean and stackable.
Great for survival, SMP, and economy servers.
Flexible for future expansion (e.g., sell shops, auction systems).

## License
This project is licensed under the MIT License.
