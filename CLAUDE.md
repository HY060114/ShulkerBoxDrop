# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Build & Test

```bash
./gradlew build          # Compile and package the plugin JAR
./gradlew clean          # Clean build artifacts
```

There are no automated tests in this project.

## Architecture

This is a Spigot 1.20 plugin (Java 17). The plugin duplicates a shulker box (including its contents) when a player has broken a configurable number of shulker boxes in a row.

**Entry point:** `ShulkerBoxDrop extends JavaPlugin` — registers event listeners and the command executor on enable.

**Core flow (`BlockBreakListener`):**
1. Player breaks a shulker box → increment per-player (UUID-keyed) counter.
2. When counter reaches the configured threshold (`shulkerbox-break-threshold`, default 10), the block's `BlockState` (a `ShulkerBox`) is read, its inventory contents are copied onto a new `ItemStack` via `BlockStateMeta`, and the item is dropped naturally at the location.
3. Counter resets to 0 after a drop.
4. Whitelist mode (`whitelist-mode`): when enabled, the shulker box must contain *only* items listed in `allowed-items` for the duplication to trigger. Otherwise the counter resets without dropping.

**Per-player state cleanup:** Both `PlayerQuitListener` and `PlayerWorldChangeListener` clear a player's break counter from `BlockBreakListener` when they quit or change worlds (both are Java `record` listeners holding a reference to `BlockBreakListener`).

**Commands (`CommandHandler`):** `/shulkerboxdrop` (alias `/sbd`) uses two-level dispatch. Each category has its own permission node (all default `op` except `status` which defaults to `true`). Tab completion hides categories the sender lacks permission for; `/sbd help` also filters by permission.

| Category | Subcommand | Permission node |
|---|---|---|
| `plugin on\|off` | 开关插件 | `shulkerboxdrop.plugin` |
| `player disable\|enable <name>` | 禁用/恢复玩家复制权限 | `shulkerboxdrop.player` |
| `item add\|remove <id>` | 添加/移除白名单物品 | `shulkerboxdrop.item` |
| `whitelist on\|off` | 开关白名单模式 | `shulkerboxdrop.whitelist` |
| `threshold [<N>]` | 查看/设置挖掘阈值 | `shulkerboxdrop.threshold` |
| `status` | 查看完整状态（含禁用玩家 UUID） | `shulkerboxdrop.status` |
| `reload` | 重载配置文件 | `shulkerboxdrop.reload` |

**Config (`config.yml`):**
- `shulkerbox-break-threshold` (int, default 10)
- `whitelist-mode` (boolean, default false)
- `allowed-items` (list of material keys, e.g. `minecraft:oak_planks`)
- `disabled-players` (list of UUID strings, persisted across restarts)
