# Egg Badges 0.5.0

Egg Badges adds configurable visual indicators to visible Cobblemon eggs in Minecraft
1.21.1.

This is the first public release of the 0.5 series. It follows the 0.2.4 public release.

Egg Badges is an independent project and is not an official Cobblemon Town or Cobblemon
release.

## Features

- Full-slot background for perfect 6x31-IV eggs.
- Gold, Cyan, or White circle for 5x31 and custom IV filters.
- Compact perfect-IV count.
- Independent gender indicator.
- Confirmed Hidden Ability marker.
- In-game settings with preview and installed-version footer.

## Installation

1. Install Fabric Loader and Fabric API for Minecraft 1.21.1.
2. Download one Egg Badges JAR from the GitHub release.
3. Place it in the client instance's `mods` folder.
4. Start Minecraft and open an inventory containing eggs.

Install only one Egg Badges JAR.

The release includes:

| Asset | Purpose |
| --- | --- |
| `egg-badges-0.5.0.jar` | Versioned runtime JAR |
| `egg-badges.jar` | Permanent stable-download alias |
| `SHA256SUMS.txt` | Checksums for both JARs |

Both JARs are byte-identical. SHA-256:

```text
123ddd0d519b0a2053f0c535b4bb079a5ad011c9de1221b75449d184784137bc
```

## Requirements

- Minecraft 1.21.1
- Fabric Loader 0.18.0 or newer
- Fabric API 0.116.7+1.21.1 or newer
- Java 21 or newer
- Cobblemon 1.8.2 client
- Client-side installation only

No server installation is required. Hidden Ability indicators require local Cobblemon
species data.

## Compatibility and privacy

- Client-only.
- No network requests, telemetry, account identifiers, or uploads.
- No commands, packet hooks, click interception, tooltip changes, or inventory mutation.
- Egg information is read from the visible item name and lore. Missing or unrecognized
  data is not guessed.
- Backgrounds render on standard handled inventory screens. Some custom server GUIs may
  render backgrounds differently.

## Upgrade note

Settings from earlier Egg Badges versions migrate automatically. A backup of the
previous configuration is written before migration.

## Documentation

- [Settings guide](../docs/settings-guide.md)
- [Changelog](../CHANGELOG.md)
- [Issues](https://github.com/SwampDad/egg-badges/issues)
