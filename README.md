# Blood (BloodParticlesReloaded)

A super simple Spigot/Paper plugin that sprays blood particles (redstone block break particles) when an entity gets hit.

## Features

- Blood scales with the damage dealt: 5 particles per point of damage by default, capped per hit.
- Can be limited to players only, or show for all mobs.
- Lets you set the intensity, maximum particles per hit, spread and particle block.
- Skips cancelled hits (e.g. inside protected regions) and non-living entities like item frames.
- Players can turn their own blood off with `/blood toggle`, and staff can toggle it for others.
- `blood.bypass` permission to disable blood for specific players or groups.
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
- **Dev builds:** every push to `master` publishes a numbered pre-release (`dev-1`, `dev-2`, …) on the [Releases page](https://github.com/devilAPI/BloodParticlesReloaded/releases).

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

| Command                  | Permission                     | Default  | Description                                  |
|--------------------------|--------------------------------|----------|----------------------------------------------|
| `/blood toggle`          | `blood.commands.toggle`        | everyone | Turn your own blood on/off                   |
| `/blood toggle <player>` | `blood.commands.toggle.others` | op       | Turn another online player's blood on/off    |
| `/blood reload`          | `blood.commands.reload`        | op       | Reload `config.yml`                          |

| Permission         | Default | Description                                         |
|--------------------|---------|-----------------------------------------------------|
| `blood.bypass`     | nobody  | Players with this permission never bleed            |
| `blood.commands.*` | op      | All of the command permissions above                |

The toggle state is saved on the player, so it survives relogs and restarts.
`blood.bypass` is not given to ops automatically; grant it with your permissions plugin.

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

- **Dev builds:** the `Dev Build` workflow runs on every push to `master` and publishes jars for all versions as the next `dev-X` pre-release (continuous numbering).
- **Stable releases:** go to *Actions → Release → Run workflow* and enter a version (e.g. `1.1.0`). It builds every Minecraft version and publishes a `v<version>` release.

## Issues

Found a bug or have an idea? [Open an issue](https://github.com/devilAPI/BloodParticlesReloaded/issues).
