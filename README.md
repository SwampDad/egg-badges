# Egg Badges

Egg Badges is an independent client-side Fabric mod for Minecraft 1.21.1. It adds
configurable visual badges to visible Cobblemon eggs so useful breeding information is
easy to spot in an open inventory.

Egg Badges is not an official Cobblemon Town or Cobblemon project.

## What it shows

- A full-slot background for perfect 6x31-IV eggs.
- A Gold, Cyan, or White circle for 5x31 or custom IV filters.
- A compact count of perfect 31-IV stats.
- An independent gender corner indicator.
- An orange lower-right marker for a confirmed Hidden Ability.

## Settings

Open Minecraft **Options**, then choose **Egg Badges**. The screen does not pause the
world.

The settings are grouped into:

1. **PERFECT EGGS (6x31)** - optional background.
2. **ICON** - Colored Circle filters or Show # of 31 IVs.
3. **OTHER** - Show Gender and Show Hidden Ability.

Selecting `5x31` or `Custom IV Filter` activates the Colored Circle directly. There is
no separate circle activation checkbox. Selecting the active filter again clears it.
The circle and Show # Count are mutually exclusive.

## Requirements

- Minecraft 1.21.1
- Fabric Loader 0.18.0 or newer
- Fabric API 0.116.7+1.21.1 or newer
- Java 21 or newer
- A Cobblemon 1.8.2 client
- Client-side installation only

Hidden Ability indicators require the local Cobblemon client data. Unknown or
unavailable species, forms, abilities, or egg lore produce no marker rather than a
guess.

## Install

1. Install Fabric Loader and Fabric API for Minecraft 1.21.1.
2. Download one Egg Badges JAR from the
   [Releases page](https://github.com/SwampDad/egg-badges/releases).
3. Place the JAR in the client instance's `mods` folder.
4. Start Minecraft and open an inventory containing eggs.

Install only one Egg Badges JAR.

## Compatibility and privacy

- Client-only; the server does not need the mod.
- No network requests, telemetry, account identifiers, or uploads.
- No commands, packet hooks, click interception, tooltip changes, or inventory
  mutation.
- The mod reads the egg's visible item name and lore. If the expected labels are
  missing, badges are omitted rather than guessed.
- Backgrounds render on standard handled inventory screens. Some custom server GUIs
  may render backgrounds differently.

## Build from source

With JDK 21 or newer:

```sh
./gradlew test build
```

The remapped runtime JAR is produced under `build/libs/`. Do not distribute a `-dev`
JAR.

## License

Licensed under this repository's [MIT License](LICENSE).
