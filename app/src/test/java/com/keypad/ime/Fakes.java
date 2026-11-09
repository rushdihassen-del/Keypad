package com.keypad.ime;

import java.util.List;

/** Test helpers: a fake text box, fake saved settings, and ways to find keys. */
final class Fakes {
    private Fakes() {
    }

    static final class FakeEditor implements Editor {
        final StringBuilder text = new StringBuilder();
        String composing = "";
        int enters;
        int sends;

        @Override
        public void commit(String s) {
            composing = "";
            text.append(s);
        }

        @Override
        public void setComposing(String s) {
            composing = s;
        }

        @Override
        public void finishComposing() {
            text.append(composing);
            composing = "";
        }

        @Override
        public void backspace() {
            if (composing.isEmpty() && text.length() > 0) {
                text.deleteCharAt(text.length() - 1);
            }
        }

        @Override
        public void enter() {
            enters++;
        }

        @Override
        public void send() {
            sends++;
        }

        String visible() {
            return text.toString() + composing;
        }
    }

    static final class FakeSettings implements SettingsStore {
        Scheme scheme = Scheme.YELLOW_ON_BLACK;
        LayoutMode layout = LayoutMode.STANDARD;

        @Override
        public Scheme getScheme() {
            return scheme;
        }

        @Override
        public void setScheme(Scheme s) {
            scheme = s;
        }

        @Override
        public LayoutMode getLayout() {
            return layout;
        }

        @Override
        public void setLayout(LayoutMode l) {
            layout = l;
        }
    }

    static Key byLabel(KeyboardController c, String label) {
        for (List<Key> row : Layouts.rows(c.getLayout(), c.getPage())) {
            for (Key k : row) {
                if (k.label.equals(label)) {
                    return k;
                }
            }
        }
        throw new AssertionError("No key labelled " + label + " on " + c.getLayout() + "/" + c.getPage());
    }

    static Key byType(KeyboardController c, KeyType type) {
        return byType(c.getLayout(), c.getPage(), type);
    }

    static Key byType(LayoutMode layout, Page page, KeyType type) {
        for (List<Key> row : Layouts.rows(layout, page)) {
            for (Key k : row) {
                if (k.type == type) {
                    return k;
                }
            }
        }
        throw new AssertionError("No " + type + " key on " + layout + "/" + page);
    }

    static boolean hasType(LayoutMode layout, Page page, KeyType type) {
        for (List<Key> row : Layouts.rows(layout, page)) {
            for (Key k : row) {
                if (k.type == type) {
                    return true;
                }
            }
        }
        return false;
    }
}
