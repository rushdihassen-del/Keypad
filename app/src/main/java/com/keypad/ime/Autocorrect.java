package com.keypad.ime;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Fixes obvious mistypes using a fixed British English word list that ships inside the app.
 * The list is read once and never changes: nothing typed is ever added to it, saved or sent.
 * A word is only changed when it is not in the list and exactly one slip away from a listed word:
 * a neighbouring key, two letters swapped, a doubled letter, or a missing apostrophe.
 */
final class Autocorrect {
    private static final String[] KEY_ROWS = {"qwertyuiop", "asdfghjkl", "zxcvbnm"};
    private static final int MIN_LENGTH = 3;
    private static final int MIN_LENGTH_SUBSTITUTE = 4;

    private final Map<String, Integer> rank = new HashMap<>();
    private final Map<String, String> withoutApostrophe = new HashMap<>();
    private final Map<Character, String> neighbours = new HashMap<>();

    /** Words separated by spaces or new lines, most common first. */
    Autocorrect(String wordList) {
        String[] words = wordList.split("\\s+");
        for (int i = 0; i < words.length; i++) {
            String w = words[i].toLowerCase(Locale.ROOT);
            if (w.isEmpty() || rank.containsKey(w)) {
                continue;
            }
            rank.put(w, i);
            if (w.indexOf('\'') >= 0) {
                String plain = w.replace("'", "");
                if (!withoutApostrophe.containsKey(plain)) {
                    withoutApostrophe.put(plain, w);
                }
            }
        }
        buildNeighbours();
    }

    boolean knows(String word) {
        return rank.containsKey(word.toLowerCase(Locale.ROOT));
    }

    /** The corrected word, or null to leave it alone. Keeps a leading capital. */
    String correct(String typed) {
        if (typed.length() < MIN_LENGTH) {
            return null;
        }
        String lower = typed.toLowerCase(Locale.ROOT);
        if (typed.length() > 1 && typed.equals(typed.toUpperCase(Locale.ROOT))) {
            return null; // looks like an abbreviation
        }
        if (!typed.substring(1).equals(lower.substring(1))) {
            return null; // capitals in the middle: a name or code, leave it
        }
        if (rank.containsKey(lower) || lower.charAt(0) == '\'' || lower.endsWith("'")) {
            return null;
        }

        String best = null;
        int bestRank = Integer.MAX_VALUE;

        String stripped = withoutApostrophe.get(lower);
        if (stripped != null) {
            best = stripped;
            bestRank = rank.get(stripped);
        }

        char[] c = lower.toCharArray();
        for (int i = 0; i + 1 < c.length; i++) {
            if (c[i] != c[i + 1]) {
                char t = c[i];
                c[i] = c[i + 1];
                c[i + 1] = t;
                String cand = new String(c);
                Integer r = rank.get(cand);
                if (r != null && r < bestRank) {
                    best = cand;
                    bestRank = r;
                }
                c[i + 1] = c[i];
                c[i] = t;
            }
        }

        if (lower.length() >= MIN_LENGTH_SUBSTITUTE) {
            for (int i = 0; i < c.length; i++) {
                String near = neighbours.get(c[i]);
                if (near == null) {
                    continue;
                }
                char original = c[i];
                for (int n = 0; n < near.length(); n++) {
                    c[i] = near.charAt(n);
                    String cand = new String(c);
                    Integer r = rank.get(cand);
                    if (r != null && r < bestRank) {
                        best = cand;
                        bestRank = r;
                    }
                }
                c[i] = original;
            }
            for (int i = 0; i + 1 < c.length; i++) {
                if (c[i] == c[i + 1]) {
                    String cand = lower.substring(0, i) + lower.substring(i + 1);
                    Integer r = rank.get(cand);
                    if (r != null && r < bestRank) {
                        best = cand;
                        bestRank = r;
                    }
                }
            }
        }

        if (best == null) {
            return null;
        }
        if (Character.isUpperCase(typed.charAt(0))) {
            best = best.substring(0, 1).toUpperCase(Locale.ROOT) + best.substring(1);
        }
        return best;
    }

    /** Keys touching each letter on a standard QWERTY keyboard, diagonals included. */
    private void buildNeighbours() {
        for (int r = 0; r < KEY_ROWS.length; r++) {
            for (int col = 0; col < KEY_ROWS[r].length(); col++) {
                StringBuilder sb = new StringBuilder();
                add(sb, r, col - 1);
                add(sb, r, col + 1);
                add(sb, r - 1, col);
                add(sb, r - 1, col + 1);
                add(sb, r + 1, col - 1);
                add(sb, r + 1, col);
                neighbours.put(KEY_ROWS[r].charAt(col), sb.toString());
            }
        }
    }

    private void add(StringBuilder sb, int row, int col) {
        if (row >= 0 && row < KEY_ROWS.length && col >= 0 && col < KEY_ROWS[row].length()) {
            sb.append(KEY_ROWS[row].charAt(col));
        }
    }
}
