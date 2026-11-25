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

    public static final class Entry implements Comparable<Entry> {
        public final int score;
        public final int wave;
        public final String shipName;

        public Entry(int score, int wave, String shipName) {
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

    public HighScoreStore() {
        this(new File(System.getProperty("user.home"), ".space_ships_blaster_scores.txt"));
    }

    public HighScoreStore(File file) {
        this.file = file;
        load();
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
                    if (score > 0 && wave > 0) {
                        entries.add(new Entry(score, wave, ship));
                    }
                }
            }
            Collections.sort(entries);
            while (entries.size() > MAX_ENTRIES) {
                entries.remove(entries.size() - 1);
            }
        } catch (Exception ignored) {
            // Gracefully ignore unreadable/corrupt local score files
        }
    }

    private void save() {
        if (file == null) return;
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(file))) {
            for (Entry e : entries) {
                bw.write(e.score + "," + e.wave + "," + e.shipName);
                bw.newLine();
            }
        } catch (Exception ignored) {
            // Ignore write errors in restricted environments
        }
    }

    /**
     * Records a finished run if score > 0 and returns its 1-based leaderboard rank (1..5), or 0 if not in top 5.
     */
    public int record(int score, int wave, ShipStyle ship) {
        if (score <= 0) {
            return 0;
        }
        String shipName = (ship != null) ? ship.getDisplayName() : ShipStyle.BLUE.getDisplayName();
        Entry candidate = new Entry(score, Math.max(1, wave), shipName);
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

    public int getTopScore() {
        return entries.isEmpty() ? 0 : entries.get(0).score;
    }

    public List<Entry> getEntries() {
        return Collections.unmodifiableList(new ArrayList<Entry>(entries));
    }
}
