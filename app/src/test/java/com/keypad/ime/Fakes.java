package com.keypad.ime;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

/** Test helpers: a fake text box, fake saved settings, and ways to find and press keys. */
final class Fakes {
    private Fakes() {
    }

    static final class FakeEditor implements Editor {
        final StringBuilder text = new StringBuilder();
        int enters;
        int voice;
        int pickers;

        @Override
        public void commit(String s) {
            text.append(s);
        }

        @Override
        public void replace(int deleteBefore, String s) {
            text.setLength(text.length() - deleteBefore);
            text.append(s);
        }

        @Override
        public String textBefore(int count) {
            int start = Math.max(0, text.length() - count);
            return text.substring(start);
        }

        @Override
        public void backspace() {
            if (text.length() > 0) {
                text.deleteCharAt(text.length() - 1);
            }
        }

        @Override
        public void enter() {
            enters++;
        }

        @Override
        public void showKeyboardList() {
            pickers++;
        }

        @Override
        public void startVoiceTyping() {
            voice++;
        }

        String visible() {
            return text.toString();
        }
    }

    static final class FakeSettings implements SettingsStore {
        Scheme scheme = Scheme.YELLOW_ON_BLACK;
        int writes;

        @Override
        public Scheme getScheme() {
            return scheme;
        }

        @Override
        public void setScheme(Scheme s) {
            scheme = s;
            writes++;
        }
    }

    static Autocorrect words() {
        try {
            String list = new String(Files.readAllBytes(Paths.get("src/main/res/raw/words.txt")),
                    StandardCharsets.UTF_8);
            return new Autocorrect(list);
        } catch (IOException e) {
            throw new AssertionError(e);
        }
    }

    static KeyboardController controller(FakeEditor editor, FakeSettings settings) {
        return new KeyboardController(editor, settings, words());
    }

    static KeyboardController plainField(FakeEditor editor, FakeSettings settings) {
        KeyboardController c = controller(editor, settings);
        c.startField(false, true);
        return c;
    }

    static Key byLabel(KeyboardController c, String label) {
        return byLabel(c.getPage(), label);
    }

    static Key byLabel(Page page, String label) {
        for (List<Key> row : Layouts.rows(page)) {
            for (Key k : row) {
                if (k.label.equals(label)) {
                    return k;
                }
            }
        }
        throw new AssertionError("No key labelled " + label + " on " + page);
    }

    static Key byType(Page page, KeyType type) {
        for (List<Key> row : Layouts.rows(page)) {
            for (Key k : row) {
                if (k.type == type) {
                    return k;
                }
            }
        }
        throw new AssertionError("No " + type + " key on " + page);
    }

    static int count(Page page, KeyType type) {
        int n = 0;
        for (List<Key> row : Layouts.rows(page)) {
            for (Key k : row) {
                if (k.type == type) {
                    n++;
                }
            }
        }
        return n;
    }

    private static long clock = 10_000;

    static void tap(KeyboardController c, Key k) {
        clock += 2000; // taps far apart unless a test says otherwise
        c.press(k, clock);
    }

    static void tap(KeyboardController c, String label) {
        tap(c, byLabel(c, label));
    }

    static void hold(KeyboardController c, Key k) {
        clock += 2000;
        c.hold(k, clock);
    }

    /** Types a whole string on the letter page with ordinary taps. */
    static void typeText(KeyboardController c, String s) {
        for (char ch : s.toCharArray()) {
            if (ch == ' ') {
                tap(c, byType(Page.LETTERS, KeyType.SPACE));
            } else if (ch == '.') {
                tap(c, byType(Page.LETTERS, KeyType.PERIOD));
            } else {
                tap(c, byLabel(Page.LETTERS, String.valueOf(ch)));
            }
        }
    }
}
