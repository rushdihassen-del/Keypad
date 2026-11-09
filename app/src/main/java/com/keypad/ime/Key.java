package com.keypad.ime;

/** One key on a page. Immutable. */
final class Key {
    final KeyType type;
    /** Big text on the key. May contain a newline for two lines. */
    final String label;
    /** Second line (the letters) on phone-pad keys, else empty. */
    final String sub;
    /** What a CHAR key types, or the digit of a MULTITAP key. */
    final String output;
    final float weight;
    final String description;

    private Key(KeyType type, String label, String sub, String output, float weight, String description) {
        this.type = type;
        this.label = label;
        this.sub = sub;
        this.output = output;
        this.weight = weight;
        this.description = description;
    }

    static Key ch(String s) {
        return ch(s, 1f);
    }

    static Key ch(String s, float weight) {
        return new Key(KeyType.CHAR, s, "", s, weight, spoken(s));
    }

    static Key tap(String digit, String sub) {
        String desc = digit;
        for (int i = 0; i < sub.length(); i++) {
            desc += " " + sub.charAt(i);
        }
        return new Key(KeyType.MULTITAP, digit, sub, digit, 1f, desc);
    }

    static Key fn(KeyType type, String label, float weight, String description) {
        return new Key(type, label, "", "", weight, description);
    }

    static Key spacer(float weight) {
        return new Key(KeyType.SPACER, "", "", "", weight, "");
    }

    private static String spoken(String s) {
        switch (s) {
            case ",": return "comma";
            case ".": return "full stop";
            case "'": return "apostrophe";
            case "?": return "question mark";
            case "!": return "exclamation mark";
            case "-": return "dash";
            case "_": return "underscore";
            case "@": return "at sign";
            case "#": return "hash";
            case "*": return "star";
            case "+": return "plus";
            case "=": return "equals";
            case "/": return "slash";
            case "\\": return "backslash";
            case ":": return "colon";
            case ";": return "semicolon";
            case "\"": return "quote";
            case "(": return "open bracket";
            case ")": return "close bracket";
            case "$": return "dollar";
            case "%": return "percent";
            case "&": return "and sign";
            default: return s;
        }
    }
}
