package com.keypad.ime;

/** The old phone keypad letters, in the order they cycle. */
final class MultiTap {
    private MultiTap() {
    }

    static String lettersFor(char digit) {
        switch (digit) {
            case '1': return ".,?!'-@";
            case '2': return "abc";
            case '3': return "def";
            case '4': return "ghi";
            case '5': return "jkl";
            case '6': return "mno";
            case '7': return "pqrs";
            case '8': return "tuv";
            case '9': return "wxyz";
            default: return "";
        }
    }
}
