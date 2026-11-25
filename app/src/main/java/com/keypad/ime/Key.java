package com.keypad.ime;

/** One key on a page. Immutable. */
final class Key {
    final KeyType type;
    /** Big text on the key. Empty for keys that draw an icon. */
    final String label;
    /** What a CHAR key types. */
    final String output;
    final float weight;
    final String description;
    /** Marker keys (T I J F X B) get their own colour so the eye can find its place. */
    final boolean anchor;

    private Key(KeyType type, String label, String output, float weight, String description,
            boolean anchor) {
        this.type = type;
        this.label = label;
        this.output = output;
        this.weight = weight;
        this.description = description;
        this.anchor = anchor;
    }

    static Key ch(String s) {
        return new Key(KeyType.CHAR, s, s, 1f, spoken(s), false);
    }

    static Key anchor(String s) {
        return new Key(KeyType.CHAR, s, s, 1f, spoken(s), true);
    }

    static Key at(float weight) {
        return new Key(KeyType.AT, "@", "@", weight, "At sign. Hold for voice typing", false);
    }

    static Key period(float weight) {
        return new Key(KeyType.PERIOD, ".", ".", weight, "Full stop. Hold for comma", false);
    }

    static Key fn(KeyType type, String label, float weight, String description) {
        return new Key(type, label, "", weight, description, false);
    }

    static Key spacer(float weight) {
        return new Key(KeyType.SPACER, "", "", weight, "", false);
    }

    boolean isLetter() {
        return type == KeyType.CHAR && output.length() == 1 && Character.isLetter(output.charAt(0));
    }

    private static String spoken(String s) {
        switch (s) {
            case ",": return "comma";
            case ".": return "full stop";
            case "'": return "apostrophe";
            case "?": return "question mark";
            case "!": return "exclamation mark";
            case "-": return "minus";
            case "_": return "underscore";
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
