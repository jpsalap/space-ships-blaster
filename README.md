# Space Ships Blaster

A classic Asteroids-inspired 2D arcade shooter written in pure Java (AWT / Swing) with zero external dependencies.

Originally built in **2018** as a freshman-year *Algorithms & Programming* final project in NetBeans, and restored & expanded in **2026** to run cleanly on modern Java with distinct playable starfighters, cosmic space visuals, enemy UFOs, power-ups, and ethereal ambient space audio.

## Features

- **Title Screen & Starfighter Picker**: Choose from **3 distinct starfighters**, each with its own hull design, matching laser color, matching HUD life icon, and flight characteristics:
  - **Interceptor (Blue)** — *Balanced* speed, agility, and fire rate.
  - **Viper (Green)** — *High Speed* & razor-sharp turning.
  - **Striker (Red)** — *Rapid Fire* twin-cannon gunship.
- **Deep Space Visuals & Dynamic Thrust**: Pre-rendered cosmic nebula backdrop, 3-layer twinkling parallax starfield, animated glowing plasma thruster jets with trailing exhaust sparks, centered explosions, glowing debris particles, floating score popups, and subtle camera shake.
- **Difficulty Progression**:
  - Escalating asteroid waves with gradual speed scaling and safe edge spawns away from the player.
  - **Enemy Alien UFOs** appear starting at **Wave 3+**, weaving across space and firing aimed plasma bolts (+150 pts when destroyed).
  - **Power-Up Drops**: Collect temporary **Shield** (`S`) and **Rapid Fire** (`R`) pickups with live HUD duration bars.
  - **Bonus Extra Lives**: Earn `+1` extra life every **1,500 points**.
- **Atmospheric Space Audio**: Ethereal, mysterious ambient deep-space room tone (`espacio.wav`), clearly audible click-free ion thruster loop (`propulsor.wav`), pooled SFX for lasers, explosions, UFO shots, power-ups, wave clears, extra lives, and Game Over—plus a mute toggle (`M`). Original 2018 Reason `.wav` files are archived in `recursos/Sounds/archive_2018/`.
- **Pause & Game Over Flows**: Pause anytime (`P` / `Esc`), session High Score tracking, instant restart, or quick return to the starfighter picker (`T`).

## Controls

| Key | Action |
| --- | --- |
| `←` / `→` | Rotate ship *(or select starfighter on Title Screen)* |
| `↑` | Thrust forward |
| `Space` | Fire laser *(or start / restart)* |
| `Enter` | Start game / Restart / Return to Title *(from Pause)* |
| `P` / `Esc` | Pause / Resume *(or Quit on Title / Game Over)* |
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
│   ├── main/           # Entry point, JFrame window, game loop (ventana) & pooled audio (Sonidos)
│   ├── states/         # TitleScreen (starfighter picker) & Estado_juego (waves, HUD, pause, game over)
│   ├── GameObjects/    # Jugador, ShipStyle, Ufo, PowerUp, Asteroid, Laser, Moving_obj, Constantes & Crono
│   ├── graphics/       # Starfield, Particle, Assets, Loader & centered Animacion
│   ├── input/          # Edge-triggered Keyboard listener
│   └── math/           # 2D vector math utilities (Vector2D)
├── recursos/
│   ├── naves/          # Distinct Blue, Green, Red starfighters, UFO sprite (+ original 2018 player.png)
│   ├── lasers/         # Matching Blue, Green, and Red laser sprites
│   ├── vidas/          # Matching Blue, Green, and Red HUD life icons
│   ├── asteroids/      # Big, Medium, Small, and Tiny asteroid sprites
│   ├── explosion/      # 9-frame explosion animation
│   ├── fuente/         # kenvector_future.ttf arcade font
│   └── Sounds/         # Ethereal ambient space & arcade SFX (+ archive_2018/ for original Reason audio)
├── Space_ships.iml     # IntelliJ IDEA module configuration
└── nbproject/          # Original 2018 NetBeans project files
```
