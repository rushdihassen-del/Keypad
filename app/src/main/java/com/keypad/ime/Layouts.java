package com.keypad.ime;

import java.util.ArrayList;
import java.util.List;

/** The rows of keys for every page. */
final class Layouts {
    /** Letters that get the light orange marker colour. */
    static final String ANCHORS = "tijfxb";

    private Layouts() {
    }

    static List<List<Key>> rows(Page page) {
        List<List<Key>> rows = new ArrayList<>();
        switch (page) {
            case NUMBERS:
                numberPad(rows);
                break;
            case SYMBOLS:
                symbols(rows);
                rows.add(bottomRow(page));
                break;
            default:
                qwerty(rows);
                rows.add(bottomRow(page));
                break;
        }
        return rows;
    }

    private static List<Key> letters(String s) {
        List<Key> list = new ArrayList<>();
        for (int i = 0; i < s.length(); i++) {
            String c = String.valueOf(s.charAt(i));
            list.add(ANCHORS.contains(c) ? Key.anchor(c) : Key.ch(c));
        }
        return list;
    }

    private static List<Key> chars(String s) {
        List<Key> list = new ArrayList<>();
        for (int i = 0; i < s.length(); i++) {
            list.add(Key.ch(String.valueOf(s.charAt(i))));
        }
        return list;
    }

    private static Key backspace(float weight) {
        return Key.fn(KeyType.BACKSPACE, "", weight, "Backspace");
    }

    private static Key enter(float weight) {
        return Key.fn(KeyType.ENTER, "Enter", weight, "Enter");
    }

    private static void qwerty(List<List<Key>> rows) {
        rows.add(letters("qwertyuiop"));

        List<Key> r2 = new ArrayList<>();
        r2.add(Key.spacer(0.5f));
        r2.addAll(letters("asdfghjkl"));
        r2.add(Key.spacer(0.5f));
        rows.add(r2);

        List<Key> r3 = new ArrayList<>();
        r3.add(Key.fn(KeyType.SHIFT, "", 1.5f, "Shift. Hold to change colours"));
        r3.addAll(letters("zxcvbnm"));
        r3.add(backspace(1.5f));
        rows.add(r3);
    }

    /** Few, big keys. The full stop and comma live on the letter keyboard. */
    private static void symbols(List<List<Key>> rows) {
        rows.add(chars("/\\+-!?;"));
        rows.add(chars(":_#&=()"));
        List<Key> r3 = new ArrayList<>(chars("\"'$%*"));
        r3.add(backspace(2f));
        rows.add(r3);
    }

    /** Same spots on the letter page and the special characters page. */
    private static List<Key> bottomRow(Page page) {
        List<Key> r = new ArrayList<>();
        if (page == Page.SYMBOLS) {
            r.add(Key.fn(KeyType.SYMBOLS, "ABC", 1f, "Back to letters"));
        } else {
            r.add(Key.fn(KeyType.SYMBOLS, "#+=", 1f, "Special characters"));
        }
        r.add(Key.fn(KeyType.NUMBERS, "123", 1f, "Number pad"));
        r.add(Key.at(1f));
        r.add(Key.fn(KeyType.SPACE, "space", 3f, "Space. Hold to change keyboard"));
        r.add(Key.period(1f));
        r.add(enter(2f));
        return r;
    }

    /** 1 2 3 on top, 0 under the 8. The extras sit in their own columns on the right. */
    private static void numberPad(List<List<Key>> rows) {
        rows.add(padRow(Key.ch("1"), Key.ch("2"), Key.ch("3"), Key.ch("+"), backspace(1f)));
        rows.add(padRow(Key.ch("4"), Key.ch("5"), Key.ch("6"), Key.ch("-"), Key.ch("*")));
        rows.add(padRow(Key.ch("7"), Key.ch("8"), Key.ch("9"), Key.ch("#"), Key.ch(",")));
        rows.add(padRow(Key.fn(KeyType.NUMBERS, "ABC", 1f, "Back to letters"), Key.ch("0"),
                Key.spacer(1f), Key.ch("."), enter(1f)));
    }

    /** Three number columns, a gap, then two extra columns. */
    private static List<Key> padRow(Key a, Key b, Key c, Key d, Key e) {
        List<Key> list = new ArrayList<>();
        list.add(a);
        list.add(b);
        list.add(c);
        list.add(Key.spacer(0.3f));
        list.add(d);
        list.add(e);
        return list;
    }
}
