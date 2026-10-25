# Egg Badges Releases

The current stable release is Egg Badges 0.5.0, the first public release of the 0.5
series.

Download it from [GitHub Releases](https://github.com/SwampDad/egg-badges/releases).

## Current release: 0.5.0

- Minecraft 1.21.1
- Fabric Loader 0.18.0 or newer
- Fabric API 0.116.7+1.21.1 or newer
- Java 21 or newer
- Cobblemon 1.8.2 client
- Client-side installation only

### Download files

| File | Purpose |
| --- | --- |
| `egg-badges-0.5.0.jar` | Versioned download |
| `egg-badges.jar` | Permanent stable filename |

Both files contain the same build. Install only one.

[Download the stable JAR](https://github.com/SwampDad/egg-badges/releases/latest/download/egg-badges.jar)

## SHA-256

Both 0.5.0 JARs have this checksum:

```text
123ddd0d519b0a2053f0c535b4bb079a5ad011c9de1221b75449d184784137bc
```

The repository also contains [SHA256SUMS-v0.5.0.txt](SHA256SUMS-v0.5.0.txt) and
[SHA256SUMS.txt](SHA256SUMS.txt).

See [0.5.0 release notes](RELEASE-NOTES-v0.5.0.md).

## Previous stable release: 0.2.4

Egg Badges 0.2.4 was the previous stable release. Its original release assets remain
available.

- [0.2.4 checksum](SHA256SUMS-v0.2.4.txt)

## Build from source

Use Java 21 or newer.

```sh
./gradlew test
./gradlew build
```

Build outputs are generated in `build/libs/`. Release binaries are distributed through
GitHub Releases.
