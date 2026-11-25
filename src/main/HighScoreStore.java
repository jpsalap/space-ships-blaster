package main;

import GameObjects.ShipStyle;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class HighScoreStore {
    public static final int MAX_ENTRIES = 5;
    public static final int MAX_NAME_LEN = 10;

    public static final class Entry implements Comparable<Entry> {
        public final String playerName;
        public final int score;
        public final int wave;
        public final String shipName;

        public Entry(String playerName, int score, int wave, String shipName) {
            this.playerName = sanitizeName(playerName);
            this.score = score;
            this.wave = wave;
            this.shipName = shipName;
        }

        @Override
        public int compareTo(Entry other) {
            if (other.score != this.score) {
                return Integer.compare(other.score, this.score);
            }
            return Integer.compare(other.wave, this.wave);
        }
    }

    private final File file;
    private final List<Entry> entries = new ArrayList<Entry>();
    private String lastPlayerName = "";

    public HighScoreStore() {
        this(new File(System.getProperty("user.home"), ".space_ships_blaster_scores.txt"));
    }

    public HighScoreStore(File file) {
        this.file = file;
        load();
    }

    public static String sanitizeName(String raw) {
        if (raw == null) {
            return "PILOT";
        }
        StringBuilder sb = new StringBuilder();
        String up = raw.trim().toUpperCase();
        for (int i = 0; i < up.length() && sb.length() < MAX_NAME_LEN; i++) {
            char ch = up.charAt(i);
            if ((ch >= 'A' && ch <= 'Z') || (ch >= '0' && ch <= '9') || ch == ' ' || ch == '-' || ch == '_') {
                sb.append(ch);
            }
        }
        String cleaned = sb.toString().trim();
        return cleaned.isEmpty() ? "PILOT" : cleaned;
    }

    private void load() {
        entries.clear();
        if (file == null || !file.isFile()) {
            return;
        }
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                String trimmed = line.trim();
                if (trimmed.isEmpty()) continue;
                String[] parts = trimmed.split(",");
                if (parts.length >= 3) {
                    int score = Integer.parseInt(parts[0].trim());
                    int wave = Integer.parseInt(parts[1].trim());
                    String ship = parts[2].trim();
                    String name = (parts.length >= 4) ? parts[3].trim() : "PILOT";
                    if (score > 0 && wave > 0) {
                        entries.add(new Entry(name, score, wave, ship));
                    }
                }
            }
            Collections.sort(entries);
            while (entries.size() > MAX_ENTRIES) {
                entries.remove(entries.size() - 1);
            }
            if (!entries.isEmpty()) {
                lastPlayerName = entries.get(0).playerName;
            }
        } catch (Exception ignored) {
            // Gracefully ignore unreadable/corrupt local score files
        }
    }

    private void save() {
        if (file == null) return;
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(file))) {
            for (Entry e : entries) {
                bw.write(e.score + "," + e.wave + "," + e.shipName + "," + e.playerName);
                bw.newLine();
            }
        } catch (Exception ignored) {
            // Ignore write errors in restricted environments
        }
    }

    public boolean qualifies(int score) {
        if (score <= 0) {
            return false;
        }
        if (entries.size() < MAX_ENTRIES) {
            return true;
        }
        return score > entries.get(entries.size() - 1).score;
    }

    public int record(int score, int wave, ShipStyle ship) {
        return record("PILOT", score, wave, ship);
    }

    /**
     * Records a finished run if score > 0 and returns its 1-based leaderboard rank (1..5), or 0 if not in top 5.
     */
    public int record(String playerName, int score, int wave, ShipStyle ship) {
        if (score <= 0) {
            return 0;
        }
        String cleanName = sanitizeName(playerName);
        this.lastPlayerName = cleanName;
        String shipName = (ship != null) ? ship.getDisplayName() : ShipStyle.BLUE.getDisplayName();
        Entry candidate = new Entry(cleanName, score, Math.max(1, wave), shipName);
        entries.add(candidate);
        Collections.sort(entries);
        int rank = entries.indexOf(candidate) + 1;
        while (entries.size() > MAX_ENTRIES) {
            entries.remove(entries.size() - 1);
        }
        if (rank <= MAX_ENTRIES) {
            save();
            return rank;
        }
        return 0;
    }

    public String getLastPlayerName() {
        return lastPlayerName;
    }

    public int getTopScore() {
        return entries.isEmpty() ? 0 : entries.get(0).score;
    }

    public List<Entry> getEntries() {
        return Collections.unmodifiableList(new ArrayList<Entry>(entries));
    }
}
