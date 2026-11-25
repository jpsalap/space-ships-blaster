# Space Ships Blaster

A classic Asteroids-inspired 2D arcade shooter written in pure Java (AWT / Swing) with zero external dependencies.

Originally built in **2018** as a freshman-year *Algorithms & Programming* final project in NetBeans, and restored & expanded in **2026** to run cleanly on modern Java with distinct playable starfighters, cosmic space visuals, warp dash, enemy UFOs, power-ups, combo scoring, persistent high scores, and ethereal ambient space audio.

## Features

- **Title Screen & Starfighter Picker**: Choose from **3 distinct 96×96 HD starfighters** (scaled to a nimble `44×44` in-game), each with its own hull silhouette, matching laser color, matching HUD life icon, and stats:
  - **Interceptor (Blue)** — *Balanced* speed, agility, and fire rate.
  - **Viper (Green)** — *High Speed* & razor-sharp turning.
  - **Striker (Red)** — *Rapid Fire* twin-cannon gunship.
- **Deep Space Visuals & Dynamic Thrust**: Pre-rendered cosmic nebula backdrop, 3-layer twinkling parallax starfield, animated glowing plasma thruster jets with curving exhaust trails, seamless edge ghost-wrapping, randomized asteroid spin, centered explosions, glowing debris particles, floating score popups, and subtle camera shake.
- **Warp Dash (`Shift`)**: Short-cooldown invulnerable boost with translucent afterimages and warp sparks for threading tight asteroid gaps.
- **Difficulty Progression & Combos**:
  - Escalating asteroid waves with gradual speed scaling and safe edge spawns away from the player.
  - **Score Combo Multiplier (`x2` / `x3` / `x4`)**: Chain rapid hits within ~2.2 seconds to rack up bonus multipliers.
  - **Enemy Alien UFOs** appear starting at **Wave 3+** with an eerie entrance cue, weaving across space and firing aimed plasma bolts (+150 base pts).
  - **Power-Up Drops**: Collect temporary **Shield** (`S`), **Rapid Fire** (`R`), and **Triple Spread Shot** (`3`) pickups with live HUD duration bars.
  - **Bonus Extra Lives**: Earn `+1` extra life every **1,500 points**.
- **Persistent Top-5 High Scores**: Saved locally to `~/.space_ships_blaster_scores.txt` (press `H` on the Title Screen or view automatically on Game Over).
- **Atmospheric Space Audio**: 28-second stereo ethereal deep-space room tone (`espacio.wav`), silky fade-in/out ion thruster whoosh (`propulsor.wav`), pooled SFX for lasers, explosions, dash, UFO entrance & shots, power-ups, wave clears, extra lives, and Game Over—plus a mute toggle (`M`). Original 2018 Reason `.wav` files are archived in `recursos/Sounds/archive_2018/`.

## Controls

| Key | Action |
| --- | --- |
| `←` / `→` or `A` / `D` | Rotate ship *(or select starfighter on Title Screen)* |
| `↑` or `W` | Thrust forward *(classic Asteroids flight — no reverse)* |
| `Space` | Fire laser *(or start / restart)* |
| `Shift` | Warp Dash *(short cooldown dodge burst)* |
| `Enter` | Start game / Restart / Return to Title *(from Pause)* |
| `P` / `Esc` | Pause / Resume *(or Quit on Title / Game Over)* |
| `H` | View Top 5 High Scores *(on Title Screen)* |
| `T` | Return to Title Screen *(from Game Over)* |
| `M` | Mute / Unmute audio |
| `F3` | Toggle FPS counter |

## Requirements

- **Java 8 or newer** (tested on JDK 8 and JDK 26) — zero external dependencies, built purely on the Java Standard Library (`java.awt`, `javax.swing`, `javax.imageio`, `javax.sound.sampled`).

## How to Run

### IntelliJ IDEA
1. Clone the repository and open the folder in IntelliJ IDEA.
2. Go to **File → Project Structure → Project** and set the **SDK** to any installed JDK 8+ (`recursos/` is already marked as a Resources Root in `Space_ships.iml`).
3. Run `main.ventana`.

### Command Line (macOS / Linux)
```bash
mkdir -p out/production/Space_ships
javac -encoding UTF-8 -d out/production/Space_ships $(find src -name '*.java')
cp -R recursos/. out/production/Space_ships/
java -cp out/production/Space_ships main.ventana
```

## Project Structure

```text
Space_ships/
├── src/
│   ├── main/           # Entry point, game loop (ventana), audio (Sonidos) & leaderboard (HighScoreStore)
│   ├── states/         # TitleScreen (starfighter picker & scores) & Estado_juego (waves, HUD, combos, pause)
│   ├── GameObjects/    # Jugador, ShipStyle, Ufo, PowerUp, Asteroid, Laser, Moving_obj, Constantes & Crono
│   ├── graphics/       # Starfield, Particle, Assets, Loader & centered Animacion
│   ├── input/          # Synchronized Keyboard listener (WASD/Arrows + focus-loss safety)
│   └── math/           # 2D vector math utilities (Vector2D)
├── recursos/
│   ├── naves/          # 96x96 HD Blue, Green, Red starfighters, UFO sprite (+ original 2018 player.png)
│   ├── lasers/         # Matching Blue, Green, and Red laser sprites
│   ├── vidas/          # Matching Blue, Green, and Red HUD life icons
│   ├── asteroids/      # Big, Medium, Small, and Tiny asteroid sprites
│   ├── explosion/      # 9-frame explosion animation
│   ├── fuente/         # kenvector_future.ttf arcade font
│   └── Sounds/         # Stereo ethereal space & arcade SFX (+ archive_2018/ for original Reason audio)
├── Space_ships.iml     # IntelliJ IDEA module configuration
└── nbproject/          # Original 2018 NetBeans project files
```
