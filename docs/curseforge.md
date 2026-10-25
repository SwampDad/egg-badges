# CurseForge listing material: Egg Badges

## One-line summary

See perfect-IV, 5x31, gender, and confirmed Hidden Ability information on Cobblemon eggs
at a glance.

## Project description

**Make useful Cobblemon egg information visible without opening every tooltip.**

Egg Badges adds compact, configurable indicators to eggs in standard handled inventory
screens. It is a client-side presentation mod and does not change egg data, breeding,
inventories, or server gameplay.

This is the first public release of the 0.5 series.

### Features

- Full-slot background for perfect 6x31-IV eggs.
- Gold, Cyan, or White circle for 5x31 and custom IV filters.
- Compact perfect-IV count.
- Independent gender indicator.
- Confirmed Hidden Ability marker.
- In-game settings screen with live preview.

### Requirements

- Minecraft 1.21.1
- Fabric Loader 0.18.0 or newer
- Fabric API 0.116.7+1.21.1 or newer
- Java 21 or newer
- A Cobblemon 1.8.2 client
- Client-side installation only

### Installation

Install Fabric Loader and Fabric API for Minecraft 1.21.1, place the Egg Badges JAR in
the client `mods` folder, and start Minecraft. Open **Options**, then **Egg Badges** to
configure the display.

### Compatibility and limitations

- Client-only; no server installation is required.
- No network requests, telemetry, or inventory mutation.
- Egg information comes from the visible item name and lore. If the expected labels
  are unavailable, badges are omitted rather than guessed.
- Hidden Ability indicators require local Cobblemon species data.
- Backgrounds render on standard handled inventory screens; some custom server GUIs
  may render backgrounds differently.

## CurseForge form data

| Field | Value |
| --- | --- |
| Public project name | Egg Badges |
| Publisher | SwampDad |
| Class | Mods |
| Main category | Cosmetic |
| Additional category | Utility & QoL |
| License | MIT |
| GitHub | https://github.com/SwampDad/egg-badges |
| Minecraft version | 1.21.1 |
| Mod loader | Fabric |
| Environment | Client only |
| Project logo | Original PNG, 400x400 or larger |
| Screenshots | Final in-game captures |

## Candidate file

- Version: `0.5.0`
- Runtime filename: `egg-badges-0.5.0.jar`
- Stable alias: `egg-badges.jar`
- Release type: stable
