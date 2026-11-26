# Changelog

All notable changes to **Space Ships Blaster** are documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/), and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

## [v1.1.2] - 2026-11-26

### Fixed
- **Retina / HiDPI Ship Sharpness**: Restored full `96×96` HD player starfighter crispness on 2× Retina displays by rendering the high-resolution ship sprite directly with bicubic filtering scoped strictly to the player ship, keeping fast bilinear rendering for asteroids, lasers, and dash afterimages.

## [v1.1.1] - 2026-11-26

### Changed
- **Major Combat Performance & CPU Optimization**: Eliminated FPS drops and lowered CPU usage during intense waves:
  - Pre-baked glowing visual effects (thruster bloom, shield bubble, power-ups, and enemy plasma bolts) and spark/debris particles into cached GPU-compatible sprites with a 200-particle cap (~4× faster heavy combat rendering).
  - Converted loaded sprites to display-compatible images and optimized the cosmic starfield backdrop to eliminate per-frame allocations.
  - Replaced heavy collision scans with lightweight dead-flag and primitive distance checks.
  - Decoupled game-logic catch-up from rendering, enabled native repaint control, and replaced busy-waiting with smart frame sleeping.
  - Optimized thruster audio fade updates and idle sound clip selection to prevent audio stutters on macOS.

## [v1.1.0] - 2026-11-26

### Added
- **3 Selectable Control Modes**: Press `C` on the Title Screen or inside the Pause Menu to switch between **Classic** (Asteroids rotation), **Direct 8-Way**, and **Mouse Aim** (twin-stick with a targeting crosshair, Left Click to shoot, Right Click to dash). Your preferred mode is saved automatically to `~/.space_ships_blaster_prefs.txt`.

### Fixed
- **Game Over `Esc` Behavior**: Pressing `Esc` to skip pilot name entry on Game Over no longer quits the app on the same frame.
- **Mouse Aim & Trackpad Responsiveness**: Right-clicking to dash no longer cancels held left-click firing, and quick sub-frame trackpad clicks or key taps are now reliably captured.
- **Focus & Cursor Polish**: Synced pointer state across mode switches, reclaimed canvas focus on click, and updated the targeting cursor safely on the UI thread.
- **Wrapped Edge Collisions**: Ship-vs-asteroid collisions across screen borders now use toroidal wrap-around distance (while keeping UFO hits non-wrapped).
- **Audio & Stability Hardening**: Trimmed audio clip pools for better system compatibility and clean shutdown, hardened render buffer recovery, and enforced UTF-8 for saved scores and preferences.

## [v1.0.1] - 2026-11-25

### Added
- **Intel Mac Standalone Build**: Added a dedicated **macOS Intel (x64)** `.dmg` standalone installer to GitHub Releases alongside Apple Silicon, Windows, Linux, and the Universal JAR.

## [v1.0.0] - 2026-11-25

### Added
- **Initial Standalone Release**: Automated multi-platform standalone builds (no Java required) for **macOS (Apple Silicon)**, **Windows (x64)**, and **Linux (x64)**, plus a universal **Java 8+ JAR** and custom cosmic starfighter app icons.
- **Title Screen & 3 Playable HD Starfighters**: Choose from the **Blue Interceptor** (Balanced), **Green Viper** (High Speed needle-dart), and **Red Striker** (Rapid Fire gunship), each with unique stats, matching lasers, and HUD life icons.
- **Core Arcade Gameplay & Polish**:
  - **Warp Dash (`Shift`)**: Brief invulnerable boost with afterimages and warp sparks (7.5s cooldown).
  - **Power-Ups & Enemy UFOs**: Temporary **Shield**, **Rapid Fire**, and **Triple Spread Shot** pickups, plus aimed-bolt alien UFOs starting at Wave 3+.
  - **Combos, Waves & Extra Lives**: `x2`/`x3`/`x4` combo multiplier, escalating wave speeds, randomized asteroid spin, seamless edge ghost-wrapping, and escalating extra-life milestones (capped at 5 lives).
  - **Persistent Top-5 High Scores**: Arcade pilot name entry on Game Over saved locally to `~/.space_ships_blaster_scores.txt` (press `H` on the Title Screen to view).
  - **Cosmic Visuals & Atmospheric Audio**: Pre-rendered nebula backdrop, 3-layer parallax starfield, dynamic plasma thruster plumes, stereo ethereal deep-space ambient soundtrack, ion thruster whoosh, pooled retro SFX, and mute toggle (`M`).
  - **Modern Java Compatibility**: Restored from the 2018 university project with `javax.sound.sampled` audio, thread-safe controls, Pause/Game Over screens, and full Java 8 through Java 26+ support.

[Unreleased]: https://github.com/jpsalap/space-ships-blaster/compare/v1.1.2...HEAD
[v1.1.2]: https://github.com/jpsalap/space-ships-blaster/compare/v1.1.1...v1.1.2
[v1.1.1]: https://github.com/jpsalap/space-ships-blaster/compare/v1.1.0...v1.1.1
[v1.1.0]: https://github.com/jpsalap/space-ships-blaster/compare/v1.0.1...v1.1.0
[v1.0.1]: https://github.com/jpsalap/space-ships-blaster/compare/v1.0.0...v1.0.1
[v1.0.0]: https://github.com/jpsalap/space-ships-blaster/releases/tag/v1.0.0
