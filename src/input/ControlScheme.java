package input;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;

public enum ControlScheme {
    CLASSIC("CLASSIC", "A/D OR ARROWS: ROTATE  |  W / UP: THRUST  |  SPACE: SHOOT  |  SHIFT: DASH"),
    DIRECT("DIRECT 8-WAY", "WASD / ARROWS: MOVE & AIM  |  SPACE: SHOOT  |  SHIFT: DASH"),
    MOUSE_AIM("MOUSE AIM", "WASD / ARROWS: MOVE  |  MOUSE: AIM  |  L-CLICK: SHOOT  |  R-CLICK / SHIFT: DASH");

    private final String label;
    private final String hint;

    ControlScheme(String label, String hint) {
        this.label = label;
        this.hint = hint;
    }

    public String getLabel() {
        return label;
    }

    public String getHint() {
        return hint;
    }

    public ControlScheme next() {
        ControlScheme[] vals = values();
        return vals[(ordinal() + 1) % vals.length];
    }

    public static ControlScheme fromName(String name) {
        if (name != null) {
            for (ControlScheme cs : values()) {
                if (cs.name().equalsIgnoreCase(name.trim())) {
                    return cs;
                }
            }
        }
        return CLASSIC;
    }

    private static File defaultPrefsFile() {
        return new File(System.getProperty("user.home"), ".space_ships_blaster_prefs.txt");
    }

    public static ControlScheme loadSaved() {
        return loadSaved(defaultPrefsFile());
    }

    public static ControlScheme loadSaved(File file) {
        if (file == null || !file.isFile()) {
            return CLASSIC;
        }
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (line.startsWith("controls=")) {
                    return fromName(line.substring("controls=".length()));
                }
            }
        } catch (Exception ignored) {
        }
        return CLASSIC;
    }

    public void save() {
        save(defaultPrefsFile());
    }

    public void save(File file) {
        if (file == null) return;
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(file))) {
            bw.write("controls=" + name());
            bw.newLine();
        } catch (Exception ignored) {
        }
    }
}
