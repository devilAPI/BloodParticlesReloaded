# Blood (BloodParticlesReloaded)

A super simple Spigot/Paper plugin that sprays blood particles (redstone block break particles) when an entity gets hit.

## Features

- Blood scales with the damage dealt: 5 particles per point of damage by default, capped per hit.
- Can be limited to players only, or show for all mobs.
- Lets you set the intensity, maximum particles per hit, spread and particle block.
- Skips cancelled hits (e.g. inside protected regions) and non-living entities like item frames.
- `/blood reload` reloads the config without restarting the server.

## Supported versions

Jars are built for Minecraft **1.16.5, 1.17.1, 1.18.2, 1.19.4, 1.20.4, 1.20.6, 1.21.4, 1.21.11 and 26.3**.
Grab the jar whose `mc` version matches (or is closest below) your server version.

| Server version | Java needed |
|----------------|-------------|
| 1.16.5         | 8+          |
| 1.17.x         | 16+         |
| 1.18 – 1.20.4  | 17+         |
| 1.20.5+        | 21+         |

## Download

- **Stable:** [Releases](https://github.com/devilAPI/BloodParticlesReloaded/releases/latest)
- **Dev builds:** every push creates a `dev-<branch>` pre-release on the [Releases page](https://github.com/devilAPI/BloodParticlesReloaded/releases).

## Configuration

`plugins/Blood/config.yml`:

```yaml
# true  = only bleed when a player gets hit
# false = bleed for every living entity (players and mobs)
players-only: false

# How much blood to spawn. 1.0 = default, 2.0 = twice as much, 0.5 = half.
intensity: 1.0

# Maximum number of particles spawned for a single hit.
max-particles: 50

# How far the particles spread out from the hit entity (in blocks).
spread: 0.4

# Block whose break particles are used as blood.
block: REDSTONE_BLOCK
```

## Commands & permissions

| Command         | Permission     | Default | Description              |
|-----------------|----------------|---------|--------------------------|
| `/blood reload` | `blood.reload` | op      | Reloads `config.yml`     |

## Building

Requires JDK 21 and Maven.

```bash
mvn package
```

To build for a specific Minecraft version:

```bash
mvn package -Dspigot.version=1.20.4 -Djava.release=17
```

The jar ends up in `target/Blood-<version>-mc<mc-version>.jar`.

## Releasing

- **Dev builds:** the `Dev Build` workflow runs on every push and publishes jars for all versions as a `dev-<branch>` pre-release.
- **Stable releases:** go to *Actions → Release → Run workflow* and enter a version (e.g. `1.1.0`). It builds every Minecraft version and publishes a `v<version>` release.

## Issues

Found a bug or have an idea? [Open an issue](https://github.com/devilAPI/BloodParticlesReloaded/issues).
