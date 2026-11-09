package com.keypad.ime;

import java.util.ArrayList;
import java.util.List;

/** The rows of keys for every layout and page. Every page ends with the same control row. */
final class Layouts {
    private Layouts() {
    }

    static List<List<Key>> rows(LayoutMode layout, Page page) {
        List<List<Key>> rows = new ArrayList<>();
        switch (page) {
            case NUMBERS:
                numberPad(rows);
                break;
            case SYMBOLS1:
                symbols1(rows);
                break;
            case SYMBOLS2:
                symbols2(rows);
                break;
            default:
                if (layout == LayoutMode.PHONE) {
                    phoneLetters(rows);
                } else {
                    qwerty(rows);
                }
                break;
        }
        rows.add(controlRow(layout));
        return rows;
    }

    private static List<Key> chars(String s) {
        List<Key> list = new ArrayList<>();
        for (int i = 0; i < s.length(); i++) {
            list.add(Key.ch(String.valueOf(s.charAt(i))));
        }
        return list;
    }

    private static Key shift() {
        return Key.fn(KeyType.SHIFT, "", 1.5f, "Shift");
    }

    private static Key backspace(float weight) {
        return Key.fn(KeyType.BACKSPACE, "", weight, "Backspace");
    }

    private static void qwerty(List<List<Key>> rows) {
        rows.add(chars("qwertyuiop"));

        List<Key> r2 = new ArrayList<>();
        r2.add(Key.spacer(0.5f));
        r2.addAll(chars("asdfghjkl"));
        r2.add(Key.spacer(0.5f));
        rows.add(r2);

        List<Key> r3 = new ArrayList<>();
        r3.add(shift());
        r3.addAll(chars("zxcvbnm"));
        r3.add(backspace(1.5f));
        rows.add(r3);

        List<Key> r4 = new ArrayList<>();
        r4.add(Key.fn(KeyType.SYMBOLS, "?123", 2f, "Special characters"));
        r4.add(Key.ch(",", 2f));
        r4.add(Key.ch(".", 2f));
        r4.add(Key.ch("'", 2f));
        r4.add(Key.ch("?", 2f));
        rows.add(r4);
    }

    private static void phoneLetters(List<List<Key>> rows) {
        rows.add(row(Key.tap("1", ".,?!"), Key.tap("2", "ABC"), Key.tap("3", "DEF"), backspace(1f)));
        rows.add(row(Key.tap("4", "GHI"), Key.tap("5", "JKL"), Key.tap("6", "MNO"),
                Key.fn(KeyType.SHIFT, "", 1f, "Shift")));
        rows.add(row(Key.tap("7", "PQRS"), Key.tap("8", "TUV"), Key.tap("9", "WXYZ"),
                Key.fn(KeyType.SYMBOLS, "Symbols", 1f, "Special characters")));
        rows.add(row(Key.ch(","), Key.ch("."), Key.ch("?"),
                Key.fn(KeyType.NUMBERS, "123", 1f, "Number pad")));
    }

    private static void numberPad(List<List<Key>> rows) {
        rows.add(row(Key.ch("1"), Key.ch("2"), Key.ch("3"), backspace(1f)));
        rows.add(row(Key.ch("4"), Key.ch("5"), Key.ch("6"), Key.ch(".")));
        rows.add(row(Key.ch("7"), Key.ch("8"), Key.ch("9"), Key.ch("-")));
        rows.add(row(Key.ch("*"), Key.ch("0"), Key.ch("#"),
                Key.fn(KeyType.LETTERS, "ABC", 1f, "Letters")));
    }

    private static void symbols1(List<List<Key>> rows) {
        rows.add(chars("1234567890"));
        rows.add(chars("@#$%&*-+()"));

        List<Key> r3 = new ArrayList<>();
        r3.add(Key.fn(KeyType.SYMBOLS_MORE, "More", 1.5f, "More special characters"));
        r3.addAll(chars("!\"':;/?"));
        r3.add(backspace(1.5f));
        rows.add(r3);

        List<Key> r4 = new ArrayList<>();
        r4.add(Key.fn(KeyType.LETTERS, "ABC", 1.5f, "Letters"));
        r4.add(Key.fn(KeyType.NUMBERS, "Numbers", 2.5f, "Number pad"));
        r4.add(Key.ch(",", 1.5f));
        r4.add(Key.ch(".", 1.5f));
        r4.add(Key.ch("_", 1.5f));
        r4.add(Key.ch("=", 1.5f));
        rows.add(r4);
    }

    private static void symbols2(List<List<Key>> rows) {
        rows.add(chars("[]{}<>^`|\\"));
        rows.add(chars("€£¥¢°©®±×÷"));

        List<Key> r3 = new ArrayList<>();
        r3.add(Key.fn(KeyType.SYMBOLS_MORE, "Back", 1.5f, "Back to first special characters"));
        r3.addAll(chars("¿¡«»§¶~"));
        r3.add(backspace(1.5f));
        rows.add(r3);

        List<Key> r4 = new ArrayList<>();
        r4.add(Key.fn(KeyType.LETTERS, "ABC", 1.5f, "Letters"));
        r4.add(Key.fn(KeyType.NUMBERS, "Numbers", 2.5f, "Number pad"));
        r4.add(Key.ch(",", 1.5f));
        r4.add(Key.ch(".", 1.5f));
        r4.add(Key.ch("™", 1.5f));
        r4.add(Key.ch("µ", 1.5f));
        rows.add(r4);
    }

    private static List<Key> controlRow(LayoutMode layout) {
        String layoutLabel = layout == LayoutMode.STANDARD ? "Phone\npad" : "QWERTY";
        String layoutDesc = layout == LayoutMode.STANDARD
                ? "Switch to phone style letter pad" : "Switch to standard keyboard";
        return row(
                Key.fn(KeyType.COLOUR, "Colour", 1.2f, "Switch colours"),
                Key.fn(KeyType.LAYOUT, layoutLabel, 1.3f, layoutDesc),
                Key.fn(KeyType.SPACE, "space", 1.5f, "Space"),
                Key.fn(KeyType.ENTER, "Enter", 1.4f, "Enter, new line"),
                Key.fn(KeyType.SEND, "Send", 1.4f, "Send"));
    }

    private static List<Key> row(Key... keys) {
        List<Key> list = new ArrayList<>();
        for (Key k : keys) {
            list.add(k);
        }
        return list;
    }
}
