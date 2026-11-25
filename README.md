# Space Ships Blaster

A classic Asteroids-inspired 2D arcade shooter written in pure Java (AWT / Swing) with zero external dependencies.

Originally built in **2018** as a freshman-year *Algorithms & Programming* final project in NetBeans, and restored & polished in **2026** to run cleanly on modern Java with a title screen, ship selector, cosmic nebula starfield, and retro arcade audio.

## Features

- **Title Screen & Ship Picker**: Choose between **Cyber Blue**, **Venom Green**, and **Crimson Red** ships—each with matching laser beams and HUD life icons.
- **Deep Space Visuals**: Pre-rendered cosmic nebula backdrop paired with a 3-layer twinkling parallax starfield that reacts to your ship's velocity.
- **Custom 60 FPS Game Loop**: Triple-buffered `java.awt.Canvas` (`BufferStrategy`), `Graphics2D` affine transforms, flickering twin thruster flames, centered explosions, glowing spark particles, floating score popups, and subtle screen shake.
- **Escalating Asteroid Waves**: 4 asteroid sizes (`BIG` → `MED` → `SMALL` → `TINY`) with size-scaled points, smooth edge wrap-around, and fading wave banners.
- **Retro Arcade Audio**: Cohesive synthesized SFX (pooled laser & explosion clips so rapid sounds overlap cleanly, click-free thruster loop, player hit, wave chime, game-over synth, and looping synthwave space track) plus a mute toggle (`M`). The original 2018 Reason `.wav` recordings are preserved in `recursos/Sounds/archive_2018/`.
- **Pause & Game Over Flows**: Pause anytime (`P` / `Esc`), session High Score tracking, and quick restart or return to the ship picker.

## Controls

| Key | Action |
| --- | --- |
| `←` / `→` | Rotate ship *(or select ship on Title Screen)* |
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
│   ├── states/         # TitleScreen (ship picker) & Estado_juego (waves, HUD, pause, game over)
│   ├── GameObjects/    # Jugador, ShipStyle, Asteroid, Laser, Moving_obj, Constantes & Crono
│   ├── graphics/       # Starfield, Particle, Assets, Loader & centered Animacion
│   ├── input/          # Edge-triggered Keyboard listener
│   └── math/           # 2D vector math utilities (Vector2D)
├── recursos/
│   ├── naves/          # Blue, Green, and Red ship sprites
│   ├── lasers/         # Matching Blue, Green, and Red laser sprites
│   ├── vidas/          # Matching Blue, Green, and Red HUD life icons
│   ├── asteroids/      # Big, Medium, Small, and Tiny asteroid sprites
│   ├── efectos/        # Twin thruster flame sprites (fire01, fire04)
│   ├── explosion/      # 9-frame explosion animation
│   ├── fuente/         # kenvector_future.ttf arcade font
│   └── Sounds/         # Retro arcade WAVs (+ archive_2018/ for original Reason audio)
├── Space_ships.iml     # IntelliJ IDEA module configuration
└── nbproject/          # Original 2018 NetBeans project files
```
