# Space Ships Blaster

A classic Asteroids-inspired 2D arcade shooter written in pure Java (AWT / Swing) with zero external dependencies.

Originally built in **2018** as a freshman-year *Algorithms & Programming* final project in NetBeans, and restored in **2026** to run cleanly in modern IntelliJ IDEA with critical crash fixes and a playable Game Over screen.

## Features

- **Custom 60 FPS Game Loop** using a triple-buffered `java.awt.Canvas` (`BufferStrategy`) and `Graphics2D` affine transforms for smooth sprite rotation.
- **2D Vector Physics** (`math.Vector2D`) with angular steering, thruster acceleration, inertia/deceleration, and toroidal screen wrap-around.
- **Escalating Asteroid Waves** with 4 asteroid sizes (`BIG` → `MED` → `SMALL` → `TINY`) that split apart when shot.
- **Visual & Audio Feedback**: frame-by-frame explosion animations, thruster flame sprites, laser & thruster SFX, and looping space background music.
- **Lives, Respawn Invulnerability & Game Over Screen**: 3 lives with brief blinking invulnerability after respawning, plus a Game Over overlay displaying the final score with restart and quit options.

## Controls

| Key | Action |
| --- | --- |
| `←` / `→` | Rotate ship |
| `↑` | Thrust forward |
| `Space` | Fire laser *(also restarts on Game Over)* |
| `Enter` | Restart on Game Over |
| `Esc` | Quit from Game Over |

## Requirements

- **Java 8** (recommended) — or any JDK up to Java 25.
  > **Note:** Audio playback (`src/main/Sonidos.java`) uses `java.applet.AudioClip`, which was removed in JDK 26. Use JDK 8 (e.g., BellSoft Liberica 8 or Temurin 8) to compile and run as-is.

## How to Run

### IntelliJ IDEA
1. Clone the repository and open the folder in IntelliJ IDEA.
2. Go to **File → Project Structure → Project** and set the **SDK** to a **Java 8** JDK (`recursos/` is already marked as a Resources Root in `Space_ships.iml`).
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
│   ├── main/           # Entry point, JFrame window, game loop (ventana) & audio (Sonidos)
│   ├── states/         # Core gameplay state, waves, HUD, Game Over overlay (Estado_juego)
│   ├── GameObjects/    # Player ship (Jugador), Asteroid, Laser, physics base, constants & timers
│   ├── graphics/       # Asset loader (Loader, Assets) & frame animation (Animacion)
│   ├── input/          # Keyboard input listener (Keyboard)
│   └── math/           # 2D vector math utilities (Vector2D)
├── recursos/           # Sprites (ships, asteroids, lasers, explosions, HUD numbers), font & WAV audio
├── Space_ships.iml     # IntelliJ IDEA module configuration
└── nbproject/          # Original 2018 NetBeans project files
```

## Restoration Notes

When reviving the project after 8 years, the following minimal fixes were applied on top of the original codebase:
- Configured `recursos/` as a Resources Root in `Space_ships.iml` and restored classpath-relative sound paths (`/Sounds/*.wav`) in `Sonidos.java`.
- Fixed critical crashes: `ClassCastException` & broken spawn invulnerability in `Moving_obj`, keyboard `ArrayIndexOutOfBoundsException` on extended keys in `Keyboard`, startup thread race & `stop()` self-join deadlock in `ventana`, and `NumberFormatException` on negative lives in `Estado_juego`.
- Added a **Game Over** screen using the bundled `kenvector_future.ttf` font with **Enter / Space** to restart and **Esc** to quit.
